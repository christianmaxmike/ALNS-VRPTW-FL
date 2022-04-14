package vrptwfl.metaheuristic.data;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the Data object. In the data object all immutable information
 * of the input data is stored. It encompasses information about the locations,
 * distances between the customers, earliest and latest service times, demands of the 
 * customers, the number of customers/vehicles, as well as the possible locations a
 * customers can be served.
 * 
 * @author: Christian M.M. Frey, Alexander Jungwirth
 */
public class Data {

    private String instanceName;
    private int nCustomers;
    private int nVehicles;
    private int vehicleCapacity;
    private int[] customers;
    private int[] locationCapacity;
    private int[] demands;
    private int[] requiredSkillLvl;
    private int[] vehiclesSkillLvl;
    private int[] earliestStartTimes;
    private int[] latestStartTimes;
    private int[] serviceDurations;
    private double maxDistanceInGraph;
    private double endOfPlanningHorizon;
    private double[][] distanceMatrix;
    private double[][] swappingCosts;
    private double[][] averageStartTimes;
    // private double[][] locationCoordinates; // used for kmeans removal
    private int[] originalCustomerIds;
    private int[] customersPreferredLocationId;
    HashMap<Integer, ArrayList<Integer>> predCustomers;
    
    // Key: CustomerID - Values: LocationIds
    private HashMap<Integer, ArrayList<Integer>> customerToLocations;

	/**
	 * Constructor for data object.
	 * @param instanceName: filename of instance being processed
	 */
    public Data(String instanceName){
    	this.instanceName = instanceName;
    }
    
    /**
     * Constructor for a data object containing all immutable information for 
     * the VRPTW-FL problem
     * @param instanceName: name of the instance
     * @param nCustomers: number of customers
     * @param nVehicles: number of customers
     * @param vehicleCapacity: vehicles' capacity
     * @param customers: array of customers
     * @param locationCapacity: array of location's capacity
     * @param customerToLocations: HashMap linking customers to theirs possible locations
     * @param location2Id: mapping of the x-y-coords to location ids
     * @param demands: array of customers' demands
     * @param earliestStartTimes: array of customers' earliest start service times
     * @param latestStartTimes: array of customers' latest start service times
     * @param serviceDurations: customers' service durations
     */
    public Data(String instanceName, int nCustomers, int nVehicles, int vehicleCapacity, int[] customers,
                int[] locationCapacity, 
                HashMap<Integer, ArrayList<Integer>> customerToLocations, 
                HashMap<java.awt.geom.Point2D, Integer> location2Id,
                int[] demands, int[] earliestStartTimes, int[] latestStartTimes, int[] serviceDurations,
                int[] requiredSkillLvl, int[] vehiclesSkillLvl, HashMap<Integer, ArrayList<Integer>> predJobs,
                int[] preferredLocations) {
        this.instanceName = instanceName;
        this.nCustomers = nCustomers;
        this.nVehicles = nVehicles;
        this.vehicleCapacity = vehicleCapacity;
        this.customers = customers;
        this.originalCustomerIds = new int[customers.length+1];
        System.arraycopy(this.customers, 0, this.originalCustomerIds, 1, this.originalCustomerIds.length-1);
        this.locationCapacity = locationCapacity;
        this.customerToLocations = customerToLocations;
        this.demands = demands;
        this.earliestStartTimes = earliestStartTimes;
        this.latestStartTimes = latestStartTimes;
        this.serviceDurations = serviceDurations;
        this.requiredSkillLvl = requiredSkillLvl;
        this.vehiclesSkillLvl = vehiclesSkillLvl;
        this.customersPreferredLocationId = preferredLocations;
        this.predCustomers = predJobs;

        // service Durations always the same within the dataset
        // latest StartTimes of first customer (depot) indicates the max latest start time
        this.endOfPlanningHorizon = this.latestStartTimes[0] + this.serviceDurations[0];

        // Creates the distance matrix w.r.t the locations
        this.createDistanceMatrix(location2Id);
        // Creates mx of swapping costs
        this.createSwappingCosts();
        // Calculates average start times
        this.calculateAverageStartTimes();
    }

