package vrptwfl.metaheuristic.alns.insertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.WriterUtils;

/**
 * This class implements the backtracking mechanism using the 
 * k-regret heuristic for its insertions. 
 *
 * @author Christian M.M. Frey
 *
 */
public class RegretInsertionBacktracking extends AbstractInsertion {
	
    private int k;
    private int backtrackJump;    
    private int noBackTrackJumps;
    private ArrayList<Solution> solutionSequence;
    private Solution bestSolutionInTrial = null;
    private Solution bestSolution = null;

   /**
    * Initialize k-regret w/ backtracking heuristic
    * @param k: k defines what regret measure to use; e.g. k=3 means difference between best insertion and 3rd best insertion
    * @param data
    * @throws ArgumentOutOfBoundsException
    */
    public RegretInsertionBacktracking(int k, Data data) throws ArgumentOutOfBoundsException {
        super(data);
        if (k <= 1) 
        	throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");
        this.k = k;
        this.backtrackJump = Config.getInstance().backtrackJump;
        this.noBackTrackJumps = 0;
    }
    
    public Solution runBacktracking(Solution solution) {
    	// Try backtracking for x trials
    	for (int trial = 0; trial<Config.getInstance().backtrackTrials; trial++) {
    		long startTime = System.currentTimeMillis();
    		this.noBackTrackJumps = 0;
    		bestSolutionInTrial = null;
    		System.out.println("Backtracking Trial:" + trial);
    		
    		// Get a copy of the 'empty' solution
    		Solution initSolution = solution.copyDeep();
    		// The list solutionSequence stores the current path in the backtracking-tree
    		solutionSequence = new ArrayList<Solution>();
    		// Add initial solution to solution sequence
    		solutionSequence.add(initSolution);
    		
    		// Set initial solution as starting point
    		// currSolution indicates the current node we try to explore in the backtracking-tree
    		Solution currSolution = initSolution;
    		
    		// loop while there are unscheduled customers
    		//while (!currSolution.isFeasible() && !currSolution.getNotAssignedCustomers().isEmpty() && this.noBackTrackJumps < Config.getInstance().maxBacktrackJumps) {
        	while (!currSolution.isFeasible() && this.noBackTrackJumps < Config.getInstance().maxBacktrackJumps) {
    			boolean backTrackFlag = false;
        		if (currSolution.getNotAssignedCustomers().isEmpty())
        			backTrackFlag = true;

        		// receive the next possible insertions in the current node in the backtracking-tree
    			// if in a backtrack-node a tuple (customerID, vehicleId,...) has already been tried, it 
    			// is not further considered as a possible next insertions, i.e, the subprocedure
    			// getNextInsertion() filters tuples having already been explored in earlier iterations
    			double[] nextInsertion = this.getNextInsertion(currSolution);
    			
    			// check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
    			if (nextInsertion[0] != -1) {
    				
    				// store explored insertion in the current solution
    				// calling getNextInsertion in the next iteration filters these already explored tuples
    				if (currSolution.getTriedInsertions().get((int) nextInsertion[0]) == null) 
    					currSolution.getTriedInsertions().put((int) nextInsertion[0], new ArrayList<double[]>());
    				currSolution.getTriedInsertions().get((int) nextInsertion[0]).add(nextInsertion);        	
    				
    				// continue on a copy -> going down one level in the backtracking-tree
    				currSolution = currSolution.copyDeep();
    				
    				// select the vehicle for which the insertion was calculated, then apply insertion to that vehicle
    				currSolution.getVehicles().get((int) nextInsertion[1]).applyInsertion(nextInsertion, currSolution.getData(), currSolution);
    				
    				// remove element from list of notAssignedCustomers
    				currSolution.getNotAssignedCustomers().remove(Integer.valueOf((int) nextInsertion[0]));
    				
    				// update solution object
    				currSolution.updateSolutionAfterInsertion(false);
    				
    				// check for new best solution

    				//  solutionTemp.getTotalPenalyCosts() <= solutionBestGlobal.getTotalPenalyCosts()
    				if (this.bestSolutionInTrial==null || currSolution.getTotalCosts() < this.bestSolutionInTrial.getTotalCosts()  ) 
    					this.bestSolutionInTrial = currSolution;
    				
    				// Add the current solution in the solution sequence (path in the backtracking-tree) we currently explore
    				solutionSequence.add(currSolution);
    			}
    			else {
    				/*
    				// Get id in the solution sequence (=path in the backtracking-tree) where we jump back
    				int jumpToSolIdx = getJumpIdx(solutionSequence.size() - 1);
    				// Set the current solution
    				currSolution = solutionSequence.get(jumpToSolIdx);
    				// delete all successors from the current path (=old (explored) branch in the backtracking-tree)
    				for (int removeIdx = solutionSequence.size()-1 ; removeIdx > jumpToSolIdx; removeIdx--)
    					solutionSequence.remove(removeIdx);
    				
    				// increment counter for number of backtrack jumps
    				this.noBackTrackJumps ++;
    				*/
    				backTrackFlag = true;
    			}
    			
    			if (backTrackFlag)
    				currSolution = doBackTrack();
    		}
    		this.bestSolutionInTrial.updateSolutionAfterInsertion(false);
    		System.out.println("Number of backtrack jumps in this trial: " + this.noBackTrackJumps);
    		System.out.println("Costs of best solution found: " + this.bestSolutionInTrial.getTotalCosts());
    		WriterUtils.writeBacktrackingInfo(trial, noBackTrackJumps, this.bestSolutionInTrial.getTotalCosts(), (System.currentTimeMillis() - startTime));
    		
    		if (this.bestSolution==null || this.bestSolutionInTrial.getTotalCosts() < this.bestSolution.getTotalCosts())
    			this.bestSolution = this.bestSolutionInTrial;
    	}
    	
        // update best solution object, then return it
        // this.bestInitialSolution.updateSolutionAfterInsertion();
        this.bestSolution.calculateTotalCosts(false);
    	return this.bestSolution;
    }
    
