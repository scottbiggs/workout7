/**
 * This shows our preference screen.  I'm trying to make it
 * as Android as possible.
 */
package com.sleepfuriously.hpgworkout;

import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PrefsActivity extends PreferenceActivity {

	//--------------------------------
	//	Constants
	//--------------------------------

	private static final String tag = "PrefsActivity";

	/** The number of pixels wide (default) for a single Wheel */
	public static final int DEFAULT_WHEEL_WIDTH = 10;

	//--------------------------------
	//	Data
	//--------------------------------

	/** Made external so that we can control a help dialog */
	protected static Dialog m_dialog = null;


	//--------------------------------
	//	Methods
	//--------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the volume buttons to control the volume of our sounds
		// (music).
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Shows our prefs.xml file.
		addPreferencesFromResource(R.xml.pref);	// Load the preferences xml
		setContentView(R.layout.my_prefs);		// Load the Activity xml (note that
												// it references via the ListView with
												// a special ID).

		Button done = (Button) findViewById(R.id.my_prefs_done_butt);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WGlobals.play_short_click();
				finish();
			}
		});


		ImageView logo = (ImageView) findViewById(R.id.my_prefs_logo);
		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WGlobals.play_help_click();

				if (m_dialog != null) {
					Log.e(tag, "Starting a new dialog, but m_dialog is NOT null!!");
					m_dialog.dismiss();
					m_dialog = null;
				}

				// Start a new dialog.  Copied from BaseDialogActivity.
				m_dialog = new Dialog(PrefsActivity.this);

				// This prevents the automatic title area from being made.
				// And it has to be the first thing, too.
				m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				m_dialog.setContentView(R.layout.dialog_help);

				// Set the volume buttons to control the volume of our sounds
				// (music).
				m_dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);

				// Fill in the Views (title & msg).
				TextView title = (TextView) m_dialog.findViewById(R.id.dialog_help_title_tv);
				title.setText(R.string.my_prefs_help_title);

				TextView msg = (TextView) m_dialog.findViewById(R.id.dialog_help_msg_tv);
				msg.setText(R.string.my_prefs_help_msg);

				Button ok_butt = (Button) m_dialog.findViewById(R.id.dialog_help_ok_butt);
				ok_butt.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						WGlobals.play_short_click();
						m_dialog.dismiss();
						m_dialog = null;
					}
				});
				m_dialog.show();

			} // onClick(v)
		});

	} // onCreate (.)



}
