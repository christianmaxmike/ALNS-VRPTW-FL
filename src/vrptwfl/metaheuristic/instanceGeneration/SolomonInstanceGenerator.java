package vrptwfl.metaheuristic.instanceGeneration;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolomonInstanceGenerator {

    private String[] readInstanceTextFile(String fileName) throws IOException {
        String locationOfSolomonInstances = "./instances/Instances-Solomon/";
        String entireTextFile = Files.readString(Path.of(locationOfSolomonInstances + fileName));
        entireTextFile = entireTextFile.replaceAll("\r\n", "\n"); // windows carriage returns
        return entireTextFile.split("\n"); // former by Alex (\r\n)
    }
    
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
        
        HashMap<Integer, ArrayList<Integer>> customerToLocations = new HashMap<Integer, ArrayList<Integer>>();

        HashMap<java.awt.geom.Point2D, Integer> coordsToId = new HashMap<java.awt.geom.Point2D, Integer>();

        int locationId = 0;
        // Read customer information
        for (int i=9; i < 10 + nCustomers; i++ ) { // nCustomers + one additional for depot
            List<Integer> lineCustomer = getIntegerArrayFromLine(lines[i]);
            
            java.awt.geom.Point2D c = new java.awt.geom.Point2D.Double(lineCustomer.get(1), lineCustomer.get(2));
            if (coordsToId.get(c) == null)
            	coordsToId.put(c, locationId++);
            
            if (customerToLocations.get(lineCustomer.get(0)) == null)
            	customerToLocations.put(lineCustomer.get(0), new ArrayList<Integer>());
            customerToLocations.get(lineCustomer.get(0)).add(coordsToId.get(c));
            
            demands.add(lineCustomer.get(3));
            earliestStartTimes.add(lineCustomer.get(4));
            latestStartTimes.add(lineCustomer.get(5));
            serviceDurations.add(lineCustomer.get(6));
            
            // TODO CHRIS: check capacity slots only for critical locations - check count of overlappings of all jobs
            // TODO CHRIS: pre-processing - identify critical locations
            
            locationCapacity.add(1); 
            requiredSkillLvl.add(0);
        }
        
        for (int nLocations = 1; nLocations < Config.numberOfLocationsPerCustomer; nLocations++) {
        	for (int customerId = 1; customerId < customerToLocations.size()-1; customerId++) {
        		customerToLocations.get(customerId).add(customerToLocations.get(customerId+1).get(customerToLocations.get(customerId+1).size()-1));
        	}
        	customerToLocations.get(customerToLocations.size()-1).add(
        			customerToLocations.get(1).get(nLocations-1));
        }

        List<Integer> customerIds = IntStream.rangeClosed(1, nCustomers).boxed().collect(Collectors.toList());
        
		// TODO CHRIS - bei hospital instanzen nicht jeder patient hat gleich viel mögliche locations
		// TODO CHRIS - in hospital instanzen, location mit id 0 nicht zwingend präferierte location
        // TODO Chris - für customers nur locations ids abspeichern, hashmaps/arraylist
        int numberOfLocations = Config.numberOfLocationsPerCustomer;
                
        return new Data(
                instanceName,
                nCustomers,
                nVehicles,
                vehicleCapacity,
                DataUtils.convertListToArray(customerIds),
                DataUtils.convertListToArray(locationCapacity),
                customerToLocations,
                coordsToId,
                DataUtils.convertListToArray(demands),
                DataUtils.convertListToArray(earliestStartTimes),
                DataUtils.convertListToArray(latestStartTimes),
                DataUtils.convertListToArray(serviceDurations),
                DataUtils.convertListToArray(requiredSkillLvl),
                vehiclesSkillLvl
        );
    }

    private List<Integer> getIntegerArrayFromLine(String line) {
        return getIntegerArrayFromLine(line, false);
    }

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
    
    // TODO Alex: main wieder entfernen
    public static void main(String[] args) throws IOException {
        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data d;
        try {
            d = generator.loadInstance("R101.txt", 100);
        } catch (ArgumentOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
