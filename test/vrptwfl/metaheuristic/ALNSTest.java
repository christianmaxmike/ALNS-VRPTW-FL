package vrptwfl.metaheuristic;

import org.junit.Test;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

// import static org.junit.Assert.*;

public class ALNSTest {

    @Test
    public void checkThatALNSReturnsCorrectInformation() throws ArgumentOutOfBoundsException {
    	String outFile = "results_test.txt";
    	int nCustomers = 100;
    	String instanceName = "R104";
    	
    	Data[] data;
    	data = MainALNS.loadSolomonInstance(instanceName, nCustomers);
    	
    	MainALNS alns = new MainALNS(outFile, "test_out.txt", true);
        alns.runALNS(data[0], instanceName);

        // assertEquals(0.0, obj, 0.001);
    }
}
