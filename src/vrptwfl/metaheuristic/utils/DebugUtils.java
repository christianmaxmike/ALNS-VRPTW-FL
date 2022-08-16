package vrptwfl.metaheuristic.utils;

import java.util.Arrays;

/**
 * Helper functions for debugging.
 * 
 * @author Alexander Jungwirth
 */
public class DebugUtils {

	/**
	 * print the attached matrix on the console.
	 * @param matrix: two dimensional double array being printed out on the console
	 */
    public static  void printNumericMatrix(double[][] matrix) {
        for (double[] array : matrix) {
            System.out.println(Arrays.toString(array));
        }
    }
}
