/**************************
 * This is the ARMInstruction class that converts the S Expression from an
 * RTL instruction and builds the corresponding ARM instruction.
 * 
 * @author Justin Herrera
 * @author James Kwan
 **/

import java.util.HashMap;

public class ARMInstruction {

   private enum SET_SIGNATURE {
      SET_MEM_REG,
      SET_REG_REG,
      SET_REG_MEM,
      SET_REG_CONST,
      SET_REG_PLUS,
      PLUS_REG_REG,
      PLUS_REG_CONST,
      PLUS_REG_F_CONST,
      COMPARE_CONST,
      SET_ADD_REG_REG,
      SET_PC_LABEL,
      SET_B_CONDITIONAL,
      SET_COND_JUMP,
      SET_ADD_STR,
      SET_RETURN_REG,
      UNKOWN
   };

   private static final int DST_LOC = 1;
   private static final int SRC_LOC = 2;
   private static final int ARG_LOC = 1;

   /* RTL attributes for ARM Instruction */
   private String insn;
   private InstructionType insn_type;
   private Instruction insn_destination;
   private Instruction insn_source;
   
   /* ARM Instruction attributes */
   private String arm_destination;
   private String arm_source;

   /* Finalized converted ARM string */
   private String arm_string = "";

   /* Register map and counts */
   private HashMap<String, String> reg_map;
   private Counter reg_count;
   private Boolean[] regs_avail;

  /*************************
   * ACCESSOR METHODS
   ************************/ 
   public String getArmDestination() { return this.arm_destination; }
   public String getArmSource() { return this.arm_source; }
   public String toString() { return this.arm_string; }

  /*************************
   * CONSTRUCTOR
   ************************/ 
   public ARMInstruction (Instruction insn, HashMap<String, String> reg_map, Counter reg_count, Boolean[] regs_avail) {
      this.insn = insn.getInsn();
      this.insn_type = insn.getType();
      this.regs_avail = regs_avail;

      this.reg_map = reg_map;
      this.reg_count = reg_count;

      this.insn_destination=(Instruction)(insn.getAttributes()).get(DST_LOC);
      //this.arm_destination = rtl2arm (this.insn_destination);

      /* This case checks to see the S Exp is of type SET. Otherwise, it could
         be type USE which does not have a source expression */
      /*
      
      */
      if (this.insn_type == InstructionType.SET) {
          this.insn_source = (Instruction) insn.getAttributes().get(SRC_LOC);
          //this.arm_source = rtl2arm (this.insn_source);
          this.arm_string = rtl2arm (insn);
      }
     
   }

  /*************************
   * ARMInstruction LOGIC
   ************************/ 
   /**
    * This is a driver method to convert and evaluate an RTL expression to
    * an appropriate ARM instruction
    * 
    * This assumes that the instruction passed in is only a destination,
    * source, or the parent of dst/src
    *
    * @param Instruction insn
    * @param Hashmap RegisterTable
    * @return String representation of an RTL expression
    */
   private String rtl2arm (Instruction insn) {
      InstructionType type = insn.getType();
      StringBuilder out = new StringBuilder();
      String dst_res, src_res, signature;
      Instruction dst, src;

      switch (type) {
         case NOTE:
            out.append(" NOTE");
            break;
         case SET: // NON-TERMINAL
            src_res = rtl2arm(this.insn_source);
            dst_res = rtl2arm(this.insn_destination);
            signature = evalSet(type, dst_res, src_res);
            out.append(signature); 
            break;
         case REG_SI:
            out.append(getRegister(insn, false));
            break;
         case REG_F_SI:
            out.append((String)insn.getAttributes().get(DST_LOC));
            break;
         case REG_I_SI:
            out.append("reg " + (String) insn.getAttributes().get(DST_LOC));
            break;
         case REG_CC: //this isnt a reg virtual reg... is condition code reg for booleans
            //out.append(getRegister(insn, false));
            out.append("CC " + (String)insn.getAttributes().get(DST_LOC));
            break;
         case MEM_C_SI: // NON-TERMINAL
            dst = (Instruction) insn.getAttributes().get(DST_LOC); 
            dst_res = rtl2arm(dst); 
            out.append("mem[" + dst_res + "]");
            break;
         case CONST_INT: // TERMINAL
            out.append("#" + (String) insn.getAttributes().get(ARG_LOC));
            break;
         case COMPARE_CC:
            dst = (Instruction) insn.getAttributes().get(DST_LOC); 
            src = (Instruction) insn.getAttributes().get(SRC_LOC); 
            dst_res = rtl2arm(dst); 
            src_res = rtl2arm(src);
            out.append("COMPARE " + dst_res + ", " + src_res);
            break;
         case PLUS: // NON-TERMINAL
            dst = (Instruction) insn.getAttributes().get(DST_LOC); 
            src = (Instruction) insn.getAttributes().get(SRC_LOC); 
            dst_res = rtl2arm(dst);  
            src_res = rtl2arm(src);
            //System.out.println(dst_res);
            //System.out.println(src_res);
            signature = evalSet(type, dst_res, src_res);
            //System.out.println("SIG: " + signature);
            out.append(signature);
            break;
          case PC:
            out.append("PC"); 
            break;
          case LABEL_REF:
            out.append(insn.getAttributes().get(DST_LOC));
            break;
          case IF_THEN_ELSE:
            dst = (Instruction) insn.getAttributes().get(DST_LOC); // le(condition) (cc) (const_int 0)
            src = (Instruction) insn.getAttributes().get(SRC_LOC); // label
            dst_res = rtl2arm(dst); //condition (ie -> le)
            src_res = rtl2arm(src); //jump destination (label xx)
            //signature = evalSet(type, dst_res, src_res);
            //System.out.println("RAWR " + signature);
            out.append("b" + dst_res + " .L" + src_res); // IF_THEN_ELSE
            break;
            //out.append()
          case LE: 
            out.append("le");
            break; 
          case USE: 
            //dst = (Instruction) insn.getAttributes().get(DST_LOC); 
            //src = (Instruction) insn.getAttributes().get(SRC_LOC); 
            //dst_res = rtl2arm(dst);  
            //src_res = rtl2arm(src);
            out.append("use");

         default:
            return "ARMInsn rtl2arm(): OPERATION NOT SUPPORTED\n";
      }

      return out.toString();
   }


