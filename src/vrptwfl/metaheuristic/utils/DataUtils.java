package vrptwfl.metaheuristic.utils;

import java.util.List;

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
}
