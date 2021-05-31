package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.Map;

public class WorstRemoval extends AbstractRemoval {

    public WorstRemoval(Data data) {
        super(data);
    }

    // see Ropke & Pisinger 2006, p. 460 Algorithm 3 (An ALNS Heuristic for the PDPTW)
    @Override
    public void destroy(Solution solution) {

        // get number of removals based on parameters defined in config file
        int nRemovals = getNRemovals();

        Map<int[], Double> mycache; 

        while (nRemovals > 0) {



            // calculate cost of removing customers from route
            // cache already calculated removals [pred, cust, succ]; wenn es das schon gibt, nicht neu rechnen

            double rand = Config.randomGenerator.nextDouble();
//            int idx = Math.pow(rand, Config.worstRemovalExponent)*laenge_planned_requests;

            nRemovals--;
        }

    }
}
