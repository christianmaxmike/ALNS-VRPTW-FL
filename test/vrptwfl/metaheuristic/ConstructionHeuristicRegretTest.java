package vrptwfl.metaheuristic;

import org.junit.Before;
import org.junit.Test;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;

import static org.junit.Assert.fail;

public class ConstructionHeuristicRegretTest {

    private ConstructionHeuristicRegret construction;

    @Before
    public void setup() throws IOException, ArgumentOutOfBoundsException {
        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = generator.loadInstance("R104.txt", 50);
        construction = new ConstructionHeuristicRegret(data);
    }

    @Test(expected = ArgumentOutOfBoundsException.class)
    public void rejectInvalidK() throws ArgumentOutOfBoundsException {
        construction.solve(1);
    }

    // TODO Test, ob possibleInsertionsForCustomer auch wirklich nach groesstem Regret sortiert ist (ist das was vorne steht wirklich groesser?)?

    // TODO checken, ob die Touren wirklich feasible sind (zumindest exemplarisch)
}
