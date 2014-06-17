package com.sleepfuriously.hpgworkout;


/****************************************
 * Holds the data needed to fill in an entire set's layout.
 * To be used ONLY in the Inspector.  For accessibility,
 * this class needs to be defined outside of the
 * Inspector file.
 */
public class SetLayout {
	/** The order that this set should be displayed. Zero based. */
	int order;

	/** Holds all the data for the exercise set */
	SetData data;
} // class SetLayout

