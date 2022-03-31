package vrptwfl.metaheuristic.instanceGeneration;

public class ImportedJob {

	private int id;
	private double earliestStart;
	private double latestStart;
	private double duration;
	private int skill;
	private int[] locationIds;
	private int preferredLocationId;
	private int[] predJobsIds;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getEarliestStart() {
		return earliestStart;
	}
	public void setEarliestStart(double earliestStart) {
		this.earliestStart = earliestStart;
	}
	public double getLatestStart() {
		return latestStart;
	}
	public void setLatestStart(double latestStart) {
		this.latestStart = latestStart;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	public int getSkill() {
		return skill;
	}
	public void setSkill(int skill) {
		this.skill = skill;
	}
	public int[] getLocationIds() {
		return locationIds;
	}
	public void setLocationIds(int[] locationIds) {
		this.locationIds = locationIds;
	}
	public int getPreferredLocationId() {
		return preferredLocationId;
	}
	public void setPreferredLocationId(int preferredLocationId) {
		this.preferredLocationId = preferredLocationId;
	}
	public int[] getPredJobsIds() {
		return predJobsIds;
	}
	public void setPredJobsIds(int[] predJobsIds) {
		this.predJobsIds = predJobsIds;
	}
	
	
}
