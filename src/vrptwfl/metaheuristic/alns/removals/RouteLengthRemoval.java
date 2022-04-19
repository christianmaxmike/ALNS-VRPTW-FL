package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

/**
 * This class implements the route removal heuristic taking the routes' lengths
 * into account. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class RouteLengthRemoval extends AbstractRemoval {

	private boolean shortestRoutesFirst;
	
	/**
	 * Constructor for the route's length removal heuristic.
	 * The attached data object is forwarded to the parent class 
	 * AbstractRemoval.
	 * @param data: data object
	 * @param shortedRoutesFirst: indicator whether short routes are removed first
	 */
	public RouteLengthRemoval(Data data, boolean shortestRoutesFirst) {
		super(data);
		this.shortestRoutesFirst = shortestRoutesFirst;
	}

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
		List<Integer> removedCustomers = new ArrayList<Integer>();
		@SuppressWarnings("unchecked")
		ArrayList<Vehicle> copyVehicles = (ArrayList<Vehicle>) solution.getVehicles().clone();
		copyVehicles.sort(new Comparator<Vehicle>() {
			
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				if (v1.getCustomers().size() < v2.getCustomers().size()) return -1;
				else if (v1.getCustomers().size() > v2.getCustomers().size()) return 1;
				else return 0;
			}
			
		});
		
		if (!this.shortestRoutesFirst) 
			Collections.reverse(copyVehicles);
		
		while (nRemovals > 0) {
			for (int rv_idx = 0; rv_idx <copyVehicles.size(); rv_idx ++) {
				Vehicle rv = copyVehicles.get(rv_idx);
				for (int customerId = 1; customerId<rv.getCustomers().size()-1; customerId++) {
					int rCustomer = rv.getCustomers().get(customerId);
					solution.getVehicles().get(rv.getId()).applyRemovalForCustomer(rCustomer, this.data, solution);
					removedCustomers.add(rCustomer);
					
					nRemovals--;
					if (nRemovals == 0) break;
				}
				
			}
		}
		return removedCustomers;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Route elim. (" + (this.shortestRoutesFirst?"least nodes":"most nodes") + ")";
	}

}
