package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.*;

public class ConstructionHeuristicRegret {

    private Data data;

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution constructSolution(int k) throws ArgumentOutOfBoundsException {
        RegretInsertion inserter = new RegretInsertion(k, this.data);
        return inserter.solve(Solution.getEmptySolution(data));
    }
}