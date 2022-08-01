package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;


/**
 * This class implements the random vehicle removal heuristic.
 * From the current scheduling, the procedure selects any vehicles and remove its
 * customers. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class RandomVehicleRemoval extends AbstractRemoval {
	
	int selectedVehicle;

	/**
	 * Constructor for the random vehicle removal object. 
	 * @param data: Data object
	 */
	public RandomVehicleRemoval(Data data) {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
        // get number of removals based on parameters defined in config file
        List<Integer> removedCustomers = new ArrayList<>();

        int nVehicles = solution.getVehicles().size();
        // List<Integer> shuffledVehicleIndices = CalcUtils.getSortedUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);
        List<Integer> shuffledVehicleIndices = CalcUtils.getShuffledUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);

        vehicleIndexLoop:
        for (Integer idx: shuffledVehicleIndices) {
            Vehicle vehicle = solution.getVehicles().get(idx);
            if (!vehicle.isUsed()) continue;

            selectedVehicle = idx;
            for (int c = 1; c < vehicle.getnCustomersInTour()+1; c++) {  // c starts at 1 as first customer is at position 1 (0 is dummy out)
                int removedCustomer = vehicle.applyRemoval(c, this.data, solution);
                removedCustomers.add(removedCustomer); // vehicle.getCustomers().get(c));
            }
            break;
        }
        return removedCustomers;

	}
	
	/**
	 * Returns the vehicle's id whose customer are removed from the current scheduling.
	 * @return id of removed vehicle
	 */
	public int getSelectedIdx () {
		return this.selectedVehicle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedClassName() {
		return "RandomVehicleRemoval";
	}
}