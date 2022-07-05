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
	
	public boolean randomizeConfig;

    // --- GENERAL ---
    public Random randomGenerator;

    // --- CALCULATIONS ---
    public double epsilon;
    public int bigMRegret;
    public double roundingPrecisionFactor;

    // --- ALNS ---
    public int alnsIterations;
    public int lowerBoundRemovals;
    public double lowerBoundRemovalsFactor;
    public int lowerBoundRemovalsMax;
    public int upperBoundRemovals;
    public double upperBoundRemovalsFactor;
    public int upperBoundRemovalsMax;
    public double checkIntervalInsertion;

    // --- GLS SETTINGS ---
    // GLS Instance-based
    public boolean enableGLS;
    public double glsLambdaUnscheduled;
    public double glsLambdaTimeWindow;
    public double glsLambdaPredJobs;
    public double glsLambdaCapacity;
    public double glsLambdaSkill;
    public int glsNFeaturesForPenaltyUpdate;
    public int glsIterUntilPenaltyUpdate;
    public double glsPenaltyInitValue;
    public double glsPenaltyIncrease;
    public double glsPenaltyReduction;
    // GLS Feature-based
    public boolean enableGLSFeature;
    public double glsFeatureOmega;
    public double[] glsFeatureRangeUnserved;
    public double[] glsFeatureRangeTimeWindow;
    public double[] glsFeatureRangePredJobs;
    public double[] glsFeatureRangeCapacity;
    public double[] glsFeatureRangeSkill;
    public double glsFeatureUnserved;
    public double glsFeatureTimeWindow;
    public double glsFeaturePredJobs;
    public double glsFeatureCapacity;
    public double glsFeatureSkill;
    // Penalty Costs
    public int exponentSwappingLocations;
    public double penaltyUnservedCustomer;	// set in MainALNS -> setInstanceSpecificParameters
    public double costUnservedCustomerViolation;
    public double costTimeWindowViolation;
    public double costPredJobsViolation;
    public double costCapacityViolation;
    public double costSkillLvlViolation;
    public double maxTimeWindowViolation;
    // Penalty Weights - Schiffer
    public boolean enableSchiffer;
    public double[] penaltyWeightUnservedCustomerRange;
    public double[] penaltyWeightTimeWindowRange;
    public double[] penaltyWeightPredecessorJobsRange;
    public double[] penaltyWeightCapacityRange;
    public double[] penaltyWeightSkillLvlRange;
    public double penaltyWeightUnservedCustomer;
    public double penaltyWeightTimeWindow;
    public double penaltyWeightPredecessorJobs;
    public double penaltyWeightCapacity;
    public double penaltyWeightSkillLvl;
    public double penaltyWeightOmega;
    public int penaltyWeightUpdateIteration;

    // --- ALNS OEPRATORS TO USE ---
    // removals
    public boolean useClusterRemovalKruskal;
    public boolean useKMeansRemoval;
    public boolean useHistoricNodePairRemovalDeterministic;
    public boolean useHistoricNodePairRemovalRandom;
    public boolean useHistoricRequestPairRemoval;
    public boolean useRandomRemoval;
    public boolean useRandomRouteRemoval;
    public boolean useShawSimplifiedRemovalDeterministic;
    public boolean useShawSimplifiedRemovalRandom;
    public boolean useTimeOrientedRemovalJungwirthDeterministic;
    public boolean useTimeOrientedRemovalJungwirthRandom;
    public boolean useTimeOrientedRemovalPisingerDeterministic;
    public boolean useTimeOrientedRemovalPisingerRandom;
    public boolean useWorstRemovalDeterministic;
    public boolean useWorstRemovalRandom;
    public boolean useSkillMismatchRemovalRandom;
    public boolean useSkillMismatchRemovalDeterministic;
    public boolean useTimeFlexibilityRemovalRandom;
    public boolean useTimeFlexibilityRemovalDeterministic;
    public boolean useRouteEliminationLeast;
    public boolean useRouteEliminationMost;
    public boolean useZoneRemoval;
    public boolean useSubrouteRemoval;
    public boolean useLocationRelatedRemoval;
    public boolean useLocationAndTimeRelatedRemoval;
    // insertions
    public boolean useGreedyInsert;
    public boolean useSkillMatchingInsert;
    public boolean useNRegret2;
    public boolean useNRegret3;
    public boolean useNRegret4;
    public boolean useNRegret5;
    public boolean useNRegret6;
    public boolean regretConsiderAllPossibleInsertionPerRoute;
    public boolean regretSumOverAllNRegret;

    // --- ALNS OPERATOR PARAMETERS ---
    public int historicNodePairRemovalExponent;
    public int historicRequestRemovalExponent;
    public int shawRemovalExponent;
    public int timeOrientedRemovalExponent;
    public int timeOrientedNrOfClosest;
    public double timeOrientedJungwirthWeightStartTimeIinSolution;
    public int worstRemovalExponent;
    public int skillMismatchRemovalExponent;
    public int timeFlexibilityRemovalExponent;
    public int requestGraphSolutionsSize;
    public int[] kMeansClusterSettings;
    
    // --- BACKTRACKING SETTINGS ---
    public boolean enableBacktracking;
    public int backtrackTrials;
    public int backtrackJump;
    public int backtrackJumpToLevel;
    public double[] backtrackJumpToLevelProbabilities;
    public boolean backtrackBySteps;
    public int maxBacktrackJumps;

    // --- UPDATE VALUES FOR DESTROY/REPAIR OPS ---
    public int sigma1;
    public int sigma2;
    public int sigma3;
    public double reactionFactor;
    public double minOpProb;
    public boolean drawOpUniformly;
    public int updateInterval;
    
    // --- SIMULATED ANNEALING ---
    public double coolingRate;
    public double minTempPercent;
    public double startTempControlParam;
    public double bigOmega;
    
    // --- LOCATION DEPENDENT VARIABLES (for solomon instances) ---
    public int numberOfLocationsPerCustomer;
        
    // --- HOSPITAL INSTANCES ---
    public int planningIntervals;
    public int maxCapacityVehicles;
    public boolean solveAsTwoProblems;
    public boolean splitRegularShift;
    public boolean printHospitalLoaderInfo;
    
    // --- additional values (used for I/O) ---
    public double avgOptimalityGapValue = Double.MAX_VALUE;
    public double optimalityGapValue = Double.MAX_VALUE;
    
    public static String configFile; // = "resources/config.yaml";

    //NOTE maybe as singleton pattern (getInstance())
    private static Config instance;
    //public Config conf = new Config();

    public static Config getInstance() {
    	if (instance == null) {
    		instance = new Config();
    	}
    	return instance;
    }
    // private to prevent anyone else from instantiating
    private Config() {
        loadConfig();
    }
    
    /**
     * Loads the configuration settings from the config.yaml file.
     */
    private void loadConfig() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(configFile));
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
        checkIntervalInsertion = (double) obj.get("check_interval_for_insertion");

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
        useRouteEliminationLeast = (boolean) obj.get("use_route_eliminiation_least");
        useRouteEliminationMost = (boolean) obj.get("use_route_eliminiation_most");
        useZoneRemoval = (boolean) obj.get("use_zone_removal");
        useSubrouteRemoval = (boolean) obj.get("use_subroute_removal");
        useLocationRelatedRemoval = (boolean) obj.get("use_locationRelated_removal");
        useLocationAndTimeRelatedRemoval = (boolean) obj.get("use_locationAndTimeRelated_removal");
        
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
        drawOpUniformly = (boolean) obj.get("drawOpUniformly");
        updateInterval = (int) obj.get("updateInterval");
        
        // --- SIMULATED ANNEALING ---
        coolingRate = (double) obj.get("coolingRate");
        minTempPercent = (double) obj.get("minTempPercent"); 
        startTempControlParam = (double) obj.get("startTempControlParam");
        bigOmega = (double) obj.get("bigOmega");
        
        // --- GLS ----
        enableGLS = (boolean) obj.get("enable_gls");
        
        glsLambdaUnscheduled = (double) obj.get("gls_lambda_unscheduled");
        glsLambdaTimeWindow = (double) obj.get("gls_lambda_timeWindow");
        glsLambdaPredJobs = (double) obj.get("gls_lambda_predJobs");
        glsLambdaCapacity = (double) obj.get("gls_lambda_capacity");
        glsLambdaSkill = (double) obj.get("gls_lambda_skill");
        glsNFeaturesForPenaltyUpdate = (int) obj.get("gls_nFeaturesForPenaltyUpdate");
        glsIterUntilPenaltyUpdate = (int) obj.get("gls_iterUntilPenaltyUpdate");
        glsPenaltyInitValue = (double) obj.get("gls_penaltyInitValue");
        glsPenaltyIncrease = (double) obj.get("gls_penaltyIncrease");
        glsPenaltyReduction = (double) obj.get("gls_penaltyReduction");
        // penalties
        exponentSwappingLocations = (int) obj.get("exponent_swapping_locations");
        costUnservedCustomerViolation = (double) obj.get("cost_factor_unserved_customer");
        costTimeWindowViolation = (double) obj.get("cost_timeWindow_violation");
        costPredJobsViolation = (double) obj.get("cost_predJobs_violation");
        costCapacityViolation = (double) obj.get("cost_capacity_violation");
        costSkillLvlViolation = (double) obj.get("cost_skillLvl_violation");
        maxTimeWindowViolation = (double) obj.get("max_timeWindow_violation");
        
        // GLS Feature-based
        enableGLSFeature = (boolean) obj.get("enable_glsFeature");
        glsFeatureOmega = (double) obj.get("gls_feature_omega");
        glsFeatureRangeUnserved = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("gls_feature_range_unserved"));
        glsFeatureRangeTimeWindow = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("gls_feature_range_timeWindow"));
        glsFeatureRangePredJobs = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("gls_feature_range_predJobs"));
        glsFeatureRangeCapacity = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("gls_feature_range_capacity"));
        glsFeatureRangeSkill = DataUtils.convertDoubleListToArr((ArrayList<Double>) obj.get("gls_feature_range_skill"));
        glsFeatureUnserved = (double) obj.get("gls_feature_weight_unserved");
        glsFeatureTimeWindow = (double) obj.get("gls_feature_weight_timeWindow");
        glsFeaturePredJobs = (double) obj.get("gls_feature_weight_predJobs");
        glsFeatureCapacity = (double) obj.get("gls_feature_weight_capacity");
        glsFeatureSkill = (double) obj.get("gls_feature_weight_skill");
        
        // --- PENALTY UPDATE SCHIFFER ----
        enableSchiffer = (boolean) obj.get("enable_schiffer"); 
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
    
    public void randomizeConfig () {
    	getInstance().reactionFactor = randomizeDoubleValue(0.22, getInstance().reactionFactor);
    	getInstance().sigma1 = (int) (randomizeDoubleValue(40.0, getInstance().sigma1));
    	getInstance().sigma2 = (int) (randomizeDoubleValue(20.0, getInstance().sigma2));
    	getInstance().sigma3 = (int) (randomizeDoubleValue(15.0, getInstance().sigma3));
    	
    	getInstance().coolingRate = randomizeDoubleValue(0.9999, getInstance().coolingRate);
    	
    	getInstance().costUnservedCustomerViolation = randomizeDoubleValue(5, getInstance().costUnservedCustomerViolation);
    	getInstance().costTimeWindowViolation = randomizeDoubleValue(6, getInstance().costTimeWindowViolation);
    	getInstance().costPredJobsViolation = randomizeDoubleValue(10, getInstance().costPredJobsViolation);
    	getInstance().costSkillLvlViolation = randomizeDoubleValue(5, getInstance().costSkillLvlViolation);
    	getInstance().costCapacityViolation = randomizeDoubleValue(5, getInstance().costCapacityViolation);
    	
    	getInstance().glsPenaltyInitValue = randomizeDoubleValue(1, getInstance().glsPenaltyInitValue);
    	getInstance().glsPenaltyIncrease = randomizeDoubleValue(0.5, getInstance().glsPenaltyIncrease);
    	getInstance().glsPenaltyReduction = randomizeDoubleValue(0.25, getInstance().glsPenaltyReduction);
    	
    	getInstance().exponentSwappingLocations = (int) randomizeDoubleValue(1, getInstance().exponentSwappingLocations);
    	getInstance().historicNodePairRemovalExponent = (int) randomizeDoubleValue(9, getInstance().historicNodePairRemovalExponent);
    	getInstance().historicRequestRemovalExponent = (int) randomizeDoubleValue(9, getInstance().historicRequestRemovalExponent);
    	getInstance().shawRemovalExponent = (int) randomizeDoubleValue(9, getInstance().shawRemovalExponent);
    	getInstance().skillMismatchRemovalExponent = (int) randomizeDoubleValue(9, getInstance().skillMismatchRemovalExponent);
    	getInstance().timeFlexibilityRemovalExponent = (int) randomizeDoubleValue(9, getInstance().timeFlexibilityRemovalExponent);
    	getInstance().timeOrientedRemovalExponent = (int) randomizeDoubleValue(9, getInstance().timeFlexibilityRemovalExponent);
    	getInstance().worstRemovalExponent = (int) randomizeDoubleValue(9, getInstance().worstRemovalExponent);
    	
    	getInstance().glsLambdaUnscheduled = (double) randomizeDoubleValue(15, getInstance().glsLambdaUnscheduled);
    	getInstance().glsLambdaTimeWindow = (double) randomizeDoubleValue(5, getInstance().glsLambdaTimeWindow);
    	getInstance().glsLambdaPredJobs = (double) randomizeDoubleValue(5, getInstance().glsLambdaPredJobs);
    	getInstance().glsLambdaCapacity = (double) randomizeDoubleValue(5, getInstance().glsLambdaCapacity);
    	getInstance().glsLambdaSkill = (double) randomizeDoubleValue(5, getInstance().glsLambdaSkill);
    	
    	getInstance().updateInterval = (int) randomizeDoubleValue(200, 1);
    	
    }
    
    private double randomizeDoubleValue (double upperBound, double startValue) {
    	return randomGenerator.nextDouble() * (upperBound - startValue) + startValue;
    }
}
