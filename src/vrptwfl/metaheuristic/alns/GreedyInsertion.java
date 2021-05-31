package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.ListIterator;

public class GreedyInsertion extends AbstractInsertion {

    public GreedyInsertion(Data data) {
        super(data);
    }

    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double minCostIncrease = Config.bigMRegret;

        double[] nextInsertion = new double[5]; // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        nextInsertion[4] = -1; // positionInRoute is defined as the position at which the customer will be inserted

        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();

        while(iter.hasNext()){

            // init info for customer
            int customer = iter.next();

            // get all possible insertions for the customer
            ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);

            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {

                solution.getTempInfeasibleCustomers().add(customer);
                iter.remove();
            } else {
                double[] possibleInsertion = possibleInsertionsForCustomer.get(0);

                // compare cost increase to currently best (lowest) cost increase
                if (possibleInsertion[4] + Config.epsilon < minCostIncrease) {
                    nextInsertion = possibleInsertion;
                }
            }
        } // end while(iter.hasNext())
        return nextInsertion;
    }


}
