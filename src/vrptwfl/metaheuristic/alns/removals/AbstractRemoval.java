package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.List;

/**
 * This class implements an abstract removal operation
 * @author: Alexander Jungwirth, Christian M.M. Frey
 */
public abstract class AbstractRemoval {

    protected Data data;
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
    public AbstractRemoval(Data data) {
        this.data = data;
    }

    
    //
    // ABSTRACT METHODS
    //
    /**
     * Abstract method which when implemented defines the individual destroy operation
     * @param solution: Solution object to be updated
     * @param nRemovals: number of removals to be executed
     * @return List of removed customers
     * @throws ArgumentOutOfBoundsException 
     */
    abstract List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException;


    //
    // FUNCTIONALITY
    //
    /**
     * Get the number of removals which are applied. The number of removals is chosen 
     * in the range defined by Config.lowerBoundRevomals and Config.upperBoundRemovals
     * @param solution: Solution object
     * @return number of removals
     */
    final int getNRemovals(Solution solution) {
        int nRemovals = CalcUtils.getRandomNumberInClosedRange(Config.lowerBoundRemovals, Config.upperBoundRemovals);
        int nrOfAssignedCustomers = solution.getNrOfAssignedCustomers();
        if (nRemovals > nrOfAssignedCustomers) nRemovals = nrOfAssignedCustomers;
        return nRemovals;
    }

    /**
     * The function handles the general working flow of the destroy procedure. 
     * It first identifies how man removals will be applied. Subsequently, the 
     * operation defined in each destroy subclass is executed. After that, the
     * solution object is updated, correspondingly.
     * @param solution: Solution object to be updated
     * @throws ArgumentOutOfBoundsException: is thrown if the specific destroy
     *              operation couldn't be executed
     */
    public final void destroy(Solution solution) throws ArgumentOutOfBoundsException {
        // get number of removals based on parameters defined in config file
        int nRemovals = getNRemovals(solution);

        List<Integer> removedCustomers = this.operatorSpecificDestroy(solution, nRemovals);

        // Update the solution object.  The tours of the vehicle are already update by the removals.  However, global
        // information such as the total costs and list of notAssignedCustomers still need to be updated.
        solution.updateSolutionAfterRemoval(removedCustomers);
    }

    
    //
    // UPDATE METHODS
    //
    /**
     * Method increases the count how many times the destroy operation
     * has been drawn
     */
    public void incrementDraws() {
    	this.draws ++;
    }
    
    /**
     * Aggregates the attached value to the pi value of the destroy operation
     * @param add: number to be aggregated to pi
     */
    public void addToPI (double add) {
    	this.pi += add;
    }
    
    
    //
    // SETTERS
    //
    /**
     * Method to set the probability of the destroy operation
     * @param probability: Probability value to be set in the range [0;1]
     */
    public void setProbability(double probability) {
    	this.probability = probability;
    }
    
    /**
     * Method to set the pi value of the destroy operation
     * @param pi: value to be set
     */
    public void setPi(double pi) {
    	this.pi = pi;
    }
    
    /**
     * Method to set the weight of the destroy operation
     * @param weight: value to be set
     */
    public void setWeight(double weight) {
    	this.weight = weight;
    }
    
    /**
     * Method to set the number of draws of the destroy operation
     * @param draws: value to be set
     */
    public void setDraws(int draws) {
    	this.draws = draws;
    }
       
    
    //
    // GETTERS
    //
    /**
     * Retrieve the probability of the destroy operation
     * @return probability
     */
    public double getProbability() {
    	return this.probability;
    }
    
    /**
     * Retrieve the pi value of the destroy operation
     * @return pi value
     */
    public double getPi() {
    	return this.pi;
    }
    
    /**
     * Retrieve the weight value of the destroy operation
     * @return weight value
     */
    public double getWeight() {
    	return this.weight;
    }
    
    /**
     * Retrieve the number how many times the destroy operation has been drawn
     * @return draws
     */
    public double getDraws() {
    	return this.draws;
    }
}