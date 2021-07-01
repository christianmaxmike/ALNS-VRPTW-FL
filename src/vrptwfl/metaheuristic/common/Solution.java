package vrptwfl.metaheuristic.common;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private ArrayList<Integer> tempInfeasibleCustomers; // needed to store customer that cannot be assigned to any route
    private double totalCosts;
    private ArrayList<Vehicle> vehicles;
    private boolean isFeasible = false;
    private Data data;

    public int getNrOfAssignedCustomers() {
        return this.data.getnCustomers() - this.notAssignedCustomers.size();
    }

    public ArrayList<Integer> getTempInfeasibleCustomers() {
        return tempInfeasibleCustomers;
    }

    public void setTempInfeasibleCustomers(ArrayList<Integer> tempInfeasibleCustomers) {
        this.tempInfeasibleCustomers = tempInfeasibleCustomers;
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public void setNotAssignedCustomers(ArrayList<Integer> notAssignedCustomers) {
        this.notAssignedCustomers = notAssignedCustomers;
    }

    public void setTotalCosts(double totalCosts) {
        this.totalCosts = totalCosts;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void setFeasible(boolean feasible) {
        isFeasible = feasible;
    }

//    public Solution(ArrayList<Vehicle> vehicles, ArrayList<Integer> notAssignedCustomers) {
//        // TODO muss hier auch infeasible rein?
//        this.vehicles = vehicles;
//        this.calculateCostsFromVehicles();
//        this.notAssignedCustomers = notAssignedCustomers;
//
//        if (notAssignedCustomers.isEmpty()) { // TODO brauchen wir einen Test dafür (?) --> eher wenn Lösung in ALNS bearbeitet wurde, ob dann noch alles passt
//            this.isFeasible = true;
//        }
//    }

    public Solution(Data data) {
        this.data = data;
    }

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

        return sol;
    }

    public void setSolution(Solution solutionTemp) {
        this.notAssignedCustomers = new ArrayList<>(solutionTemp.getNotAssignedCustomers());
        this.totalCosts = solutionTemp.getTotalCosts();
        this.isFeasible = solutionTemp.isFeasible();

        ArrayList<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle veh: solutionTemp.getVehicles()) {
            newVehicles.add(veh.copyDeep());
        }

        this.setVehicles(newVehicles);

    }

    public ArrayList<Integer> getNotAssignedCustomers() {
        return notAssignedCustomers;
    }

    public void addCustomerToNotAssignedCustomers(int customer) {
        this.notAssignedCustomers.add(customer);
    }

    public double getTotalCosts() {
        return totalCosts;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles; // TODO how to handle escaping reference ?
    }


    private void calculateCostsFromVehicles() {
        this.totalCosts = 0.;
        for (Vehicle veh: vehicles) {
            this.totalCosts += veh.getTourLength();
        }
    }

    public void printSolution() {
        int nActiveVehicles = this.getNActiveVehicles();
        System.out.println("Solution total costs: " + this.totalCosts + "\tn vehicles used: " + nActiveVehicles); // TODO logger debug!
        for (Vehicle veh: this.vehicles) {
            if (veh.isUsed()) {
                veh.printTour();
            }
        }
    }

    private int getNActiveVehicles() {

        int count = 0;
        for (Vehicle v: this.vehicles) {
            if (v.isUsed()) count++;
        }
        return count;
    }

    public void updateSolutionAfterRemoval(List<Integer> removedCustomers) {

        this.calculateCostsFromVehicles();

        if (!removedCustomers.isEmpty()) {
            this.notAssignedCustomers.addAll(removedCustomers);
            isFeasible = false;
        }

//        this.calculatePenaltyCosts(); // TODO kann ggf raus, da penalties er nach Insertion berechnet werden muessen
    }

    public void updateSolutionAfterInsertion() {
        this.calculateCostsFromVehicles();
        this.addInfeasiblesToNotAssigned();
        this.calculatePenaltyCosts();
    }

    private void calculatePenaltyCosts() {
        this.addCostsForUnservedCustomers();
    }

    private void addCostsForUnservedCustomers() {
        this.totalCosts += this.notAssignedCustomers.size() * Config.penaltyUnservedCustomer;
    }

    private void addInfeasiblesToNotAssigned() {

        this.notAssignedCustomers.addAll(this.tempInfeasibleCustomers);
        this.tempInfeasibleCustomers.clear();

        this.isFeasible = this.notAssignedCustomers.isEmpty();
    }


    // method is public such that logic can be tested
    public ArrayList<double[]> getPossibleInsertionsForCustomer(int customer) {
        ArrayList<double[]> possibleInsertionsForCustomer = new ArrayList<>();

        boolean triedUnusedVehicle = false;
        for (Vehicle vehicle: this.getVehicles()) {
            // generate insertion for unused vehicle only once, otherwise regrets between all unused vehicles will be zero
            if (!vehicle.isUsed()) {
                if (triedUnusedVehicle) continue;
                triedUnusedVehicle = true;
            }

            ArrayList<double[]> insertions = vehicle.getPossibleInsertions(customer, this.data);


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

    public ArrayList<double[]> getPossibleRemovalsSortedByCostReduction() {
        ArrayList<double[]> possibleRemovals = new ArrayList<>();

        for (Vehicle vehicle: this.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<double[]> removals = vehicle.getPossibleRemovals(this.data);
                possibleRemovals.addAll(removals);
            }
        }

        possibleRemovals.sort(Comparator.comparing(a -> a[3], Collections.reverseOrder())); // sort by travelTimeReduction
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
}
