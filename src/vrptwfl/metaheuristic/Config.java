package vrptwfl.metaheuristic;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

public class Config {

    // GENERAL
    public static Random randomGenerator;

    // CALCULATIONS
    public static double epsilon;
    public static int bigMRegret;
    public static double roundingPrecisionFactor;

    // ALNS
    public static int alnsIterations;
    public static int lowerBoundRemovals;
    public static int upperBoundRemovals;
    public static double upperBoundRemovalsFactor;
    public static int upperBoundRemovalsMax;

    // alns operators
    public static int worstRemovalExponent;


    private static Config conf = new Config();

    // private to prevent anyone else from instantiating
    private Config() {
        loadConfig();
    }

    public void loadConfig() {

        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("resources/config.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> obj = yaml.load(inputStream);

        // --- GENERAL ---
        Integer randomSeed =  (Integer) obj.get("random_seed");
        randomGenerator = randomSeed != null ? new Random(randomSeed) : new Random();

        // --- CALCULATION / math helpers ---
        epsilon = (double) obj.get("epsilon");
        bigMRegret = (int) obj.get("bigM_regret");
        roundingPrecisionFactor = Math.pow(10, ((int) obj.get("rounding_precision")));

        // --- ALNS configurations ---
        alnsIterations = (int) obj.get("alns_iterations");

        // - ALNS destroy operators
        worstRemovalExponent = (int) obj.get("worst_removal_exponent");

        // see Ropke & Pisinger 2006, p. 465 (An ALNS Heuristic for the PDPTW)
        // upper bound will be determined instance specific when number of customers is knwon
        lowerBoundRemovals = (int) obj.get("lower_bound_nr_of_removals");
        upperBoundRemovalsFactor = (double) obj.get("upper_bound_factor_nr_of_removals");
        upperBoundRemovalsMax = (int) obj.get("upper_bound_nr_of_removals");

    }

    // TODO wieder raus
    public static void main(String[] args) {
        System.out.println("Hello Config");
    }
}
