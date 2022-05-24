package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

/**
 * This class implements the Start Time Flexibility Removal heuristic.
 * It removes customers first having small time windows as their range of 
 * possible starting time are rather small. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class TimeFlexibilityRemoval extends AbstractRemoval {
	
	private final boolean randomize;
	
	/**
	 * Constructor for the start time flexibility removal heuristic.
	 * @param data: data object
	 * @param randomize: use randomized version
	 */
	public TimeFlexibilityRemoval(Data data, boolean randomize) {
		super(data);
		this.randomize = randomize;
	}
	
	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
	@Override
	public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {
		List<Integer> removedCustomers = new ArrayList<Integer>();
		
		double[] timeWindowIntervals = new double[solution.getData().getEarliestStartTimes().length];
		for (int i = 1; i<timeWindowIntervals.length; i++) 
			timeWindowIntervals[i] = solution.getData().getLatestStartTimes()[i] - solution.getData().getEarliestStartTimes()[i];
		
		while (nRemovals > 0) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> copyList = (ArrayList<Integer>) solution.getAssignedCustomers().clone();
			copyList.sort(new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					if (timeWindowIntervals[o1] < timeWindowIntervals[o2]) return 1;
					else if (timeWindowIntervals[o1] > timeWindowIntervals[o2]) return -1;
					else return 0;
				}
			});
			
			int idx = 0;
			if (this.randomize) {
				double rand = Config.getInstance().randomGenerator.nextDouble();
				idx = (int) Math.floor(Math.pow(rand, Config.getInstance().timeFlexibilityRemovalExponent) * copyList.size());
			}
			
			int removeCustomer = copyList.get(idx);
			
			solution.getVehicles().get(solution.getCustomersAssignedToVehicles()[removeCustomer]).applyRemovalForCustomer(removeCustomer, this.data, solution);
			removedCustomers.add(removeCustomer);
			
			nRemovals--;
		}
		return removedCustomers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Time Flexibility (" + (this.randomize?"determ.":"random") + ")";
	}
}
