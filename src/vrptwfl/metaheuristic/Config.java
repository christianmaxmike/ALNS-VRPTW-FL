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
    public static double lowerBoundRemovalsFactor;
    public static int lowerBoundRemovalsMax;
    public static int upperBoundRemovals;
    public static double upperBoundRemovalsFactor;
    public static int upperBoundRemovalsMax;

    public static double costFactorUnservedCustomer;
    public static double penaltyUnservedCustomer;

    // alns operators to use
    public static boolean useClusterRemovalKruskal;
    public static boolean useHistoricNodePairRemovalDeterministic;
    public static boolean useHistoricNodePairRemovalRandom;
    public static boolean useHistoricRequestPairRemoval;
    public static boolean useRandomRemoval;
    public static boolean useRandomRouteRemoval;
    public static boolean useShawSimplifiedRemovalDeterministic;
    public static boolean useShawSimplifiedRemovalRandom;
    public static boolean useTimeOrientedRemovalJungwirthDeterministic;
    public static boolean useTimeOrientedRemovalJungwirthRandom;
    public static boolean useTimeOrientedRemovalPisingerDeterministic;
    public static boolean useTimeOrientedRemovalPisingerRandom;
    public static boolean useWorstRemovalDeterministic;
    public static boolean useWorstRemovalRandom;

    public static boolean useGreedyInsert;
    public static boolean useNRegret2;
    public static boolean useNRegret3;
    public static boolean useNRegret4;
    public static boolean useNRegret5;
    public static boolean useNRegret6;
    public static boolean regretConsiderAllPossibleInsertionPerRoute;
    public static boolean regretSumOverAllNRegret;

    // alns operator parameters
    public static int historicNodePairRemovalExponent;
    public static int shawRemovalExponent;
    public static int timeOrientedRemovalExponent;
    public static int timeOrientedNrOfClosest;
    public static double timeOrientedJungwirthWeightStartTimeIinSolution;
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

        costFactorUnservedCustomer = (double) obj.get("cost_factor_unserved_customer");

        useClusterRemovalKruskal = (boolean) obj.get("use_cluster_removal_kruskal");
        useHistoricNodePairRemovalDeterministic = (boolean) obj.get("use_historic_node_pair_removal_deterministic");
        useHistoricNodePairRemovalRandom = (boolean) obj.get("use_historic_node_pair_removal_random");
        useHistoricRequestPairRemoval = (boolean) obj.get("use_historic_request_pair_removal");
        useRandomRemoval = (boolean) obj.get("use_random_removal");
        useRandomRouteRemoval = (boolean) obj.get("use_random_route_removal");
        useShawSimplifiedRemovalDeterministic = (boolean) obj.get("use_shaw_simplified_deterministic");
        useShawSimplifiedRemovalRandom = (boolean) obj.get("use_shaw_simplified_random");
        useTimeOrientedRemovalJungwirthDeterministic = (boolean) obj.get("use_time_oriented_removal_jungwirth_deterministic");
        useTimeOrientedRemovalJungwirthRandom = (boolean) obj.get("use_time_oriented_removal_jungwirth_random");
        useTimeOrientedRemovalPisingerDeterministic = (boolean) obj.get("use_time_oriented_removal_pisinger_deterministic");
        useTimeOrientedRemovalPisingerRandom = (boolean) obj.get("use_time_oriented_removal_pisinger_random");
        useWorstRemovalDeterministic = (boolean) obj.get("use_worst_removal_deterministic");
        useWorstRemovalRandom = (boolean) obj.get("use_worst_removal_random");

        useGreedyInsert = (boolean) obj.get("use_greedy_insert");
        useNRegret2 = (boolean) obj.get("use_nregret_2");
        useNRegret3 = (boolean) obj.get("use_nregret_3");
        useNRegret4 = (boolean) obj.get("use_nregret_4");
        useNRegret5 = (boolean) obj.get("use_nregret_5");
        useNRegret6 = (boolean) obj.get("use_nregret_6");
        regretConsiderAllPossibleInsertionPerRoute = (boolean) obj.get("regret_consider_all_possible_insertion_per_route");
        regretSumOverAllNRegret = (boolean) obj.get("regret_sum_over_all_n_regret");

        // - ALNS destroy operators
        historicNodePairRemovalExponent = (int) obj.get("neighbor_graph_removal_exponent");
        shawRemovalExponent = (int) obj.get("shaw_removal_exponent");
        timeOrientedRemovalExponent = (int) obj.get("time_oriented_removal_exponent");
        timeOrientedNrOfClosest = (int) obj.get("time_oriented_nr_of_closest");
        timeOrientedJungwirthWeightStartTimeIinSolution = (double) obj.get("time_oriented_jungwirth_weight_start_time_in_solution");
        worstRemovalExponent = (int) obj.get("worst_removal_exponent");

        // see Ropke C&OR §6.1.1 p. 2417
        // upper bound will be determined instance specific when number of customers is known
        lowerBoundRemovalsFactor = (double) obj.get("lower_bound_factor_nr_of_removals");
        lowerBoundRemovalsMax = (int) obj.get("lower_bound_nr_of_removals");
        upperBoundRemovalsFactor = (double) obj.get("upper_bound_factor_nr_of_removals");
        upperBoundRemovalsMax = (int) obj.get("upper_bound_nr_of_removals");


    }

    // TODO wieder raus
    public static void main(String[] args) {
        System.out.println("Hello Config");
    }
}
