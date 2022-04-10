package vrptwfl.metaheuristic.instanceGeneration;

/**
 * Helper class for loading therapist instances. 
 * The class stores the id, the skill level, whether the therapist
 * has only a short shift (either morning or evening shift), if 
 * the therapists scheduling has already been splitted, as well 
 * as its original identifier.
 * 
 * @author Christian M.M. Frey
 *
 */
public class ImportedTherapist {

	private int 	id;
	private int		skill;
	private boolean	shortShift;
	private boolean	shiftStartMorning;
	private boolean wasSplit;  // split in morning/evening problems
	private int 	originalId; // Id as loaded from datafile
	
	// Getters and setters are not required for this example.
    // GSON sets the fields directly using reflection.
	
    /**
     * Constructor for a therapist being imported by GSON.
     * @param id: identifier of the therapist
     * @param skill: skill level of the therapist
     * @param shortShift: indicator whether the therapist has only a short shift
     * @param shiftStartMorning: indicator whether the shift starts in the morning
     */
    public ImportedTherapist(int id, int skill, boolean shortShift, boolean shiftStartMorning) {
    	this.id = id;
    	this.originalId = id;
    	this.skill = skill;
    	this.shortShift = shortShift;
    	this.shiftStartMorning = shiftStartMorning;
    }

    //
    // GETTERS
    //
    /**
     * Retrieve the therapist's identifier.
     * @return id of the therapist
     */
	public int getId() {
		return id;
	}

	/**
	 * Retrieve the skill level of the therapist.
	 * @return therapist's skill level
	 */
	public int getSkill() {
		return skill;
	}

	/**
	 * Retrieve whether the therapists shift is only a short one (either morning or evening shift).
	 * @return is therapist's shift a short one
	 */
	public boolean isShortShift() {
		return shortShift;
	}
	
	/**
	 * Retrieve whether the shift has been splitted.
	 * @return was shift splitted
	 */
	public boolean isWasSplit() {
		return wasSplit;
	}
	
	/**
	 * Retrieve whether the therapist's shift starts in the morning.
	 * @return is the therapist's shift starting in the morning.
	 */
	public boolean isShiftStartMorning() {
		return shiftStartMorning;
	}

	/**
	 * Retrieve the therapist's original identifier.
	 * @return therapist's original identifier
	 */
	public int getOriginalId() {
		return originalId;
	}
	
	//
	// SETTERS
	//
	/**
	 * Sets the identifier of the therapist.
	 * @param id identifier of the therapist
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the skill level of the therapist.
	 * @param skill: therapist's skill level
	 */
	public void setSkill(int skill) {
		this.skill = skill;
	}

	/**
	 * Sets whether the therapist's shift is a short one.
	 * @param shortShift: is shift a short one
	 */
	public void setShortShift(boolean shortShift) {
		this.shortShift = shortShift;
	}

	/**
	 * Sets whether the therapist's shift starts in the morning.
	 * @param shiftStartMorning: does the therapist's shift start in the morning
	 */
	public void setShiftStartMorning(boolean shiftStartMorning) {
		this.shiftStartMorning = shiftStartMorning;
	}
	
	/**
	 * Sets whether the therapist's shift has been splitted or not.
	 * @param wasSplit is shift splitted
	 */
	public void setWasSplit(boolean wasSplit) {
		this.wasSplit = wasSplit;
	}

	/**
	 * Sets the original therapist's identifier.
	 * @param originalId original identifier
	 */
	public void setOriginalId(int originalId) {
		this.originalId = originalId;
	}
	
	//
	// PRINTING
	//
	/**
	 * String formatting of the ImportedTherapist object.
	 */
	@Override
	public String toString() {
		return "id = " + this.id + "\tskill = " + this.skill + "\tshort = " + this.shortShift + "\tmorning = " + this.shiftStartMorning;
	}    
}
