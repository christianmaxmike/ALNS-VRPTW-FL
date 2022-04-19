package vrptwfl.metaheuristic.alns.insertions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

/**
 * This class implements an insertion heuristic according to the skill matches. 
 * The heuristic calculates the skill discrepancy of a customer to the available 
 * vehicles. If the skill discrepancy is lower, i.e., there are not many vehicles
 * that can serve the customer at hand, the customer is favored over other 
 * customers. 
 * 
 * @author Christian M.M. Frey
 */
public class SkillMatchingInsertion extends AbstractInsertion {

	/**
	 * Constructor of the skill matching heuristic. The attached data
	 * object is forwarded to its parent class AbstractInsertion.
	 * @param data: data object
	 */
	public SkillMatchingInsertion(Data data) {
		super(data);
	}
	
    /**
     * Retrieve the next possible insertions following the skill matching insertion heuristic.
     * The method iterates all unscheduled customers, identifies the next possible insertions for them, and
     * yields the next insertion according to the best skill matches.
     * 
     * @param solution solution object storing information about the scheduled and unscheduled customers
     */
	@Override
	public double[] getNextInsertion(Solution solution) {
		double[] nextInsertion = new double[8];
        nextInsertion[4] = -1;
        
        // Sort unscheduled customers according to the number of possible insertions
        // w.r.t the skill leves of the therapists/vehicles. 
        solution.getNotAssignedCustomers().sort(new Comparator<Integer>() {

			@Override
			public int compare(Integer customer1, Integer customer2) {
				int reqLvl1 = solution.getData().getRequiredSkillLvl()[customer1];
				int reqLvl2 = solution.getData().getRequiredSkillLvl()[customer2];
				if (getSkillDiscrepancy(reqLvl1) < getSkillDiscrepancy(reqLvl2)) return -1;
				else if (getSkillDiscrepancy(reqLvl1) > getSkillDiscrepancy(reqLvl2)) return +1;
				else return 0;
			}
			
			private int getSkillDiscrepancy(int requiredSkill) {
				int skilldisc = 0;
				for (Vehicle v: solution.getVehicles())
					skilldisc += (v.getSkillLvl() - requiredSkill) >= 0 ? (v.getSkillLvl() - requiredSkill) : -1;
				return skilldisc;
			}
        	
        });
        
        // Get iterator for unscheduled customers
		ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();
        
		// initialize values
        double minCostIncrease = Config.bigMRegret;
		
        // the current inspected Skill lvl; if customers have the same skill discrepancy
        // the best insertion is searched among all of them
		int inspectedSkillLvl = -1;
        while (iter.hasNext()) {
        	int customer = iter.next();
        	
        	// check if a new skill lvl is inspected
        	if (inspectedSkillLvl == -1)
        		inspectedSkillLvl = solution.getData().getRequiredSkillLvl()[customer];
        	else
        		if (solution.getData().getRequiredSkillLvl()[customer] != inspectedSkillLvl)
        			break;

        	// Get possible insertions
        	ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);
        	
        	if (possibleInsertionsForCustomer.isEmpty()) {
	        	if (solution.checkSchedulingOfPredecessors(customer)) {
	        		solution.getTempInfeasibleCustomers().add(customer);
	        		iter.remove();
	        	}
        	} else {
        		possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs
                double[] possibleInsertion = possibleInsertionsForCustomer.get(0);
                
             // compare cost increase to currently best (lowest) cost increase
                if (possibleInsertion[4] + Config.epsilon < minCostIncrease) {
                	minCostIncrease = possibleInsertion[4];  // update new min cost
                    nextInsertion = possibleInsertion;
                }
        	}
        }
		return nextInsertion;
	}

    /**
     * Runs the insertion heuristic with the backtracking logic.
     * TODO Chris : currently not implemented - ask Alex why Backtracking only w/ k-regret
     */
	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Skill Matching";
	}
}
