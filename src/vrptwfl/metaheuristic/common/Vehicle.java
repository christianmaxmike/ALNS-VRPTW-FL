package vrptwfl.metaheuristic.common;

import com.google.common.base.Objects;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class implements a vehicle object
 */
public class Vehicle {

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
     */
    public Vehicle(int id, int capacityLimit, double latestEndOfService, int skillLvl) {
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
        this.startOfServices.add(0.0);
        this.startOfServices.add(latestEndOfService);
        this.endOfServices = new ArrayList<>();
        this.endOfServices.add(0.0);
        this.endOfServices.add(latestEndOfService);
        this.isUsed = false;
        this.skillLvl = skillLvl;
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
        v.setUsed(this.isUsed);
        v.setnCustomersInTour(this.nCustomersInTour);
        v.setSkillLvl(this.skillLvl);
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

        // insufficient skill level
        if (this.skillLvl < data.getRequiredSkillLvl()[customer])
        	return possibleInsertions;
        
        // if capacity limit would be reached, the customer cannot be inserted
        if (this.capacityUsed + data.getDemands()[customer] > this.capacityLimit) 
        	return possibleInsertions;

        double earliestStartCustomer = data.getEarliestStartTimes()[customer];
        double latestStartCustomer = data.getLatestStartTimes()[customer];

        for (int i = 0; i < this.customers.size() - 1; i++ ) {
            int pred = this.customers.get(i);
            int succ = this.customers.get(i+1);
            double endServicePred = this.endOfServices.get(i);
            double startServiceSucc = this.startOfServices.get(i+1);
            
            // return arr in format [location, capacitySlot, startTimeService, costs, entryIdxInLoc]
            // locationIdx, capacity, timeStart, additionalTravelCosts, entryIdx};
            ArrayList<double[]> customersPossibleLTW = solution.getAvailableLTWForCustomer(customer, 
            		                                                     (int) earliestStartCustomer, 
            		                                                     (int) latestStartCustomer,
            		                                                     data.getServiceDurations()[customer],
            															 pred, succ,
            															 (int) endServicePred, (int) startServiceSucc);
            if (customersPossibleLTW.size() == 0) // no match could be found
            	continue;
            else // possible insertion found
            	for (int newEntry = 0 ; newEntry<customersPossibleLTW.size(); newEntry++)
            		// customer, vehicleId, posInRoute, starTime, costs, location, capacity, entryIdxInLoc
            		possibleInsertions.add(new double[] {customer, 
            											 this.id, 
            											 i+1, 
            											 customersPossibleLTW.get(newEntry)[2], 
            											 customersPossibleLTW.get(newEntry)[3], 
            											 customersPossibleLTW.get(newEntry)[0], 
            											 customersPossibleLTW.get(newEntry)[1], 
            											 customersPossibleLTW.get(newEntry)[4]});
        }
        return possibleInsertions;
    }
    
