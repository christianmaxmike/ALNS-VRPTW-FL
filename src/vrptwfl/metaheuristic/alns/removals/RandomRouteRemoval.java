package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

// randomly select routes and then remove customers from the route
public class RandomRouteRemoval extends AbstractRemoval {

    public RandomRouteRemoval(Data data) {
        super(data);
    }

    @Override
    public void destroy(Solution solution) {

        // get number of removals based on parameters defined in config file
        int nRemovals = getNRemovals();
        List<Integer> removedCustomers = new ArrayList<>();

        int nVehicles = solution.getVehicles().size();
        List<Integer> shuffledVehicleIndices = CalcUtils.getSortedUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);

        vehicleIndexLoop:
        for (Integer idx: shuffledVehicleIndices) {
            Vehicle vehicle = solution.getVehicles().get(idx);
            if (!vehicle.isUsed()) continue;

            for (int c = 1; c < vehicle.getnCustomersInTour()+1; c++) {  // c starts at 1 as first customer is at position 1 (0 is dummy out)
                vehicle.applyRemoval(c, this.data);
                removedCustomers.add(vehicle.getCustomers().get(c));
                nRemovals--;
                if (nRemovals == 0) break vehicleIndexLoop;
            }
        }

        // Update the solution object.  The tours of the vehicle are already update by the removals.  However, global
        // information such as the total costs and list of notAssignedCustomers still need to be updated.
        solution.updateSolutionAfterRemoval(removedCustomers);

    }

}
