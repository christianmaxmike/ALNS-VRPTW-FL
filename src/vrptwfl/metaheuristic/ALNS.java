package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

public class ALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS() {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = generator.loadInstance("R101.txt", 100);

        // TODO test für geladene instanzen

        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        construction.solve(3);

        return 0.0;
    }

    public static void main(String[] args) {
        ALNS algo = new ALNS();
        algo.runALNS();
    }
}
