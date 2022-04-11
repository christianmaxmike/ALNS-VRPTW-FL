package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

/**
 * This class implements the greedy insertion heuristic.
 * The heuristic greedily inserts customers according to their currently 
 * best insertions scores.
 * 
 * @author: Alexander Jungwirth, Christian M.M. Frey
 */
public class GreedyInsertion extends AbstractInsertion {

	/**
	 * Constructor for the greedy insertion
	 * @param data: Data object
	 */
    public GreedyInsertion(Data data) {
        super(data);
    }

    /**
     * Retrieve the next insertion in a greedy fashion, i.e, get the insertion
     * with the lowest possible costs.
     */
    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double minCostIncrease = Config.bigMRegret;
        
        // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        double[] nextInsertion = new double[7]; 
        nextInsertion[4] = -1; // positionInRoute is defined as the position at which the customer will be inserted

        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();

        while(iter.hasNext()){

            // initialize next customer id
            int customer = iter.next();

            // get all possible insertions for the customer
            ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);

            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {
            	// check whether all predecessor jobs have been scheduled;
            	if (solution.checkSchedulingOfPredecessors(customer)) {
	                solution.getTempInfeasibleCustomers().add(customer);
	                iter.remove();
            	}
            } else {
                possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs
                double[] possibleInsertion = possibleInsertionsForCustomer.get(0);

                // compare cost increase to currently best (lowest) cost increase
                if (possibleInsertion[4] + Config.epsilon < minCostIncrease) {
                	minCostIncrease = possibleInsertion[4];  // update new min cost
                    nextInsertion = possibleInsertion;
                }
            }
        } 
        return nextInsertion;
    }

    /**
     * Runs the insertion heuristic with the backtracking logic.
     * TODO Chris : currently not implemented - ask Alex why Backtracking only w/ k-regret
     */
	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}
}
