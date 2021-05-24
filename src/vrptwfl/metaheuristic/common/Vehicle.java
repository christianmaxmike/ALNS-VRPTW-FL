package vrptwfl.metaheuristic.common;

import java.util.ArrayList;

public class Vehicle {

    private int id;
    private int capacityLimit;
    private int capacityUsed;
    private ArrayList<Integer> customers;
    private ArrayList<Double> startOfServices;
    private ArrayList<Double> endOfServices;
    private boolean isUsed;

    public Vehicle(int id, int capacityLimit, double latestEndOfService) {
        this.id = id;
        this.capacityLimit = capacityLimit;
        this.capacityUsed = 0;

        // create empty route only dummy node for start and end of tour
        this.customers = new ArrayList<>();
        this.customers.add(0);
        this.customers.add(0);
        this.startOfServices = new ArrayList<>();
        this.startOfServices.add(0.0);
        this.startOfServices.add(0.0);
        this.endOfServices = new ArrayList<>();
        this.endOfServices.add(latestEndOfService);
        this.endOfServices.add(latestEndOfService);

        this.isUsed = false;
    }


}
