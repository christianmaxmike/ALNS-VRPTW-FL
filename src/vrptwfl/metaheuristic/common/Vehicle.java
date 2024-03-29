package vrptwfl.metaheuristic.common;

import com.google.common.base.Objects;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class implements a vehicle/therapist object
 * 
 * @author: Christian M.M. Frey, Alexander Jungwirth
 */
public class Vehicle {
	
    // TODO Alex: Methode für cost increase und reduction (tour laenge)

    private int id;
    private int capacityLimit;
    private int capacityUsed;
    private double tourLength;
    private int skillLvl;
    private ArrayList<Integer> customers;
    private ArrayList<Double> startOfServices;
    private ArrayList<Double> endOfServices;
    private boolean isUsed;
    private int nCustomersInTour;
    
    private boolean isAvailable;
    
    /**
     * Empty constructor
     */
    public Vehicle() {}

    /**
     * Constructor for a vehicle object. 
     * Initializes all class variables to their default values
     * @param id: vehicle id
     * @param capacityLimit: capacity limit
     * @param latestEndOfService: the latest end time of any service to be scheduled
     * @param skillLvl: skill level of vehicle
     */
    public Vehicle(int id, int capacityLimit, double earliestStartOfService, double latestEndOfService, int skillLvl) {
        this.id = id;
        this.capacityLimit = capacityLimit;
        this.capacityUsed = 0;
        this.tourLength = 0.;

        // create empty route only dummy node for start and end of tour
        this.customers = new ArrayList<>();
        this.customers.add(0);  // Start Depot
        this.customers.add(0);  // End Depot
        this.nCustomersInTour = 0;
        this.startOfServices = new ArrayList<>();
        this.startOfServices.add(earliestStartOfService);
        this.startOfServices.add(latestEndOfService);
        this.endOfServices = new ArrayList<>();
        this.endOfServices.add(earliestStartOfService);
        this.endOfServices.add(latestEndOfService);
        this.isUsed = false;
        this.skillLvl = skillLvl;
        
        this.isAvailable = true;
    }
    
    /**
     * Copies the vehicle object
     * @return Copy of vehicle object
     */
    public Vehicle copyDeep() {
        Vehicle v = new Vehicle();
        v.setId(this.id);
        v.setCapacityLimit(this.capacityLimit);
        v.setCapacityUsed(this.capacityUsed);
        v.setTourLength(this.tourLength);
        v.setCustomers(new ArrayList<>(this.customers));
        v.setStartOfServices(new ArrayList<>(this.startOfServices));
        v.setEndOfServices(new ArrayList<>(this.endOfServices));
        v.setnCustomersInTour(this.nCustomersInTour);
        v.setSkillLvl(this.skillLvl);
        v.setUsed(this.isUsed);
        v.setAvailable(this.isAvailable);
        return v;
    }
    
