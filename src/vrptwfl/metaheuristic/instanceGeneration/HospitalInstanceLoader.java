package vrptwfl.metaheuristic.instanceGeneration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;
import vrptwfl.metaheuristic.Config;

/**
 * Class for loading hospital instances. 
 * 
 * @author Christian M.M. Frey
 *
 */
public class HospitalInstanceLoader {
	private int planningInterval;

	/**
	 * Starting point for loading a hospital instance. The function calls
	 * all of the sub-procedures to load a hospital instance and create 
	 * data objects such that it can be processed by the ALNS framework.
	 * @param instanceName: name of instance being processed
	 * @return array of data objects (if Config.solveAsTwoProblems is true, idx:0 data object for the morning shift and idx:1 for the evening shift)
	 */
	// Data is array because we have two data sets for morning and evening if problem is solved as two problems.
	public Data[] loadHospitalInstanceFromJSON(String instanceName) {
		
		// planning interval
		this.planningInterval = Config.planningIntervals; // default 5 min
		int vehicleCapacity = Config.maxCapacityVehicles; // big enough to never be critical

		Data[] data;
		if (Config.solveAsTwoProblems) {
			data = new Data[2];
			data[0] = new Data(instanceName + "_m"); // m: morning shift
			data[1] = new Data(instanceName + "_e"); // e: evening shift
		} else {
			data = new Data[1];
			data[0] = new Data(instanceName);
		}

		// create GSON loader
		HospitalGSONLoader dataLoader = new HospitalGSONLoader();
		
		try {
			// load instance from JSON
			ImportedInstance instance = dataLoader.loadHospitalInstanceFromJSON(instanceName);
			
			// Load therapists
			ImportedTherapist[] therapists =  instance.getTherapists(); 
			
			// if split regular shift is activated 
			if(Config.splitRegularShift) 
				therapists = this.splitRegularShifts(therapists);
			
			// generate working patterns and vehicles
			if (Config.solveAsTwoProblems) {
				// note the therapists below are already split because splitRegularShifts is always true if solveAsTwoProblems
				ImportedTherapist[][] aux = this.getTherapistsForMorningAndEveningShift(therapists);
				ImportedTherapist[] morningTherapists = aux[0];
				ImportedTherapist[] eveningTherapists = aux[1];
				
				ArrayList<Integer> mTherapistsSkill = new ArrayList<Integer>();
				for (ImportedTherapist t : morningTherapists)
					mTherapistsSkill.add(t.getSkill());
				
				ArrayList<Integer> eTherapistsSkill = new ArrayList<Integer>();
				for (ImportedTherapist t : eveningTherapists)
					eTherapistsSkill.add(t.getSkill());

				// generate vehicles // initialization of vehicles when empty solution is created
				data[0].setNVehicles(morningTherapists.length);
				data[0].setVehicleCapacity(vehicleCapacity);
				data[0].setVehiclesSkillLvl(DataUtils.convertListToArray(mTherapistsSkill));

				data[1].setVehicleCapacity(vehicleCapacity);
				data[1].setNVehicles(eveningTherapists.length);
				data[1].setVehiclesSkillLvl(DataUtils.convertListToArray(eTherapistsSkill));
			} else {
				//TODO DONE? Chris - create data object if there is not solveAsTwoProblems activated
				ArrayList<Integer> therapistsSkillLvls = new ArrayList<Integer>();
				for (ImportedTherapist t : therapists)
					therapistsSkillLvls.add(t.getSkill());
				
				data[0].setNVehicles(therapists.length);
				data[0].setVehicleCapacity(vehicleCapacity);
				data[0].setVehiclesSkillLvl(DataUtils.convertListToArray(therapistsSkillLvls));
			}

			ImportedRoom[] rooms = instance.getRooms(); 
			ImportedJob[] importedRealJobs = instance.getJobs();
			double[][] realJobs = this.generateRealJobs(importedRealJobs);
			HashMap<Integer, ArrayList<Integer>> predJobs = this.generatePredJobs(importedRealJobs);
			HashMap<Integer, ArrayList<Integer>> job2Location = getJobToLocation(importedRealJobs);
			HashMap<Integer, ArrayList<Integer>> location2Job = getLocationToJob(importedRealJobs);

			// generate locations from rooms
			ArrayList<Integer> locationCapacity = this.getLocationCapacities(rooms);
			double[][] distances = instance.getDistances();
			// location network stays the same for morning and evening, and none-split
			
			for (Data d : data) {
				d.setDistanceMatrix(distances);
				d.setMaxDistanceInGraph(this.getMaxDistanceInGraph(distances));
				d.setLocationCapacity(locationCapacity);
				d.setCustomerToLocation(job2Location);
				d.setLocationsToCustomers(location2Job);
			}

			if (Config.solveAsTwoProblems) {
				ArrayList<double[][]> morningEveningJobs = this.getJobsForMorningAndEveningShift(realJobs);
				HashMap<Integer, ArrayList<Integer>> morningPredJobs = this.getPredJobsShift(predJobs, morningEveningJobs.get(0));
				HashMap<Integer, ArrayList<Integer>> eveningPredJobs = this.getPredJobsShift(predJobs, morningEveningJobs.get(1));
				this.handleDataTransfer(morningEveningJobs.get(0), data[0], morningPredJobs);
				this.handleDataTransfer(morningEveningJobs.get(1), data[1], eveningPredJobs);
			} else {
				//TODO Chris - add depot dummy job
				this.handleDataTransfer(realJobs, data[0], predJobs);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Handles Data transfer to initialize the attached data object.
	 * @param jobInformation
	 * @param data
	 */
	private void handleDataTransfer(double[][] jobInformation, Data data, HashMap<Integer, ArrayList<Integer>> predJobs) {
        List<Integer> customerIds = IntStream.rangeClosed(1, jobInformation[0].length-1).boxed().collect(Collectors.toList());
		int[] originalCustomerIds = DataUtils.convertDoubleArrToIntArr(jobInformation[6]);
        data.setEndOfPlanningHorizon((int) jobInformation[1][0]); // <-- Depot Info
		data.setCustomers(DataUtils.convertListToArray(customerIds));
        data.setOriginalCustomerIds(originalCustomerIds);
		data.setEarliestStartTimes(DataUtils.convertDoubleArrToIntArr(jobInformation[0]));
		data.setLatestStartTimes(DataUtils.convertDoubleArrToIntArr(jobInformation[1]));
		data.setServiceDurations(DataUtils.convertDoubleArrToIntArr(jobInformation[2]));
		data.setDemands(DataUtils.convertDoubleArrToIntArr(jobInformation[3]));
		data.setNCustomers(jobInformation[0].length-1);
		data.setRequiredSkillLvl(DataUtils.convertDoubleArrToIntArr(jobInformation[4]));
		data.setCustomersPreferredLocation(DataUtils.convertDoubleArrToIntArr(jobInformation[5]));
		data.setPredCustomers(predJobs);
		data.calculateAverageStartTimes();
		data.createSwappingCosts();
	}
	
	/**
	 * Retrieve the predecessor jobs of attached jobs information. 
	 * That is, the method iterates the job information and summarizes the predecessor
	 * jobs in a HashMap, where customer identifiers are used as keys and values are lists of 
	 * potential predecessor jobs
	 * @param predJobs: HashMap with all predecessor jobs
	 * @param jobInfo: job information - shift dependent
	 * @return HashMap with all relevant predecessor jobs
	 */
	private HashMap<Integer, ArrayList<Integer>> getPredJobsShift (HashMap<Integer, ArrayList<Integer>> predJobs, double[][] jobInfo) {
		HashMap<Integer, ArrayList<Integer>> shiftPredJobs = new HashMap<Integer, ArrayList<Integer>>();
		for (int i=1; i<jobInfo[0].length; i++) {
			int customerId = (int) jobInfo[6][i];
			shiftPredJobs.put(customerId, predJobs.get(customerId));
		}
		return shiftPredJobs;
	}
	
	/**
	 * Retrieve the jobs separates in morning and evening shifts. 
	 * The planning horizon consists of 540 min with a break 
	 * form 12am-1pm. Calculating with a time interval of 5 min, the
	 * break is between 48-60 (morning: 8am-12am = 240min; 240min/5 = 48)
	 * @param realJobs: (all) imported jobs
	 * @return List of morning jobs (index: 0) and evening jobs (index: 1)
	 */
	// Planning with 540 min after 8am (working start); 48-60 lunch time
	private ArrayList<double[][]> getJobsForMorningAndEveningShift(double[][] realJobs) {
		int endMorningShift = (int) Math.floor((double) 240 / this.planningInterval);
		int startEveningShift = (int) Math.floor((double) 300 / this.planningInterval);
		int shiftEnd = (int) Math.floor((double) 540 / this.planningInterval);
		
		// job information:
		// 0: earliest start time - 1:latest start time - 2:duration - 3:demand - 4: skill - 5: preferred location id

		int nEveningJobs = 0;
		int endOfPlanningHorizon = -1;
		for (int i = 0 ; i<realJobs[0].length; i++) {
			if (realJobs[1][i] > startEveningShift - Config.epsilon)
				nEveningJobs++;
			if ( (realJobs[1][i] + realJobs[2][i]) > endOfPlanningHorizon)
				endOfPlanningHorizon = (int) (realJobs[1][i] + realJobs[2][i]);
		}
		
		double[][] morningJobInformation = new double[7][realJobs[0].length-nEveningJobs+1];
		double[][] eveningJobInformation = new double[7][nEveningJobs+1];
		int morningIdx = 1;
		int eveningIdx = 1;

		// init depot values
		// =0 can be disregarded; however, for the sake of comprehensibility it is shown...
		// morning jobs
		morningJobInformation[0][0] = 0;
		morningJobInformation[1][0] = endMorningShift;
		morningJobInformation[2][0] = 0;
		morningJobInformation[3][0] = 0;
		morningJobInformation[4][0] = 0;
		morningJobInformation[5][0] = 0;
		morningJobInformation[6][0] = 0;
		// evening jobs
		eveningJobInformation[0][0] = startEveningShift; //TODO Chris: changed here something
		eveningJobInformation[1][0] = shiftEnd;
		eveningJobInformation[2][0] = 0;
		eveningJobInformation[3][0] = 0;
		eveningJobInformation[4][0] = 0;
		eveningJobInformation[5][0] = 0;
		eveningJobInformation[6][0] = 0;

		// check morning shift
		for (int i = 0; i<realJobs[0].length; i++) {
			int jobEarliestStart = (int) realJobs[0][i];
			int jobLatestStart = (int) realJobs[1][i];
			int jobDuration = (int) realJobs[2][i];
			int jobDemand = (int) realJobs[3][i];
			int jobSkill = (int) realJobs[4][i];
			int jobPreferredLocationId = (int) realJobs[5][i];
			int customerId = (int) realJobs[6][i];
			
			if (jobEarliestStart + jobDuration < endMorningShift + Config.epsilon) {
				morningJobInformation[0][morningIdx] = jobEarliestStart;
				morningJobInformation[1][morningIdx] = jobLatestStart;
				morningJobInformation[2][morningIdx] = jobDuration;
				morningJobInformation[3][morningIdx] = jobDemand;
				morningJobInformation[4][morningIdx] = jobSkill;
				morningJobInformation[5][morningIdx] = jobPreferredLocationId;
				morningJobInformation[6][morningIdx] = customerId;
				morningIdx ++;
			}
			
			if (jobLatestStart > startEveningShift - Config.epsilon) {
				eveningJobInformation[0][eveningIdx] = jobEarliestStart;
				eveningJobInformation[1][eveningIdx] = jobLatestStart;
				eveningJobInformation[2][eveningIdx] = jobDuration;
				eveningJobInformation[3][eveningIdx] = jobDemand;
				eveningJobInformation[4][eveningIdx] = jobSkill;
				eveningJobInformation[5][eveningIdx] = jobPreferredLocationId;
				eveningJobInformation[6][eveningIdx] = customerId;
				eveningIdx ++;
			}
		}
		ArrayList<double[][]> morningEveningInfo = new ArrayList<double[][]>();
		morningEveningInfo.add(morningJobInformation);
		morningEveningInfo.add(eveningJobInformation);
		return morningEveningInfo;
	}

	/**
	 * Retrieve the therapists separated in the morning and evening shift.
	 * The method checks whether the shift starts in the morning. If so,
	 * the therapist is added to the morning fleet, otherwise it is assigned
	 * to the evening fleet. First index 0 is morning shift, first index 1 is evening.
	 * @param therapists: (all) imported therapists
	 * @return array with morning (index:0) and evening (index:1) therapists
	 */
	private ImportedTherapist[][] getTherapistsForMorningAndEveningShift(ImportedTherapist[] therapists) {
		ArrayList<ImportedTherapist> auxMorning = new ArrayList<>();
		int indexMorning = 0;
		ArrayList<ImportedTherapist> auxEvening = new ArrayList<>();
		int indexEvening = 0;

		for (ImportedTherapist therapist: therapists) {
			if (therapist.isShortShift()) {
				if (therapist.isShiftStartMorning()) {
					therapist.setId(indexMorning++);  // re-index -> if there are n therapist, ids 0 to n-1 must exist
					auxMorning.add(therapist);
				} else {
					auxEvening.add(therapist);
					therapist.setId(indexEvening++);
				}
			}
		}
		// assert that no regular shifts exist
		assert (auxMorning.size() + auxEvening.size() == therapists.length):
				"Error: length of morning shift (" + auxMorning.size() +
						") and evening shift (" + auxEvening.size() +
						") not equal length of all shifts (" + therapists.length + ")";

		return new ImportedTherapist[][]{auxMorning.toArray(new ImportedTherapist[0]), 
										 auxEvening.toArray(new ImportedTherapist[0])};
	}

	/**
	 * Method to split regular shift into 2x short shifts (morning and evening)
	 * this can be done since all therapists have a lunch break from 12-1pm
	 * @param therapists therapists
	 * @return imported therapists (just split if long shift)
	 */
	private ImportedTherapist[] splitRegularShifts(ImportedTherapist[] therapists) {
		ArrayList<ImportedTherapist> listSplitShifts = new ArrayList<>();
		int idx = -1;
		// information about which therapists were split important to connect them after the optimization again
		for(ImportedTherapist input: therapists) {
			if(input.isShortShift()) {
				// if already short shift, only adjust id
				idx++;
				input.setId(idx);
				input.setWasSplit(false);
				listSplitShifts.add(input);
			} else {
				// if regular shift 
				// a) create morning shift
				idx++;
				ImportedTherapist morning = new ImportedTherapist(idx, input.getSkill(), true, true);
				morning.setWasSplit(true);
				listSplitShifts.add(morning);
				
				// b) create evening shift
				idx++;
				ImportedTherapist evening = new ImportedTherapist(idx, input.getSkill(), true, false);
				evening.setWasSplit(true);
				listSplitShifts.add(evening);
			}
		}
		return listSplitShifts.toArray(new ImportedTherapist[listSplitShifts.size()]);
	}

	/**
	 * Retrieve the job locations for the customers. The possible locations are 
	 * stored within a HashMap, where keys are the customer identifiers and the
	 * values are lists of possible location where a customer can be served. 
	 * Note (0 is reserved for the depots).
	 * @param importedRealJobs: imported job information
	 * @return HashMap containing the job identifiers per customer
	 */
	private HashMap<Integer, ArrayList<Integer>> getJobToLocation(ImportedJob[] importedRealJobs) {
		HashMap<Integer, ArrayList<Integer>> customerToLocations = new HashMap<Integer, ArrayList<Integer>>();
		for(int i = 0; i < importedRealJobs.length; i++) {
			ImportedJob im = importedRealJobs[i];
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int entryidx = 0 ; entryidx<im.getLocationIds().length; entryidx++)
				tmp.add(im.getLocationIds()[entryidx]);
			customerToLocations.put(im.getId()+1, tmp);
		}
		customerToLocations.put(0, new ArrayList<Integer>(Arrays.asList(0)));
		return customerToLocations;
	}
	
	/**
	 * Retrieve a mapping from locations to the possible customers/patients/job-identifiers.
	 * Note: index 0 is reserved for the depots
	 * @param importedRealJobs: imported job information
	 * @return HashMap mapping from locations to customers identifiers
	 */
	private HashMap<Integer, ArrayList<Integer>> getLocationToJob (ImportedJob[] importedRealJobs) {
		HashMap<Integer, ArrayList<Integer>> locationsToCustomers = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < importedRealJobs.length; i++) {
			ImportedJob im = importedRealJobs[i];
			for (int locEntry = 0 ; locEntry<im.getLocationIds().length; locEntry++) {
				if (locationsToCustomers.get(im.getLocationIds()[locEntry]) == null)
					locationsToCustomers.put(im.getLocationIds()[locEntry], new ArrayList<Integer>());
				if (!locationsToCustomers.get(im.getLocationIds()[locEntry]).contains(im.getId() + 1))
					locationsToCustomers.get(im.getLocationIds()[locEntry]).add(im.getId() + 1);
			}
		}
		locationsToCustomers.put(0, new ArrayList<Integer>());
		return locationsToCustomers;
	}
	
	/**
	 * Retrieve all the predecessor jobs being defined in the input data. 
	 * They are returned within a HashMap where the key are the customer 
	 * identifiers and the values are lists containing the 
	 * identifiers (+1) of the predecessor jobs. Note: +1 because 0 is reserved
	 * for the depot information.
	 * @param importedRealJobs: imported jobs
	 * @return HashMap containing the predecessor jobs
	 */
	private HashMap<Integer, ArrayList<Integer>> generatePredJobs(ImportedJob[] importedRealJobs) {
		HashMap<Integer, ArrayList<Integer>> predJobs = new HashMap<Integer, ArrayList<Integer>>();
		for(int i = 0; i < importedRealJobs.length; i++) {
			ImportedJob im = importedRealJobs[i];
			ArrayList<Integer> listPredJobs = new ArrayList<Integer>();
			for (int predJobId : im.getPredJobsIds())
				listPredJobs.add(predJobId + 1);
			predJobs.put(im.getId() + 1, listPredJobs);
		}
		return predJobs;
	}
	
	/**
	 * Get the job information from the imported jobs as a two-dimensional
	 * double array. The format is as follows:
	 * 0 - earliest start times
	 * 1 - latest start times
	 * 2 - service duration 
	 * 3 - demand
	 * 4 - required skill of the job
	 * 5 - preferred location id
	 * 6 - id of the job (+1; as 0 is reserved for the depot)
	 * @param importedRealJobs: imported job information
	 * @return job information as two-dimensional array
	 */
	private double[][] generateRealJobs(ImportedJob[] importedRealJobs) {		
		// init jobs only with job id
		double[][] jobInformation = new double[7][importedRealJobs.length];
		
		for(int i = 0; i < jobInformation[0].length; i++) {
			ImportedJob im = importedRealJobs[i];
			jobInformation[0][i] = im.getEarliestStart();
			jobInformation[1][i] = im.getLatestStart();
			jobInformation[2][i] = im.getDuration();
			jobInformation[3][i] = 1;
			jobInformation[4][i] = im.getSkill();
			jobInformation[5][i] = im.getPreferredLocationId();
			jobInformation[6][i] = im.getId() + 1;
		}
		return jobInformation;
	}
	
	/**
	 * Defines the maximal capacity slots for the various locations being 
	 * attached as parameters. If a capacity is set within the input of 
	 * a hospital instance it is pre-defined, otherwise the capacity is 
	 * set to a number that no restrictions can occur.
	 * @param rooms: imported locations
	 * @return list of capacity slots
	 */
	private ArrayList<Integer> getLocationCapacities(ImportedRoom[] rooms) {
		ArrayList<Integer> locationCapacity = new ArrayList<Integer>();
		for(int i = 0; i < rooms.length; i++) {
			ImportedRoom r = rooms[i];
			if (r.getCapacity() > 0)
				locationCapacity.add(r.getCapacity());
			else
				locationCapacity.add(5);
		}
		return locationCapacity;
	}
	
	/**
	 * Returns the maximal distance with the attached distance matrix.
	 * @param distMatrix matrix containing distances between locations
	 * @return maximal value in the matrix
	 */
	private double getMaxDistanceInGraph(double[][] distMatrix) {
		double max = -1;
		for (int i=0; i<distMatrix.length; i++) 
			for (int j=0; j<distMatrix[i].length; j++)
				if (distMatrix[i][j] > max)
					max = distMatrix[i][j];
		return max;
	}

	/**
	 * Main function for debugging purpose.
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
    	HospitalInstanceLoader loader = new HospitalInstanceLoader();
    	loader.loadHospitalInstanceFromJSON("hospital_instance_i020_b1_f6_v01");
    }
}
