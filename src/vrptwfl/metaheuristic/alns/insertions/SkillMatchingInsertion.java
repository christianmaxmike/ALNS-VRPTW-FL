package vrptwfl.metaheuristic.alns.insertions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

public class SkillMatchingInsertion extends AbstractInsertion {

	public SkillMatchingInsertion(Data data) {
		super(data);
	}
	
	@Override
	public double[] getNextInsertion(Solution solution) {
		double[] nextInsertion = new double[8];
        nextInsertion[4] = Config.bigMRegret;
        
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
        
		ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();
        
		// initialize values
        double minCostIncrease = Config.bigMRegret;
		
		int inspectedSkillLvl = -1;
        while (iter.hasNext()) {
        	int customer = iter.next();
        	if (inspectedSkillLvl == -1)
        		inspectedSkillLvl = solution.getData().getRequiredSkillLvl()[customer];
        	else
        		if (solution.getData().getRequiredSkillLvl()[customer] != inspectedSkillLvl)
        			break;

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

	@Override
	public Solution runBacktracking(Solution initSolution) {
		return initSolution;
	}
}
