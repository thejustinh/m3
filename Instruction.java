import java.util.Stack;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.StringBuilder;

public class Instruction {

    private static final int OPR_LOC = 5;
    private static final int SRC_LOC = 2;
    private static final int DST_LOC = 1;

    private String insn;
    private ArrayList<Object> attributes;
    private InstructionType insn_type;
    private int basic_block_num;
    private int curr_id;
    private int prev_id;
    private int next_id;

/****************************
 * CONSTRUCTOR
 ***************************/
    public Instruction(String insn) {
        this.insn = insn;
        attributes = new ArrayList<Object>();
    }

/****************************
 * ACESSOR METHODS
 ***************************/
    public String getInsn() { return insn; }
    public ArrayList<Object> getAttributes() { return attributes; }
    public InstructionType getType() { return insn_type; }
    public int getBasicBlock () { return basic_block_num; }
    public String getCurrID() { return Integer.toString(curr_id); }
    public String getNextID() { return Integer.toString(next_id); }
    public String getPrevID() { return Integer.toString(prev_id); }
    public Instruction getSExp() {
        Instruction insn = (Instruction) this.attributes.get(OPR_LOC);
        return insn;
    }
    public Instruction getInstance() { return this;}
/****************************
 * SETTER METHODS
 ***************************/

    public void setBasicBlock (int basic_block_num) { this.basic_block_num = basic_block_num; }
    public void setCurrID(int curr_id) { this.curr_id = curr_id; }
    public void setNextID(int next_id) { this.next_id = next_id; }
    public void setPrevID(int prev_id) { this.prev_id = prev_id; }

/****************************
 * Getting Defs and Uses
 ***************************/

   /**
    * Method to get the set of virtual register definitions from an RTL
    * Instruction
    *
    * @return HashSet<String> defs
    **/
    public HashSet<String> getDefs() {

        HashSet<String> defs = new HashSet<>();;

        /* Get SExpression from RTL Instruction */
        Instruction sexp = (Instruction) this.attributes.get(OPR_LOC);
        setInstruction(sexp);
    
        /* Get the DST location from the S Expression. This is our Def! */
        Instruction dst = (Instruction) sexp.attributes.get(DST_LOC);
        setInstruction(dst);

        /* Get the Definitions from the DST sub S Expression */
        HashSet<String> registers = getVRegisters(dst);

        /* Add this virtual register definition to the defs hashset */
        defs.addAll(registers);

        return defs;

    } 

