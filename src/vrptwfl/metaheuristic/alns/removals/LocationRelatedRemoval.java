package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

public class LocationRelatedRemoval extends AbstractRemoval {

	public LocationRelatedRemoval(Data data) {
		super(data);
	}

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
        
        // 2)  Retrieve location related customers
        List<double[]> closest = new ArrayList<>();

        // add all customers already assigned to the vehicle
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;
            for (int customer: vehicle.getCustomers()) {
                if (customer == 0) continue;
                double locationRelatedness = computeRelatedness(firstCustomer, customer, solution);
                closest.add(new double[] {customer, vehicle.getId(), locationRelatedness});
            }
        }
        
        // 3) sort according to distance (smallest scores (location relatedness) first)
        // Location relatedness is defined as distance metric; 
        closest.sort(Comparator.comparing(v->v[2]));

        // 4) --- remove customers which are related in their location
        int idx = 0;
        while (nRemovals > 0) {
            double[] removal = closest.get(idx);
            removedCustomers.add((int) removal[0]);
            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data, solution);

            // remove customer from list of closest customers
            closest.remove(idx);

            nRemovals--;
            idx ++;
        }
        return removedCustomers;
	}
	
	
	private double computeRelatedness (int customerA, int customerB, Solution s) {
		ArrayList<Integer> locationsA = s.getData().getCustomersToLocations().get(customerA);
		ArrayList<Integer> locationsB = s.getData().getCustomersToLocations().get(customerB);
		double min = Math.min(locationsA.size(), locationsB.size());
		ArrayList<Integer> copy = new ArrayList<Integer>(locationsA);
		copy.retainAll(locationsB);
		return 1.0 - (copy.size() / min);
	}

	@Override
	public String getFormattedClassName() {		
		return "Location related";
	}

}
