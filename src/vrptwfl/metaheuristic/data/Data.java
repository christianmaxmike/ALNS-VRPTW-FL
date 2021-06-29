package vrptwfl.metaheuristic.data;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.utils.DebugUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Data {

    private String instanceName;
    private int nCustomers;
    private int nVehicles;
    private int vehicleCapacity;
    private int[] customers;
    private int[] xcoords;
    private int[] ycoords;
    private int[] demands;
    private int[] earliestStartTimes;
    private int[] latestStartTimes;
    private int[] serviceDurations;
    private double[][] distanceMatrix;
    private double maxDistanceInGraph;
    private double endOfPlanningHorizon;

    public double getMaxDistanceInGraph() {
        return maxDistanceInGraph;
    }


    public int getnCustomers() {
        return nCustomers;
    }

    public double getEndOfPlanningHorizon() {
        return endOfPlanningHorizon;
    }

    public int[] getCustomers() {
        return customers;
    }

    public void setCustomers(int[] customers) {
        this.customers = customers;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public int getnVehicles() {
        return nVehicles;
    }

    public void setnVehicles(int nVehicles) {
        this.nVehicles = nVehicles;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public int[] getXcoords() {
        return xcoords;
    }


    public void setXcoords(int[] xcoords) {
        this.xcoords = xcoords;
    }

    public int[] getYcoords() {
        return ycoords;
    }

    public void setYcoords(int[] ycoords) {
        this.ycoords = ycoords;
    }

    public int[] getDemands() {
        return demands;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public int[] getEarliestStartTimes() {
        return earliestStartTimes;
    }

    public void setEarliestStartTimes(int[] earliestStartTimes) {
        this.earliestStartTimes = earliestStartTimes;
    }

    public int[] getLatestStartTimes() {
        return latestStartTimes;
    }

    public void setLatestStartTimes(int[] latestStartTimes) {
        this.latestStartTimes = latestStartTimes;
    }

    public int[] getServiceDurations() {
        return serviceDurations;
    }

    public void setServiceDurations(int[] serviceDurations) {
        this.serviceDurations = serviceDurations;
    }

    public Data(String instanceName, int nCustomers, int nVehicles, int vehicleCapacity, int[] customers,
                int[] xcoords, int[] ycoords, int[] demands,
                int[] earliestStartTimes, int[] latestStartTimes, int[] serviceDurations) {
        this.instanceName = instanceName;
        this.nCustomers = nCustomers;
        this.nVehicles = nVehicles;
        this.vehicleCapacity = vehicleCapacity;
        this.customers = customers;
        this.xcoords = xcoords;
        this.ycoords = ycoords;
        this.demands = demands;
        this.earliestStartTimes = earliestStartTimes;
        this.latestStartTimes = latestStartTimes;
        this.serviceDurations = serviceDurations;

        this.endOfPlanningHorizon = this.latestStartTimes[0] + this.serviceDurations[0];

        this.createDistanceMatrix();
//        DebugUtils.printNumericMatrix(this.distanceMatrix);  // TODO Debug Methode wieder raus
    }

    private void createDistanceMatrix() {
        this.distanceMatrix = new double[this.xcoords.length][this.xcoords.length];
        this.maxDistanceInGraph = 0.;

        for (int i = 0; i < this.xcoords.length -1; i++) {
            this.distanceMatrix[i][i] = 0;
            for (int j = i+1; j < this.xcoords.length; j++) {
                double distance = this.getDistanceValue(i,j);
                this.distanceMatrix[i][j] = distance;
                this.distanceMatrix[j][i] = distance;

                if (distance > this.maxDistanceInGraph + Config.epsilon) {
                    this.maxDistanceInGraph = distance;
                }
            }
        }

    }

    private double getDistanceValue(int i, int j) {
        double diffX = this.xcoords[i] - this.xcoords[j];
        double diffY = this.ycoords[i] - this.ycoords[j];

        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY,2));
        // round to
        distance = Math.round(distance * Config.roundingPrecisionFactor)/Config.roundingPrecisionFactor;
        return distance;
    }


    public ArrayList<Vehicle> initializeVehicles() {

        ArrayList<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < this.nVehicles; i++) {
            vehicles.add(new Vehicle(i, this.vehicleCapacity, this.endOfPlanningHorizon));
        }

        return vehicles;

    }

    public double getDistanceBetweenCustomers(int customer1, int customer2) {
        return this.distanceMatrix[customer1][customer2];
    }

    // FOR TESTING ONLY
    public void setDemandOfCustomer(int customer, int demand) {
        this.demands[customer] = demand;
    }
}
