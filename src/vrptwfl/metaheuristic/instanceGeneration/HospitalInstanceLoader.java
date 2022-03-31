package vrptwfl.metaheuristic.instanceGeneration;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.DataUtils;
import vrptwfl.metaheuristic.Config;

public class HospitalInstanceLoader {
	private int planningInterval;

	// Data is array because we have two data sets for morning and evening if problem is solved as two problems.
	public Data[] loadHospitalInstanceFromJSON(String instanceName) {
		
		// planning interval
		this.planningInterval = Config.planningIntervals; // default 5 min
		int vehicleCapacity = Config.maxCapacityVehicles; // big enough to never be critical

		//this.printStartMessage(logger);

		Data[] data;
		if (Config.solveAsTwoProblems) {
			data = new Data[2];
			data[0] = new Data(instanceName);
			data[1] = new Data(instanceName);
		} else {
			data = new Data[1];
			data[0] = new Data(instanceName);
		}

		// create gson loader
		HospitalGSONLoader dataLoader = new HospitalGSONLoader();
		
		try {
			// load instance from json
			ImportedInstance instance = dataLoader.loadHospitalInstanceFromJSON(instanceName);
			
			ImportedTherapist[] therapists =  instance.getTherapists(); 
			
			// if split regular shift is activated 
			if(Config.splitRegularShift) {
				therapists = this.splitRegularShifts(therapists);
			}

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
				data[1].setNVehicles(eveningTherapists.length);
				data[0].setVehicleCapacity(vehicleCapacity);
				data[1].setVehicleCapacity(vehicleCapacity);
				data[0].setVehiclesSkillLvl(DataUtils.convertListToArray(mTherapistsSkill));
				data[1].setVehiclesSkillLvl(DataUtils.convertListToArray(eTherapistsSkill));
				
				
				
				// Vehicle[] vehicles = this.generateVehicles(therapists, wp, vehicleCapacity);
				// _data.setVehicles(vehicles);

				// this.generateWorkingPatternAndVehicles(data[0], morningTherapists, vehicleCapacity, logger, 0);
				// this.generateWorkingPatternAndVehicles(data[1], eveningTherapists, vehicleCapacity, logger, 1);
			} else {
				//this.generateWorkingPatternAndVehicles(data[0], therapists, vehicleCapacity, logger, 0);
			}

			ImportedRoom[] rooms = instance.getRooms(); //dataLoader.loadRoomsFromJSON(instanceName);
			ImportedJob[] importedRealJobs = instance.getJobs();
			double[][] realJobs = this.generateRealJobs(importedRealJobs);
//			Job[] realJobs = this.generateRealJobs(importedRealJobs, locations);			
			HashMap<Integer, ArrayList<Integer>> job2Location = getJobToLocation(importedRealJobs);

			
			// generate locations from rooms
			ArrayList<Integer> locationCapacity = this.getLocationCapacities(rooms);
			double[][] distances = instance.getDistances(); //dataLoader.loadDistancesFromJSON(instanceName);
			// location network stays the same for morning and evening, and none-split
			
			for (Data d : data) {
//				datum.setLocations(locations);
//				datum.setTravelCostMatrix(distances);
//				datum.setTravelDurationMatrix(distances);
				d.setDistanceMatrix(distances);
				d.setMaxDistanceInGraph(this.getMaxDistanceInGraph(distances));
				d.setLocationCapacity(locationCapacity);
				d.setCustomerToLocation(job2Location);
			}

			// print info
			// if(Config.printHospitalLoaderInfo) {
			//  	this.printLocationInformation(locations, distances, logger);
			// }


			if (Config.solveAsTwoProblems) {
				// Job[][] aux = this.getJobsForMorningAndEveningShift(realJobs, data);
				// Job[] realJobsMorning = aux[0];
				// Job[] realJobsEvening = aux[1];
				// data[0].setRealJobs(realJobsMorning);
				// data[1].setRealJobs(realJobsEvening);
				ArrayList<double[][]> morningEveningJobs = this.getJobsForMorningAndEveningShift(realJobs);
				this.handleDataTransfer(morningEveningJobs.get(0), data[0]);
				this.handleDataTransfer(morningEveningJobs.get(1), data[1]);
			} else {
				this.handleDataTransfer(realJobs, data[0]);
				// data[0].setRealJobs(realJobs);
			}

			// print info --> TODO maybe separate method
//			if(Config.printHospitalLoaderInfo) {
//				this.printRealJobsInformation(realJobs, logger);
//			}
			
		} catch (IOException e) {
			//logger.trackProgress("ERROR: Failed to load instance '" + instanceName + "' from json.\n");
			e.printStackTrace();
		}
//		for (Data datum : data) {
//			datum.additionalInitsHospitalInstances();
//		}

