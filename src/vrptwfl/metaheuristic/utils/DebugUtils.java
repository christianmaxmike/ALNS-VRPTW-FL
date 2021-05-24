package vrptwfl.metaheuristic.utils;

import java.util.Arrays;

public class DebugUtils {

    public static  void printNumericMatrix(double[][] matrix) {
        for (double[] array : matrix) {
            System.out.println(Arrays.toString(array));
        }
    }
}
