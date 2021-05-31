package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

public class RegretInsertion {

    private int k;
    private Data data;

    public RegretInsertion(int k, Data data) throws ArgumentOutOfBoundsException {

        // enforce k > 1. otherwise, no regret measure possible
        if (k <= 1) throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");

        this.k = k;
        this.data = data;
    }

    public double[] getNextInsertion(Solution solution) {
        // initialize values
        double maxRegret = -1;
        double[] nextInsertion = new double[5]; // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        nextInsertion[4] = -1; // positionInRoute is defined as the position at which the customer will be inserted

        ListIterator<Integer> iter = solution.getNotAssignedCustomers().listIterator();

        while(iter.hasNext()){

            // init info for customer
            int customer = iter.next();
            double regret = -1;

            // get all possible insertions for the customer
            ArrayList<double[]> possibleInsertionsForCustomer = this.getPossibleInsertionsForCustomer(solution, customer);

            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {

                solution.getTempInfeasibleCustomers().add(customer);
                iter.remove();
            } else {
                // get regret by sorting list and calculating difference between best and k-th best insertion
                regret = this.calculateRegret(this.k, possibleInsertionsForCustomer);

                // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                if (regret > maxRegret) {
                    maxRegret = regret;
                    nextInsertion = possibleInsertionsForCustomer.get(0);
                }
            }
        } // end while(iter.hasNext())
        return nextInsertion;
    }

    // TODO die zwei Methoden zu Insertion helpers auslagern
    // Method is public such that logic can be tested
    public double calculateRegret(int k, ArrayList<double[]> possibleInsertionsForCustomer) {
        double regret;
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

        if (possibleInsertionsForCustomer.size() >= k) {
            // if k-regret can be calculated as there enough at least k insertions
            regret = possibleInsertionsForCustomer.get(k - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
        } else {
            // if list has entries, but not k (i.e. not enough to calculate k-regret)
            int bigM = Config.bigMRegret;
            regret = (k-possibleInsertionsForCustomer.size())*bigM - possibleInsertionsForCustomer.get(0)[4];
        }
        return regret;
    }

    // method is public such that logic can be tested
    public ArrayList<double[]> getPossibleInsertionsForCustomer(Solution solution, int customer) {
        ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();

        boolean triedUnusedVehicle = false;
        for (Vehicle vehicle: solution.getVehicles()) {
            // generate insertion for unused vehicle only once, otherwise regrets between all unused vehicles will be zero
            if (!vehicle.isUsed()) {
                if (triedUnusedVehicle) continue;
                triedUnusedVehicle = true;
            }

            ArrayList<double[]> insertions = vehicle.getPossibleInsertions(customer, this.data);
            possibleInsertionsForCustomer.addAll(insertions);

        }
        return possibleInsertionsForCustomer;
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution solve(Solution solution) {

        while (!solution.getNotAssignedCustomers().isEmpty()) {
            double[] nextInsertion = this.getNextInsertion(solution);

            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                // select the vehicle for which the insertion was calculated, then apply insertion to that vehicle
                solution.getVehicles().get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                solution.getNotAssignedCustomers().remove(Integer.valueOf((int) nextInsertion[0]));
            }
        }

        // update solution object, then return it
        solution.updateSolutionAfterInsertion();

        return solution;
    }

}
