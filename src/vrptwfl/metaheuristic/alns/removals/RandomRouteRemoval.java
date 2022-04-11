package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the random route removal heuristic which randomly removes all
 * customers from the selected route.
 * (Mancini 2016, page 107 - (Transportation Research Part C))
 * 
 * @author Alexander Jungwirth, Christian M.M. Frey
*/
public class RandomRouteRemoval extends AbstractRemoval {

	/**
	 * Constructor for the random route removal class. 
	 * The attached data object is forwarded to the parent class 
	 * AbstractRemoval.
	 * @param data: data object
	 */
    public RandomRouteRemoval(Data data) {
        super(data);
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        // get number of removals based on parameters defined in config file
        List<Integer> removedCustomers = new ArrayList<>();

        int nVehicles = solution.getVehicles().size();
        // List<Integer> shuffledVehicleIndices = CalcUtils.getSortedUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);
        List<Integer> shuffledVehicleIndices = CalcUtils.getShuffledUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);

        vehicleIndexLoop:
        for (Integer idx: shuffledVehicleIndices) {
            Vehicle vehicle = solution.getVehicles().get(idx);
            if (!vehicle.isUsed()) continue;

            for (int c = 1; c < vehicle.getnCustomersInTour()+1; c++) {  // c starts at 1 as first customer is at position 1 (0 is dummy out)
                int removedCustomer = vehicle.applyRemoval(c, this.data, solution);
                removedCustomers.add(removedCustomer); // vehicle.getCustomers().get(c));
                nRemovals--;
                if (nRemovals == 0) break vehicleIndexLoop;
            }
        }
        return removedCustomers;
    }
}