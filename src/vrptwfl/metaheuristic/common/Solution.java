package vrptwfl.metaheuristic.common;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private double totalCosts;
    private ArrayList<Vehicle> vehicles;
    private boolean isFeasible = false;

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

    public Solution(ArrayList<Vehicle> vehicles, ArrayList<Integer> notAssignedCustomers) {
        this.vehicles = vehicles;
        this.calculateCostsFromVehicles();
        this.notAssignedCustomers = notAssignedCustomers;

        if (notAssignedCustomers.isEmpty()) { // TODO brauchen wir einen Test dafür (?) --> eher wenn Lösung in ALNS bearbeitet wurde, ob dann noch alles passt
            this.isFeasible = true;
        }
    }

    public Solution() {

    }

    public Solution copyDeep() {

        Solution sol = new Solution();
        sol.setNotAssignedCustomers(new ArrayList<>(this.notAssignedCustomers));
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

    public void updateSolution(List<Integer> removedCustomers) {

        this.calculateCostsFromVehicles();

        if (!removedCustomers.isEmpty()) {
            this.notAssignedCustomers.addAll(removedCustomers);
            isFeasible = false;
        }
    }



}
