package vrptwfl.metaheuristic.common;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class VehicleTest {

    private Vehicle vehicle;

    @Before
    public void setup() {
        vehicle = new Vehicle(4, 200, 230.);
    }

    @Test
    public void dummyLeaveDepotAtTimeZero() {
        // TODO assert start of service and end of service
        fail();
    }

    @Test
    public void dummyEnterDepotAtEndOfPlanningHorizon() {
        // TODO assert start of service and end of service
        fail();
    }

    @Test
    public void noInsertionPossibleIfCapacityLimitExceeded() {
        // TODO (return null)
        fail();
    }

    // TODO 1-2 Test, dass berechnete Insertions korrekt sind
    //  {customer, vehicle.id, i+1, earliestStartAtInsertion, additionTravelCosts}

    // TODO Test, dass Insertion geklappt, hat (bisschen komplizierters Beispiel mit min 4 Kunden in Liste)
}
