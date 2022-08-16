package vrptwfl.metaheuristic.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import vrptwfl.metaheuristic.common.Solution;

/**
 * General helper functions for data transformations (e.g., list to array function)
 *
 * @author Christian M.M. Frey, Alexander Jungwirth
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
    
    /**
     * Sort the attached double array by its arguments.
     * @param a: array being sorted
     * @param ascending: indicator whether the sorting is in ascending order
     * @return array containing the sorted arguments/indices
     */
    public static int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        return asArray(indexes);
    }
    
    /**
     * Helper function transforming the attached values in an array
     * @param <T>: types of the values being stored
     * @param a: values being stored in an array
     * @return
     */
    private static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
    
    /**
     * Enumeration being used for penalty indices.
     * 
     * @author Christian M.M. Frey
     *
     */
    public enum PenaltyIdx {
    	
    	// enum constants calling the enum constructors 
    	Unscheduled(0),
    	TWViolation(1), 
    	Predecessor(2),
    	Capacity(3),
    	SkillLvl(4);
    	
        private final int id;

        /**
         * Constructor for a penalty index being called by the enum constants
         * @param id: index being used for the enum constant
         */
        private PenaltyIdx(int id) {
            this.id = id;
        }

        /**
         * Retrieve the index of the penalty.
         * @return penalty's identifier
         */
        public int getId() {
            return id;
        }
    }
}