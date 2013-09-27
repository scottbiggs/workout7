/**
 * The SetData class is a nice way to encapsulate all
 * the data associated with an exercise set.
 */
package com.sleepfuriously.hpgworkout;


public class SetData {
	/** The id for this item in the database */
	int _id;

	/**
	 * Name of the exercise.  This should be exactly
	 * the same as the name in the corresponding ExerciseData.
	 */
	String name;

	/** The time and date (in milliseconds) of this set */
	long millis;

	/** The actual data for the set (float values) */
	float weight, dist, time, other;

	/** The actual data for the set (int values) */
	int reps, levels, cals;

	/** The condition code */
	int cond;

	/** Any notes attached to this set */
	String notes;

	/***************
	 * Constructors
	 */
	public SetData() {
		_id = -1;
		name = null;
		millis = 0l;
		weight = dist = time = other = 0f;
		reps = levels = cals = 0;
		cond = DatabaseHelper.SET_COND_NONE;
		notes = null;
	}


	public SetData (SetData data) {
		_id = data._id;
		if (data.name == null) {
			name = null;
		}
		else {
			name = new String(data.name);	// Creates a copy of the string.
		}
		millis = data.millis;
		weight = data.weight;
		dist = data.dist;
		time = data.time;
		other = data.other;
		reps = data.reps;
		levels = data.levels;
		cals = data.cals;
		cond = data.cond;
		if (data.notes == null) {
			notes = null;
		}
		else {
			notes = new String (data.notes);
		}
	}


	/***************
	 * Creates a copy of a SetData instance.
	 *
	 * @param data	The data to copy.
	 *
	 * @return	An exact copy of the original (including the _id).
	 */
	public static SetData clone (SetData data) {
		SetData s = new SetData();
		s._id = data._id;

		if (data.name == null) {
			s.name = null;
		}
		else {
			s.name = new String(data.name);	// Creates a copy of the string.
		}

		s.millis = data.millis;
		s.weight = data.weight;
		s.dist = data.dist;
		s.time = data.time;
		s.other = data.other;
		s.reps = data.reps;
		s.levels = data.levels;
		s.cals = data.cals;
		s.cond = data.cond;
		if (data.notes == null) {
			s.notes = null;
		}
		else {
			s.notes = new String (data.notes);
		}

		return s;
	} // copy (data)


}
