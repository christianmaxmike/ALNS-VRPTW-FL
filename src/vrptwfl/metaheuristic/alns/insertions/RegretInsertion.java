package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

public class RegretInsertion extends AbstractInsertion {

    private int k;

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public RegretInsertion(int k, Data data) throws ArgumentOutOfBoundsException {

        super(data);

        // enforce k > 1. otherwise, no regret measure possible
        if (k <= 1) throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");

        this.k = k;
    }

    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double maxRegret = -1;
        
        // nextInsertion : [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        // Chris; new nextInsertion : [customerID, vehicleID, idxPositionInRoute, serviceStartTime, additionalCosts, preferencedLocation, capacitySlot]
        double[] nextInsertion = new double[8]; 
        // positionInRoute is defined as the position at which the customer will be inserted
        nextInsertion[4] = Config.bigMRegret;

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
        
        while(iter.hasNext()) {

            // init info for customer
            int customer = iter.next();
            
            double regret = -1;

            // get all possible insertions for the customer
            // entries have the form: 
    		// customer, vehicleId, posInRoute, starTime, costs, location, capacity, entryIdxInLoc
            ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);

            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {
            	if (solution.checkSchedulingOfPredecessors(customer)) {
            		solution.getTempInfeasibleCustomers().add(customer);
            		iter.remove();            		
            	}
            } else {
                // get regret by sorting list and calculating difference between best and k-th best insertion
                regret = this.calculateRegret(this.k, possibleInsertionsForCustomer);

                // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                if (regret > maxRegret - Config.epsilon) {  // check if regret >= maxRegret
                    // either (regret > maxRegret) or (regret == maxRegret but lower insertion cost (tie-breaker))
                    if ((regret > maxRegret + Config.epsilon) || (nextInsertion[4] < possibleInsertionsForCustomer.get(0)[4] + Config.epsilon)) {
                        maxRegret = regret;
                        nextInsertion = possibleInsertionsForCustomer.get(0);
                    }
                }
            }
        } // END WHILE
        return nextInsertion;
    }

    // TODO die zwei Methoden zu Insertion helpers auslagern
    // TODO: Testcase, dass k<2 nicht akzeptiert wird
    // Method is public such that logic can be tested
    public double calculateRegret(int k, ArrayList<double[]> possibleInsertionsForCustomer) {
        double regret = 0.;
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

        for (int i = k; i>=2; i--) {
            if (possibleInsertionsForCustomer.size() >= i) {
                // if k-regret can be calculated as there enough at least k insertions
                regret += possibleInsertionsForCustomer.get(i - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
            } else {
                // if list has entries, but not k (i.e. not enough to calculate k-regret)
                regret += (i - possibleInsertionsForCustomer.size())*Config.bigMRegret - possibleInsertionsForCustomer.get(0)[4];
            }

            // if only the regret between n-th and best should be considered, break loop
            if (!Config.regretSumOverAllNRegret) {
                break;
            }
        }

//        if (possibleInsertionsForCustomer.size() >= k) {
//            // if k-regret can be calculated as there enough at least k insertions
//            regret = possibleInsertionsForCustomer.get(k - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
//        } else {
//            // if list has entries, but not k (i.e. not enough to calculate k-regret)
//            int bigM = Config.bigMRegret;
//            regret = (k-possibleInsertionsForCustomer.size())*bigM - possibleInsertionsForCustomer.get(0)[4];
//        }
        return regret;
    }

	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}

}
