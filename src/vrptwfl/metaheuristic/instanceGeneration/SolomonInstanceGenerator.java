package vrptwfl.metaheuristic.instanceGeneration;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SolomonInstanceGenerator {

    public Data loadInstance(String fileName, int nCustomers) {

        String locationOfSolomonInstances = "resources/Instances-Solomon/";

        String entireTextFile = null;
        try {
            entireTextFile = Files.readString(Path.of(locationOfSolomonInstances + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = entireTextFile.split("\n");

        // get general information
        String instanceName = lines[0].replaceAll(" ", "");  // remove white space at end of name

        // get vehicle information
        List<Integer> lineVehicleInfo = getIntegerArrayFromLine(lines[4]);
        int nVehicles = lineVehicleInfo.get(0);
        int vehicleCapacity = lineVehicleInfo.get(1);
        List<Integer> xcoords = new ArrayList<>();
        List<Integer> ycoords = new ArrayList<>();
        List<Integer> demands = new ArrayList<>();
        List<Integer> earliestStartTimes = new ArrayList<>();
        List<Integer> latestStartTimes = new ArrayList<>();
        List<Integer> serviceDurations = new ArrayList<>();
        // get customer information
        for (int i=9; i < 10 + nCustomers; i++ ) { // nCustomers + one additional for depot
            List<Integer> lineCustomer = getIntegerArrayFromLine(lines[i]);
            xcoords.add(lineCustomer.get(1));
            ycoords.add(lineCustomer.get(2));
            demands.add(lineCustomer.get(3));
            earliestStartTimes.add(lineCustomer.get(4));
            latestStartTimes.add(lineCustomer.get(5));
            serviceDurations.add(lineCustomer.get(6));
        }

        return new Data(
                DataUtils.convertListToArray(xcoords),
                DataUtils.convertListToArray(ycoords),
                DataUtils.convertListToArray(demands),
                DataUtils.convertListToArray(earliestStartTimes),
                DataUtils.convertListToArray(latestStartTimes),
                DataUtils.convertListToArray(serviceDurations)
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

    public static void main(String[] args) throws IOException {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
//        generator.loadInstance("R101.txt", 25);
        generator.loadInstance("C106.txt", 25);
    }

}

