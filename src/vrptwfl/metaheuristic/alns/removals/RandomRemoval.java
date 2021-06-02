package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.List;


// Randomly remove customers
// Ropke and Pisinger 2006, page 460 (Transportation Science)
public class RandomRemoval extends AbstractRemoval {

    public RandomRemoval(Data data) {
        super(data);
    }

    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        // get index positions of the nRemovals customers to be removed (there are nCustomers - number of not assigned customers that can be removed)
        // NOTE: these positions are the positions in the tours (not the customer ids!)
        List<Integer> sortedPositionsToRemove =  CalcUtils.getSortedUniqueRandomNumbersInRange(nRemovals, 0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);

        // TODO wieder raus, stattdessen TEST CASE!! jeweils 1x, dass ranges immer passen (keiner außerhalb)
//        System.out.println("\n\nRemovals " + nRemovals + "\t[" + Config.lowerBoundRemovals + ", " + Config.upperBoundRemovals + "]");
//        System.out.println(sortedPositionsToRemove);

        return this.removeCustomersFromTours(solution, sortedPositionsToRemove);

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

}
