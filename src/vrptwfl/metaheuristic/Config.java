package vrptwfl.metaheuristic;

import org.yaml.snakeyaml.Yaml;

import vrptwfl.metaheuristic.utils.DataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Configuration class.
 * 
 * @author Christian M.M. Frey, Alexander Jungwirth
 */
public class Config {

    // --- GENERAL ---
    public static Random randomGenerator;

    // --- CALCULATIONS ---
    public static double epsilon;
    public static int bigMRegret;
    public static double roundingPrecisionFactor;

    // --- ALNS ---
    public static int alnsIterations;
    public static int lowerBoundRemovals;
    public static double lowerBoundRemovalsFactor;
    public static int lowerBoundRemovalsMax;
    public static int upperBoundRemovals;
    public static double upperBoundRemovalsFactor;
    public static int upperBoundRemovalsMax;

    // --- GLS SETTINGS ---
    public static boolean enableGLS;
    // Penalty Costs
    public static int exponentSwappingLocations;
    public static double penaltyUnservedCustomer;	// set in MainALNS -> setInstanceSpecificParameters
    public static double costUnservedCustomerViolation;
    public static double costTimeWindowViolation;
    public static double costPredJobsViolation;
    public static double costCapacityViolation;
    public static double costSkillLvlViolation;
    public static double maxTimeWindowViolation;
    // Penalty Weights
    public static double[] penaltyWeightUnservedCustomerRange;
    public static double[] penaltyWeightTimeWindowRange;
    public static double[] penaltyWeightPredecessorJobsRange;
    public static double[] penaltyWeightCapacityRange;
    public static double[] penaltyWeightSkillLvlRange;
    public static double penaltyWeightUnservedCustomer;
    public static double penaltyWeightTimeWindow;
    public static double penaltyWeightPredecessorJobs;
    public static double penaltyWeightCapacity;
    public static double penaltyWeightSkillLvl;
    public static double penaltyWeightOmega;
    public static int penaltyWeightUpdateIteration;

    // --- ALNS OEPRATORS TO USE ---
    // removals
    public static boolean useClusterRemovalKruskal;
    public static boolean useKMeansRemoval;
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
    public static boolean useSkillMismatchRemovalRandom;
    public static boolean useSkillMismatchRemovalDeterministic;
    public static boolean useTimeFlexibilityRemovalRandom;
    public static boolean useTimeFlexibilityRemovalDeterministic;
    // insertions
    public static boolean useGreedyInsert;
    public static boolean useSkillMatchingInsert;
    public static boolean useNRegret2;
    public static boolean useNRegret3;
    public static boolean useNRegret4;
    public static boolean useNRegret5;
    public static boolean useNRegret6;
    public static boolean regretConsiderAllPossibleInsertionPerRoute;
    public static boolean regretSumOverAllNRegret;

    // --- ALNS OPERATOR PARAMETERS ---
    public static int historicNodePairRemovalExponent;
    public static int historicRequestRemovalExponent;
    public static int shawRemovalExponent;
    public static int timeOrientedRemovalExponent;
    public static int timeOrientedNrOfClosest;
    public static double timeOrientedJungwirthWeightStartTimeIinSolution;
    public static int worstRemovalExponent;
    public static int skillMismatchRemovalExponent;
    public static int timeFlexibilityRemovalExponent;
    public static int requestGraphSolutionsSize;
    public static int[] kMeansClusterSettings;
    
    // --- BACKTRACKING SETTINGS ---
    public static boolean enableBacktracking;
    public static int backtrackTrials;
    public static int backtrackJump;
    public static int backtrackJumpToLevel;
    public static double[] backtrackJumpToLevelProbabilities;
    public static boolean backtrackBySteps;
    public static int maxBacktrackJumps;

    // --- UPDATE VALUES FOR DESTROY/REPAIR OPS ---
    public static int sigma1;
    public static int sigma2;
    public static int sigma3;
    public static double reactionFactor;
    public static double minOpProb;
    public static int updateInterval;
    
    // --- SIMULATED ANNEALING ---
    public static double coolingRate;
    public static double minTempPercent;
    public static double startTempControlParam;
    public static double bigOmega;
    
