package vrptwfl.metaheuristic.data;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
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
    private double startOfPlanningHorizon;
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
    private HashMap<Integer, ArrayList<Integer>> locationsToCustomers;
    
    // GLS
    private double[][] glsCounterViolations;
    private double[][] glsPenalties;
    private double[] sumGLSCounterViolations;
    private ArrayList<Solution> glsSolutionHistory;

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
                HashMap<Integer, ArrayList<Integer>> locationsToCustomers,
                HashMap<java.awt.geom.Point2D, Integer> location2Id,
                int[] demands, int[] earliestStartTimes, int[] latestStartTimes, int[] serviceDurations,
                int[] requiredSkillLvl, int[] vehiclesSkillLvl, HashMap<Integer, ArrayList<Integer>> predJobs,
                int[] preferredLocations) {
        this.instanceName = instanceName;
        this.nCustomers = nCustomers;
        this.nVehicles = nVehicles;
        this.vehicleCapacity = vehicleCapacity;
        this.customers = customers;
        this.originalCustomerIds = new int[customers.length +1];
        System.arraycopy(this.customers, 0, this.originalCustomerIds, 1, this.originalCustomerIds.length-1);
        this.locationCapacity = locationCapacity;
        this.customerToLocations = customerToLocations;
        this.locationsToCustomers = locationsToCustomers;
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
        // this.endOfPlanningHorizon = this.latestStartTimes[0] + this.serviceDurations[0];

        // Creates the distance matrix w.r.t the locations
        this.createDistanceMatrix(location2Id);
        // Creates matrix of swapping costs
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
        this.maxDistanceInGraph = 0.;
        
        Map<Object, Object> sortedMap = location2Id.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        for (int i = 0; i<n-1; i++) {
        	this.distanceMatrix[i][i] = 0;
        	for (int j = i+1; j<m; j++) {
        		double distance = this.getDistanceValue((java.awt.geom.Point2D) sortedMap.get(i), 
        												(java.awt.geom.Point2D) sortedMap.get(j));
        		this.distanceMatrix[i][j] = distance;
        		this.distanceMatrix[j][i] = distance;
        		
        		if (distance > this.maxDistanceInGraph + Config.getInstance().epsilon) 
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
    	if (Config.getInstance().exponentSwappingLocations >= 0) {
    		for (int n = 0; n < swappingCosts.length; n++) {
    			for (int m = n+1; m < swappingCosts[n].length; m++) {
    				double dist = this.distanceMatrix[n][m];
    				double swapCost = Math.pow(dist, Config.getInstance().exponentSwappingLocations);
    				this.swappingCosts[n][m] = swapCost;
    				this.swappingCosts[m][n] = swapCost;
    			}
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
            vehicles.add(new Vehicle(i, this.vehicleCapacity, this.startOfPlanningHorizon, this.endOfPlanningHorizon, this.vehiclesSkillLvl[i]));
        }
        return vehicles;
    }
    
    /**
     * Initialize counter and penalty array used for the guided local search (GLS)
     */
    public void initGLSSettings() {
    	this.glsCounterViolations = new double[DataUtils.PenaltyIdx.values().length][this.getnCustomers() + 1];
    	this.sumGLSCounterViolations = new double[DataUtils.PenaltyIdx.values().length];
    	this.glsSolutionHistory = new ArrayList<Solution>();
    	
    	this.glsPenalties = new double[DataUtils.PenaltyIdx.values().length][this.getnCustomers() + 1];
    	for (double[] row: this.glsPenalties)
    		Arrays.fill(row, Config.getInstance().glsPenaltyInitValue);
    	
    }
    
    /**
     * Reset the violation counters used for guided local search (GLS)
     */
    public void resetGLSSettings () {
    	this.glsCounterViolations = new double[DataUtils.PenaltyIdx.values().length][this.getnCustomers() + 1];    	
    	this.sumGLSCounterViolations = new double[DataUtils.PenaltyIdx.values().length];
    	this.glsSolutionHistory.clear();
    }
    
    public void updateGLSCounter (Solution s) {
    	// Iterate observed penalties in attached solution
    	for (int[] entry: s.getListOfPenalties()) {
    		// Increment counter of occurrence of penalty (entry[0]: penalty identifier (DataUtils.PenaltyIdx); entry[1]: customer id)
    		this.glsCounterViolations[entry[0]][entry[1]] ++;
    		this.sumGLSCounterViolations[entry[0]] ++;
    	}    		
    }
    
    public void addToGLSSolutionHistory(Solution s) {
    	this.glsSolutionHistory.add(s);
    }
    
    public TreeSet<double[]> glsInitUtilitySet() {
    	TreeSet<double[]> utilitiesSet =  new TreeSet<double[]>(new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				return o1[0] > o2[0] ? -1 : 1;
			}
		});
    	return utilitiesSet;
    }
    
    private void calcUtilitySet (TreeSet<double[]> utilitiesSet) {
    	//for (Solution s : this.glsSolutionHistory) {
    		// Iterate observed penalties in attached solution
    		//for (int[] entry: s.getListOfPenalties()) {	
    		for (int violationid = 0 ; violationid < DataUtils.PenaltyIdx.values().length; violationid ++) {
    			for (int customerid = 0; customerid<this.customers.length; customerid++) {
    				int[] entry = new int[] {violationid, customerid};
    				// Get counter & penalty values
    				double counter = this.glsCounterViolations[entry[0]][entry[1]];
    				double penalty = this.glsPenalties[entry[0]][entry[1]];
    				
    				// Get cost of violation
    				double violationCost = -1;
    				DataUtils.PenaltyIdx whichPenalty = DataUtils.PenaltyIdx.values()[entry[0]];
    				switch(whichPenalty) {
    				case TWViolation: violationCost = Config.getInstance().costTimeWindowViolation; break;
    				case Unscheduled: violationCost = Config.getInstance().costUnservedCustomerViolation; break;
    				case Predecessor: violationCost = Config.getInstance().costPredJobsViolation; break;
    				case Capacity: violationCost = Config.getInstance().costCapacityViolation; break;
    				case SkillLvl: violationCost = Config.getInstance().costSkillLvlViolation; break;
    				default: violationCost = -1;
    				}
    				
    				// Calculate utility and add to set of utilities (sorted by utility values in descending order)
    				double utility = (counter * violationCost) / (1 + penalty);
    				utilitiesSet.add(new double[] {utility, entry[0], entry[1]});
    			}
    		//}
    		//}
    	}
    }
    
    public void glsFeatureUpdatePenaltyWeights() {
    	TreeSet<double[]> utilitiesSet = glsInitUtilitySet();
    	this.calcUtilitySet(utilitiesSet);
    	
    	boolean[] penaltyFlags = new boolean[DataUtils.PenaltyIdx.values().length];
    	for (int i=0; i<utilitiesSet.size(); i++) {
    		double[] utilityEntry = utilitiesSet.pollFirst();
    		if (utilityEntry == null) break;
    		
    		int penaltyIdx = (int) utilityEntry[1];
			double counter = sumGLSCounterViolations[penaltyIdx];
			DataUtils.PenaltyIdx whichPenalty = DataUtils.PenaltyIdx.values()[penaltyIdx];
			switch(whichPenalty) {
				case Unscheduled: 
					if (!penaltyFlags[penaltyIdx] && counter != 0.0) 
						Config.getInstance().glsFeatureUnserved = Math.min(Config.getInstance().glsFeatureRangeUnserved[1], Config.getInstance().glsFeatureUnserved * (counter * Config.getInstance().glsFeatureOmega));
				case TWViolation: 
					if (!penaltyFlags[penaltyIdx] && counter != 0.0) 
						Config.getInstance().glsFeatureTimeWindow = Math.min(Config.getInstance().glsFeatureRangeTimeWindow[1], Config.getInstance().glsFeatureTimeWindow * (counter * Config.getInstance().glsFeatureOmega));
				case Predecessor: 
					if (!penaltyFlags[penaltyIdx] && counter != 0.0) 
						Config.getInstance().glsFeaturePredJobs = Math.min(Config.getInstance().glsFeatureRangePredJobs[1], Config.getInstance().glsFeaturePredJobs * (counter * Config.getInstance().glsFeatureOmega));					
				case Capacity:
					if (!penaltyFlags[penaltyIdx] && counter != 0.0) 
						Config.getInstance().glsFeatureCapacity = Math.min(Config.getInstance().glsFeatureRangeCapacity[1], Config.getInstance().glsFeatureCapacity * (counter * Config.getInstance().glsFeatureOmega));				
				case SkillLvl: 
					if (!penaltyFlags[penaltyIdx] && counter != 0.0)
						Config.getInstance().glsFeatureSkill = Math.min(Config.getInstance().glsFeatureRangeSkill[1], Config.getInstance().glsFeatureSkill * (counter * Config.getInstance().glsFeatureOmega));
			}   		
			penaltyFlags[penaltyIdx] = true; 
    	}
    	
    	for (DataUtils.PenaltyIdx penaltyIdx : DataUtils.PenaltyIdx.values()) {
			// double counter = sumGLSCounterViolations[penaltyIdx.getId()];
    		
    		// TODO: Check for " .../ ((1+counter) * Config.getInstance().glsFeatureOmega) "; 
			switch(penaltyIdx) {
			case Unscheduled: 
				if (!penaltyFlags[penaltyIdx.getId()]) 
					Config.getInstance().glsFeatureUnserved = Math.max(Config.getInstance().glsFeatureRangeUnserved[0], Config.getInstance().glsFeatureUnserved / (Config.getInstance().glsFeatureOmega));
			case TWViolation: 
				if (!penaltyFlags[penaltyIdx.getId()]) 
					Config.getInstance().glsFeatureTimeWindow = Math.max(Config.getInstance().glsFeatureRangeTimeWindow[0], Config.getInstance().glsFeatureTimeWindow / (Config.getInstance().glsFeatureOmega));
			case Predecessor: 
				if (!penaltyFlags[penaltyIdx.getId()]) 
					Config.getInstance().glsFeaturePredJobs = Math.max(Config.getInstance().glsFeatureRangePredJobs[0], Config.getInstance().glsFeaturePredJobs / (Config.getInstance().glsFeatureOmega));					
			case Capacity:
				if (!penaltyFlags[penaltyIdx.getId()]) 
					Config.getInstance().glsFeatureCapacity = Math.max(Config.getInstance().glsFeatureRangeCapacity[0], Config.getInstance().glsFeatureCapacity / (Config.getInstance().glsFeatureOmega));				
			case SkillLvl: 
				if (!penaltyFlags[penaltyIdx.getId()])
					Config.getInstance().glsFeatureSkill = Math.max(Config.getInstance().glsFeatureRangeSkill[0], Config.getInstance().glsFeatureSkill / (Config.getInstance().glsFeatureOmega));
			}
    	}
    }

    /**
     * Update penalties used for the guided local search (GLS). 
     * Violations yielding the highest utilities are update.
     * A utility is calculated by (counter * violationCost) / (1 + penalty).
     * The penalty of the highest utility (customer-dependent) are increment by
     * a constant value defined in the configuration file (Config.getInstance().glsPenaltyIncrease).
     * @param s: solution object carrying information about the observed violations
     */
    public void glsUpdatePenaltyWeights() {
    	TreeSet<double[]> utilitiesSet = glsInitUtilitySet();
    	
    	// Decrement penalty values by constant reduction value (set by Config.getInstance().glsPenaltyReduction)
    	for (int i = 0 ; i<this.glsPenalties.length; i++) {
    		for (int j = 0; j<this.glsPenalties[i].length; j++) {
    			// TODO: checken, Auswirkung von init value
    			this.glsPenalties[i][j] = Math.max(Config.getInstance().glsPenaltyInitValue, this.glsPenalties[i][j] - Config.getInstance().glsPenaltyReduction);
    			// this.glsPenalties[i][j] = Math.max(0, this.glsPenalties[i][j] - Config.getInstance().glsPenaltyReduction);
    		}
    	}
    	
    	// Calculate utility set
    	this.calcUtilitySet(utilitiesSet);
    	
    	// Update penalty values for n features (set by Config.getInstance().glsNFeturesForPenaltyUpdate
    	for (int i=0; i<Config.getInstance().glsNFeaturesForPenaltyUpdate; i++) {
    		double[] utilityEntry = utilitiesSet.pollFirst();
    		if (utilityEntry == null) break;
    		this.glsPenalties[(int) utilityEntry[1]][(int) utilityEntry[2]] += (Config.getInstance().glsPenaltyIncrease + Config.getInstance().glsPenaltyReduction);
    	}
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
        distance = Math.round(distance * Config.getInstance().roundingPrecisionFactor)/Config.getInstance().roundingPrecisionFactor;
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
    
    public double getStartOfPlanningHorizon() {
    	return startOfPlanningHorizon;
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
     * @return required skill level for all customers (i-th position = required skill level of i-th customer)
     */
    public int[] getRequiredSkillLvl() {
    	return requiredSkillLvl;
    }
    
    /**
     * Get the locations where customers can be processed. 
     * Customer's id is used as key for the retrieved HashMap. 
     * The individual lists contain the location identifierss.
     * @return customer's assignment to locations
     */
    public HashMap<Integer, ArrayList<Integer>> getCustomersToLocations () {
    	return this.customerToLocations;
    }
    
    /**
     * Get the customers being assigned to their possible locations.
     * The location identifiers are used as keys for the retrieved HashMap.
     * The individual lists contain the customer identifiers.
     * @return mapping from location to customers
     */
    public HashMap<Integer, ArrayList<Integer>> getLocationsToCustomers() {
    	return this.locationsToCustomers;
    }
    
    /**
     * Get the locations' capacities
     * @return array of locations' capacities
     */
    public int[] getLocationCapacity() {
    	return this.locationCapacity;
    }
    
    /**
     * Retrieve original customer identifiers
     * @return: original customer identifiers
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
    
    /**
     * Retrieve the violations counter used in the guided local search (GLS) heuristic
     * @return counter of violation occurrences
     */
    public double[][] getGLSCounterViolations () {
    	return this.glsCounterViolations;
    }
    
    /**
     * Retrieve aggregated counter for violations
     * @return counter of violations (over all customers)
     */
    public double[] getSumGLSCounterViolations() {
    	return this.sumGLSCounterViolations;
    }
    
    /**
     * Retrieve the penalty values used in the guided local search (GLS) heuristic.
     * @return penalties (GLS)
     */
    public double[][] getGLSPenalties () {
    	return this.glsPenalties;
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
     * Sets HashMap mapping locations to customers being possible scheduled for the respective location.
     * @param locationsToCustomers: mapping locations to customers
     */
    public void setLocationsToCustomers(HashMap<Integer, ArrayList<Integer>> locationsToCustomers) {
    	this.locationsToCustomers = locationsToCustomers;
    }
    
    /**
     * Sets the end of the planing horizon. Indicator for end of any service time.
     * @param endOfPlanningHorizon: end of planning horizon
     */
    public void setEndOfPlanningHorizon(int endOfPlanningHorizon) {
    	this.endOfPlanningHorizon = endOfPlanningHorizon;
    }
    
    public void setStartOfPlanningHorizon (int startOfPlanningHorizon) {
    	this.startOfPlanningHorizon = startOfPlanningHorizon;
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