import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;

public class BasicBlockGraph {

    private static ArrayList<BasicBlock> bb_graph;
    private static ArrayList<HashSet<String>> prev_live_in_set;
    private static ArrayList<HashSet<String>> live_in_set;
    private static ArrayList<HashSet<String>> live_out_set;

    private ArrayList<LinkedList<String>> interference_graph;

/**************************
 * Constructor
 **************************/
    public BasicBlockGraph (ArrayList<LinkedList<Instruction>> graph,
                            HashMap<String, String> bb_map) {
        this.bb_graph = new ArrayList<>();
        this.prev_live_in_set = new ArrayList<>();
        this.live_in_set = new ArrayList<>();
        this.live_out_set = new ArrayList<>();
        HashSet<String> dummy = new HashSet<>();
        dummy.add("dummy");

        /* This initializes the array of basic block objects */
        for (int i = 0; i < graph.size(); i++) {

            System.out.println("i" + i );
            LinkedList<Instruction> block = graph.get(i);

            if (block.isEmpty()) {
                this.bb_graph.add(null);
            } else {
                this.bb_graph.add(new BasicBlock(block, i, bb_map)); 
            }

            this.prev_live_in_set.add(dummy);
            this.live_in_set.add(new HashSet<>());
            this.live_out_set.add(new HashSet<>());
        }

    }

/*********************************
 * Data Structure Accessor Methods
 ********************************/
   /**
    * Method to get the basic block object given the basic block number
    *
    * @param int basic_block_num
    * @return BasicBlock object
    **/
    private static BasicBlock getBasicBlock(int block_num) {
        return bb_graph.get(block_num);
    } 

   /**
    * Method to get the live in set for a given basic block.
    *
    * @param int basic_block_num
    * @return HashSet<String> 
    **/
    private static HashSet<String> getLiveInAtBlock(int block_num) {
        return live_in_set.get(block_num);
    } 

   /**
    * Method to get the live out set for a given basic block.
    *
    * @param int basic_block_num
    * @return HashSet<String> 
    **/
    private static HashSet<String> getLiveOutAtBlock(int block_num) {
        return live_out_set.get(block_num);
    } 



/**************************
 * Main Logic Methods 
 **************************/
    public ArrayList<LinkedList<String>> getInterferenceGraph() {

        if (this.interference_graph != null)
            return this.interference_graph;

        calculateLiveInSet();

        this.interference_graph = new ArrayList<>();

        for (int bb_num = 0; bb_num < bb_graph.size(); bb_num++) {

            /* for each basic block, we want to get the live in set */
            HashSet<String> live_ins = getLiveInAtBlock(bb_num); 

            /* For every element */

        }

        return this.interference_graph;
    
    }

   /**
    * Method to calculate the set of Live Ins used to build the interference graph
    * This method uses the algorithm discussed in class
    **/
    public static void calculateLiveInSet() {
        
        int i = 0;
        boolean flag = true; 

        System.out.println("Calculating LIVE IN SET");

        while (flag || isChangedSet(prev_live_in_set, live_in_set)) {

            flag = false;
            System.out.println("Iteration: " + i);
            i++;

            for (int bb_num = 0; bb_num < bb_graph.size(); bb_num++) {

                if (getBasicBlock(bb_num) != null) { 

                    HashSet<String> new_live_out = calculateLiveOut(bb_num);

                    HashSet<String> new_live_in = calculateLiveIn(bb_num, new_live_out);

                    // Update Prev Live_in, cur_live_in, and live out sets
                    live_out_set.set(bb_num, new_live_out);
                    prev_live_in_set.set(bb_num, getLiveInAtBlock(bb_num));
                    live_in_set.set(bb_num, new_live_in);
                }

            }
            
            //printInsOuts();

        }

    }


/**************************
 * Additional Helper Methods 
 **************************/

   /**
    * Method to calculate the live_out of a basic block.
    *
    * @param int basic_block_num
    * @return HashSet<String> a basic block's live out
    **/
    private static HashSet<String> calculateLiveOut(int block_num) {

        HashSet<String> block_live_out = new HashSet<>();

        /* Get successors of curent basic block */
        BasicBlock block = getBasicBlock(block_num);

        if (block != null) {
            HashSet<String> succs = block.getSuccessors();

            /* Add the sets of live ins from each successor into the created set */
            for (String succ : succs) {
                /* If a successor is of label EXIT, nothing (null) should be 
                 * added to the live out */
                if (!succ.equals("EXIT")) {
                    block_live_out.addAll(getLiveInAtBlock(Integer.parseInt(succ)));
                }
            }
        }

        return block_live_out;
    }

   /**
    * Method to calculate the live_in of a basic block
    * We pass the live out set to the block which then performs the arithmetic
    *
    * @param int block_num
    * @param HashSet<String> Live_out of the basic block
    **/
    private static HashSet<String> calculateLiveIn(int block_num, 
                                                   HashSet<String> live_out) {
        BasicBlock block = getBasicBlock(block_num);

        if (block == null)
            return new HashSet<>();

        return adjustLiveSet(block, live_out);
    }