   /**
    * Method to get the set of virtual regiseter uses from an RTL Instruction
    *
    * @return HashSet<String> uses
    **/
    public HashSet<String> getUses() {

        HashSet<String> uses = new HashSet<>();

        /* Get SExpression from RTL Instruction */
        Instruction sexp = (Instruction) this.attributes.get(OPR_LOC);
        setInstruction(sexp);
 
        /* Get the SRC location from the S Expression. */
        /* These will countain our uses */
        Instruction src = (Instruction) sexp.attributes.get(SRC_LOC);
        setInstruction(src);

        /* Get the Uses from the SRC sub S Expression */
        HashSet<String> registers = getVRegisters(src);

        /* Add this virtual register definition to the uses hashset */
        uses.addAll(registers);

        return uses;

    } 

          
   /**
    * Helper method to retrieve registers used in subSexpressions (src or dst)
    *
    * @param Instruction sub S Expression
    * @return HashSet<String> virtual registers
    **/
    private HashSet<String> getVRegisters(Instruction subExpression) {

        HashSet<String> registers = new HashSet<>();

        InstructionType type = subExpression.getType();

        switch (type) {
            case REG_SI:
                registers.add((String)subExpression.getAttributes().get(DST_LOC));
                break;
            case REG_F_SI:
                registers.add((String)subExpression.getAttributes().get(DST_LOC));
                break;
            case REG_I_SI:
                registers.add((String)subExpression.getAttributes().get(DST_LOC));
                break;
            case MEM_C_SI:
                StringBuilder mem_label = new StringBuilder();

                /* Get Sub Expressions for Plus, Register, and CONST */
                Instruction plus = (Instruction) subExpression.getAttributes().get(DST_LOC);
                setInstruction(plus);
                Instruction reg_exp = (Instruction) plus.getAttributes().get(DST_LOC);
                setInstruction(reg_exp);
                Instruction offset_exp = (Instruction) plus.getAttributes().get(SRC_LOC);
                setInstruction(offset_exp);

                /* Get the values from the sub expressions for register and const */
                String reg_val = (String) reg_exp.getAttributes().get(DST_LOC);
                String offset_val = (String) offset_exp.getAttributes().get(DST_LOC);
                
                /* Construct memory string */
                mem_label.append("mem[" + reg_val + ", #" + offset_val + "]");
                registers.add(mem_label.toString()); 
                break;
            case PLUS:
                Instruction dst_exp = (Instruction) subExpression.getAttributes().get(DST_LOC);
                setInstruction(dst_exp);
                Instruction src_exp = (Instruction) subExpression.getAttributes().get(SRC_LOC);
                setInstruction(src_exp);

                registers.addAll(getVRegisters(dst_exp));
                registers.addAll(getVRegisters(src_exp));
                break;
            default:
                //System.out.println("Instruction.java: Default case hit for type: " + type);
                break;
        }
        
        return registers;
    }

    public static void setInstruction (Instruction insn) {
        insn.parseSExpressions();
        insn.setTypes();
        insn.setBasicBlock();
    }

/****************************
 * HELPER METHODS
 ***************************/


/****************************
 * INSTRUCTION LOGIC METHODS
 ***************************/
    public void parseSExpressions() {
        StringBuilder word = new StringBuilder();
        for(int i = 1; i < insn.length(); i++) {
            char ch = insn.charAt(i);

            if(ch == '\"' || ch == '>' || ch == '<') {
                word.append("\\");
            }

            if(Character.isWhitespace(ch)) {
                if(word.length() > 0) {
                    attributes.add(word.toString());   
                    word.setLength(0);
                }
            }
            else if(ch == '(') {
                //find matching closing paren...
                int end_index = findIndexOfClosingParen(i, insn);
                //new insn obj
                String s_exp = insn.substring(i, end_index+1);
                Instruction s_exp_obj = new Instruction(s_exp);
                //call new obj.parseSExpression
                s_exp_obj.parseSExpressions();
                attributes.add(s_exp_obj);

                i = end_index + 1;
            }
            else if (ch == ')') {
                attributes.add(word.toString());   
                word.setLength(0);
            }
            else {
                word.append(ch);
            }
        }
    }

