package vrptwfl.metaheuristic;

import org.junit.Test;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import static org.junit.Assert.*;

public class ALNSTest {

    @Test
    public void checkThatALNSReturnsCorrectInformation() throws ArgumentOutOfBoundsException {
        MainALNS alns = new MainALNS();
        double obj = alns.runALNS("R104", 100);

        assertEquals(0.0, obj, 0.001);
    }
}
