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
import java.util.Iterator;

public class GenerateAssembly {
    
    private static final int BB_START = 2;
    private static ArrayList<LinkedList<Instruction>> graph = new ArrayList<>();
 
    public static void main (String[] args) {

        // Command line check for file
        if (args.length != 2) {
            System.out.print("Please enter a expand file as first input ");
            System.out.print("param and asm file as second param.\n");
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

        /*
        * 1st pass
        *   Find the largest register size
        *   Find the smallest register size
        *   Populate and generate a set of known registers
        */

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
        
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        * 2nd pass
        *   For each instruction, we generate the corresponding assembly insn
        *
        *
        */
        for (int i = BB_START; i < graph.size(); i++) {     
            for (Instruction insn : graph.get(i)) {
                InstructionType type = insn.getType();
                switch (type) {
                    case NOTE:
                        System.out.println("Note switch");
                        break;
                    case INSN:
                        System.out.println("INSN switch");
                        break;
                    default:
                        System.out.println("not supported instruction");
                        break; 
                }
            }
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
}
