package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;

public class ALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS() throws ArgumentOutOfBoundsException {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = null;
        try {
            data = generator.loadInstance("R104.txt", 100);
        } catch (ArgumentOutOfBoundsException | IOException e) {
            e.printStackTrace();
        }

        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.solve(2);
        long finishTimeConstruction = System.currentTimeMillis();
        long timeElapsed = (finishTimeConstruction - startTimeConstruction);
        System.out.println("Time for construction " + timeElapsed + " ms.");


        // TODO wieder raus
        System.out.println(construction.getNotAssignedCustomers());
        System.out.println(construction.getInfeasibleCustomers());
        solutionConstr.printSolution();

        // TODO 2: tests für geladene instanzen

        // TODO config file nutzbar machen. (bigMRegret, auf 1 nachkommastelle runden etc)
        //  (wenn runden moeglich ist, dann auch tour kosten runden; vielleicht auch nicht, kann spaeter probleme geben)
        // TODO 1c: Logik ALNS anfangen

        // TODO moegliches hashing
        //  - bereits generierte Loesungen
        //  - ggf. earliest, latest possible starts in partial routes (pred_id, pred_time,)
        return 0.0;
    }

    public static void main(String[] args) throws ArgumentOutOfBoundsException {
        ALNS algo = new ALNS();
        algo.runALNS();
    }
}