    /**
     * Retrieve the possible insertions of a customer within the vehicle's route
     * @param customer: customer to be inserted
     * @param data: Data object
     * @param solution: Solution object where customer shall be inserted
     * @return list with all possible insertions
     */
    public ArrayList<double[]> getPossibleInsertions(int customer, Data data, Solution solution) {
        ArrayList<double[]> possibleInsertions = new ArrayList<>();

        // XXX : GLS - SKILL Violation
        // SKILL CHECK - insufficient skill level
        if (!(Config.getInstance().enableGLS || Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature ) || solution.isConstruction()) {
        	if (this.skillLvl < data.getRequiredSkillLvl()[customer])
        		return possibleInsertions;        	
        }
        
        // CAPACITY CHECK - if capacity limit is reached, the customer can't be inserted
        if (this.capacityUsed + data.getDemands()[customer] > this.capacityLimit) 
        	return possibleInsertions;

        // Get service interval for customer
        double earliestStartCustomer = data.getEarliestStartTimes()[customer];
        double latestStartCustomer = data.getLatestStartTimes()[customer];
        
//        if ((Config.getInstance().enableGLS||Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) & !solution.isConstruction()) {
//        	earliestStartCustomer = Math.max(earliestStartCustomer - Config.getInstance().maxTimeWindowViolation, 0);
//        	latestStartCustomer = Math.min(latestStartCustomer + Config.getInstance().maxTimeWindowViolation,  solution.getData().getEndOfPlanningHorizon());
//        }

        for (int i = 0; i < this.customers.size() - 1; i++ ) {
        	// Get predecessor end and successor start time
            int pred = this.customers.get(i);
            int succ = this.customers.get(i+1);
            double endServicePred = this.endOfServices.get(i);
            double startServiceSucc = this.startOfServices.get(i+1);
            
            ArrayList<double[]> customersPossibleLTW = new ArrayList<double[]>();
			// Get possible insertions - return array in format 
            // [location, capacitySlot, startTimeService, costs, entryIdxInLoc]
                		
            // check w/o violations
            customersPossibleLTW = solution.getPossibleLTWInsertionsForCustomer(customer, earliestStartCustomer, latestStartCustomer, data.getServiceDurations()[customer], pred, succ, endServicePred, startServiceSucc);

    		// check w/ left-sided % right-sided time violations
	        if ((Config.getInstance().enableGLS||Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) && !solution.isConstruction()) {
	            if (customersPossibleLTW.size() == 0) {
    	        	double corruptedEarliestStartCustomer = Math.max(earliestStartCustomer - Config.getInstance().maxTimeWindowViolation, 0);
    	            customersPossibleLTW = solution.getPossibleLTWInsertionsForCustomer(customer, corruptedEarliestStartCustomer, latestStartCustomer, data.getServiceDurations()[customer], pred, succ, endServicePred, startServiceSucc);
	    	    }
	            if (customersPossibleLTW.size() == 0) {
    	        	double corruptedLatestStartCustomer = Math.min(latestStartCustomer + Config.getInstance().maxTimeWindowViolation,  solution.getData().getEndOfPlanningHorizon());
    	            customersPossibleLTW = solution.getPossibleLTWInsertionsForCustomer(customer, earliestStartCustomer, corruptedLatestStartCustomer, data.getServiceDurations()[customer], pred, succ, endServicePred, startServiceSucc);
    	        }
	        }
    		    		
            if (customersPossibleLTW.size() == 0) {
            	// no insertion could be found
            	continue;
            }
            else  // add insertions
            	for (int newEntry = 0 ; newEntry<customersPossibleLTW.size(); newEntry++) {
            		// customer, vehicleId, posInRoute, starTime, costs, location, capacity, entryIdxInLoc
            		double[] newInsertion = new double[] {customer, 
            				this.id, 
            				i+1, 
            				customersPossibleLTW.get(newEntry)[2], 
            				customersPossibleLTW.get(newEntry)[3], 
            				customersPossibleLTW.get(newEntry)[0], 
            				customersPossibleLTW.get(newEntry)[1], 
            				customersPossibleLTW.get(newEntry)[4],
            				0.0};

            		if (Config.getInstance().enableGLS || Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) {
            			double penaltyCosts = solution.getViolationCostsForInsertion(newInsertion, false);
            			newInsertion[8] += penaltyCosts;
            		}

            		possibleInsertions.add(newInsertion);
            	}
        }
        return possibleInsertions;
    }
    
