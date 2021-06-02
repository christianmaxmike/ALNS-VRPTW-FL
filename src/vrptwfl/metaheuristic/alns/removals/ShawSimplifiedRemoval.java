package vrptwfl.metaheuristic.alns.removals;

import com.google.common.collect.Ordering;
import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.*;

// removes customers which are "related".  Here related is defined solely by distance.
// Ropke and Pisinger 2006, page 759 (EJOR)
public class ShawSimplifiedRemoval extends AbstractRemoval {

    private boolean randomize;

    public ShawSimplifiedRemoval(Data data, boolean randomize) {
        super(data);
        this.randomize = randomize;
    }

    @Override
    // TODO hier nochmal drüber gehen und vereinfachen und kommentieren
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

        // choose the first customer to be removed at random
        int firstCustomer = -1;
        int posFirstRemoval = CalcUtils.getRandomNumberInClosedRange(0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;

            if (posFirstRemoval >= vehicle.getnCustomersInTour()) {
                posFirstRemoval -= vehicle.getnCustomersInTour();
            } else {
                firstCustomer = vehicle.applyRemoval(posFirstRemoval + 1, this.data);  // +1 as dummy out is at index 0
                removedCustomers.add(firstCustomer); //vehicle.getCustomers().get(posFirstRemoval));
                nRemovals--;
                break;
            }
        }

        // get customers closest to the first one
        // get row form travel time matrix
        double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomer];
        ArrayList<double[]> closest = new ArrayList<>();

        // add all customers in vehicles
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;
            for (int customer: vehicle.getCustomers()) {
//            for (int c = 1; c < vehicle.getnCustomersInTour()+1; c++) {
                if (customer == 0) continue;
                closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customer]});
            }
        }
        closest.sort(Comparator.comparing(v->v[2]));  // sort according to distance (smallest distance first)

        while (nRemovals > 0) {
            nRemovals--;
            int idx = 0;
            if (this.randomize) {
                double rand = Config.randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.shawRemovalExponent) * closest.size());
            }
            double[] removal = closest.get(idx);
            removedCustomers.add((int) removal[0]);
            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data);
            closest.remove(idx);

            if (nRemovals == 0) break;
        }

        // TODO wenn es locations gibt, geht die Logik so nicht komplett

        return removedCustomers;

    }

    public static void main(String[] args) {

        List<Integer> listInt = new ArrayList<>();
        listInt.add(1);
        listInt.add(2);
        listInt.add(3);
        listInt.add(4);

        System.out.println(listInt); // 1,2,3,4

        List<Double> listDouble = new ArrayList<>();
        listDouble.add(1.2);
        listDouble.add(-3.4);
        listDouble.add(-0.1);
        listDouble.add(20.1);

        listInt.sort(Comparator.comparing(v->listDouble.get(listInt.indexOf(v))));
//        listInt.sort(Ordering.explicit(listDouble).onResultOf(List<Double>::indexOf));
        System.out.println(listInt); // 3, 1, 4, 2

//        for (int i=0; i < 4; i++) {
//            System.out.println(listDouble.indexOf(i));
//        }

        List<double[]> newDouble = new ArrayList<>();
        newDouble.add(new double[] {1., 1.2});
        newDouble.add(new double[] {2., -3.4});
        newDouble.add(new double[] {3., -.1});
        newDouble.add(new double[] {4., 20.1});

        newDouble.sort(Comparator.comparing(v->v[1]));
        for (double[] d: newDouble) {
            System.out.println("[" + d[0] + ", " + d[1] + "]");
        }

    }

}