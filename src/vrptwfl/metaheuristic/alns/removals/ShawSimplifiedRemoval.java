package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.*;

/**
 * This class implements the Time Oriented heuristic.
 * It removes customers which are "related".  
 * Here related is defined solely by distance.
 * (cf. Ropke and Pisinger 2006, page 759 (EJOR))
 * 
 * @author: Alexander Jungwirth, Christian M.M. Frey
 */
public class ShawSimplifiedRemoval extends AbstractRemoval {

    private final boolean randomize;

    /**
     * Constructor for the shaw simplified removal heuristic.
     * @param data: data object
     * @param randomize: use randomized version
     */
    public ShawSimplifiedRemoval(Data data, boolean randomize) {
        super(data);
        this.randomize = randomize;
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    // TODO Alex : hier nochmal drüber gehen und vereinfachen und kommentieren
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

        // --- choose the first customer to be removed at random ---
        int firstCustomer = -1; // set dummy value to see later if customer could be removed
        int posFirstRemoval = CalcUtils.getRandomNumberInClosedRange(0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);
        int firstCustomerLocationIdx = -1;
        // int firstCustomerPreferencedLocation = -1;

        // go through all vehicles and count the customers until the count corresponds to the position to remove
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;

            if (posFirstRemoval >= vehicle.getnCustomersInTour()) {
                posFirstRemoval -= vehicle.getnCustomersInTour();
            } else {
            	// TODO_DONE Chris - adapt to multiple locations
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

        // --- main loop ---
        // (randomly) choose an already selected customer i, and select customer j which is closest to i
        while (nRemovals > 0) {

            // 1) choose already selected customer i (reference customer)
            // int idxI = CalcUtils.getRandomNumberInClosedRange(0, removedCustomers.size() - 1);
            // int customerI = removedCustomers.get(idxI);

            // 2) get customers closest to the reference customer
            // get row form travel time matrix
            // TODO Alex - wenn mehrere locations, dann die location beruecksichtigen, die am dichtesten ist
            //  (in distanceToFirstCustomer[customer])
            // TODO Alex - Methode auslagern, wird auch in ClusterKruskal benutzt
            //double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[customerI];
            double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomerLocationIdx];
            ArrayList<double[]> closest = new ArrayList<>();

            // add all customers already assigned to the vehicles
            for (Vehicle vehicle: solution.getVehicles()) {
                if (!vehicle.isUsed()) continue;
                for (int customer: vehicle.getCustomers()) {
                    if (customer == 0) 
                    	continue;
                    int customersLocation = DataUtils.getLocationIndex(customer, solution);
                    closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customersLocation]});
                    // closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customer]});
                }
            }
            closest.sort(Comparator.comparing(v->v[2]));  // sort according to distance (smallest distance first)

            // select next customer to be removed
            int idxJ = 0;
            if (this.randomize) {
                double rand = Config.getInstance().randomGenerator.nextDouble();
                idxJ = (int) Math.floor(Math.pow(rand, Config.getInstance().shawRemovalExponent) * closest.size());
            }
            double[] removal = closest.get(idxJ);
            removedCustomers.add((int) removal[0]);
            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data, solution);

            // 3) update nRemovals and break loop if desired number of removals has been met
            nRemovals--;
            if (nRemovals == 0) break;
        } // end while (nRemovals > 0)

        // TODO Alex - entferne alten Teil, wenn man es nicht mehr braucht
//        // get customers closest to the first one
//        // get row form travel time matrix
//        double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomer];
//        ArrayList<double[]> closest = new ArrayList<>();
//
//        // add all customers already assigned to the vehicles
//        for (Vehicle vehicle: solution.getVehicles()) {
//            if (!vehicle.isUsed()) continue;
//            for (int customer: vehicle.getCustomers()) {
////            for (int c = 1; c < vehicle.getnCustomersInTour()+1; c++) {
//                if (customer == 0) continue;
//                closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customer]});
//            }
//        }
//        closest.sort(Comparator.comparing(v->v[2]));  // sort according to distance (smallest distance first)
//
//        while (nRemovals > 0) {
//            nRemovals--;
//            int idx = 0;
//            if (this.randomize) {
//                double rand = Config.getInstance().randomGenerator.nextDouble();
//                idx = (int) Math.floor(Math.pow(rand, Config.getInstance().shawRemovalExponent) * closest.size());
//            }
//            double[] removal = closest.get(idx);
//            removedCustomers.add((int) removal[0]);
//            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data);
//            closest.remove(idx);
//
//            if (nRemovals == 0) break;
//        }

        // TODO Alex: wenn es locations gibt, geht die Logik so nicht komplett

        return removedCustomers;

    }
        
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Shaw's Proximity (" + (this.randomize?"random":"determ.") + ")";
	}
}
