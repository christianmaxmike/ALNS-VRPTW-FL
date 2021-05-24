package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

public class ALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS() {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = generator.loadInstance("R101", 25);

        // TODO methode, um distanz matrix zu generieren

        // TODO test für geladene instanzen

        return 0.0;
    }
}
