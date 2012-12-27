/**
 * This is a replacement for the regular Calendar class.
 * I found that it's rather awkward to use.
 */
package com.sleepfuriously.hpgworkout;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;

public class MyCalendar {

	private static final String tag = "MyCalendar";

	public static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

	/** Start with a calendar for NOW */
	public Calendar m_cal = Calendar.getInstance();

	/** Determines if the current date is legal or not. */
	private boolean m_legal_date = false;

	//------------------------------
	// Constructors
	//------------------------------
	MyCalendar() {
		m_legal_date = true;
	}
	MyCalendar(MyCalendar cal_to_copy) {
		m_cal = (Calendar) cal_to_copy.m_cal.clone();
		m_legal_date = true;
	}
	MyCalendar(Calendar cal_to_copy) {
		m_cal = (Calendar) cal_to_copy.clone();
		m_legal_date = true;
	}
	MyCalendar (long millis) {
		set_millis(millis);
		m_legal_date = true;
	}

	/********************
	 * Convenience function.
	 *
	 * @return	The year that this date was set.
	 */

	int get_year() {
		if (!m_legal_date)
			return -1;
		return m_cal.get(Calendar.YEAR);
	}

	/*********************
	 * Convenience function.  Simply returns the last two digits
	 * of the year, instead of the whole year number.
	 *
	 * @return	The last two year digits
	 */
	int get_year_two_digit() {
		return get_year() % 100;
	} // get_year_two_digit()


	/********************
	 * Convenience function.
	 *
	 * @param	The context of the calling class.  Needed to
	 * 			access the resource file.
	 *
	 * @return	The month that this date was set (0 based).
	 */

	String get_month_text(Context ctxt) {
		if (!m_legal_date)
			return null;

		String months[] = ctxt.getResources().getStringArray(R.array.months);
		int mo = m_cal.get(Calendar.MONTH);

		return months[mo];
	}

