package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.RegretInsertion;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.*;

public class ConstructionHeuristicRegret {

    private Data data;

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;

    }

    private Solution getEmptySolution() {

        Solution start = new Solution();
        start.setVehicles(this.data.initializeVehicles());
        // initially add all customers to list of not assigned customers
        start.setNotAssignedCustomers(new ArrayList<>() {{ for (int i : data.getCustomers()) add(i); }});
        start.setTempInfeasibleCustomers(new ArrayList<>());
        start.setFeasible(false);

        return start;
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution constructSolution(int k) throws ArgumentOutOfBoundsException {

        RegretInsertion inserter = new RegretInsertion(k, this.data);
        return inserter.solve(this.getEmptySolution());

    }

    // ONLY FOR TEST PURPOSES
    // TODO raus
//    public ArrayList<Vehicle> getVehicles() {
//        return vehicles;
//    }


}
