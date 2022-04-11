package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

/**
 * This class implements an abstract insertion object
 * 
 * @author: Alexander Jungwirth, Christian M.M. Frey
 */
public abstract class AbstractInsertion {

    private Data data;
    protected double pi;
    protected double probability;
    protected double weight;
    protected int draws;

	/**
	 * This abstract class implements the basics for an
	 * removal operation 
	 * @param data - contains data of loaded instance
	 * {@value #pi} pi - 
	 * {@value #probability} probability - probability of drawing operation
	 * {@value #weight} weight - weights of operation
	 */
    public AbstractInsertion(Data data) {
        this.data = data;
    }

    //
    // ABSTRACT METHODS
    //
    /**
     * Abstract method for retrieving the next possible insertion in the 
     * solution being attached as parameter
     * @param solution: Solution object being inspected
     * @return double array containg the next possible insertion
     */
    abstract double[] getNextInsertion(Solution solution);
    
    public abstract Solution runBacktracking(Solution initSolution);


    //
    // FUNCTIONALITY
    //
    /**
     * Function handles the general solving procedure. 
     * For each un-assigned customer it is checked whether the customer can be scheduled
     * in the current solution. If so, the function applies the insertion iteratively
     * and update the solution object.
     * @param solution
     * @return updated solution
     */
    public final Solution solve(Solution solution) {
        // function is final such that method cannot be accidentally overridden in subclass
        
    	while (!solution.getNotAssignedCustomers().isEmpty()) {
        	
            double[] nextInsertion = this.getNextInsertion(solution);
            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                // select the vehicle for which the insertion was calculated, then apply insertion to that vehicle
                solution.getVehicles().get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data, solution);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                solution.getNotAssignedCustomers().remove(Integer.valueOf((int) nextInsertion[0]));
            }
        }

        // update solution object, then return it
        solution.updateSolutionAfterInsertion();

        // NOTE Chris - call by reference, den return value könnte man sich wohl sparen
        return solution;
    }

    /**
     * Starts the insertion heuristic with the backtracking mechanism. 
     * @param solution: solution object the backtracking starts with
     * @return the solution after the backtracking 
     */
    public final Solution solveBacktrack (Solution solution) {
    	Solution sol = this.runBacktracking(solution);
    	return sol;
    }
    
    //
    // UPDATE METHODS
    //
    /**
     * Method increases the count how many times the insertion operation
     * has been drawn
     */
    public void incrementDraws() {
    	this.draws ++;
    }

    /**
     * Aggregates the attached value to the pi value of the insertion operation
     * @param add: number to be aggregated to pi
     */
    public void addToPi(double add) {
    	this.pi += add;
    }
    
    //
    // SETTERS
    //
    /**
     * Method to set the probability of the insertion operation
     * @param probability: Probability value to be set in the range [0;1]
     */
    public void setProbability(double probability) {
    	this.probability = probability;
    }
    
    /**
     * Method to set the pi value of the insertion operation
     * @param pi: value to be set
     */
    public void setPi(double pi) {
    	this.pi = pi;
    }
    
    /**
     * Method to set the weight of the insertion operation
     * @param weight: value to be set
     */
    public void setWeight(double weight) {
    	this.weight = weight;
    }
    
    /**
     * Method to set the number of draws of the insertion operation
     * @param draws: value to be set
     */
    public void setDraws(int draws) {
    	this.draws = draws;
    }
        
    //
    // GETTERS
    //
    /**
     * Retrieve the probability of the insertion operation
     * @return probability
     */
    public double getProbability() {
    	return this.probability;
    }
    
    /**
     * Retrieve the pi value of the insertion operation
     * @return pi value
     */
    public double getPi() {
    	return this.pi;
    }
    
    /**
     * Retrieve the weight value of the insertion operation
     * @return weight value
     */
    public double getWeight() {
    	return this.weight;
    }
    
    /**
     * Retrieve the number how many times the insertion operation has been drawn
     * @return draws
     */
    public int getDraws() {
    	return this.draws;
    }
}
