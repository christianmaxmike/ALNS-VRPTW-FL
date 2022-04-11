package vrptwfl.metaheuristic.common;

import com.google.common.base.Objects;
import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class implementing the solution object.
 * A solution object carries the information about unassigned customers,
 * temporary infeasible customers, the total costs, a list of all vehicles,
 * the customers affiliation to their current location and capacity slots,
 * as well as the occupancy of capacity slots of all locations for the 
 * whole range of possible service times. We also have an indicator if 
 * the solution object leads to a feasible solution or not.
 * 
 * @author: Christian M.M. Frey, Alexander Jungwirth
 */
public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private ArrayList<Integer> tempInfeasibleCustomers; // needed to store customer that cannot be assigned to any route
    private ArrayList<Vehicle> vehicles;
    private Data data;
    private double totalCosts;
    private double vehicleTourCosts;
    private double swappingCosts;
    private double penaltyUnservedCustomers;
    private double penaltyTimeWindowViolation;
    private double penaltyPredJobsViolation;
    private double penaltySkillViolation;
    private boolean isFeasible = false;
    private boolean isConstruction = false;
    private HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>> map; // location -> capacitySlot -> list of service time tuples [start, end]
    private int[] customersAssignedLocations;     // length: customer size + 1 (depot)
    private int[] customersAssignedCapacitySlot;  // length: customer size + 1 (depot)
    private int[] customersAssignedToVehicles;	  // length: customer size + 1 (depot)

    private HashMap<Integer, ArrayList<double[]>> triedInsertions;
    
    
    /**
     * Constructor for a solution object.
     * @param data: Data object
     */
    public Solution(Data data) {
        this.data = data;
    }

    /**
     * Assigns the solution's parameters being attached to this solution object.
     * @param solutionTemp: solution object whose parameters will be inherited
     */
	public void setSolution(Solution solutionTemp) {
		this.notAssignedCustomers = new ArrayList<>(solutionTemp.getNotAssignedCustomers());
		this.totalCosts = solutionTemp.getTotalCosts();
		this.penaltyPredJobsViolation = solutionTemp.penaltyPredJobsViolation;
		this.penaltySkillViolation = solutionTemp.penaltySkillViolation;
		this.penaltyTimeWindowViolation = solutionTemp.penaltyTimeWindowViolation;
		this.penaltyUnservedCustomers = solutionTemp.penaltyUnservedCustomers;
		this.swappingCosts = solutionTemp.swappingCosts;
		this.vehicleTourCosts = solutionTemp.vehicleTourCosts;
		this.isFeasible = solutionTemp.isFeasible();
		this.customersAssignedCapacitySlot = Arrays.copyOf(solutionTemp.getCustomerAffiliationToCapacity(), solutionTemp.getCustomerAffiliationToCapacity().length);
		this.customersAssignedLocations = Arrays.copyOf(solutionTemp.getCustomerAffiliationToLocations(), solutionTemp.getCustomerAffiliationToLocations().length);
		this.customersAssignedToVehicles = Arrays.copyOf(solutionTemp.getCustomersAssignedToVehicles(), solutionTemp.getCustomersAssignedToVehicles().length);
		
        this.triedInsertions = new HashMap<Integer, ArrayList<double[]>>();

		// Copy vehicles
		ArrayList<Vehicle> newVehicles = new ArrayList<>();
		for (Vehicle veh: solutionTemp.getVehicles())
			newVehicles.add(veh.copyDeep());
		this.setVehicles(newVehicles);
	}
	
	/**
	 * This function yields an empty solution, i.e, it initializes vehicle
	 * objects and set the field variables to its default values
	 * @param data: Data object
	 * @return: Empty solution object
	 */
    @SuppressWarnings("serial")
	public static Solution getEmptySolution(Data data) {
        Solution start = new Solution(data);
        start.setVehicles(data.initializeVehicles());
        // initially add all customers to list of not assigned customers
        start.setNotAssignedCustomers(new ArrayList<Integer>() {{ for (int i : data.getCustomers()) add(i); }});
        start.setTempInfeasibleCustomers(new ArrayList<>());
        start.setFeasible(false);
        
        start.triedInsertions = new HashMap<Integer, ArrayList<double[]>>();
        
        // Create array indicating to which location a customer is assigned to (-1: no assignment)
        start.customersAssignedLocations = new int[data.getCustomers().length+1];
        Arrays.fill(start.customersAssignedLocations, -1);
        start.customersAssignedLocations[0] = 0;  // <- Depot
        
        // Create array indicating to which capacity slot a customer is assigned to (-1: no assignment)
        start.customersAssignedCapacitySlot = new int[data.getCustomers().length+1];
        Arrays.fill(start.customersAssignedCapacitySlot, -1);
        start.customersAssignedCapacitySlot[0] = 0; // -> Depot
        
        // Create array indicating to which vehicle a customer is assigned to (-1: no assignment)
        start.customersAssignedToVehicles = new int[data.getCustomers().length + 1];
        Arrays.fill(start.customersAssignedToVehicles, -1);
        start.customersAssignedToVehicles[0] = 0; // -> Depot
        
        start.map = new HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>>();

        for (int loc = 0; loc<data.getDistanceMatrix().length; loc++) {
        	for (int capacity = 0; capacity < data.getLocationCapacity()[loc]; capacity ++) {
        		ArrayList<Double[]> tmp = new ArrayList<Double[]>();
        		tmp.add(new Double[] {-1.0, 0.0});
        		tmp.add(new Double[] {data.getEndOfPlanningHorizon(), -1.0});
        		if (start.map.get(loc) == null)
        			start.map.put(loc, new HashMap<Integer, ArrayList<Double[]>>());
        		start.map.get(loc).put(capacity, new ArrayList<Double[]>(tmp));
        	}
        }
        return start;
    }
	
    //
    // FUNCTIONALITY
    //
    /**
     * Retrieve the available Location-TimeWindow (LTW) information of a customer.
     * @param customer: customer id
     * @param earliestStartCustomer: earliest start service time 
     * @param latestStartCustomer: latest end service time
     * @param serviceTime: service time duration
     * @param pred: id of predecessor within the route
     * @param succ: id of successor within the route
     * @param endServicePred: end service time of predecessor
     * @param startServiceSucc: start service time of successor
     * @return
     */
    public ArrayList<double[]> getPossibleLTWInsertionsForCustomer(int customer, int earliestStartCustomer, int latestStartCustomer, int serviceTime, int pred, int succ, int endServicePred, int startServiceSucc) {
    	ArrayList<double[]> possibleInsertions = new ArrayList<double[]>();
    	for (int locationIdx=0; locationIdx < this.data.getCustomersToLocations().get(this.data.getOriginalCustomerIds()[customer]).size() ; locationIdx ++) {
    		// Get current location id for customer (e.g. customer 1 with locations: 3, 4 ; locationIdx refers to 0 -> 3, 1 -> 4)
    		int location = this.data.getCustomersToLocations().get(this.data.getOriginalCustomerIds()[customer]).get(locationIdx);
    		
    		// Get travel distances from and to the customer
    		double distToCustomer = data.getDistanceBetweenLocations(DataUtils.getLocationIndex(pred, this), location);
    		double distFromCustomer = data.getDistanceBetweenLocations(location, DataUtils.getLocationIndex(succ, this));
        	// Check earliest and latest insertions to fit predecessor's and successor's service time
            double earliestStartAtInsertion = Math.max(endServicePred + distToCustomer, earliestStartCustomer);
            double latestStartAtInsertion = Math.min(startServiceSucc - distFromCustomer - this.data.getServiceDurations()[customer], latestStartCustomer);
            
            
            // XXX:(if Config.enableGLS) -> check for violations in calculatePenaltyCosts()
            // Check end service time of dependencies to predecessor jobs [customerId, endServiceTime, LocationIdx];
            // if there is no latest predecessor job, the sub-procedure returns an array consisting of -1's ,ie., [-1,-1,-1]
            if (!Config.enableGLS || this.isConstruction) {
	            double[] infoOfLatestPredJob = this.getEndServiceTimeOfLatestPredJob(customer);
	            // if a predecessor job couldn't be scheduled, the current job can also not be scheduled; break
	            if (infoOfLatestPredJob[1] == -1) return possibleInsertions;
	            else if  (infoOfLatestPredJob[1] > 0){
	            	double distToPredecessorJob = data.getDistanceBetweenLocations(DataUtils.getLocationIndex((int) infoOfLatestPredJob[0], this), location);
	            	earliestStartAtInsertion = Math.max(earliestStartAtInsertion, infoOfLatestPredJob[1] + distToPredecessorJob);            	
	            }
            }            
            // check if location is feasible at all w.r.t to service times
            // E.g. : 10 < 10.00004 - 1e-6  ; just prevent numerical instability
            // if (latestStartCustomer < earliestStartAtInsertion - Config.epsilon) break;
            if (latestStartAtInsertion < earliestStartAtInsertion - Config.epsilon) break;
 
			double additionalTravelCosts = distToCustomer + distFromCustomer - data.getDistanceBetweenLocations(DataUtils.getLocationIndex(pred, this), DataUtils.getLocationIndex(succ, this));
            // check available capacity
    		for (int capacity = 0; capacity < this.data.getLocationCapacity()[location]; capacity ++) {
    		    // Key: Location e.g. 1; where start of planning horizon = 0 and end of planning horizon = 230
    		    //        --> HashMap - Key: capacitySlot e.g.:  0  (..., 1, 2, ...) 
    		    //                      Value: ArrayList
    		    // 								(null, 0) _ (167,177) _ (230, null)
    			
    			// start with entryIdx = 1; two entries at initial state (null, earliestStartingPoint) (LatestStartingPoint, null)
    			for (int entryIdx = 1; entryIdx<map.get(location).get(capacity).size(); entryIdx++) {
    				Double[] timePred = map.get(location).get(capacity).get(entryIdx-1);
    				Double[] timeSucc = map.get(location).get(capacity).get(entryIdx);
    				
    				if (timeSucc[0] < endServicePred) 
    					continue; 
    				
    				// TODO Chris - Check conditions
    				// i)   predecessor in location ends before current starting point for job
    				// ii)  predecessor in vehicle's route ends before current starting point for job
    				// iii) successor in location starts after current job is served
    				// iv)  successor in vehicle's route starts after current job is served
    				if (timePred[1] < earliestStartAtInsertion & 
    					endServicePred < earliestStartAtInsertion &
    					timeSucc[0] > earliestStartAtInsertion + serviceTime & 
    					startServiceSucc > earliestStartAtInsertion + serviceTime
    					) {
						
    					double timeStart = earliestStartAtInsertion;
						// TODO_DONE: retrieve multiple solutions (possible that first match not the best one)
						double[] possibleInsertion = new double[]{locationIdx, capacity, timeStart, additionalTravelCosts, entryIdx};
						possibleInsertions.add(possibleInsertion);
    				}
    			}    			
    		}
    	}
    	// no match at all
    	return possibleInsertions;
    }


    /**
     * Retrieve list of possible next insertions for a customer
     * @param customer: identifier of customer whose next insertions are inspected.
     * @return list of possible next insertions
     */
    public ArrayList<double[]> getPossibleInsertionsForCustomer(int customer) {
        ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();
//        boolean triedUnusedVehicle = false;
        
        for (Vehicle vehicle: this.getVehicles()) {
            // generate insertion for unused vehicle only once, otherwise regrets between all unused vehicles will be zero
        	  // NOTE: can't be applied, as skill lvl is important (individually set for each vehicle/therapist)
//            if (!vehicle.isUsed()) {
//                if (triedUnusedVehicle) 
//                	continue;
//                triedUnusedVehicle = true;
//            }
        	
            ArrayList<double[]> insertions = vehicle.getPossibleInsertions(customer, this.data, this);

            if (Config.regretConsiderAllPossibleInsertionPerRoute) { // add all possible position (can be multiple per route)
                possibleInsertionsForCustomer.addAll(insertions);
            } else if (!insertions.isEmpty()){
                // only consider the best possible insertion in this route (as described in Ropke & Pisinger 2007 C&OR §5.2.2 p. 2415)
                insertions.sort(Comparator.comparing(a -> a[4])); // sort by additional costs
                possibleInsertionsForCustomer.add(insertions.get(0));
            }
        }
        return possibleInsertionsForCustomer;
    }

    /**
     * Function is called by the WorstRemoval heuristic. It iterates all vehicles
     * and collects all the possible removals within each vehicle. 
     * After sorting the possible removals, the function returns the sorted list.
     * Entries in the sorted list have the form: [customerID, vehicleId, positionInRoute, travelTimeReduction}]
     * @return sorted list with all possible removals of all cars
     */
    public ArrayList<double[]> getPossibleRemovalsSortedByCostReduction() {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();
        for (Vehicle vehicle: this.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<double[]> removals = vehicle.getPossibleRemovals(this.data, this);
                possibleRemovals.addAll(removals);
            }
        }
        // sort by travelTimeReduction
        possibleRemovals.sort(Comparator.comparing(a -> a[3], Collections.reverseOrder())); 
        return possibleRemovals;
    }

    /**
     * Retrieve possible removals according to the information stored within the neighbor graph.
     * The neighbor graph stores the current best score being observed in any solution so far.
     * @param neighborGraph: scoring between neighbors
     * @return list of possible next removals
     */
    public ArrayList<double[]> getPossibleRemovalsSortedByNeighborGraph(double[][] neighborGraph) {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();
        for (Vehicle vehicle: this.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<double[]> removals = vehicle.getPossibleRemovals(neighborGraph);
                possibleRemovals.addAll(removals);
            }
        }
        possibleRemovals.sort(Comparator.comparing(a -> a[3], Collections.reverseOrder())); // sort by travelTimeReduction
        return possibleRemovals;
    }
    
    /**
     * Retrieve the list of vehicles being used, i.e., vehicles where at least
     * one customer is scheduled.
     * @return list of used vehicles
     */
    public ArrayList<Vehicle> getUsedVehicles() {
        ArrayList<Vehicle> usedVehicles = new ArrayList<>();
        for (Vehicle veh: this.vehicles) {
            if (veh.isUsed()) 
                usedVehicles.add(veh);
        }
        return usedVehicles;
    }
	
    /**
     * Retrieve the number of used vehicles in the current solution.
     * @return number of used vehicles
     */
	private int getNActiveVehicles() {
	  int count = 0;
	  for (Vehicle v: this.vehicles) {
	      if (v.isUsed()) 
	    	  count++;
	  }
	  return count;
	}
	
	/**
	 * Calculates the total costs. Calls subprocedures for calculating 
	 * i) the routing costs of vehicles; ii) swapping costs; and iii) penalty costs  
	 */
	private void calculateTotalCosts() {
        this.calculateCostsFromVehicles();
        this.calculateSwappingCostsForLocations();
        this.calculatePenaltyCosts();
        this.totalCosts = this.vehicleTourCosts + 
        		this.swappingCosts + 
        		this.penaltyUnservedCustomers + 
        		this.penaltyPredJobsViolation +
        		this.penaltySkillViolation + 
        		this.penaltyTimeWindowViolation;
	}

	/**
	 * Computes the current total costs of all vehicles.
	 * Result is stored in the class variable totalCosts.
	 */
    private void calculateCostsFromVehicles() {
        this.vehicleTourCosts = 0.;
        for (Vehicle veh: vehicles)
        	this.vehicleTourCosts += veh.getTourLength();
    }

    /**
     * Aggregates the swapping costs of all vehicles
     */
    private void calculateSwappingCostsForLocations( ) {
    	this.swappingCosts = 0.0;
    	for (Vehicle v: vehicles)
    		swappingCosts += v.getSwappingCosts(this);
    }
    
    
    //
    // UPDATE METHODS
    //
    /**
     * Update the solution's state after removal operation. It adds the 
     * list of removed customers to the list of un-assigned customers and sets
     * the feasibility of the solution to false.
     * @param removedCustomers: list of removed customers
     */
    public void updateSolutionAfterRemoval(List<Integer> removedCustomers) {
    	// TODO Chris - do we need to calculate the costs after removals?
    	this.calculateTotalCosts();
        if (!removedCustomers.isEmpty()) {
            this.notAssignedCustomers.addAll(removedCustomers);
            isFeasible = false;
        }
    }

    /**
     * Update the solution's state after an insertion operation. 
     */
    public void updateSolutionAfterInsertion() {
    	this.addInfeasiblesToNotAssigned();
        this.calculateTotalCosts();
    }

    /**
     * Method calculates the penalty costs. The result is stored in the field
     * variable totalCosts.
     */
    private void calculatePenaltyCosts() {
        this.calcCostsForUnservedCustomers();
        this.calcCostsForTimeWindowViolation();
        this.calcCostsForPredJobsViolation();
        this.calcCostsForSkillViolation();
    }

    /**
     * The function aggregates the penalty costs for unassigned customers.
     */
    private void calcCostsForUnservedCustomers() {
    	// Compute penalty for unscheduled customers
        this.penaltyUnservedCustomers = this.notAssignedCustomers.size() * Config.penaltyUnservedCustomer;
    }
    
    /**
     * Calculate penalty costs for all skill violations.
     * A skill violation occurs if a customer/patient is handled by a vehicle/therapist 
     * whose skill level is below the required one of a customer. 
     */
    private void calcCostsForSkillViolation() {
    	this.penaltySkillViolation = 0;
    	// Iterate customers
    	for (int i = 1; i < this.data.getCustomers().length; i++) {
    		// Get customer id
    		int customerID = this.data.getCustomers()[i];    		
    		// Check whether customer is scheduled; if not -> no skill violation possible
    		if (this.customersAssignedToVehicles[customerID] == -1)
    			continue;
    		// Check if vehicles skill level satisfies the customer's required skill level
    		if (this.data.getRequiredSkillLvl()[customerID] > this.vehicles.get(this.customersAssignedToVehicles[customerID]).getSkillLvl())
    			// Aggregate penalty costs 
    			this.penaltySkillViolation += Config.costSkillLvlViolation;
    	}
    }
    
    /**
     * Calculate penalty costs for all predecessor jobs violation.
     * A predecessor job violation occurs if there is a customer whose predecessor 
     * jobs are not scheduled yet, respectively, which is scheduled in a non-consecutive
     * ordering.
     */
    private void calcCostsForPredJobsViolation() {
    	this.penaltyPredJobsViolation = 0;
    	// Iterate given predecessor jobs and check for violations
    	for (Map.Entry<Integer, ArrayList<Integer>> entry : this.data.getPredCustomers().entrySet()) {
    		// Check for customers if there exist predecessor jobs
    		if (entry.getValue().size() == 0) continue;
    		else {
    			// Iterate predecessor jobs
    			for (Integer predJobOrigIdx : entry.getValue()) {
    				// get customer id in the current scheduling problem (!= original customer ids)
    				int predJobId = Arrays.stream(this.data.getOriginalCustomerIds()).boxed().collect(Collectors.toList()).indexOf(predJobOrigIdx);
    				// check whether the predecessor job is scheduled or not (-1: not scheduled)
    				if (predJobId != -1 && this.customersAssignedToVehicles[predJobId] == -1)
    					// Aggregate penalty costs
    					this.penaltyPredJobsViolation += Config.costPredJobsViolation;
    				//TODO Chris - predecessor job can be scheduled but we also have
    				// to check the service times for possible violations
    				// predecessor job has to be already finished when succeeding job
    				// starts
    			}
    		}
    	}
    }
    
    /**
     * Calculate penalty costs for all time window violations. 
     * A time window violation occurs if the customer's service time window
     * is not in the range in which a customer has to be handled according to the
     * input information. The maximal violation of the service times are predefined
     * by the max_timeWindow_violation parameter in the configuration file (default: 10 time units)
     */
    private void calcCostsForTimeWindowViolation() {
    	this.penaltyTimeWindowViolation = 0;
    	// Iterate customers and check for time window violations
    	for (int i = 0; i<this.data.getCustomers().length; i++) {
    		// Get customer id
    		int customerID = this.data.getCustomers()[i];
    		
    		// Check whether customer is scheduled; if not -> no time window violation possible
    		if (this.customersAssignedToVehicles[customerID] == -1)
    			continue;

    		// get vehicle where current customer is scheduled
    		Vehicle vehicleOfInterest = this.vehicles.get(this.customersAssignedToVehicles[customerID]);
    		// Get customer's index within the route
    		int idxInRoute = vehicleOfInterest.getCustomers().indexOf(customerID);
    		// check for TW violation
    		// i) the current scheduling is earlier compared to the given earliest starting point for the customer
    		// ii) the current scheduling is later compared to the given latest ending points for the customer
    		if (this.data.getEarliestStartTimes()[customerID] > vehicleOfInterest.getStartOfServices().get(idxInRoute) 
    				|| this.data.getLatestStartTimes()[customerID] + this.data.getServiceDurations()[customerID] < vehicleOfInterest.getEndOfServices().get(idxInRoute))
    			// Aggregate penalty for time window violations
    			this.penaltyTimeWindowViolation += Config.costTimeWindowViolation;
    	}
    }

    /**
     * Add a customer to the list of unassigned customers.
     * @param customer: id of customer
     */
    public void addCustomerToNotAssignedCustomers(int customer) {
        this.notAssignedCustomers.add(customer);
    }
    
    /**
     * The function adds infeasible customers to unassigned customers.
     */
    private void addInfeasiblesToNotAssigned() {
        this.notAssignedCustomers.addAll(this.tempInfeasibleCustomers);
        this.tempInfeasibleCustomers.clear();
        this.isFeasible = this.notAssignedCustomers.isEmpty();
    }
    
    //
    // CUSTOM GET FNCS
    //   
    /**
     * Get the end service time of the latest predecessor jobs. 
     * If there is not predecessor job, the end service time of predecessor jobs is 
     * set to 0, hence, it is possible to schedule the customer from the very beginning.
     * If a predecessor job couldn't be scheduled, then the current job can also not 
     * be scheduled and a '-1' is returned. 
     * @param customerId: id of customer which has to be scheduled next
     * @return Get the end service time of latest predecessor job 
     */
    public double[] getEndServiceTimeOfLatestPredJob (int customerId) {
    	double endServiceTimeOfLatestPredJob = 0;
    	int locationIdOfLatestPredJob = 0; 
    	int customerIdOfLatestPredJob = 0;
    	ArrayList<Integer> predIds = this.data.getPredCustomers().get(this.data.getOriginalCustomerIds()[customerId]);
    	for (int originalPredCustomerId : predIds) {
    		int predCustomerId = Arrays.stream(this.data.getOriginalCustomerIds()).boxed().collect(Collectors.toList()).indexOf(originalPredCustomerId);
    		// if predCustomerID cannot be found, i.e., == -1 ; the predecessor job is handled in the morning shift
    		
    		// if there is a predecessor job which couldn't be scheduled, the current customer
    		// can also not be scheduled and a -1 is returned
    		if (predCustomerId == -1 ||  this.customersAssignedToVehicles[predCustomerId] == -1) {
    			endServiceTimeOfLatestPredJob = -1;    			
    			return new double[] {-1, -1,-1};    			
    		}
    		// check the end service time
    		Vehicle v = this.vehicles.get(this.customersAssignedToVehicles[predCustomerId]);
			int idx = v.getCustomers().indexOf(predCustomerId);
			if (endServiceTimeOfLatestPredJob < v.getEndOfServices().get(idx)) {
				endServiceTimeOfLatestPredJob = v.getEndOfServices().get(idx);
				customerIdOfLatestPredJob = predCustomerId;
				locationIdOfLatestPredJob = this.customersAssignedLocations[predCustomerId];
			}
    	}
    	return new double[] {customerIdOfLatestPredJob, endServiceTimeOfLatestPredJob, locationIdOfLatestPredJob};
    }
    
    /**
     * Returns whether the predecessor jobs of the attached customer are already scheduled.
     * Returns false if there is at least one job which is not scheduled yet,
     * otherwise true is returned.
     * @param customerId customer id whose predecessor jobs are checked
     * @return false if one predecessor job is not scheduled, otherwise true
     */
    public boolean checkSchedulingOfPredecessors (int customerId) {
    	// Get predecessor job of attached customer
    	ArrayList<Integer> predIds = this.data.getPredCustomers().get(this.data.getOriginalCustomerIds()[customerId]);
    	for (int originalPredCustomerId: predIds) {
    		// Get the predecessor id being handled in the current run
    		int predCustomerId = Arrays.stream(this.data.getOriginalCustomerIds()).boxed().collect(Collectors.toList()).indexOf(originalPredCustomerId);
    		// check whether the predecessor job in list of infeasible customers
    		int existenceOfPredecessor = this.tempInfeasibleCustomers.indexOf(predCustomerId);
    		// check infeasibility AND if the customer is not assigned to any vehicle 
    		if (existenceOfPredecessor == -1)
    			return false;
    		if (this.customersAssignedToVehicles[predCustomerId] == -1)
    			return false;
    	}
    	// All predecessor jobs are scheduled -> Return true
    	return true;
    }
    
    /**
     * Retrieve customers being assigned to any vehicle
     * @return list of scheduled customers
     */
    public ArrayList<Integer> getAssignedCustomers() {
    	ArrayList<Integer> list = new ArrayList<Integer>();
    	for (int i=1; i<this.customersAssignedToVehicles.length; i++) {
    		if (this.customersAssignedToVehicles[i]!=-1)
    			list.add(i);
    	}
    	return list;
    }

    
    //
    // GETTERS
    //
    /**
     * Retrieve the number of assigned customers.
     * @return number of assigned customers
     */
    public int getNrOfAssignedCustomers() {
        return this.data.getnCustomers() - this.notAssignedCustomers.size();
    }

    /**
     * Get list of infeasible customers.
     * @return list of infeasible customers
     */
    public ArrayList<Integer> getTempInfeasibleCustomers() {
        return tempInfeasibleCustomers;
    }

    /**
     * Get whether solution is feasible.
     * @return is solution feasible
     */
    public boolean isFeasible() {
        return isFeasible;
    }
    
    /**
     * Returns whether the current solution is from the construction phase.
     * @return is solution from the initial construction step
     */
    public boolean isConstruction(){
    	return isConstruction;
    }
    
    /**
     * Get list of unassigned customers.
     * @return list of unassigned customers
     */
    public ArrayList<Integer> getNotAssignedCustomers() {
        return notAssignedCustomers;
    }

    /**
     * Get the total cost of the solution.
     * @return total cost
     */
    public double getTotalCosts() {
        return totalCosts;
    }

    /**
     * Get list of vehicles.
     * @return list of vehicles
     */
    public ArrayList<Vehicle> getVehicles() {
        return vehicles; // TODO Alex - how to handle escaping reference ?
    }
    
    /**
     * Get array storing the customers' affiliation to theirs possible locations.
     * @return customers' affiliation to locations
     */
    public int[] getCustomerAffiliationToLocations() {
    	return customersAssignedLocations;
    }
    
    /**
     * Get array storing the customers' affiliation to the capacity slots of 
     * the locations.
     * @return Customers' affiliation to capacity slots
     */
    public int[] getCustomerAffiliationToCapacity() {
    	return customersAssignedCapacitySlot;
    }
    
    /**
     * Get array storing assignment of customers to vehicles
     * @return: array customers to vehicles
     */
    public int[] getCustomersAssignedToVehicles() {
    	return customersAssignedToVehicles;
    }
    
    /**
     * Retrieve customer to locations mapping. The keys are the customer ids,
     * and the values are the list of possible locations per customer.
     * @return HashMap Customer to location list
     */
    public HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>> getMap() {
    	return map;
    }
    
    /**
     * Retrieve the data object.
     * @return Data Object
     */
    public Data getData() {
    	return data;
    }
    
    /**
     * Get a mapping containing information about tuples which have already been
     * explored in the backtracking tree. The key values are the customer ids. 
     * The values stores double arrays with information about the insertions. 
     * @return mapping of already explored insertions
     */
    public HashMap<Integer, ArrayList<double[]>> getTriedInsertions() {
    	return triedInsertions;
    }
    

    //
    // CUSTOM SETTER FUNCS
    //
    /**
     * Sets the customer's affiliated location.
     * @param customer: customer's id
     * @param locationAffiliation: customer's location affiliation
     */
    public void setCustomerAffiliationToLocation (int customer, int locationAffiliation) {
    	this.customersAssignedLocations[customer] = locationAffiliation;
    }
    
    /**
     * Sets the customer's capacity slot.
     * @param customer: customer's id
     * @param capacity: capacity slot
     */
    public void setCustomerAssignmentToCapacitySlot (int customer, int capacity) {
    	this.customersAssignedCapacitySlot[customer] = capacity;
    }
    
    /**
     * Sets customer's assignment to vehicles
     * @param customer: customer id
     * @param vehicleId: vehicle id 
     */
    public void setCustomerAssignmentToVehicles (int customer, int vehicleId) {
    	this.customersAssignedToVehicles[customer] = vehicleId;
    }
    
    /**
     * Sets the occupancy of a capacity slot within a location w.r.t 
     * the attached service interval.
     * @param location: location to be occupied
     * @param capacity: capacity slot
     * @param startServiceTime: start of service
     * @param endServiceTime: end of service
     */
    public void setLocationOccupancy (int location, int capacitySlot, double startServiceTime, double endServiceTime, int entryIndex) {
    	map.get(location).get(capacitySlot).add(entryIndex, new Double[]{startServiceTime, endServiceTime});
    }
    
    
    //
    // SETTERS
    //
    /**
     * Set list of unassigned customers.
     * @param notAssignedCustomers: list of unassigned customers
     */
    public void setNotAssignedCustomers(ArrayList<Integer> notAssignedCustomers) {
        this.notAssignedCustomers = notAssignedCustomers;
    }

    /**
     * Set the total cost of the solution.
     * @param totalCosts: total cost
     */
    public void setTotalCosts(double totalCosts) {
        this.totalCosts = totalCosts;
    }

    /**
     * Set vehicle list.
     * @param vehicles: list of vehicles
     */
    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    /**
     * Set whether solution is feasible.
     * @param feasible: boolean indicating the feasibility of solution
     */
    public void setFeasible(boolean feasible) {
        isFeasible = feasible;
    }
    
    /**
     * Set whether the current solution is in the construction phase
     * @param isConstruction: boolean indicating if the solution is in construction phase.
     */
    public void setIsConstruction(boolean isConstruction) {
    	this.isConstruction = isConstruction;
    }
    
    /**
     * Set list of temporary infeasible customers.
     * @param tempInfeasibleCustomers: list of temporary infeasible customers
     */
    public void setTempInfeasibleCustomers(ArrayList<Integer> tempInfeasibleCustomers) {
        this.tempInfeasibleCustomers = tempInfeasibleCustomers;
    }


    //
    // DEALLOCATION of customer's info
    //
    /**
     * Deallocates a customer's affiliation to a location.
     * @param customer: customer's id
     */
    public void freeCustomerAffiliationToLocation(int customer) {
    	this.customersAssignedLocations[customer] = -1;
    }
    
    /**
     * Deallocates a customer's assignment to a capacity slot.
     * @param customer: customer's id
     */
    public void freeCustomerAffiliationToCapacity(int customer) {
    	this.customersAssignedCapacitySlot[customer] = -1;
    }
    
    /**
     * Deallocates a customer' assignment to a vehicle/therapist
     * @param customer: customer's id 
     */
    public void freeCustomerAffiliationToVehicle(int customer) {
    	this.customersAssignedToVehicles[customer] = -1;
    }
    
    /**
     * Deallocates a location's capacity slot within a certain time interval
     * being attached as parameters.
     * @param location: location id
     * @param capacitySlot: capacity slot
     * @param startServiceTime: start of service time
     * @param endServiceTime: end of service time
     */
    public void freeLocationOccupancy(int location, int capacitySlot, double startServiceTime, double endServiceTime) {
    	for (int i = 1 ; i<map.get(location).get(capacitySlot).size()-1; i++) {
    		Double[] arr = map.get(location).get(capacitySlot).get(i);
    		if (arr[0] == startServiceTime && arr[1] == endServiceTime) {
    			map.get(location).get(capacitySlot).remove(i);
    			return;
    		}
    	}
    }
    

    //
    // COPY
    //
    /**
     * Creates a copy of the current solution object.
     * @return copy of the current solution
     */
    public Solution copyDeep() {
        Solution sol = new Solution(this.data);
        sol.setNotAssignedCustomers(new ArrayList<>(this.notAssignedCustomers));
        sol.setTempInfeasibleCustomers(new ArrayList<>(this.tempInfeasibleCustomers));
        sol.setTotalCosts(this.totalCosts);
        sol.setFeasible(this.isFeasible);
        sol.penaltyUnservedCustomers = this.penaltyUnservedCustomers;
        sol.penaltyPredJobsViolation = this.penaltyPredJobsViolation;
        sol.penaltySkillViolation = this.penaltySkillViolation;
        sol.penaltyTimeWindowViolation = this.penaltyTimeWindowViolation;
        sol.swappingCosts = this.swappingCosts;
        sol.vehicleTourCosts = this.vehicleTourCosts;
        sol.isConstruction = this.isConstruction;

        ArrayList<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle veh: this.vehicles) {
            newVehicles.add(veh.copyDeep());
        }
        sol.setVehicles(newVehicles);

        sol.customersAssignedCapacitySlot = Arrays.copyOf(this.customersAssignedCapacitySlot, this.customersAssignedCapacitySlot.length);
        sol.customersAssignedLocations = Arrays.copyOf(this.customersAssignedLocations, this.customersAssignedLocations.length);
        sol.customersAssignedToVehicles = Arrays.copyOf(this.customersAssignedToVehicles,  this.customersAssignedToVehicles.length);
        
        sol.map = new HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>>();
        for (Map.Entry<Integer, HashMap<Integer, ArrayList<Double[]>>> entry: this.map.entrySet()) {
        	for (Map.Entry<Integer, ArrayList<Double[]>> innerEntry : this.map.get(entry.getKey()).entrySet()) {
        		if (sol.map.get(entry.getKey()) == null)
        			sol.map.put(entry.getKey(), new HashMap<Integer, ArrayList<Double[]>>());
        		sol.map.get(entry.getKey()).put(innerEntry.getKey(), new ArrayList<Double[]>(this.map.get(entry.getKey()).get(innerEntry.getKey())));
        	}
        }
        
        // Copy from triedInsertions -> solution is in a new state 
        sol.triedInsertions = new HashMap<Integer, ArrayList<double[]>>();
        for  (Map.Entry<Integer, ArrayList<double[]>> entry: this.triedInsertions.entrySet()) {
    		sol.triedInsertions.put(entry.getKey(), new ArrayList<double[]>(entry.getValue()));
        }
 
        return sol;
    }


    //
    // EQUALITY & HASHING
    //
    /**
     * Checks equality between two Solution objects.
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return Double.compare(solution.totalCosts, totalCosts) == 0 &&
                isFeasible == solution.isFeasible &&
                Objects.equal(notAssignedCustomers, solution.notAssignedCustomers) &&
                Objects.equal(tempInfeasibleCustomers, solution.tempInfeasibleCustomers) &&
                Objects.equal(vehicles, solution.vehicles);
    }
    
    /**
     * Compute a hashCode for the solution object. The hashCode of a solution object
     * is dependent on the ordering of the scheduled customers within the vehicles.
     * Hence, two solution objects have the same hashCode if the routes are the same
     * for two solution objects.
     * @return hashCode for solution object
     */
    public int hashCode_tmp() {
    	// TODO Chris - when flexible locations come into play; check whether the vehiclesInfo 
    	// still contains the relevant information or do we have to encode the locations, too.
    	// E.G. customer A(1)-B(1) with their preferential locations (1). 
    	// -> delete B(1) from series
    	// -> add B(2) 
    	// -> resulting series: A(1) - B(2) 
    	ArrayList<ArrayList<Integer>> vehiclesInfo = new ArrayList<ArrayList<Integer>>();
    	for (Vehicle v: this.vehicles) {
    		vehiclesInfo.add(v.getCustomers());
    	}
    	vehiclesInfo.sort(new Comparator<ArrayList<Integer>>() {

			@Override
			public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
				if  (o1.size() < o2.size()) return -1;
				else if (o1.size() > o2.size()) return 1;
				else {
					for (int customerFirst = 0 ; customerFirst<o1.size(); customerFirst++) {
						if (o1.get(customerFirst) < o2.get(customerFirst)) {
							return -1;
						}
						else if (o1.get(customerFirst) > o2.get(customerFirst)) {
							return 1;
						}
						// TODO_DONE Chris - check for first customer should be sufficient
						// equality not possible; customer only assigned once
					}
				}
				return 0;
			}
    	}
    	);
    	
    	return vehiclesInfo.hashCode();
    }


    //
    // PRINTING
    //
    /**
     * Prints the current solution to the standard output.
     */
    public void printSolution() {
        int nActiveVehicles = this.getNActiveVehicles();
        System.out.println("VehicleCosts:" + this.vehicleTourCosts + " - SwappingCosts:" + this.swappingCosts + " - penUnserved: " + this.penaltyUnservedCustomers + " - penSkillLvl:" + this.penaltySkillViolation + " - penTW:" + this.penaltyTimeWindowViolation + " - penPred:" + this.penaltyPredJobsViolation);
        // TODO Alex - logger debug!
        System.out.println("Solution total costs: " + this.totalCosts + "\tn vehicles used: " + nActiveVehicles); 
        for (Vehicle veh: this.vehicles) {
            if (veh.isUsed()) {
                veh.printTour(this);
            }
        }
    }
    
    
    //
    // DEPRECATED
    //
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(notAssignedCustomers, tempInfeasibleCustomers, totalCosts, vehicles, isFeasible);
    }

    //public Solution(ArrayList<Vehicle> vehicles, ArrayList<Integer> notAssignedCustomers) {
    	// TODO Alex - muss hier auch infeasible rein?
    	// this.vehicles = vehicles;
    	//  this.calculateCostsFromVehicles();
    	//  this.notAssignedCustomers = notAssignedCustomers;
    	//
    	//  if (notAssignedCustomers.isEmpty()) { // TODO Alex - brauchen wir einen Test dafür (?) --> eher wenn Lösung in ALNS bearbeitet wurde, ob dann noch alles passt
    	//      this.isFeasible = true;
    	//  }
    //}
}
