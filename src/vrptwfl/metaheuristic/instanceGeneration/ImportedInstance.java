package vrptwfl.metaheuristic.instanceGeneration;

/**
 * Helper class for loading hospital instances. 
 * The class stores the loaded therapist, rooms, jobs and distances between
 * the various locations (=rooms)
 *
 * @author Christian M.M. Frey
 *
 */
public class ImportedInstance {
	
	private ImportedTherapist[] therapists;
	private ImportedRoom[] rooms;
	private ImportedJob[] jobs;
	private double[][] distances;
	
	//
	// GETTERS
	//
	/**
	 * Retrieve the loaded therapists.
	 * @return array with ImportedTherapist objects
	 */
	public ImportedTherapist[] getTherapists() {
		return therapists;
	}
	
	/**
	 * Retrieve the loaded rooms.
	 * @return array with ImportedRooms objects
	 */
	public ImportedRoom[] getRooms() {
		return rooms;
	}
	
	/**
	 * Retrieve the loaded distances stored as a two-dimensional array
	 * @return two-dimensional array storing distances between rooms
	 */
	public double[][] getDistances() {
		return distances;
	}

	/**
	 * Retrieve the loaded jobs.
	 * @return array with ImportedJobs objects
	 */
	public ImportedJob[] getJobs() {
		return jobs;
	}

	//
	// SETTERS
	//
	/**
	 * Sets the therapists array
	 * @param therapists array with ImportedTherapist objects
	 */
	public void setTherapists(ImportedTherapist[] therapists) {
		this.therapists = therapists;
	}
	
	/**
	 * Sets the rooms objects
	 * @param rooms array with ImportedRoom objects
	 */
	public void setRooms(ImportedRoom[] rooms) {
		this.rooms = rooms;
	}
	
	/**
	 * Sets the two-dimensional array storing the distances between the 
	 * various locations
	 * @param distances two-dimensional array storing distances 
	 */
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	/**
	 * Sets the jobs array
	 * @param jobs array with ImportedJob objects
	 */
	public void setJobs(ImportedJob[] jobs) {
		this.jobs = jobs;
	}	
}
