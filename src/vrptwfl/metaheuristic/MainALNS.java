package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.data.OptimalSolutions;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.HospitalInstanceLoader;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.WriterUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Main class of adaptive large neighborhood search (ALNS) for solving
 * Vehicle Routing Problems with Time Windows and flexible locations (VRPTW-FL) 
 * problems
 */
public class MainALNS {
	
	// private FileWriter writer;
	private boolean isSolomonInstance;
	private String instanceName;
	
	/**
	 * Constructor of the MainALNS class. 
	 * A FileWriter object is initialized for results logging
	 */
	public MainALNS(String instanceName, boolean isSolomonInstance) {
		this.instanceName = instanceName;
		this.isSolomonInstance = isSolomonInstance;
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
    public void runALNS(Data data, String instanceName, String outFile, String outDir) throws ArgumentOutOfBoundsException {
    	System.out.println("Start processing:" + instanceName);
    	// --- INIT WRITERS ---
    	WriterUtils.initWriters(data, outDir, outFile, data.getInstanceName());
    	WriterUtils.initPenaltyCounts();
    	WriterUtils.initSummaryLog();
    	
        // --- INIT STEPS ---
    	setInstanceSpecificParameters(data.getnCustomers(), data.getMaxDistanceInGraph());

        // --- INITIAL SOLUTION ---
        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.constructSolution(2);
        // LOGGING infos of initial solution
        //WriterUtils.writeBacktrackingPenalties(solutionConstr.getListOfPenalties());
    	WriterUtils.writeTourCSV(WriterUtils.writerInitialTourCSV, solutionConstr);
    	WriterUtils.writePenaltyCount(0, solutionConstr);
    	WriterUtils.writeSummaryLog(0, solutionConstr, System.currentTimeMillis() - startTimeConstruction);
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

        System.out.println();
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
    private static void setInstanceSpecificParameters(int nCustomers, double maxDistance) {
        // Set lower bound for removals
    	int lb1 = Config.getInstance().lowerBoundRemovalsMax;
        int lb2 = (int) Math.round(nCustomers * Config.getInstance().lowerBoundRemovalsFactor);
        Config.getInstance().lowerBoundRemovals = Math.min(lb1,  lb2);

        // Set upper bound for removals
        int ub1 = Config.getInstance().upperBoundRemovalsMax;
        int ub2 = (int) Math.round(nCustomers * Config.getInstance().upperBoundRemovalsFactor);
        Config.getInstance().upperBoundRemovals = Math.min(ub1,  ub2);

        // set penalty (costs) for unserved customers
        Config.getInstance().penaltyUnservedCustomer = maxDistance * Config.getInstance().costUnservedCustomerViolation;
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
        	Config.getInstance().optimalityGapValue = gap;
        	System.out.println("Optimality Gap: " + gap);
        }        
        
        // Write result
        WriterUtils.writeSolomonResults(solutionALNS, instanceName, timeElapsed, gap);
        WriterUtils.writeFinalTour(WriterUtils.writerFinalTour, solutionALNS.getStringRepresentionSolution());
    	WriterUtils.writeTourCSV(WriterUtils.writerFinalTourCSV, solutionALNS);
    	WriterUtils.writeUnscheduledInfo(WriterUtils.writerUnscheduled, solutionALNS);
    }
    
    /**
     * Logging of result for hospital instances. Information being logged:
     * <name of instance>, <total costs>, <elapsed time> 
     * @param data current hospital data object
     * @param solutionALNS Solution object
     * @param timeElapsed elapsed time being logged
     */
    private void logResultHospital(Data data, Solution solutionALNS, long timeElapsed) {
        boolean exists = false;
        if (data.getCustomersToLocations().size() - 1 == 40) exists = true;
        else if (data.getCustomersToLocations().size() - 1 == 80) exists = true;
        else if (data.getCustomersToLocations().size() - 1 == 120) exists = true;
        else ; // no optimal value stored 
        
        
        double gap = -1;
        double optimalObjFuncVal = -1;
        if (exists) {
        	// Calculate optimality gap
    		optimalObjFuncVal = OptimalSolutions.optimalHospital.get(data.getInstanceName().substring(18));
    		gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());   
    		Config.getInstance().optimalityGapValue = gap;
    		System.out.println("Optimality Gap: " + gap);
        }
        
    	WriterUtils.writerHospitalResults(data, solutionALNS, timeElapsed, gap);
    	WriterUtils.writeFinalTour(WriterUtils.writerFinalTour, solutionALNS.getStringRepresentionSolution());
    	WriterUtils.writeTourCSV(WriterUtils.writerFinalTourCSV, solutionALNS);
    	WriterUtils.writeUnscheduledInfo(WriterUtils.writerUnscheduled, solutionALNS);
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
    	int mode = Integer.valueOf(args[0]);
    	String outDir = args[1];
    	        
        if (mode == 0) {
        	String instanceName = args[2];
        	int nCustomers = Integer.valueOf(args[3]);
        	String outFile = args.length > 4 ? args[4] : "results.txt";
        	Config.configFile = args.length > 5 ? ("resources/"+args[5]): ("resources/config.yaml"); 
        	runSingleInstance(instanceName, outFile, outDir, nCustomers);        	
        }
        else if (mode == 1) {
        	int nCustomers = Integer.valueOf(args[2]);
        	int numConfigs = Integer.valueOf(args[3]);
        	int numRunsPerConfig = Integer.valueOf(args[4]);
        	String outFile = args.length > 5 ? args[5] : "results.txt";
        	Config.configFile = String.valueOf(args.length > 6 ? ("resources/"+args[6]): "resources/config.yaml"); 
        	tuningParam(numConfigs, numRunsPerConfig, nCustomers, outDir, outFile);        	
        }
        else
        	System.out.println("Unknown mode - 1:run single instance; 2:run parameter tuning");
        	System.exit(0);
    }
    