   /**
    * Method that iterates through the blocks in reverse order and for each insn
    *   1) remove defs in RTL insn from live_out set
    *   2) add uses in RTL insn to live_out set
    *
    * @param BasicBlock block
    * @param HashSet<String> live_out
    * @return HashSet<String> live_in (updated live_out)
    **/
    public static HashSet<String> adjustLiveSet(BasicBlock block,
                                                HashSet<String> live_out) {

        if (block != null) {

            /* Get instructions from Block object */
            LinkedList<Instruction> instructions = block.getInstructions(); 

            /* Iterate through instructions starting from the end */
            for (int insn_num = instructions.size() - 1; insn_num >= 0; insn_num--) {

                /* Retrieve the instruction object */
                Instruction insn = instructions.get(insn_num);
                insn.parseSExpressions();
                insn.setTypes();
                insn.setBasicBlock();


                /* If we've reached a NOTE, we are done */
                if (insn.getType() == InstructionType.NOTE) 
                    break;
               
                /* If we reached a USE, we are done */ 
                Instruction sexp = (Instruction) insn.getAttributes().get(5);
                sexp.parseSExpressions();
                sexp.setTypes();
                sexp.setBasicBlock();

                if (sexp.getType() == InstructionType.USE)
                    break; 
    
                //System.out.print("BasicBlockGraph.java - Live Set Calc - ");
                //System.out.print("Instruction ID: " + insn.getCurrID() + "\n");
                //System.out.println("Instruction Type: " + insn.getType());

                /* get defs in instruction and remove from live_out set */
                HashSet<String> defs = insn.getDefs();
                live_out.removeAll(defs);

                /* get uses from instruction and add to live_out set */
                HashSet<String> uses = insn.getUses();
                live_out.addAll(uses);
        
            }

        }

        return live_out;
    }


   /**
    * Method checks to see if there is a change in the live in set 
    * Returns true if there is a difference in set, false otherwise
    *
    * @param ArrayList<HashSet<String>> prev_set
    * @param ArrayList<HashSet<String>> cur_set
    * @return 
    **/
    private static boolean isChangedSet(ArrayList<HashSet<String>> prev_set, 
                                        ArrayList<HashSet<String>> cur_set) {

        if (prev_set.size() != cur_set.size()) {
            System.out.println("Fatal Error: Uneven set size. Extra or lacking indices?");
            System.exit(0);
        }

        System.out.println("Comparing the sets");
        for (int i = 0; i < prev_set.size(); i++) {

            if (cur_set.get(i).isEmpty()) 
                continue;

            //printPrevBlockLiveIn(i);
            //printBlockLiveIn(i);

            /* I think theres something wrong wit hthis part */

            if (!prev_set.get(i).containsAll(cur_set.get(i)) || 
                  !cur_set.get(i).containsAll(prev_set.get(i))) {
                return true;
            }
        }

        return false;

    }
    

/**************************
 * Methods for Testing
 **************************/
    public void printAll() {
        System.out.println("Printing Basic Block Graph:");
        for (int bb_num = 0; bb_num < bb_graph.size(); bb_num++) {
            BasicBlock block = getBasicBlock(bb_num);
            if (block != null) {
                block.printAll();
                printBlockLiveIn(bb_num);
                printBlockLiveOut(bb_num);
            }
        } 
    }

    public static void printInsOuts() {
        System.out.println("Printing Live INS and Live OUTS:");
        for (int bb_num = 0; bb_num < bb_graph.size(); bb_num++) {
            System.out.println("Basic Block " + bb_num + ":");
            printPrevBlockLiveIn(bb_num);
            printBlockLiveIn(bb_num);
            printBlockLiveOut(bb_num);
        }
    }
    
    public static void printPrevBlockLiveIn(int block_num) {
        BasicBlock block = getBasicBlock(block_num);
    
        if (block == null) {
            System.out.println("\tTrying to print PREV LIVE IN for null block " + block_num);
            return;
        }

        System.out.println("\tPREV LIVE IN for block " + 
                            block_num + ": " + 
                            getLiveInAtBlock(block_num).toString());
    }
    
    public static void printBlockLiveIn(int block_num) {
        BasicBlock block = getBasicBlock(block_num);
    
        if (block == null) {
            System.out.println("\tTrying to print LIVE IN for null block " + block_num);
            return;
        }

        System.out.println("\tLIVE IN for block " + 
                            block_num + ": " + 
                            getLiveInAtBlock(block_num).toString());
    }

    public static void printBlockLiveOut(int block_num) {
        BasicBlock block = getBasicBlock(block_num);
        
        if (block == null) {
            System.out.println("\tTrying to print LIVE OUT for null block " + block_num);
            return;
        }

        System.out.println("\tLIVE OUT for block " + 
                            block_num + ": " + 
                            getLiveOutAtBlock(block_num).toString());
    }
}