  /**
   * Method to arrange ARM syntax depending on RTL signature.
   * 
   * @param InstructionType type
   * @param String destination
   * @param String source
   * @return String The result is a valid ARM instruction
   */
   private String evalSet (InstructionType type, String dst, String src) {
      
      StringBuilder arm_out = new StringBuilder();
      SET_SIGNATURE signature = determineSetSignature(type, dst, src);
 
      if (signature == SET_SIGNATURE.SET_MEM_REG) {

         System.out.println("Added to map! key dst: " + dst + " key src: " + src);
         reg_map.put(dst, src);
         reg_count.increment();

      } else if (signature == SET_SIGNATURE.SET_REG_CONST) { 

          arm_out.append("\tmov r2, " + src + "\n"); 
          arm_out.append("\tstr r2, " + dst + "\n");

      } else if (signature == SET_SIGNATURE.SET_REG_MEM) {

         /* 
          * EX: if we load the data at mem[X] into r5. We cannot overwrite this
          * register in later instructions.
          */
         
         arm_out.append("\tldr r2, " + reg_map.get(src) + "\n");
         arm_out.append("\tstr r2, " + dst + "\n");

      } else if (signature == SET_SIGNATURE.PLUS_REG_REG) {
         /* 
          * EX: if we had data from 111 and 112 that we loaded into r3 and r4
          * respectively, we would have to figure out in this call that it was
          * actually loaded into r3 and r4
          */
         
         arm_out.append("\tldr r1, " + src + "\n");
         arm_out.append("\tldr r2, " + dst + "\n");

      } else if (signature == SET_SIGNATURE.PLUS_REG_CONST) {

          //arm_out.append("[" + dst + ", " + src + "]");
         arm_out.append(dst + ", " + src);

      } else if (signature == SET_SIGNATURE.PLUS_REG_F_CONST) {

         //arm_out.append("[" + dst + ", " + src + "]");
         arm_out.append(dst + ", " + src);

      } else if (signature == SET_SIGNATURE.SET_REG_PLUS) {

         arm_out.append("\tadd r2, r2, " + src.substring(12) + "\n"); 
         arm_out.append("\tstr r2 " + dst + "\n"); 
    
      } else if (signature == SET_SIGNATURE.COMPARE_CONST) {
          //arm_out.append("dst: " + dst + "\n");
          //arm_out.append("src: " + src + "\n");
          arm_out.append("\tcmp r2, " + src.substring(20) + "\n"); //HACKKKKKKK
          //String[] compare_ops = src.substring(8).split(", ");
          //arm_out.append("RAWRAWRAWRAWRAWR " + compare_ops[0] + " sfafa " + compare_ops[1]);
      } else if (signature == SET_SIGNATURE.SET_ADD_REG_REG) {
          arm_out.append("\tadd r2, " + src + "\n");
          arm_out.append("\tstr r0, " + dst + "\n");
      } else if (signature == SET_SIGNATURE.SET_PC_LABEL) {
          arm_out.append("\tb .L" +  src + "\n");
      } else if (signature == SET_SIGNATURE.SET_B_CONDITIONAL) {
          //"if then else???"
      } else if (signature == SET_SIGNATURE.SET_COND_JUMP) {
          arm_out.append("\t" + src + "\n");
      } else if (signature == SET_SIGNATURE.SET_ADD_STR) {
          arm_out.append(src);
          arm_out.append("\tadd r0, r1, r2\n");
          arm_out.append("\tstr r0, " + dst + "\n");
      } else if (signature == SET_SIGNATURE.SET_REG_REG) {
          arm_out.append("\tldr r2, " + src + "\n");
          arm_out.append("\tstr r2, " + dst + "\n");
      } else if (signature == SET_SIGNATURE.SET_RETURN_REG) {
          arm_out.append("\tldr r2, " + src + "\n");
          arm_out.append("\tmov r0, r2\n");
      } else {

         System.out.println("\tTYPE: " + type + " dst: " + dst + " src: " + src);
         arm_out.append("\tARMInstruction evalSet(): Unsupported Set Signature!\n");

      }

      return arm_out.toString();
   }