    private Solution doBackTrack() {
		// Get id in the solution sequence (=path in the backtracking-tree) where we jump back
		int jumpToSolIdx = getJumpIdx(solutionSequence.size() - 1);
		// Set the current solution
		Solution currSolution = solutionSequence.get(jumpToSolIdx);
		// delete all successors from the current path (=old (explored) branch in the backtracking-tree)
		for (int removeIdx = solutionSequence.size()-1 ; removeIdx > jumpToSolIdx; removeIdx--)
			solutionSequence.remove(removeIdx);
		
		// increment counter for number of backtrack jumps
		this.noBackTrackJumps ++;
		return currSolution;
    }

    /**
     * Retrieve the next possible insertions following the k-regret heuristic. 
     * The method iterates all unscheduled customers, identifies the next possible insertions for them, and
     * yields the next insertion according to the k-regret heuristic.
     * The regret scores are calculated by the function calculateRegret.
     * 
     * @param solution solution object storing information about the scheduled and unscheduled customers
     */
    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double maxRegret = -1;
        
        // nextInsertion : [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        // Chris; new nextInsertion : [customerID, vehicleID, idxPositionInRoute, serviceStartTime, additionalCosts, preferencedLocation, capacitySlot]
        double[] nextInsertion = new double[8]; 
        // positionInRoute is defined as the position at which the customer will be inserted
        nextInsertion[0] = -1;
        nextInsertion[4] = -1; //Config.getInstance().bigMRegret;

        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();

        while(iter.hasNext()) {
            // init info for customer
            int customer = iter.next();

            // get all possible insertions for the customer
            // entries have the form: 
    		// customer, vehicleId, posInRoute, starTime, costs, location, capacity, entryIdxInLoc
            final ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);
            
