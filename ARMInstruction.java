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
      this.insn_destination = (Instruction) insn.getAttributes().get(DST_LOC);
      this.insn_source = (Instruction) insn.getAttributes().get(SRC_LOC);
      this.arm_destination = rtl2arm (this.insn_destination);
      this.arm_source = rtl2arm (this.insn_source);
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
            System.out.println("rtl2arm: REG_SI");
            break; 
         case REG_F_SI:
            System.out.println("rtl2arm: REG_F_SI");
            break; 
         case MEM:
            System.out.println("rtl2arm: MEM");
            break; 
         case CONST_INT:
            System.out.println("rtl2arm: CONST_INT");
            break; 
         case PLUS:
            System.out.println("rtl2arm: PLUS");
            break; 
         default:
            System.out.println("ARMInsn rtl2arm(): OPERATION NOT SUPPORTED");
            break;
      }
  
      return "hello!";
   }
}
