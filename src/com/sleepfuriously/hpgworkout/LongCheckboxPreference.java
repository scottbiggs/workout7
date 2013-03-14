/**
 * Extends CheckboxPreference so I can have longer messages.
 *
 * What a pain.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LongCheckboxPreference extends CheckBoxPreference {

	//---------------------
	//	Constructors
	//---------------------

	public LongCheckboxPreference(Context context,
	                              AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public LongCheckboxPreference(Context context,
	                              AttributeSet attrs)
	{
		super(context, attrs);
	}


	//---------------------
	//	Overloaded Methods
	//---------------------
	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		TextView summary= (TextView)view.findViewById(android.R.id.summary);

		// This is IT!  The regular version just has 2.
		summary.setMaxLines(8);
	}

	/* (non-Javadoc)
	 * @see android.preference.CheckBoxPreference#onClick()
	 */
	@Override
	protected void onClick() {
		WGlobals.play_short_click();
		super.onClick();
	}

	
	
}