    /**
     * Calculate the average start service times between customers.
     * The average customers' service times are use for the TimeOrientedRemoval operation.
     */
    public void calculateAverageStartTimes() {
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
    private void createDistanceMatrix(Map<java.awt.geom.Point2D, Integer> location2Id) {
    	int n = location2Id.size();
    	int m = location2Id.size();
    	this.distanceMatrix = new double[n][m];
    	// this.locationCoordinates = new double[n][2];
        this.maxDistanceInGraph = 0.;
        
        Map<Object, Object> sortedMap = location2Id.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        for (int i = 0; i<n-1; i++) {
        	this.distanceMatrix[i][i] = 0;
        	for (int j = i+1; j<m; j++) {
        		double distance = this.getDistanceValue((java.awt.geom.Point2D) sortedMap.get(i), 
        												(java.awt.geom.Point2D) sortedMap.get(j));
        		this.distanceMatrix[i][j] = distance;
        		this.distanceMatrix[j][i] = distance;
        		
        		// this.locationCoordinates[i][0] = ((java.awt.geom.Point2D) sortedMap.get(i)).getX();
        		// this.locationCoordinates[i][1] = ((java.awt.geom.Point2D) sortedMap.get(i)).getY();
        		
        		if (distance > this.maxDistanceInGraph + Config.epsilon) 
        			this.maxDistanceInGraph = distance;
        	}
        }
    }
    
    /**
     * Creates matrix of swapping costs. If a customer cannot be served at its 
     * preferential location, the total costs subsumes the swapping costs of all
     * customers which are served at any of their non-preferential locations.
     */
    public void createSwappingCosts() {
    	this.swappingCosts = new double[this.distanceMatrix.length][this.distanceMatrix[0].length];
    	for (int n = 0; n < swappingCosts.length; n++) {
    		for (int m = n+1; m < swappingCosts[n].length; m++) {
    			double dist = this.distanceMatrix[n][m];
    			double swapCost = Math.pow(dist, Config.exponentSwappingLocations);
    			this.swappingCosts[n][m] = swapCost;
    			this.swappingCosts[m][n] = swapCost;
    		}
    	}
    }

	/**
	 * Calling this method will initialize n vehicles according to 
	 * nVehicles. Vehicles are initialized with values being defined
	 * for vehicleCapacity and endOfPlanningHorizon. 
	 * @return list containing  (default) vehicles.
	 */
    public ArrayList<Vehicle> initializeVehicles() {
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < this.nVehicles; i++) {
            vehicles.add(new Vehicle(i, this.vehicleCapacity, this.endOfPlanningHorizon, this.vehiclesSkillLvl[i]));
        }
        return vehicles;
    }
    

    //
    // CUSTOM GET FNCS
    //    
    /**
     * Retrieve the average start service times between two customers.
     * @param customerI: id of first customer
     * @param customerJ: id of second customer
     * @return average start service time between first and second customer 
     */
    public double getAverageStartTimes(int customerI, int customerJ) {
        return averageStartTimes[customerI][customerJ];
    }
    
    /**
     * Get the distance between to coordinates/points. 
     * @param p1: first Point2D specifying a point with x-coordinates and y-coordinates
     * @param p2: second Point2D specifying a point with x-coordinates and y-coordinates
     * @return distance between the two points
     */
    private double getDistanceValue(java.awt.geom.Point2D p1, java.awt.geom.Point2D p2) {
    	double distance = p1.distance(p2);
        distance = Math.round(distance * Config.roundingPrecisionFactor)/Config.roundingPrecisionFactor;
        return distance;    	
    }
    
