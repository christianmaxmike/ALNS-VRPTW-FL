package vrptwfl.metaheuristic.instanceGeneration;

public class ImportedRoom {

	private int id;
	private int	capacity;
	private boolean	depot;
	
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public boolean isDepot() {
		return depot;
	}

	public void setDepot(boolean depot) {
		this.depot = depot;
	}

	@Override
    public String toString() {
        return "id = " + this.id + "\tcapacity = " + this.capacity + "\tdepot = " + this.depot;
    }
	
}