    private static int findIndexOfClosingParen(int num, String str){
        Stack<Character> stack = new Stack<Character>();
        if (str.charAt(num) != '(') {
            System.out.println("error");
            return 0;
        }
        stack.push(str.charAt(num));

        for(int i = num + 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == '(') {
                stack.push(ch);
            }
            else if(ch == ')') {
                stack.pop();
                if (stack.empty()) {
                    return i;
                }
            }
        }
        return 0;

    }

    public void setTypes() {
        String type = (String)attributes.get(0);

        switch (type) {
            case "insn":
                insn_type = InstructionType.INSN;
                break;
            case "note": 
                insn_type = InstructionType.NOTE;
                break;
            case "barrier": 
                insn_type = InstructionType.BARRIER;
                break;
            case "code_label": 
                insn_type = InstructionType.CODE_LABEL;
                break;
            case "jump_insn": 
                insn_type = InstructionType.JUMP_INSN;
                break;
            case "set":
                insn_type = InstructionType.SET;
                break;
            case "reg:SI":
                      insn_type = InstructionType.REG_SI;
                      break;
            case "reg/f:SI":
                        insn_type = InstructionType.REG_F_SI;
                        break;
            case "reg/i:SI":
                        insn_type = InstructionType.REG_I_SI;
                        break;
            case "reg:CC":
                      insn_type = InstructionType.REG_CC;
                      break;
            case "compare:CC":
                          insn_type = InstructionType.COMPARE_CC;
                          break;
            case "mem/c:SI":
                        insn_type = InstructionType.MEM_C_SI;
                        break;
            case "plus:SI":
                       insn_type = InstructionType.PLUS;
                       break;
            case "const_int":
                       insn_type = InstructionType.CONST_INT;
                       break;
            case "label_ref":
                       insn_type = InstructionType.LABEL_REF;
                       break;
            case "pc":
                       insn_type = InstructionType.PC;
                       break;
            case "if_then_else":
                       insn_type = InstructionType.IF_THEN_ELSE;
                       break;
            case "le":  
                       insn_type = InstructionType.LE;
                       break;
            case "use": 
                       insn_type = InstructionType.USE;
                       break;
            case "call_insn":
                       insn_type = InstructionType.CALL;
                       break;
            case "symbol_ref:SI":
                             insn_type = InstructionType.SYMBOL_REF;
                             break;
            default: 
                             insn_type = InstructionType.DEFAULT;
        }


        for (int i = 1; i < attributes.size(); i++) {
            if(attributes.get(i) instanceof Instruction) {
                ((Instruction)attributes.get(i)).setTypes();
            }
        }
    }

    public void setBasicBlock() {
        try {
            curr_id = Integer.parseInt(((String)attributes.get(1))); 
            prev_id = Integer.parseInt(((String)attributes.get(2))); 
            next_id = Integer.parseInt(((String)attributes.get(3))); 
            basic_block_num = Integer.parseInt(((String)attributes.get(4)));
            for(int i = 0; i < attributes.size(); i++) {
                Object temp = attributes.get(i);
                if(temp instanceof Instruction) {
                    setRestBasicBlock((Instruction)temp, curr_id, prev_id, next_id, basic_block_num);
                }
            } 
        } catch (Exception e) {
            basic_block_num = -1;
        }

    }

    private void setRestBasicBlock(Instruction temp, int curr_id, int prev_id, int next_id, int basic_block_num) {


        temp.setBasicBlock(basic_block_num);
        temp.setNextID(next_id);
        temp.setCurrID(curr_id);
        temp.setPrevID(prev_id);

        for(int i = 0; i < temp.getAttributes().size(); i++) {
            Object elem = temp.getAttributes().get(i);
            if(elem instanceof Instruction) {
                Instruction instruction = (Instruction)elem;
                setRestBasicBlock(instruction, curr_id, prev_id, next_id, basic_block_num);
            } 
        } 
    }

    public void print_type_and_bb() {
        System.out.println(insn_type);
        System.out.println(basic_block_num);

    }

    public void printAll(){

        for (int i = 0; i < attributes.size(); i++) {
            Object temp = attributes.get(i);
            if (temp instanceof String) {
                String str = (String) temp;
                System.out.print(str);
            }
            if (temp instanceof Instruction) {
                Instruction instr = (Instruction) temp;
                System.out.print("{");
                instr.printAll();
                System.out.print("}");

            }
            if(i + 1 < attributes.size()) {
                System.out.print(" ");
            }
        }
    }


    public void writeInsnIntoDot(FileWriter writer) {
        try {for (int i = 0; i < attributes.size(); i++) {
            Object temp = attributes.get(i);
            if (temp instanceof String) {
                String str = (String) temp;
                writer.write(str);
            }
            if (temp instanceof Instruction) {
                Instruction instr = (Instruction) temp;
                writer.write("\\{");
                instr.writeInsnIntoDot(writer);
                writer.write("\\}");

            }
            if(i + 1 < attributes.size()) {
                writer.write("\\ ");
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findRegisters(HashMap<String, String> reg_map, Counter counter) {
        if (insn_type == InstructionType.REG_SI) {
            String reg_num = (String)attributes.get(DST_LOC);

            if(!reg_map.containsKey(reg_num)) {
                reg_map.put(reg_num, "[fp, #-" + Integer.toString(counter.getCount() * 4) + "]");
                counter.increment();
                System.out.println("eEG_NUM: " + reg_num + " Location: " + reg_map.get(reg_num));
            }

            return;
        }
        /* 
           else if (insn_type == InstructionType.REG_F_SI) {
           counter.increment();
           return;
           } 
           */
        for(int i = 0; i < attributes.size(); i++) {
            Object temp = attributes.get(i);
            if(temp instanceof Instruction) {
                Instruction instr = (Instruction)temp;
                instr.findRegisters(reg_map, counter);
            }
        }
    }

    public void storeJumpLabel (HashMap<String, String> jump_label_map) {
        StringBuilder label = new StringBuilder();
        label.append(".L" + (String)attributes.get(attributes.size()-1));
        jump_label_map.put((String)attributes.get(attributes.size()-1), label.toString());
        //System.out.println("label: " + attributes.get(attributes.size()-1) + " output: " + label.toString());
        return;
    }

    public String getCallFunc() {
        System.out.println("point 1");
        if(insn_type == InstructionType.SYMBOL_REF) {
            Instruction temp = (Instruction)attributes.get(1);
            String target = (String)temp.getAttributes().get(0);
            return target.replaceAll("\\\\\"", "");
        }
        for (int i = 0; i < attributes.size(); i++){
            Object temp = attributes.get(i);
            if(temp instanceof Instruction) {
                Instruction temp_insn = (Instruction)temp;
                //System.out.println("recurse");
                return temp_insn.getCallFunc();
            }
        }
        return "mistake";

    }

    public static void main(String[] args) {
        HashMap<String, String> reg_map = new HashMap<>(); 
        Counter virtual_reg_count = new Counter();
        Boolean[] rawr = {true, true, true};
        //String test = "(jump_insn 32 31 33 5 (set (pc) (if_then_else (le (reg:CC 100 cc) (const_int 0 [0])) (label_ref 29) (pc))) \"fib.c\":7 -1 (nil) -> 29)";
        String test_2 = "(call_insn 13 12 14 2 (parallel [ (call (mem:SI (symbol_ref:SI (\"printint\") [flags 0x41]  <function_decl 0x768ba180 printint>) [0 printint S4 A32]) (const_int 0 [0])) (use (const_int 0 [0])) (clobber (reg:SI 14 lr)) ]) addwithprint.c:5 -1 (nil) (expr_list (clobber (reg:SI 12 ip)) (expr_list:SI (use (reg:SI 0 r0)) (nil))))";
        //System.out.println(test);
        Instruction in = new Instruction(test_2);
        in.parseSExpressions();
        in.setTypes();
        in.setBasicBlock();
        in.findRegisters(reg_map, virtual_reg_count);
        //in.print_type_and_bb();
        Instruction temp = (Instruction)in.getAttributes().get(5);
        Instruction temp_2 = (Instruction)temp.getAttributes().get(2);
        System.out.print("{");
        temp.printAll();
        System.out.print("}");
        System.out.println();
        System.out.println(in.getCurrID());
        System.out.println(in.getNextID());
        System.out.println(in.getPrevID());
        System.out.println(in.getBasicBlock());
        System.out.println(temp.getCurrID());
        System.out.println(temp.getNextID());
        System.out.println(temp.getPrevID());
        System.out.println(temp.getBasicBlock());
        //System.out.print(rawr[2]);
        //helper(rawr);
        //System.out.print(rawr[2]);
    }

    private static void helper(Boolean[] rawr) {

        rawr[2] = false;
    }



}
