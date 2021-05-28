package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.List;

public class ALNSCore {

    private Data data;

    public ALNSCore(Data data) {
        this.data = data;
    }

    public Solution runALNS(Solution solution) {

        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
            int i = 2 + 3;

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            this.destroyRandom(solution);

            // repair solution
            // TODO erstmal nur mit k=2
            this.repairRegret(solution, 2);
        }

        return solution;
    }

    private void destroyRandom(Solution solution) {
        // TODO effizient die Kunden bekommen, die in den Touren verplant sind
        //  random diese Kunden rausnehmen und auf die Liste der unschedult customers packen

        // get number of removals based on parameters defined in config file
        int nRemovals = CalcUtils.getRandomNumberInClosedRange(Config.lowerBoundRemovals, Config.upperBoundRemovals); // +1 needed, otherwise UB would not be included

        // get index positions of the nRemovals customers to be removed (there are nCustomers - number of not assigned customers that can be removed)
        List<Integer> positionsToRemove =  CalcUtils.getUniqueRandomNumbersInRange(nRemovals, 0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);

        System.out.println("nRemovals " + nRemovals + "\t[" + Config.lowerBoundRemovals + ", " + Config.upperBoundRemovals + "]");
        System.out.println(positionsToRemove);


        // 1. bestimme Anzahl zu entfernender Kunden: q

        // 2. bestimme welche kunden:
        //    - wir wissen wie viele Kunden ingesamt in Tour sind (nCust - notAssigned)
        //    - q davon bestimmen


        // TODO apply removal (analog zu insertion)

        // TODO
    }

    private void repairRegret(Solution solution, int k) {
        // TODO analog zu dem Regret in Construction
        //  Frage: wohin muessen generische methoden ausgelagert werden?



    }

}
