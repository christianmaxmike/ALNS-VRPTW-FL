package vrptwfl.metaheuristic.instanceGeneration;

/**
 * Helper class for loading job instances. 
 * The class stores the id, earliestStart, latestStart, duration time,
 * the required skill, the possible locations, the preferred location,
 * as well as the list of predecessor jobs.
 *
 * @author Christian M.M. Frey
 *
 */
public class ImportedJob {

	private int id;
	private double earliestStart;
	private double latestStart;
	private double duration;
	private int skill;
	private int[] locationIds;
	private int preferredLocationId;
	private int[] predJobsIds;

	//
	// GETTERS
	//
	/**
	 * Retrieve the job id.
	 * @return job id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Retrieve the earliest starting time of the job.
	 * @return earliest starting time as double value
	 */
	public double getEarliestStart() {
		return earliestStart;
	}
	
	/**
	 * Retrieve the latest starting time of the job.
	 * @return latest starting time as double value
	 */
	public double getLatestStart() {
		return latestStart;
	}

	/**
	 * Retrieve the duration of the service.
	 * @return service time of the job
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Retrieve the required skill level for processing the job.
	 * @return required skill level
	 */
	public int getSkill() {
		return skill;
	}

	/**
	 * Retrieve the possible location identifiers the job can be served.
	 * @return possible location identifiers
	 */
	public int[] getLocationIds() {
		return locationIds;
	}

	/**
	 * Retrieve the location id of the preferred location the job can be handled.
	 * @return preferred location id
	 */
	public int getPreferredLocationId() {
		return preferredLocationId;
	}

	/**
	 * Retrieve the array of predecessor jobs.
	 * @return predecessor jobs
	 */
	public int[] getPredJobsIds() {
		return predJobsIds;
	}
	
	//
	// SETTERS
	//
	/**
	 * Sets the id of the job(=patient).
	 * @param id : job id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the earliest starting time of the job.
	 * @param earliestStart: earliest starting time
	 */
	public void setEarliestStart(double earliestStart) {
		this.earliestStart = earliestStart;
	}
	
	/**
	 * Sets the latest starting time of the job.
	 * @param latestStart: latest starting time
	 */
	public void setLatestStart(double latestStart) {
		this.latestStart = latestStart;
	}
	
	/**
	 * Sets the duration time of the job.
	 * @param duration: duration time of the job
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	/**
	 * Sets the required skill level of the job.
	 * @param skill: required skill level
	 */
	public void setSkill(int skill) {
		this.skill = skill;
	}
	
	/**
	 * Sets the possible location identifiers the job can be served.
	 * @param locationIds: possible location identifiers
	 */
	public void setLocationIds(int[] locationIds) {
		this.locationIds = locationIds;
	}
	
	/**
	 * Sets the location id of the preferred location identifier.
	 * @param preferredLocationId: preferred location identifier
	 */
	public void setPreferredLocationId(int preferredLocationId) {
		this.preferredLocationId = preferredLocationId;
	}
	
	/**
	 * Sets the array of predecessor jobs that have to be handled before the job can be processed.
	 * @param predJobsIds: array of predecessor jobs
	 */
	public void setPredJobsIds(int[] predJobsIds) {
		this.predJobsIds = predJobsIds;
	}
}
