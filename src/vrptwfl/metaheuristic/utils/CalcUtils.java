package vrptwfl.metaheuristic.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalcUtils {

    public static double calculateGap(double valueOptimal, double compValue) {
        return Math.abs(compValue - valueOptimal) / compValue;
    }

    // returns random number in given range [lowerBound, upperBound]
    public static int getRandomNumberInClosedRange(int lowerBound, int upperBound) {
        upperBound++; // otherwise [lowerBound, upperBound)
        return (int) ((Math.random() * (upperBound - lowerBound)) + lowerBound);
    }

    public static List<Integer> getUniqueRandomNumbersInRange(int nNumbers, int rangeLowerBound, int rangeUpperBound) {
        // add all numbers in range to list (numbers will be sorted in ascending order)
        List<Integer> numbersInRange = new ArrayList<>() {{ for (int i = rangeLowerBound; i <= rangeUpperBound; i++) add(i); }};

        Collections.shuffle(numbersInRange); // shuffle order of numbers in list
        numbersInRange = numbersInRange.subList(0, nNumbers);  // get first n numbers of shuffled list

        Collections.sort(numbersInRange); // sort these first n elements
        return numbersInRange;
    }


}
