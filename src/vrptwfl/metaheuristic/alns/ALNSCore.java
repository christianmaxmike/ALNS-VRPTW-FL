package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.ArrayList;
import java.util.List;

public class ALNSCore {

    private Data data;

    public ALNSCore(Data data) {
        this.data = data;
    }

    public Solution runALNS(Solution solutionConstr) throws ArgumentOutOfBoundsException {

        // init ALNS
        Solution solutionCurrent = solutionConstr.copyDeep();
        Solution solutionBestGlobal = solutionConstr.copyDeep();

        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
//        for (int iteration = 1; iteration <= 10_000; iteration++) {

            Solution solutionTemp = solutionCurrent.copyDeep();

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            this.destroyRandom(solutionTemp);

            // repair solution
            // TODO erstmal nur mit k=2
            solutionTemp = this.repairRegret(solutionTemp, 2);

            if (iteration % 1000 == 0) {
                System.out.println("\n\nIteration " + iteration);
                System.out.println("Cost temp " + solutionTemp.getTotalCosts());
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }

            solutionCurrent = this.checkImprovement(solutionTemp, solutionCurrent, solutionBestGlobal);
            if (iteration % 1000 == 0) {
                System.out.println();
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }

        }

        return solutionBestGlobal;
    }

    // TODO hier brauchen wir auch noch Test cases
    private Solution checkImprovement(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {

        // feasible solution ?
        // TODO hier kommt dann wahrscheinlichkeit etc rein, dass trotzdem schlechtere loesung
        //  angenommen wird
        if (solutionTemp.isFeasible()) {
            // improvement
            if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                // check if also better than best global
                if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                    solutionBestGlobal.setSolution(solutionTemp);
                }
                return solutionTemp;
            }
        }

        // no improvement
        return solutionCurrent;
    }


    private void destroyRandom(Solution solution) {

        // get number of removals based on parameters defined in config file
        int nRemovals = CalcUtils.getRandomNumberInClosedRange(Config.lowerBoundRemovals, Config.upperBoundRemovals); // +1 needed, otherwise UB would not be included

        // get index positions of the nRemovals customers to be removed (there are nCustomers - number of not assigned customers that can be removed)
        // NOTE: these positions are the positions in the tours (not the customer ids!)
        List<Integer> sortedPositionsToRemove =  CalcUtils.getSortedUniqueRandomNumbersInRange(nRemovals, 0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);

        // TODO wieder raus, stattdessen TEST CASE!! jeweils 1x, dass ranges immer passen (keiner außerhalb)
//        System.out.println("\n\nRemovals " + nRemovals + "\t[" + Config.lowerBoundRemovals + ", " + Config.upperBoundRemovals + "]");
//        System.out.println(sortedPositionsToRemove);

        this.removeCustomersFromTours(solution, sortedPositionsToRemove);

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
    private void removeCustomersFromTours(Solution solution, List<Integer> sortedPositionsToRemove) {
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

        // Update the solution object.  The tours of the vehicle are already update by the removals.  However, global
        // information such as the total costs and list of notAssignedCustomers still need to be updated.
        solution.updateSolutionAfterRemoval(removedCustomers);
    }

    //        // access all customers assigned to vehicles (only these can be removed)
//        int runningPositionNr = 0;
//        for (Vehicle vehicle: solution.getVehicles()) {
//            int nCustomersInTourBeforeRemoval = vehicle.getnCustomersInTour();
//
//
//            // raise running position number
//            runningPositionNr += nCustomersInTourBeforeRemoval;
//        }

    // TODO Testcase, mit genau den Touren und den Indices, ob das auch alles so passt
//        Tour of vehicle 0 (n=9):
//        0 -> 40 -> 26 -> 28 -> 27 -> 53 -> 12 -> 4 -> 24 -> 68 -> 0
//        Tour of vehicle 1 (n=7):
//        0 -> 3 -> 78 -> 76 -> 79 -> 35 -> 65 -> 34 -> 0
//        Tour of vehicle 2 (n=11):
//        0 -> 92 -> 98 -> 91 -> 16 -> 99 -> 84 -> 5 -> 93 -> 42 -> 2 -> 58 -> 0
//        Tour of vehicle 3 (n=8):
//        0 -> 41 -> 15 -> 87 -> 22 -> 74 -> 72 -> 21 -> 73 -> 0
//        Tour of vehicle 4 (n=9):
//        0 -> 63 -> 62 -> 11 -> 64 -> 49 -> 36 -> 47 -> 19 -> 88 -> 0
//        Tour of vehicle 5 (n=8):
//        0 -> 57 -> 14 -> 44 -> 38 -> 43 -> 86 -> 100 -> 37 -> 0
//        Tour of vehicle 6 (n=8):
//        0 -> 56 -> 75 -> 23 -> 67 -> 39 -> 55 -> 25 -> 54 -> 0
//        Tour of vehicle 7 (n=9):
//        0 -> 46 -> 45 -> 83 -> 8 -> 60 -> 97 -> 13 -> 85 -> 59 -> 0
//        Tour of vehicle 8 (n=8):
//        0 -> 50 -> 1 -> 69 -> 81 -> 9 -> 66 -> 32 -> 71 -> 0
//        Tour of vehicle 9 (n=8):
//        0 -> 29 -> 33 -> 51 -> 20 -> 30 -> 90 -> 77 -> 80 -> 0
//        Tour of vehicle 10 (n=10):
//        0 -> 10 -> 70 -> 31 -> 7 -> 48 -> 82 -> 17 -> 61 -> 96 -> 6 -> 0
//        Tour of vehicle 11 (n=5):
//        0 -> 52 -> 18 -> 89 -> 94 -> 95 -> 0
//
//                [ 1,  2,  5,  8, 15, 18, 26, 31, 34, 46, 51, 59, 60, 61, 77, 81, 82, 98]
//[26, 28, 12, 68, 34, 91, 58, 74, 73, 44, 37, 54, 46, 45, 29, 30, 90, 94]

    private Solution repairRegret(Solution solution, int k) throws ArgumentOutOfBoundsException {
        // TODO analog zu dem Regret in Construction
        //  Frage: wohin muessen generische methoden ausgelagert werden?

        RegretInsertion inserter = new RegretInsertion(k, this.data);
        solution = inserter.solve(solution);

        return solution;

    }


}