  /**
   * MSethod to determine the RTL instruction signature based on the destination
   * and source S Expressions
   *
   * This method uses regular expressions to match a dst or source expression
   *
   * @param InstructionType type
   * @param String Destination
   * @param String Source
   * @return SET_SIGNATURE 
   */
   private SET_SIGNATURE determineSetSignature(InstructionType type, 
            String dst, String src) {
      
      String regex_register = "\\[fp, #-[0-9]+\\]";
      String regex_register_f = "[0-9]+";
      String regex_mem = "mem\\[.*\\]";
      String regex_const_int = "#.*";
      String regex_plus_reg_reg = "\\[.*\\], \\[.*\\]"; 
      String regex_plus_reg_const = "\\[.*\\], #.*";
      String regex_cond_compare = "COMPARE \\[.*\\], #.*";
      String regex_cc = "CC [0-9]+";
      //String regex_add_regs = "add regs \\[.*\\], \\[.*\\]";
      String regex_add_regs = "r[0-9]+, r[0-9]+";
      String regex_pc = "PC";
      String regex_label_rf = "[0-9]+";
      String regex_jump_cond = "le"; // maybe later ge, gt, lt
      String regex_if_then_else = "ble .L[0-9]+";
      String regex_set_plus = "\tldr r1, \\[.*\\]\n\tldr r2, \\[.*\\]\n"; 
      String regex_reg_return = "reg [0-9]";

      if (type == InstructionType.SET) {

         if (dst.matches(regex_register) && src.matches(regex_const_int)) {

            return SET_SIGNATURE.SET_REG_CONST;

         } else if (dst.matches(regex_mem) && src.matches(regex_register)) {

            return SET_SIGNATURE.SET_MEM_REG;

         } else if (dst.matches(regex_register) && src.matches(regex_mem)) {

            return SET_SIGNATURE.SET_REG_MEM;

         } else if (dst.matches(regex_register) && src.matches(regex_plus_reg_reg)) {

            return SET_SIGNATURE.SET_REG_PLUS; 
 
         } else if (dst.matches(regex_register)&&src.matches(regex_plus_reg_const)){

            return SET_SIGNATURE.SET_REG_PLUS;

         } else if(dst.matches(regex_cc) && src.matches(regex_cond_compare)) {
            
            return SET_SIGNATURE.COMPARE_CONST;
          
          } else if (dst.matches(regex_register) && src.matches(regex_add_regs)) {
              return SET_SIGNATURE.SET_ADD_REG_REG;
          } else if (dst.matches(regex_pc) && src.matches(regex_label_rf)) {
              return SET_SIGNATURE.SET_PC_LABEL;
          } else if (dst.matches(regex_jump_cond) && src.matches(regex_label_rf)) {
              return SET_SIGNATURE.SET_B_CONDITIONAL;
          } else if (dst.matches(regex_pc) && src.matches(regex_if_then_else)) {
              return SET_SIGNATURE.SET_COND_JUMP;
          } else if (dst.matches(regex_register) && src.matches(regex_set_plus)) {
              return SET_SIGNATURE.SET_ADD_STR;
          } else if (dst.matches(regex_register) && src.matches(regex_register)) {
              return SET_SIGNATURE.SET_REG_REG;
          } else if (dst.matches(regex_reg_return) && src.matches(regex_register)) {
              return SET_SIGNATURE.SET_RETURN_REG;
          }
 
      } else if (type == InstructionType.PLUS) {

         //System.out.println("PLUS- dst " + dst + " src: " + src);
         if (dst.matches(regex_register) && src.matches(regex_const_int)) {

            return SET_SIGNATURE.PLUS_REG_CONST;

         } else if (dst.matches(regex_register_f) && src.matches(regex_const_int)) {

            return SET_SIGNATURE.PLUS_REG_F_CONST;

         } else if (dst.matches(regex_register) && src.matches(regex_register)) {
            return SET_SIGNATURE.PLUS_REG_REG;

         } else if (dst.matches(regex_register) && src.matches(regex_plus_reg_reg)) {
            return SET_SIGNATURE.PLUS_REG_REG;

         }
 
      } else if (type == InstructionType.JUMP_INSN) {
          
      }

      return SET_SIGNATURE.UNKOWN;  
   }


  /** 
   * Method to convert a virtual register to its corresponding home value
   *
   * @param Instruction insn
   * @param boolean virtual-stack-vars
   * @return String representation of the register's home register 
   */
   private String getRegister (Instruction insn, boolean vsv) {
      String reg_num = (String) insn.getAttributes().get(ARG_LOC);
      String t_reg_num = reg_map.getOrDefault(reg_num, Integer.toString(1001));
      return t_reg_num;
   }

   private void reset_regs(Boolean[] regs) {
      for(int i = 0; i < regs.length; i++) {
        regs[i] = true;
      }
   }
}
