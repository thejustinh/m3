import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;

public class Driver
{
    private static final int BB_START = 2;
    private static ArrayList<LinkedList<Instruction>> graph = new ArrayList<>();

    public static void main (String[] args) {

        // Command line check for file
        if (args.length != 2) {
            System.out.print("Please enter a expand file as first input ");
            System.out.print("param and dot file as second param.\n");
            return;
        }

        File file = new File(args[0]);

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
            StringBuilder object = new StringBuilder();
            Stack<Character> stack = new Stack<Character>();

            char c;

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
                        storeInstruction(obj);
                    }
                    object = new StringBuilder();
                }                   
            }
        
        writeDotFile(graph, args[1]);

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

    private static void storeInstruction(Instruction obj) {
        int basicBlock = obj.getBasicBlock();

        if (basicBlock < 0) return;
        // Initialize ArrayList indexes that have not been set
        for (int i = graph.size(); i <= basicBlock; i++ ) { 
            graph.add(new LinkedList<Instruction>());
        }

        graph.get(basicBlock).add(obj);
    }

    private static void printGraph() {
        for (int i = 0; i < graph.size(); i++) {
            //System.out.print("Basic Block: ", i);
            //graph.get(i)
        }
    }

    public static void writeDotFile(ArrayList<LinkedList<Instruction>> list, 
                                    String filename) {
        //key -> insn line, val -> block num
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        //map.put(Integer.toString(0), 0);
        try {
            File file = new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("digraph \"test\" {\n");
            writer.write("overlap=false;\n");
            writer.write("subgraph \"cluster_main\" {\n");
            writer.write("    label=\"main ()\";\n");
            writer.write("    bb_0 [shape=Mdiamond,label=\"ENTRY\"];\n");

            int last_block = 2;

            for(int i = 0; i < list.size(); i++) {
                if(list.get(i).size() > 0) {
                    writer.write("\n");
                    writer.write("      bb_"+i+" [shape=record, label=\"\n{");
                    LinkedList<Instruction> block = list.get(i);
                    if(block.size() > 0) {
                        Iterator<Instruction> iter = block.listIterator(0);
                        while(iter.hasNext()) {
                            Instruction insn = iter.next();
                            /*
                            if(insn.getTypes() == Instruction.INSN_TYPE.JUMP_INSN) {
                                
                            }
                            */
                            map.put(insn.getCurrID(), insn.getBasicBlock());
                            writer.write(insn.getCurrID() + ":\\ ");
                            insn.writeInsnIntoDot(writer);
                            writer.write("\\l\\\n");
                            if(iter.hasNext()) {writer.write("|");}
                        }
                    }
                    writer.write("}\"];\n");
                    if(i > last_block) {last_block = i;}
                }
            }
            writer.write("\n   bb_1 [shape=Mdiamond,label=\"EXIT\"];\n\n");
            writer.write("   bb_0 -> bb_2;\n");
            bindBlocks(list, map, writer);
            writer.write("   bb_" + last_block +" -> bb_1;\n");
            writer.write("}\n");
            writer.write("}\n");
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            System.out.println("you dun goofed");
        }
    }

    public static void bindBlocks(ArrayList<LinkedList<Instruction>> list,
                                  HashMap<String, Integer> map, FileWriter writer) {
        
        int block_num = BB_START;
        boolean[] visited = new boolean[list.size()];
        bindHelper(list, map, writer, visited, block_num);        
    }

    private static void bindHelper(ArrayList<LinkedList<Instruction>> list,
                                  HashMap<String, Integer> map, FileWriter writer, boolean[] visited,
                                  int basic_Block_num) {
        if (basic_Block_num == 0) {
            return;
        }

        if(visited[basic_Block_num]) {
            return;
        }
        LinkedList<Instruction> all_insn = list.get(basic_Block_num);
        Instruction last_insn = all_insn.peekLast();
        try {
            if(last_insn.getTypes() == Instruction.INSN_TYPE.JUMP_INSN) {
                //get jump label...
                String jump_label = getJumpLabel(last_insn);
                if(map.containsKey(jump_label)) {
                    writer.write("   bb_" + basic_Block_num + " -> " + "bb_" + map.get(jump_label) + ";\n");
                    visited[basic_Block_num] = true;
                    bindHelper(list, map, writer, visited, map.get(jump_label));
                }
        }
        
            String next_id = last_insn.getNextID();
            if(map.containsKey(next_id)) {
                int next_block = map.get(next_id);
                visited[basic_Block_num] = true;    
                writer.write("   bb_" + basic_Block_num + " -> " + "bb_" + next_block + ";\n");
                bindHelper(list,map,writer,visited, next_block);
                
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }   
    private static String getJumpLabel(Instruction insn) {
        ArrayList<Object> attributes = insn.getAttributes();
        String str = (String)attributes.get(attributes.size() - 1);
        //System.out.println("Printing jump label: " + str);
        return str;
    }
}
