package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertionBacktracking;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

public class ConstructionHeuristicRegret {

    private Data data;

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution constructSolution(int k) throws ArgumentOutOfBoundsException {
    	Solution emptySolution = Solution.getEmptySolution(data);
    	// SkillMatchingInsertion inserter = new SkillMatchingInsertion(data);
        if (!Config.enableBacktracking) {
        	RegretInsertion inserter = new RegretInsertion(k, data);
        	return inserter.solve(emptySolution);        	
        }
        else {
        	RegretInsertionBacktracking inserter = new RegretInsertionBacktracking(k, data);
        	return inserter.solveBacktrack(emptySolution);
        }
    }
}