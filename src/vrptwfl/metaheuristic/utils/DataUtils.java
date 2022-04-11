package vrptwfl.metaheuristic.utils;

import java.util.ArrayList;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;

/**
 * General helper functions for data transformations (e.g., list to array function)
 *
 * @author Alexander Jungwirth, Christian M.M. Frey
 */
public class DataUtils {

	/**
	 * Converts a list of integer values into an array
	 * @param listResult: List of integers
	 * @return array of integers
	 */
    public static int[] convertListToArray(List<Integer> listResult) {
        int[] result = new int[listResult.size()];
        int i = 0;
        for (int num : listResult) {
            result[i++] = num;
        }
        return result;
    }
    
    /**
     * Converts a list of double values into an array
     * @param list: list of double values
     * @return array of doubles
     */
    public static double[] convertDoubleListToArr(ArrayList<Double> list) {
    	double[] result = new double[list.size()];
    	for (int i = 0; i<list.size(); i++)
    		result[i] = list.get(i);
    	return result;
    }
    
    /**
     * Converts a double array into an integer array
     * @param inputArray: double array
     * @return integer array
     */
    public static int[] convertDoubleArrToIntArr (double[] inputArray) {
    	int[] intArray = new int[inputArray.length];
    	for (int i=0; i<intArray.length; ++i)
    		intArray[i] = (int) inputArray[i];
    	return intArray;
    }

    /**
     * Retrieve the location index a customer is currently assigned to.
     * The possible locations are stored within the data object whereas 
     * the current assignment to one of its possible location is dependent
     * on the current solution object.
     * @param customerId: customer id
     * @param solution: solution object
     * @return location index 
     */
    public static int getLocationIndex (int customerId, Solution solution) {
    	return solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customerId]).get(solution.getCustomerAffiliationToLocations()[customerId]);
    }
    
    /**
     * Retrieve the location indices of all customers.
     * The possible locations are stored within the data object whereas 
     * the current assignment to one of its possible location is dependent
     * on the current solution object.
     * @param solution: solution object
     * @return array with all location indices for all customers being scheduled
     */
    public static int[] getLocationIdxOfAllCustomers(Solution solution) {
    	int[] locs = new int[solution.getData().getCustomers().length+1]; //<- Depot + 1
    	for (int customerID = 1; customerID<locs.length; customerID++) {
    		if (solution.getCustomerAffiliationToLocations()[customerID] != -1)
    			locs[customerID] = getLocationIndex(customerID, solution);
    		else 
    			locs[customerID] = -1;
    	}
    	return locs;
    }
}