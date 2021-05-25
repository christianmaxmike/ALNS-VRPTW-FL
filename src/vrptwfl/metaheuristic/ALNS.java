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
        Solution solutionConstr = construction.solve(2);

        // TODO 1: Zeitmessen construction
        // TODO 1b: Hash tables || Hashtables for OR algorithms example (youtube, google?)
        //  hashtable in collections ?
        // TODO 1c: Logik ALNS anfangen

        // TODO Mittwoch 26.05. morgens
        // TODO 2: tests für geladene instanzen
        // TODO 3: nachdem die Tests da sind, refactring
        // TODO 4: wie funktioniert logger?


        return 0.0;
    }

    public static void main(String[] args) {
        ALNS algo = new ALNS();
        algo.runALNS();
    }
}
