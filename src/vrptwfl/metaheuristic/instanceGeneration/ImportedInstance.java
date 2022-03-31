package vrptwfl.metaheuristic.instanceGeneration;

public class ImportedInstance {
	
	private ImportedTherapist[] therapists;
	private ImportedRoom[] rooms;
	private ImportedJob[] jobs;
	private double[][] distances;
	
	public ImportedTherapist[] getTherapists() {
		return therapists;
	}
	public void setTherapists(ImportedTherapist[] therapists) {
		this.therapists = therapists;
	}
	public ImportedRoom[] getRooms() {
		return rooms;
	}
	public void setRooms(ImportedRoom[] rooms) {
		this.rooms = rooms;
	}
	public double[][] getDistances() {
		return distances;
	}
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	public ImportedJob[] getJobs() {
		return jobs;
	}
	public void setJobs(ImportedJob[] jobs) {
		this.jobs = jobs;
	}	
}
