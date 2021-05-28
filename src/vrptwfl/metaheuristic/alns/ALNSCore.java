package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

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

        // TODO mehr Beschreibung, was generelle Idee hier ist
        int runningPositionNr = 0;
        int nVehicles = solution.getVehicles().size(); // e.g. 25
        int vStart = 0; // initially start with vehicle at position 0 in list of vehicles
        int nCustomersAlreadyRemoved = 0; // needed to map correct positions after customers were already removed from tour

        for (Integer removePosition: positionsToRemove) { // e.g. [29, 34, 38, 53, 56, 66, 91, 93]
            System.out.println("\nRemove position: " + removePosition); // TODO wieder raus
            for (int v = vStart; v < nVehicles; v++){
                Vehicle vehicle = solution.getVehicles().get(v);
                int nCustomersInTourBeforeRemoval = vehicle.getnCustomersInTour();

                if ( runningPositionNr + nCustomersInTourBeforeRemoval < removePosition) {
                    runningPositionNr += nCustomersInTourBeforeRemoval;
                    vStart++;  // (0), 9, 7, 11, 8 : --> 9, 16, 27, 35
                } else {

                    // iterate over all customers in list
                    int removeFromTour = removePosition - (runningPositionNr - nCustomersAlreadyRemoved);
                    // TODO print outs wieder raus
                    System.out.println("vehicle jobs: " + vehicle.getCustomers().size() + "\t" + nCustomersInTourBeforeRemoval);
                    System.out.println("removePosition " + removePosition);
                    System.out.println("runningPositionNr " + runningPositionNr);
                    System.out.println("nCustomersAlreadyRemoved " + nCustomersAlreadyRemoved);
                    vehicle.applyRemoval(removeFromTour, this.data);
                    nCustomersAlreadyRemoved++;
                    break;
                }
            }

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
