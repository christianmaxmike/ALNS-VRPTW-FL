package vrptwfl.metaheuristic.utils;

import vrptwfl.metaheuristic.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper functions for calculation.
 * 
 * @author Alexander Jungwirth, Christian M.M. Frey
 */
public class CalcUtils {

	/**
	 * Calculates the gap between the optimal value and the computed one, i.e.,
	 * the one being observed in a solution
	 * @param valueOptimal optimal value (reference value)
	 * @param compValue compute value
	 * @return optimality gap
	 */
    public static double calculateGap(double valueOptimal, double compValue) {
        // return Math.abs(compValue - valueOptimal) / compValue;
        return (compValue - valueOptimal) / compValue;
    }

    /**
     * Retrieve a random number in a given range [lowerBound, upperBound]
     * @param lowerBound: lower bound
     * @param upperBound: upper bound
     * @return random number within the predefined range
     */
    public static int getRandomNumberInClosedRange(int lowerBound, int upperBound) {
        upperBound++; // otherwise [lowerBound, upperBound)
        return Config.randomGenerator.nextInt((upperBound - lowerBound)) + lowerBound;
    }

    /**
     * Retrieve sorted(!) unique random numbers in a predefined range
     * @param nNumbers: how many numbers are drawn
     * @param rangeLowerBound: lower bound
     * @param rangeUpperBound: upper bound
     * @return sorted random numbers within the predefined range
     */
    public static List<Integer> getSortedUniqueRandomNumbersInRange(int nNumbers, int rangeLowerBound, int rangeUpperBound) {
        // add all numbers in range to list (numbers will be sorted in ascending order)
        List<Integer> numbersInRange = new ArrayList<>() {{ for (int i = rangeLowerBound; i <= rangeUpperBound; i++) add(i); }};

        Collections.shuffle(numbersInRange, Config.randomGenerator); // shuffle order of numbers in list
        numbersInRange = numbersInRange.subList(0, nNumbers);  // get first n numbers of shuffled list

        Collections.sort(numbersInRange); // sort these first n elements
        return numbersInRange;
    }

    /**
     * Retrieve shuffled unique random numbers in a predefined range
     * @param nNumbers: how many numbers are drawn
     * @param rangeLowerBound: lower bound
     * @param rangeUpperBound: upper bound
     * @return shuffled random numbers iwthin the predefined range
     */
    public static List<Integer> getShuffledUniqueRandomNumbersInRange(int nNumbers, int rangeLowerBound, int rangeUpperBound) {
        // add all numbers in range to list (numbers will be sorted in ascending order)
        List<Integer> numbersInRange = new ArrayList<>() {{ for (int i = rangeLowerBound; i <= rangeUpperBound; i++) add(i); }};

        Collections.shuffle(numbersInRange, Config.randomGenerator); // shuffle order of numbers in list
        numbersInRange = numbersInRange.subList(0, nNumbers);  // get first n numbers of shuffled list

        return numbersInRange;
    }
}