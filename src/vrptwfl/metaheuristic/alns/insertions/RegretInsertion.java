package vrptwfl.metaheuristic.alns.insertions;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

public class RegretInsertion extends AbstractInsertion {

    private int k;

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public RegretInsertion(int k, Data data) throws ArgumentOutOfBoundsException {

        super(data);

        // enforce k > 1. otherwise, no regret measure possible
        if (k <= 1) throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");

        this.k = k;
    }

    @Override
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
            ArrayList<double[]> possibleInsertionsForCustomer = solution.getPossibleInsertionsForCustomer(customer);

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




}
