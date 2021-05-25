package vrptwfl.metaheuristic.common;

import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;

public class Vehicle {

    private int id;
    private int capacityLimit;
    private int capacityUsed;
    private double tourLength;
    private ArrayList<Integer> customers;
    private ArrayList<Double> startOfServices;
    private ArrayList<Double> endOfServices;
    private boolean isUsed;

    public int getId() {
        return id;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public Vehicle(int id, int capacityLimit, double latestEndOfService) {
        this.id = id;
        this.capacityLimit = capacityLimit;
        this.capacityUsed = 0;
        this.tourLength = 0.;

        // create empty route only dummy node for start and end of tour
        this.customers = new ArrayList<>();
        this.customers.add(0);
        this.customers.add(0);
        this.startOfServices = new ArrayList<>();
        this.startOfServices.add(0.0);
        this.startOfServices.add(latestEndOfService);
        this.endOfServices = new ArrayList<>();
        this.endOfServices.add(0.0);
        this.endOfServices.add(latestEndOfService);

        // TODO add Test, dass dummy jobs korrekt drin sind, (wirklich auch bei 0 vorbei sind und bei was auch immer enden)

        this.isUsed = false;
    }


    public ArrayList<double[]> getPossibleInsertions(int customer, Data data) {

        // TODO epsilon in config file
        double epsilon = 0.0001;

        ArrayList<double[]> possibleInsertions = new ArrayList<>();

        // if capacity limit would be reached, the customer cannot be inserted
        // TODO add Test, dass wir hier rausfliegen, wenn Capa zu hoch
        if (this.capacityUsed + data.getDemands()[customer] > this.capacityLimit) return possibleInsertions;

        double earliestStartCustomer = data.getEarliestStartTimes()[customer];
        double latestStartCustomer = data.getLatestStartTimes()[customer];

        // TODO fuer die Methode brauchen wir auf jeden Fall ein paar Testcases
        // iterate over all customers in tour
        for (int i = 0; i < this.customers.size() - 1; i++ ) {
            int pred = this.customers.get(i);
            int succ = this.customers.get(i+1);

            double distToCustomer = data.getDistanceBetweenCustomers(pred, customer);
            double earliestStartAtInsertion = Math.max(this.endOfServices.get(i) + distToCustomer, earliestStartCustomer);
            double distFromCustomer = data.getDistanceBetweenCustomers(customer, succ);
            double latestStartAtInsertion = Math.min(this.startOfServices.get(i+1) - distFromCustomer - data.getServiceDurations()[customer], latestStartCustomer);

            // if latest start of customer is less than earliest start at position, later position will also not be possible
            if (latestStartCustomer < earliestStartAtInsertion - epsilon) break;

            // check if time window feasible (if enough time between customers already in route
            if (latestStartAtInsertion - earliestStartAtInsertion > epsilon) {
                double additionTravelCosts = distToCustomer + distFromCustomer - data.getDistanceBetweenCustomers(pred, succ);
                possibleInsertions.add(new double[] {customer, this.id, i+1, earliestStartAtInsertion, additionTravelCosts});
            }

        }

        return possibleInsertions;
    }

    public void applyInsertion(double[] insertion, Data data) {
        int pos = (int) insertion[2];
        int customer = (int) insertion[0];
        int demand = data.getDemands()[customer];
        int duration = data.getServiceDurations()[customer];
        double start = insertion[3];
        double additionCosts = insertion[4];

        this.customers.add(pos, customer);
        this.startOfServices.add(pos, start);
        this.endOfServices.add(pos, start+duration);
        this.capacityUsed += demand;
        this.tourLength += additionCosts;
        this.isUsed = true;

    }

    public void printTour() {
        System.out.println("Tour of vehicle " + this.id + ":");
        for (int i = 0; i < this.customers.size() -1; i++) {
            System.out.print(this.customers.get(i) + " -> ");
        }
        System.out.println(this.customers.get(this.customers.size() -1) + "");
    }
}