    /**
     * Retrieve the possible removals of customers within the vehicle's route
     * @param data: Data object
     * @param solution: Solution object where customer shall be removed
     * @return list with all possible removals
     */
    public ArrayList<double[]> getPossibleRemovals(Data data, Solution solution) {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();
        if (!this.isUsed) 
        	return possibleRemovals;

        // init values
        int pred;
        int customer = this.customers.get(0);
        int succ = this.customers.get(1);

        int i = 1; // start with first customer (position i=0 is dummy depot out)
        do {
            // update for next iteration
            pred = customer;
            customer = succ;
            succ = this.customers.get(i+1);

            // Get locations
            int locSucc = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[succ]).get(solution.getCustomerAffiliationToLocations()[succ]);
            int locPred = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[pred]).get(solution.getCustomerAffiliationToLocations()[pred]);
            int locCustomer = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customer]).get(solution.getCustomerAffiliationToLocations()[customer]);
            
            // Get distances
            double distToCustomer = solution.getData().getDistanceBetweenLocations(locPred, locCustomer);
            double distFromCustomer = solution.getData().getDistanceBetweenLocations(locCustomer, locSucc);
            double distWithoutCustomer = solution.getData().getDistanceBetweenLocations(locPred, locSucc);
                 
            // Calculate costs
            double travelTimeReduction = distToCustomer + distFromCustomer - distWithoutCustomer;
            
            double costs = travelTimeReduction;
            double penaltyCosts = 0.0;
            if (Config.getInstance().enableGLS || Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) {
    			penaltyCosts = solution.getCustomersCostsForViolations(customer, false);
    		}
            	
            // Add to possible removals
            possibleRemovals.add(new double[] {customer, this.id, i, costs, penaltyCosts});
            i++;
        } while (i < this.customers.size() - 1);
        return possibleRemovals;
    }

    /**
     * Retrieve possible removals of customers within the vehicle's route based on 
     * the attached neighbor Graph
     * @param neighborGraph: neighbor graph
     * @return list of all possible removals
     */
    public ArrayList<double[]> getPossibleRemovals(double[][] neighborGraph, Solution solution) {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();

        if (!this.isUsed) 
        	return possibleRemovals;

        // init values
        int pred;
        int customer = this.customers.get(0);
        int succ = this.customers.get(1);

        int i = 1; // start with first customer (position i=0 is dummy depot out)
        do {
            // update for next iteration
            pred = customer;
            customer = succ;
            succ = this.customers.get(i+1);

            double score = neighborGraph[pred][customer] + neighborGraph[customer][succ];
            
            double penaltyCosts = 0.0;
            if (Config.getInstance().enableGLS || Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) {
    			penaltyCosts = solution.getCustomersCostsForViolations(customer, false);
    			// costs += penaltyCosts;
    		}
            possibleRemovals.add(new double[] {customer, this.id, i, score, penaltyCosts});
            i++;

        } while (i < this.customers.size() - 1);

        return possibleRemovals;
    }
    
    /**
     * Retrieve possible removals of customers within the vehicle's route based
     * on the attached request graph.
     * @param requestGraph: request graph
     * @return list of all possible removals
     */
    public ArrayList<double[]> getPossibleRequestRemovals(double[][] requestGraph, Solution solution) {
    	ArrayList<double[]> possibleRemovals = new ArrayList<>();
    	
    	// if car is not used -> quit
    	if (!this.isUsed) 
    		return possibleRemovals;
    	
    	// iterate customers
    	for (int i=1; i<this.customers.size()-1; i++) {
    		int customerI = this.customers.get(i);
    		int scoreI = 0;
    		for (int j=1; j<this.customers.size()-1; j++) {
    			if (i==j) continue;
    			
    			int customerJ = this.customers.get(j);
    			// aggregate values within the request graph for customerI
    			scoreI += requestGraph[customerI][customerJ];
    		}
    		
            double costs = scoreI;
            double penaltyCosts = 0.0;
            if (Config.getInstance().enableGLS || Config.getInstance().enableSchiffer || Config.getInstance().enableGLSFeature) {
    			penaltyCosts = solution.getCustomersCostsForViolations(customerI, false);
    			// costs += penaltyCosts;
    		}
            	
    		possibleRemovals.add(new double[] {customerI, this.id, i, costs, penaltyCosts});
    	}
    	
    	return possibleRemovals;
    }

    /**
     * This method realizes the insertion of an customer within the 
     * scheduling and updates the vehicles information, correspondingly.
     * @param insertion: array with insertion information
     * 			[customerID, vehiclesID, positionInRoute, startofService, 
     *           additionalCosts, customersPreferredLocation, locationsCapacitySlot]
     * @param data: Data object carrying the current scheduling's state
     */
    public void applyInsertion(double[] insertion, Data data, Solution solution) {
        // insertion: customer - vehicle id - positionInRoute - timeStart - additionCosts - locationIdx - capacitySlot - MapEntryIdx
    	
    	int pos = (int) insertion[2];  
        int customer = (int) insertion[0]; 
        int demand = data.getDemands()[customer];  
        int duration = data.getServiceDurations()[customer];  
        double start = insertion[3];  
        double additionCosts = insertion[4];
        int customerPreferredLocation = (int) insertion[5];
        int locationCapacityOccupied = (int) insertion[6];

        this.customers.add(pos, customer);
        this.nCustomersInTour++;
        this.startOfServices.add(pos, start);
        this.endOfServices.add(pos, start+duration);
        this.capacityUsed += demand;
        this.tourLength += additionCosts;
        
        solution.setCustomerAffiliationToLocation(customer, customerPreferredLocation);
        solution.setCustomerAssignmentToCapacitySlot(customer, locationCapacityOccupied);
        solution.setCustomerAssignmentToVehicles(customer, this.id);
        solution.setLocationOccupancy(DataUtils.getLocationIndex(customer, solution), 
        						  locationCapacityOccupied, 
        						  start, 
        						  start+duration,
        						  (int) insertion[7]);
        this.isUsed = true;
    }

    /**
     * Applies a removal at a certain position in the route which is specified by the attached
     * parameter removePosition. 
     * @param removePosition: position to be removed
     * @param data: Data Object
     * @return Customer id which has been removed
     */
    public int applyRemoval(int removePosition, Data data, Solution solution) {
    	// Get individual customer ids (first and last position in route indicate depots)
        int customer = this.customers.get(removePosition);
        int pred = this.customers.get(removePosition - 1);
        int succ = this.customers.get(removePosition + 1);

        // Deallocation of customer's information
        solution.freeLocationOccupancy(DataUtils.getLocationIndex(customer, solution), 
				   solution.getCustomerAffiliationToCapacity()[customer], 
				   this.startOfServices.get(removePosition), 
				   this.endOfServices.get(removePosition));
        int demand = data.getDemands()[customer];
        this.capacityUsed -= demand;
        this.customers.remove(removePosition);
        this.startOfServices.remove(removePosition);
        this.endOfServices.remove(removePosition);
        this.nCustomersInTour--;
        if (this.nCustomersInTour == 0) 
            this.isUsed = false;

        // Calculate new tour costs after removal
        // TODO_DONE Chris - costs have to be calculated if call comes from applyRemovalForCustomer(...)
        // Get locations
        int locSucc = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[succ]).get(solution.getCustomerAffiliationToLocations()[succ]);
        int locPred = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[pred]).get(solution.getCustomerAffiliationToLocations()[pred]);
        int locCustomer = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customer]).get(solution.getCustomerAffiliationToLocations()[customer]);
        // Calculate distances
        double distToCustomer = solution.getData().getDistanceBetweenLocations(locPred, locCustomer);
        double distFromCustomer = solution.getData().getDistanceBetweenLocations(locCustomer, locSucc);
        // Calculate cost reduction
        double reductionTravelCosts = distToCustomer + distFromCustomer - solution.getData().getDistanceBetweenLocations(locPred, locSucc);
        
        // Deallocate customer's information from the current solution object
        solution.freeCustomerAffiliationToLocation(customer);
        solution.freeCustomerAffiliationToCapacity(customer);
        solution.freeCustomerAffiliationToVehicle(customer);
        this.tourLength -= reductionTravelCosts;

        // return id of removed customer
        return customer;
    }
    
    /**
     * Applies a removal of the attached customer
     * @param customer: identifier of customer to be removed
     * @param data: data object
     * @param solution: solution object
     * @return identifier of customer being removed
     */
    public int applyRemovalForCustomer(int customer, Data data, Solution solution) {
        // find position of customer in tour
        int position = this.customers.indexOf(customer);
        return this.applyRemoval(position, data, solution);
    }
    
    //
    // CUSTOM GETTERS
    // 
    /**
     * Calculates the swapping costs for locations of all customers within the 
     * vehicle's route.
     * @param s: current solution object
     * @return the total swapping costs
     */
    public double getSwappingCosts(Solution s) {
    	double swappingCosts = 0.0;
    	for (int i = 1 ; i < this.customers.size()-1; i++)
    		swappingCosts += s.getData().getSwappingCosts()[s.getData().getCustomersPreferredLocation()[this.customers.get(i)]]
    													   [DataUtils.getLocationIndex(this.customers.get(i), s)];
    	return swappingCosts;
    }
    
    //
    // SETTERS
    //
    /**
     * Set the vehicle's id
     * @param id: id of vehicle
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Set the max capacity of the vehicle
     * @param capacityLimit: capacity limit
     */
    public void setCapacityLimit(int capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    /**
     * Set the capacity being used
     * @param capacityUsed: used capacity
     */
    public void setCapacityUsed(int capacityUsed) {
        this.capacityUsed = capacityUsed;
    }

    /**
     * Set the current tour length
     * @param tourLength: tour length
     */
    public void setTourLength(double tourLength) {
        this.tourLength = tourLength;
    }

    /**
     * Set the customers being scheduled within the vehicle's route
     * @param customers: list of customers
     */
    public void setCustomers(ArrayList<Integer> customers) {
        this.customers = customers;
    }

    /**
     * Set the start times of the customer's services
     * @param startOfServices: start times of services
     */
    public void setStartOfServices(ArrayList<Double> startOfServices) {
        this.startOfServices = startOfServices;
    }

    /**
     * Set the end times of the customer's services
     * @param endOfServices: end times of services
     */
    public void setEndOfServices(ArrayList<Double> endOfServices) {
        this.endOfServices = endOfServices;
    }

    /**
     * Set whether the vehicles is used or not
     * @param used: is vehicle used
     */
    public void setUsed(boolean used) {
        isUsed = used;
    }

    /**
     * Set the number of customers within the tour
     * @param nCustomersInTour: number of customers in the tour
     */
    public void setnCustomersInTour(int nCustomersInTour) {
        this.nCustomersInTour = nCustomersInTour;
    }
    
    /**
     * Set the skill level of the vehicle
     * @param skillLvl: skill level
     */
    public void setSkillLvl (int skillLvl) {
    	this.skillLvl = skillLvl;
    }
    
    public void setAvailable (boolean isAvailable) {
    	this.isAvailable = isAvailable;
    }

    //
    // GETTERS
    //
    /**
     * Get the number of customers within the tour of the vehicle
     * @return number of customers being scheduled for the vehicle
     */
    public int getnCustomersInTour() {
        return nCustomersInTour;
    }

    /**
     * The the vehicle's used capacity
     * @return capacity used
     */
    public int getCapacityUsed() {
        return capacityUsed;
    }

    /**
     * List of customers being in the tour
     * @return list of customers in the tour
     */
    public ArrayList<Integer> getCustomers() {
        return customers;
    }

    /**
     * Get all (real) customers in the tour.
     * Note, that the first and the last element defines the depot in the
     * list of all customers
     * @return list (real) customers
     */
    public ArrayList<Integer> getRealCustomers() {
        return new ArrayList<Integer>(customers.subList(1, customers.size()-1));
    }

    /**
     * Retrieve the starting times of the services 
     * @return list of start times
     */
    public ArrayList<Double> getStartOfServices() {
        return startOfServices;
    }

    /**
     * Retrieve the ending times of the service
     * @return list of end times
     */
    public ArrayList<Double> getEndOfServices() {
        return endOfServices;
    }

    /**
     * Get the total tour length
     * @return tour length
     */
    public double getTourLength() {
        return tourLength;
    }

    /**
     * Get the vehicle's id
     * @return id of vehicle
     */
    public int getId() {
        return id;
    }

    /**
     * Return whether the vehicles is used or not
     * @return vehicle being used or not
     */
    public boolean isUsed() {
        return isUsed;
    }
    
    /**
     * Returns the vehicle's/therapist's skill level 
     * @return skill level
     */
    public int getSkillLvl() {
    	return skillLvl;
    }
    
    /**
     * Returns whether the current vehicle is available for scheduling. 
     * If LNS with vehicle optimiatzion is used before the ALNS procedure,
     * a vehicles might get blocked and is not used for scheduling customers.
     * @return
     */
    public boolean isAvailable() {
    	return isAvailable;
    }
    
    
    //
    // EQUALITY & HASHING
    //
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return capacityLimit == vehicle.capacityLimit &&
                capacityUsed == vehicle.capacityUsed &&
                Double.compare(vehicle.tourLength, tourLength) == 0 &&
                Objects.equal(customers, vehicle.customers) &&
                Objects.equal(startOfServices, vehicle.startOfServices) &&
                Objects.equal(endOfServices, vehicle.endOfServices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(capacityLimit, capacityUsed, tourLength, customers, startOfServices, endOfServices);
    }
    
    
    //
    // PRINTING
    //
    /**
     * Prints the current tour
     */
    public void printTour(Solution sol) {
    	StringBuilder builder = new StringBuilder("");
    	DecimalFormat df = new DecimalFormat("0.0");
    	// TODO Alex -logger debug
    	System.out.println("Tour of vehicle " + this.id + " (n=" +  this.nCustomersInTour +  ") (TourCosts:" + df.format(this.tourLength) + "):"); 
        System.out.print(this.customers.get(0) + " --(" + sol.getData().getDistanceBetweenLocations(0, sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(1)])) + ")-> ");
        for (int i = 1; i < this.customers.size() - 1; i++) {
        	int originalCustomerId = sol.getData().getOriginalCustomerIds()[this.customers.get(i)]; 
        	System.out.print(this.customers.get(i) + 
            		"(" +originalCustomerId + "|" + 
            		DataUtils.getLocationIndex(this.customers.get(i), sol) + "|" + 
            		sol.getCustomerAffiliationToCapacity()[this.customers.get(i)] + "|" + 
            		sol.getData().getServiceDurations()[this.customers.get(i)] + "|" + 
            		this.startOfServices.get(i) + "-" + 
            		this.endOfServices.get(i) +  "|" + 
            		sol.getData().getEarliestStartTimes()[this.customers.get(i)] + "-" + 
            		sol.getData().getLatestStartTimes()[this.customers.get(i)] + ")" + " --");
            
            int locPred = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i)]);
            int locSucc = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i+1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i+1)]);

            if (i==this.customers.size()-2)
                System.out.print("(" + sol.getData().getDistanceBetweenLocations(locPred, 0) + ")-> ");
            else
            	System.out.print("(" + sol.getData().getDistanceBetweenLocations(locPred, locSucc) + ")-> ");
        }
        System.out.println(this.customers.get(this.customers.size() - 1) + "");
    }

    /**
     * Prints the current tour
     */
    public String getStringReprTour(Solution sol) {
    	StringBuilder builder = new StringBuilder("");
    	DecimalFormat df = new DecimalFormat("0.0");
    	builder.append("Tour of vehicle " + this.id + " (n=" +  this.nCustomersInTour +  ") (TourCosts:" + df.format(this.tourLength) + "):\n");
        builder.append(this.customers.get(0) + " --(" + sol.getData().getDistanceBetweenLocations(0, sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(1)])) + ")-> ");
        for (int i = 1; i < this.customers.size() - 1; i++) {
        	int originalCustomerId = sol.getData().getOriginalCustomerIds()[this.customers.get(i)];
            builder.append(this.customers.get(i) + 
            		"(" +originalCustomerId + "|" + 
            		DataUtils.getLocationIndex(this.customers.get(i), sol) + "|" + 
            		sol.getCustomerAffiliationToCapacity()[this.customers.get(i)] + "|" + 
            		sol.getData().getServiceDurations()[this.customers.get(i)] + "|" + 
            		this.startOfServices.get(i) + "-" + 
            		this.endOfServices.get(i) +  "|" + 
            		sol.getData().getEarliestStartTimes()[this.customers.get(i)] + "-" + 
            		sol.getData().getLatestStartTimes()[this.customers.get(i)]+  ")" + " --");            
            int locPred = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i)]);
            int locSucc = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i+1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i+1)]);

            if (i==this.customers.size()-2)
            	builder.append("(" + sol.getData().getDistanceBetweenLocations(locPred, 0) + ")-> ");
            else
            	builder.append("(" + sol.getData().getDistanceBetweenLocations(locPred, locSucc) + ")-> ");
        }
        builder.append(this.customers.get(this.customers.size() - 1) + "\n");

        String repr = builder.toString();
        return repr;
    }

    
    //
    // DEPRECATED
    //
