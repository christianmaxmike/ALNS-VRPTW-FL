package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// removes customers which are "related" in terms of their start times.
// Pisinger & Ropke 2007, page 2414 (C&OR)
public class TimeOrientedRemoval extends AbstractRemoval {


    private final boolean randomize;

    private double weightStartTimeInSolution; // [%] in Pisinger & Ropke = 1; in Jungwirth can be between 0 and 1

    public TimeOrientedRemoval(Data data, boolean randomize, double weightStartTimeInSolution) throws ArgumentOutOfBoundsException {
        super(data);
        this.randomize = randomize;
        if (weightStartTimeInSolution < - Config.epsilon || weightStartTimeInSolution > 1 + Config.epsilon) throw new ArgumentOutOfBoundsException("Weight parameter (alpha_1) for time-oriented destroy must be in interval [0,1]. Given was: " + weightStartTimeInSolution + ".");
        this.weightStartTimeInSolution = weightStartTimeInSolution; // alpha_2 - Wert im draft
    }

    @Override
    List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {

        if (nRemovals > Config.timeOrientedNrOfClosest) throw new ArgumentOutOfBoundsException("nRemovals (q=" + nRemovals + ") must be less than or equal to timeOrientedNrOfClosest (B=" + Config.timeOrientedNrOfClosest + ").");

        List<Integer> removedCustomers = new ArrayList<>();

        // 1) --- choose the first customer to be removed at random ---
        int firstCustomer = -1; // set dummy value to see later if customer could be removed
        int posFirstRemoval = CalcUtils.getRandomNumberInClosedRange(0, this.data.getnCustomers() - solution.getNotAssignedCustomers().size() - 1);
        double startTimeFirstCustomer = -1.;

        // TODO koennen wir das auslagern in eigene Methode? Ist identisch mit ShawSimplified
        // go through all vehicles and count the customers until the count corresponds to the position to remove
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;

            if (posFirstRemoval >= vehicle.getnCustomersInTour()) {
                posFirstRemoval -= vehicle.getnCustomersInTour();
            } else {
                startTimeFirstCustomer = vehicle.getStartOfServices().get(posFirstRemoval + 1);  // +1 as dummy out is at index 0
                firstCustomer = vehicle.applyRemoval(posFirstRemoval + 1, this.data);  // +1 as dummy out is at index 0
                removedCustomers.add(firstCustomer); //vehicle.getCustomers().get(posFirstRemoval));
                nRemovals--;
                break;
            }
        }

        // if no customer could be removed (no one was in tour), then return empty list
        if (firstCustomer == -1) return removedCustomers;

        // 2) --- get customers closest to the reference customer ---
        double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[firstCustomer];
        List<double[]> closest = new ArrayList<>();

        // add all customers already assigned to the vehicle
        for (Vehicle vehicle: solution.getVehicles()) {
            if (!vehicle.isUsed()) continue;
            int positionCounter = 0;
            for (int customer: vehicle.getCustomers()) {
                positionCounter++;
                if (customer == 0) continue;
                double timeDiff = Math.abs(startTimeFirstCustomer - vehicle.getStartOfServices().get(positionCounter));
                double avgStartTime = data.getAverageStartTimes(firstCustomer, customer);
                double timeRelatedness = this.weightStartTimeInSolution * timeDiff + (1 - this.weightStartTimeInSolution) * avgStartTime;
                closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customer], timeRelatedness});
            }
        }
        // sort according to distance (smallest distance first)
        closest.sort(Comparator.comparing(v->v[2]));
        // remove customers which are not close
        closest = closest.subList(0,  Math.min(closest.size(), Config.timeOrientedNrOfClosest));

        // 3) --- sort according to time difference (smallest difference first)
        closest.sort(Comparator.comparing(v->v[3]));

        // 4) --- remove customers which are related in time
        while (nRemovals > 0) {

            int idx = 0;
            if (this.randomize) {
                double rand = Config.randomGenerator.nextDouble();
                idx = (int) Math.floor(Math.pow(rand, Config.timeOrientedRemovalExponent) * closest.size());
            }
            double[] removal = closest.get(idx);

            removedCustomers.add((int) removal[0]);
            solution.getVehicles().get((int) removal[1]).applyRemovalForCustomer((int) removal[0], this.data);

            // remove customer from list of closest customers
            closest.remove(idx);

            nRemovals--;
        }
        return removedCustomers;
    }
}