    // --- LOCATION DEPENDENT VARIABLES (for solomon instances) ---
    public static int numberOfLocationsPerCustomer;
        
    // --- HOSPITAL INSTANCES ---
    public static int planningIntervals;
    public static int maxCapacityVehicles;
    public static boolean solveAsTwoProblems;
    public static boolean splitRegularShift;
    public static boolean printHospitalLoaderInfo;
    

    //NOTE Chris - if boring - make singleton pattern (getInstance())
    private static Config conf = new Config();

    // private to prevent anyone else from instantiating
    private Config() {
        loadConfig();
    }

    /**
     * Loads the configuration settings from the config.yaml file.
     */
    public void loadConfig() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("resources/config.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inputStream);

        // *** SETTINGS ***
        // --- GENERAL ---
        Integer randomSeed =  (Integer) obj.get("random_seed");
        randomGenerator = randomSeed != null ? new Random(randomSeed) : new Random();

        // --- CALCULATION / math helpers ---
        epsilon = (double) obj.get("epsilon");
        bigMRegret = (int) obj.get("bigM_regret");
        roundingPrecisionFactor = Math.pow(10, ((int) obj.get("rounding_precision")));

        // --- ALNS configurations ---
        alnsIterations = (int) obj.get("alns_iterations");

        // --- DESTROY / REPAIR OPERATORS ---
        // removals
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
        useSkillMismatchRemovalRandom = (boolean) obj.get("use_skill_mismatch_removal_random");
        useSkillMismatchRemovalDeterministic = (boolean) obj.get("use_skill_mismatch_removal_deterministic");
        useTimeFlexibilityRemovalRandom = (boolean) obj.get("use_time_flexibility_removal_random");
        useTimeFlexibilityRemovalDeterministic = (boolean) obj.get("use_time_flexibility_removal_deterministic");
        useKMeansRemoval = (boolean) obj.get("use_kmeans_removal");
        // insertions
        useGreedyInsert = (boolean) obj.get("use_greedy_insert");
        useSkillMatchingInsert = (boolean) obj.get("use_skill_matching_insert");
        useNRegret2 = (boolean) obj.get("use_nregret_2");
        useNRegret3 = (boolean) obj.get("use_nregret_3");
        useNRegret4 = (boolean) obj.get("use_nregret_4");
        useNRegret5 = (boolean) obj.get("use_nregret_5");
        useNRegret6 = (boolean) obj.get("use_nregret_6");
        
        // --- SETTINGS FOR ALNS DESTROY OPERATORS ---
        regretConsiderAllPossibleInsertionPerRoute = (boolean) obj.get("regret_consider_all_possible_insertion_per_route");
        regretSumOverAllNRegret = (boolean) obj.get("regret_sum_over_all_n_regret");
        historicNodePairRemovalExponent = (int) obj.get("neighbor_graph_removal_exponent");
        historicRequestRemovalExponent = (int) obj.get("request_graph_removal_exponent");
        shawRemovalExponent = (int) obj.get("shaw_removal_exponent");
        timeOrientedRemovalExponent = (int) obj.get("time_oriented_removal_exponent");
        timeOrientedNrOfClosest = (int) obj.get("time_oriented_nr_of_closest");
        timeOrientedJungwirthWeightStartTimeIinSolution = (double) obj.get("time_oriented_jungwirth_weight_start_time_in_solution");
        worstRemovalExponent = (int) obj.get("worst_removal_exponent");
        skillMismatchRemovalExponent = (int) obj.get("skill_mismatch_removal_exponent");
        timeFlexibilityRemovalExponent = (int) obj.get("time_flexibility_removal_exponent");
        requestGraphSolutionsSize = (int) obj.get("request_graph_solutions_size");
        kMeansClusterSettings = DataUtils.convertListToArray((ArrayList<Integer>) obj.get("kmeansNClusters"));
        // see Ropke C&OR §6.1.1 p. 2417
        // upper bound will be determined instance specific when number of customers is known
        lowerBoundRemovalsFactor = (double) obj.get("lower_bound_factor_nr_of_removals");
        lowerBoundRemovalsMax = (int) obj.get("lower_bound_nr_of_removals");
        upperBoundRemovalsFactor = (double) obj.get("upper_bound_factor_nr_of_removals");
        upperBoundRemovalsMax = (int) obj.get("upper_bound_nr_of_removals");
        
        // --- BACKTRACKING OPTIONS ---
        enableBacktracking = (boolean) obj.get("enable_backtracking");
        backtrackTrials = (int) obj.get("backtrackTrials");
        backtrackJump = (int) obj.get("backtrackJump");
        backtrackJumpToLevel = (int) obj.get("backtrackJumpToLevel");
        backtrackJumpToLevelProbabilities = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("backtrackJumpToLevelProbabilities"));
        backtrackBySteps = (boolean) obj.get("backtrackBySteps");
        maxBacktrackJumps = (int) obj.get("maxBacktrackJumps");

