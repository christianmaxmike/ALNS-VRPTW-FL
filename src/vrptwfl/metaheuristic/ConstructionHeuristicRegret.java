package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

import java.util.*;

public class ConstructionHeuristicRegret {

    private Data data;

    public ConstructionHeuristicRegret(Data data) {
        this.data = data;
    }

    // k defines what regret measure to use
    //  e.g. k=3 means difference between best insertion and 3rd best insertion
    public Solution solve(int k) {

        ArrayList<Integer> notAssignedCustomers = new ArrayList<>() {{ for (int i : data.getCustomers()) add(i); }};
        ArrayList<Integer> infeasibleCustomers = new ArrayList<>(); // needed to store customer that cannot be assigned to any route
        ArrayList<Vehicle> vehicles = data.initializeVehicles();

        // FIRST STEP OF k-regret --> TODO in eigene Methode

        while (!notAssignedCustomers.isEmpty()) {
            // initialize values
            double maxRegret = -1;
            double[] nextInsertion = new double[5]; // [customerId, vehicleId, positionInRoute, startTime, additionalCosts]
            // positionInRoute is defined as the position at which the customer will be inserted
            nextInsertion[4] = -1;

            ListIterator<Integer> iter = notAssignedCustomers.listIterator();

            while(iter.hasNext()){

                // init info for customer
                int customer = iter.next();
                double regret = -1;

                // get all possible insertions for the customer
                ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();
                for (Vehicle vehicle: vehicles) {
                    ArrayList<double[]> insertions = vehicle.getPossibleInsertions(customer, this.data);
                    possibleInsertionsForCustomer.addAll(insertions);
                }

                // if list is empty, no feasible assignment to any route exists for that customer
                if (possibleInsertionsForCustomer.isEmpty()) {
                    infeasibleCustomers.add(customer);
                    iter.remove();
                } else {
                    // get regret by sorting list and calculating difference between best and k-th best insertion
                    possibleInsertionsForCustomer.sort(Comparator.comparing(a -> a[4])); // sort by additional costs

                    if (possibleInsertionsForCustomer.size() >= k) {
                        // if k-regret can be calculated as there enough at least k insertions
                        regret = possibleInsertionsForCustomer.get(k - 1)[4] - possibleInsertionsForCustomer.get(0)[4];
                    } else {
                        // if list has entries, but not k (i.e. not enough to calculate k-regret)
                        int bigM = 100_000;
                        regret = (k-possibleInsertionsForCustomer.size())*bigM - possibleInsertionsForCustomer.get(0)[4];
                    }

                    // if regret is higher than currently highest regret, update maxRegret and update nextInsertion
                    if (regret > maxRegret) {
                        maxRegret = regret;
                        nextInsertion = possibleInsertionsForCustomer.get(0);
                    }
                }
            } // end while(iter.hasNext())


            // check if at least one insertion has been found (-1 was initial dummy value and should be replaced by something >= 0)
            if (nextInsertion[4] > -1) {
                vehicles.get((int) nextInsertion[1]).applyInsertion(nextInsertion, this.data);

                // remove element from list of notAssignedCustomers
                // Integer.valueOf(xy) is needed as otherwise value at position xy will be removed not xy itself
                notAssignedCustomers.remove(Integer.valueOf((int) nextInsertion[0]));

            }

            System.out.println(); // TODO wieder raus
            for (Vehicle veh: vehicles) {
                if (veh.isUsed()) {
                    veh.printTour();
                }
            }
            // TODO wieder raus
            System.out.println(notAssignedCustomers);
            System.out.println(infeasibleCustomers);
            // TODO Loesungsobjekt erzeugen
        }


        return null;
    }

}
