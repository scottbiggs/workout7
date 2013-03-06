/**
 * This is an Activity that looks like a dialog.  Use it
 * to grab a TEXT from the user.  It's for *notes*!
 *
 * It's a copy of NumberActivity.
 *
 * Call this Activity via startActivityForResult().  But
 * first, you gotta set the Intent...
 * 		1.	If there's an old value, supply the boolean
 * 			that indicates YES (true).
 * 		2.	If you did do step 1, supply that string.
 *
 *
 * NEXT, when this returns, in your onActivityResult(),
 * check the data Intent.  It'll hold the value if the
 * returnCode is RESULT_OK.  Otherwise, there may not
 * even be any value (the user aborted, hit "back", etc.).
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class TextActivity
			extends
				BaseDialogActivity
			implements
				View.OnClickListener {

	//----------------------------
	//	Constants
	//----------------------------

	/**
	 * INPUT
	 * 		boolean
	 *
	 * The key for the boolean that indicates there will
	 * be another item in the Intent that holds the old
	 * note to display.  If you don't want to show it
	 * set this to FALSE).
	 */
	public static final String ITT_KEY_OLD_NOTE_BOOL = "old_bool";

	/**
	 * INPUT
	 * 		String
	 *
	 * Only used if the above is TRUE.  This holds the str
	 * to display as an old value that the user is replacing.
	 */
	public static final String ITT_KEY_OLD_NOTE_STRING = "old_string";

	/**
	 * OUTPUT
	 * 		String
	 *
	 * The key for the RETURN VALUE of this intent.
	 */
	public static final String ITT_KEY_RETURN_STRING = "new_string";


	//----------------------------
	//	Widgets
	//----------------------------
	Button m_cancel, m_done;
	ImageView m_help;

	/** The main thing! */
	EditText m_note;


	//----------------------------
	//	Data
	//----------------------------

	/** Has the user touched anything yet? */
	boolean m_dirty = false;


	//----------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enter_text);

		m_cancel = (Button) findViewById(R.id.enter_text_cancel_butt);
		m_help = (ImageView) findViewById(R.id.enter_text_logo);
		m_done = (Button) findViewById(R.id.enter_text_ok_butt);

		m_note = (EditText) findViewById(R.id.enter_text_display_et);

		m_cancel.setOnClickListener(this);
		m_help.setOnClickListener(this);
		m_done.setOnClickListener(this);

		// A couple of TextViews that need to be filled in
		// programmatically.
		TextView prompt_tv = (TextView) findViewById(R.id.enter_text_prompt_tv);
		TextView old_note_tv = (TextView) findViewById(R.id.enter_text_old_note_tv);

		Intent itt = getIntent();

		// If there's an old value, then get it. Also
		// set the TextView appropriately.
		if (itt.getBooleanExtra(ITT_KEY_OLD_NOTE_BOOL, false)) {
			prompt_tv.setText(R.string.enter_text_msg_old);
			String note = itt.getStringExtra(ITT_KEY_OLD_NOTE_STRING);
			old_note_tv.setText(note);
		}
		else {
			// With no old string, things are a little
			// different.
			prompt_tv.setText(R.string.enter_text_msg_empty);
			old_note_tv.setVisibility(View.INVISIBLE);
			m_done.setEnabled(false);	// They gotta make a change
										// for this button to activate.

		}

		// Allows us to see if anything has changed in the
		// EditText where they are typing.  Just to keep track
		// of the dirtiness!
		m_note.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence str, int start,
									int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence str,
										int start, int count,
										int after) {
			}

			@Override
			public void afterTextChanged(Editable str) {
				m_dirty = true;
				m_done.setEnabled(true);
			}
		});

	} // onCreate (.)


	//----------------------------
	@Override
	public void onClick(View v) {
		WGlobals.button_click();

		if (v == m_done) {
			Intent itt = new Intent();
			itt.putExtra(ITT_KEY_RETURN_STRING, m_note.getText().toString());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_help) {
			show_help_dialog(R.string.enter_text_help_title,
					R.string.enter_text_help_msg);
		}

		else if (v == m_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

	} // onClick (v)

}
