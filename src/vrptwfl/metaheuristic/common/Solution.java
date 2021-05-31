package vrptwfl.metaheuristic.common;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private ArrayList<Integer> tempInfeasibleCustomers; // needed to store customer that cannot be assigned to any route
    private double totalCosts;
    private ArrayList<Vehicle> vehicles;
    private boolean isFeasible = false;

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

    public Solution() {

    }

    public Solution copyDeep() {

        Solution sol = new Solution();
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
        return vehicles;
    }


    private void calculateCostsFromVehicles() {
        this.totalCosts = 0.;
        for (Vehicle veh: vehicles) {
            this.totalCosts += veh.getTourLength();
        }
    }

    public void printSolution() {
        System.out.println("Solution total costs: " + this.totalCosts); // TODO logger debug!
        for (Vehicle veh: this.vehicles) {
            if (veh.isUsed()) {
                veh.printTour();
            }
        }
    }

    public void updateSolutionAfterRemoval(List<Integer> removedCustomers) {

        this.calculateCostsFromVehicles();

        if (!removedCustomers.isEmpty()) {
            this.notAssignedCustomers.addAll(removedCustomers);
            isFeasible = false;
        }
    }

    public void updateSolutionAfterInsertion() {
        this.calculateCostsFromVehicles();
        this.addInfeasiblesToNotAssigned();
    }

    private void addInfeasiblesToNotAssigned() {

        this.notAssignedCustomers.addAll(this.tempInfeasibleCustomers);
        this.tempInfeasibleCustomers.clear();

        this.isFeasible = this.notAssignedCustomers.isEmpty();
    }



}