        // --- UPDATE VALUES FOR DESTROY/REPAIR OPERATORS ---
        sigma1 = (int) obj.get("sigma1");
        sigma2 = (int) obj.get("sigma2");
        sigma3 = (int) obj.get("sigma3");
        reactionFactor = (double) obj.get("reactionFactor");
        minOpProb = (double) obj.get("minOpProb");
        updateInterval = (int) obj.get("updateInterval");
        
        // --- SIMULATED ANNEALING ---
        coolingRate = (double) obj.get("coolingRate");
        minTempPercent = (double) obj.get("minTempPercent"); 
        startTempControlParam = (double) obj.get("startTempControlParam");
        bigOmega = (double) obj.get("bigOmega");
        
        // --- GLS ----
        enableGLS = (boolean) obj.get("enable_gls");
        // penalties
        exponentSwappingLocations = (int) obj.get("exponent_swapping_locations");
        costUnservedCustomerViolation = (double) obj.get("cost_factor_unserved_customer");
        costTimeWindowViolation = (double) obj.get("cost_timeWindow_violation");
        costPredJobsViolation = (double) obj.get("cost_predJobs_violation");
        costCapacityViolation = (double) obj.get("cost_capacity_violation");
        costSkillLvlViolation = (double) obj.get("cost_skillLvl_violation");
        maxTimeWindowViolation = (double) obj.get("max_timeWindow_violation");
        // penalty weights
        penaltyWeightUnservedCustomerRange = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("penalty_weight_unserved_customer_range"));
        penaltyWeightTimeWindowRange = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("penalty_weight_timeWindow_range"));
        penaltyWeightPredecessorJobsRange = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("penalty_weight_predecessor_range"));
        penaltyWeightCapacityRange = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("penalty_weight_capacity_range"));
        penaltyWeightSkillLvlRange = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("penalty_weight_skillLvl_range"));
        penaltyWeightUnservedCustomer = (double) obj.get("penalty_weight_unserved_customer");
        penaltyWeightTimeWindow = (double) obj.get("penalty_weight_timeWindow");
        penaltyWeightPredecessorJobs = (double) obj.get("penalty_weight_predecessor");
        penaltyWeightCapacity = (double) obj.get("penalty_weight_capacity");
        penaltyWeightSkillLvl = (double) obj.get("penalty_weight_skillLvl");
        penaltyWeightOmega = (double) obj.get("penalty_weight_omega");
        penaltyWeightUpdateIteration = (int) obj.get("penalty_weight_update_iteration");
        
    
        // --- LOCATION SETTINGS (for solomon instances) ---
        numberOfLocationsPerCustomer = (int) obj.get("numberOfLocationsPerCustomer");
        
        // --- SETTINGS FOR HOSPITAL INSTANCES ---
        planningIntervals = (int) obj.get("planningIntervals");
        maxCapacityVehicles = (int) obj.get("maxCapacityVehicles");
        solveAsTwoProblems = (boolean) obj.get("solveAsTwoProblems");
        splitRegularShift = (boolean) obj.get("splitRegularShift");
        printHospitalLoaderInfo = (boolean) obj.get("printHospitalLoaderInfo");
    }
}
