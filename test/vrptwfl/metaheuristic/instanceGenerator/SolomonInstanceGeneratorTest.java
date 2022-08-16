package vrptwfl.metaheuristic.instanceGenerator;

import org.junit.Before;
import org.junit.Test;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SolomonInstanceGeneratorTest {

    private SolomonInstanceGenerator generator;

    @Before
    public void setup() {
        generator = new SolomonInstanceGenerator();
    }

    @Test(expected = ArgumentOutOfBoundsException.class)
    public void throwErrorIfNumberOfCustomersTooHigh() throws ArgumentOutOfBoundsException, IOException {
        generator.loadInstance("R101.txt", 125); // maximum is 100
    }

    @Test(expected = ArgumentOutOfBoundsException.class)
    public void throwErrorIfNumberOfCustomersTooLow() throws ArgumentOutOfBoundsException, IOException {
        generator.loadInstance("R101.txt", 0); // minimum is 1
    }

    @Test(expected = NoSuchFileException.class)
    public void throwErrorIfFileNameDoesNotExist() throws ArgumentOutOfBoundsException, IOException {
        generator.loadInstance("DoesNotExist.txt", 25);
    }

    @Test(expected = ArgumentOutOfBoundsException.class)
    public void textFilesHaveCorrectLength()  throws ArgumentOutOfBoundsException, IOException {
        generator.loadInstance("wrong_number_of_lines.txt", 25);
    }

    @Test
    public void dataOutputIsCalculatedCorrectly() throws ArgumentOutOfBoundsException, IOException {
        Data data = generator.loadInstance("R101.txt", 50);

        assertEquals("Number of customer", 50, data.getCustomers().length);

        assertEquals("Distance [0][1]", 15.2, data.getDistanceBetweenLocations(0,1), 0.0001);
        assertEquals("Distance [38][12]", 54.1, data.getDistanceBetweenLocations(38,12), 0.0001);

        assertEquals("Demand customer 11", 12, data.getDemands()[11]);
        assertEquals("Earliest start customer 23", 68, data.getEarliestStartTimes()[23]);
        assertEquals("Latest start customer 34", 127, data.getLatestStartTimes()[34]);
        assertEquals("Service time customer 44", 10, data.getServiceDurations()[44]);

        assertEquals("Number of vehicles", 25, data.getnVehicles());
        assertEquals("Vehicle capacity", 200, data.getVehicleCapacity());
        assertEquals("End of planning horizon", 230.0, data.getEndOfPlanningHorizon(), 0.0001);
    }
}
