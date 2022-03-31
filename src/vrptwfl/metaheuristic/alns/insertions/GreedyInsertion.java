package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class implements the greedy insertion heuristic.
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
        /*
        int[] unscheduledCostumers = DataUtils.convertListToArray(solution.getNotAssignedCustomers());
        Integer[] indexes = IntStream.range(0, unscheduledCostumers.length).boxed().toArray(Integer[]::new);
        Arrays.sort(indexes, Comparator.<Integer>comparingDouble(i -> solution.getData().getRequiredSkillLvl()[unscheduledCostumers[i]]).reversed());
        int[] it = new int[unscheduledCostumers.length];
        for (int i = 0 ; i< unscheduledCostumers.length; i++)
        	it[i] = unscheduledCostumers[indexes[i]];
        ArrayList<Integer> list = (ArrayList<Integer>) Arrays.stream(it).boxed().collect(Collectors.toList());
        ListIterator<Integer> iter = list.listIterator();
        */
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
                possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs
                double[] possibleInsertion = possibleInsertionsForCustomer.get(0);

                // compare cost increase to currently best (lowest) cost increase
                if (possibleInsertion[4] + Config.epsilon < minCostIncrease) {
                	minCostIncrease = possibleInsertion[4];  // update new min cost
                    nextInsertion = possibleInsertion;
                }
            }
        } // end while(iter.hasNext())
        return nextInsertion;
    }
}
