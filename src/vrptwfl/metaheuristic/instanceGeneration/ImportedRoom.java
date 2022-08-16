package vrptwfl.metaheuristic.instanceGeneration;

/**
 * Helper class for loading room instances. 
 * The class stores the id, capacity and whether it is a depot or not
 *
 * @author Christian M.M. Frey
 *
 */
public class ImportedRoom {

	private int id;
	private int	capacity;
	private boolean	depot;
	
	//
	// GETTERS
	//
	/**
	 * Retrieve the id of the room (=location).
	 * @return id of the room
	 */
    public int getId() {
		return id;
	}

    /**
     * Retrieve the maximal capacity of the room.
     * @return capacity of the room
     */
    public int getCapacity() {
    	return capacity;
    }

    /**
     * Retrieve whether the room(=location) is a depot or not.
     * @return is room a depot
     */
    public boolean isDepot() {
    	return depot;
    }

    //
    // SETTERS
    //
    /**
     * Sets the room's identifier (=id of location).
     * @param id room's identifier
     */
    public void setId(int id) {
		this.id = id;
	}

    /**
     * Sets the capacity of the room(=location).
     * @param capacity: capacity of the room
     */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Sets whether the room is a depot or not.
	 * @param depot: is room a depot
	 */
	public void setDepot(boolean depot) {
		this.depot = depot;
	}

	//
	// PRINTING
	//
	/**
	 * String formatting of the ImportedRoom object.
	 * {@inheritDoc}
	 */
	@Override
    public String toString() {
        return "id = " + this.id + "\tcapacity = " + this.capacity + "\tdepot = " + this.depot;
    }
}