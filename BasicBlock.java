import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;

public class BasicBlock {

    private int block_num;
    private LinkedList<Instruction> block;
    private HashSet<String> successors;

/*******************
 * Accessor Methods
 *******************/
    public LinkedList<Instruction> getInstructions() { return this.block; }
    public int getBlockNum() { return this.block_num; }
    public HashSet<String> getSuccessors() { return this.successors; }
   
/*******************
 * Constructor
 *******************/
    public BasicBlock (LinkedList<Instruction> block, int block_num, 
                       HashMap<String, String> bb_map)  {

        this.block = block;
        this.block_num = block_num;
        this.successors = new HashSet<>();

        /* This adds to successor list if jump label */
        Instruction last_insn = block.get(block.size() - 1);
        System.out.println(last_insn.getInsn());
        if (last_insn.getType() == InstructionType.JUMP_INSN) {
            String jump_label = getJumpLabel(last_insn);
            if(bb_map.containsKey(jump_label)) {
                this.successors.add(bb_map.get(jump_label));
            }
        }
        
        /* This adds to successor list if there is a next ID that exists */
        if (Integer.parseInt(last_insn.getNextID()) > 0) {
            if (Integer.parseInt(bb_map.get(last_insn.getNextID())) > 0) {
                this.successors.add(bb_map.get(last_insn.getNextID()));
            }
        } else {
            this.successors.add("EXIT");
        }

    }


/******************* 
 * Helper Methods
 *******************/
    private static String getJumpLabel(Instruction insn) {
        ArrayList<Object> attributes = insn.getAttributes();
        String str = (String)attributes.get(attributes.size() - 1);
        return str;
    }

    public void printAll() {
        System.out.println("Block Num: " + this.getBlockNum());
        System.out.println("\tSuccessors: " + this.getSuccessors().toString());
    } 

}
