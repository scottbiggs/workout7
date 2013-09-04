package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
	 * This is the string that was last selected to go to the
	 * ManageDatabasePopupActivity.  This will be the username
	 * for the database in question.
	 */
	private String m_last_popup_db = null;

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
		WGlobals.play_short_click();

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
		WGlobals.play_short_click();
		Log.d(tag, "clicked item " + pos);

		// Get the username that was clicked.
		String username = m_user_names.get(pos);
//		Log.d(tag, "Selected user: " + username);
		m_current_db_tv.setText(username);

		// Set the current db to this username.
		DatabaseFilesHelper.activate(username, this);
		
		// And don't forget to set our local data member, too.
		m_current_db = username;

	} // onItemClick (parent, v, pos, id)


	//-------------------
	//	For list long clicks
	//
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v,
								int pos, long id) {
		WGlobals.play_long_click();
		Log.d(tag, "long-clicked item " + pos);

		// Get the username that was clicked.
		m_last_popup_db = m_user_names.get(pos);
		Log.d(tag, "Selected user: " + m_last_popup_db);

		// Start the new Activity
		Intent itt = new Intent (this, ManageDatabasePopupActivity.class);
		itt.putExtra(ManageDatabasePopupActivity.DB_USERNAME_KEY, m_last_popup_db);
		startActivityForResult(itt, WGlobals.MANAGEDATABASEPOPUPACTIVITY);

		return true;	// true = long click consumed here, false NOT consumed
	}


	//-------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);	// Typically not used.

		switch (requestCode) {
			case WGlobals.MANAGEDATABASEPOPUPACTIVITY:
				if (resultCode == RESULT_CANCELED) {
					return;	// Don't do anything on Cancel or Back button
				}

				int operation = data.getIntExtra(ManageDatabasePopupActivity.OPERATION_CODE_KEY,
												ManageDatabasePopupActivity.OPERATION_CODE_NOT_USED);

				switch (operation) {
					// -- DELETE --
					case ManageDatabasePopupActivity.OPERATION_CODE_DELETE:
//						String username = data.getStringExtra(ManageDatabasePopupActivity.OPERATION_DELETE_NAME_KEY);
//						if (username == null) {
//							Log.e(tag, "Can't find the username when trying to delete in onActivityResult()!");
//							return;
//						}
						delete(m_last_popup_db);
						break;


					// -- RENAME --
					case ManageDatabasePopupActivity.OPERATION_CODE_RENAME:
						Log.v(tag, "onActivityResult(): rename code detected.");
						String new_name = data.getStringExtra(ManageDatabasePopupActivity.OPERATION_NEW_NAME_KEY);
						if (new_name == null) {
							Log.e(tag, "Can't find the new username when trying to rename in onActivityResult()!");
							return;
						}
						rename (m_last_popup_db, new_name);
						break;


					// -- EXPORT --
					case ManageDatabasePopupActivity.OPERATION_CODE_EXPORT:
						Log.v(tag, "onActivityResult(): export code detected.");
						export(m_last_popup_db);
						break;

					default:
						Log.e(tag, "Unknown operation returned in onActivityResult()!");
						break;
				} // switch (operation)
				break;

			default:
				Log.e(tag, "Unrecognized requestCode in onActivityResult()!");
				return;

		} // switch (requestCode)

	} // onActivityResult (requestCode, resultCode, data)



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

		m_user_names.add(new_db_name);
		alphabetize(m_user_names);

		int pos = m_user_names.indexOf(new_db_name);
		if (pos == -1) {
			Log.e (tag, "BIG problem finding the position in add()!");
			pos = 0;
		}
		m_lv.setItemChecked(pos, true);
		m_lv.setSelection(pos);

		m_current_db_tv.setText(new_db_name);
//		m_lv.setItemChecked(m_current_db_index, true);
//		m_lv.setSelection(m_current_db_index);	// scrolls up to reveal if necessary


		m_add_et.setText("");	// clear the edittext
	} // add()


	/************************
	 *
	 * @param username
	 */
	private void delete (String username) {
		// update the UI
		m_user_names.remove(m_last_popup_db);
		alphabetize(m_user_names);

		set_current_in_lv();

//		m_lv.invalidate();
//		((BaseAdapter) (m_lv.getAdapter())).notifyDataSetChanged();

		// update the DB
		DatabaseFilesHelper.remove(m_last_popup_db, this);
		m_last_popup_db = null;
	} // delete (usernae)


	/************************
	 *
	 * @param orig_username
	 * @param new_username
	 */
	private void rename (String orig_username, String new_username) {
		DatabaseFilesHelper.rename(orig_username, new_username, this);
		m_user_names.remove(orig_username);
		m_user_names.add(new_username);
		if (m_current_db.equals(orig_username)) {
			m_current_db = new_username;
			m_current_db_tv.setText(new_username);
		}
		alphabetize(m_user_names);

		//	make sure the current is highlighted
		set_current_in_lv();

	} // rename (orig, new)

	/************************
	 *
	 * @param username
	 */
	private void export (String username) {
		// todo
	}


	//-------------------
	//	Secondary Methods
	//-------------------

	private void create_listview() {
		m_lv = (ListView) findViewById(R.id.manage_db_list_lv);

		m_user_names = DatabaseFilesHelper.get_all_user_names(this);

		alphabetize (m_user_names);

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

		// Tell the user what the current DB is.
		m_current_db_tv = (TextView) findViewById(R.id.manage_db_current_tv);
		m_current_db_tv.setOnLongClickListener(this);
		m_current_db_tv.setText(m_current_db);

		// And finally display which is current in our ListView.
		set_current_in_lv();

	} // create_listview()


	/***************************
	 * Turns the radio button and highlights the current database in
	 * our ListView.
	 *
	 * preconditions:
	 * 		m_current_db		Should be properly set.
	 */
	private void set_current_in_lv () {
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) m_lv.getAdapter();
		int current_db_index = adapter.getPosition(m_current_db);
		if (current_db_index == -1) {	// getPosition() returns -1 when not found
			Log.e(tag, "Problem getting the position of the current database!  m_currend_db = " + m_current_db);
			current_db_index = 0;
		}

		m_lv.setItemChecked(current_db_index, true);	// turn on this radio button
		m_lv.setSelection(current_db_index);	// scrolls up to reveal if necessary
	}

	/***************************
	 * Given an ArrayList of Strings, this creates a new list that's
	 * in alphabetical order.
	 *
	 * For expediency, this is a crappy bubble sort.  O(n^2).
	 *
	 * @param unsorted	Original list of Strings.  Probably unsorted.
	 *
	 */
	private void alphabetize (ArrayList<String> a_list) {
		Collections.sort(a_list, String.CASE_INSENSITIVE_ORDER);
	} // alphabetize(unsorted)
}