            // check if one of the possibleInesrtions have already been tried in earlier iterations of the backtracking procedure
            // if yes: branch in backtracking has already been explored
            // if no: still a valid insertion
            if (solution.getTriedInsertions().get(customer) != null) {
            	for (int j = possibleInsertionsForCustomer.size()-1; j>=0; j--) {
            		final int fj = j;
            		boolean check =  solution.getTriedInsertions().get(customer).stream().anyMatch(a -> Arrays.equals(a, possibleInsertionsForCustomer.get(fj)));
            		if (check) {
            			// System.out.println("Insert known from prev. its -> remove" + Arrays.toString(possibleInsertionsForCustomer.get(fj)));
            			possibleInsertionsForCustomer.remove(j);
            		}
            	}
            }
            
            double regret = -1;
            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {
            	if (solution.checkSchedulingOfPredecessors(customer)) {
            		solution.getTempInfeasibleCustomers().add(customer);
            		iter.remove();            		
            	}
            } else {
                // get regret by sorting list and calculating difference between best and k-th best insertion
                //regret = this.calculateRegret(this.k, possibleInsertionsForCustomer);
                regret = this.calculateRegret(CalcUtils.getRandomNumberInClosedRange(2, 3), possibleInsertionsForCustomer);

                // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                if (regret > maxRegret - Config.getInstance().epsilon) {  // check if regret >= maxRegret
                    // either (regret > maxRegret) or (regret == maxRegret but lower insertion cost (tie-breaker))
                    if ((regret > maxRegret + Config.getInstance().epsilon) || (nextInsertion[4] < possibleInsertionsForCustomer.get(0)[4] + Config.getInstance().epsilon)) {
                        maxRegret = regret;
                        nextInsertion = possibleInsertionsForCustomer.get(0);
                    }
                } 
                
                else {
                	if (solution.checkSchedulingOfPredecessors(customer)) {
                		solution.getTempInfeasibleCustomers().add(customer);
                		iter.remove();            		
                	}
                }
            }
        } // END WHILE

        return nextInsertion;
    }

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
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

        for (int i = k; i>=2; i--) {
            if (possibleInsertionsForCustomer.size() >= i) 
                // if k-regret can be calculated as there enough at least k insertions
                regret += possibleInsertionsForCustomer.get(i - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
            else
                // if list has entries, but not k (i.e. not enough to calculate k-regret)
                regret += (i - possibleInsertionsForCustomer.size())*Config.getInstance().bigMRegret - possibleInsertionsForCustomer.get(0)[4];
            
            // if only the regret between n-th and best should be considered, break loop
            if (!Config.getInstance().regretSumOverAllNRegret) 
                break;
        }
        return regret;
    }
    
    /**
     * Retrieve the index to the position the backtracking mechanism
     * is jumping back.
     * @param depth: the current depth of the backtracking tree
     * @return jump index 
     */
    private int getJumpIdx(int depth) {
    	int jumpToSolIdx = 0;
    	if (Config.getInstance().backtrackBySteps)
    		jumpToSolIdx = Math.max( (depth) - this.backtrackJump, 0);
    	else {
    		int startIdx = Math.min(Config.getInstance().backtrackJumpToLevelProbabilities.length-1, depth-1);
    		for (int j = startIdx; j>=0; j--) {
    			double rand = Config.getInstance().randomGenerator.nextDouble();
    			if (rand < Config.getInstance().backtrackJumpToLevelProbabilities[j]) {
    				jumpToSolIdx = j;
    				break;
    			}
    		}
    		// logic of jumping to a fixed level (=no probabilities)
    		// jumpToSolIdx = Math.min( depth, Config.getInstance().backtrackJumpToLevel);
    	}
    	return jumpToSolIdx;
    }
    
    private void updateBestSolution () {
    	for (Solution s: this.solutionSequence) {
    		if (this.bestSolutionInTrial == null || s.getTotalCosts() < this.bestSolutionInTrial.getTotalCosts())
    			this.bestSolutionInTrial = s;
    	}
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Regret Insertion Backtracking (k=" + this.k + ")";
	}
}