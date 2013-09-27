/**
 * This is the screen that pops up immediately after the
 * user hits "graphs" from the welcome screen.  Here they
 * choose which graph to look at.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GraphSelectorActivity
				extends BaseDialogActivity
				implements OnClickListener,
						OnItemClickListener {

	//------------------------
	//	Constants
	//------------------------

	private static final String tag = "GraphSelectorActivity";



	//------------------------
	//	Widgets
	//------------------------
	ListView m_lv;

	ImageView m_help;

	//------------------------
	//	Data
	//------------------------

	SimpleCursorAdapter m_adapter;

	Cursor m_set_cursor = null;


	//------------------------
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_selector);

		m_help = (ImageView) findViewById(R.id.graph_selector_logo);
		m_help.setOnClickListener(this);

		// 1.
		m_lv = (ListView) findViewById(R.id.graph_selector_lv);

		String name_array[] = DatabaseHelper.getAllExerciseNames();


		ArrayAdapter<String> adapter =
			new ArrayAdapter<String>(this,
					R.layout.graph_selector_row,
					name_array);
		m_lv.setAdapter(adapter);

		m_lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		m_lv.setOnItemClickListener(this);

		TextView title_tv = (TextView) findViewById(R.id.graph_selector_title_tv);
		String user = DatabaseFilesHelper.get_active_username(this);
		String possessive = getString(R.string.possessive_suffix);
		String title = getString(R.string.graph_selector_title);
		title_tv.setText(user + possessive + " " + title);

	} // onCreate (.)


	//----------------------------
	@Override
	public void onClick(View v) {
		if (v == m_help) {
			WGlobals.play_help_click();
			show_help_dialog(R.string.graph_selector_help_title,
					R.string.graph_selector_help_msg);
		}
	} // onClick(v)


	//----------------------------
	//	This hits when the use clicks on an item (part
	//	of the ListView).
	//
	@Override
	public void onItemClick(AdapterView<?> parent, View v,
							int pos, long id) {

		WGlobals.play_short_click();

		// Finding the name they clicked on.  This seems oddly tricky.
		// Because views are recycled, getChildAt() returns the position
		// of selected item among the VISIBLE lines (starting at 0).  To
		// find the actual position in the list, simply subtract from
		// what the ListView tells us is the first visible position.
		TextView tv = (TextView) m_lv.getChildAt(pos - m_lv.getFirstVisiblePosition());
		String name = (String) tv.getText();

		// Start a new Intent, supplying it with the data
		// for the list item that was selected.
		Intent itt = new Intent (this, GraphActivity.class);
		itt.putExtra(GraphActivity.NAME_KEY, name);
		startActivity(itt);	// Don't need any results

	} // onItemClick(...)

}
