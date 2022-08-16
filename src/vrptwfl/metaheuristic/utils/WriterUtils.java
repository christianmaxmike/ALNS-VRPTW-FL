package vrptwfl.metaheuristic.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.removals.AbstractRemoval;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

/**
 * This class implements writers for logging. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class WriterUtils {
	
	public static FileWriter writerRemovals;
	public static FileWriter writerRepairs;
	public static FileWriter writerPenalties;
	public static FileWriter writerResults;
	public static FileWriter writerConfig;
	public static FileWriter writerFinalTour;
	public static FileWriter writerFinalTourCSV;
	public static FileWriter writerInitialTourCSV;
	public static FileWriter writerBacktracking;
	public static FileWriter writerProcessLog;
	public static FileWriter writerSummary;
	public static FileWriter writerUnscheduled;
	public static FileWriter writerPenaltiesDetailed;
	public static FileWriter writerAllTours;
	public static FileWriter writerIndividualPenalties;
	public static String outDir;
	
	// private static JSONArray penaltiesInfo = new JSONArray();
	private static JSONObject penaltiesInfo = new JSONObject();
	private static JSONObject allTours = new JSONObject();
	
	/**
	 * Initialization of the writers. 
	 * @param data: data object
	 * @param parentDir: parent directory where the files are stored
	 * @param outputFile: output file
	 * @param instanceName: name of the current instance
	 */
	public static void initWriters(Data data, String parentDir, String outputFile, String instanceName) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm");  
        Date date = new Date();  
        System.out.println(formatter.format(date));  

        outDir = "./"+ parentDir +"/run_"+instanceName+ "/"; //"_" + formatter.format(date) + "/";
        File f = new File(outDir);
        f.mkdirs();
        
		// Initialize Writers - TO FILE
		try {
			writerRemovals = new FileWriter(outDir + "removalProbabilities.txt", true);
			writerRepairs = new FileWriter(outDir + "" + "repairProbabilities.txt", true);
			writerPenalties = new FileWriter(outDir + "" + "logPenalties.txt");
			writerResults = new FileWriter("./"+ parentDir +"/" + outputFile, true);
			writerConfig = new FileWriter(outDir + "config.json");
			writerFinalTour = new FileWriter(outDir + "finalTour.txt");
			writerFinalTourCSV = new FileWriter(outDir + "finalTourCSV.csv");
			writerInitialTourCSV = new FileWriter(outDir + "initialTourCSV.csv");
			writerBacktracking = new FileWriter(outDir + "backTrackingLog.txt");
			//writerBacktrackingPenalties = new FileWriter(outDir + "InitialPenalties.txt");
			writerProcessLog = new FileWriter(outDir + "logCosts.txt");
			writerSummary = new FileWriter(outDir + "summary.txt");
			writerUnscheduled = new FileWriter(outDir + "unscheduledInfo.csv");
			writerPenaltiesDetailed = new FileWriter(outDir + "penaltiesDetailed.json");
			writerAllTours = new FileWriter(outDir + "allTours.json");
			writerIndividualPenalties = new FileWriter(outDir + "individualPenalties.json");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// INTIIALE SINGLE WRITERS - HEADERS
	/**
	 * Initialization of the writer for logging removal ops' probabilities.
	 * @param writer: Writer which logs the removal ops' probabilities
	 * @param operators: removal operators
	 */
	public static void initWriterRemovalProbabilities(FileWriter writer, AbstractRemoval[] operators) {
		StringBuilder builder = new StringBuilder("iteration");
		int i = 0;
		do {
			builder.append(";");
			builder.append(operators[i].getFormattedClassName());
			i++;
		} while (i<operators.length);
		try {
			writer.write(builder.toString() + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initilization of the writer for logging insertion ops' probabilities.
	 * @param writer: Writer which logs the insertion ops' probabilities
	 * @param operators: ineration operators
	 */
	public static void initWriterRepairProbabilities(FileWriter writer, AbstractInsertion[] operators) {
		StringBuilder builder = new StringBuilder("iteration");
		int i = 0;
		do {
			builder.append(";");
			builder.append(operators[i].getFormattedClassName());
			i++;
		} while (i<operators.length);
		try {
			writer.write(builder.toString() + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialization of the summary writer.
	 */
	public static void initSummaryLog() {
		try {
			writerSummary.write("iteration;instanceName;nCustomers;nVehicles;nVehiclesUsed;notScheduledCustomers;elapsedTime;totalCosts;RoutingCosts;PenaltyCosts;SwappingCosts\n");
			writerSummary.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialization of the processing log writer.
	 */
	public static void initProcessLog() {
		try {
			writerProcessLog.write("instanceName;iteration;GlobalCosts;TmpCosts;CurrCosts;BestFeasibleCosts;isFeasible;timeElapsed;temperature;simulatedAnnealingRandomVal;DestroyOp;InsertionOp;nRemovals;GlobalCosts_var;TmpCosts_var;CurrCosts_var;BestFeasible_var\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialization of the penalties writer.
	 */
	public static void initPenaltyCounts() {
		StringBuilder builder = new StringBuilder("iteration;total");
		int i = 0;
		do {
			builder.append(";");
			builder.append(DataUtils.PenaltyIdx.values()[i]);
			i++;
		} while (i<DataUtils.PenaltyIdx.values().length);
		builder.append(";cumulatedTWDelta;cumulatedSkillDelta;swappingCosts");
		try {
			writerPenalties.write(builder.toString() + "\n");
			writerPenalties.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialization of the backtracking logging.
	 */
	public static void initBacktrackingLogging() {
		StringBuilder builder = new StringBuilder("trial;noJumps;bestCosts;time\n");
		try {
			writerBacktracking.write(builder.toString());
			writerBacktracking.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	// WRITE INFOS
	/**
	 * Write probabilities of the removal operators being drawn. 
	 * @param writer: FileWriter which logs the removal ops' probabilities
	 * @param removals: removal operators
	 * @param iterationNumber: the current iteration number
	 */
	public static void writeRemovalProbabilities(FileWriter writer, AbstractRemoval[] removals, int iterationNumber) {
		StringBuilder builder = new StringBuilder("" + iterationNumber);
		int i = 0;
		do {
			builder.append(";");
			builder.append(removals[i].getProbability());
			i++;
		} while (i<removals.length);
		try {
			writer.write(builder.toString() + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write probabilities of the insertion operators being drawn.
	 * @param writer: FileWriter which logs the insertion ops' probabilities
	 * @param repairs: insertion operators
	 * @param iterationNumber: the current iteration number
	 */
	public static void writeRepairProbabilities(FileWriter writer, AbstractInsertion[] repairs, int iterationNumber) {
		StringBuilder builder = new StringBuilder("" + iterationNumber);
		int i = 0;
		do {
			builder.append(";");
			builder.append(repairs[i].getProbability());
			i++;
		} while (i<repairs.length);
		try {
			writer.write(builder.toString() + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the configuration file.
	 * @param writer: Writer which logs the configuration file
	 * @param config: Configuration file being logged
	 */
	public static void writeConfig(FileWriter writer, Config config) {
		// Gson gson = new Gson();
	    GsonBuilder gsonBuilder  = new GsonBuilder();
	    // Allowing the serialization of static fields    
	    gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC);
	    // Creates a Gson instance based on the current configuration
	    Gson gson = gsonBuilder.setPrettyPrinting().create();
	      
	    // FileWriter fileWriter;
		try {
			//fileWriter = new FileWriter(outfile);
			gson.toJson(config, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the summary of an iteration.
	 * @param iteration: the current iteration number
	 * @param s: the current solution object
	 * @param elapsedTime: the elapsed time
	 */
	public static void writeSummaryLog (int iteration, Solution s, long elapsedTime) {
		// iteration;instanceName;nCustomers;nVehicles;nVehiclesUsed;notScheduledCustomers;elapsedTime;totalCosts;RoutingCosts;PenaltyCosts
		try {
			writerSummary.append(iteration + ";" + s.getData().getInstanceName() + ";" + s.getData().getnCustomers() 
					+ ";" + s.getData().getnVehicles() + ";" + s.getUsedVehicles().size() + ";" + s.getNotAssignedCustomers().size() + ";"
					+ Math.round(elapsedTime) + ";" + s.getTotalCosts() + ";" + s.getVehicleTourCosts() + ";" + s.getTotalPenalyCosts() + ";" + s.getSwappingCosts() + "\n");
			writerSummary.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the information of the current processing state:
	 * @param solutionGlobal: global solution object
	 * @param solutionTemp: temporary solution object
	 * @param solutionCurr: current solution object
	 * @param solutionBestFeasible: best feasible solution object
	 * @param instanceName: current instance name being processed
	 * @param iteration: the current iteration number
	 * @param timeElapsed: the elapsed time
	 * @param temperature: the temperature in the current iteration
	 * @param simulatedAnnealingRandomVal: value for simulated annealing in the current iteration
	 * @param destroyOp: the selected destroy operation
	 * @param insertionOp: the selected insertion operation
	 * @param nRemovals: the number of customer removals in the current iteration
	 */
	public static void writeProcessLog(Solution solutionGlobal, Solution solutionTemp, Solution solutionCurr, Solution solutionBestFeasible, 
			String instanceName, int iteration, long timeElapsed, double temperature, double simulatedAnnealingRandomVal, 
			AbstractRemoval destroyOp, AbstractInsertion insertionOp, int nRemovals) {
		try {
			solutionGlobal.calculateTotalCosts(false);
			double solutionGlobalVariable = solutionGlobal.getTotalCosts();
			solutionGlobal.calculateTotalCosts(true);
			double solutionGlobalFixed = solutionGlobal.getTotalCosts();
			
			solutionTemp.calculateTotalCosts(true);
			double solutionTempFixed = solutionTemp.getTotalCosts();
			solutionTemp.calculateTotalCosts(false);
			double solutionTempVariable = solutionTemp.getTotalCosts();
			
			solutionCurr.calculateTotalCosts(true);
			double solutionCurrFixed = solutionCurr.getTotalCosts();
			solutionCurr.calculateTotalCosts(false);
			double solutionCurrVariable = solutionCurr.getTotalCosts();
			
			double solutionBestFeasibleFixed = -1;
			double solutionBestFeasibleVariable = -1;
			if (solutionBestFeasible.isFeasible()) {
				solutionBestFeasible.calculateTotalCosts(true);
				solutionBestFeasibleFixed = solutionBestFeasible.getTotalCosts();
				solutionBestFeasible.calculateTotalCosts(false);
				solutionBestFeasibleVariable = solutionBestFeasible.getTotalCosts();
			}
			
			writerProcessLog.append(instanceName + ";" + iteration + ";" + 
					solutionGlobalFixed + ";" + solutionTempFixed + ";" + solutionCurrFixed + ";" + solutionBestFeasibleFixed + ";" + 
					solutionGlobal.isFeasible() + ";" + timeElapsed + ";" + temperature + ";" + simulatedAnnealingRandomVal + ";" + 
					(destroyOp != null ? destroyOp.getFormattedClassName(): "null") + ";" + 
					(insertionOp != null ? insertionOp.getFormattedClassName(): "null") + ";" + nRemovals + ";" +
					solutionGlobalVariable + ";" + solutionTempVariable + ";" + solutionCurrVariable + ";" + solutionBestFeasibleVariable +
					"\n");
			writerProcessLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write results for the solomon instance.
	 * @param solutionALNS: solution object after the ALNS procedure
	 * @param instanceName: current solomon instance being processed
	 * @param timeElapsed: elapsed time
	 * @param gap: optimality gap if available
	 */
	public static void writeSolomonResults(Solution solutionALNS, String instanceName, long timeElapsed, double gap) {
        try {
			writerResults.append(instanceName + ";" + solutionALNS.getTotalCosts() + ";" + timeElapsed + ";" + gap + "\n");
	        writerResults.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write results for the hospital instance
	 * @param data: data object
	 * @param solutionALNS: solution object after the ALNS procedure
	 * @param timeElapsed: elapsed time
	 * @param gap: optimality gap if available
	 */
	public static void writerHospitalResults(Data data, Solution solutionALNS, long timeElapsed, double gap) {
		try {
			writerResults.append(data.getInstanceName() + ";" + solutionALNS.getTotalCosts() + ";" + timeElapsed + ";" + gap + "\n");
			writerResults.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writer information of unscheduled customers.
	 * @param writer: FileWriter which logs the information about unscheduled customers
	 * @param s: solution object whose information is logged
	 */
	public static void writeUnscheduledInfo(FileWriter writer, Solution s) {
		try {
			writer.write("customer;originalCustomerID;customersStartTime;customersEndTime;serviceTime;preferredLocation\n");
			writer.flush();
			
			for (Integer notAssigned : s.getNotAssignedCustomers()) {
				double startTime = s.getData().getEarliestStartTimes()[notAssigned];
				double endTime = s.getData().getLatestStartTimes()[notAssigned];
				int preferredLoc = s.getData().getCustomersPreferredLocation()[notAssigned];
				double serviceDuration = s.getData().getServiceDurations()[notAssigned];
				int originalCustomerID = s.getData().getOriginalCustomerIds()[notAssigned];
				writer.append(notAssigned + ";" + originalCustomerID + ";" + startTime + ";" + endTime + ";" + serviceDuration + ";" + preferredLoc + "\n");
				writer.flush();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
//	public static void writePenaltyCounts(int iteration, double[] penaltyCnts) {
//		StringBuilder builder = new StringBuilder("" + iteration);
//		int i = 0;
//		do {
//			builder.append(";");
//			builder.append(penaltyCnts[i]);
//			i++;
//		} while (i<penaltyCnts.length);
//		try {
//			writerPenalties.write(builder.toString() + "\n");
//			writerPenalties.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Write count of penalties.
	 * @param iteration: current iteration number
	 * @param solution: solution object
	 */
	public static void writePenaltyCount(int iteration, Solution solution) {
		int[] penaltyArr = new int[5];
		int sumPenalty = 0;
		for (int[] entry: solution.getListOfPenalties()) {
			penaltyArr[entry[0]] ++;
			sumPenalty ++;
		}
		StringBuilder builder = new StringBuilder(iteration+ ";" + sumPenalty);
		int i = 0;
		do {
			builder.append(";");
			builder.append(penaltyArr[i]);
			i++;
		} while (i<penaltyArr.length);
		builder.append(";" + solution.getCumDeltaTW());
		builder.append(";" + solution.getCumDeltaskill());
		builder.append(";" + solution.getSwappingCosts());
		try {
			writerPenalties.write(builder.toString() + "\n");
			writerPenalties.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the resulting scheduling as a formatted csv file.
	 * @param writer: FileWriter which logs the tour information as csv
	 * @param s: solution object
	 */
	public static void writeTourCSV(FileWriter writer, Solution s) {
		/*
		 * vehicleID, customerID, OriginalCustomerId, servedLoc, preferredLoc, capacity, duration, starttime, endtime, travelTimePred, travelTimeSucc
		 */
		try {
			writer.write("vehicleID;customerID;originalCustomerID;servedLoc;preferredLoc;capacitySlot;duration;starttime;endtime;customersStartTime;customersEndTime;distFrom;distTo;distToPreferredLoc\n");
			for (Vehicle v: s.getVehicles()) {
				int vehicleId = v.getId();
				for (int i = 1; i < v.getCustomers().size() - 1 ; i++) {
					int customerId = v.getCustomers().get(i);
					int originalCustomerId = s.getData().getOriginalCustomerIds()[customerId];
					int servedLoc = DataUtils.getLocationIndex(customerId, s);
					int preferredLoc = s.getData().getCustomersPreferredLocation()[customerId];
					int capacitySlot = s.getCustomerAffiliationToCapacity()[customerId];
					int duration = s.getData().getServiceDurations()[customerId];
					double startService = v.getStartOfServices().get(i);
					double endService = v.getEndOfServices().get(i);
					double customersStartTime = s.getData().getEarliestStartTimes()[customerId];
					double customersEndTime = s.getData().getLatestStartTimes()[customerId];
					
		            int locPred = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i-1)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i-1)]);
		            int locCurr = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i)]);
		            int locSucc = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i+1)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i+1)]);
		            double distPred = s.getData().getDistanceBetweenLocations(locPred, locCurr);
		            double distSucc = s.getData().getDistanceBetweenLocations(locCurr, locSucc);
		            
		            double distToPreferredLoc = s.getData().getDistanceBetweenLocations(servedLoc, preferredLoc);
					
					writer.write(vehicleId + ";" + customerId + ";" + originalCustomerId + ";" + 
							     servedLoc  + ";" + preferredLoc + ";" + capacitySlot  + ";" +
							     duration + ";" + startService + ";" + endService + ";" +
							     customersStartTime + ";" + customersEndTime + ";" + 
							     distPred + ";" + distSucc + ";" + distToPreferredLoc + "\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the final tour as un-formatted text.
	 * @param writer: FileWriter which logs the un-formatted tour
	 * @param tour: string representation of the current scheduling
	 */
	public static void writeFinalTour(FileWriter writer, String tour) {
		try {
			writer.write(tour);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Logging for the backtracking procedure.
	 * @param trial: current backtracking trial
	 * @param jumps: number of backtracking jumps
	 * @param costs: costs of the solution
	 * @param time: elapsed time
	 */
	public static void writeBacktrackingInfo (int trial, int jumps, double costs, long time) {
		try {
			writerBacktracking.write(trial + ";" + jumps + ";" + costs + ";" + time + "\n");
			writerBacktracking.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper function for tracking a tour information.
	 * The method extracts the current scheduling and stores it in the class variable
	 * 'allTours'.
	 * 
	 * @param it: current iteration number
	 * @param s: solution object
	 */
	public static void addTourInformation (Integer it, Solution s) {
		JSONObject tourInfo = new JSONObject();
		for (Vehicle v: s.getVehicles()) {
			// JSONObject vehicleInfo = new JSONObject();

			int vehicleId = v.getId();
			JSONArray customersArrInfo = new JSONArray();
			
			for (int i = 1; i < v.getCustomers().size() - 1 ; i++) {
				// JSONObject customerInfo = new JSONObject();
				JSONArray customerInfo = new JSONArray();

				int customerId = v.getCustomers().get(i);
				int originalCustomerId = s.getData().getOriginalCustomerIds()[customerId];
				int servedLoc = DataUtils.getLocationIndex(customerId, s);
				int preferredLoc = s.getData().getCustomersPreferredLocation()[customerId];
				int capacitySlot = s.getCustomerAffiliationToCapacity()[customerId];
				int duration = s.getData().getServiceDurations()[customerId];
				double startService = v.getStartOfServices().get(i);
				double endService = v.getEndOfServices().get(i);
				double customersStartTime = s.getData().getEarliestStartTimes()[customerId];
				double customersEndTime = s.getData().getLatestStartTimes()[customerId];
				
	            int locPred = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i-1)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i-1)]);
	            int locCurr = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i)]);
	            int locSucc = s.getData().getCustomersToLocations().get(s.getData().getOriginalCustomerIds()[v.getCustomers().get(i+1)]).get(s.getCustomerAffiliationToLocations()[v.getCustomers().get(i+1)]);
	            double distPred = s.getData().getDistanceBetweenLocations(locPred, locCurr);
	            double distSucc = s.getData().getDistanceBetweenLocations(locCurr, locSucc);
	            
	            double distToPreferredLoc = s.getData().getDistanceBetweenLocations(servedLoc, preferredLoc);
				
	            
	            /*
	            customerInfo.put("customerId", customerId);
	            customerInfo.put("originalCustomerId", originalCustomerId);
	            customerInfo.put("PosInRoute", i);
	            customerInfo.put("servedLocation", servedLoc);
	            customerInfo.put("preferredLocation", preferredLoc);
	            customerInfo.put("capacitySlot", capacitySlot);
	            customerInfo.put("duration", duration);
	            customerInfo.put("startServiceTime", startService);
	            customerInfo.put("endServiceTime", endService);
	            customerInfo.put("customersStartPlaningHorizon", customersStartTime);
	            customerInfo.put("customersEndPlaningHorizon", customersEndTime);
	            customerInfo.put("distPredLoc", distPred);
	            customerInfo.put("distSuccLoc", distSucc);
	            customerInfo.put("distToPreferredLoc", distToPreferredLoc);
	            */
	            
	            customerInfo.add(customerId);
	            customerInfo.add(originalCustomerId);
	            customerInfo.add(i);
	            customerInfo.add(servedLoc);
	            customerInfo.add(preferredLoc);
	            customerInfo.add(capacitySlot);
	            customerInfo.add(duration);
	            customerInfo.add(startService);
	            customerInfo.add(endService);
	            customerInfo.add(customersStartTime);
	            customerInfo.add(customersEndTime);
	            customerInfo.add(distPred);
	            customerInfo.add(distSucc);
	            customerInfo.add(distToPreferredLoc);
	            
	            
	            double[] info = new double[]{customerId, originalCustomerId, i, servedLoc, preferredLoc,
	            		capacitySlot, duration, startService, endService, customersStartTime,
	            		customersEndTime, distPred, distSucc, distToPreferredLoc};
	            
	            customersArrInfo.add(customerInfo);
	            // vehicleInfo.put(customerId, customerInfo);
			}
			
			//tourInfo.put(vehicleId, vehicleInfo);
			tourInfo.put(vehicleId, customersArrInfo);
		}
		allTours.put(it, tourInfo);
	}
	
	
	/**
	 * Write out all tours being stored in the field variable 'allTours'.
	 */
	public static void writeAllTourInformation () {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(allTours.toJSONString());
			String prettyJsonString = gson.toJson(je);
			writerAllTours.write(prettyJsonString);
			writerAllTours.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Helper function for tracking the penalty feature vectors.
	 * The method iterates the current GLS penalties and stores the in the class variable
	 * 'penaltiesInfo'.
	 * 
	 * @param it: current iteration number
	 * @param s: solution object
	 */
	public static void addToPenaltiesInformation (Integer it, Solution s) {
		JSONObject customerCollection = new JSONObject();
		for (int customer = 0; customer<s.getData().getGLSPenalties()[0].length; customer ++) {
			JSONObject customerObj = new JSONObject();
			JSONObject penaltyObj = new JSONObject();
			for (int penaltyIdx = 0; penaltyIdx<s.getData().getGLSPenalties().length; penaltyIdx++) {
				penaltyObj.put(DataUtils.PenaltyIdx.values()[penaltyIdx],
							   s.getData().getGLSPenalties()[penaltyIdx][customer]
							   );
				penaltyObj.put("originalCustomerID", s.getData().getOriginalCustomerIds()[customer]);
				
			}
			customerCollection.put(String.valueOf(customer), penaltyObj);
		}
		penaltiesInfo.put(it, customerCollection);
	}
	
	/**
	 * Write out the penalty feature vectors being stored in the field variable 'penaltiesInfo'.
	 */
	public static void writePenaltiesDetailedInformation () {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(penaltiesInfo.toJSONString());
			String prettyJsonString = gson.toJson(je);
			writerPenaltiesDetailed.write(prettyJsonString);
			writerPenaltiesDetailed.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initializeIndividualPenaltiesLogging () {
		try {
			writerIndividualPenalties.write("iteration;penalty;customerID\n");
			writerIndividualPenalties.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void writeIndividualPenalties(Integer iteration, Solution solution) {
		for (int[] penaltyInfo : solution.getListOfPenalties()) {
			try {
				writerIndividualPenalties.write(iteration + ";" + 
											    DataUtils.PenaltyIdx.values()[penaltyInfo[0]] +  ";" + 
												penaltyInfo[1] + "\n");
				writerIndividualPenalties.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
//	public static void writeBacktrackingPenalties (ArrayList<int[]> listOfPenalties) {
//		int[] noPenalties = new int[DataUtils.PenaltyIdx.values().length];
//		int sum = listOfPenalties.size();
//		for (int[] entry: listOfPenalties) 
//			noPenalties[entry[0]] ++;
//		
//		StringBuilder builder = new StringBuilder("total");
//		for (DataUtils.PenaltyIdx idx: DataUtils.PenaltyIdx.values()) 
//			builder.append(";" + idx.name());
//		
//		builder.append("\n");
//		builder.append(sum);
//		for (int penaltycnt : noPenalties)
//			builder.append(";" + penaltycnt);
//		
//		builder.append("\n");
//		try {
//			writerBacktrackingPenalties.write(builder.toString());
//			writerBacktrackingPenalties.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
