/**
 * CSC 431 - Milestone 2
 *
 * This is a Driver file to generate assembly given an RTL file
 * @author Justin Herrera
 * @author James Kwan
 **/

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GenerateAssembly {
    
    private static final int BB_START = 2;
    private static ArrayList<LinkedList<Instruction>> graph = new ArrayList<>();
    private static HashMap<String, String> reg_map = new HashMap<>();
    private static HashMap<String, String> jump_label_map = new HashMap<>();
    private static Counter reg_count = new Counter();
    private static boolean[] visited; 
    private static HashSet<String> basic_block_set = new HashSet<>();
    private static HashMap<String, String> bb_map = new HashMap<>(); //key: insn; val: bb


    public static void main (String[] args) {

        // Command line check for file
        if (args.length != 2) {
            System.out.print("Please enter a expand file as first input ");
            System.out.print("param and asm file as second param.\n");
            return;
        }

        File file = new File(args[0]);
        File file2 = new File(args[1]);

        // Check to see if file exists
        if (!file.exists()) {
            System.out.println(args[0] + " does not exist");
            return;
        }
    
        // Check to see if we can open the file for reading
        if (!(file.isFile() && file.canRead())) {
            System.out.println("Cannot read file " + file.getName());
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            file.createNewFile();
            FileWriter writer = new FileWriter(file2);

            /* Method to populate graph from RTL file */        
            generateGraph(fis); // should have registers calculated by now
            visited = new boolean[graph.size()];
            /* Write initialize lines to ASM file*/
            writer.write("\t.arch armv6\n\t.text\n\t.global main\n");

            writer.write("main:\n"); // TODO: Function name hardcoded
            writer.write("\tpush {fp, lr}\n");
            writer.write("\tmov fp, sp\n");
            writer.write("\tsub sp, sp, #"+ Integer.toString(reg_count.getCount() * 4) + "\n");
            //writer.write("\tsub sp, sp, #100\n");
            /* Loop through graph to convert each RTL insn to ARM insn and 
               write to ASM file. */
            /*
            for (int i = BB_START; i < graph.size(); i++) {     
                for (Instruction insn : graph.get(i)) {
                    String out = armify(insn, writer);
                    System.out.println(out);
                }
            }
            */
            armifyBlocks(writer, BB_START);

            writer.write("\tmov sp, fp\n");
            writer.write("\tpop {fp, pc}\n");



            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

   /**
    * Method to convert RTL instruction into ARM instruction
    *
    * @param Instruction insn 
    * @return String insn in ARM format
    */
    private static String armify (Instruction insn, FileWriter writer) {
        InstructionType type = insn.getType();
        String basicBlock = Integer.toString(insn.getBasicBlock());
        if(!basic_block_set.contains(basicBlock)) {
            try {
                writer.write("BB_" + basicBlock + ":\n");
                basic_block_set.add(basicBlock);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StringBuilder out = new StringBuilder();
        ARMInstruction arm;
        Instruction s_exp;
        out.append(insn.getCurrID());
        switch (type) {
            case NOTE:
                out.append(" NOTE\n");
                break;
            case INSN:
                out.append(" INSN\n");
                s_exp = insn.getSExp();
                System.out.println("TYPE OF S_EXP: " + s_exp.getType());
                if(s_exp.getType() == InstructionType.USE) {
                    break;
                }
                arm = new ARMInstruction(s_exp, reg_map, reg_count, bb_map);
                
                out.append("\tDST: "+arm.getArmDestination() + "\n"); 
                out.append("\tSRC: " + arm.getArmSource() + "\n");
                out.append(arm.toString() + "\n");
                try {
                    writer.write(arm.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case JUMP_INSN:
                out.append(" JUMP\n");
                
                s_exp = insn.getSExp(); // set pc..
                arm = new ARMInstruction(s_exp, reg_map, reg_count, bb_map);
                out.append("\tDST: "+arm.getArmDestination() + "\n"); 
                out.append("\tSRC: " + arm.getArmSource() + "\n");
                out.append(arm.toString() + "\n");
                try {
                    writer.write(arm.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //out.append("\tDST: "+arm.getArmDestination() + "\n"); 
                //out.append("\tSRC: " + arm.getArmSource() + "\n");

                
                break;
            case CODE_LABEL:
                
                out.append(" CODE_LABEL\n");
                /*
                try {
                    writer.write(".L" + (String)insn.getAttributes().get(1) + ":\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //out.append(jump_label_map.get(insn.getAttributes().get(1)) + ":\n");
                */
                break;
            case USE: 
                
                break;
            case CALL:
                try {

                    writer.write("\tbl " + insn.getCallFunc() + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                out.append(" GenerateAssembly: OPERATION NOT SUPPORTED\n");
                break; 
        }
        return out.toString();
    }

   /**
    * Method to read in an RTL file, parse each instruction, and populate a
    * graph of RTL instructions.
    * 
    * @param FileInputStream
    */
    private static void generateGraph(FileInputStream fis) {
        StringBuilder object = new StringBuilder();
        Stack<Character> stack = new Stack<Character>();

        char c;

        try {
            while (fis.available() > 0) {
                c = (char) fis.read();

                if (c == '(') 
                    stack.push(c); 
                if (c == ')') 
                    stack.pop();   
                
                object.append(c);

                if (stack.empty() && !object.toString().equals("\n")) {
                    String result = object.toString().trim();
                    if (isValidInsn(result)) {
                        Instruction obj = new Instruction(result);
                        obj.parseSExpressions();
                        obj.setTypes();
                        obj.setBasicBlock();
                        obj.findRegisters(reg_map, reg_count);
                        storeInstruction(obj);
                        bb_map.put(obj.getCurrID(), Integer.toString(obj.getBasicBlock()));
                        System.out.println("ID: " + obj.getCurrID() + " BB: " + obj.getBasicBlock() + " table?: " + bb_map.getOrDefault(obj.getCurrID(), "RAWRAWRAW"));
                    }
                    object = new StringBuilder();
                }                   
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /**
    * Method to verify if a string is in the form of a lisp instruction.
    *
    * @param String insn
    @ @return boolean val: returns true on success, false otherwise
    */    
    private static boolean isValidInsn(String insn) {
        if (insn.length() < 2)
            return false;

        return (insn.charAt(0) == '(' && insn.charAt(insn.length()-1) == ')');
    }

   /**
    * Method to verify if a string is in the form of a lisp instruction.
    *
    * @param Instruction obj
    @ @return void
    */    
    private static void storeInstruction(Instruction obj) {
        int basicBlock = obj.getBasicBlock();

        if (basicBlock < 0) return;
        // Initialize ArrayList indexes that have not been set
        for (int i = graph.size(); i <= basicBlock; i++ ) { 
            graph.add(new LinkedList<Instruction>());
        }

        graph.get(basicBlock).add(obj);
    }

    private static void armifyBlocks(FileWriter writer, int basic_block_num) {
        if (basic_block_num <= 0) {
            return;
        }
        if(visited[basic_block_num]) {
            return;
        }

        LinkedList<Instruction> all_insn = graph.get(basic_block_num);
        Instruction last_insn = all_insn.peekLast();
        


        System.out.println("BB: " + last_insn.getBasicBlock());
        
        armifyBlock(all_insn, writer);

        if(last_insn.getType() == InstructionType.JUMP_INSN) {
            String jump_label = getJumpLabel(last_insn);
            if(bb_map.containsKey(jump_label)) {
                visited[basic_block_num] = true;
                armifyBlocks(writer, Integer.parseInt(bb_map.get(jump_label)));
            }
        }

        String next_id = last_insn.getNextID();
        int next_block = Integer.parseInt(bb_map.get(next_id));
        visited[basic_block_num] = true;    
        armifyBlocks(writer, next_block);
    }

    private static String getJumpLabel(Instruction insn) {
        ArrayList<Object> attributes = insn.getAttributes();
        String str = (String)attributes.get(attributes.size() - 1);
        //System.out.println("Printing jump label: " + str);
        return str;
    }

    private static void armifyBlock(LinkedList<Instruction> all_insn, FileWriter writer) {
        String out = "" ; 
        for(int i = 0; i < all_insn.size() - 1; i++) {
            Instruction temp = all_insn.get(i);
            out = armify(temp, writer);
            System.out.println(out);
        }

        //last insn
        Instruction last_insn = all_insn.get(all_insn.size() - 1);
        if(last_insn.getType() == InstructionType.JUMP_INSN) {
            out = armify(last_insn, writer);
            System.out.println(out);
        }
        else if (Integer.parseInt(last_insn.getNextID()) > 0) {
            try {
                String next_block = bb_map.get(last_insn.getNextID());
                out = armify(last_insn, writer);
                System.out.println(out);
                writer.write("\tb BB_" + next_block + "\n");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

}
