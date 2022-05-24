package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

/**
 * This class implements the Zone Removal Heuristic
 * It removes customers which are within the same zone.  
 * A zone is defined by the nearby location to an initial chosen
 * location where a customer can be scheduled
 * 
 * @author: Christian M.M. Frey
 */

public class ZoneRemoval extends AbstractRemoval {

    /**
     * Constructor for the zone removal heuristic.
     * @param data: data object
     * @param randomize: use randomized version
     */
    public ZoneRemoval(Data data) {
        super(data);
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

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

        // --- main loop ---
        while (nRemovals > 0) {
            // 2) get customers closest to the zone of reference customer
            // get row from travel distance matrix
            double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomerLocationIdx];
            ArrayList<double[]> closest = new ArrayList<>();
            
            // arg sort on distance mx
            int[] argSorted = DataUtils.argsort(distanceToFirstCustomer, true);
            argSort:
            for (int arg = 0; arg<argSorted.length; arg++) {
            	// get location at position arg
            	int argIdx = argSorted[arg];
            	// get customers at location at position 'argIdx'
            	ArrayList<Integer> customersAtLocation = solution.getData().getLocationsToCustomers().get(argIdx);
            	// check if customers can possibly be scheduled in the current location; if not -> no list is stored
            	if (customersAtLocation == null) continue;
            	for (int originalCustomerId : customersAtLocation) {
            		int schedulingIdx = Arrays.stream(this.data.getOriginalCustomerIds()).boxed().collect(Collectors.toList()).indexOf(originalCustomerId);

            		// check if customer is in current scheduling problem
            		if (!Arrays.stream(solution.getData().getOriginalCustomerIds()).boxed().collect(Collectors.toList()).contains(originalCustomerId)) continue;
            		// check if customer is the first customer being already removed
            		if (schedulingIdx == firstCustomer) continue;
            		
            		// TODO Chris - is 0th customer existent
            		if (schedulingIdx == 0)
            			continue;
            		
            		// check if customer is scheduled in the current solution object
            		if (!solution.getNotAssignedCustomers().contains(schedulingIdx)) {
            			// add customer to removed customers
            			removedCustomers.add(schedulingIdx);
            			// get vehicle
            			Vehicle v = solution.getVehicles().get(solution.getCustomersAssignedToVehicles()[schedulingIdx]);
            			// apply removal on vehicle
            			v.applyRemovalForCustomer(schedulingIdx, solution.getData(), solution);

            			// 3) update nRemovals and break loop if desired number of removals is reached
            			nRemovals--;
            			if (nRemovals == 0) break argSort;
            		}            		
            	}
            }
        } // end while (nRemovals > 0)
        
        return removedCustomers;
    }
    
    
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Zone";
		//return "Zone (" + (this.randomize?"determ.":"random") + ")";
	}

}
