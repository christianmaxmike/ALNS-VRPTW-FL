package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.insertions.GreedyInsertion;
import vrptwfl.metaheuristic.alns.removals.AbstractRemoval;
import vrptwfl.metaheuristic.alns.removals.RandomRouteRemoval;
import vrptwfl.metaheuristic.alns.removals.FavVehicleRemoval;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

/**
 * This class implements the first stage of the optimization procedure as described by
 * Pisinger & Ropke.
 * In the first stage, the number of vehicles used is optimized. We apply an LNS procedure
 * where the destroy operation is defined by a random removal of any vehicle whose customers
 * are being tried to be scheduled in any of the remaining routes. 
 * The insertion operation is a greedy heuristic. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class LNSOptimization {
	
	// Data
	private Data data;

	// Repair/Destroy Operator
	private AbstractInsertion repairOperator;
	private AbstractRemoval destroyOperator;
	
	private boolean acceptedNewSolution;

	/**
	 * Constructor for the LNS optimization procedure. It initializes the insertion
	 * operation and destroy operation being used in the optimization scheme. 
	 * @param data: Data object
	 */
	public LNSOptimization(Data data) {
		this.data = data;
		
		this.repairOperator = new GreedyInsertion(data);
		this.destroyOperator = new FavVehicleRemoval(data);
	}
	
	/**
	 * The main procedure of the LNS optimization. 
	 * We attach and initial solution object being constructed by the construction phase.
	 * In the LNS optimization, we remove a random vehicle and try to schedule its customers
	 * in any route of the remaining routes. If the heuristic succeeds, we found a solution with
	 * a lower amount of vehicles being used. If not, the heuristic tries the insertion for any
	 * other vehicle. 
	 * All vehicles which are not used in the returned solution are also blocked for the ALNS
	 * procedure, the second phase. 
	 * 
	 * @param solutionConstr: initial solution
	 * @return best solution w.r.t the number of vehicles being used is returned
	 * @throws ArgumentOutOfBoundsException
	 */
	public Solution runLNS (Solution solutionConstr) throws ArgumentOutOfBoundsException {
		long startTime = System.currentTimeMillis();
		
		if (solutionConstr.isFeasible()) {
			for (Vehicle v: solutionConstr.getVehicles())
				if (!v.isUsed())
					v.setAvailable(false);			
		}
		
		Solution solutionBest = solutionConstr.copyDeep();
		Solution solutionTemp = solutionConstr.copyDeep();
		Solution solutionCurr = solutionConstr.copyDeep();
		
		for (int iteration = 1; iteration<= 25; iteration ++) {
			//TODO: Strafterm für not scheduled customer hoch setzen (Faktor für erhöhung/minimierung in config setzen)
			//TODO: check, ob man LNS auch in ALNS verwurschteln kann 
			//TODO: checkVehicleOpt() ist mit feasible check schon implizit abgedeckt
			//TODO: Aussetzen der updateTemperature funktion für simulated annealing
			//TODO: ! LNS phase arbeitet auch mit allen destroy/repair ops !
			//TODO: 10 % der Gesamtiterationen wird für LNS-Phase verwendet
			//TODO: Keine Adaption der Gewichte für die OPs in der LNS-Phase
			
			solutionTemp = solutionCurr.copyDeep();
			
			this.destroyOperator.destroy(solutionTemp);
			
			int removedVehicle = ((FavVehicleRemoval) this.destroyOperator).getSelectedIdx();
			solutionTemp.getVehicles().get(removedVehicle).setAvailable(false);
			
			this.repairOperator.solve(solutionTemp);
			
			solutionCurr = this.checkVehicleOpt(iteration, solutionTemp, solutionCurr, solutionBest);
			
			if (!this.acceptedNewSolution)
				solutionCurr.getVehicles().get(removedVehicle).setAvailable(true);
		}
		
		System.out.println("Time for LNS - vehicle optimization:" + (System.currentTimeMillis() - startTime));
		
		return solutionBest;
	}
	
	/**
	 * Check if a solution is found where the number of vehicles being used is minimized. 
	 * 
	 * @param iteration: the current iteration id
	 * @param solutionTemp: the temporary solution object
	 * @param solutionCurr: the current solution object
	 * @param solutionBest: the best solution object 
	 * @return the solution object with the lowest amount of vehicles being used.
	 */
	public Solution checkVehicleOpt(int iteration, Solution solutionTemp, Solution solutionCurr, Solution solutionBest) {
		if (solutionTemp.isFeasible()) {
			if (solutionTemp.getUsedVehicles().size() < solutionBest.getUsedVehicles().size()) {
				this.acceptedNewSolution = true;
				solutionBest.setSolution(solutionTemp);
				return solutionTemp;				
			}
		} 
		else {
			this.acceptedNewSolution = false;
		}
		return solutionCurr;
	}
}
