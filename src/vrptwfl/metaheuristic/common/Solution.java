package vrptwfl.metaheuristic.common;

import java.util.ArrayList;

public class Solution {

    private double totalCosts;
    private ArrayList<Vehicle> vehicles;

    public double getTotalCosts() {
        return totalCosts;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public Solution( ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
        this.calculateCostsFromVehicles();
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