		/*
		 * Check instance
		 */
//		boolean instanceCheckedSuccessfully = true;
//		for (Data datum: data) {
//			InstanceChecker checker = new InstanceChecker(datum, logger);
//			if (!checker.checkInstance()) {
//				instanceCheckedSuccessfully = false;
//				break;
//			}
//
//		}

//		if(instanceCheckedSuccessfully) {
//			//this.printFinishOnSuccessMessage(logger);
//			return data;
//		} else {
//			//this.printFinishOnFailureMessage(logger);
//			return null;
//		}
		return data;
	}
	
	/**
	 * Handles Data transfer to initialize the attached data object
	 * @param jobInformation
	 * @param data
	 */
	private void handleDataTransfer(double[][] jobInformation, Data data) {
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
		data.calculateAverageStartTimes();
	}
	
	private ArrayList<double[][]> getJobsForMorningAndEveningShift(double[][] realJobs) {
		int endMorningShift = (int) Math.floor((double) 240 / this.planningInterval);
		int startEveningShift = (int) Math.floor((double) 300 / this.planningInterval);
		int shiftEnd = (int) Math.floor((double) 540 / this.planningInterval);
		
//		jobInformation[0][i] = im.getEarliestStart();
//		jobInformation[1][i] = im.getLatestStart();
//		jobInformation[2][i] = im.getDuration();
//		jobInformation[3][i] = 1;
//		jobInformation[4][i] = im.getSkill();
//		jobInformation[5][i] = im.getPreferredLocationId();
		
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
		morningJobInformation[0][0] = 0;
		morningJobInformation[1][0] = endMorningShift;
		morningJobInformation[2][0] = 0;
		morningJobInformation[3][0] = 0;
		morningJobInformation[4][0] = 0;
		morningJobInformation[5][0] = 0;
		morningJobInformation[6][0] = 0;
		eveningJobInformation[0][0] = 0;
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
//		if (job.getEarliestStart() + job.getDuration() < endMorningShift + Config.epsilon) {
//			job.setMorningJob(true);
//			job.setId(indexMorning++);
//			realJobsMorning.add(job);
//		} else {
//			job.setMorningJob(false);
//		}
//		// check evening shift
//		if (job.getLatestStart() > startEveningShift - Config.epsilon) {
//			job.setEveningJob(true);
//			job.setId(indexEvening++);
//			realJobsEvening.add(job);
//		} else {
//			job.setEveningJob(false);
//		}
		
		// TODO: Chris - handle mandatory jobs
		ArrayList<double[][]> morningEveningInfo = new ArrayList<double[][]>();
		morningEveningInfo.add(morningJobInformation);
		morningEveningInfo.add(eveningJobInformation);
		return morningEveningInfo;

	}

