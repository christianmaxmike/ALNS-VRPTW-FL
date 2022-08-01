package vrptwfl.metaheuristic.alns.insertions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;


/**
 * This class implements the sequential allocation heuristic (SAH)
 * according to Gartner et al. 2018
 * The heuristic greedily inserts customers having a fixed start time (step1 in the paper)
 * up to customers having a more flexible time window (step2 in the paper)
 * 
 * Reference:
 *   Daniel Gartner, Markus Frey & Rainer Kolisch (2018) 
 *   Hospital-wide therapist scheduling and routing: Exact and heuristic methods, 
 *   IISE Transactions on Healthcare Systems Engineering, 8:4, 268-279, 
 *   DOI: 10.1080/24725579.2018.1530314 
 * 
 * @author: Christian M.M. Frey
 */
public class SequentialAllocationHeuristic extends AbstractInsertion {

	/**
	 * Constructor for the greedy insertion
	 * @param data: Data object
	 */
    public SequentialAllocationHeuristic(Data data) {
        super(data);
    }

    /**
     * Retrieve the next insertion according to a customer's time flexibility
     * starting with the narrowest time windows.
     */
    @Override
    public double[] getNextInsertion(Solution solution) {
    	// System.out.println("Use Sequential Allocation Heuristic");
    	
        // initialize values
        double minCostIncrease = Config.getInstance().bigMRegret;
    	double timeFlexibility = -1;
        
        // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        double[] nextInsertion = new double[7]; 
        nextInsertion[4] = -1; // positionInRoute is defined as the position at which the customer will be inserted

        
        solution.getNotAssignedCustomers().sort(new Comparator<Integer>() {

			@Override
			public int compare(Integer customer1, Integer customer2) {
				double timeFlex1 = solution.getData().getLatestStartTimes()[customer1] - solution.getData().getEarliestStartTimes()[customer1];
				double timeFlex2 = solution.getData().getLatestStartTimes()[customer2] - solution.getData().getEarliestStartTimes()[customer2];
				if (timeFlex1 <= timeFlex2)
					return -1;
				else
					return 1;
			}
		});
        
        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();
        
        while(iter.hasNext()){

            // initialize next customer id
            int customer = iter.next();
			double timeFlex = solution.getData().getLatestStartTimes()[customer] - solution.getData().getEarliestStartTimes()[customer];
			if (timeFlexibility == -1 || timeFlexibility == timeFlex) {
				
				timeFlexibility = timeFlex;
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
					possibleInsertionsForCustomer.sort(Comparator.comparing(a -> (a[4]+a[8]))); // sort by additional costs
					double[] possibleInsertion = possibleInsertionsForCustomer.get(0);
					
					// compare cost increase to currently best (lowest) cost increase
					if ((possibleInsertion[4]+possibleInsertion[8]) + Config.getInstance().epsilon < minCostIncrease) {
						minCostIncrease = possibleInsertion[4] + possibleInsertion[8];  // update new min cost
						nextInsertion = possibleInsertion;
					}
				}	
			}
        } 
        return nextInsertion;
    }

    /**
     * Runs the insertion heuristic with the backtracking logic.
     */
	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Sequential Allocation Heuristic";
	}
}
