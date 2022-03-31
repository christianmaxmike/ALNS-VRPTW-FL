package vrptwfl.metaheuristic.instanceGeneration;

public class ImportedTherapist {

    //{
    //    "id": 0,
    //    "skill": 1,
    //    "shortShift": false,
    //    "shiftStartMorning": true
    //  },
	private int 	id;
	private int		skill;
	private boolean	shortShift;
	private boolean	shiftStartMorning;

	// if regular shift is split
	private boolean wasSplit;  // split in 'Vormittags'/'Nachmittags'-Problem
	private Integer	connectionId;

	private int 	originalId; // Id as loaded from datafile
	
	// Getters and setters are not required for this example.
    // GSON sets the fields directly using reflection.
	
    @Override
    public String toString() {
        return "id = " + this.id + "\tskill = " + this.skill + "\tshort = " + this.shortShift + "\tmorning = " + this.shiftStartMorning;
    }
    
    public ImportedTherapist(int id, int skill, boolean shortShift, boolean shiftStartMorning) {
    	this.id = id;
    	this.originalId = id;
    	this.skill = skill;
    	this.shortShift = shortShift;
    	this.shiftStartMorning = shiftStartMorning;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSkill() {
		return skill;
	}

	public void setSkill(int skill) {
		this.skill = skill;
	}

	public boolean isShortShift() {
		return shortShift;
	}

	public void setShortShift(boolean shortShift) {
		this.shortShift = shortShift;
	}

	public boolean isShiftStartMorning() {
		return shiftStartMorning;
	}

	public void setShiftStartMorning(boolean shiftStartMorning) {
		this.shiftStartMorning = shiftStartMorning;
	}

	public boolean isWasSplit() {
		return wasSplit;
	}

	public void setWasSplit(boolean wasSplit) {
		this.wasSplit = wasSplit;
	}

	public Integer getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(Integer connectionId) {
		this.connectionId = connectionId;
	}

	public int getOriginalId() {
		return originalId;
	}

	public void setOriginalId(int originalId) {
		this.originalId = originalId;
	}
    
}
