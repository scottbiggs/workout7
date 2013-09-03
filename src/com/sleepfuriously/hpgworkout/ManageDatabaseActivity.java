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
import android.widget.ImageView;
import android.widget.ListAdapter;
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

	/** A TextView that displays the current database. */
	private TextView m_current_db_tv;

	/** This button is hit when the user adds a new database. */
	private Button m_add_butt;

	/** Holds the name of a new database file */
	private EditText m_add_et;

	/** The list of all the databases */
	private ListView m_lv;

	/** The logo/help button */
	private ImageView m_help;


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

	/** Holds the list used by the ListView for all the user names */
	private ArrayList<String> m_user_names;


	//-----------------------
	//	UI Callback Methods
	//-----------------------

	//-----------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_database);

		m_add_butt = (Button) findViewById(R.id.manage_db_add_butt);
		m_add_butt.setOnClickListener(this);
		m_add_butt.setOnLongClickListener(this);

		m_add_et = (EditText) findViewById(R.id.manage_db_add_et);
		m_add_et.setOnLongClickListener(this);

		m_help = (ImageView) findViewById(R.id.manage_db_logo);
		m_help.setOnClickListener(this);

		// Grab all the names of databases that this program has
		// already created.
		m_name_list = DatabaseFilesHelper.get_all_user_names(this);

		// Get the name of the current database
		m_current_db = DatabaseFilesHelper.get_active_username(this);

		// todo
		create_listview();

	} // onCreate(.)


	//-------------------
	@Override
	public boolean onLongClick(View v) {
		if (v == m_add_butt) {
			show_help_dialog(R.string.manage_db_add_butt_help_title,
							R.string.manage_db_add_butt_help_msg);
			return true;
		}


		return false;
	}


	//-------------------
	@Override
	public void onClick(View v) {
		Log.d(tag, "click");

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

		else if (v == m_help) {
			show_help_dialog(R.string.manage_db_help_title,
							R.string.manage_db_help_msg);
		}


	} // onClick(v)


	//-------------------
	//	For list item clicks.
	//
	@Override
	public void onItemClick(AdapterView<?> parent, View v,
							int pos, long id) {
		Log.d(tag, "clicked item " + pos);

		// Get the username that was clicked.
		String username = m_user_names.get(pos);
		Log.d(tag, "Selected user: " + username);
		m_current_db_tv.setText(username);

		// Set the current db to this username.
		DatabaseFilesHelper.activate(username, this);

	} // onItemClick (parent, v, pos, id)


	//-------------------
	//	For list long clicks
	//
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v,
								int pos, long id) {

		Log.d(tag, "long-clicked item " + pos);

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
			Log.w(tag, "Hit the 'add' button, but no name has been typed in.  Ignoring.");
			return;
		}

		// check to see if this name is already used.
		if (DatabaseFilesHelper.is_name_used (new_db_name, this)) {
			String title = getString(R.string.manage_db_username_used_dialog_title,
									new_db_name);
			String msg = getString(R.string.manage_db_username_used_dialog_msg,
								new_db_name);
			show_help_dialog(title, msg);
			return;
		}

		// Create the new database.
		int count = DatabaseFilesHelper.add(new_db_name, this);

		// All clear.  Do our bookkeeping and add it.
		m_current_db_index = m_name_list.size();
//		m_name_list.add(new_db_name);

		// todo
		//	Add in alphabetical order
		m_user_names.add(new_db_name);

		int pos = m_user_names.indexOf(new_db_name);
		if (pos == -1) {
			Log.e (tag, "BIG problem finding the position in add()!");
			pos = 0;
		}
		m_lv.setItemChecked(pos, true);
		m_lv.setSelection(pos);

		m_current_db_tv.setText(new_db_name);
		Log.d(tag, "adding an item to this position: " + m_current_db_index);
//		m_lv.setItemChecked(m_current_db_index, true);
//		m_lv.setSelection(m_current_db_index);	// scrolls up to reveal if necessary


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

	private void create_listview() {
		m_lv = (ListView) findViewById(R.id.manage_db_list_lv);

		m_user_names = DatabaseFilesHelper.get_all_user_names(this);

		// The adapter for the listview.  And set it to use str_list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this,
//						R.layout.graph_selector_row,
//						android.R.layout.simple_list_item_1,
						android.R.layout.simple_list_item_single_choice,
						m_user_names);
		m_lv.setAdapter(adapter);

		m_lv.setOnItemClickListener(this);
		m_lv.setOnItemLongClickListener(this);

		// Find and highlight the active database.
		String current_db = DatabaseFilesHelper.get_active_username(this);
		if (current_db == null) {
			Log.e (tag, "Can't get the current database username in create_listview()!");
			return;
		}
		m_current_db_index = adapter.getPosition(current_db);
		if (m_current_db_index == -1) {	// getPosition() returns -1 when not found
			Log.e(tag, "Problem getting the position of the current database!");
			m_current_db_index = 0;
		}

		// Tell the user what the current DB is.
		m_current_db_tv = (TextView) findViewById(R.id.manage_db_current_tv);
		m_current_db_tv.setOnLongClickListener(this);
		m_current_db_tv.setText(m_current_db);

		Log.d(tag, "m_current_db_index is " + m_current_db_index);
		m_lv.setItemChecked(m_current_db_index, true);	// turn on this radio button
		m_lv.setSelection(m_current_db_index);	// scrolls up to reveal if necessary

	} // create_listview()


}
