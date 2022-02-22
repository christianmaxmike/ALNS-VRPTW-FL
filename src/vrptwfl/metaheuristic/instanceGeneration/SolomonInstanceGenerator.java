package vrptwfl.metaheuristic.instanceGeneration;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

        List<Integer> customerIds = IntStream.rangeClosed(1, nCustomers)
                .boxed()
                .collect(Collectors.toList());

        return new Data(
                instanceName,
                nCustomers,
                nVehicles,
                vehicleCapacity,
                DataUtils.convertListToArray(customerIds),
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

    // TODO main wieder entfernen
    public static void main(String[] args) throws IOException {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
//        generator.loadInstance("R101.txt", 25);
        try {
            generator.loadInstance("C106.txt", 125);
//            generator.loadInstance("GibtEsNicht.txt", 125);
        } catch (ArgumentOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}

