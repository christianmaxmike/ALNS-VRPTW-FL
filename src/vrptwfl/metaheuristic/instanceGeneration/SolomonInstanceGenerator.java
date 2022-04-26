package vrptwfl.metaheuristic.instanceGeneration;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for loading solomon instances. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class SolomonInstanceGenerator {
    
    /**
     * Starting point for loading a solomon instance. The input instance 
     * is processed with the number of customers being attached as parameters. 
     * Valid options for calculating also an optimality gap being reported in
     * the literature are 25,50,100. A data object is returned such that it 
     * can be processed by the ALNS framework.
     * @param fileName: name of instance
     * @param nCustomers: number of customers
     * @return data object
     * @throws ArgumentOutOfBoundsException
     * @throws IOException
     */
    public Data loadInstance(String fileName, int nCustomers) throws ArgumentOutOfBoundsException, IOException {
        String[] lines = readInstanceTextFile(fileName);

        // get general information
        String instanceName = lines[0];

        if (lines.length != 110) throw new ArgumentOutOfBoundsException("Expected that " + fileName + " is 110 lines long. However, found "  + lines.length + " lines.");
        if (nCustomers < 1) throw new ArgumentOutOfBoundsException("At least one customer must exists (you passed " + nCustomers + ")");
        if (lines.length < nCustomers + 10) throw new ArgumentOutOfBoundsException("Number of customers (" + nCustomers + ") less than entries for customers (" + (lines.length-10) + ") in instance " + instanceName + ".");

        // get vehicle information
        List<Integer> lineVehicleInfo = getIntegerArrayFromLine(lines[4]);
        int nVehicles = lineVehicleInfo.get(0);
        int[] vehiclesSkillLvl = new int[nVehicles];  // default: 0 entries
        int vehicleCapacity = lineVehicleInfo.get(1);
        List<Integer> locationCapacity = new ArrayList<>();
        List<Integer> demands = new ArrayList<>();
        List<Integer> earliestStartTimes = new ArrayList<>();
        List<Integer> latestStartTimes = new ArrayList<>();
        List<Integer> serviceDurations = new ArrayList<>();
        List<Integer> requiredSkillLvl = new ArrayList<>();
        List<Integer> preferredLocations = new ArrayList<>();
        HashMap<Integer, ArrayList<Integer>> predJobs = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, ArrayList<Integer>> customerToLocations = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, ArrayList<Integer>> locationsToCustomers = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<java.awt.geom.Point2D, Integer> coordsToId = new HashMap<java.awt.geom.Point2D, Integer>();

        int locationId = 0;
        // Read customer information
        for (int i=9; i < 10 + nCustomers; i++ ) { // nCustomers + one additional for depot
            List<Integer> lineCustomer = getIntegerArrayFromLine(lines[i]);
            
            // check whether location is already known, otherwise add it to possible locations
            java.awt.geom.Point2D c = new java.awt.geom.Point2D.Double(lineCustomer.get(1), lineCustomer.get(2));
            if (coordsToId.get(c) == null)
            	coordsToId.put(c, locationId++);
            
            // Add location to the list of possible locations a customer can be served
            if (customerToLocations.get(lineCustomer.get(0)) == null)
            	customerToLocations.put(lineCustomer.get(0), new ArrayList<Integer>());
            customerToLocations.get(lineCustomer.get(0)).add(coordsToId.get(c));
            
            // Add customer to location
            if (locationsToCustomers.get(coordsToId.get(c)) == null)
            	locationsToCustomers.put(coordsToId.get(c), new ArrayList<Integer>());
            if (!locationsToCustomers.get(coordsToId.get(c)).contains(lineCustomer.get(0)))
            	locationsToCustomers.get(coordsToId.get(c)).add(lineCustomer.get(0));
            
            // Add customer's information
            demands.add(lineCustomer.get(3));
            earliestStartTimes.add(lineCustomer.get(4));
            latestStartTimes.add(lineCustomer.get(5));
            serviceDurations.add(lineCustomer.get(6));
            locationCapacity.add(1); 
            requiredSkillLvl.add(0);
            preferredLocations.add(coordsToId.get(c));
            // in solomon instances, there are no predecessor jobs -> empty lists
            predJobs.put(lineCustomer.get(0), new ArrayList<Integer>());
                        
            // TODO Chris - check capacity slots only for critical locations - check count of overlappings of all jobs
            // 			  - pre-processing - identify critical locations
            
        }
        
        for (int nLocations = 1; nLocations < Config.numberOfLocationsPerCustomer; nLocations++) {
        	for (int customerId = 1; customerId < customerToLocations.size()-1; customerId++) {
        		customerToLocations.get(customerId).add(customerToLocations.get(customerId+1).get(customerToLocations.get(customerId+1).size()-1));
        	}
        	customerToLocations.get(customerToLocations.size()-1).add(customerToLocations.get(1).get(nLocations-1));
        }

        List<Integer> customerIds = IntStream.rangeClosed(1, nCustomers).boxed().collect(Collectors.toList());
        
        // Create and return data object
        return new Data(
                instanceName,
                nCustomers,
                nVehicles,
                vehicleCapacity,
                DataUtils.convertListToArray(customerIds),
                DataUtils.convertListToArray(locationCapacity),
                customerToLocations,
                locationsToCustomers,
                coordsToId,
                DataUtils.convertListToArray(demands),
                DataUtils.convertListToArray(earliestStartTimes),
                DataUtils.convertListToArray(latestStartTimes),
                DataUtils.convertListToArray(serviceDurations),
                DataUtils.convertListToArray(requiredSkillLvl),
                vehiclesSkillLvl,
                predJobs,
                DataUtils.convertListToArray(preferredLocations)
        );
    }
    
	/**
	 * Read the input file and return the information in a string array.
	 * @param fileName: instance name to be processed
	 * @return string array with the loaded information
	 * @throws IOException
	 */
    private String[] readInstanceTextFile(String fileName) throws IOException {
        String locationOfSolomonInstances = "./instances/Instances-Solomon/";
        String entireTextFile = Files.readString(Path.of(locationOfSolomonInstances + fileName));
        entireTextFile = entireTextFile.replaceAll("\r\n", "\n"); // windows carriage returns
        return entireTextFile.split("\n"); // former by Alex (\r\n)
    }

    /**
     * Calls getIntegerArrayFromLine with no verbosity.
     * @param line: line to be processed
     * @return list of integers = information in the processed line.
     */
    private List<Integer> getIntegerArrayFromLine(String line) {
        return getIntegerArrayFromLine(line, false);
    }

    /**
     * Returns the information from the processed line and converts
     * them into integer values.
     * @param line: line to be processed
     * @param verbose: verbosity (for debugging purpose)
     * @return list of integers = information in the processed line.
     */
    private List<Integer> getIntegerArrayFromLine(String line, boolean verbose) {
        String numbersLine = line.replaceAll("[^0-9]+", " ");
        String[] strArray = numbersLine.split(" ");
        List<Integer> intArrayList = new ArrayList<>();
        for (String string : strArray) {
            if (!string.equals("")) {
                intArrayList.add(Integer.parseInt(string));
                if (verbose) System.out.println(string);
            }
        }
        return intArrayList;
    }
    
    // DEBUG - Main kann wieder entfernen
//    public static void main(String[] args) throws IOException {
//        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
//        try {
//            generator.loadInstance("R101.txt", 100);
//        } catch (ArgumentOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//    }
}
