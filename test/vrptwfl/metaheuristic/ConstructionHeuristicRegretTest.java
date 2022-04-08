package vrptwfl.metaheuristic;

import org.junit.Before;
import org.junit.Test;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ConstructionHeuristicRegretTest {

    private ConstructionHeuristicRegret construction;
    private Data data;

//    @Before
//    public void setup() throws IOException, ArgumentOutOfBoundsException {
//        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
//        data = generator.loadInstance("R104.txt", 100);
//        construction = new ConstructionHeuristicRegret(data);
//    }
//
//    @Test(expected = ArgumentOutOfBoundsException.class)
//    public void rejectInvalidK() throws ArgumentOutOfBoundsException {
//        construction.solve(1);
//    }
//
//    @Test
//    public void firstCustomerHasOnlyOnePossibleInsertion() {
//
//        int customer = 1;
//        ArrayList<double[]> possibleInsertions =  construction.getPossibleInsertionsForCustomer(customer);
//
//        // there should only be one possible insertions (all vehicles are empty, i.e. only the first one should be tried)
//        assertEquals(1, possibleInsertions.size());
//
//        // ensure correctness of insertion info
//        double[] insertion = possibleInsertions.get(0);
//        assertEquals("Customer", 1., insertion[0], Config.epsilon);
//        assertEquals("Vehicle", 0., insertion[1], Config.epsilon);
//        assertEquals("Position in route", 1., insertion[2], Config.epsilon);
//        assertEquals("Start time", 15.2, insertion[3], Config.epsilon); // distance of depot to customer 1 in instance
//        assertEquals("Additional costs", 30.4, insertion[4], Config.epsilon); // two times distance depot to customer 1
//    }
//
//    @Test
//    public void informationInTourCorrectAfterFirstCustomerIsInserted() {
//        int customer = 12;
//        ArrayList<double[]> possibleInsertions =  construction.getPossibleInsertionsForCustomer(customer);
//        double[] insertion = possibleInsertions.get(0);
//        construction.getVehicles().get((int) insertion[1]).applyInsertion(insertion, data);
//
//        Vehicle vehicle1 =  construction.getVehicles().get(0); // first customer has to be inserted into vehicle with id = 0
//
//        assertEquals("Number of nodes in tour", 3, vehicle1.getCustomers().size()); // 2x depot, 1x customer 12
//        assertEquals("Customer is customer 12", 12, (int) vehicle1.getCustomers().get(1)); // 0 is depot, 1 is customer, 2 is depot
//        assertEquals("Number of customers in tour", 1, vehicle1.getnCustomersInTour());
//        assertEquals("Start time", 15., vehicle1.getStartOfServices().get(1), Config.epsilon);
//        assertEquals("End time", 25., vehicle1.getEndOfServices().get(1), Config.epsilon); // start time 15 + duration 10
//        assertEquals("Tour length", 30., vehicle1.getTourLength(), Config.epsilon);
//        assertTrue("Vehicle is used", vehicle1.isUsed());
//    }
//
//    @Test
//    public void secondCustomerHasTwoPossibleInsertions() {
//        int firstCustomer = 1;
//        int secondCustomer = 69; // 69 is very close to 1 in instance
//
//        // first customer 1
//        ArrayList<double[]> possibleInsertions =  construction.getPossibleInsertionsForCustomer(firstCustomer);
//        double[] insertion = possibleInsertions.get(0);
//        construction.getVehicles().get((int) insertion[1]).applyInsertion(insertion, data);
//
//        // second customer 69
//        possibleInsertions =  construction.getPossibleInsertionsForCustomer(secondCustomer);
//
//        // there should be two insertions possible (vehicle 0 (with customer 1) and vehicle 1 (empty))
//        assertEquals("Number of possible insertions:", 2, possibleInsertions.size());
//
//        // for k = 2
//        int k = 2;
//        double calculateRegret = construction.calculateRegret(k, possibleInsertions);
//        // insertion vehicle 2 (after cust 1): dist(1,69) + dist(69,depot) - dist(1,depot)
//        // insertion vehicle 1 (empty): 2x distance to depot (12.2)
//        double manualValue = 12.2*2 - (4.5 + 12.2 - 15.2);
//        assertEquals("Regret value (k=2)", manualValue, calculateRegret, Config.epsilon);
//
//        // for k = 3
//        k = 3 ;
//        calculateRegret = construction.calculateRegret(k, possibleInsertions);
//        manualValue = Config.bigMRegret - (4.5 + 12.2 - 15.2);
//        assertEquals("Regret value (k=3)", manualValue, calculateRegret, Config.epsilon);
//    }
//
//    @Test
//    public void secondCustomerToDifferentRoute() {
//        // TODO Alex - hier Fall konstruieren, wo man wirklich die andere Route nehmen sollte (ggf. über Mocks, dass distance matrix ueberschrieben wird)
//        int firstCustomer = 17;
//        int secondCustomer = 23; // 17 is far away from 1 in instance
//
//        // first customer 17
//        ArrayList<double[]> possibleInsertions =  construction.getPossibleInsertionsForCustomer(firstCustomer);
//        double[] insertion = possibleInsertions.get(0);
//        construction.getVehicles().get((int) insertion[1]).applyInsertion(insertion, data);
//
//        // second customer 23
//        possibleInsertions =  construction.getPossibleInsertionsForCustomer(secondCustomer);
//
//        // there should be two insertions possible (vehicle 0 (with customer 1) and vehicle 1 (empty))
//        assertEquals("Number of possible insertions:", 2, possibleInsertions.size());
//
//        // for k = 2
//        int k = 2;
//        double calculateRegret = construction.calculateRegret(k, possibleInsertions);
//        // insertion vehicle 2 (after cust 17): dist(17,23) + dist(23,depot) - dist(17,depot)
//        // insertion vehicle 1 (empty): 2x distance to depot (36.1)
//        double manualValue = 36.1*2 - (55.9 + 36.1 - 30.4);
//        assertEquals("Regret value (k=2)", manualValue, calculateRegret, Config.epsilon);
//
//    }
//
//    // TODO Alex - Test, ob possibleInsertionsForCustomer auch wirklich nach groesstem Regret sortiert ist (ist das was vorne steht wirklich groesser?)?
//
//    // TODO Alex - checken, ob die Touren wirklich feasible sind (zumindest exemplarisch) --> ZU ALNS (dann alns mit 10 iterationen laufen lassen)


}
