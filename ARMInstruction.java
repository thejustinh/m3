/**************************
 * This is the ARMInstruction class that converts the S Expression from an
 * RTL instruction and builds the corresponding ARM instruction.
 * 
 * @author Justin Herrera
 * @author James Kwan
 **/

import java.util.HashMap;

public class ARMInstruction {

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
  /*************************
   * ACCESSOR METHODS
   ************************/ 
   public String getArmDestination() { return this.arm_destination; }
   public String getArmSource() { return this.arm_source; }
   public String toString() { return this.arm_string; }

  /*************************
   * CONSTRUCTOR
   ************************/ 
   public ARMInstruction (Instruction insn, HashMap<String, String> reg_map, Counter reg_count) {
      this.insn = insn.getInsn();
      this.insn_type = insn.getType();

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
      String dst_res;
      String src_res;
      Instruction dst;
      Instruction src;

      switch (type) {
         case NOTE:
            out.append(" NOTE");
            break;
         case SET: // NON-TERMINAL
            dst_res = rtl2arm(this.insn_destination);
            src_res = rtl2arm(this.insn_source);
        
            out.append("LDR or STR " + dst_res + ", " +  src_res);
            break;
         case REG_SI:
            out.append(getRegister(insn, false));
            break;
         case REG_F_SI:
            out.append(getRegister(insn, true));
            break;
         case REG_I_SI:
            out.append(getRegister(insn, false));
            break;
         case REG_CC:
            out.append(getRegister(insn, false));
            break;
         case MEM_C_SI: // NON-TERMINAL
            dst = (Instruction) insn.getAttributes().get(DST_LOC); 
            dst_res = rtl2arm(dst); 

            out.append(dst_res);
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

            out.append("[" + dst_res + ", " + src_res + "]");
            break;
         default:
            return "ARMInsn rtl2arm(): OPERATION NOT SUPPORTED";
      }

      return out.toString();
   }

   private String getRegister (Instruction insn, boolean vsv) {
      return (String) insn.getAttributes().get(ARG_LOC);
   }
}
