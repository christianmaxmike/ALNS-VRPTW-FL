package vrptwfl.metaheuristic.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.removals.AbstractRemoval;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

public class WriterUtils {
	
	public static FileWriter writerRemovals;
	public static FileWriter writerRepairs;
	public static FileWriter writerPenalties;
	public static FileWriter writerResults;
	public static FileWriter writerConfig;
	public static FileWriter writerFinalTour;
	public static FileWriter writerBacktracking;
	public static String outDir;
	
	public static void initWriters(Data data, String parentDir, String outputFile, String instanceName) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm");  
        Date date = new Date();  
        System.out.println(formatter.format(date));  

        outDir = "./"+ parentDir +"/run_"+instanceName+"_" + formatter.format(date) + "/";
        File f = new File(outDir);
        f.mkdirs();
        
		// Initialize Writers
		try {
			writerRemovals = new FileWriter(outDir + data.getInstanceName() + "_removalProbabilities.txt", true);
			writerRepairs = new FileWriter(outDir + "" + data.getInstanceName() + "__repairProbabilities.txt", true);
			writerPenalties = new FileWriter(outDir + "" + data.getInstanceName() + "__penaltiesCount.txt");
			writerResults = new FileWriter("./"+ parentDir +"/" + outputFile, true);
			writerConfig = new FileWriter(outDir + "config.json");
			writerFinalTour = new FileWriter(outDir + "finalTour.txt");
			writerBacktracking = new FileWriter(outDir + "backTrackingLog.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static <T> void initWriterRemovalProbabilities(FileWriter writer, AbstractRemoval[] operators) {
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
	
	public static <T> void initWriterRepairProbabilities(FileWriter writer, AbstractInsertion[] operators) {
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
	
	public static void writeSolomonResults(Solution solutionALNS, String instanceName, long timeElapsed, double gap) {
        try {
			writerResults.append(instanceName + "," + solutionALNS.getTotalCosts() + "," + timeElapsed + "," + gap + "\n");
	        writerResults.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writerHospitalResults(Data data, Solution solutionALNS, long timeElapsed) {
		try {
			writerResults.append(data.getInstanceName() + "," + solutionALNS.getTotalCosts() + "," + timeElapsed + "\n");
			writerResults.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initPenaltyCounts () {
		StringBuilder builder = new StringBuilder("iteration");
		int i = 0;
		do {
			builder.append(";");
			builder.append(DataUtils.PenaltyIdx.values()[i]);
			i++;
		} while (i<DataUtils.PenaltyIdx.values().length);
		try {
			writerPenalties.write(builder.toString() + "\n");
			writerPenalties.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initBacktrackingLogging () {
		StringBuilder builder = new StringBuilder("trial;noJumps;bestCosts\n");
		try {
			writerBacktracking.write(builder.toString());
			writerBacktracking.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writePenaltyCounts (int iteration, double[] penaltyCnts) {
		StringBuilder builder = new StringBuilder("" + iteration);
		int i = 0;
		do {
			builder.append(";");
			builder.append(penaltyCnts[i]);
			i++;
		} while (i<penaltyCnts.length);
		try {
			writerPenalties.write(builder.toString() + "\n");
			writerPenalties.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeFinalTour (FileWriter writer, String tour) {
		try {
			writer.write(tour);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBacktrackingInfo (int trial, int jumps, double costs) {
		try {
			writerBacktracking.write(trial + ";" + jumps + ";" + costs + "\n");
			writerBacktracking.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