    /**
     * Retrieve the distance between two locations.
     * @param location1: id of first location
     * @param location2: id of second location
     * @return distance between two locations
     */
    public double getDistanceBetweenLocations (int location1, int location2) {
    	return this.getDistanceMatrix()[location1][location2];
    }

    
    //
    // GETTERS
    //
    /**
     * Get maximal distance of locations
     * @return maximal distance
     */
    public double getMaxDistanceInGraph() {
        return maxDistanceInGraph;
    }

    /**
     * Get number of customers
     * @return number of customers
     */
    public int getnCustomers() {
        return nCustomers;
    }

    /**
     * Get end of the planning horizon
     * @return end of planning horizon
     */
    public double getEndOfPlanningHorizon() {
        return endOfPlanningHorizon;
    }

    /**
     * Get array of customers
     * @return array of customers
     */
    public int[] getCustomers() {
        return customers;
    }
   
    /**
     * Retrieve the distance matrix
     * @return distance matrix
     */
    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    /**
     * Get the instance name
     * @return instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Get number of vehicles
     * @return number of vehicles
     */
    public int getnVehicles() {
        return nVehicles;
    }

    /**
     * Get the vehicle's capacity
     * @return vehicle's capacity
     */
    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    /**
     * Get the array of customer's demands. 
     * @return customer's demands
     */
    public int[] getDemands() {
        return demands;
    }

    /**
     * Retrieve array of earliest start service times
     * @return earliest start service times
     */
    public int[] getEarliestStartTimes() {
        return earliestStartTimes;
    }
    
    /**
     * Retrieve array of latest start service times
     * @return latest start service times
     */
    public int[] getLatestStartTimes() {
        return latestStartTimes;
    }

    /**
     * Retrieve array of service durations
     * @return customer's service durations
     */
    public int[] getServiceDurations() {
        return serviceDurations;
    }
    
    /**
     * Retrieve array storing the customers required skill level.
     * @return required skill leve for all customers (i-th position = required skill level of i-th customer)
     */
    public int[] getRequiredSkillLvl() {
    	return requiredSkillLvl;
    }
    
    /**
     * Get the locations where customers can be processed. 
     * Customer's id is used as key for the retrieved HasMap. 
     * The individual lists contain the location ids.
     * @return customer's assignment to locations
     */
    public HashMap<Integer, ArrayList<Integer>> getCustomersToLocations () {
    	return this.customerToLocations;
    }
    
    /**
     * Get the locations' capacities
     * @return array of locations' capacities
     */
    public int[] getLocationCapacity() {
    	return this.locationCapacity;
    }
    
    /**
     * Retrieve original customer ids
     * @return: original customer ids
     */
    public int[] getOriginalCustomerIds() {
    	return this.originalCustomerIds;
    }
    
    /**
     * Retrieve mapping from customers to their predecessor jobs
     * @return: Predecessor jobs (key: customer id - value: list of predecessor jobs)
     */
    public HashMap<Integer, ArrayList<Integer>> getPredCustomers() {
    	return this.predCustomers;
    }
    
    /**
     * Retrieve customers preferential location identifiers.
     * @return: preferred location id
     */
    public int[] getCustomersPreferredLocation() {
    	return this.customersPreferredLocationId;
    }
    
    /**
     * Retrieve matrix storing the swapping costs from one location to another
     * @return: Swapping costs
     */
    public double[][] getSwappingCosts() {
    	return this.swappingCosts;
    }

    
    //
    // SETTERS
    //
    /**
     * Set the customers
     * @param customers: customers array
     */
    public void setCustomers(int[] customers) {
        this.customers = customers;
    }