//	private Job[][] getJobsForMorningAndEveningShift(Job[] realJobs, Data[] _data) {
//
//		ArrayList<Job> realJobsMorning = new ArrayList<>();
//		int indexMorning = 0;
//		double endMorningShift = -Double.MAX_VALUE;
//		if (_data[0].getWorkingPattern()[0] != null) {
//			endMorningShift = _data[0].getWorkingPattern()[0].getShiftEnd();
//		}
//
//		ArrayList<Job> realJobsEvening = new ArrayList<>();
//		int indexEvening = 0;
//		double startEveningShift = Double.MAX_VALUE;
//		if (_data[1].getWorkingPattern()[0] != null) {
//			startEveningShift = _data[1].getWorkingPattern()[0].getShiftStart();
//		}
//
//		// decide if job belongs to morning or evening shift
//		for (Job job: realJobs) {
//			// check morning shift
//			if (job.getEarliestStart() + job.getDuration() < endMorningShift + Config.epsilon) {
//				job.setMorningJob(true);
//				job.setId(indexMorning++);
//				realJobsMorning.add(job);
//			} else {
//				job.setMorningJob(false);
//			}
//			// check evening shift
//			if (job.getLatestStart() > startEveningShift - Config.epsilon) {
//				job.setEveningJob(true);
//				job.setId(indexEvening++);
//				realJobsEvening.add(job);
//			} else {
//				job.setEveningJob(false);
//			}
//
//			assert !job.isMorningJob() || !job.isEveningJob():
//					"Error: job " + job.getId() + " is morning and evening job. This cannot be handled in solving two separate problems.";
//
//			assert job.isMorningJob() || job.isEveningJob():
//					"Error: job " + job.getId() + " is neither morning nor evening job. Thus job would not be planed.";
//		}
//
//		// process mandatory predecessor and successor jobs
//		// if job is morning job and mandatory successor is in evening -> delete from mandatory list
//		// if job is evening job and mandatory predecessor is in morning -> delete from mandatory list
//		for (Job job: realJobsMorning) {
////			job.getMandatorySuccJobs().removeIf(aux -> aux.isEveningJob());
//			job.getMandatorySuccJobs().removeIf(Job::isEveningJob);
//		}
//		for (Job job: realJobsEvening) {
////			job.getMandatoryPredJobs().removeIf(aux -> aux.isMorningJob());
//			job.getMandatoryPredJobs().removeIf(Job::isMorningJob);
//		}
//
//		return new Job[][] {realJobsMorning.toArray(new Job[0]), realJobsEvening.toArray(new Job[0])};
//	}

//	private void printVehicleInformation(Vehicle[] vehicles, WorkingPattern[] wp, Logger logger, int dataArrayId) {
//		logger.writeString(5, "Print loader info: vehicles (dataArray[" + dataArrayId + "])\n");
//		for(Vehicle v: vehicles) {
//			logger.writeString(5, v.toString() + "\n");
//		}
//		logger.writeString(5, "\n");
//
//		logger.writeString(5, "Print loader info: working patterns (dataArray[\" + dataArrayId + \"])\n");
//		for(WorkingPattern w: wp) {
//			logger.writeString(5, w.toString() + "\n");
//		}
//		logger.writeString(5, "\n");
//	}

	// first index 0 is morning shift, first index 1 is evening
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
		int connectionId = -1;
		// information about which therapists were split important to connect them after the optimization again
		for(ImportedTherapist input: therapists) {
			if(input.isShortShift()) {
				// if already short shift, only adjust id
				idx++;
				input.setId(idx);
				input.setWasSplit(false);
				input.setConnectionId(null);
				listSplitShifts.add(input);
			} else {
				// if regular shift 
				connectionId++;
				// a) create morning shift
				idx++;
				ImportedTherapist morning = new ImportedTherapist(idx, input.getSkill(), true, true);
				morning.setWasSplit(true);
				morning.setConnectionId(connectionId);
				listSplitShifts.add(morning);
				
				// b) create evening shift
				idx++;
				ImportedTherapist evening = new ImportedTherapist(idx, input.getSkill(), true, false);
				evening.setWasSplit(true);
				evening.setConnectionId(connectionId);
				listSplitShifts.add(evening);
			}
		}
