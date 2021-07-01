package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRemoval {

    protected Data data;

    public AbstractRemoval(Data data) {
        this.data = data;
    }

    /**
     * Removes a subset of customers specified in sortedPositionsToRemove from a given solution.  These postitionsToRemove
     * correspond to the position of the customers in a 'giant tour' without considering the depot dummy customers:
     * E.g.: tour in vehicle 1: [0, 14,  7,  6,  0]    --> positions [0,1,2]
     *       tour in vehicle 2: [0,  3,  2, 16,  5,  0] --> positions [3,4,5,6]
     *       --> position of customer 14 = 0
     *           position of customer  7 = 1
     *           position of customer  3 = 3
     *           position of customer 16 =
     *
     * If e.g. sortedPositionsToRemove are [0,2,3], then the tours after the removals will be:
     *       tour in vehicle 1: [0,  7,  0]
     *       tour in vehicle 2: [0,  2, 16,  5,  0]
     * (i.e., customers 14, 6, and 3 were removed.)
     *
     * The advantage of this rather complicated method is that we do not have to check were exactly a given customers
     * is.  The position in the 'giant tour' is sufficent.  Therefore, the iteration over the customers within the
     * tours are reduced.
     *
     * @param solution Solution object from which customers will be removed.
     * @param sortedPositionsToRemove Sorted list of positions of customers (in the 'giant tour') that will be removed.
     */
    public final List<Integer> removeCustomersFromTours(Solution solution, List<Integer> sortedPositionsToRemove) {
        List<Integer> removedCustomers = new ArrayList<>();

        ArrayList<Vehicle> vehicles = solution.getVehicles();
        int nVehicles = vehicles.size(); // e.g. 25
        int runningPositionNr = 0; // counter to track at which point of the giant tour we are

        // We need to store the lengths of the tours before the removals.  Otherwise, the mapping of the positions
        // would be incorrect later on.
        int[] nCustomersInTourBeforeRemoval = new int[nVehicles];
        for (Vehicle vehicle: vehicles) {
            nCustomersInTourBeforeRemoval[vehicle.getId()] = vehicle.getnCustomersInTour();
        }

        // Keeps track of the vehicle (tour) with which we start to check if a customer can be removed.
        // E.g. if position to remove is 15 and vehicle (id=0) visits only 9 customer, then this vehicle does
        // not need to be considered.
        int vStart = 0; // initially start with vehicle at position 0 in list of vehicles

        // Once a customer is removed from a tour, the positions inside the tour do no longer correspond to the once
        // used to calculate the positionsToRemove.  Therefore, we keep track of the number of already removed
        // customers and use this information to correct the positions.
        int nCustomersAlreadyRemovedInTour = 0; // needed to map correct positions after customers were already removed from tour

        // Main loop.  Process all position at which customers should be removed.
        for (Integer removePosition: sortedPositionsToRemove) { // e.g. [1, 2, 5, 8, 15, 18, 26, 31, 34, 46, 51, 59, ...]

            // go over all vehicles, start with vStart
            for (int v = vStart; v < nVehicles; v++){
                Vehicle vehicle = vehicles.get(v);
                int vId = vehicle.getId();

                // If position to remove is high than the positions in the current tour, set vStart to the next vehicle
                // and update the running position nr.
                if ( runningPositionNr + nCustomersInTourBeforeRemoval[vId] <= removePosition) {
                    runningPositionNr += nCustomersInTourBeforeRemoval[vId];
                    vStart++;  // (0), 9, 7, 11, 8 : --> 9, 16, 27, 35
                    // When we start with a new vehicle, we have to reset the counter of already assigned customers.
                    nCustomersAlreadyRemovedInTour = 0;
                } else {
                    // If position to remove is in current vehicle, determine index of position to remove in tour of
                    // the current vehicle.
                    int removeFromTour = removePosition - runningPositionNr - nCustomersAlreadyRemovedInTour + 1; // +1 because first node in tour is dummy for leaving depot

                    // apply the removal
                    int removedCustomer = vehicle.applyRemoval(removeFromTour, this.data);
                    removedCustomers.add(removedCustomer);
                    nCustomersAlreadyRemovedInTour++;
                    break; // break as we have processed the position to remove and don't need to check additional vehicles
                }
            }
        }

        return removedCustomers;
    }

    final int getNRemovals(Solution solution) {
        int nRemovals = CalcUtils.getRandomNumberInClosedRange(Config.lowerBoundRemovals, Config.upperBoundRemovals);
        int nrOfAssignedCustomers = solution.getNrOfAssignedCustomers();
        if (nRemovals > nrOfAssignedCustomers) nRemovals = nrOfAssignedCustomers;
        return nRemovals;
    }

    public final void destroy(Solution solution) throws ArgumentOutOfBoundsException {

        // get number of removals based on parameters defined in config file
        int nRemovals = getNRemovals(solution);

        List<Integer> removedCustomers = this.operatorSpecificDestroy(solution, nRemovals);

        // Update the solution object.  The tours of the vehicle are already update by the removals.  However, global
        // information such as the total costs and list of notAssignedCustomers still need to be updated.
        solution.updateSolutionAfterRemoval(removedCustomers);

    }

    abstract List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException;


}
