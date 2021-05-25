package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

public class ALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS() {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = generator.loadInstance("R104.txt", 100);


        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.solve(2);
        long finishTimeConstruction = System.currentTimeMillis();
        long timeElapsed = (finishTimeConstruction - startTimeConstruction);
        System.out.println("Time for construction " + timeElapsed + " ms.");

        // TODO Mittwoch 26.05. morgens
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