    /**
     * Set the vehicles' capacities
     * @param vehicleCapacity: vehicles' capacities
     */
    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }
    
    /**
     * Set the number of vehicles
     * @param nVehicles: number of vehicles
     */
    public void setNVehicles(int nVehicles) {
    	this.nVehicles = nVehicles;
    }
    
    /**
     * Set the number of customers
     * @param nCustomers: number of customers
     */
    public void setNCustomers(int nCustomers) {
    	this.nCustomers = nCustomers;
    }

    /**
     * Set the customers' demands
     * @param demands: customers' demands
     */
    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    /**
     * Set the customers' earliest start service times
     * @param earliestStartTimes: earliest start service times
     */
    public void setEarliestStartTimes(int[] earliestStartTimes) {
        this.earliestStartTimes = earliestStartTimes;
    }

    /**
     * Set the customers' latest start service times
     * @param latestStartTimes: latest start service times
     */
    public void setLatestStartTimes(int[] latestStartTimes) {
        this.latestStartTimes = latestStartTimes;
    }

    /**
     * Set the service durations
     * @param serviceDurations: service durations
     */
    public void setServiceDurations(int[] serviceDurations) {
        this.serviceDurations = serviceDurations;
    }

    /**
     * Sets the distance matrix (nLocations x nLocations) of the data object.
     * @param distanceMatrix: distance matrix
     */
    public void setDistanceMatrix(double[][] distanceMatrix) {
    	this.distanceMatrix = distanceMatrix;
    }
    
    /**
     * Sets the capacity slots of the locations
     * @param locationCapacity: list of capacity slots. i-th index indicates the number of slots for the i-th location (0=depot)
     */
    public void setLocationCapacity(ArrayList<Integer> locationCapacity) {
    	this.locationCapacity = DataUtils.convertListToArray(locationCapacity);
    }
    
    /**
     * Sets the maximal distance between locations = max value in distance matrix
     * @param maxDistValue: maximal distance
     */
    public void setMaxDistanceInGraph(double maxDistValue) {
    	this.maxDistanceInGraph = maxDistValue;
    }
    
    /**
     * Sets HashMap mapping customers to theirs individual possible locations.
     * Key: customer id - Value: list of locations
     * @param customerToLocations: mapping customers to their possible locations
     */
    public void setCustomerToLocation(HashMap<Integer, ArrayList<Integer>> customerToLocations) {    	
    	this.customerToLocations = customerToLocations; 
    }
    
    /**
     * Sets the end of the planing horizon. Indicator for end of any service time.
     * @param endOfPlanningHorizon: end of planning horizon
     */
    public void setEndOfPlanningHorizon(int endOfPlanningHorizon) {
    	this.endOfPlanningHorizon = endOfPlanningHorizon;
    }
    
    /**
     * Sets the original customer identifiers.
     * @param originalCustomerIds: original identifiers
     */
    public void setOriginalCustomerIds(int[] originalCustomerIds) {
    	this.originalCustomerIds = originalCustomerIds;
    }
    
    /**
     * Sets the required skill level being necessary for a customer to be served.
     * @param requiredSkillLvl: required skill level
     */
    public void setRequiredSkillLvl(int[] requiredSkillLvl) {
    	this.requiredSkillLvl = requiredSkillLvl;
    }
    
    /**
     * Sets the vehicles skill level. 
     * @param vehiclesSkillLvl: vehicle skill level
     */
    public void setVehiclesSkillLvl(int[] vehiclesSkillLvl) {
    	this.vehiclesSkillLvl = vehiclesSkillLvl;
    }
    
    /**
     * Sets the predecessor jobs of customers.
     * Key: customer id - value: identifiers of predecessor jobs
     * @param predCustomers: mapping of customers to theirs predecessor jobs
     */
    public void setPredCustomers(HashMap<Integer, ArrayList<Integer>> predCustomers) {
    	this.predCustomers = predCustomers;
    }
    
    /**
     * Sets the preferred location identifiers of customers. If a customer is served at its
     * preferred location, there are no swapping costs.
     * @param preferredLocations: preferential location identifiers
     */
    public void setCustomersPreferredLocation(int[] preferredLocations) {
    	this.customersPreferredLocationId = preferredLocations;
    }
    
    
    //
    // Custom setters
    //
    /**
     * Sets the demand of a customer
     * @param customer: customer id
     * @param demand: customer's demand
     */
    public void setDemandOfCustomer(int customer, int demand) {
        this.demands[customer] = demand;
    }
}