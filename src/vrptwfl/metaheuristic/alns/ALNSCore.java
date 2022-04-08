package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.insertions.GreedyInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertionBacktracking;
import vrptwfl.metaheuristic.alns.insertions.SkillMatchingInsertion;
import vrptwfl.metaheuristic.alns.removals.*;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Practical heart of the adaptive large neighborhood search applied 
 * on the vehicle routing problem with time windows and flexible locations.
 * This class initializes and stores the repair and destroy operators as
 * well as the visited solutions.
 */
public class ALNSCore {
	
	private FileWriter writer;

    private Data data;

    private AbstractInsertion[] repairOperators;
    private AbstractRemoval[] destroyOperators;
    
    private int currentDestroyOpIdx;
    private int currentRepairOpIdx;
    private int currentSigma; 
    private double temperature;
    private double temperatureEnd;
    
    private HashMap<Integer, Solution> visitedSolutions;
    
    //if useNeighborGraphRemoval, then this graph contains information about the best solution in which the edge (i,j) was used
    private double[][] neighborGraph;

    /**
     * Constructor for the ALNSCore class. 
     * It initializes the repair and destroy operators as defined in the configuration file.
     * @param data: Data object
     * @throws ArgumentOutOfBoundsException
     */
    public ALNSCore(Data data) throws ArgumentOutOfBoundsException {
        this.data = data;
        this.initRepairOperators();
        this.initDestroyOperators();    
        this.visitedSolutions = new HashMap<Integer, Solution>();
		try {
			this.writer = new FileWriter("./probability.txt", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    //
    // INITIALIZATION METHODS
    //
    /**
     * Initializes the destroy operators as defined in the configuration file.
     * @throws ArgumentOutOfBoundsException
     */
    private void initDestroyOperators() throws ArgumentOutOfBoundsException {
        List<AbstractRemoval> destroyList = new ArrayList<>();

        if (Config.useClusterRemovalKruskal) destroyList.add(new ClusterKruskalRemoval(data));

        if (Config.useHistoricNodePairRemovalDeterministic) destroyList.add(new HistoricNodePairRemoval(data, this,false));
        if (Config.useHistoricNodePairRemovalRandom) destroyList.add(new HistoricNodePairRemoval(data, this, true));
        if (Config.useHistoricNodePairRemovalDeterministic || Config.useHistoricNodePairRemovalRandom) this.initNeighborGraph();

        if (Config.useRandomRemoval) destroyList.add(new RandomRemoval(data));
        if (Config.useRandomRouteRemoval) destroyList.add(new RandomRouteRemoval(data));
        if (Config.useHistoricRequestPairRemoval) destroyList.add(new HistoricRequestNodeRemoval(data));
        if (Config.useShawSimplifiedRemovalDeterministic) destroyList.add(new ShawSimplifiedRemoval(data, false));
        if (Config.useShawSimplifiedRemovalRandom) destroyList.add(new ShawSimplifiedRemoval(data, true));
        if (Config.useTimeOrientedRemovalJungwirthDeterministic) destroyList.add(new TimeOrientedRemoval(data, false, Config.timeOrientedJungwirthWeightStartTimeIinSolution));
        if (Config.useTimeOrientedRemovalJungwirthRandom) destroyList.add(new TimeOrientedRemoval(data, true, Config.timeOrientedJungwirthWeightStartTimeIinSolution));
        if (Config.useTimeOrientedRemovalPisingerDeterministic) destroyList.add(new TimeOrientedRemoval(data, false, 1.0));
        if (Config.useTimeOrientedRemovalPisingerRandom) destroyList.add(new TimeOrientedRemoval(data, true, 1.0));
        if (Config.useWorstRemovalDeterministic) destroyList.add(new WorstRemoval(data, false));
        if (Config.useWorstRemovalRandom) destroyList.add(new WorstRemoval(data, true));
        if (Config.useSkillMismatchRemovalDeterministic) destroyList.add(new SkillMismatchRemoval(data, false));
        if (Config.useSkillMismatchRemovalRandom) destroyList.add(new SkillMismatchRemoval(data, true));

        this.destroyOperators = new AbstractRemoval[destroyList.size()];
        this.destroyOperators = destroyList.toArray(this.destroyOperators);
        
    	// initialize values w/ their default values
        for (AbstractRemoval entry : this.destroyOperators) {
        	entry.setPi(0.0);
        	entry.setWeight(1.0);
        	entry.setProbability(1.0/this.destroyOperators.length);
        	entry.setDraws(0);
        }
    }

    /**
     * Initializes the repair operators as defined in the configuration file.
     * @throws ArgumentOutOfBoundsException
     */
    private void initRepairOperators() throws ArgumentOutOfBoundsException {
        List<AbstractInsertion> repairList = new ArrayList<>();

        if (Config.useGreedyInsert) repairList.add(new GreedyInsertion(data));
        if (Config.useSkillMatchingInsert) repairList.add(new SkillMatchingInsertion(data));
        if (Config.useNRegret2) repairList.add(new RegretInsertion(2, data));
        if (Config.useNRegret3) repairList.add(new RegretInsertion(3, data));
        if (Config.useNRegret4) repairList.add(new RegretInsertion(4, data));
        if (Config.useNRegret5) repairList.add(new RegretInsertion(5, data));
        if (Config.useNRegret6) repairList.add(new RegretInsertion(6, data));
        
        if (Config.enableBacktracking) repairList.add(new RegretInsertionBacktracking(2, data));

        this.repairOperators = new AbstractInsertion[repairList.size()];
        this.repairOperators = repairList.toArray(this.repairOperators);
        
    	// Initialize values w/ their default values
        for (AbstractInsertion entry : this.repairOperators) {
        	entry.setPi(0.0);
        	entry.setWeight(1.0);
        	entry.setProbability(1.0/this.repairOperators.length);
        	entry.setDraws(0);
        }
    }
    
    /**
     * Initializes the neighbor graphs.
     * Its default entries are initialized with the value of bigMRegret defined in the configuration file.
     */
    private void initNeighborGraph() {
        // complete, directed, weighted graph
        this.neighborGraph = new double[this.data.getnCustomers() + 1][this.data.getnCustomers() + 1];
        // edges are initially set to infinity (or a reasonably high value)
        Arrays.stream(this.neighborGraph).forEach(row -> Arrays.fill(row, Config.bigMRegret));
    }
    
    /**
     * In this method the temperature parameter used in simulated annealing is calculated
     * according to the cost of the initial solution.
     * @param costInitialSolution: costs of the initial solution
     */
    private void initTemperature(double costInitialSolution) {
    	this.temperature = -(Config.startTempControlParam / Math.log(Config.bigOmega)) * costInitialSolution;
    	this.temperatureEnd = Config.minTempPercent * this.temperature;
    }
    
    //
    // MAIN FUNCTIONALITY
    //
    /**
     * This method starts the adpative large neighborhood search. 
     * The initial solution on which the ALNS starts is attached as parameter.
     * @param solutionConstr: initial solution
     * @return best found solution
     * @throws ArgumentOutOfBoundsException
     */
    public Solution runALNS(Solution solutionConstr) throws ArgumentOutOfBoundsException {

    	// initialize temperature for simulated annealing - added 03/03/22
    	initTemperature(solutionConstr.getTotalCosts());
    	
        // init ALNS
        Solution solutionCurrent = solutionConstr.copyDeep();
        Solution solutionBestGlobal = solutionConstr.copyDeep();

        // add information from construction to neighbor graph
        if (Config.useHistoricNodePairRemovalRandom || Config.useHistoricNodePairRemovalDeterministic) 
        	this.updateNeighborGraph(solutionConstr);


        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
            Solution solutionTemp = solutionCurrent.copyDeep();
            // TODO Alex: random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            //AbstractRemoval destroyOp = getDestroyOperatorAtRandom();
            AbstractRemoval destroyOp = drawDestroyOperator();
            destroyOp.destroy(solutionTemp);

            // repair solution
            // returns one repair operator specified in repairOperators
            //AbstractInsertion repairOp = getRepairOperatorAtRandom();
            AbstractInsertion repairOp = drawInsertionOperator();
            repairOp.solve(solutionTemp);

            
            // update neighbor graph if new solution was found (TODO Alex - check if the solution is really a new one (hashtable?)
            if (Config.useHistoricNodePairRemovalRandom || Config.useHistoricNodePairRemovalDeterministic) this.updateNeighborGraph(solutionTemp);

            if (iteration % 1000 == 0) {
                System.out.println("\n\nIteration " + iteration);
                System.out.println("Cost temp " + solutionTemp.getTotalCosts());
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }

            solutionCurrent = this.checkImprovement(solutionTemp, solutionCurrent, solutionBestGlobal);
            
            
            if (iteration % 1000 == 0) {
                System.out.println();
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }
            
            // END OF ITERATION
            // Call update operations after each iter.
            this.updateWeightofOperators(iteration);
            this.updateTemperature();
        }
        
        return solutionBestGlobal;
    }

    // TODO Alex: Testcase um zu checken, ob auch die richtigen werte upgdated werden
    private void updateNeighborGraph(Solution solution) {
        double obj = solution.getTotalCosts();

        for (Vehicle vehicle: solution.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<Integer> customers = vehicle.getCustomers();

                int pred = customers.get(0);
                int succ = -1;
                for (int c = 1; c < customers.size(); c++) {
                    succ = customers.get(c);

                    if (this.neighborGraph[pred][succ] > obj + Config.epsilon) 
                    	this.neighborGraph[pred][succ] = obj;

                    pred = succ;
                }
            }
        }
    }
    
    /**
     * This method is used to check for improvements of the temporary solution.
     * There are four cases:
     * 1) Temporary solution is the new best global solution;
     * 2) Temporary solution has not been checked before and is better than the current solution;
     * 3) Temporary solution has not been checked before and is not better than the current solution but
     *    is picked due to simulated annealing allowing for worse solutions;
     * 4) Temporary solution has been checked before; no changes;
     * @param solutionTemp: Temporary solution
     * @param solutionCurrent: Current solution
     * @param solutionBestGlobal: Global best solution
     * @return Solution object according to the four cases
     */
    private Solution checkImprovement(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {
        // CASE 1 : check if improvement of global best
        if (solutionTemp.isFeasible()) {
            if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
            	this.currentSigma = Config.sigma1;
                solutionBestGlobal.setSolution(solutionTemp);
                return solutionTemp;
            }
        }

        // CASE 2&3: solution has not been visited before
        if (!visitedSolutions.containsKey(solutionTemp.hashCode_tmp()) ) {
            // check if temporary solution become new current solution
        	// CASE 2: temp objective fnc better than current solution 
            if (this.tempSolutionIsAcceptedByCosts(solutionTemp, solutionCurrent)) {
            	this.currentSigma = Config.sigma2;
                return solutionTemp;
            }
            
            // CASE 3: simulated annealing - temp solution no improvement but still accepted 
        	double val = Math.exp(-(solutionTemp.getTotalCosts()-solutionCurrent.getTotalCosts()) / this.temperature);
    		if(Math.random() < val){
                this.currentSigma = Config.sigma3;
                return solutionTemp;
        	}
            
    		// add solution to visited solutions
    		visitedSolutions.put(solutionTemp.hashCode_tmp(), solutionTemp);
    		// tmp solution has shown no improvement and is not accepted; remains the same
        	this.currentSigma = -1;
            return solutionCurrent;        	
        }
        else {
        	// solution has been visited in previous iterations
        	// current solution and scores remain unchanged;
        	this.currentSigma = -1;
        	return solutionCurrent;
        }

    }
    
    /**
     * Check whether the temporary solution is accepted compare to the current solution according 
     * to their specific costs.
     * @param solutionTemp: temporary solution
     * @param solutionCurrent: current solution
     * @return boolean indicating whether the temporary solution's costs are better than 
     *                 the costs of the current solution
     */
    private boolean tempSolutionIsAcceptedByCosts(Solution solutionTemp, Solution solutionCurrent) {    	
        return solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon;
    }

    
    //
    // SAMPLING OF DESTROY/REPAIR OPERATORS
    //
    /**
     * Sample uniformly an insertion operator.
     * @return Insertion operator
     */
    private AbstractInsertion getRepairOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.repairOperators.length);
        this.currentRepairOpIdx = idx;
        return this.repairOperators[idx];
    }

    /**
     * Sample uniformly a destroy operator.
     * @return Destroy operator
     */
    private AbstractRemoval getDestroyOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.destroyOperators.length);
        this.currentDestroyOpIdx = idx;
        return this.destroyOperators[this.currentDestroyOpIdx];
    }
    
    /**
     * This method is used to draw a destroy operator according to the attached probabilities.
     * @return drawn destroy operator
     */
    private AbstractRemoval drawDestroyOperator() {
    	double randomValue = Config.randomGenerator.nextDouble();
    	double cumulatedSum = 0.0;
    	for (int idx = 0; idx<this.destroyOperators.length; idx++) {
    		cumulatedSum += this.destroyOperators[idx].getProbability();
    		if (randomValue <= cumulatedSum) {
    			this.currentDestroyOpIdx = idx;
    			return this.destroyOperators[idx];
    		}
    	}
    	return null;
    }
    
    /**
     * This method is used to draw an insertion operator according to the attached probabilities.
     * @return drawn repair operator
     */
    private AbstractInsertion drawInsertionOperator() {
    	double randomValue = Config.randomGenerator.nextDouble();
    	double cumulatedSum = 0.0;
    	for (int idx = 0; idx < this.repairOperators.length; idx++) {
    		cumulatedSum += this.repairOperators[idx].getProbability();
    		if (randomValue <= cumulatedSum) {
    			this.currentRepairOpIdx = idx;
    			return this.repairOperators[idx];
    		}
    	}
    	return null;
    }
    
 
    //
    // UPDATE METHODS
    //
    /**
     * This method updates the weights of the operators. Weights are updated only after a certain
     * number of iterations.
     * @param currentIteration: the current iteration number
     */
    private void updateWeightofOperators(int currentIteration) {
    	if (this.currentSigma < 0) {
    		return;
    	}    	    	
    	// TODO Chris - Wahrscheinlichkeiten mittracken und mal plotten
    	{
        	this.destroyOperators[this.currentDestroyOpIdx].incrementDraws();
        	this.destroyOperators[this.currentDestroyOpIdx].addToPI(this.currentSigma);
        	if (currentIteration % Config.updateInterval == 0) {
        		double portionOldWeight = this.destroyOperators[this.currentDestroyOpIdx].getWeight() * (1 - Config.reactionFactor);
        		double updatedWeight = this.destroyOperators[this.currentDestroyOpIdx].getPi() / 
        				(double) this.destroyOperators[this.currentDestroyOpIdx].getDraws();
        		updatedWeight *= Config.reactionFactor;
        		this.destroyOperators[this.currentDestroyOpIdx].setWeight(portionOldWeight + updatedWeight);        		
            	this.updateProbabilitiesDestroyOps(this.getSumWeightsDestroyOps());    	
        	}
    	}
    			
    	// update insertion Operator
    	{
        	this.repairOperators[this.currentRepairOpIdx].incrementDraws();
        	this.destroyOperators[this.currentDestroyOpIdx].addToPI(this.currentSigma);
        	if (currentIteration % Config.updateInterval == 0) {
        		double portionOldWeight = this.repairOperators[this.currentRepairOpIdx].getWeight() * (1 - Config.reactionFactor);
        		double updatedWeight = this.repairOperators[this.currentRepairOpIdx].getPi() / 
        				(double) this.repairOperators[this.currentRepairOpIdx].getDraws();
        		updatedWeight *= Config.reactionFactor;
        		this.repairOperators[this.currentRepairOpIdx].setWeight(portionOldWeight + updatedWeight);        		
            	this.updateProbabilitiesRepairOps(this.getSumWeightsRepairOps());
        	}
    	}    	
    }
        
    /**
     * This method updates the probabilities of the repair operators.
     * @param sumWeights: sum of weights
     */
    private void updateProbabilitiesRepairOps(double sumWeights) {
    	for (AbstractInsertion entry : this.repairOperators) {
    		double newProb = entry.getWeight() / sumWeights;
    		entry.setProbability(newProb > Config.minOpProb ? newProb : Config.minOpProb);
    	}
    }
    
    /**
     * This method updated the probabilities of the destroy operators.
     * @param sumWeights: sum of weights
     */
    private void updateProbabilitiesDestroyOps(double sumWeights) {
    	for (AbstractRemoval entry : this.destroyOperators) {
    		double newProb = entry.getWeight() / sumWeights;
    		entry.setProbability(newProb > Config.minOpProb ? newProb : Config.minOpProb);
    	}
    }
    
    /**
     * This method updates the temperature parameter according to the cooling rate.
     * @see vrptwfl.metaheuristic.Config
     */
    private void updateTemperature() {
    	if (this.temperature > this.temperatureEnd) {
    		this.temperature *= Config.coolingRate;
    	}
    }
    
    
    //
    // CUSTOM GETTERS
    //
    /**
     * This method returns the total sum of weights of the insertion/repair operators.
     * @return sum of weights
     */
    private double getSumWeightsRepairOps() {
    	double sum = 0.0;
    	for (AbstractInsertion entry : this.repairOperators) {
    		sum += entry.getWeight();
    	}
    	return sum;
    }
    
    /**
     * This method returns the total sum of weights of the destroy operators.
     * @return sum of weights
     */
    private double getSumWeightsDestroyOps() {
    	double sum = 0.0;
    	for (AbstractRemoval entry : this.destroyOperators) {
    		sum += entry.getWeight();
    	}
    	return sum;
    }
    
    
    //
    // GETTERS
    //
    /**
     * Retrieve the neighbor graphs.
     * It comprises all customers in an n x n matrix. 
     * Entries denote the best value found so far for scheduling the customers.
     * @return 2-dim array denoting the neighbor graphs
     */
    public double[][] getNeighborGraph() {
        return neighborGraph;
    }

    
    //
    // DEPRECATED FUNCTIONS - START
    //
    // TODO Alex - hier brauchen wir auch noch Test cases
    private Solution checkImprovement_orig(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {
        if (solutionTemp.isFeasible()) {
            if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                solutionBestGlobal.setSolution(solutionTemp);
            }
        }

        // check if temporary solution become new current solution
        if (this.tempSolutionIsAcceptedByCosts(solutionTemp, solutionCurrent)) {
            return solutionTemp;
        }
        return solutionCurrent;        	


//        // check if temporary solution become new current solution
//        if (solutionCurrent.isFeasible()) {
//            if (solutionTemp.isFeasible()) {
//                // improvement
//                if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
//                    // check if also better than best global
////                    if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
////                        solutionBestGlobal.setSolution(solutionTemp);
////                    }
//                    return solutionTemp;
//                }
//            }
//        } else { // if no feasible solution found yet
//            // improvement
//            if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
//                return solutionTemp;
//            }
//        }
//
//        // TODO Alex - feasible nur relevant fuer beste globale loesung
//        // was current solution feasible ?
//        // TODO Alex - hier kommt dann wahrscheinlichkeit etc rein, dass trotzdem schlechtere loesung
//        //  angenommen wird
//
//        // no improvement
//        return solutionCurrent;
    }
}
