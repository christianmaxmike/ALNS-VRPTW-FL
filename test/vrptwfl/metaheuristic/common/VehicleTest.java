package vrptwfl.metaheuristic.common;

import org.junit.Before;
import org.junit.Test;
import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;

import static org.junit.Assert.*;

public class VehicleTest {

    private Vehicle vehicle;
    private Data data;

    @Before
    public void setup() throws IOException, ArgumentOutOfBoundsException {
        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        data = generator.loadInstance("C104.txt", 100);
        vehicle = new Vehicle(4, 200, 230., 1);
    }

    @Test
    public void dummyLeaveDepotAtTimeZero() {
        assertEquals("Leaving depot start", 0.0, vehicle.getStartOfServices().get(0), Config.getInstance().epsilon);
        assertEquals("Leaving depot end", 0.0, vehicle.getEndOfServices().get(0), Config.getInstance().epsilon);
    }

    @Test
    public void dummyEnterDepotAtEndOfPlanningHorizon() {
        assertEquals("Returning to depot start", 230.0, vehicle.getStartOfServices().get(1), Config.getInstance().epsilon);
        assertEquals("Returning to depot end", 230.0, vehicle.getEndOfServices().get(1), Config.getInstance().epsilon);
    }

//    @Test
//    public void noInsertionPossibleIfCapacityLimitExceeded() {
//        int customer = 10;
//        data.setDemandOfCustomer(customer, 1_000);
//        // if capacity is exceeded return empty list of possible insertions
//        assertTrue(vehicle.getPossibleInsertions(customer, data).isEmpty());
//    }

    // TODO Alex - 1-2 Test, dass berechnete Insertions korrekt sind
    //  {customer, vehicle.id, i+1, earliestStartAtInsertion, additionTravelCosts}
    // nur das vehicle oben übrig lassen

    // TODO Alex - Test, dass Insertion geklappt, hat (bisschen komplizierters Beispiel mit min 4 Kunden in Liste)
    // 4 Kunden in tour einfügen
}
