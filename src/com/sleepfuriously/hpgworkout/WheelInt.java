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

//public class WheelInt implements OnWheelChangedListener {
public class WheelInt implements OnWheelScrollListener {

	//--------------------
	//	Constants
	//--------------------

	private static final String tag = "WheelInt";

	/** The size of the text in the wheels. */
	public static final int DEFAULT_WHEEL_TEXT_SIZE = 12;

	/** Width of each wheel */
	public static final int DEFAULT_WHEEL_WIDTH = 40;

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


	//--------------------
	//	Methods
	//--------------------

	/**
	 * Constructor.
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
			adapter.setTextSize(DEFAULT_WHEEL_TEXT_SIZE);
			m_wheels[i].setViewAdapter(adapter);

			m_wheels[i].addScrollingListener(this);
//			m_wheels[i].addScrollingListener(m_scrolledListener);
			m_wheels[i].setCyclic(true);
			m_wheels[i].setInterpolator(new AnticipateOvershootInterpolator());
			m_wheels[i].setMinimumWidth(DEFAULT_WHEEL_WIDTH);
		}

		// Finish by zeroing out the number.
		reset();
	} // constructor

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
	 */
	public void set_ind_wheel (int index, int val) {
		m_wheels[index].setCurrentItem(val);
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
	 * Sets all the wheels to zero.
	 *
	 * side effects:
	 * 		If there's a TextView attached, it goes
	 * 		to zero as well.
	 */
	public void reset() {
		for (int i = 0; i < m_wheels.length; i++) {
			set_ind_wheel(i, 0);
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
	 */
	public void set_value (int val) {
		// Easy case first.
		if (val == 0) {
			reset();
			return;
		}

		if (val > max_val()) {
			val = max_val();
		}
		else if (val < min_val()) {
			val = min_val();
		}
		for (int i = 0; i < m_wheels.length; i++) {
			int digit = val / (10 ^ i) - (val / (10 ^ (i + 1))) * 10; // http://www.vbforums.com/showthread.php?375620-Finding-the-value-of-the-nth-digit-of-a-number
//			int digit = (val / (10 ^ i)) % 10;	// http://stackoverflow.com/questions/203854/how-to-get-the-nth-digit-of-an-integer-with-bit-wise-operations
			m_wheels[i].setCurrentItem(digit, true);
		}
		if (m_result_tv != null) {
			m_result_tv.setText("" + val);
		}
	} // set_value (val)

	/************************
	 * Returns the maximum value that this widget
	 * can display.
	 */
	public int max_val() {
		return (10 ^ m_wheels.length) - 1;
	}

	/************************
	 * Returns the minimum value that this widget
	 * can display.
	 */
	public int min_val() {
		return 0;
	}

	// Not used
	@Override
	public void onScrollingStarted(WheelView wheel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollingFinished(WheelView wheel) {
		int val = get_value();
		Log.d(tag, "onChanged(). get_value() is " + val);

		if (m_result_tv != null) {
			m_result_tv.setText(Integer.toString(val));
		}
	}

	/*********************
	 * Hits when a wheel changes.
	 */
//	@Override
//	public void onChanged(WheelView wheel, int old_val, int new_val) {
//		int val = get_value();
//		Log.d(tag, "onChanged(). get_value() is " + val);
//
//		if (m_result_tv != null) {
//			m_result_tv.setText(Integer.toString(val));
//		}
//	} // onChanged(wheel, old_val, new_val)
//

}
