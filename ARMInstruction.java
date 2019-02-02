/**************************
 * This is the ARMInstruction class that converts the S Expression from an
 * RTL instruction and builds the corresponding ARM instruction.
 * 
 * @author Justin Herrera
 * @author James Kwan
 **/
public class ARMInstruction {

   private static final int DST_LOC = 1;
   private static final int SRC_LOC = 2;

   /* RTL attributes for ARM Instruction */
   private String insn;
   private InstructionType insn_type;
   private Instruction insn_destination;
   private Instruction insn_source;
   
   /* ARM Instruction attributes */
   private String arm_destination;
   private String arm_source;

  /*************************
   * ACCESSOR METHODS
   ************************/ 
   public String getArmDestination() { return this.arm_destination; }
   public String getArmSource() { return this.arm_source; }

  /*************************
   * CONSTRUCTOR
   ************************/ 
   public ARMInstruction (Instruction insn) {
      this.insn = insn.getInsn();
      this.insn_type = insn.getType();
      this.insn_destination = (Instruction) (insn.getAttributes()).get(DST_LOC);
      this.arm_destination = rtl2arm (this.insn_destination);

      /* This case checks to see the S Exp is of type SET. Otherwise, it could
         be type USE which does not have a source expression */
      if (this.insn_type == InstructionType.SET) {
         this.insn_source = (Instruction) insn.getAttributes().get(SRC_LOC);
         this.arm_source = rtl2arm (this.insn_source);
      }
   }

  /*************************
   * ARMInstruction LOGIC
   ************************/ 
   /**
    * This is a driver method to convert and evaluate an RTL expression to
    * an appropriate ARM instruction
    * 
    * This assumes that the instruction passed in is only a destination or
    * source argument
    *
    * @param Instruction insn
    * @param Hashmap RegisterTable
    * @return String representation of an RTL expression
    */
   private String rtl2arm (Instruction insn) {
      InstructionType type = insn.getType();
      switch (type) {
         case REG_SI:
            return "rtl2arm: REG_SI";
         case REG_F_SI:
            return "rtl2arm: REG_F_SI";
         case REG_I_SI:
            return "rtl2arm: REG_I_SI";
         case REG_CC:
            return "rtl2arm: REG_CC";
         case MEM_C_SI:
            return "rtl2arm: MEM_C_SI";
         case CONST_INT:
            return "rtl2arm: CONST_INT";
         case COMPARE_CC:
            return "rtl2arm: COMPARE_CC";
         case PLUS:
            return "rtl2arm: PLUS";
         default:
            return "ARMInsn rtl2arm(): OPERATION NOT SUPPORTED";
      }
   }
}
