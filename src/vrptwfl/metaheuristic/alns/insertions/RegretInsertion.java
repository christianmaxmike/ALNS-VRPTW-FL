package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

/**
 * This class implements the k-regret insertion heuristic. 
 *
 * @author Alexander Jungwirth, Christian M.M. Frey
 *
 */
public class RegretInsertion extends AbstractInsertion {

    private int k;

    /**
     * Initialize k-regret insertion heuristic
     * @param k: k defines what regret measure to use; e.g. k=3 means difference between best insertion and 3rd best insertion
     * @param data
     * @throws ArgumentOutOfBoundsException
     */
    public RegretInsertion(int k, Data data) throws ArgumentOutOfBoundsException {

        super(data);

        // enforce k > 1. otherwise, no regret measure possible
        if (k <= 1) throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");

        this.k = k;
    }

    /**
     * Retrieve the next possible insertions following the k-regret heuristic. 
     * The method iterates all unscheduled customers, identifies the next possible insertions for them, and
     * yields the next insertion according to the k-regret heuristic.
     * The regret scores are calculated by the function calculateRegret.
     * 
     * @param solution solution object storing information about the scheduled and un-scheduled customers
     */
    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double maxRegret = -1;
        
        // nextInsertion : [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        // Chris; new nextInsertion : [customerID, vehicleID, idxPositionInRoute, serviceStartTime, additionalCosts, preferencedLocation, capacitySlot]
        double[] nextInsertion = new double[8]; 
        // positionInRoute is defined as the position at which the customer will be inserted
        nextInsertion[4] = -1; //Config.getInstance().bigMRegret;

        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();
        
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
                if (regret > maxRegret - Config.getInstance().epsilon) {  // check if regret >= maxRegret
                    // either (regret > maxRegret) or (regret == maxRegret but lower insertion cost (tie-breaker))
                    if ((regret > maxRegret + Config.getInstance().epsilon) || ((nextInsertion[4]+nextInsertion[8]) < (possibleInsertionsForCustomer.get(0)[4]+possibleInsertionsForCustomer.get(0)[8]) + Config.getInstance().epsilon)) {
                        maxRegret = regret;
                        nextInsertion = possibleInsertionsForCustomer.get(0);
                    }
                }
            }
        } // END WHILE
        return nextInsertion;
    }

    // TODO Alex - die zwei Methoden zu Insertion helpers auslagern
    // TODO Alex - Testcase, dass k<2 nicht akzeptiert wird
    /**
     * Calculate the regret scores for the possible insertions for a customer. 
     * The parameter k defines what regret measure to use; 
     * e.g. k=3 means difference between best insertion and 3rd best insertion
     * @param k: regret measure
     * @param possibleInsertionsForCustomer: list of possible insertions for a customer
     * @return regret score
     */
    private double calculateRegret(int k, ArrayList<double[]> possibleInsertionsForCustomer) {
        double regret = 0.;
        // possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> (a[4]+a[8]))); // sort by additional costs
        
        for (int i = k; i>=2; i--) {
            if (possibleInsertionsForCustomer.size() >= i) {
                // if k-regret can be calculated as there enough at least k insertions
            	// regret += possibleInsertionsForCustomer.get(i - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
            	regret += (possibleInsertionsForCustomer.get(i - 1)[4] + possibleInsertionsForCustomer.get(i - 1)[8]) 
            			- (possibleInsertionsForCustomer.get(0)[4] + possibleInsertionsForCustomer.get(0)[8]);
            } else {
                // if list has entries, but not k (i.e. not enough to calculate k-regret)
                // regret += (i - possibleInsertionsForCustomer.size())*Config.getInstance().bigMRegret - possibleInsertionsForCustomer.get(0)[4];
                regret += (i - possibleInsertionsForCustomer.size())*Config.getInstance().bigMRegret - (possibleInsertionsForCustomer.get(0)[4] + possibleInsertionsForCustomer.get(0)[8]);
            }

            // if only the regret between n-th and best should be considered, break loop
            if (!Config.getInstance().regretSumOverAllNRegret) {
                break;
            }
        }

//        if (possibleInsertionsForCustomer.size() >= k) {
//            // if k-regret can be calculated as there enough at least k insertions
//            regret = possibleInsertionsForCustomer.get(k - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
//        } else {
//            // if list has entries, but not k (i.e. not enough to calculate k-regret)
//            int bigM = Config.getInstance().bigMRegret;
//            regret = (k-possibleInsertionsForCustomer.size())*bigM - possibleInsertionsForCustomer.get(0)[4];
//        }
        return regret;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Regret Insertion (k=" + this.k + ")";
	}
}