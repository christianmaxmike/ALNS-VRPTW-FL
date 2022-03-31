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

/**
 * Class implementing the solution object.
 * A solution object carries the information about unassigned customers,
 * temporary infeasible customers, the total costs, a list of all vehicles,
 * the customers affiliation to their current location and capacity slots,
 * as well as the occupancy of capacity slots of all locations for the 
 * whole range of possible service times. We also have an indicator if 
 * the solution object leads to a feasible solution or not.
 */
public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private ArrayList<Integer> tempInfeasibleCustomers; // needed to store customer that cannot be assigned to any route
    private ArrayList<Vehicle> vehicles;
    private Data data;
    private double totalCosts;
    private boolean isFeasible = false;
    private HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>> map; // location -> capacitySlot -> list of service time tuples [start, end]
    private int[] customersAssignedLocations;     // length: customer size + 1 (depot)
    private int[] customersAssignedCapacitySlot;  // length: customer size + 1 (depot)

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
		this.isFeasible = solutionTemp.isFeasible();
		this.customersAssignedCapacitySlot = Arrays.copyOf(solutionTemp.getCustomerAffiliationToCapacity(), solutionTemp.getCustomerAffiliationToCapacity().length);
		this.customersAssignedLocations = Arrays.copyOf(solutionTemp.getCustomerAffiliationToLocations(), solutionTemp.getCustomerAffiliationToLocations().length);
		
		ArrayList<Vehicle> newVehicles = new ArrayList<>();
		for (Vehicle veh: solutionTemp.getVehicles()) {
			newVehicles.add(veh.copyDeep());
		}
		this.setVehicles(newVehicles);
	}
	
	/**
	 * This function yields an empty solution, i.e, it initializes vehicle
	 * objects and set the field variables to its default values
	 * @param data: Data object
	 * @return: Empty solution object
	 */
    public static Solution getEmptySolution(Data data) {
        Solution start = new Solution(data);
        start.setVehicles(data.initializeVehicles());
        // initially add all customers to list of not assigned customers
        start.setNotAssignedCustomers(new ArrayList<>() {{ for (int i : data.getCustomers()) add(i); }});
        start.setTempInfeasibleCustomers(new ArrayList<>());
        start.setFeasible(false);
        
        // Create array indicating to which coords a customer is assigned to (-1: no assignment)
        start.customersAssignedLocations = new int[data.getCustomers().length+1];
        Arrays.fill(start.customersAssignedLocations, -1);
        start.customersAssignedLocations[0] = 0;  // <- Depot
        
        start.customersAssignedCapacitySlot = new int[data.getCustomers().length+1];
        Arrays.fill(start.customersAssignedCapacitySlot, -1);
        start.customersAssignedCapacitySlot[0] = 0; // -> Depot
        
        start.map = new HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>>();
        //for (int loc = 0; loc<data.getCustomersToLocations().size(); loc++) {
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
    public ArrayList<double[]> getAvailableLTWForCustomer(int customer, int earliestStartCustomer, int latestStartCustomer, int serviceTime, int pred, int succ, int endServicePred, int startServiceSucc) {
    	ArrayList<double[]> possibleInsertions = new ArrayList<double[]>();
    	for (int locationIdx=0; locationIdx < this.data.getCustomersToLocations().get(this.data.getOriginalCustomerIds()[customer]).size() ; locationIdx ++) {
    		int location = this.data.getCustomersToLocations().get(this.data.getOriginalCustomerIds()[customer]).get(locationIdx);
    		
    		// Get travel distances from and to the customer
    		double distToCustomer = getDistanceBetweenLocations(DataUtils.getLocationIndex(pred, this), location);
    		double distFromCustomer = getDistanceBetweenLocations(location, DataUtils.getLocationIndex(succ, this));
    		//double distFromCustomer = getDistance(succ, location);
        	// Check earliest and latest insertions to fit predecessor's and successor's service time
            double earliestStartAtInsertion = Math.max(endServicePred + distToCustomer, earliestStartCustomer);
            double latestStartAtInsertion = Math.min(startServiceSucc - distFromCustomer - this.data.getServiceDurations()[customer], latestStartCustomer);
            
            // check if location is feasible at all w.r.t to service times
            // E.g. : 10 < 10.00004 - 1e-6  ; just prevent numerical instability
            // if (latestStartCustomer < earliestStartAtInsertion - Config.epsilon) break;
            if (latestStartAtInsertion < earliestStartAtInsertion - Config.epsilon) break;
 
//			double additionalTravelCosts = distToCustomer + distFromCustomer - getDistanceBetweenCustomersByAffiliations(pred, succ);
			double additionalTravelCosts = distToCustomer + distFromCustomer - getDistanceBetweenLocations(DataUtils.getLocationIndex(pred, this), DataUtils.getLocationIndex(succ, this));
            // check available capacity
			// int loc = DataUtils.getPreferredLocationIndex(customer, location, this.data);
    		for (int capacity = 0; capacity < this.data.getLocationCapacity()[location]; capacity ++) {
    		    // Key: Location e.g. 1
    		    //        --> HashMap - Key: capacitySlot e.g.:  0  (..., 1, 2, ...) 
    		    //                      Value: ArrayList
    		    // 								(null, 0) _ (167,177) _ (230, null)
    			
    			// start with entryIdx = 1; two entries at initial state (null, earliestStartingPoint) (LatestStartingPoint, null)
    			for (int entryIdx = 1; entryIdx<map.get(location).get(capacity).size(); entryIdx++) {
    				Double[] timePred = map.get(location).get(capacity).get(entryIdx-1);
    				Double[] timeSucc = map.get(location).get(capacity).get(entryIdx);
    				
    				if (timeSucc[0] < endServicePred) 
    					continue; 
    				
    				if (timePred[1] < earliestStartAtInsertion & 
    					timeSucc[0] > latestStartAtInsertion + serviceTime & 
    					earliestStartAtInsertion + serviceTime < startServiceSucc & 
    					endServicePred < earliestStartAtInsertion) {
						
    					double timeStart = earliestStartAtInsertion;
						// TODO: retrieve multiple solutions (possible that first match not the best one)
						double[] possibleInsertion = new double[]{locationIdx, capacity, timeStart, additionalTravelCosts, entryIdx};
						possibleInsertions.add(possibleInsertion);
    				}
    			}    			
    		}
    	}
    	// no match at all
    	return possibleInsertions;
    }


    public ArrayList<double[]> getPossibleInsertionsForCustomer(int customer) {
        ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();
//        boolean triedUnusedVehicle = false;
        
        for (Vehicle vehicle: this.getVehicles()) {
            // generate insertion for unused vehicle only once, otherwise regrets between all unused vehicles will be zero
        	  //TODO: can't be applied, as skill lvl is important (individually set for each vehicle/therapist)
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
	 * Computes the current total costs of all vehicles.
	 * Result is stored in the class variable totalCosts.
	 */
    private void calculateCostsFromVehicles() {
        this.totalCosts = 0.;
        for (Vehicle veh: vehicles)
            this.totalCosts += veh.getTourLength();
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
        this.calculateCostsFromVehicles();
        if (!removedCustomers.isEmpty()) {
            this.notAssignedCustomers.addAll(removedCustomers);
            isFeasible = false;
        }
        //  this.calculatePenaltyCosts(); // TODO Alex: kann ggf raus, da penalties er nach Insertion berechnet werden muessen
    }

    /**
     * Update the solution's state after an insertion operation. 
     */
    public void updateSolutionAfterInsertion() {
        this.calculateCostsFromVehicles();
        this.addInfeasiblesToNotAssigned();
        this.calculatePenaltyCosts();
    }

    /**
     * Method calculates the penalty costs. The result is stored in the field
     * variable totalCosts.
     */
    private void calculatePenaltyCosts() {
        this.addCostsForUnservedCustomers();
    }

    /**
     * The function aggregates the penalty costs for unassigned customers.
     */
    private void addCostsForUnservedCustomers() {
        this.totalCosts += this.notAssignedCustomers.size() * Config.penaltyUnservedCustomer;
    }

    /**
     * Add a customer to the list of unassigned customers.
     * @param customer
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
     * Retrieve the distance between two locations.
     * @param location1: id of first location
     * @param location2: id of second location
     * @return distance between two locations
     */
    public double getDistanceBetweenLocations (int location1, int location2) {
    	return this.data.getDistanceMatrix()[location1][location2];
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
        return vehicles; // TODO how to handle escaping reference ?
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
    public void setCustomerAffiliationToCapacity (int customer, int capacity) {
    	this.customersAssignedCapacitySlot[customer] = capacity;
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
     * Deallocates a customer's affiliation to a capacity slot.
     * @param customer: customer's id
     */
    public void freeCustomerAffiliationToCapacity(int customer) {
    	this.customersAssignedCapacitySlot[customer] = -1;
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

        ArrayList<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle veh: this.vehicles) {
            newVehicles.add(veh.copyDeep());
        }
        sol.setVehicles(newVehicles);

        sol.customersAssignedCapacitySlot = Arrays.copyOf(this.customersAssignedCapacitySlot, this.customersAssignedCapacitySlot.length);
        sol.customersAssignedLocations = Arrays.copyOf(this.customersAssignedLocations, this.customersAssignedLocations.length);
        
        sol.map = new HashMap<Integer, HashMap<Integer, ArrayList<Double[]>>>();
        for (Map.Entry<Integer, HashMap<Integer, ArrayList<Double[]>>> entry: this.map.entrySet()) {
        	for (Map.Entry<Integer, ArrayList<Double[]>> innerEntry : this.map.get(entry.getKey()).entrySet()) {
        		if (sol.map.get(entry.getKey()) == null)
        			sol.map.put(entry.getKey(), new HashMap<Integer, ArrayList<Double[]>>());
        		sol.map.get(entry.getKey()).put(innerEntry.getKey(), new ArrayList<Double[]>(this.map.get(entry.getKey()).get(innerEntry.getKey())));
        	}
        }
        return sol;
    }


    //
    // EQUALITY & HASHING
    //
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
    
    public int hashCode_tmp() {
    	// TODO: when Flexible locations come into play; check whether the vehiclesInfo 
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
						else {
							return 1;
						}
						// TODO: check for first customer should be sufficient
						// equality not possible; customer only assigned once
					}
				}
				return 0;
			}
    	}
    	);
    	
    	return vehiclesInfo.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(notAssignedCustomers, tempInfeasibleCustomers, totalCosts, vehicles, isFeasible);
    }

    //
    // PRINTING
    //
    /**
     * Prints the current solution to the standard output.
     */
    public void printSolution() {
        int nActiveVehicles = this.getNActiveVehicles();
        System.out.println("Solution total costs: " + this.totalCosts + "\tn vehicles used: " + nActiveVehicles); // TODO logger debug!
        for (Vehicle veh: this.vehicles) {
            if (veh.isUsed()) {
                veh.printTour(this);
            }
        }
    }
    
    
    //
    // DEPRECATED
    //
    //public Solution(ArrayList<Vehicle> vehicles, ArrayList<Integer> notAssignedCustomers) {
    	// TODO muss hier auch infeasible rein?
    	// this.vehicles = vehicles;
    	//  this.calculateCostsFromVehicles();
    	//  this.notAssignedCustomers = notAssignedCustomers;
    	//
    	//  if (notAssignedCustomers.isEmpty()) { // TODO brauchen wir einen Test dafür (?) --> eher wenn Lösung in ALNS bearbeitet wurde, ob dann noch alles passt
    	//      this.isFeasible = true;
    	//  }
    //}
}