    /**
     * Retrieve the possible removals of customers within the vehicle's route
     * @param data: Data object
     * @param solution: Solution object where customer shall be removed
     * @return  list with all possible removals
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
            
            // Add to possible removals
            possibleRemovals.add(new double[] {customer, this.id, i, travelTimeReduction});
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
    public ArrayList<double[]> getPossibleRemovals(double[][] neighborGraph) {
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
            possibleRemovals.add(new double[] {customer, this.id, i, score});
            i++;

        } while (i < this.customers.size() - 1);

        return possibleRemovals;
    }


    // TODO Alex: Methode für cost increase und reduction (tour laenge)


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
    	//if ((int) insertion[0] == 3)
        //	System.out.println();
    	
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
        
        //System.out.println();
        //System.out.println("After insertion");
        //this.printTour(solution);
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
        // TODO Chris - costs have already been calculated, attach complete removal array as parameter
        // TODO Chris - likewise to abstractInsertion
        
        int locSucc = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[succ]).get(solution.getCustomerAffiliationToLocations()[succ]);
        int locPred = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[pred]).get(solution.getCustomerAffiliationToLocations()[pred]);
        int locCustomer = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customer]).get(solution.getCustomerAffiliationToLocations()[customer]);
        double distToCustomer = solution.getData().getDistanceBetweenLocations(locPred, locCustomer);
        double distFromCustomer = solution.getData().getDistanceBetweenLocations(locCustomer, locSucc);
        
        // double distToCustomer = solution.getDistanceBetweenCustomersByAffiliations(pred, customer);        
        // double distFromCustomer = solution.getDistanceBetweenCustomersByAffiliations(customer, succ);
        // double reductionTravelCosts = distToCustomer + distFromCustomer - solution.getDistanceBetweenCustomersByAffiliations(pred, succ);
        double reductionTravelCosts = distToCustomer + distFromCustomer - solution.getData().getDistanceBetweenLocations(locPred, locSucc);
        
        solution.freeCustomerAffiliationToLocation(customer);
        solution.freeCustomerAffiliationToCapacity(customer);
        solution.freeCustomerAffiliationToVehicle(customer);
        this.tourLength -= reductionTravelCosts;

        // this.printTour(solution);
        // return id of removed customer
        return customer;
    }
    
    
    public int applyRemovalForCustomer(int customer, Data data, Solution solution) {
        // find position of customer in tour
        int position = this.customers.indexOf(customer);
        return this.applyRemoval(position, data, solution);
    }
    
    //
    // CUSTOM GETTERS
    // 
    public double getSwappingCosts(Solution s) {
    	double swappingCosts = 0.0;
    	for ( int i = 1 ; i < this.customers.size()-1; i++)
    		swappingCosts += s.getData().getSwappingCosts()[s.getData().getCustomersPreferredLocation()[this.customers.get(i)]][s.getCustomerAffiliationToLocations()[this.customers.get(i)]];
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
    
    public int getSkillLvl() {
    	return skillLvl;
    }
    
    
    //
    // EQUALITY & HASHING
    //
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
    	DecimalFormat df = new DecimalFormat("0.0");
        System.out.println("Tour of vehicle " + this.id + " (n=" +  this.nCustomersInTour +  ") (TourCosts:" + df.format(this.tourLength) + "):"); // TODO Alex -logger debug
        System.out.print(this.customers.get(0) + " --(" + sol.getData().getDistanceBetweenLocations(0, sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(1)])) + ")-> ");
        for (int i = 1; i < this.customers.size() - 1; i++) {
        	int originalCustomerId = sol.getData().getOriginalCustomerIds()[this.customers.get(i)];
            System.out.print(this.customers.get(i) + 
            		"(" +originalCustomerId + "|" + 
            		DataUtils.getLocationIndex(this.customers.get(i), sol) + "|" + 
            		sol.getCustomerAffiliationToCapacity()[this.customers.get(i)] + "|" + 
            		sol.getData().getServiceDurations()[this.customers.get(i)] + "|" + 
            		this.startOfServices.get(i) + "-" + 
            		this.endOfServices.get(i) +  ")" + " --");
            
            int locPred = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i)]);
            int locSucc = sol.getData().getCustomersToLocations().get(sol.getData().getOriginalCustomerIds()[this.customers.get(i+1)]).get(sol.getCustomerAffiliationToLocations()[this.customers.get(i+1)]);

            if (i==this.customers.size()-2)
                System.out.print("(" + sol.getData().getDistanceBetweenLocations(locPred, 0) + ")-> ");
            else
            	System.out.print("(" + sol.getData().getDistanceBetweenLocations(locPred, locSucc) + ")-> ");
        }
        System.out.println(this.customers.get(this.customers.size() - 1) + "");
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
            if (latestStartCustomer < earliestStartAtInsertion - Config.epsilon) break;

            // check if time window feasible (if enough time between customers already in route
            if (latestStartAtInsertion - earliestStartAtInsertion > Config.epsilon) {
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