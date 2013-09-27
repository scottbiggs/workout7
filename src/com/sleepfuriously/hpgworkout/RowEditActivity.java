/**
 * This Activity allows the user to edit the order of the
 * rows in their exercise list.
 *
 * Each row has a name and a button that moves it up or
 * down.
 *
 * Perhaps an movement animation would be nice?
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RowEditActivity
					extends
						BaseDialogActivity
					implements
						OnClickListener {

	//--------  Constants ----------

	private static final String tag = "RowEditActivity";


	//--------	Widgets  ----------

	/** Static so it can be accessed through the Adapter. Sigh. */
	protected static Button m_okay;
	protected Button m_cancel;

	private ImageView m_help;

	private ListView m_listview;

	//---------  Data  -------------

	/**
	 * TRUE when the user has made changes.
	 */
	private static boolean m_dirty = false;


	/** The Cursor to hold info from the database. */
//	Cursor m_cursor = null;

	/**
	 * The array of ViewHolder objects that holds all the
	 * data for this Activity.  VERY important.
	 */
	protected static ArrayList<ViewHolder> m_view_array;



	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 *	Holds the widgets for a row in the ListView as well as
	 *	some important information about this row.
	 */
	static class ViewHolder {
		public TextView text;
		public ImageView up, down;

		/** Same as the textview's getText() */
		String name;

		/** The actual ROW id for this exercise */
		int id;

		/**
		 * The order (starting at 0) for this exercise BEFORE this was called.
		 */
		int lorder;

		/** The current position of this row in the ListView */
		int pos;

		/**
		 * Constructor
		 *
		 * Fill in the blanks!
		 *
		 * @param _name
		 * @param _id
		 * @param _lorder
		 * @param _pos
		 */
		public ViewHolder(String _name, int _id, int _lorder, int _pos) {
			name = _name;
			id = _id;
			lorder = _lorder;
			pos = _pos;
		}

		public ViewHolder() {
		}

		@Override
		public String toString() {
			return name;
		}
	} // class ViewHolder


	//-----------------------------
	//	Methods
	//-----------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row_edit);

		m_okay = (Button) findViewById(R.id.row_edit_ok_butt);
		m_okay.setOnClickListener(this);

		m_cancel = (Button) findViewById(R.id.row_edit_cancel_butt);
		m_cancel.setOnClickListener(this);

		m_help = (ImageView) findViewById(R.id.row_edit_logo_id);
		m_help.setOnClickListener(this);

		m_listview = (ListView) findViewById(R.id.row_edit_lv);

		// Load the data into our ListView
		load_data();


		m_dirty = false;
	} // onCreate (.)


	//-----------------------------
	@Override
	protected void onDestroy() {
		if (m_view_array != null) {
			m_view_array.clear();
			m_view_array = null;
		}
		super.onDestroy();
	}

	/*************************
	 * Called during creation, this loads up the
	 * data from the database.
	 */
	protected void load_data() {
		int col, id, lorder, pos;
		String name;

		if (m_db != null) {
			Log.e(tag, "m_db is NOT null!!! Do something, dammit!");
		}

		try {
			m_db = WGlobals.g_db_helper.getReadableDatabase();
			Cursor cursor = null;
			try {
				cursor = m_db.query(
						DatabaseHelper.EXERCISE_TABLE_NAME,	// table
						new String[] {DatabaseHelper.COL_ID,
									DatabaseHelper.EXERCISE_COL_NAME,
									DatabaseHelper.EXERCISE_COL_LORDER},	//	columns[]
						null,//selection
						null,// selectionArgs[]
						null,	//	groupBy
						null,	//	having
						DatabaseHelper.EXERCISE_COL_LORDER,	//	orderBy
						null);	//	limit

				// Convert the cursor to an arrayList for the adapter
				// and pop it in.
				m_view_array = new ArrayList<ViewHolder>();
				while (cursor.moveToNext()) {
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
					name = cursor.getString(col);
					col = cursor.getColumnIndex(DatabaseHelper.COL_ID);
					id = cursor.getInt(col);
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LORDER);
					lorder = cursor.getInt(col);
					pos = cursor.getPosition();
					m_view_array.add(new ViewHolder (name, id, lorder, pos));
				}

				MyAdapter adapter = new MyAdapter (this);
				m_listview.setAdapter(adapter);

			} catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}
	} // load_data()

	//-----------------------------
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.row_edit_ok_butt:
				WGlobals.play_short_click();
				if (m_dirty) {
					save();
					setResult(RESULT_OK);
				}
				finish();
				break;

			case R.id.row_edit_logo_id:
				WGlobals.play_help_click();
				show_help_dialog(R.string.row_edit_help_title,
						R.string.row_edit_help_msg);
				break;

			case R.id.row_edit_cancel_butt:
				WGlobals.play_short_click();
				if (m_dirty && WGlobals.g_nag) {
					show_yes_no_dialog(R.string.row_edit_cancel_warning_title, null,
							R.string.editexer_cancel_warning_msg, null,
							new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// Yes, they want to cancel.
							WGlobals.play_short_click();

							setResult(RESULT_CANCELED);
							dismiss_all_dialogs();
							finish();
							}
					});
					return;
				}

				setResult(RESULT_CANCELED);
				finish();
				break;
		}
	} // onClick (v)


	/*********************
	 * Only intercepting the back key.  It's just to make sure
	 * the user hasn't accidentally hit it when making changes
	 * while the page is still dirty.
	 *
	 * @param keyCode
	 * @param event
	 * @return	True - that this method completely handled
	 * 			the event.
	 * 			False - Let other handlers have a crack at this.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (m_dirty && WGlobals.g_nag &&
				(keyCode == KeyEvent.KEYCODE_BACK)) {
			show_yes_no_dialog(R.string.row_edit_cancel_warning_title, null,
					R.string.row_edit_cancel_warning_msg, null,
					new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Yes, they want to cancel.
					WGlobals.play_short_click();

					setResult(RESULT_CANCELED);
					dismiss_all_dialogs();
					finish();
					}
			});
		return true;		// We handled this event!
		}

		return super.onKeyDown(keyCode, event);
	} // onKeyDown (keyCode, event)


	/********************
	 * Saves the current row order into the database.  Assumes
	 * that everything is already checked and waiting for this
	 * process to occur.
	 */
	private void save() {

		if (m_db != null) {
			Log.e(tag, "m_db is NOT null when trying to save!");
		}

		try {
			m_db = WGlobals.g_db_helper.getWritableDatabase();
			if (m_db == null) {
				Log.e(tag, "m_db is null in save()!  Aborting!");
				return;
			}

			// Update one at a time.
			for (int i = 0; i < m_view_array.size(); i++) {

				// Try it the hard way.  Works.
				String name = m_view_array.get(i).name;

				String sql_str = "update " +
					DatabaseHelper.EXERCISE_TABLE_NAME + " " +
					"set " + DatabaseHelper.EXERCISE_COL_LORDER + " " +
					"= " + i + " " +
					"where " + DatabaseHelper.EXERCISE_COL_NAME + " " +
					"= \"" + name + "\"";

				try {
					m_db.execSQL(sql_str);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

	} // save

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	CLASS	MyArrayAdapter
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private static class MyAdapter extends ArrayAdapter<ViewHolder> {

		// These are made global to the class to speed up
		// the inflation process.
		private LayoutInflater mm_inflater;

		Context mm_context;				// Nice to have
		//-----------------------------
		//	Constructor
		//
		public MyAdapter(Context context) {
			super(context, R.layout.row_edit_row,
				R.id.row_edit_row_text, m_view_array);
			mm_context = context;

			// Cache the inflater.
			mm_inflater = LayoutInflater.from(mm_context);
		} // constructor


		//-----------------------------
		@Override
		public View getView(int position, View convertView,
							final ViewGroup parent) {
			ViewHolder holder;

			// Find out if we have to construct (inflate) a new
			// row or are recycling a used row.
			if (convertView == null) {
				// Brand new row: make it!
				convertView = mm_inflater.inflate(R.layout.row_edit_row, null);

				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.row_edit_row_text);

				holder.up = (ImageView) convertView.findViewById(R.id.row_edit_row_up_image);
				holder.up.setFocusable(true);
				holder.up.setClickable(true);
				holder.up.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						WGlobals.play_short_click();

						ViewHolder holder = (ViewHolder) v.getTag();
						int pos = holder.pos;
//						Toast.makeText (mm_context, "Up for " + holder.text.getText() + ", at position " + holder.pos, Toast.LENGTH_SHORT).show();
						if (pos > 0) {
							// When swapping, you need a temp.
							ViewHolder temp = m_view_array.get(pos);

							// Inform those rows that they now have
							// new positions.
							temp.pos--;
							m_view_array.get(pos - 1).pos++;

							// Swap positions with the above view.
							m_view_array.set(pos, m_view_array.get(pos - 1));
							m_view_array.set(pos - 1, temp);

							// Signal the change.
							MyAdapter.this.notifyDataSetChanged();
							m_dirty = true;
							m_okay.setEnabled(true);
						}
					}
				});

				holder.down = (ImageView) convertView.findViewById(R.id.row_edit_row_down_image);
				holder.down.setClickable(true);
				holder.down.setFocusable(true);
				holder.down.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						WGlobals.play_short_click();

						ViewHolder holder = (ViewHolder) v.getTag();
						int pos = holder.pos;
//						Toast.makeText (mm_context, "Down for " + holder.text.getText() + ", at position " + holder.pos, Toast.LENGTH_SHORT).show();
						if (pos < m_view_array.size() - 1) {
							// When swapping, you need a temp.
							ViewHolder temp = m_view_array.get(pos);

							// Inform those rows that they now have
							// new positions.
							temp.pos++;
							m_view_array.get(pos + 1).pos--;

							// Swap positions with the lower view.
							m_view_array.set(pos, m_view_array.get(pos + 1));
							m_view_array.set(pos + 1, temp);

							// Signal the change.
							MyAdapter.this.notifyDataSetChanged();
							m_dirty = true;
							m_okay.setEnabled(true);
						}
					}
				});

				convertView.setTag(holder);
			}
			else {
				// Get our already-made holder from the tag.
				holder = (ViewHolder) convertView.getTag();
			}


			// Fill in the holder with all our stuff
			holder.text.setText(m_view_array.get(position).toString());

			holder.id = m_view_array.get(position).id;
			holder.lorder = m_view_array.get(position).lorder;
			holder.pos = position;

			holder.up.setTag(holder);	// Set it to the ViewHolder for this row.
			holder.down.setTag(holder);	// Set it to the ViewHolder for this row.

//			Log.i(tag, "name = " + holder.text.getText() +
//					", lorder = " + holder.lorder +
//					", pos = " + holder.pos);

			return convertView;		// Return the row we just made.
		} // getView (position, convertView, parent)


	} // class MyAdapter


}
