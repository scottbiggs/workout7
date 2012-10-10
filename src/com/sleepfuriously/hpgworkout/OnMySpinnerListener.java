/**
 * This interface is needed to receive events from
 * MySpinner.  It's really pretty simple to use.
 *
 *	1. Implement OnMySpinnerListener
 *		eg:  class foo extends Activity
 *					   implements OnMySpinnerListener {
 *
 *	2. The required method, onMySpinnerSelected (spinner, selection)
 *		needs to be implemented.  This is where you put
 *		your reaction to the user's selection.
 */
package com.sleepfuriously.hpgworkout;

public interface OnMySpinnerListener {

	/**
	 * This is the callback that is fired when the user
	 * selects an item from the MySpinner class.  You
	 * need to override it and write your own version.
	 *
	 * @param spinner		A copy of the MySpinner that
	 * 						was activated.  Use this to
	 * 						see which MySpinner fired.
	 *
	 * @param position		The number in the array that
	 * 						the user selected.  Starts at
	 * 						0, like any normal array.
	 *
	 * @param new_item		When TRUE, the user created a
	 * 						brand-new item.  It has been
	 * 						added to the array and is at
	 * 						this position.  Use
	 * 						MySpinner.get_item (position)
	 * 						to find out this new string.
	 */
	public void onMySpinnerSelected (MySpinner spinner,
	                                 int position,
	                                 boolean new_item);
}