	/********************
	 * Finds the month number.  Months start at 0 (January) and
	 * end with 11 (December).
	 */
	int get_month() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.MONTH);
	}

	/********************
	 * Finds the month number, but this version uses the
	 * standard [1..12] format.
	 */
	int get_month_12() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.MONTH) + 1;
	}

	/********************
	 * Convenience function.
	 *
	 * @return	The day of the month that this date was set.
	 */

	int get_day() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.DAY_OF_MONTH);
	}

	/*********************
	 * @return	The number of days since Jan 1st, 1970
	 * 			that this calendar day falls on.
	 */
	public long get_absolute_days() {
		MyCalendar temp_copy;
		long millis;

		if (!m_legal_date)
			return 0;

		// Find the number of milliseconds for the beginning of
		// today.
		temp_copy = new MyCalendar(this);
		temp_copy.set_time(0, 0, 0);

		// Needed to make sure there are no milliseconds to give us errors.
		millis = truncate_to_seconds(temp_copy.get_millis());

		long return_val = millis / MILLIS_PER_DAY;
		return return_val;

	} // get_absolute_days()


	/********************
	 * Finds how many days the two MyCalendar dates differ.
	 * Equivalent to:
	 * 		this.day - other_date.day
	 *
	 * @param other_cal		The subtractor date.
	 *
	 * @return	The difference in number of days between
	 * 			this date subtracted by other_date.  Yes,
	 * 			this could be a negative number.  Note that
	 * 			if they are on the same date, then this
	 * 			returns 0.
	 * 			- Also returns 0 if either is not a legal
	 * 			date.
	 */
	public long get_difference_in_days (MyCalendar other_cal) {
		return get_absolute_days() - other_cal.get_absolute_days();
//		MyCalendar temp;
//		long millis;
//
//		if (!m_legal_date)
//			return 0;
//
//		if (!other_cal.is_legal_date())
//			return 0;
//
//		// Find the number of milliseconds for the beginning of
//		// today.
//		temp = new MyCalendar(this);
//
//		temp.set_time(0, 0, 0);
//
//		// Needed to make sure there are no milliseconds to give us errors.
//		millis = truncate_to_seconds(temp.get_millis());
//		temp.set_millis(millis);
//
//		long millis_beginning_today = temp.get_millis();
//
//		// do the same for the other date.
//		temp.set_millis(other_cal.get_millis());
//		temp.set_time(0, 0, 0);
//		millis = truncate_to_seconds(temp.get_millis());
//		temp.set_millis(millis);
//		long millis_beginning_other = temp.get_millis();
//
//		// NOTE: neither of these millis numbers should be zero!
//		// The chances of that happening are pretty low!
//
//		long millis_diff = millis_beginning_today - millis_beginning_other;
//		long return_val = millis_diff / MILLIS_PER_DAY;
//		return return_val;
	} // get_difference_in_days (other_date)


	/********************
	 * Convenience function.  NOTE: hours are military.
	 */
	int get_hour() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.HOUR_OF_DAY);
	}

	/********************
	 * Convenience function.
	 */
	int get_minutes() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.MINUTE);
	}

	/********************
	 * Convenience function.
	 */
	int get_seconds() {
		if (!m_legal_date)
			return -1;

		return m_cal.get(Calendar.SECOND);
	}

	/********************
	 * Convenience function.
	 */
	long get_millis() {
		if (!m_legal_date)
			return -1;

		return m_cal.getTimeInMillis();
	}

	/********************
	 * Convenience function.  This returns a String suitable
	 * for displaying the time-of-day for this MyCalendar.
	 *
	 * @param	military		TRUE means to use military time.
	 * 						Otherwise am/pm is appended.
	 *
	 * @return	A String for the time.
	 */
	String print_time (boolean military) {
		if (!m_legal_date)
			return null;

		String str = "", am_pm = "";
		int hours, mins;

		mins = m_cal.get(Calendar.MINUTE);

		if (military) {
			hours = m_cal.get(Calendar.HOUR_OF_DAY);
		}
		else {
			hours = m_cal.get(Calendar.HOUR);
			// Hours can sometimes be '0'.  Change this to '12'.
			if (hours == 0) {
				hours = 12;
			}
			if (m_cal.get(Calendar.AM_PM) == Calendar.AM) {
				am_pm = "am";
			}
			else {
				am_pm = "pm";
			}

			// The two special cases.
			if ((mins == 0) && (hours == 12)) {
				if (am_pm.equals("am"))
					return "midnight";
				else
					return "noon";
			}
		}

		if (military) {
			str = String.format("%02d:%02d", hours, mins);
		}
		else {
			str = String.format("%d:%02d %s", hours, mins, am_pm);
		}
		return str;
	} // print_time (military)

	/********************
	 * Convenience function.  This returns a String suitable
	 * for displaying the complete date (no time).
	 *
	 * @param	ctx		A context.  This allows the routine
	 * 					to look up teh string for the month
	 * 					name (varies by language!).
	 */
	public String print_date (Context ctx) {
		return get_month_text(ctx) + " " + get_day() + ", " + get_year();
	}

	/********************
	 * Returns a string suitable for displaying the date
	 * WITHOUT the year.
	 *
	 * @param	ctx		A context.  This allows the routine
	 * 					to look up teh string for the month
	 * 					name (varies by language!).
	 */
	public String print_month_day (Context ctx) {
		return get_month_text(ctx) + " " + get_day();
	}

	/********************
	 * Returns a string suitable for printing the date.  This
	 * version uses just a number format, like 9/12/2011.
	 */
	public String print_date_numbers() {
		return get_month_12() + "/" + get_day() + "/" + get_year();
	}

	/********************
	 * Returns a string suitable for printing the date.  This
	 * version uses numbers, but does NOT include the year.
	 */
	public String print_month_day_numbers() {
		return get_month_12() + "/" + get_day();
	}

	/********************
	 * Sets the calendar to a time denoted by all the milliseconds
	 * since some day in 1970.
	 *
	 * @param	millis		The number of milliseconds this
	 * 						calendar represents.
	 */
	void set_millis (long millis) {
		m_cal.setTimeInMillis(millis);
		m_legal_date = true;
	}

	/********************
	 * Helper method.  Sets the year, month, and day,
	 * changing nothing else.  Note that the month is
	 * zero based [0..11].
	 */
	void set_year_month_day (int year, int month, int day) {
		m_cal.set(year, month, day);
		m_legal_date = true;
	}

	/********************
	 * Sets the time of this calendar, and nothing else.
	 *
	 * NOTE:
	 * 		hours is military time.
	 */
	void set_time (int hours, int mins, int secs) {
		m_cal.set(Calendar.HOUR_OF_DAY, hours);
		m_cal.set(Calendar.MINUTE, mins);
		m_cal.set(Calendar.SECOND, secs);
		m_legal_date = true;
	}


	/********************
	 * Tests to see if this other MyCalendar has the same day
	 * as this one.
	 *
	 * @param other	The other MyCalendar to test.
	 *
	 * @return	TRUE iff the year, month, and day-of-month is
	 * 			the same for 'other' and this calendar.
	 */
	boolean is_same_day (MyCalendar other) {
		if (!m_legal_date)
			return false;

		int day_me, day_other;
		int month_me, month_other;
		int year_me, year_other;

		year_me = m_cal.get(Calendar.YEAR);
		year_other = other.m_cal.get(Calendar.YEAR);
		month_me = m_cal.get(Calendar.MONTH);
		month_other = other.m_cal.get(Calendar.MONTH);
		day_me = m_cal.get(Calendar.DAY_OF_MONTH);
		day_other = other.m_cal.get(Calendar.DAY_OF_MONTH);

		if (year_me != year_other) {
			return false;
		}
		if (month_me != month_other) {
			return false;
		}
		if (day_me != day_other) {
			return false;
		}
		return true;
	} // is_same_day (other)


	/****************************
	 * Tests to see if this calendar has been set to
	 * an illegal date or not.
	 *
	 * NOTE:		There's really no such thing as an illegal
	 * 			date.  This is just a state that's different
	 * 			from any possible date.  Used to tell if
	 * 			the instance has been initialized or forcibly
	 * 			set to this state.
	 */
	public boolean is_legal_date() {
		return m_legal_date;
		}

	/****************************
	 * Clears the calendar of all date info.  Any
	 * inquries should induce an error.
	 *
	 * Setting this instance to a valid date makes
	 * it legal (again).
	 */
	public void make_illegal_date() {
		m_legal_date = false;
	}

	/*****************************
	 * Given a date in millis form, this truncates the extraneous
	 * milliseconds.  Use this to set the time EXACTLY.
	 *
	 * @param millis		A date/time represented in milliseconds.
	 *
	 * @return		The same date and time, but the remaining
	 * 				milliseconds should be 0.
	 */
	protected long truncate_to_seconds (long date_in_millis) {
		long l = date_in_millis / 1000L;
		l *= 1000L;
		return l;
	} // truncate_to_seconds(date_in_millis)
}
