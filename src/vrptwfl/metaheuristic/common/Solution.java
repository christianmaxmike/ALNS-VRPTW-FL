package vrptwfl.metaheuristic.common;

import java.util.ArrayList;

public class Solution {

    private ArrayList<Integer> notAssignedCustomers;
    private double totalCosts;
    private ArrayList<Vehicle> vehicles;
    private boolean isFeasible = false;

    public Solution(ArrayList<Vehicle> vehicles, ArrayList<Integer> notAssignedCustomers) {
        this.vehicles = vehicles;
        this.calculateCostsFromVehicles();
        this.notAssignedCustomers = notAssignedCustomers;

        if (notAssignedCustomers.isEmpty()) { // TODO brauchen wir einen Test dafür (?) --> eher wenn Lösung in ALNS bearbeitet wurde, ob dann noch alles passt
            this.isFeasible = true;
        }
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



}
