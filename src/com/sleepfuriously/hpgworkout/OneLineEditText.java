package com.sleepfuriously.hpgworkout;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * This replaces the normal EditText widget with one that
 * ignores <enter> keys, preventing users from entering text
 * with more than one line.
 *
 * Need to specially use this in the xml files via:
 *
 * 		<com.sleepfuriously.hpgworkout.OneLineEditText
 * 			android:id=...
 * 			...
 * 		/>
 *
 * Based on code found here:
 * 	http://stackoverflow.com/questions/6070805/prevent-enter-key-on-edittext-but-still-show-the-text-as-multi-line
 *
 */
public class OneLineEditText extends EditText {

	public OneLineEditText(Context context) {
		super(context);
	}
	public OneLineEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public OneLineEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			// Just ignore the [Enter] key
			return true;
		}
		// Handle all other keys in the default way
		return super.onKeyDown(keyCode, event);
	}

}
