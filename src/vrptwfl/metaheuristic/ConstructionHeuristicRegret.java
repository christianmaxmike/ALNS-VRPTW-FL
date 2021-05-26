package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.*;

public class ConstructionHeuristicRegret {

    private Data data;
    private final ArrayList<Integer> notAssignedCustomers;
    private final ArrayList<Integer> infeasibleCustomers;
    private final ArrayList<Vehicle> vehicles;

    public ArrayList<Integer> getNotAssignedCustomers() {
        return notAssignedCustomers;
    }

    public ArrayList<Integer> getInfeasibleCustomers() {
        return infeasibleCustomers;
    }

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
        // initally add all customers to list of not assigned customers
        notAssignedCustomers = new ArrayList<>() {{ for (int i : data.getCustomers()) add(i); }};
        // needed to store customer that cannot be assigned to any route
        infeasibleCustomers = new ArrayList<>();
        vehicles = data.initializeVehicles();
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution solve(int k) throws ArgumentOutOfBoundsException {

        // enforce k > 1. otherwise, no regret measure possible
        if (k <= 1) throw new ArgumentOutOfBoundsException("regret parameter k must be greater than one. Value passed was " + k + ".");

        while (!notAssignedCustomers.isEmpty()) {
            double[] nextInsertion = getNextInsertion(k);

            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                vehicles.get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                notAssignedCustomers.remove(Integer.valueOf((int) nextInsertion[0]));
            }
        }

        // create solution object, then return it
        return new Solution(vehicles);
    }

    private double[] getNextInsertion(int k) {
        // initialize values
        double maxRegret = -1;
        double[] nextInsertion = new double[5]; // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
        nextInsertion[4] = -1; // positionInRoute is defined as the position at which the customer will be inserted

        ListIterator<Integer> iter = notAssignedCustomers.listIterator();

        while(iter.hasNext()){

            // init info for customer
            int customer = iter.next();
            double regret = -1;

            // get all possible insertions for the customer
            ArrayList<double[]> possibleInsertionsForCustomer = this.getPossibleInsertionsForCustomer(customer);

            // if list is empty, no feasible assignment to any route exists for that customer
            if (possibleInsertionsForCustomer.isEmpty()) {
                infeasibleCustomers.add(customer);
                iter.remove();
            } else {
                // get regret by sorting list and calculating difference between best and k-th best insertion
                regret = this.calculateRegret(k, possibleInsertionsForCustomer);

                // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                if (regret > maxRegret) {
                    maxRegret = regret;
                    nextInsertion = possibleInsertionsForCustomer.get(0);
                }
            }
        } // end while(iter.hasNext())
        return nextInsertion;
    }

    private double calculateRegret(int k, ArrayList<double[]> possibleInsertionsForCustomer) {
        double regret;
        possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

        if (possibleInsertionsForCustomer.size() >= k) {
            // if k-regret can be calculated as there enough at least k insertions
            regret = possibleInsertionsForCustomer.get(k - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
        } else {
            // if list has entries, but not k (i.e. not enough to calculate k-regret)
            int bigM = 100_000; // TODO bigM (for regret) in config file mit Kommentar dass das groesser sein muss als das maximale Regret
            regret = (k-possibleInsertionsForCustomer.size())*bigM - possibleInsertionsForCustomer.get(0)[4];
        }
        return regret;
    }

    private ArrayList<double[]> getPossibleInsertionsForCustomer(int customer) {
        ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();
        for (Vehicle vehicle: vehicles) {
            ArrayList<double[]> insertions = vehicle.getPossibleInsertions(customer, this.data);
            possibleInsertionsForCustomer.addAll(insertions);

            // generate insertion for unused vehicle only once, otherwise regrets between all unused vehicles will be zero
            if (!vehicle.isUsed()) break;
        }
        return possibleInsertionsForCustomer;
    }

}
