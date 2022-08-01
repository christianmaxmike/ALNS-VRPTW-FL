package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertionBacktracking;
import vrptwfl.metaheuristic.alns.insertions.SequentialAllocationHeuristic;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.WriterUtils;
import vrptwfl.metaheuristic.Config;

/**
 * Class implements the construction heuristic combining k-regret with
 * the backtracking mechanism. 
 * 
 * @author Christian M.M. Frey, Alexander Jungwirth
 *
 */
public class ConstructionHeuristicRegret {

    private Data data;

    /**
     * Constructor of the construction heuristic.
     * 
     * @param data: data object
     */
    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
    }

    /**
     * Runs the construction heuristic and returns the solution object.
     * The parameter k sets which regret measure to use, e.g.,
     * k=2 means difference between the best insertion and 3rd best insertion.
     * @param k: defines what regret measure to use
     * @return Solution object after construction
     * @throws ArgumentOutOfBoundsException
     */
    public Solution constructSolution(int k) throws ArgumentOutOfBoundsException {
    	Solution emptySolution = Solution.getEmptySolution(data);
    	emptySolution.setIsConstruction(true);
    	if (Config.getInstance().useSAH) {
    		SequentialAllocationHeuristic inserter = new SequentialAllocationHeuristic(data);
    		return inserter.solve(emptySolution);
    	}
    	else if (!Config.getInstance().enableBacktracking) {
        	RegretInsertion inserter = new RegretInsertion(k, data);
        	return inserter.solve(emptySolution);        	
        }
        else {
        	WriterUtils.initBacktrackingLogging();
        	RegretInsertionBacktracking inserter = new RegretInsertionBacktracking(k, data);
        	return inserter.solveBacktrack(emptySolution);
        }
    }
}