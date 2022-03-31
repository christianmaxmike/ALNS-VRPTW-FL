package vrptwfl.metaheuristic.utils;

import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

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
    
    
    public static int getLocationIndex (int customerId, Solution solution) {
    	return solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customerId]).get(solution.getCustomerAffiliationToLocations()[customerId]);
    }
    
//  
//  public static int getMatchingIndexInBooleanArray (boolean[] arr, boolean value) {
//  	for (int idx = 0 ; idx<arr.length; idx++)
//  		if (arr[idx] == value) return idx;
//  	return -1;
//  }
    
    public static int[] convertDoubleArrToIntArr (double[] inputArray) {
    	int[] intArray = new int[inputArray.length];
    	for (int i=0; i<intArray.length; ++i)
    		intArray[i] = (int) inputArray[i];
    	return intArray;
    }
    
}
