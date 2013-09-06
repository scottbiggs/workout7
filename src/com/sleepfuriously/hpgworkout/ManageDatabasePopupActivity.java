package com.sleepfuriously.hpgworkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is a dialog activity that is activated when the user
 * long-clicks on one of the databases.  They select an action
 * here.  This returns to ManageDatabaseActivity which then
 * acts appropriate.
 */
public class ManageDatabasePopupActivity extends BaseDialogActivity
							implements OnClickListener,
										OnLongClickListener {

	private static final String tag = "ManageDatabasePopupActivity";

	/**
	 * Used in the Intent that's starts this Activity, this
	 * is the key to the username for the database that we're
	 * working on.
	 */
	public static final String DB_USERNAME_KEY = "ManageDBPopupActivity_username_key";

	/**
	 * The key to get at a new name from an Intent (after you figure
	 * out that the user has selected 'rename').
	 */
	public static final String OPERATION_NEW_NAME_KEY = "ManageDBPopupActivity_new_name_key";

	/**
	 * The key to access the username of the database to delete.
	 * Only active if OPERATION_CODE_DELETE is set.
	 */
//	public static final String OPERATION_DELETE_NAME_KEY = "ManageDBPopupActivity_delete_name_key";


	/**
	 * This is the key to get at the int for the operation code.
	 * The code tells the caller what the user did.  See below.
	 */
	public static final String OPERATION_CODE_KEY = "ManageDBPopupActivity_operation_code";

	/**
	 * When this Activity exits, it'll put the operation code
	 * in its intent.  These will only be valid if the RETURN_CODE
	 * is RESULT_OK (not RESULT_CANCEL).
	 */
	public static final int
			OPERATION_CODE_NOT_USED = 0,
			OPERATION_CODE_RENAME = 1,
			OPERATION_CODE_DELETE = 2,
			OPERATION_CODE_EXPORT = 3,
			OPERATION_CODE_CLEAR_SET_DATA = 4;

	/** The username for the database that this is acting on */
	private String m_db_username;

	private OneLineEditText m_rename_et;

	private TextView m_rename_tv;

	private Button m_delete_butt, m_rename_butt, m_cancel_butt, m_clear_set_data_butt;

	private ImageView m_help_iv;


	//-------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_db_popup);

		Intent itt = getIntent();
		m_db_username = itt.getStringExtra(DB_USERNAME_KEY);
		if (m_db_username == null) {
			Log.e (tag, "onCreate(): can't find the username!");
			return;
		}


		// The title
		TextView title_tv = (TextView) findViewById(R.id.manage_db_popup_title_tv);
		title_tv.setText(m_db_username);

		// The other widgets
		m_delete_butt = (Button) findViewById(R.id.manage_db_popup_delete_butt);
		m_delete_butt.setOnClickListener(this);
		m_delete_butt.setOnLongClickListener(this);

		m_rename_butt = (Button) findViewById(R.id.manage_db_popup_rename_butt);
		m_rename_butt.setOnClickListener(this);
		m_rename_butt.setOnLongClickListener(this);
		m_rename_et = (OneLineEditText) findViewById(R.id.manage_db_popup_rename_et);
		m_rename_tv = (TextView) findViewById(R.id.manage_db_popup_rename_label_tv);
		String rename_label = getString(R.string.manage_db_popup_rename_label, m_db_username);
		m_rename_tv.setText(rename_label);
		m_rename_tv.setOnLongClickListener(this);

		m_help_iv = (ImageView) findViewById(R.id.manage_db_popup_logo);
		m_help_iv.setOnClickListener(this);

		m_cancel_butt = (Button) findViewById(R.id.manage_db_popup_cancel_butt);
		m_cancel_butt.setOnClickListener(this);
		m_cancel_butt.setOnLongClickListener(this);

		m_clear_set_data_butt = (Button) findViewById(R.id.manage_db_popup_clear_set_data_butt);
		m_clear_set_data_butt.setOnClickListener(this);
		m_clear_set_data_butt.setOnLongClickListener(this);

	} // onCreate(.)


	@Override
	public void onClick(View v) {
		WGlobals.play_short_click();

		if (v == m_delete_butt) {
			show_yes_no_dialog(R.string.manage_db_popup_delete_sure_title, new String[] {m_db_username},
							R.string.manage_db_popup_delete_sure_msg, new String[] {m_db_username},
							new OnClickListener() {
				@Override
				public void onClick(View v) {
					WGlobals.play_short_click();
					Intent itt = new Intent();
					itt.putExtra(OPERATION_CODE_KEY, OPERATION_CODE_DELETE);
					setResult(RESULT_OK, itt);
					dismiss_all_dialogs();	// Tells the yes/no dialog to go away
					finish();
				}
			});
		} // delete

		else if (v == m_clear_set_data_butt) {
			show_yes_no_dialog(R.string.manage_db_popup_clear_set_data_yesno_title, new String[] {m_db_username},
								R.string.manage_db_popup_clear_set_data_yesno_msg, new String[] {m_db_username},
								new OnClickListener() {
					@Override
					public void onClick(View v) {
						WGlobals.play_short_click();
						Intent itt = new Intent();
						itt.putExtra(OPERATION_CODE_KEY, OPERATION_CODE_CLEAR_SET_DATA);
						setResult(RESULT_OK, itt);
						dismiss_all_dialogs();	// Tells the yes/no dialog to go away
						finish();
					}
				});

		} // clear set data

		else if (v == m_rename_butt) {
			String new_name = m_rename_et.getText().toString();
			if (new_name.length() == 0) {
				my_toast(this, R.string.manage_db_popup_rename_no_name_error);
				return;
			}
			Intent itt = new Intent();
			itt.putExtra(OPERATION_CODE_KEY, OPERATION_CODE_RENAME);
			itt.putExtra(OPERATION_NEW_NAME_KEY, new_name);
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_help_iv) {
			show_help_dialog(R.string.manage_db_popup_help_title, R.string.manage_db_popup_help_msg);
		}

		else if (v == m_cancel_butt) {
			setResult(RESULT_CANCELED);
			finish();
		}


		else {
			Log.e (tag, "Unrecognized View in onClick()!");
		}

	} // onClick


	@Override
	public boolean onLongClick(View v) {
		WGlobals.play_long_click();

		if (v == m_delete_butt) {
			show_help_dialog(R.string.manage_db_popup_delete_help_title, R.string.manage_db_popup_delete_help_msg);
			return true;
		}

		else if (v == m_clear_set_data_butt) {
			show_help_dialog(R.string.manage_db_popup_clear_set_data_help_title, R.string.manage_db_popup_clear_set_data_help_msg);
			return true;
		}

		else if ((v == m_rename_butt) || (v == m_rename_tv)) {
			show_help_dialog(R.string.manage_db_popup_rename_help_title, R.string.manage_db_popup_rename_help_msg);
			return true;
		}

		else if (v == m_cancel_butt) {
			show_help_dialog(R.string.manage_db_popup_cancel_help_title, R.string.manage_db_popup_cancel_help_msg);
			return true;
		}

		Log.e(tag, "Unhandled View in onLongClick()!");
		return false;
	}


}
