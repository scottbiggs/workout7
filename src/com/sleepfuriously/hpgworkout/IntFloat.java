/**
 * This is a custom data structure.  Sometimes we don't
 * know if the number is a float or an int beforehand.
 * This class tries to take care of that problem.
 *
 * The boolean tells whether the item is an int or float.
 * The DEFAULT is INT.
 */
package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;

public class IntFloat {
	/** This should be read-only!!!! */
	public int i;

	/** This should be read-only!!!! */
	public float f;
	public boolean is_float;

	/**
	 * Constructor
	 *
	 * Sets all the numbers to a given default.
	 *
	 * @param start		The value to set both numbers to.
	 */
	public IntFloat (int start) {
		i = start;
		f = start;
		is_float = false;
	}

	/**
	 * Constructor
	 *
	 * No default values set--the easy blind version.
	 */
	public IntFloat() {
		is_float = false;
	}

	/**
	 * Sets the given value.
	 */
	public void set(int new_i) {
		i = new_i;
		f = new_i;		// Just to be anal
	}
	public void set(float new_f) {
		f = new_f;
		i = (int) (Math.round(new_f));	// Just to be anal
	}

	/**
	 * Checks to see if THIS instance is greater in value
	 * than the supplied one.
	 *
	 * preconditions:
	 * 		All the values in both instances are correctly
	 * 		set.
	 *
	 * @return	true		This is strictly greater than 'other'
	 * 			false	'other' is greater or equal to this.
	 */
	public boolean greater (IntFloat other) {
		// If either are integers, compare their integer
		// components.
		if ((!is_float) || (!other.is_float)) {
			return (i > other.i);
		}
		else {
			return (f > other.f);
		}
	} // greater (other)

	/**
	 * Checks to see if THIS instance is LESS in value
	 * than the supplied one.
	 *
	 * preconditions:
	 * 		All the values in both instances are correctly
	 * 		set.
	 *
	 * @return	true		This is strictly less than 'other'
	 * 			false	'other' is less or equal to this.
	 */
	public boolean less (IntFloat other) {
		// If either are integers, compare their integer
		// components.
		if ((!is_float) || (!other.is_float)) {
			return (i < other.i);
		}
		else {
			return (f < other.f);
		}
	} // less (other)

	/**
	 * Checks to see if the supplied IntFloat has the same
	 * value as this one.
	 *
	 * preconditions:
	 * 		All the values in both instances are correctly
	 * 		set.
	 */
	public boolean equal (IntFloat other) {
		// If either are integers, compare their integer
		// components.
		if ((!is_float) || (!other.is_float)) {
			return (other.i == i);
		}
		else {
			return (other.f == f);
		}
	} // equal (other)

	/**
	 * Is this value negative (less than zero)?
	 */
	public boolean is_negative() {
		if (is_float) {
			return (f < 0);
		}
		else {
			return (i < 0);
		}
	} // is_negative

	/**
	 * Is this value positive (greater than zero)?
	 */
	public boolean is_positive() {
		if (is_float) {
			return (f > 0);
		}
		else {
			return (i > 0);
		}
	} // is_positive()


	//----------------------------------
	@Override
	public String toString() {
		if (is_float) {
			return new DecimalFormat("#.###").format(f);
//			return String.format("%f", f);
		}
		return Integer.toString(i);
	} // toString()

}
