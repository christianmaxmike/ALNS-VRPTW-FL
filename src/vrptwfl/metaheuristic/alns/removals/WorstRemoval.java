package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the Worst Removal heuristic.
 * Remove customers which yield highest cost reductions when they are removed.
 * (cf. Ropke and Pisinger 2006, page 460 (Transportation Science))
 * (see Ropke & Pisinger 2006, p. 460 Algorithm 3 (An ALNS Heuristic for the PDPTW))
 * 
 * @author: Alexander Jungwirth
 */
public class WorstRemoval extends AbstractRemoval {

    private final boolean randomize;

    /**
     * Constructor for the worst removal heuristic. 
     * @param data: data object
     * @param randomize: use randomized version
     */
    public WorstRemoval(Data data, boolean randomize) {
        super(data);
        this.randomize = randomize;
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

//        Map<int[], Double> myCache; // TODO Alex - hier gehts mitm Guava Cache weiter

        // TODO Alex - kann man ausnutzen, dass bei jedem removal immer nur ein vehicle betroffen ist?
        //  eigentlich duerfte deswegen nur eine

        while (nRemovals > 0) {

            // TODO Alex - fuer jeden Kunden in Loesung: gib mir position und costReduction zurueck

            // calculate cost of removing customers from route
            ArrayList<double[]> possibleRemovals = solution.getPossibleRemovalsSortedByCostReduction();

            // TODO Alex - cache already calculated removals [pred, cust, succ]; wenn es das schon gibt, nicht neu rechnen

            int idx = 0;
            if (this.randomize) {
                double rand = Config.getInstance().randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.getInstance().worstRemovalExponent) * possibleRemovals.size());
            }
            // System.out.println(possibleRemovals.get(0)[3] +"\t"+ possibleRemovals.get(possibleRemovals.size()-1)[3]);
            double[] removal = possibleRemovals.get(idx);

            // apply removal {customer, vehicle.id, i, travelTimeReduction}
            // TODO Alex - ggf. eigene applyRemovalMethode, wo kosten nicht nochmal ausgerechnet werden muessen
            solution.getVehicles().get((int) removal[1]).applyRemoval((int) removal[2], this.data, solution);
            removedCustomers.add((int) removal[0]);

            nRemovals--;
        }
        return removedCustomers;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Worst (" + (this.randomize?"determ.":"random") + ")";
	}
}
