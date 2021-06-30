package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// remove customers which yield highest cost reduction if they are removed
// Ropke and Pisinger 2006, page 460 (Transportation Science)
public class WorstRemoval extends AbstractRemoval {

    private final boolean randomize;

    public WorstRemoval(Data data, boolean randomize) {
        super(data);
        this.randomize = randomize;
    }

    // see Ropke & Pisinger 2006, p. 460 Algorithm 3 (An ALNS Heuristic for the PDPTW)
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

//        Map<int[], Double> myCache; // TODO hier gehts mitm Guava Cache weiter

        // TODO kann man ausnuzten, dass bei jedem removal immer nur ein vehicle betroffen ist?
        //  eigentlich duerfte deswegen nur eine

        while (nRemovals > 0) {

            // TODO fuer jeden Kunden in Loesung: gib mir position und costReduction zurueck

            // calculate cost of removing customers from route
            ArrayList<double[]> possibleRemovals = solution.getPossibleRemovalsSortedByCostReduction();

            // TODO cache already calculated removals [pred, cust, succ]; wenn es das schon gibt, nicht neu rechnen

            int idx = 0;
            if (this.randomize) {
                double rand = Config.randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.worstRemovalExponent) * possibleRemovals.size());
            }
//            System.out.println(possibleRemovals.get(0)[3] +"\t"+ possibleRemovals.get(possibleRemovals.size()-1)[3]); // TODO wieder raus
            double[] removal = possibleRemovals.get(idx);


            // apply removal {customer, vehicle.id, i, travelTimeReduction}
            // TODO ggf. eigene applyRemovalMethode, wo kosten nicht nochmal ausgerechnet werden muessen
            solution.getVehicles().get((int) removal[1]).applyRemoval((int) removal[2], this.data);
            removedCustomers.add((int) removal[0]);

            nRemovals--;
        }

        return removedCustomers;
    }

}
