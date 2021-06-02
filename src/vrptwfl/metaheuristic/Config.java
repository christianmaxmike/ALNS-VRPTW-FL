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

    // alns operators to use
    public static boolean useRandomRemoval;
    public static boolean useRandomRouteRemoval;
    public static boolean useWorstRemovalRandom;
    public static boolean useWorstRemovalDeterministic;
    public static boolean useShawSimplifiedRandom;
    public static boolean useShawSimplifiedDeterministic;

    public static boolean useGreedyInsert;
    public static boolean useNRegret2;
    public static boolean useNRegret3;
    public static boolean useNRegret4;
    public static boolean useNRegret5;
    public static boolean useNRegret6;

    // alns operator parameters
    public static int worstRemovalExponent;
    public static int shawRemovalExponent;


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

        useRandomRemoval = (boolean) obj.get("use_random_removal");
        useRandomRouteRemoval = (boolean) obj.get("use_random_route_removal");
        useWorstRemovalRandom = (boolean) obj.get("use_worst_removal_random");
        useWorstRemovalDeterministic = (boolean) obj.get("use_worst_removal_deterministic");
        useShawSimplifiedRandom = (boolean) obj.get("use_shaw_simplified_random");
        useShawSimplifiedDeterministic = (boolean) obj.get("use_shaw_simplified_deterministic");

        useGreedyInsert = (boolean) obj.get("use_greedy_insert");
        useNRegret2 = (boolean) obj.get("use_nregret_2");
        useNRegret3 = (boolean) obj.get("use_nregret_3");
        useNRegret4 = (boolean) obj.get("use_nregret_4");
        useNRegret5 = (boolean) obj.get("use_nregret_5");
        useNRegret6 = (boolean) obj.get("use_nregret_6");

        // - ALNS destroy operators
        worstRemovalExponent = (int) obj.get("worst_removal_exponent");
        shawRemovalExponent = (int) obj.get("shaw_removal_exponent");

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