//		ImportedTherapist[] splittedTherapists = listSplitShifts.toArray(new ImportedTherapist[listSplitShifts.size()]);
//		return splittedTherapists;
		return listSplitShifts.toArray(new ImportedTherapist[listSplitShifts.size()]);
		// return listSplitShifts.toArray(new ImportedTherapist[0]);
	}

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
	
	private double[][] generateRealJobs(ImportedJob[] importedRealJobs) {
		
		// init jobs only with job id
		// Job[] jobs = new Job[importedRealJobs.length];
		double[][] jobInformation = new double[7][importedRealJobs.length];
//		for(int i = 0; i < importedRealJobs.length; i++) {
//			jobs[i] = new Job(i);
//		}
		for(int i = 0; i < jobInformation[0].length; i++) {
			ImportedJob im = importedRealJobs[i];
			jobInformation[0][i] = im.getEarliestStart();
			jobInformation[1][i] = im.getLatestStart();
			jobInformation[2][i] = im.getDuration();
			jobInformation[3][i] = 1;
			jobInformation[4][i] = im.getSkill();
			jobInformation[5][i] = im.getPreferredLocationId();
			jobInformation[6][i] = im.getId();
		}

		//TODO Chris - mandatory jobs
//		for(int i = 0; i < importedRealJobs.length; i++) {
//			// define locations for job
//			// jobs[i].setPreferredLocation(this.getLocationFromId(locations, im.getPreferredLocationId()));
//			jobs[i].setLocations(this.generateLocationsForJob(im, locations));
//			
//			// define pred jobs for job
//			jobs[i].setMandatoryPredJobs(this.generatePredJobs(im, jobs));
//		}
//
//		this.generateMandatorySuccJobs(jobs);
		
		return jobInformation;
	}
	
	private ArrayList<Integer> getLocationCapacities(ImportedRoom[] rooms) {
		ArrayList<Integer> locationCapacity = new ArrayList<Integer>();
		for(int i = 0; i < rooms.length; i++) {
			ImportedRoom r = rooms[i];
			if (r.getCapacity() > 0)
				locationCapacity.add(r.getCapacity());
			else
				locationCapacity.add(1);
		}
		return locationCapacity;
	}
	
	private double getMaxDistanceInGraph(double[][] distMatrix) {
		double max = -1;
		for (int i=0; i<distMatrix.length; i++) 
			for (int j=0; j<distMatrix[i].length; j++)
				if (distMatrix[i][j] > max)
					max = distMatrix[i][j];
		return max;
	}
	


	//
	// CODE FROM BAD CODE / EXACT PROCEDURE
	//
	private void generateMandatorySuccJobs(Job[] jobs) {
		for(Job job: jobs) {
			if(job.getMandatoryPredJobs().size() > 0) {
				for(Job pred: job.getMandatoryPredJobs()) {
					pred.addMandatorySuccJob(job);
				}
			}
		}		
	}

	private ArrayList<Job> generatePredJobs(ImportedJob im, Job[] jobs) {
		ArrayList<Job> predJobs = new ArrayList<>();

		if(im.getPredJobsIds().length == 0) {
			return predJobs;
		} else {
			for(Integer idx: im.getPredJobsIds()) {
				predJobs.add(jobs[idx]);
			}
			return predJobs;
		}
	}

	private ArrayList<Location> generateLocationsForJob(ImportedJob im, Location[] locations) {
		ArrayList<Location> locForJob = new ArrayList<>();
		for(Integer idx: im.getLocationIds()) {
			locForJob.add(this.getLocationFromId(locations, idx));
		}
		return locForJob;
	}

	
	private void setNVehiclesPerWorkingPattern(WorkingPattern[] wp, Vehicle[] vehicles) {
		for(WorkingPattern w: wp) {
			int count = 0;
			for(Vehicle v: vehicles) {
				if(v.getWorkingPattern().getId() == w.getId()) {
					count++;
				}
			}
			w.setNVehiclesInPattern(count);
		}
	}

	private Vehicle[] generateVehicles(ImportedTherapist[] therapists, WorkingPattern[] wp, int vehicleCapacity) {
		Vehicle[] vehicles = new Vehicle[therapists.length];
		for(ImportedTherapist t: therapists) {
			vehicles[t.getId()] = new Vehicle(t.getId(), vehicleCapacity, this.getWorkingPatternFromTherapist(t, wp), t.getSkill(), t.isWasSplit(), t.getConnectionId());
		}
		return vehicles;
	}

	
	/**
	 * Planning with 540 min after 8am (working start); 48-60 mittag!
	 * @param t
	 * @param wp
	 * @return
	 */
	private WorkingPattern getWorkingPatternFromTherapist(ImportedTherapist t, WorkingPattern[] wp) {
		int shiftStart = 0;
		int shiftEnd;
		if(!t.isShortShift()) {
			shiftEnd = (int)Math.floor((double)540/this.planningInterval);
		} else {
			if(t.isShiftStartMorning()) {
				shiftEnd = (int)Math.floor((double)240/this.planningInterval);
			} else {
				shiftStart = (int)Math.floor((double)300/this.planningInterval);
				shiftEnd = (int)Math.floor((double)540/this.planningInterval);
			}
		}
				
		for(WorkingPattern w: wp) {
			if(w.getShiftStart() == shiftStart && w.getShiftEnd() == shiftEnd) {
				return w;
			}
		}
		
		return null;
	}

	private WorkingPattern[] generateWorkingPattern(ImportedTherapist[] therapists) {
		
		ArrayList<WorkingPattern> wpList = new ArrayList<>();
		int wp_id = -1;
		
		boolean longShift = false;
		boolean shortShiftEarly = false;
		boolean shortShiftLate = false;

		// TO DO: hard coded start times...
		for(ImportedTherapist t: therapists) {
			if(!t.isShortShift() && !longShift) {
				longShift = true;
				wp_id++;
				int shiftEnd = (int)Math.floor((double)540/this.planningInterval);
				wpList.add(new WorkingPattern(wp_id, 0, shiftEnd, true, false, 0.0));
			} else {
				if(t.isShiftStartMorning() && t.isShortShift() && !shortShiftEarly) {
					shortShiftEarly = true;
					wp_id++;
					int shiftEnd = (int)Math.floor((double)240/this.planningInterval);
					wpList.add(new WorkingPattern(wp_id, 0, shiftEnd, false, true,0.0));
				} else if (t.isShortShift() && !shortShiftLate){
					shortShiftLate = true;
					wp_id++;
					int shiftStart = (int)Math.floor((double)300/this.planningInterval);
					int shiftEnd = (int)Math.floor((double)540/this.planningInterval);
					wpList.add(new WorkingPattern(wp_id, shiftStart, shiftEnd, false, false,0.0));
				}
			}
		}
		
		WorkingPattern[] wp = new WorkingPattern[wpList.size()];
		wp = wpList.toArray(wp);
		
		return wp;
	}
	
	private void generateWorkingPatternAndVehicles(Data _data, ImportedTherapist[] therapists, int vehicleCapacity, Logger logger, int dataArrayId) {
		WorkingPattern[] wp = this.generateWorkingPattern(therapists);
		_data.setWorkingPattern(wp);

		// generate vehicles
		Vehicle[] vehicles = this.generateVehicles(therapists, wp, vehicleCapacity);
		_data.setVehicles(vehicles);

		this.setNVehiclesPerWorkingPattern(wp, vehicles);

		if(Config.printHospitalLoaderInfo) {
			this.printVehicleInformation(vehicles, wp, logger, dataArrayId);
		}
	}


	//
	// DEPRECATED
	//
	
	// PRINTING METHODS
