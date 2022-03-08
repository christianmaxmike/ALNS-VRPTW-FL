package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.insertions.GreedyInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.removals.*;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ALNSCore {

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

    public double[][] getNeighborGraph() {
        return neighborGraph;
    }

    public ALNSCore(Data data) throws ArgumentOutOfBoundsException {
        this.data = data;

        this.initRepairOperators();
        this.initDestroyOperators();    
        
        this.visitedSolutions = new HashMap<Integer, Solution>();
    }

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

        this.destroyOperators = new AbstractRemoval[destroyList.size()];
        this.destroyOperators = destroyList.toArray(this.destroyOperators);
        
        for (AbstractRemoval entry : this.destroyOperators) {
        	// initialize values w/ their default values
        	entry.setPi(0.0);
        	entry.setWeight(1.0);
        	entry.setProbability(1.0/this.destroyOperators.length);
        	entry.setDraws(0);
        }
    }

    private void initNeighborGraph() {
        // complete, directed, weighted graph
        this.neighborGraph = new double[this.data.getnCustomers() + 1][this.data.getnCustomers() + 1];

        // edges are initially set to infinity (or a reasonably high value)
        Arrays.stream(this.neighborGraph).forEach(row -> Arrays.fill(row, Config.bigMRegret));
    }


    private void initRepairOperators() throws ArgumentOutOfBoundsException {
        List<AbstractInsertion> repairList = new ArrayList<>();

        if (Config.useGreedyInsert) repairList.add(new GreedyInsertion(data));
        if (Config.useNRegret2) repairList.add(new RegretInsertion(2 ,data));
        if (Config.useNRegret3) repairList.add(new RegretInsertion(3 ,data));
        if (Config.useNRegret4) repairList.add(new RegretInsertion(4 ,data));
        if (Config.useNRegret5) repairList.add(new RegretInsertion(5 ,data));
        if (Config.useNRegret6) repairList.add(new RegretInsertion(6 ,data));

        this.repairOperators = new AbstractInsertion[repairList.size()];
        this.repairOperators = repairList.toArray(this.repairOperators);
        
        for (AbstractInsertion entry : this.repairOperators) {
        	entry.setPi(0.0);
        	entry.setWeight(1.0);
        	entry.setProbability(1.0/this.repairOperators.length);
        	entry.setDraws(0);
        }
    }

    public Solution runALNS(Solution solutionConstr) throws ArgumentOutOfBoundsException {

    	// initialize temperature for simulated annealing - added 03/03/22
    	initTemperature(solutionConstr.getTotalCosts());
    	
        // init ALNS
        Solution solutionCurrent = solutionConstr.copyDeep();
        Solution solutionBestGlobal = solutionConstr.copyDeep();

        // add information from construction to neighbor graph
        if (Config.useHistoricNodePairRemovalRandom || Config.useHistoricNodePairRemovalDeterministic) this.updateNeighborGraph(solutionConstr);


        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
//        for (int iteration = 1; iteration <= 10_000; iteration++) {

            Solution solutionTemp = solutionCurrent.copyDeep();

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            //AbstractRemoval destroyOp = getDestroyOperatorAtRandom();
            AbstractRemoval destroyOp = drawDestroyOperator();
            destroyOp.destroy(solutionTemp);

            // repair solution
            // returns one repair operator specified in repairOperators
            //AbstractInsertion repairOp = getRepairOperatorAtRandom();
            AbstractInsertion repairOp = drawInsertionOperator();
            repairOp.solve(solutionTemp);
            
            // update neighbor graph if new solution was found (TODO check if the solution is really a new one (hashtable?)
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

            //  if (iteration % 5000 == 0) {
            //      DebugUtils.printNumericMatrix(this.neighborGraph);
            //  }
            
            
            // double deltaCosts = solutionBestGlobal.getTotalCosts() - solutionTemp.getTotalCosts();
            
            // END OF ITERATION
            // Call update operations after each iter.
            this.updateWeightofOperators();
            this.updateTemperature();
        }

        return solutionBestGlobal;
    }

    // TODO Testcase um zu checken, ob auch die richtigen werte upgdated werden
    private void updateNeighborGraph(Solution solution) {

        double obj = solution.getTotalCosts();

        for (Vehicle vehicle: solution.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<Integer> customers = vehicle.getCustomers();

                int pred = customers.get(0);
                int succ = -1;
                for (int c = 1; c < customers.size(); c++) {
                    succ = customers.get(c);

                    if (this.neighborGraph[pred][succ] > obj + Config.epsilon) this.neighborGraph[pred][succ] = obj;

                    pred = succ;
                }
            }
        }

    }
    
    

    // TODO hier brauchen wir auch noch Test cases
    private Solution checkImprovement(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {

    	// TODO Chris: Sigmas nochmal mit Alex checken
    	
        // CASE 1 : check if improvement of global best
        if (solutionTemp.isFeasible()) {
            if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
            	this.currentSigma = Config.sigma1;
                solutionBestGlobal.setSolution(solutionTemp);
            }
        }

        // CASE 2&3: solution has not been visited before
        if (!visitedSolutions.containsKey(solutionTemp.hashCode_tmp()) ) {
            // check if temporary solution become new current solution
        	// CASE 2: temp objective fnc better than current solution 
            if (this.tempSolutionIsAccepted(solutionTemp, solutionCurrent)) {
            	this.currentSigma = Config.sigma2;
                return solutionTemp;
            }
            
            // CASE 3: simulated annealing - temp solution no improvement but still accepted 
        	double val = Math.exp(-(solutionTemp.getTotalCosts()-solutionCurrent.getTotalCosts()) / this.temperature);
    		if(Math.random() < val){
                this.currentSigma = Config.sigma3;
                return solutionTemp;
    			// solutionCurrent = solutionTemp;
        	}
            
    		// add solution to visited solutions
    		visitedSolutions.put(solutionTemp.hashCode_tmp(), solutionTemp);
    		// tmp solution has shown no improvement and is not accepted; remains the same
            return solutionCurrent;        	
        }
        else {
        	// solution has been visited in previous iterations
        	// current solution and scores remain unchanged;
        	this.currentSigma = -1;
        	return solutionCurrent;
        }

    }
    
    // TODO hier brauchen wir auch noch Test cases
    private Solution checkImprovement_orig(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {

    	// TODO Chris: Sigmas nochmal mit Alex checken
    	
        if (solutionTemp.isFeasible()) {
            if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                solutionBestGlobal.setSolution(solutionTemp);
            }
        }

        // check if temporary solution become new current solution
        if (this.tempSolutionIsAccepted(solutionTemp, solutionCurrent)) {
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
//        // TODO: feasible nur relevant fuer beste globale loesung
//        // was current solution feasible ?
//        // TODO hier kommt dann wahrscheinlichkeit etc rein, dass trotzdem schlechtere loesung
//        //  angenommen wird
//
//        // no improvement
//        return solutionCurrent;
    }


    private boolean tempSolutionIsAccepted(Solution solutionTemp, Solution solutionCurrent) {    	
    	// improvement ?
        return solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon;
    }

    private AbstractInsertion getRepairOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.repairOperators.length);
        this.currentRepairOpIdx = idx;
        return this.repairOperators[idx];
    }

    private AbstractRemoval getDestroyOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.destroyOperators.length);
        this.currentDestroyOpIdx = idx;
        return this.destroyOperators[this.currentDestroyOpIdx];
    }
    
    
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
    
    private void updateWeightofOperators() {
    	if (this.currentSigma < 0) {
    		return;
    	}
    	//TODO Chris:
    	// Fragen an Alex: 
    	// - Im Paper wird nur von sigma Werte gesprochen; was war/ist der Sinn 
    	//   hinter getAdjustedSigma bzw. WertungsFaktor (Sigma faktor) im alten Code
    	// - Warum wird im alten code für repaird ops kein Anteil des alten Gewichts berechnet
    	
    	// update destroy Op
    	{
        	this.destroyOperators[this.currentDestroyOpIdx].incrementDraws();
        	this.destroyOperators[this.currentDestroyOpIdx].addToPI(this.currentSigma);
        	double portionOldWeight = this.destroyOperators[this.currentDestroyOpIdx].getWeight() * (1 - Config.reactionFactor);
        	double updatedWeight = this.destroyOperators[this.currentDestroyOpIdx].getPi() / 
        			(double) this.destroyOperators[this.currentDestroyOpIdx].getDraws();
        	updatedWeight *= Config.reactionFactor;
        	this.destroyOperators[this.currentDestroyOpIdx].setWeight(portionOldWeight + updatedWeight);    		
    	}
    			
    	// update insertion Op
    	{
        	this.repairOperators[this.currentRepairOpIdx].incrementDraws();
        	this.destroyOperators[this.currentDestroyOpIdx].addToPI(this.currentSigma);
        	// TODO Chris: Warum wird im alten code für repaird ops kein Anteil des alten Gewichts berechnet
        	double portionOldWeight = this.repairOperators[this.currentRepairOpIdx].getWeight() * (1 - Config.reactionFactor);
        	double updatedWeight = this.repairOperators[this.currentRepairOpIdx].getPi() / 
        			(double) this.repairOperators[this.currentRepairOpIdx].getDraws();
        	updatedWeight *= Config.reactionFactor;
        	this.repairOperators[this.currentRepairOpIdx].setWeight(portionOldWeight + updatedWeight);     		
    	}
    	
    	this.updateProbabilitiesRepairOps(this.getSumWeightsRepairOps());
    	this.updateProbabilitiesDestroyOps(this.getSumWeightsDestroyOps());    	
    }
    
    private double getSumWeightsRepairOps() {
    	double sum = 0.0;
    	for (AbstractInsertion entry : this.repairOperators) {
    		sum += entry.getWeight();
    	}
    	return sum;
    }
    
    private double getSumWeightsDestroyOps() {
    	double sum = 0.0;
    	for (AbstractRemoval entry : this.destroyOperators) {
    		sum += entry.getWeight();
    	}
    	return sum;
    }
    
    private void updateProbabilitiesRepairOps(double sumWeights) {
    	for (AbstractInsertion entry : this.repairOperators) {
    		entry.setProbability(entry.getWeight() / sumWeights);
    	}
    }
    
    private void updateProbabilitiesDestroyOps(double sumWeights) {
    	for (AbstractRemoval entry : this.destroyOperators) {
    		entry.setProbability(entry.getWeight() / sumWeights);
    	}
    }
    
    
    private void initTemperature(double costInitialSolution) {
    	this.temperature = -(Config.startTempControlParam / Math.log(Config.bigOmega)) * costInitialSolution;
    	this.temperatureEnd = Config.minTempPercent * this.temperature;
    }
    
    private void updateTemperature() {
    	if (this.temperature > this.temperatureEnd) {
    		this.temperature *= Config.coolingRate;
    	}
    }

}
