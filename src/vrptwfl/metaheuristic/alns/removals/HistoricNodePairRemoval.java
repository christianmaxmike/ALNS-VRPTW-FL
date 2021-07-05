package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.List;

public class HistoricNodePairRemoval extends AbstractRemoval {

    private boolean randomize;
    private ALNSCore alns;

    public HistoricNodePairRemoval(Data data, ALNSCore alns, boolean randomize) {
        super(data);
        this.randomize = randomize;
        this.alns = alns;
    }

    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

        while (nRemovals > 0) {
            // TODO HOW TO PASS THE NEIGHBOR GRAPH TO THE DESTROY OPERATOR
            ArrayList<double[]> possibleRemovals = solution.getPossibleRemovalsSortedByNeighborGraph(alns.getNeighborGraph());

            int idx = 0;
            if (this.randomize) {
                double rand = Config.randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.historicNodePairRemovalExponent) * possibleRemovals.size());
            }

            double[] removal = possibleRemovals.get(idx);
            solution.getVehicles().get((int) removal[1]).applyRemoval((int) removal[2], this.data);
            removedCustomers.add((int) removal[0]);

            nRemovals--;
        }


        return removedCustomers;
    }
}
