/**
 * This shows our preference screen.  I'm trying to make it
 * as Android as possible.
 */
package com.sleepfuriously.hpgworkout;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {

	//--------------------------------
	//	Constants
	//--------------------------------

	/** The number of pixels wide (default) for a single Wheel */
	public static final int DEFAULT_WHEEL_WIDTH = 10;


	//--------------------------------
	//	Methods
	//--------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Shows our prefs.xml file.
		addPreferencesFromResource(R.xml.pref);

	} // onCreate (.)



}
