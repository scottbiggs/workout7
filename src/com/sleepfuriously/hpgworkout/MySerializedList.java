package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

/**
 * This class plays with lists of Strings, turning them into
 * a single string (and vice-versa).  I had to write it so I
 * could keep a list of data in the SharedPreferences.
 *
 * When constructing, you need to tell the class what character
 * to use as a delimiter.  Anything is legal except 0 (which
 * denotes the end of a string).  Use something which you're
 * sure will never be needed in your Strings.
 *
 */
public class MySerializedList {

	/** The char that seperates the strings in our "list" of strings. */
	private int m_delimiter;

	/** Holds the list of strings */
	private ArrayList<String> m_list;

	/**
	 * The list converted (serialized) into one long string, with
	 * each "string" seperated by m_delimiter.
	 */
	private String m_serialized;



	/****************************
	 * Constructor
	 *
	 * @param delmiter	The char to use to seperate the
	 * 					strings in our list.  Make sure
	 * 					that it's a character that will
	 * 					NEVER be used in your strings!
	 * 					Oh yeah, it can't be 0 either
	 * 					(who knows what will happend!).
	 */
	public MySerializedList (int delimiter) {
		m_delimiter = delimiter;
		m_list = new ArrayList<String>();
		m_serialized = new String();
	}


	/***************************
	 * Set the list of this class to whatever list you
	 * already got.
	 *
	 * @param list		The list of Strings to use for this class.
	 *
	 * @return		The number of items in your (now my) list.
	 */
	public int set_list (ArrayList<String> list) {
		m_list.clear();
		for (String str : list) {
			m_list.add(str);
		}
		return m_list.size();
	}


	/***************************
	 * Call this to add a string to the list (appended).
	 *
	 * @param str	The string to add.  Be nice and make
	 * 				sure that it doesn't contain any chars
	 * 				that are the delimiter, okay?
	 *
	 * @return		The size of the list AFTER this was added.
	 */
	public int add (String str) {
		m_list.add(str);
		return m_list.size();
	}


	/***************************
	 * Clears the current list.
	 *
	 * @return	Should always return 0.
	 */
	public int clear_list() {
		m_list.clear();
		return m_list.size();
	}

	/***************************
	 * This is how you parse a serialized list into its
	 * components.  Start by calling this method to set
	 * the serialized String.  Then call other methods
	 * as needed.
	 * <p>
	 * Yep, the work is done here!  O(n)
	 *
	 * @param serialized		A String, which consists of
	 * 						strings seperated by the
	 * 						delimiter.  Or one item, no
	 * 						delimiter.  Or an empty string,
	 * 						hey, I'm easy.  Note that this
	 * 						just creates another reference
	 * 						to you String.  So don't fuck
	 * 						with it, okay?
	 *
	 * @return	The number of strings found in this "list."
	 */
	public int set_serialized (String serialized) {
		/** Position of the last delimiter (actually one past it) */
		int last = 0;
		/** The position of the next delimiter */
		int next = 0;

		m_serialized = serialized;

		clear_list();
		
		next = m_serialized.indexOf(m_delimiter, last);
		while (next != -1) {
			m_list.add(serialized.substring(last, next - 1));
			last = next + 1;
			next = m_serialized.indexOf(m_delimiter, next + 1);
		}
		
		// Check to see if there's another item to add (this will
		// take into account any string that is AFTER a delimiter).
		if (last != m_serialized.length()) {
			m_list.add(m_serialized.substring(last));
		}
		
		return m_list.size();
	} // set_serialized (serialized)


	/*****************************
	 * So you have the serialized set and want to get the list
	 * from it, right?  Call this!
	 *
	 * @return	A list corresponding to all the elements of the
	 * 			serialized list.  Please don't change it as I'm
	 * 			just sending you a reference, not a copy.
	 */
	public ArrayList<String> get_list() {
		return m_list;
	}

}
