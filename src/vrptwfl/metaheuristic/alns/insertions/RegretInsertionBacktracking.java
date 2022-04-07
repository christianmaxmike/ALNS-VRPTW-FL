package vrptwfl.metaheuristic.alns.insertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

public class RegretInsertionBacktracking extends AbstractInsertion {
	
    private int k;
    private int backtrackJump;    
    private ArrayList<Solution> solutionSequence;
    private Solution bestInitialSolution = null;


   /**
    * Runs backtracking w/ k-regret heuristic
    * @param k: k defines what regret measure to use; e.g. k=3 means difference between best insertion and 3rd best insertion
    * @param data
    * @throws ArgumentOutOfBoundsException
    */
    public RegretInsertionBacktracking(int k, Data data) throws ArgumentOutOfBoundsException {
        super(data);
        if (k <= 1) 
        	throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");
        this.k = k;
        this.backtrackJump = Config.backtrackJump;
    }
    
    // TODO: loggen wie oft zurückgesprungen worden ist
    public Solution runBacktracking(Solution solution) { 
    	// Try backtracking for x trials
    	for (int trial = 0; trial<Config.backtrackTrials; trial++) {
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
    		
    		// loop while there are un-scheduled customers
    		while (!currSolution.getNotAssignedCustomers().isEmpty()) {
    			
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
    				currSolution.updateSolutionAfterInsertion();
    				
    				// check for new best solution
    				if (this.bestInitialSolution==null || currSolution.getTotalCosts() < this.bestInitialSolution.getTotalCosts() ) 
    					this.bestInitialSolution = currSolution;
    				
    				// Add the current solution in the solution sequence (path in the backtracking-tree) we currently explore
    				solutionSequence.add(currSolution);
    			}
    			else {
    				// Get id in the solution sequence (=path in the backtracking-tree) where we jump back
    				int jumpToSolIdx = getJumpIdx(solutionSequence.size() - 1 );
    				// Set the current solution
    				currSolution = solutionSequence.get(jumpToSolIdx);
    				// delete all successors from the current path (=old (explored) branch in the backtracking-tree)
    				for (int removeIdx = solutionSequence.size()-1 ; removeIdx > jumpToSolIdx; removeIdx--)
    					solutionSequence.remove(removeIdx);
    			}
    		}
    		
    	}
    	
        // update best solution object, then return it
        this.bestInitialSolution.updateSolutionAfterInsertion();
    	return this.bestInitialSolution;
    }

    @Override
    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double maxRegret = -1;
        
        // nextInsertion : [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        // Chris; new nextInsertion : [customerID, vehicleID, idxPositionInRoute, serviceStartTime, additionalCosts, preferencedLocation, capacitySlot]
        double[] nextInsertion = new double[8]; 
        // positionInRoute is defined as the position at which the customer will be inserted
        nextInsertion[0] = -1;
        nextInsertion[4] = Config.bigMRegret;

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
                regret = this.calculateRegret(this.k, possibleInsertionsForCustomer);

                // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                if (regret > maxRegret - Config.epsilon) {  // check if regret >= maxRegret
                    // either (regret > maxRegret) or (regret == maxRegret but lower insertion cost (tie-breaker))
                    if ((regret > maxRegret + Config.epsilon) || (nextInsertion[4] < possibleInsertionsForCustomer.get(0)[4] + Config.epsilon)) {
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

    private double calculateRegret(int k, ArrayList<double[]> possibleInsertionsForCustomer) {
        double regret = 0.;
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

        for (int i = k; i>=2; i--) {
            if (possibleInsertionsForCustomer.size() >= i) 
                // if k-regret can be calculated as there enough at least k insertions
                regret += possibleInsertionsForCustomer.get(i - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
            else
                // if list has entries, but not k (i.e. not enough to calculate k-regret)
                regret += (i - possibleInsertionsForCustomer.size())*Config.bigMRegret - possibleInsertionsForCustomer.get(0)[4];
            
            // if only the regret between n-th and best should be considered, break loop
            if (!Config.regretSumOverAllNRegret) 
                break;
        }
        return regret;
    }
    
    
    private int getJumpIdx(int depth) {
    	int jumpToSolIdx = 0;
    	if (Config.backtrackBySteps)
    		jumpToSolIdx = Math.max( (depth) - this.backtrackJump, 0);
    	else {
    		int startIdx = Math.min(Config.backtrackJumpToLevelProbabilities.length-1, depth-1);
    		for (int j = startIdx; j>=0; j--) {
    			double rand = Config.randomGenerator.nextDouble();
    			if (rand < Config.backtrackJumpToLevelProbabilities[j]) {
    				jumpToSolIdx = j;
    				break;
    			}
    		}
    		// logic of jumping to a fixed level (=no probabilities)
    		// jumpToSolIdx = Math.min( depth, Config.backtrackJumpToLevel);
    	}
    	return jumpToSolIdx;
    }
}