package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.data.OptimalSolutions;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.HospitalInstanceLoader;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Main class of adaptive large neighborhood search (ALNS) for solving
 * Vehicle Routing Problems with Time Windows and flexible locations (VRPTW-FL) 
 * problems
 */
public class MainALNS {
	
	private FileWriter writer;
	
	/**
	 * Constructor of the MainALNS class. 
	 * A FileWrite object is initialized for results logging
	 */
	public MainALNS(String outputFile) {
		try {
			this.writer = new FileWriter("./"+outputFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main procedure for solving the ALNS on VRPTW-FL problems. 
	 * The function first calculates an initial solution which is then used as input 
	 * for the ALNS. The final routings as well as the optimality gap to the best
	 * known solution is printed on the standard output. The found solution for the 
	 * instance is also logged into a file defined in the constructor of the class: 
	 * Format of logging: instanceName, TotalCosts, OptimalityGap, ElapsedTime
	 * @param instanceName: instance name to be solved
	 * @param nCustomers: number of customers
	 * @throws ArgumentOutOfBoundsException
	 */
    public void runALNS(Data data, String instanceName) throws ArgumentOutOfBoundsException {

        this.setInstanceSpecificParameters(data.getnCustomers(), data.getMaxDistanceInGraph());

        // Initial Solution
        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.constructSolution(2);
        
        System.out.println("Init solution - not assigned customers   :" + solutionConstr.getNotAssignedCustomers());
        System.out.println("Init solution - temp infeasible customers:" + solutionConstr.getTempInfeasibleCustomers());
        System.out.println("Customers for scheduling:" + Arrays.toString(data.getOriginalCustomerIds()));
        solutionConstr.printSolution();
        System.exit(0);

        // ALNS
        ALNSCore alns = new ALNSCore(data);
        Solution solutionALNS = alns.runALNS(solutionConstr);

        System.out.println("Init solution - not assigned customers   :" + solutionALNS.getNotAssignedCustomers());
        System.out.println("Init solution - temp infeasible customers:" + solutionALNS.getTempInfeasibleCustomers());
        System.out.println("Customers for scheduling:" + Arrays.toString(data.getOriginalCustomerIds()));
        long finishTimeConstruction = System.currentTimeMillis();
        long timeElapsed = (finishTimeConstruction - startTimeConstruction);
        System.out.println("Time for construction " + timeElapsed + " ms.");

        // TODO Alex: brauchen irgendwas, um Lösung zu speichern (ZF und Touren startzeiten etc.)

        System.out.println(solutionALNS.getNotAssignedCustomers());
        System.out.println(solutionALNS.getTempInfeasibleCustomers());
        solutionALNS.printSolution();

        int i = -1;
        if (data.getnCustomers() == 25)		    i = 0;
        else if (data.getnCustomers() == 50)	i = 1;
        else if (data.getnCustomers() == 100)	i = 2;
        else return; // no optimal value stored --> Quit
        
        // Calculate optimality gap
        double optimalObjFuncVal = OptimalSolutions.optimalObjFuncValue.get(instanceName)[i];
        double gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());
        System.out.println("Gap: " + gap);
        
        // Write result
        try {
			writer.append(instanceName + "," + solutionALNS.getTotalCosts() + "," + gap + "," + timeElapsed + "\n");
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Sets the upper bound for number of removals in each ALNS iteration
     * (see Pisinger & Ropke 2007, C&OR §6.1.1 p. 2417)
     * @param nCustomers: number of customers
     * @param maxDistance: maximal distance in the input locations
     */
    private void setInstanceSpecificParameters(int nCustomers, double maxDistance) {
        int lb1 = Config.lowerBoundRemovalsMax;
        int lb2 = (int) Math.round(nCustomers * Config.lowerBoundRemovalsFactor);
        Config.lowerBoundRemovals = Math.min(lb1,  lb2);

        int ub1 = Config.upperBoundRemovalsMax;
        int ub2 = (int) Math.round(nCustomers * Config.upperBoundRemovalsFactor);
        Config.upperBoundRemovals = Math.min(ub1,  ub2);

        // set penalty (costs) for unserved customers
        Config.penaltyUnservedCustomer = maxDistance * Config.costUnservedCustomerViolation;
    }
    
    /**
     * Loads the attached solomon instance.
     * @param instanceName filename of solomon instance
     * @param nCustomers number of customers being scheduled for the solomon instance
     * @return array containing the data object in the 0-th position
     */
    public static Data[] loadSolomonInstance(String instanceName, int nCustomers) {
    	SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data[] data = new Data[1];
        try {
            data[0] = generator.loadInstance(instanceName + ".txt", nCustomers);
        }
        catch (ArgumentOutOfBoundsException | IOException e) { 
            e.printStackTrace();
        }		
        return data;
    }
    
    /**
     * Loads the attached hospital instance.
     * @param instanceName filename of hospital instance
     * @return array containing data objects (if solveAsTwoProblems is activate in config file -> array contains morning/evening data objects)
     */
    public static Data[] loadHospitalInstance(String instanceNam) {
    	HospitalInstanceLoader loader = new HospitalInstanceLoader();
        Data[] dataArr;
        dataArr = loader.loadHospitalInstanceFromJSON("hospital_instance_i060_b1_f6_v01");
        return dataArr;
    }

    /**
     * Entry point of the program. Calls the runALNS function with the attached
     * instance being defined as first args parameter.
     * @param args: args parameter (instance name to be solved)
     * @throws ArgumentOutOfBoundsException
     */
    public static void main(String[] args) throws ArgumentOutOfBoundsException {
        String instanceName = args[0];
        boolean isSolomonInstance = Boolean.parseBoolean(args[1]);
        int nCustomers = 25;
        String outFile = args.length > 1 ? args[1] : "results.txt";
        
        Data[] data;
        if (isSolomonInstance)
        	data = loadSolomonInstance(instanceName, nCustomers);
        else
        	data = loadHospitalInstance(instanceName);
        
    	final MainALNS algo = new MainALNS(outFile);
    	algo.runALNS(data[0], instanceName);        	
//        for (Data d: data) {
//        	final MainALNS algo = new MainALNS(outFile);
//        	algo.runALNS(d, instanceName);        	
//        }
        // Alex: Add TimeLimit (?)
    }
    
    //
    // ### TODOs ###
    //
    
    // TODO Alex : performance
    // - LRU cache (last recent usage)
    
    // TODO Chris - 23.02.2022
    // ###
    // - [v] simulated annealing  [Ropke&Pisinger, p.2416 COR]  // alter code könnte helfen
    // - [v] adaptive Komponente: Wahrscheinlichkeit von destroy und insertion 
    //     nicht mehr uniformly distributed, sondern Wahrscheinlichkeit nach historischem 
    //     Erfolg p- und p+ (sigma-Werte)
    // - [v] hashcode für einzelne solutions
    // 
    // !!Tracking für die profs; Was sind meine contributions!!
    // - [v] Prüfen ob alle Operatoren Von Pisinger&Ropke mit aufgenommen worden sind für VRPTW
    //   - wenn ja: top
    //   - wenn nein, -> Implementieren!
    // ###

    // TODO Alex : morgen früh 28.05.2021
    //  1) Min- und Max-Anzahl removals pro iteration (siehe ALNS Paper)
    //  2) Test Vehicles
    //  3) Test Construction
    //  4) ggf. weiter Tests, wenn Solution object anders aussieht nach ALNS

    // TODO Alex - 2: tests für geladene instanzen
    
    // TODO Alex - 3: Logik ALNS anfangen (50_000 iteration random destroy, und regret repairs)

    // TODO Alex - 4: greedy repair

    // TODO Alex - moegliches hashing
    //  - bereits generierte Loesungen
    //  - ggf. earliest, latest possible starts in partial routes (pred_id, pred_time,)

}
