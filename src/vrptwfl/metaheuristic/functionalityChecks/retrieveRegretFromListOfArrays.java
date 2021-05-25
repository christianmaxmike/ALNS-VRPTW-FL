package vrptwfl.metaheuristic.functionalityChecks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class retrieveRegretFromListOfArrays {

    /*
     * Get Diff between best and k-best element in list of arrays
     */
    public static void main(String[] args) {

        int k = 2;

        int[] array1 = {2,3,5};
        int[] array2 = {1,4,3};
        int[] array3 = {-1,-3,-5};
        int[] array4 = {2,3,3};
        int[] array5 = {-2,3,-10};

        ArrayList<int[]> myList = new ArrayList<>();
        myList.add(array1);
        myList.add(array2);
        myList.add(array3);
        myList.add(array4);
        myList.add(array5);

        System.out.println("\nBefore sorting");
        for (int[] arr: myList) {
            System.out.println(Arrays.toString(arr));
        }

        // SORTING

        myList.sort(Comparator.comparing(a -> a[2]));
        System.out.println("After sorting");
        for (int[] arr: myList) {
            System.out.println(Arrays.toString(arr));
        }

        // CALCULATE REGRET
        int regret = myList.get(k - 1)[2] - myList.get(0)[2];
        System.out.println("Regret (" + k + "): " + regret);

    }
}
