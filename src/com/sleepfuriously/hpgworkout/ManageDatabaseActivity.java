package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This Activity allows the user to manage their various databases.
 * It is not accessible in the free version.
 * <p>
 * The current database file is stored in a preference.
 * <p>
 * NOTE: The name of each of the database files is also the name used
 * to identify the file here (with the suffix removed).
 */
public class ManageDatabaseActivity extends BaseDialogActivity
									implements
										OnClickListener,
										OnLongClickListener,
										OnItemClickListener,
										OnItemLongClickListener {

	//-------------------
	//	Constants
	//-------------------

	private static final String tag = "ManageDatabaseActivity";

	/**
	 * This is the key that access the list of the databases that the
	 * user has for this app.  Like everything in the SharedPrefs,
	 * we need a key to access the real data.
	 * <p>
	 * Note: The list is actually a serialized list. Use
	 * MySerializedList to access it easily.
	 */
//	public static final String PREFS_NAME_KEY = "ManageDatabaseActivity_db_names";

	/**
	 * This is the char that seperates all the items in the serialized
	 * list.  The user may NOT enter this character (for obvious reasons)!
	 */
//	public static final int SERIALIZED_LIST_DELIMITER = '\f';	// form-feed


	//-------------------
	//	Widgets
	//-------------------

	/** For our list of databases */
	private ListView m_listview;

	/** A TextView that displays the current database. */
	private TextView m_current_db_tv;

	/** This button is hit when the user adds a new database. */
	private Button m_add_butt;

	/** Holds the name of a new database file */
	private EditText m_add_et;


	//-------------------
	//	Class Data
	//-------------------

	/**
	 * The name of the database file that the user is currently using
	 * or has selected.
	 */
	private String m_current_db;

	/**
	 * The index to m_listview that tells us which is the currently
	 * active database.
	 */
	private int m_current_db_index = -1;

	/** A list (used by the ArrayAdapter) for all the database names */
	private ArrayList<String> m_name_list;


	//-----------------------
	//	UI Callback Methods
	//-----------------------

	/* (non-Javadoc)
	 * @see com.sleepfuriously.hpgworkout.BaseDialogActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_database);

		m_add_butt = (Button) findViewById(R.id.manage_db_add_butt);
		m_add_butt.setOnClickListener(this);
		m_add_butt.setOnLongClickListener(this);

		m_add_et = (EditText) findViewById(R.id.manage_db_add_et);
		m_add_et.setOnLongClickListener(this);


		// Figure out what databases the user has already
		// created and put them in our list.
//		SharedPreferences prefs =
//			PreferenceManager.getDefaultSharedPreferences(this);
//
//		String db_serialized_list = prefs.getString(PREFS_NAME_KEY,
//													DatabaseHelper.DB_DEFAULT_PREFIX);
//		MySerializedList slist = new MySerializedList(SERIALIZED_LIST_DELIMITER);
//		slist.set_serialized(db_serialized_list);
//		m_name_list = slist.get_list();


		// Grab all the names of databases that this program has
		// already created.
		m_name_list = DatabaseFilesHelper.get_all_user_names(this);

		// Get the name of the current database
		m_current_db = DatabaseFilesHelper.get_active_username(this);

	} // onCreate(.)


	//-------------------
	@Override
	public boolean onLongClick(View v) {
		return false;
	}


	//-------------------
	@Override
	public void onClick(View v) {
		// Clicked the add button
		//	Game plan:
		//		If the user has put a valid name in the edit text
		//		(check first), then create a new database with that
		//		name.  Also add this to our list of databases in the
		//		preferences and specify that this new database is the
		//		current one.  But don't forget to close the current
		//		database first!
		//
		if (v == m_add_butt) {
			add();
		}


	} // onClick(v)


	//-------------------
	//	For list item clicks.
	//
	@Override
	public void onItemClick(AdapterView<?> parent, View v,
							int pos, long id) {
		Log.i(tag, "clicked item " + pos);
	}


	//-------------------
	//	For list long clicks
	//
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v,
								int pos, long id) {

		Log.i(tag, "long-clicked item " + pos);

		return true;	// true = long click consumed here, false NOT consumed
	}


	//-------------------
	//	Major Methods
	//-------------------

	/************************
	 * Called when the user taps on the add button.
	 *
	 * Closes the current DB, creates a new one, adds it
	 * to the display list, and sets this new one as the
	 * current DB.
	 */
	private void add() {
		// Is there anything in the EditText?
		String new_db_name = m_add_et.getText().toString();
		if (new_db_name.length() == 0) {
			// Nothing to do here--ignore.
			return;
		}

		// check to see if this name is already used.
		if (is_name_used (new_db_name)) {
			Log.w(tag, "Tried to add a name that's already used!");
			return;
		}

		// All clear.  Do our bookkeeping and add it.
		m_current_db_index = m_name_list.size();
		m_name_list.add(new_db_name);
		m_current_db_tv.setText(new_db_name);
		Log.d(tag, "adding an item to this position: " + m_current_db_index);
		m_listview.setItemChecked(m_current_db_index, true);
		m_listview.setSelection(m_current_db_index);	// scrolls up to reveal if necessary

		if (create_new_db (new_db_name) == false) {
			Log.e (tag, "Problem creating a new database named: " + new_db_name + ". Aborting!");
			return;
		}

		// todo
		// Save the new data in our preferences.


		m_add_et.setText("");	// clear the edittext
	} // add()

	/*************************
	 * Called when the user indicates they want to delete
	 * a database.  Handles making sure that the user really
	 * wants to delete this database as well as the nitty-gritty
	 * of of getting rid of it and setting the new current DB.
	 *
	 * @param db_file_name		The name of the full database
	 * 							filename that the user wants
	 * 							to remove.
	 */
	private void delete (String db_file_name) {
		// todo
	} // delete (db_file_name)

	/*************************
	 * Called when the user indicates that they want
	 * to export a DB.  This goes from there.
	 *
	 * Gather info on WHERE to export this, what format to
	 * export it, the actual connections, handling errors,
	 * and then informing the user that the export is
	 * complete.
	 *
	 * @param db_file_name		The DB to export.
	 */
	private void export (String db_file_name) {
		// todo
	} // export (db_file_name)


	//-------------------
	//	Secondary Methods
	//-------------------


	/************************
	 * Called during onCreate(), this sets up the ListView.
	 * It's done here to clean up onCreate().
	 *
	 * @param str_list		An ArrayList of strings to populate
	 * 						the ListView with.
	 *
	 * @param pos			The position in the ArrayList to
	 * 						highlight.  Use -1 to indicate none.
	 *
	 */
/*	private ListView create_listview (ArrayList<String> str_list, int pos) {
		// Now that we have our list of databases, fill the
		// ListView.
		ListView lv = (ListView) findViewById(R.id.manage_db_list_lv);

		// The adapter for the listview.  And set it to use str_list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this,
//						R.layout.graph_selector_row,
//						android.R.layout.simple_list_item_1,
						android.R.layout.simple_list_item_single_choice,
						str_list);
		lv.setAdapter(adapter);

//		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);	done in XML
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);

		// Which database name is current?
		SharedPreferences prefs =
						PreferenceManager.getDefaultSharedPreferences(this);
		String current_db_name = prefs.getString(DatabaseHelper.PREFS_CURRENT_NAME_KEY, "");
		m_current_db_index = adapter.getPosition(current_db_name);
		if (m_current_db_index == -1) {	// getPosition() returns -1 when not found
			Log.e(tag, "No database currently selected, resorting to default!");
			m_current_db_index = 0;
		}
		m_current_db = adapter.getItem(m_current_db_index);

		// Tell the user what the current DB is.
		m_current_db_tv = (TextView) findViewById(R.id.manage_db_current_tv);
		m_current_db_tv.setOnLongClickListener(this);
		m_current_db_tv.setText(m_current_db);

		Log.d(tag, "m_current_db_index is " + m_current_db_index);
		m_listview.setItemChecked(m_current_db_index, true);	// turn on this radio button
		m_listview.setSelection(m_current_db_index);	// scrolls up to reveal if necessary

	} // create_listview()
*/

	/************************
	 * O(n)
	 * This version figures out the position for you.
	 *
	 * @param str_list
	 * @param highlight_str		The string to highlight.
	 *
	 */
/*	private ListView create_listview (ArrayList<String> str_list,
									String highlight_str) {
		for (int i = 0; i < str_list.size(); i++) {
			if (str_list.get(i).equalsIgnoreCase(highlight_str)) {
				return create_listview (str_list, i);
			}
		}
		return create_listview (str_list, -1);
	} // create_listview (str_list, highlight_str)
*/


	/************************
	 * This turns off the current database and opens a new one.  The
	 * database that is turned on is named by this index into our
	 * list of names.
	 *
	 * @param index		Which file name to use (starts at 0).
	 */
	void set_new_db (int index) {
		// todo
	} // set_new_db (index)



	/************************
	 * Checks to see if a given String already exists in the list
	 * of database file names.
	 *
	 * O(n)
	 */
	private boolean is_name_used (String name) {

		for (String db_name : m_name_list) {
			if (db_name.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;

	} // is_name_used(name)


	/************************
	 * Creates a new database (file) with the given name.  That's all.
	 *
	 * @param name_prefix	Just the first part of the file name.
	 * 						This method will append the suffix.
	 *
	 * @return Whether or not this method succeeded.
	 */
	private boolean create_new_db (String name_prefix) {
//
//		String full_name = DatabaseHelper.convert_display_name_to_db_name(name_prefix);
//
//		// Close the old database if it's active
//		if (WGlobals.g_db_helper != null) {
//			WGlobals.g_db_helper.close();
//			WGlobals.g_db_helper = null;
//			WGlobals.g_db_helper = new DatabaseHelper(this, full_name);
//		}

		// todo  something!
		return true;
	} // add_new_db (name_prefix)




}
