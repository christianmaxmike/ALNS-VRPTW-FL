package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

/**
 * This class implements the Subroute Removal operation.
 * A customer-location tuples is randomly selected and then, starting from this tuple,
 * a virtual route of length n is constructed in a greedy fashion. All tuples in this
 * virtual route are removed from the existing routes in the current solution.
 * 
 * @author Christian M.M. Frey
 *
 */
public class SubrouteRemoval extends AbstractRemoval {

	/**
	 * Constructor of the subroute removal operation
	 * @param data: data object
	 */
	public SubrouteRemoval(Data data) {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
        List<Integer> removedCustomers = new ArrayList<>();

        // 1) choose the first customer to be removed at random
        int firstCustomer = -1; // set dummy value to see later if customer could be removed
        int posFirstRemoval = CalcUtils.getRandomNumberInClosedRange(0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);
        int firstCustomerLocationIdx = -1;

        // go through all vehicles and count the customers until the count corresponds to the position to remove
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;

            if (posFirstRemoval >= vehicle.getnCustomersInTour()) {
                posFirstRemoval -= vehicle.getnCustomersInTour();
            } else {
            	firstCustomer = vehicle.getCustomers().get(posFirstRemoval + 1);
                // firstCustomerPreferencedLocation = solution.getCustomerAffiliationToLocations()[firstCustomer];
                firstCustomerLocationIdx = DataUtils.getLocationIndex(firstCustomer, solution);

                vehicle.applyRemoval(posFirstRemoval + 1, this.data, solution);  // +1 as dummy out is at index 0
                removedCustomers.add(firstCustomer); //vehicle.getCustomers().get(posFirstRemoval));
                nRemovals--;
                break;
            }
        }

        // if no customer could be removed (no one was in tour), then return empty list
        if (firstCustomer == -1) 
        	return removedCustomers;
        

        while (nRemovals > 0) {
        	
            ListIterator<Integer> iter = solution.getAssignedCustomers().listIterator();

            int nearestCustomer = -1;
            double costs = Double.MAX_VALUE;
            int currCustomerLoc = firstCustomerLocationIdx;
            while (iter.hasNext()) {
                // initialize next customer id
                int customer = iter.next();

        		double distFromCustomer = data.getDistanceBetweenLocations(currCustomerLoc, DataUtils.getLocationIndex(customer, solution));
            	if (costs > distFromCustomer) {
            		nearestCustomer = customer;
            		costs = currCustomerLoc;
            	}
            }

            int vehicleIdx = solution.getCustomersAssignedToVehicles()[nearestCustomer];
            solution.getVehicles().get(vehicleIdx).applyRemovalForCustomer(nearestCustomer, solution.getData(), solution);
            removedCustomers.add(nearestCustomer);

            nRemovals--;
        }
        
        return removedCustomers;
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public String getFormattedClassName() {
		return "Subroute";
	}
}
