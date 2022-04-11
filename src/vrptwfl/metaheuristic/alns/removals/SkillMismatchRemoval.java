package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

/**
 * This class implements the Skill Matching removal heuristic.
 * It removes customers having the highest dismatch with the required skill leve
 * of a customer and the skill level provided by the therapist.
 *
 * @author: Christian M.M. Frey
 */
public class SkillMismatchRemoval extends AbstractRemoval{
	
    private final boolean randomize;

    /**
     * Constructor for the skill mismatch removal heuristic.
     * @param data: data object
     * @param randomize: use randomized version
     */
	public SkillMismatchRemoval(Data data, boolean randomize) {
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
		
		while (nRemovals > 0) {
			@SuppressWarnings("unchecked")
			ArrayList<Integer> copyList = (ArrayList<Integer>) solution.getAssignedCustomers().clone();
	        copyList.sort(new Comparator<Integer>() {

				@Override
				public int compare(Integer customer1, Integer customer2) {
					int reqLvl1 = solution.getData().getRequiredSkillLvl()[customer1];
					int reqLvl2 = solution.getData().getRequiredSkillLvl()[customer2];
					if (getSkillDiscrepancy(reqLvl1) > getSkillDiscrepancy(reqLvl2)) return -1;
					else if (getSkillDiscrepancy(reqLvl1) < getSkillDiscrepancy(reqLvl2)) return +1;
					else return 0;
				}
				
				private int getSkillDiscrepancy(int requiredSkill) {
					int skilldisc = 0;
					for (Vehicle v: solution.getVehicles())
						skilldisc += (v.getSkillLvl() - requiredSkill) >= 0 ? (v.getSkillLvl() - requiredSkill) : -1;
					return skilldisc;
				}
	        });

			int idx = 0;
			
            if (this.randomize) {
                double rand = Config.randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.skillMismatchRemovalExponent) * copyList.size());
            }
			
			int removeCustomer = copyList.get(idx);
			
			solution.getVehicles().get(solution.getCustomersAssignedToVehicles()[removeCustomer]).applyRemovalForCustomer(removeCustomer, this.data, solution);
			removedCustomers.add(removeCustomer);
			
			nRemovals--;
		}
		return removedCustomers;
	}
}