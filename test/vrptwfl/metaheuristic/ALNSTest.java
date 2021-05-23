package vrptwfl.metaheuristic;

import org.junit.Test;

import static org.junit.Assert.*;

public class ALNSTest {

    @Test
    public void checkThatALNSReturnsCorrectInformation() {
        ALNS alns = new ALNS();
        double obj = alns.runALNS();

        assertEquals(0.0, obj, 0.001);
    }
}
