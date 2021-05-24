package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.Arrays;

public class ConstructionHeuristicRegret {

    private Data data;

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
    }

    public Solution solve() {

        ArrayList<Integer> unscheduledCustomers = new ArrayList(Arrays.asList(this.data.getCustomers()));
        ArrayList<Vehicle> vehicles = data.initializeVehicles();

        // TODO hier gehts weiter
//        for (Integer customer: unscheduledCustomers) {
//            for (Vehicle vehicle: vehicles) {
//                vehicle.getPossibleInsertions(customer);  [customerId, vehicleId, additionalCosts]
//            }
//        }

        ArrayList<int[]> res;

        return null;
    }

}
