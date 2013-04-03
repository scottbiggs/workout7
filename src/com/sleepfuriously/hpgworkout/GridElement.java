/**
 * This is mostly a data holder class.  The main table in the
 * grid uses this to identify the various exercises via their
 * positions on the grid and their DB id (held here).
 *
 * Each visible cell in the main grid can be empty or hold one or
 * more sets of a given exercise.  That complex data structure
 * is defined here.
 *
 * The TextView that is a cell has a GridElement added to its
 * Tag.  So that when a touch event happens on that TextView,
 * this info can be easily retrieved.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//	This class holds data for the main table.  Each
//	class instance tells how many exercise sets are
//	represented by this element and what their ids
//	are.
//
//	If the number of elements is 0, then the
class GridElement {
	protected int m_count;
	protected ArrayList <Integer> m_id_list;
	protected ArrayList <IntFloat> m_sig_list;
	protected String m_exer_name;

	/*********************
	 * Constructors
	 */
	GridElement() {
		init();
	}
	GridElement(GridElement ge) {
		m_count = ge.m_count;
		m_id_list = ge.m_id_list;
		m_sig_list = ge.m_sig_list;
		m_exer_name = ge.m_exer_name;
	}

	/*********************
	 * Init
	 */
	private void init() {
		m_count = 0;
		m_id_list = new ArrayList<Integer>();
		m_sig_list = new ArrayList<IntFloat>();
		m_exer_name = null;
	}

	/*********************
	 * Adds an item to the id list and the significant list.
	 *
	 * @param _id	The COL_ID of the particular exercise set.
	 *
	 * @param _sig	The significant amount for this set.
	 *
	 * @return	The current number of items (after the
	 * 			add).
	 */
	int add (int _id, IntFloat _sig) {
		m_id_list.add(_id);
		m_sig_list.add(_sig);
		m_count++;
		return m_count;
	}

	/***********************
	 * Just like regular add, but adds a null
	 * in the significant list.
	 *
	 * @see #add(int, IntFloat)
	 */
	int add (int _id) {
		m_id_list.add(_id);
		m_sig_list.add(null);
		m_count++;
		return m_count;
	}


	/*********************
	 * Stores the string as the exercise name.  Very useful
	 * later!
	 *
	 * @param name	A string for the exercise name.
	 */
	void set_name (String name) {
		m_exer_name = name;
	}

	/*********************
	 * Clears everything out.
	 */
	void clear() {
		m_count = 0;
		m_id_list.clear();
		m_sig_list.clear();
		m_exer_name = null;
	}

	/*********************
	 * The main id for this element.  It's always the same
	 * as the first id.  If the list is empty, then -1
	 * is returned.
	 */
	public int get_first_id() {
		if (m_count < 1) {
			return -1;
		}

		return m_id_list.get(0);
	} // get_first_id()

	/*********************
	 * Gets the LAST id for this grid element.  If the
	 * list is empty, then -1 is returned.
	 */
	public int get_last_id() {
		return get_id_at (m_count - 1);
	} // get_last_id()

	/*********************
	 * Returns the element at the specified position.  If
	 * the position does not exist, returns -1;
	 *
	 * @param	pos		The position of the id to return.
	 *
	 * @return	The specified ID or -1 if none exists at
	 * 			that position.
	 */
	public int get_id_at (int pos) {
		if ((m_count < 1) || (pos < 0) || (pos >= m_count)) {
			return -1;
		}
		return m_id_list.get(pos);
	} // get_id_at (pos)

	/**********************
	 * Returns the element at the specified position.  If
	 * the position does not exist, returns -1;
	 *
	 * @param	pos		The position of the significant list to return.
	 *
	 * @return	The specified ID or -1 if none exists at
	 * 			that position.
	 */
	public IntFloat get_sig_at (int pos) {
		if ((m_count < 1) || (pos < 0) || (pos >= m_count)) {
			return new IntFloat(-1);
		}
		return m_sig_list.get(pos);
	} // get_sig_at (pos)


	/***********************
	 * @return	The maximum value of all the sigs.  If none exist,
	 * 			return -1.
	 */
	public IntFloat get_sig_max() {
		IntFloat max = new IntFloat(-1);

		for (int i = 0; i < m_count; i++) {
			if (m_sig_list.get(i) != null) {
				if (m_sig_list.get(i).greater(max)) {
					max = m_sig_list.get(i);
				}
			}
		}
		return max;
	} // get_sig_max()

	/*********************
	 * @return	The name (probably an exercise name)
	 * 			associated with this GridElement.
	 */
	String get_name() {
		return m_exer_name;
	}

	/***********************
	 * @return	The number of items in this instance.
	 */
	public int size() {
		return m_count;
	}

	/***********************
	 * Constructs the string to represent a cell for all the
	 * sets of a specific exercise on a certain day.
	 *
	 * The String will be in the form of "a/b", where 'a'
	 * is the number of sets in the GridElement and 'b' is
	 * the maximum significant value done in those sets--
	 * gives 'em something to strive for.
	 *
	 * If this is empty, then null is returned.
	 *
	 * @param 	seperator	The string to seperate the elements
	 *
	 * @param	sig_def_str	The string to display if the Signi-
	 * 						ficant is -1.
	 *
	 * @return	The resulting String or null if ge == null.
	 */
	public String construct_set_cell_string (String seperator,
											String sig_def_str) {
		if (m_count < 1) {
			return null;
		}
		IntFloat max = get_sig_max();
		if (max.is_negative()) {
			return "" + m_count + seperator + sig_def_str;
		}
		return "" + m_count + seperator + get_sig_max();
	} // construct_set_cell_string (seperator)


	/***********************
	 * Faster version: just the number of sets.
	 *
	 * Constructs the string to represent a cell for all the
	 * sets of a specific exercise on a certain day.
	 *
	 * If this is empty, then null is returned.
	 *
	 * @param 	seperator	The string to seperate the elements
	 *
	 * @param	sig_def_str	The string to display if the Signi-
	 * 						ficant is -1.
	 *
	 * @return	The resulting String or null if ge == null.
	 */
	public String construct_set_cell_string_faster() {
		if (m_count < 1) {
			return null;
		}
		return "" + m_count;
	} // construct_set_cell_string (seperator)


} // class GridElement
