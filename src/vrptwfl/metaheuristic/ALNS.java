package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;

public class ALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS() {

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

        // TODO 2: tests für geladene instanzen
        // TODO 3: nachdem die Tests da sind, refactring
        // TODO 4: wie funktioniert logger?

        // TODO 1c: Logik ALNS anfangen

        // TODO moegliches hashing
        //  - bereits generierte Loesungen
        //  - ggf. earliest, latest possible starts in partial routes (pred_id, pred_time,)
        return 0.0;
    }

    public static void main(String[] args) {
        ALNS algo = new ALNS();
        algo.runALNS();
    }
}
