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

public class LocationAndTimeRelatedRemoval extends AbstractRemoval {

    private double weightStartTimeInSolution; // [%] in Pisinger & Ropke = 1; in Jungwirth can be between 0 and 1

	public LocationAndTimeRelatedRemoval(Data data, double weightStartTimeInSolution) throws ArgumentOutOfBoundsException {
		super(data);
        if (weightStartTimeInSolution < - Config.getInstance().epsilon || weightStartTimeInSolution > 1 + Config.getInstance().epsilon) 
        	throw new ArgumentOutOfBoundsException("Weight parameter (alpha_1) for time-oriented destroy must be in interval [0,1]. Given was: " + weightStartTimeInSolution + ".");
        this.weightStartTimeInSolution = weightStartTimeInSolution; // alpha_2 - Wert im draft
	}

	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {

        if (nRemovals > Config.getInstance().timeOrientedNrOfClosest) throw new ArgumentOutOfBoundsException("nRemovals (q=" + nRemovals + ") must be less than or equal to timeOrientedNrOfClosest (B=" + Config.getInstance().timeOrientedNrOfClosest + ").");

        List<Integer> removedCustomers = new ArrayList<>();

        // 1) --- choose the first customer to be removed at random ---
        int firstCustomer = -1; // set dummy value to see later if customer could be removed
        int posFirstRemoval = CalcUtils.getRandomNumberInClosedRange(0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);
        double startTimeFirstCustomer = -1.;
        int firstCustomerLocationIdx = -1;
        // int firstCustomerPreferencedLocation = -1;

        // TODO Alex: koennen wir das auslagern in eigene Methode? Ist identisch mit ShawSimplified
        // go through all vehicles and count the customers until the count corresponds to the position to remove
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) 
            	continue;

            if (posFirstRemoval >= vehicle.getnCustomersInTour()) {
                posFirstRemoval -= vehicle.getnCustomersInTour();
            } else {
                startTimeFirstCustomer = vehicle.getStartOfServices().get(posFirstRemoval + 1);  // +1 as dummy out is at index 0
                // firstCustomerPreferencedLocation = solution.getCustomerAffiliationToLocations()[posFirstRemoval + 1];
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
        if (firstCustomer == -1) return removedCustomers;

        // 2) --- get customers closest to the reference customer ---
        // TODODone: Chris: auf mehrere locations anpassen
        // double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomer];
        double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomerLocationIdx];
        List<double[]> closest = new ArrayList<>();
        
		// double betaOne = Math.random();		// weight for time relatedness
		double betaOne = 0.5;
		double betaTwo = 1 - betaOne;		// weight for location relatedness

        // add all customers already assigned to the vehicle
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;
            int positionCounter = 0;
            for (int customer: vehicle.getCustomers()) {
                positionCounter++;
                if (customer == 0) continue;
                double timeDiff = Math.abs(startTimeFirstCustomer - vehicle.getStartOfServices().get(positionCounter));
                double avgStartTime = data.getAverageStartTimes(firstCustomer, customer);
                double timeRelatedness = this.weightStartTimeInSolution * timeDiff + (1 - this.weightStartTimeInSolution) * avgStartTime;
                double locationRelatedness = computeLocationRelatedness(firstCustomer, customer, solution);
                double score = betaOne * timeRelatedness + betaTwo * locationRelatedness;
                closest.add(new double[] {customer, vehicle.getId(), score});
            }
        }
        // sort according to distance (smallest distance first)
        closest.sort(Comparator.comparing(v->v[2]));

        // 4) --- remove customers which are related in time
        int idx = 0;
        while (nRemovals > 0 && idx < closest.size()) {
            double[] removal = closest.get(idx);
            removedCustomers.add((int) removal[0]);
            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data, solution);

            // remove customer from list of closest customers
            closest.remove(idx);

            nRemovals--;
            idx++;
        }
        return removedCustomers;	
    }

	
	private double computeLocationRelatedness (int customerA, int customerB, Solution s) {
		ArrayList<Integer> locationsA = s.getData().getCustomersToLocations().get(customerA);
		ArrayList<Integer> locationsB = s.getData().getCustomersToLocations().get(customerB);
		double min = Math.min(locationsA.size(), locationsB.size());
		ArrayList<Integer> copy = new ArrayList<Integer>(locationsA);
		copy.retainAll(locationsB);
		return 1.0 - (copy.size() / min);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedClassName() {
		return "Location And Time Related";
	}

}