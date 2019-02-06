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
      SET_REG_MEM,
      SET_REG_CONST,
      SET_REG_PLUS,
      PLUS_REG_REG,
      PLUS_REG_CONST,
      PLUS_REG_F_CONST,
      JUMP_COMPARE_CONST,
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
   private String arm_string;

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
      this.arm_destination = rtl2arm (this.insn_destination);

      /* This case checks to see the S Exp is of type SET. Otherwise, it could
         be type USE which does not have a source expression */
      if (this.insn_type == InstructionType.SET) {
         this.insn_source = (Instruction) insn.getAttributes().get(SRC_LOC);
         this.arm_source = rtl2arm (this.insn_source);
      }

      this.arm_string = rtl2arm (insn);
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
            out.append(getRegister(insn, false));
            break;
         case REG_CC: //this isnt a reg virtual reg... is condition code reg for booleans
            //out.append(getRegister(insn, false));
            out.append("CC " + (String)insn.getAttributes().get(DST_LOC) + "\n");
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
            signature = evalSet(type, dst_res, src_res);
            out.append(signature);
            break;
          case PC:
            out.append("PC\n"); 
            break;
          case LABEL_REF:
            out.append("label_ref " + insn.getAttributes().get(DST_LOC) + "\n");
            break;
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

         /* TODO:This is tricky. Because it is a load register, we must reserve
          * the register for the given virtual register.
          * EX: if we load the data at mem[X] into r5. We cannot overwrite this
          * register in later instructions.
          */
          String reg = "r";
          for(int i = regs_avail.length-1; i >= 0; i--) {
            if(regs_avail[i] == true) {
              reg += Integer.toString(i);
              regs_avail[i] = false;
              break;
            }
          }

         arm_out.append("\tldr " + reg +", " + reg_map.get(src) + "\n");
         arm_out.append("\tstr " + reg +", " + reg_map.get(dst) + "\n");

      } else if (signature == SET_SIGNATURE.PLUS_REG_REG) {

         /* TODO: This case is only true when we have an ADD set. This is
          * tricky because we have to link a virtual register to the physical
          * register.
          * EX: if we had data from 111 and 112 that we loaded into r3 and r4
          * respectively, we would have to figure out in this call that it was
          * actually loaded into r3 and r4
          */
         String reg_1 = "r";
         String reg_2 = "r";
         for(int i = 0; i < regs_avail.length; i++) {
            if(regs_avail[i] == false) {
              reg_1 += Integer.toString(i);
              regs_avail[i] = true;
              break;
            }
         }
         for(int i = 0; i < regs_avail.length; i++) {
            if(regs_avail[i] == false) {
              reg_2 += Integer.toString(i);
              regs_avail[i] = true;
              break;
            }
         }
         arm_out.append("\tadd r0, " + reg_1 + ", " + reg_2 + "\n");
         //arm_out.append(dst + ", " + src); 

      } else if (signature == SET_SIGNATURE.PLUS_REG_CONST) {

          //arm_out.append("[" + dst + ", " + src + "]");
         arm_out.append(dst + ", " + src);

      } else if (signature == SET_SIGNATURE.PLUS_REG_F_CONST) {

         //arm_out.append("[" + dst + ", " + src + "]");
         arm_out.append(dst + ", " + src);

      } else if (signature == SET_SIGNATURE.SET_REG_PLUS) {

         arm_out.append("\tadd " + dst + ", " + src + "\n"); 
    
      } else if (signature == SET_SIGNATURE.PLUS_REG_REG) {
          //???
      } else if (signature == SET_SIGNATURE.JUMP_COMPARE_CONST) {
          arm_out.append("jump compare const\n");
          //String[] compare_ops = src.substring(8).split(", ");
          //arm_out.append("RAWRAWRAWRAWRAWR " + compare_ops[0] + " sfafa " + compare_ops[1]);
      } else {

         System.out.println("\tTYPE: " + type + " dst: " + dst + " src: " + src);
         arm_out.append("\tARMInstruction evalSet(): Unsupported Set Signature!\n");

      }

      return arm_out.toString();
   }

  /**
   * Method to determine the RTL instruction signature based on the destination
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
      String regex_jump_cond_reg_const = "COMPARE \\[.*\\], #.*";
      String regex_jump_cond_counter = "CC [0-9]+";

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

         } else if(dst.matches(regex_jump_cond_counter) && src.matches(regex_jump_cond_reg_const)) {
            
            return SET_SIGNATURE.JUMP_COMPARE_CONST;
          
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
