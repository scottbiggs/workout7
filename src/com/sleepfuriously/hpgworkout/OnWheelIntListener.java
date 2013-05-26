package com.sleepfuriously.hpgworkout;

/**
 * This allows WheelInt instances to have a callback.
 *
 * <p>The onChanged() method is called whenever any of the
 * wheels change for any reason.
 *
 * <p>Note that this can cause an infinite loop if the
 * callback modifies the wheel!
 */
public interface OnWheelIntListener {
	/**
	 * Callback method to be invoked when current item changed.
	 *
	 * @param new_val	The new value of the WheelInt.
	 */
	public void onWheelIntChanged(int new_val);

}
