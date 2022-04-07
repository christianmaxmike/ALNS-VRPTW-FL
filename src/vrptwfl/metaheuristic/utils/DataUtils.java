package vrptwfl.metaheuristic.utils;

import java.util.ArrayList;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;

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

    
    public static int getLocationIndex (int customerId, Solution solution) {
    	return solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customerId]).get(solution.getCustomerAffiliationToLocations()[customerId]);
    }
    
}
