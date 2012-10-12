package com.sleepfuriously.hpgworkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {

	private static final String tag = "TestActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_layout_for_testing);
		
		Intent itt = getIntent();
		String ex_name = itt.getStringExtra(ExerciseTabHostActivity.KEY_NAME);
		int id = itt.getIntExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);

		Log.v(tag, "onCreate(): name = " + ex_name +
				", id = " + id);
	}

	
}