/*
    public ArrayList<double[]> getPossibleRemovals_old(Data data) {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();

        if (!this.isUsed) return possibleRemovals;

        // init values
        int pred;
        int customer = this.customers.get(0);
        int succ = this.customers.get(1);

//        for (int i = 1; i < this.customers.size() - 1; i++) {
        int i = 1; // start with first customer (position i=0 is dummy depot out)
        do {
            // update for next iteration
            pred = customer;
            customer = succ;
            succ = this.customers.get(i+1);

            // TODO Alex - den Teil vorher ueber globalen Cache probieren
            double distToCustomer = data.getDistanceBetweenCustomers(pred, customer);
            double distFromCustomer = data.getDistanceBetweenCustomers(customer, succ);
            double distWithoutCustomer = data.getDistanceBetweenCustomers(pred, succ);

            double travelTimeReduction = distToCustomer + distFromCustomer - distWithoutCustomer;
            possibleRemovals.add(new double[] {customer, this.id, i, travelTimeReduction});
            i++;

        } while (i < this.customers.size() - 1);

        return possibleRemovals;
    }
*/
    
    
/*    
    public ArrayList<double[]> getPossibleRemovals_old(double[][] neighborGraph) {

        ArrayList<double[]> possibleRemovals = new ArrayList<>();

        if (!this.isUsed) return possibleRemovals;

        // init values
        int pred;
        int customer = this.customers.get(0);
        int succ = this.customers.get(1);

//        for (int i = 1; i < this.customers.size() - 1; i++) {
        int i = 1; // start with first customer (position i=0 is dummy depot out)
        do {
            // update for next iteration
            pred = customer;
            customer = succ;
            succ = this.customers.get(i+1);

            double score = neighborGraph[pred][customer] + neighborGraph[customer][succ];
            possibleRemovals.add(new double[] {customer, this.id, i, score});
            i++;

        } while (i < this.customers.size() - 1);

        return possibleRemovals;
    }
*/
    
    
/*    
    public ArrayList<double[]> getPossibleInsertions_old(int customer, Data data) {

        ArrayList<double[]> possibleInsertions = new ArrayList<>();

        // if capacity limit would be reached, the customer cannot be inserted
        if (this.capacityUsed + data.getDemands()[customer] > this.capacityLimit) return possibleInsertions;

        double earliestStartCustomer = data.getEarliestStartTimes()[customer];
        double latestStartCustomer = data.getLatestStartTimes()[customer];

        // TODO Alex - fuer die Methode brauchen wir auf jeden Fall ein paar Testcases
        // iterate over all customers in tour
        for (int i = 0; i < this.customers.size() - 1; i++ ) {
            int pred = this.customers.get(i);
            int succ = this.customers.get(i+1);

            double distToCustomer = data.getDistanceBetweenCustomers(pred, customer);
            double earliestStartAtInsertion = Math.max(this.endOfServices.get(i) + distToCustomer, earliestStartCustomer);
            double distFromCustomer = data.getDistanceBetweenCustomers(customer, succ);
            double latestStartAtInsertion = Math.min(this.startOfServices.get(i+1) - distFromCustomer - data.getServiceDurations()[customer], latestStartCustomer);

            // if latest start of customer is less than earliest start at position, later position will also not be possible
            if (latestStartCustomer < earliestStartAtInsertion - Config.getInstance().epsilon) break;

            // check if time window feasible (if enough time between customers already in route
            if (latestStartAtInsertion - earliestStartAtInsertion > Config.getInstance().epsilon) {
                double additionTravelCosts = distToCustomer + distFromCustomer - data.getDistanceBetweenCustomers(pred, succ);
                possibleInsertions.add(new double[] {customer, this.id, i+1, earliestStartAtInsertion, additionTravelCosts});
            }
        }
        return possibleInsertions;
    }
*/    
    
    // returns customer id
/*
    public int applyRemoval_old(int removePosition, Data data) {

    	// System.out.println("Apply removal (v=" + this.id + ", remove=" + removePosition + ")");

        // gather information
        int customer = this.customers.get(removePosition);
        //  System.out.println(customer); 
        //  System.out.println(this.customers); 

        int pred = this.customers.get(removePosition - 1);
        int succ = this.customers.get(removePosition + 1);

        // update information in tour
        int demand = data.getDemands()[customer];
        this.capacityUsed -= demand;

        this.customers.remove(removePosition);
        this.startOfServices.remove(removePosition);
        this.endOfServices.remove(removePosition);

        this.nCustomersInTour--;
        if (this.nCustomersInTour == 0) {
            this.isUsed = false;
        }
        // tour costs
        double distToCustomer = data.getDistanceBetweenCustomers(pred, customer);
        double distFromCustomer = data.getDistanceBetweenCustomers(customer, succ);

        double reductionTravelCosts = distToCustomer + distFromCustomer - data.getDistanceBetweenCustomers(pred, succ);
        // TODO Alex - hier koennte man auch den Cache nutzen
        this.tourLength -= reductionTravelCosts;

        return customer;
    }
*/
}