    private static void runSingleInstance(String instanceName, String outFile, String outDir, int nCustomers) throws ArgumentOutOfBoundsException {
        // Define input params
        boolean isSolomonInstance = !instanceName.contains("hospital_instance"); // Boolean.parseBoolean(args[1]);
        // int nCustomers = 100; // TODO - for solomon instances as args param
        
        // Load Data
        Data[] data;
        if (isSolomonInstance)
        	data = loadSolomonInstance(instanceName, nCustomers);
        else
        	data = loadHospitalInstance(instanceName);

        // Run
    	for (Data d: data) {
        	final MainALNS algo = new MainALNS(instanceName, isSolomonInstance);
        	algo.runALNS(d, instanceName, outFile, outDir);        	
        	WriterUtils.writeConfig(WriterUtils.writerConfig, Config.getInstance());
        }
    	
        // TODO Alex: Add TimeLimit (?)
    }
    
    private static void tuningParam(int maxConfigs, int maxRuns, int nCustomers, String outDir, String outFile) throws ArgumentOutOfBoundsException {
    	String bestRun = "";
    	double bestAvgGap = Double.MAX_VALUE;

//    	String[] instanceNamesRandom = {
//    			"R104", "R108", "R111", "R112", "C105", "C106", "C107", "RC104", "RC106", "RC108" 
//    			/*"R101", "R102", "R103", "R104", "R105", "R106", "R107", "R108", "R109", "R110", "R111", "R112"*/
//    		};
    	
//    	String[] instanceNamesRandom = {
//    			"hospital_instance_i040_b1_f6_v01",
//    			"hospital_instance_i040_b2_f3_v05",
//    			"hospital_instance_i080_b2_f3_v03",
//    			"hospital_instance_i080_b6_f1_v04",
//    			"hospital_instance_i120_b6_f1_v02",
//    			"hospital_instance_i120_b1_f6_v01"    			
//    	};

    	String[] instanceNamesRandom = {
    			"hospital_instance_i120_b1_f6_v02",
    			"hospital_instance_i120_b1_f6_v03",
    			"hospital_instance_i120_b2_f3_v03",
    			"hospital_instance_i120_b2_f3_v05",
    			"hospital_instance_i120_b6_f1_v04" //,
    			//"hospital_instance_i100_b2_f3_v04",    			
    			//"hospital_instance_i100_b2_f3_v05",
    	};
    	
    	for (int config_idx = 0; config_idx < maxConfigs; config_idx++) {
    		Config.getInstance().randomizeConfig();
    		System.out.println("Start config id:" + config_idx);
    		double avg_optimalityGap = 0.0;
    		for (String instanceName : instanceNamesRandom) {
    			System.out.println("Start Processing instance: " + instanceName);
    	        boolean isSolomonInstance = !instanceName.contains("hospital_instance"); // Boolean.parseBoolean(args[1]);
    			
    			// Load Data
    	        Data[] dataArr;
    	        if (isSolomonInstance)
    	        	dataArr = loadSolomonInstance(instanceName, nCustomers);
    	        else
    	        	dataArr = loadHospitalInstance(instanceName);
    			

    			for (int run = 0; run < maxRuns; run ++) {
    				System.out.println("Run id: " + run);
    				for (Data data : dataArr) {
    					// --- INIT WRITERS ---
    					WriterUtils.initWriters(data, outDir, outFile, instanceName);
    					WriterUtils.initPenaltyCounts();
    					
    					// --- INIT STEPS ---
    					setInstanceSpecificParameters(data.getnCustomers(), data.getMaxDistanceInGraph());
    					
    					// --- INITIAL SOLUTION ---
    					ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
    					long startTimeConstruction = System.currentTimeMillis();
    					Solution solutionConstr = construction.constructSolution(2);
    					// Print initial solution
    					// printToConsole("Init solution", solutionConstr);
    					
    					// --- ALNS SOLUTION ---
    					ALNSCore alns = new ALNSCore(data);
    					Solution solutionALNS = alns.runALNS(solutionConstr);
    					long timeElapsed = (System.currentTimeMillis() - startTimeConstruction);
    					// Print ALNS(+GLS) solution
    					// printToConsole("ALNS solution", solutionALNS);
    					System.out.println("Time for construction " + timeElapsed + " ms.");
    					
    					if (isSolomonInstance) {    		        	
    						int i = -1;
    						if (nCustomers == 25) i = 0;
    						else if (nCustomers == 50) i = 1;
    						else if (nCustomers == 100) i = 2;
    						else ; // no optimal value stored 
    						// Calculate optimality gap
    						if (i!=-1) {
    							double optimalObjFuncVal = OptimalSolutions.optimalObjFuncValue.get(instanceName)[i];
    							double gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());
    							avg_optimalityGap += gap;
    							Config.getInstance().optimalityGapValue = gap; 
    							WriterUtils.writeConfig(WriterUtils.writerConfig, Config.getInstance());
        						System.out.println("Optimality Gap: " + gap);
    						}
    					}
    					else {
    						// Calculate optimality gap
    						double optimalObjFuncVal = -1;
    						double gap = -1;
    						optimalObjFuncVal = OptimalSolutions.optimalHospital.get(data.getInstanceName().substring(18));
    						gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());
    						avg_optimalityGap += gap;
    						Config.getInstance().optimalityGapValue = gap;
    						WriterUtils.writeConfig(WriterUtils.writerConfig, Config.getInstance());
    						System.out.println("Optimality Gap: " + gap);
    						System.out.println();
    					}
    				}
    			}
    		}
    		avg_optimalityGap /= (maxRuns * instanceNamesRandom.length);
    		
    		System.out.println();
    		System.out.println("=== SUMMARY ===");
    		System.out.println("Average optimality gap: " + avg_optimalityGap);
    		if (avg_optimalityGap < bestAvgGap) {
    			System.out.println("New best avg.opt.gap. Improved by:"+ (bestAvgGap - avg_optimalityGap));
    			bestAvgGap = avg_optimalityGap;
    			bestRun = WriterUtils.outDir;
    			Config.getInstance().avgOptimalityGapValue = avg_optimalityGap;
	    		FileWriter writer;
				try {
					writer = new FileWriter("./" + outDir + "/best_config.json");
					WriterUtils.writeConfig(writer, Config.getInstance());
					System.out.println("New best config written.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    		System.out.println("=== END SUMMARY ===");
    		System.out.println();
    	}
    	// System.out.println("Best run observed for:" + bestRun);
    	System.out.println("Avg gap:" + bestAvgGap);
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