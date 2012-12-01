/**
 * This class treats several wheels as if they're just
 * one, simplifying the work needed by the caller.  This
 * is built off WheelInt and works for Floats.
 *
 * Note that the order is always little-endian with these
 * wheels.  The first wheel is the least-significant digit.
 *
 */
package com.sleepfuriously.hpgworkout;

import android.app.Activity;
import android.os.PowerManager;
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

//public class WheelFloat implements OnWheelChangedListener {
public class WheelFloat implements OnWheelScrollListener {

	//--------------------
	//	Constants
	//--------------------

	private static final String tag = "WheelFloat";

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

	/*
	 *  This tells how many of the wheels are to the
	 *  right of the decimal place.
	 */
	protected int m_num_dec_places;

	// todo: is this really necessary?
	/** The Activity that is using this widget */
	Activity m_activity;

	/** Optionally displays the results of this widget */
	TextView m_result_tv = null;

	/** The max & mins that this wheel can display */
	final float m_max_val, m_min_val;

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
	 *
	 * @param num_dec	This is how many decimal places
	 * 					there are for this set.
	 */
	WheelFloat (Activity activity, int[] wheel_id_array, int num_dec) {
		m_activity = activity;
		m_num_dec_places = num_dec;
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

		// Set the mins and maxs for this instance.
		m_min_val = 0;
		m_max_val = powers_of_ten(m_wheels.length - m_num_dec_places)
							- (1 / powers_of_ten(m_num_dec_places));

		// Finish by zeroing out the number.
		reset(false);
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
	 *
	 * @param	anim			TRUE = show animation
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
	public float get_value() {
		double val = 0d;
		double mult = 1d / (double)(10 * m_num_dec_places);

		for (int i = 0; i < m_wheels.length;
				i++, mult *= 10d) {
			val += m_wheels[i].getCurrentItem() * mult;
		}

		return (float)val;
	} // get_current_value()


	/***********************
	 * Sets all the wheels to zero.
	 *
	 * side effects:
	 * 		If there's a TextView attached, it goes
	 * 		to zero as well.
	 *
	 * @param	anim		Show animation or not.
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
	 */
	public void set_value (float val, boolean anim) {
		// Easy case first.
		if (val == 0f) {
			reset(anim);
			return;
		}

		if (val > max_val()) {
				val = max_val();
			}
		else if (val < min_val()) {
				val = min_val();
		}

		// This is so much easier if we convert to a string
		// and parse it that way.
		String str = Float.toString(val), whole_str, frac_str;

		// Find the decimal point in our string.
		int point = str.indexOf('.');
		if (point == -1) {
			point = str.indexOf(',');
		}

		// todo: now that we have the decimal point, figure out
		//	what to do!

		// Split the string into whole and fractional parts.
		if (point < 0) {
			// No decimal point at all.
			whole_str = str;
			frac_str = "";
		}
		else {
			whole_str = str.substring(0, point);
			frac_str = str.substring(point + 1);
		}

		// fill in the whole number wheels
		int i = m_num_dec_places;
		int decrementer = whole_str.length() - 1;
		while (i < m_wheels.length) {
			if (decrementer < 0) {
				// Put zeros in the wheels for values not
				// specified (higher order).
				m_wheels[i].setCurrentItem(0, anim);
			}
			else {
				// The normal.
				m_wheels[i].setCurrentItem(whole_str.charAt(decrementer) - '0',
										   anim);
			}

			i++;
			decrementer--;
		}

		// Do the fractional part.
		i = 0;
		decrementer = frac_str.length() - 1;
		while (i < m_num_dec_places) {
			if (decrementer < 0) {
				m_wheels[i].setCurrentItem(0, anim);
			}
			else {
				m_wheels[i].setCurrentItem(frac_str.charAt(decrementer) - '0',
										   anim);
			}
			i++;
			decrementer--;
		}

		if (m_result_tv != null) {
			m_result_tv.setText(str);
		}
	} // set_value (val)


	/************************
	 * Returns the maximum value that this widget
	 * can display.
	 */
	public float max_val() {
		return m_max_val;
	}

	/************************
	 * Returns the minimum value that this widget
	 * can display.
	 */
	public float min_val() {
		return 0f;
	}

	/*********************
	 * Hits when a wheel changes.
	 */
//	@Override
//	public void onChanged(WheelView wheel, int old_val, int new_val) {
//		float value = get_value();
//		Log.d(tag, "onChanged(), value is " + value);
//		if (m_result_tv != null) {
//			m_result_tv.setText(Float.toString(value));
//		}
//	} // onChanged(wheel, old_val, new_val)



	// Not used
	@Override
	public void onScrollingStarted(WheelView wheel) {
	}

	@Override
	public void onScrollingFinished(WheelView wheel) {
		float value = get_value();
		Log.d(tag, "onChanged(), value is " + value);
		if (m_result_tv != null) {
			m_result_tv.setText(Float.toString(value));
		}
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

}