//	private void printStartMessage(Logger logger) {
//		logger.writeString(10, "\ngenerate instance\n");
//		logger.writeString(5, " - start generating instance\n");
//	}
//	
//	private void printFinishOnSuccessMessage(Logger logger) {
//		logger.writeString(5, " - Instance successfully generated\n");		
//	}
//	
//	private void printFinishOnFailureMessage(Logger logger) {
//		logger.writeString(10, " - Instance could not be generated\n");
//		logger.writeString(10, " - Exit program!\n");
//		System.exit(0);
//	}
//	
//	private void printLocationInformation(Location[] locations, double[][] distances, Logger logger) {
//		logger.writeString(5, "Print loader info: locations\n");
//		for(Location l: locations) {
//			logger.writeString(5, l.toString() + "\n");
//		}
//		logger.writeString(5, "\n");
//
//		if(SolverConfiguration.printTravelCostMatrix) {
//
//		// headline
//		logger.writeString(5, "Print cost matrix:\n");
//		for(Location l: locations) {
//			logger.writeString(5, "\tl_" + l.getId());
//		}
//		logger.writeString(5, "\n");
//
//		for(int i = 0; i < distances.length; i++) {
//			logger.writeString(5, "r_" + i);
//			for(int j = 0; j < distances[i].length; j++) {
//				logger.writeString(5, "\t" + distances[i][j]);
//			}
//			logger.writeString(5, "\n");
//		}
//		logger.writeString(5, "\n");
//	}
//}
//
//	private void printRealJobsInformation(Job[] realJobs, Logger logger) {
//		logger.writeString(5, "Print loader info: real jobs\n");
//		for(Job i: realJobs) {
//			logger.writeString(5, i.toString() + "\n");
//		}
//		logger.writeString(5, "\n");
//	}
	

	//
	// FUNCTIONALITY
	//
