package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A request with a low score is situated in an unsuitable route according
 * to the request graph and should be removed.
 * Our initial experiments indicated that this was an unpromising approach, 
 * probably because it strongly counteracts the diversification mechanisms 
 * in the LNS heuristic.
 *
 * Instead, the graph is used to define the relatedness between two requests, 
 * such that two requests are considered to be related if the weight of the 
 * corresponding edge in the request graph is high.
 * (Ropke & Pisinger - EJOR 2006 - 760
 * 
 * @author Christian M.M. Frey
 *
 */
public class HistoricRequestNodeRemoval extends AbstractRemoval {
	
	private boolean randomize;
	private ALNSCore alns;

    public HistoricRequestNodeRemoval(Data data, ALNSCore alns, boolean randomize) {
        super(data);
        this.randomize = randomize;
        this.alns = alns;
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {
    	List<Integer> removedCustomers = new ArrayList<>();
    	while (nRemovals > 0) {
    		// get possible removals according to the request graph
    		ArrayList<double[]> possibleRemovals = solution.getPossibleRemovalsSortedByRequestGraph(alns.getRequestGraph(), solution);
    		
    		// randomize
    		int idx = 0;
    		if (this.randomize) {
    			double rand = Config.getInstance().randomGenerator.nextDouble();
    			idx = (int) Math.floor(Math.pow(rand, Config.getInstance().historicRequestRemovalExponent) * possibleRemovals.size());
    		}
    		
    		// apply removal
    		double[] removal = possibleRemovals.get(idx);
    		solution.getVehicles().get((int) removal[1]).applyRemoval((int) removal[2], this.data, solution);
    		removedCustomers.add((int) removal[0]);
    		
    		nRemovals --;
    	}
        return removedCustomers;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Historic Request (" + (this.randomize?"determ.":"random") + ")";
	}
}
