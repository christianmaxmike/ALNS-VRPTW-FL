package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

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

    // final such that method cannot be accidentally overridden in subclass
    public final Solution solve(Solution solution) {

        while (!solution.getNotAssignedCustomers().isEmpty()) {
            double[] nextInsertion = this.getNextInsertion(solution);

            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                // select the vehicle for which the insertion was calculated, then apply insertion to that vehicle
                solution.getVehicles().get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                solution.getNotAssignedCustomers().remove(Integer.valueOf((int) nextInsertion[0]));
            }
        }

        // update solution object, then return it
        solution.updateSolutionAfterInsertion();

        return solution;
    }

    
    abstract double[] getNextInsertion(Solution solution);
    
    /*
     * UPDATE METHODS
     */
    public void incrementDraws() {
    	this.draws ++;
    }

    public void addToPi(double add) {
    	this.pi += add;
    }
    
    /*
     * SETTERS
     */
    public void setProbability(double probability) {
    	this.probability = probability;
    }
    
    public void setPi(double pi) {
    	this.pi = pi;
    }
    
    public void setWeight(double weight) {
    	this.weight = weight;
    }
    
    public void setDraws(int draws) {
    	this.draws = draws;
    }
        
    /*
     * GETTERS
     */
    public double getProbability() {
    	return this.probability;
    }
    
    public double getPi() {
    	return this.pi;
    }
    
    public double getWeight() {
    	return this.weight;
    }
    
    public int getDraws() {
    	return this.draws;
    }


}
