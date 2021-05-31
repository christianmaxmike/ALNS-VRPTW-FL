package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

public abstract class AbstractInsertion {

    private Data data;

    public AbstractInsertion(Data data) {
        this.data = data;
    }

    // final such that method cannot be accidentally overridden in subclass
    public final Solution solve(Solution solution) {

        while (!solution.getNotAssignedCustomers().isEmpty()) {
            double[] nextInsertion = this.getNextInsertion(solution);

            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                // select the vehicle for which the insertion was calculated, then apply insertion to that vehicle
                solution.getVehicles().get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                solution.getNotAssignedCustomers().remove(Integer.valueOf((int) nextInsertion[0]));
            }
        }

        // update solution object, then return it
        solution.updateSolutionAfterInsertion();

        return solution;
    }

    abstract double[] getNextInsertion(Solution solution);

}
