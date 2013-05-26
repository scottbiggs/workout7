package com.sleepfuriously.hpgworkout;

/**
 * This allows WheelFloat instances to have a callback.
 *
 * <p>The onChanged() method is called whenever any of the
 * wheels change for any reason.
 *
 * <p>Note that this can cause an infinite loop if the
 * callback modifies the wheel!
 */
public interface OnWheelFloatListener {
	/**
	 * Callback method to be invoked when current item changed.
	 *
	 * @param new_val	The new value of the WheelFloat.
	 */
	public void onWheelFloatChanged(float new_val);

}