//	private Location getLocationFromId(Location[] locations, int idx) {
//	for(Location l: locations) {
//		if(l.getId() == idx) {
//			return l;
//		}
//	}
//	return null;
//}

	
//private Location[] generateLocations(ImportedRoom[] rooms) {
//	Location[] locations = new Location[rooms.length];
//	
//	for(int i = 0; i < rooms.length; i++) {
//		ImportedRoom r = rooms[i];
//		locations[i] = new Location(r.getId(), r.isDepot(), r.getCapacity());
//	}
//
//	return locations;
//}
	
	
//private Job[] generateRealJobs(ImportedJob[] importedRealJobs, Location[] locations) {
//	
//	// init jobs only with job id
//	Job[] jobs = new Job[importedRealJobs.length];
//	for(int i = 0; i < importedRealJobs.length; i++) {
//		jobs[i] = new Job(i);
//	}
//
//	for(int i = 0; i < importedRealJobs.length; i++) {
//		ImportedJob im = importedRealJobs[i];
//		jobs[i].setEarliestStart(im.getEarliestStart());
//		jobs[i].setLatestStart(im.getLatestStart());
//		jobs[i].setDemand(1); // is constant here because doesn't matter for therapist
//		jobs[i].setDuration(im.getDuration());
//		jobs[i].setQualification(im.getSkill());
//		
//		// define locations for job
//		jobs[i].setPreferredLocation(this.getLocationFromId(locations, im.getPreferredLocationId()));
//		jobs[i].setLocations(this.generateLocationsForJob(im, locations));
//		
//		// define pred jobs for job
//		jobs[i].setMandatoryPredJobs(this.generatePredJobs(im, jobs));
//		
//	}
//
//	this.generateMandatorySuccJobs(jobs);
//	
//	return jobs;
//}


    public static void main(String[] args) throws IOException {
    	HospitalInstanceLoader loader = new HospitalInstanceLoader();
        Data[] d;
        d = loader.loadHospitalInstanceFromJSON("hospital_instance_i020_b1_f6_v02");
    }
}
