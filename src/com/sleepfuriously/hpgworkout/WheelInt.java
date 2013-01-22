/**
 * This class treats several wheels as if they're just
 * one, simplifying the work needed by the caller.  These
 * wheels represent an integer.
 *
 * Note that the order is always little-endian with these
 * wheels.  The first wheel is the least-significant digit.
 *
 */
package com.sleepfuriously.hpgworkout;

import android.app.Activity;
import android.util.Log;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

/********************************************
 *
 * This encapsulates a wheel that represents an
 * integer number.
 *
 * In order to be useful, construct this with
 *
 *
 */

public class WheelInt implements OnWheelChangedListener {
//public class WheelInt implements OnWheelScrollListener {

	//--------------------
	//	Constants
	//--------------------

	private static final String tag = "WheelInt";


	//--------------------
	//	Data
	//--------------------

	/**
	 * Holds the actual WheelViews for our wheels.
	 * Note that the array is arranged little-endian.
	 * That means that the least-significant wheel is
	 * in index 0 (the units).  Index 1 is the tens,
	 * index 2 is the hundreds, etc.
	 */
	protected WheelView[] m_wheels;

	// todo: is this really necessary?
	/** The Activity that is using this widget */
	Activity m_activity;

	/** Optionally displays the results of this widget */
	TextView m_result_tv = null;

	/** The max & mins that this wheel can display */
	final long m_max_val, m_min_val;

	//--------------------
	//	Methods
	//--------------------

	/***************************
	 * Constructor.
	 * <p>
	 * preconditions:<br/>
	 * 	WGlobals.g_wheel_width is used to initially size the wheels.
	 * 			You can change this by calling set_wheel_width().
	 *
	 * @param ctx	The context. Always useful.
	 *
	 * @param wheel_id_array
	 * 		An array of layout ids that describe the
	 * 		WheelViews used for this number.  The array
	 * 		needs to be ordered little-endian (ie least-
	 * 		significant first).
	 */
	WheelInt (Activity activity, int[] wheel_id_array) {
		m_activity = activity;
		m_wheels = new WheelView[wheel_id_array.length];
		for (int i = 0; i < m_wheels.length; i++) {
			m_wheels[i] = (WheelView) m_activity.findViewById(wheel_id_array[i]);
			NumericWheelAdapter adapter = new NumericWheelAdapter(m_activity, 0, 9);
			adapter.setTextSize(WGlobals.g_wheel_text_size);
			m_wheels[i].setViewAdapter(adapter);

			m_wheels[i].addChangingListener(this);
			m_wheels[i].setCyclic(true);
			m_wheels[i].setInterpolator(new AnticipateOvershootInterpolator());
			m_wheels[i].setMinimumWidth(WGlobals.g_wheel_width);
		}

		// Set the mins and maxs for this instance.
		m_min_val = 0;
		m_max_val = powers_of_ten(m_wheels.length) - 1;

		// Finish by zeroing out the number.
		reset(false);
	} // constructor


	/********************
	 * Call this to set the width of the wheels.  This is
	 * a good way to override the default.
	 *
	 * @param pixels_wide	Number of pixels wide.  Default
	 * 						is 10, which is pretty narrow.
	 */
	public void set_wheel_width (int pixels_wide) {
		for (int i = 0; i != m_wheels.length; i++) {
			m_wheels[i].setMinimumWidth(pixels_wide);
		}
	} // set_wheel_width (pixels_wide)


	/********************
	 * If you want a TextView to be automatically updated,
	 * call this with the TextView in question.  Calling this
	 * automatically sets the TextView with the current number.
	 *
	 * @param tv		The TextView to hold a number result.
	 * 				Note that EditTexts are subclass of
	 * 				TextView and should work fine.
	 */
	public void set_tv (TextView tv) {
		tv.setText("" + get_value());
		m_result_tv = tv;
	}

	/********************
	 * Given an int, sets the wheels to the given number.
	 * If the number is out of range, this tries its
	 * best!
	 *
	 * @param	index		The index into the m_wheel array
	 * 						to change
	 *
	 * @param	val			The number to set the wheel to.
	 *
	 * @param	anim			TRUE = show animation.
	 */
	public void set_ind_wheel (int index, int val, boolean anim) {
		m_wheels[index].setCurrentItem(val, anim);
		if (m_result_tv != null) {
			m_result_tv.setText("" + get_value());
		}
	}

	/*********************
	 * finds the number that's currently displaying in
	 * the wheels.
	 *
	 * @return	The number shown by the wheels.
	 */
	public int get_value() {
		int val = 0;

		for (int i = 0, multiplier = 1;
				i < m_wheels.length;
				i++, multiplier *= 10) {
			val += m_wheels[i].getCurrentItem() * multiplier;
		}

		return val;
	} // get_current_value()


	/***********************
	 * Forces the wheels to redraw.  I'm using the
	 * foreach technique--cute, isn't it?
	 */
	public void invalidate() {
		for (WheelView wheel : m_wheels) {
			wheel.invalidate();
		}
	} // invalidate()


	/***********************
	 * Sets all the wheels to zero.
	 *
	 * side effects:
	 * 		If there's a TextView attached, it goes
	 * 		to zero as well.
	 *
	 * @param	anim		Animate or not.
	 */
	public void reset (boolean anim) {
		for (int i = 0; i < m_wheels.length; i++) {
			set_ind_wheel(i, 0, anim);
		}
		if (m_result_tv != null) {
			m_result_tv.setText("0");
		}
	}

	/***********************
	 * Sets the wheels to the given value.  If a TextView
	 * is attached, it'll be changed, too.
	 *
	 * @param val	We'll do our best to set the number
	 * 				to this value.
	 *
	 * @parm anim	Animate the setting change?
	 */
	public void set_value (int val, boolean anim) {
		// Easy case first.
		if (val == 0) {
			reset(anim);
			return;
		}

		// Test, set the first wheel to 8.
//		m_wheels[0].setCurrentItem(8);		that worked

		if (val > max_val()) {
			val = (int) max_val();
		}
		else if (val < min_val()) {
			val = (int) min_val();
		}
		for (int i = 0; i < m_wheels.length; i++) {
			int tens = (int) powers_of_ten(i);
//			int digit = val / (10 ^ i) - (val / (10 ^ (i + 1))) * 10; // http://www.vbforums.com/showthread.php?375620-Finding-the-value-of-the-nth-digit-of-a-number
			int digit = (val / tens) % 10;	// http://stackoverflow.com/questions/203854/how-to-get-the-nth-digit-of-an-integer-with-bit-wise-operations
			m_wheels[i].setCurrentItem(digit, anim);
		}
		if (m_result_tv != null) {
			m_result_tv.setText("" + val);
		}

	} // set_value (val, anim)

	/************************
	 * Returns the maximum value that this widget
	 * can display.
	 */
	public long max_val() {
		return m_max_val;
	}

	/************************
	 * Returns the minimum value that this widget
	 * can display.
	 */
	public long min_val() {
		return m_min_val;
	}


	/***********************
	 * Returns simply 10 ^ x.  Only works with
	 * positive values of x.
	 */
	public long powers_of_ten (int x) {
		long val = 1;
		for (int i = 0; i < x; i++) {
			val *= 10;
		}
		return val;
	}

	//--------------------------------
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		int val = get_value();
		Log.d(tag, "onChanged(). get_value() is " + val);

		if (m_result_tv != null) {
			m_result_tv.setText(Integer.toString(val));
		}
	}

}
