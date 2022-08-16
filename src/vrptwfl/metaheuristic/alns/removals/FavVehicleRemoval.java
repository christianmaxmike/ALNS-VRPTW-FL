package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Comparator;
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
public class FavVehicleRemoval extends AbstractRemoval {
	
	int selectedVehicle;

	/**
	 * Constructor for the random vehicle removal object. 
	 * @param data: Data object
	 */
	public FavVehicleRemoval(Data data) {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
        //TODO: nach skill sortieren + Anzahl der momentanen gescheduldeten Customer
		
		// get number of removals based on parameters defined in config file
        List<Integer> removedCustomers = new ArrayList<>();

        int nVehicles = solution.getVehicles().size();
        // List<Integer> shuffledVehicleIndices = CalcUtils.getSortedUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);
        List<Integer> shuffledVehicleIndices = CalcUtils.getShuffledUniqueRandomNumbersInRange(nVehicles, 0, nVehicles-1);

        
        solution.getVehicles().sort(new Comparator<Vehicle>() {

			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				if (v1.getSkillLvl() < v2.getSkillLvl()) return -1;
				else if (v1.getSkillLvl() > v2.getSkillLvl()) return +1;
				else {
					if (v1.getnCustomersInTour() <= v2.getnCustomersInTour()) return -1;
					else return +1;
				}
			}
		});
        
        for (int i = 0; i<solution.getVehicles().size(); i++) {
        	
//        vehicleIndexLoop:
 //       for (Integer idx: shuffledVehicleIndices) {
            Vehicle vehicle = solution.getVehicles().get(i);
            if (!vehicle.isUsed()) continue;

//          selectedVehicle = idx;
            selectedVehicle = solution.getVehicles().get(i).getId();
            for (int c = vehicle.getnCustomersInTour(); c > 0; c--) {  // c starts at 1 as first customer is at position 1 (0 is dummy out)
                int removedCustomer = vehicle.applyRemoval(c, this.data, solution);
                removedCustomers.add(removedCustomer); // vehicle.getCustomers().get(c));
            }
            break;
        }
        
        solution.getVehicles().sort(new Comparator<Vehicle>() {

			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				if (v1.getId() < v2.getId()) return -1;
				else return +1;
			}
		});

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