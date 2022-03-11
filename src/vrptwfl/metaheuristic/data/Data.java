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
    //private int[] xcoords;
    //private int[] ycoords;
    private int[][] multipleXCoords;
    private int[][] multipleYCoords;
    private int[] customerAffiliationToLocations;
    private int[] locationCapacity;
    private int[] demands;
    private int[] earliestStartTimes;
    private int[] latestStartTimes;
    private int[] serviceDurations;
    private double[][] distanceMatrix;
    private double maxDistanceInGraph;
    private double endOfPlanningHorizon;
    private double[][] averageStartTimes;

    
    public Data(String instanceName, int nCustomers, int nVehicles, int vehicleCapacity, int[] customers,
                int[] xcoords, int[] ycoords, int[][] multipleXCoords, int[][] multipleYCoords, int[] customerAffiliationToCustomers, int[] locationCapacity,
                int[] demands, int[] earliestStartTimes, int[] latestStartTimes, int[] serviceDurations) {
        this.instanceName = instanceName;
        this.nCustomers = nCustomers;
        this.nVehicles = nVehicles;
        this.vehicleCapacity = vehicleCapacity;
        this.customers = customers;
        //this.xcoords = xcoords;
        //this.ycoords = ycoords;
        this.multipleXCoords = multipleXCoords;
        this.multipleYCoords = multipleYCoords;
        this.customerAffiliationToLocations = customerAffiliationToCustomers;
        this.locationCapacity = locationCapacity;
        this.demands = demands;
        this.earliestStartTimes = earliestStartTimes;
        this.latestStartTimes = latestStartTimes;
        this.serviceDurations = serviceDurations;

        // service Durations always the same within the dataset
        // latest StartTimes of first customer indicates the max latest start time
        this.endOfPlanningHorizon = this.latestStartTimes[0] + this.serviceDurations[0];

        // Creates the distance matrix w.r.t the locations
        this.createDistanceMatrix();
        this.calculateAverageStartTimes();
//        DebugUtils.printNumericMatrix(this.distanceMatrix);  // TODO Debug Methode wieder raus
    }



    private void calculateAverageStartTimes() {
        this.averageStartTimes = new double[this.nCustomers + 1][this.nCustomers + 1];  // index 0 is depot
        for (int i = 1; i < this.nCustomers; i++) {
            this.averageStartTimes[i][i] = 0.;
            for (int j = i+1; j <= this.nCustomers; j++) {
                double averageTime = Math.abs((this.earliestStartTimes[i] + this.latestStartTimes[i])  - (this.earliestStartTimes[j] + this.latestStartTimes[j]))/2.;
                this.averageStartTimes[i][j] = averageTime;
                this.averageStartTimes[j][i] = averageTime;
            }
        }
    }

    /**
     * Initializes the class variable distanceMatrix for storing the distances
     * between the various locations within the data input.
     * Initializes also the max distance which can be observed upon the locations.
     * The distance matrix is only formulated on the locations and is not customer dependent.
     */
    private void createDistanceMatrix() {
    	int n = this.multipleXCoords[0].length;
    	int m = this.multipleYCoords[0].length;
        this.distanceMatrix = new double[n][m];
        this.maxDistanceInGraph = 0.;

        for (int i = 0; i < n-1; i++) {
            this.distanceMatrix[i][i] = 0;
            for (int j = i+1; j < m; j++) {
                double distance = this.getDistanceValue(i,j);
                this.distanceMatrix[i][j] = distance;
                this.distanceMatrix[j][i] = distance;

                if (distance > this.maxDistanceInGraph + Config.epsilon) {
                    this.maxDistanceInGraph = distance;
                }
            }
        }
    }


    public ArrayList<Vehicle> initializeVehicles() {
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < this.nVehicles; i++) {
            vehicles.add(new Vehicle(i, this.vehicleCapacity, this.endOfPlanningHorizon));
        }
        return vehicles;

    }
    
    //
    // CUSTOM GET FNCS
    //
    public double getDistanceBetweenCustomers(int customer1, int customer2) {
        return this.distanceMatrix[customer1][customer2];
    }
    
    public double getAverageStartTimes(int customerI, int customerJ) {
        return averageStartTimes[customerI][customerJ];
    }
    
    /**
     * Retrieve the distance between to location referenced by params i and j directing to the
     * first row of xcoords and ycoords data.
     * @param i: identifier of first location
     * @param j: identifier of second location
     * @return distance between two locations
     */
    private double getDistanceValue(int i, int j) {
        //double diffX = this.xcoords[i] - this.xcoords[j];
        //double diffY = this.ycoords[i] - this.ycoords[j];
    	double diffX = this.multipleXCoords[0][i] - this.multipleXCoords[0][j];
    	double diffY = this.multipleYCoords[0][i] - this.multipleYCoords[0][j];

        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY,2));
        // round to
        distance = Math.round(distance * Config.roundingPrecisionFactor)/Config.roundingPrecisionFactor;
        return distance;
    }

    
    //
    // GETTERS
    //
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
  
    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public int getnVehicles() {
        return nVehicles;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public int[] getDemands() {
        return demands;
    }

    public int[] getEarliestStartTimes() {
        return earliestStartTimes;
    }
    
    public int[] getLatestStartTimes() {
        return latestStartTimes;
    }

    public int[] getServiceDurations() {
        return serviceDurations;
    }


    //
    // SETTERS
    //
    public void setCustomers(int[] customers) {
        this.customers = customers;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public void setEarliestStartTimes(int[] earliestStartTimes) {
        this.earliestStartTimes = earliestStartTimes;
    }

    public void setLatestStartTimes(int[] latestStartTimes) {
        this.latestStartTimes = latestStartTimes;
    }

    public void setServiceDurations(int[] serviceDurations) {
        this.serviceDurations = serviceDurations;
    }


    // FOR TESTING ONLY
    public void setDemandOfCustomer(int customer, int demand) {
        this.demands[customer] = demand;
    }
}
