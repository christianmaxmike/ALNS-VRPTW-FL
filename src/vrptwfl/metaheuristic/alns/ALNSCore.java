package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.ArrayList;
import java.util.List;

public class ALNSCore {

    private Data data;

    public ALNSCore(Data data) {
        this.data = data;
    }

    public Solution runALNS(Solution solution) {

//        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
        for (int iteration = 1; iteration <= 2; iteration++) {

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            this.destroyRandom(solution);

            // repair solution
            // TODO erstmal nur mit k=2
            this.repairRegret(solution, 2);

            break; // TODO wieder raus

        }

        return solution;
    }

    private void destroyRandom(Solution solution) {

        // get number of removals based on parameters defined in config file
        int nRemovals = CalcUtils.getRandomNumberInClosedRange(Config.lowerBoundRemovals, Config.upperBoundRemovals); // +1 needed, otherwise UB would not be included

        // get index positions of the nRemovals customers to be removed (there are nCustomers - number of not assigned customers that can be removed)
        // NOTE: these positions are the positions in the tours (not the customer ids!)
        List<Integer> positionsToRemove =  CalcUtils.getUniqueRandomNumbersInRange(nRemovals, 0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);

        // TODO wieder raus, stattdessen testcase!! jeweils 1x, dass ranges immer passen (keiner außerhalb)
        System.out.println("\n\nRemovals " + nRemovals + "\t[" + Config.lowerBoundRemovals + ", " + Config.upperBoundRemovals + "]");
        System.out.println(positionsToRemove);

        List<Integer> removedCustomers = new ArrayList<>();

        // TODO mehr Beschreibung, was generelle Idee hier ist
        int runningPositionNr = 0;
        ArrayList<Vehicle> vehicles = solution.getVehicles();
        int nVehicles = vehicles.size(); // e.g. 25

        // TODO Beschreibung, wofuer wir das brauchen
        int[] nCustomersInTourBeforeRemoval = new int[nVehicles];
        for (Vehicle vehicle: vehicles) {
            nCustomersInTourBeforeRemoval[vehicle.getId()] = vehicle.getnCustomersInTour();
        }

        int vStart = 0; // initially start with vehicle at position 0 in list of vehicles

        int nCustomersAlreadyRemovedInTour = 0; // needed to map correct positions after customers were already removed from tour

        for (Integer removePosition: positionsToRemove) { // e.g. [1, 2, 5, 8, 15, 18, 26, 31, 34, 46, 51, 59, ...]
            System.out.println("\nRemove position: " + removePosition); // TODO wieder raus
            for (int v = vStart; v < nVehicles; v++){
                Vehicle vehicle = vehicles.get(v);
                int vId = vehicle.getId();

                if ( runningPositionNr + nCustomersInTourBeforeRemoval[vId] <= removePosition) {
                    runningPositionNr += nCustomersInTourBeforeRemoval[vId];
                    vStart++;  // (0), 9, 7, 11, 8 : --> 9, 16, 27, 35
                } else {

                    int removeFromTour = removePosition - runningPositionNr - nCustomersAlreadyRemovedInTour + 1; // +1 because first node in tour is dummy for leaving depot
                    // TODO print outs wieder raus
                    System.out.println("vehicle jobs: " + vehicle.getnCustomersInTour() + "\t" + nCustomersInTourBeforeRemoval[vId]);
                    System.out.println("removePosition " + removePosition);
                    System.out.println("runningPositionNr " + runningPositionNr);
//                    System.out.println("correctIndexWithinTour " + correctIndexWithinTour);
                                        System.out.println("nCustomersAlreadyRemoved " + nCustomersAlreadyRemovedInTour);
                    System.out.println("removeFromTour " + removeFromTour);
                    int removedCustomer = vehicle.applyRemoval(removeFromTour, this.data);
//                    solution.addCustomerToNotAssignedCustomers(removedCustomer);
                    removedCustomers.add(removedCustomer);
                    nCustomersAlreadyRemovedInTour++;
                    break;
                }
                nCustomersAlreadyRemovedInTour = 0;
            }

        }

        solution.updateSolution(removedCustomers);
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



        // TODO effizient die Kunden bekommen, die in den Touren verplant sind
        //  random diese Kunden rausnehmen und auf die Liste der unschedult customers packen



        // TODO apply removal (analog zu insertion)

        // TODO
    }

    private void repairRegret(Solution solution, int k) {
        // TODO analog zu dem Regret in Construction
        //  Frage: wohin muessen generische methoden ausgelagert werden?


    }


}
