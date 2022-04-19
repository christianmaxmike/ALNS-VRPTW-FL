package vrptwfl.metaheuristic.utils;

import java.io.FileWriter;
import java.io.IOException;

import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.removals.AbstractRemoval;

public class WriterUtils {
	
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
}
