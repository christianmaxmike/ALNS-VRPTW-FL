package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;

public class ALNSCore {

    public Solution runALNS(Solution solution) {

        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
            int i = 2 + 3;

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

        // min und max anzahl von removals

        // TODO apply removal (analog zu insertion)

        // TODO
    }

    private void repairRegret(Solution solution, int k) {
        // TODO analog zu dem Regret in Construction
        //  Frage: wohin muessen generische methoden ausgelagert werden?



    }

}
