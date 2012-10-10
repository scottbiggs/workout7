/**
 * This shows our preference screen.  I'm trying to make it
 * as Android as possible.
 */
package com.sleepfuriously.hpgworkout;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Shows our prefs.xml file.
		addPreferencesFromResource(R.xml.pref);

	} // onCreate (.)



}
