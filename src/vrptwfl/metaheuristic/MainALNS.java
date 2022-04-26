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
	private boolean isSolomonInstance;
	private String instanceName;
	
	/**
	 * Constructor of the MainALNS class. 
	 * A FileWriter object is initialized for results logging
	 */
	public MainALNS(String instanceName, String outputFile, boolean isSolomonInstance) {
		this.instanceName = instanceName;
		this.isSolomonInstance = isSolomonInstance;
		initFileWriter(outputFile);
	}
	
	/**
	 * Initialize a file writer for logging of results. 
	 * @param outputFile name of output file
	 */
	private void initFileWriter(String outputFile) {
		try {
			this.writer = new FileWriter("./out/"+outputFile, true);
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
        // --- INIT STEPS ---
    	this.setInstanceSpecificParameters(data.getnCustomers(), data.getMaxDistanceInGraph());

        // --- INITIAL SOLUTION ---
        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.constructSolution(2);
        // Print initial solution
        printToConsole("Init solution", solutionConstr);

        // --- ALNS SOLUTION ---
        ALNSCore alns = new ALNSCore(data);
        Solution solutionALNS = alns.runALNS(solutionConstr);
        long timeElapsed = (System.currentTimeMillis() - startTimeConstruction);
        // Print ALNS(+GLS) solution
        printToConsole("ALNS solution", solutionALNS);
        System.out.println("Time for construction " + timeElapsed + " ms.");

        // --- LOGGING ---
        if (this.isSolomonInstance)
        	logResultSolomon(data, solutionALNS, timeElapsed);
        else 
        	logResultHospital(data, solutionALNS, timeElapsed);

        // TODO Alex: brauchen irgendwas, um Lösung zu speichern (ZF und Touren startzeiten etc.)
    }

    /**
     * Sets the upper bound for number of removals in each ALNS iteration and the
     * penalty of unserved customers by multiplying the costs of unserved customers
     * with the maximal distance between all locations.
     * (see Pisinger & Ropke 2007, C&OR §6.1.1 p. 2417)
     * @param nCustomers: number of customers
     * @param maxDistance: maximal distance in the input locations
     */
    private void setInstanceSpecificParameters(int nCustomers, double maxDistance) {
        // Set lower bound for removals
    	int lb1 = Config.lowerBoundRemovalsMax;
        int lb2 = (int) Math.round(nCustomers * Config.lowerBoundRemovalsFactor);
        Config.lowerBoundRemovals = Math.min(lb1,  lb2);

        // Set upper bound for removals
        int ub1 = Config.upperBoundRemovalsMax;
        int ub2 = (int) Math.round(nCustomers * Config.upperBoundRemovalsFactor);
        Config.upperBoundRemovals = Math.min(ub1,  ub2);

        // set penalty (costs) for unserved customers
        Config.penaltyUnservedCustomer = maxDistance * Config.costUnservedCustomerViolation;
    }
    
    /**
     * Loads the attached solomon instance with n customers.
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
    private static Data[] loadHospitalInstance(String instanceName) {
    	HospitalInstanceLoader loader = new HospitalInstanceLoader();
        Data[] dataArr;
        dataArr = loader.loadHospitalInstanceFromJSON(instanceName);
        return dataArr;
    }
    
    /**
     * Logging of result for solomon instances. Information being logged:
     * <name of instance>, <total costs>, <elapsed time>, <optimality gap>
     * @param data current solomon data object
     * @param solutionALNS Solution object
     * @param timeElapsed elapsed time being logged
     */
    private void logResultSolomon(Data data, Solution solutionALNS, long timeElapsed) {
        int i = -1;
        if (data.getnCustomers() == 25) i = 0;
        else if (data.getnCustomers() == 50) i = 1;
        else if (data.getnCustomers() == 100) i = 2;
        else ; // no optimal value stored 
        
        // Calculate optimality gap
        double optimalObjFuncVal = -1;
        double gap = -1;
        if (i!=-1) {
        	optimalObjFuncVal = OptimalSolutions.optimalObjFuncValue.get(this.instanceName)[i];
        	gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());        	
        	System.out.println("Optimality Gap: " + gap);
        }        
        
        // Write result
        try {
			writer.append(instanceName + "," + solutionALNS.getTotalCosts() + "," + timeElapsed + "," + gap + "\n");
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Logging of result for hospital instances. Information being logged:
     * <name of instance>, <total costs>, <elapsed time> 
     * @param data current hospital data object
     * @param solutionALNS Solution object
     * @param timeElapsed elapsed time being logged
     */
    private void logResultHospital(Data data, Solution solutionALNS, long timeElapsed) {
    	try {
    		writer.append(data.getInstanceName() + "," + solutionALNS.getTotalCosts() + "," + timeElapsed + "\n");
    		writer.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Prints the solution to the standard output console
     * @param prefix Prefix being used for the output
     * @param s: solution object
     */
    private void printToConsole (String prefix, Solution s) {
        System.out.println(prefix + " - not assigned customers   :" + s.getNotAssignedCustomers());
        System.out.println(prefix + " - temp infeasible customers:" + s.getTempInfeasibleCustomers());
        System.out.println("Customers for scheduling:" + Arrays.toString(s.getData().getOriginalCustomerIds()));
        s.printSolution();
    }

    /**
     * Entry point of the program. Calls the runALNS function with the attached
     * instance being defined as first args parameter.
     * @param args: args parameter (instance name to be solved)
     * @throws ArgumentOutOfBoundsException
     */
    public static void main(String[] args) throws ArgumentOutOfBoundsException {
        // Parse args parameters
    	String instanceName = args[0];
        String outFile = args.length > 1 ? args[1] : "results.txt";

        // Define input params
        boolean isSolomonInstance = !instanceName.contains("hospital_instance"); // Boolean.parseBoolean(args[1]);
        int nCustomers = 100; // TODO - for solomon instances as args param
        
        // Load Data
        Data[] data;
        if (isSolomonInstance)
        	data = loadSolomonInstance(instanceName, nCustomers);
        else
        	data = loadHospitalInstance(instanceName);
        
    	final MainALNS algo = new MainALNS(instanceName, outFile, isSolomonInstance);
    	algo.runALNS(data[1], instanceName);        	
        // Run
//    	for (Data d: data) {
//        	final MainALNS algo = new MainALNS(instanceName, outFile, isSolomonInstance);
//        	algo.runALNS(d, instanceName);        	
//        }
        // TODO Alex: Add TimeLimit (?)
    }

    
    // ### old TODOs ###
    //
    // TODO Alex - 0: performance
    // - LRU cache (last recent usage)
    // TODO Alex - 1: morgen früh 28.05.2021
    //  1) Min- und Max-Anzahl removals pro iteration (siehe ALNS Paper)
    //  2) Test Vehicles
    //  3) Test Construction
    //  4) ggf. weiter Tests, wenn Solution object anders aussieht nach ALNS
    // TODO Alex - 2: tests für geladene instanzen
    // TODO Alex - 3: Logik ALNS anfangen (50_000 iteration random destroy, und regret repairs)
    // TODO Alex - 4: greedy repair
    // TODO Alex - 5: moegliches hashing
    //  - bereits generierte Loesungen
    //  - ggf. earliest, latest possible starts in partial routes (pred_id, pred_time,)
}