import java.util.Stack;
import java.io.FileWriter;
import java.util.ArrayList;

public class Instruction {

   private static final int OPR_LOC = 5;
   private static final int SRC_LOC = 1;
   private static final int DST_LOC = 2;

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
   public Instruction getOperation() {
      Instruction insn = (Instruction) this.attributes.get(OPR_LOC);
      InstructionType type = insn.getType();
      return insn;
   }
   public Instruction getSource() {
      if (this.insn_type != InstructionType.SET || 
          this.insn_type != InstructionType.PLUS ||
          this.insn_type != InstructionType.REG) {
         System.out.println("ERROR: CANNOT GET SOURCE IN CURRENT OBJECT!");
         System.exit(0);
      } 

      return (Instruction) attributes.get(SRC_LOC);
   } 

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
         //add to AL


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
            insn_type = InstructionType.REG;
            break;
         case "plus:SI":
            insn_type = InstructionType.PLUS;
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
      } catch (Exception e) {
         basic_block_num = -1;
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

   public static void main(String[] args) {
      String test = "(insn 18 17 19 4 (set (reg:SI 116) (plus:SI (reg:SI 117) (reg:SI 118))) \"fib.c\":8 -1 (nil))";
      String test_2 = "(a b c (d e f))";
      System.out.println(test);
      Instruction in = new Instruction(test);
      in.parseSExpressions();
      in.setTypes();
      in.setBasicBlock();
      in.print_type_and_bb();
      System.out.print("{");
      in.printAll();
      System.out.print("}");
      System.out.println();
   }
   


}
