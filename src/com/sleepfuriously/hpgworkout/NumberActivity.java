/**
 * This is an Activity that looks like a dialog.  Use it
 * to grab a number from the user.  It'll be easy and
 * maybe even fun to use (instead of having the user
 * have this beautiful layout covered by their soft
 * keyboard).
 *
 * Call this Activity via startActivityForResult().  But
 * first, you gotta set the Intent...
 * 		1.	Tell us what the title should be.
 * 		2.	Do you want to allow decimal places?  In
 * 			other words, will this be a float or an
 * 			int?
 * 		3.	If there's an old value, supply the boolean
 * 			that indicates YES.
 * 		4.	If you did do step 3, supply that value.
 * 		5.	AND...tell us what aspect we are doing?
 * 			This should be in the form of an INT.
 *
 *
 * NEXT, when this returns, in your onActivityResult(),
 * check the data Intent.  It'll hold the value if the
 * returnCode is RESULT_OK.  Otherwise, there may not
 * even be any value (the user aborted, hit "back", etc.).
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class NumberActivity
						extends
							BaseDialogActivity
						implements
							View.OnClickListener {

	//----------------------------
	//	Constants
	//----------------------------

	/**
	 * INPUT
	 * 		String
	 *
	 * The key for sending a title to this class via
	 * an Intent.  The string connected to this key
	 * will be the title of this "dialog".
	 */
	public static final String ITT_KEY_TITLE = "title_str";

	/**
	 * INPUT
	 * 		boolean
	 *
	 * The key for a BOOLEAN value that determines
	 * whether or not we allow decimal values.
	 */
	public static final String ITT_KEY_DECIMAL_BOOL = "decimal_bool";

	/**
	 * INPUT
	 * 		boolean
	 *
	 * The key for the boolean that indicates there will
	 * be another item in the Intent that holds the old
	 * number to display.  If you don't want an old number
	 * to display (or don't give a shit about it), set this
	 * to FALSE).
	 */
	public static final String ITT_KEY_OLD_VALUE_BOOL = "old_bool";

	/**
	 * INPUT
	 * 		String
	 *
	 * Only used if the above is TRUE.  This holds a number
	 * to display as an old value that the user is replacing.
	 * Use the string version of the number.
	 */
	public static final String ITT_KEY_OLD_VALUE_STRING = "value_num";

	/**
	 * INPUT
	 * 		int
	 *
	 * This is a number that will be returned with the
	 * Intent when this finishes with RESULT_OK.  It
	 * tells which aspect of the exercise was modified
	 * (according to whatever system you want).
	 */
	public static final String ITT_KEY_RETURN_NUM = "ex_num";

	/**
	 * OUTPUT
	 * 		String
	 *
	 * The key for the RETURN VALUE of this intent.
	 * This will be the string version of the number.
	 */
	public static final String ITT_KEY_RETURN_VALUE = "value_num";


	/** Max size of a number in characters */
	private static final int MAX_NUMBER_LENGTH = 5;


	//----------------------------
	//	Widgets
	//----------------------------
	Button m_cancel, m_done,
		m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, mdot, mclear;

	/** Holds the number that the user types in */
	TextView m_number_tv;

	/** Help button (logo) */
	ImageView m_help;


	//----------------------------
	//	Data
	//----------------------------

	/** Are we making a float or an int? */
	boolean m_float = false;

	/** Has the user touched anything yet? */
	boolean m_dirty = false;

	/** This value is sent to us and needs to be returned. */
	int m_return_num;


	//----------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enter_a_number);

		m_cancel = (Button) findViewById(R.id.enter_a_number_cancel_butt);
		m_help = (ImageView) findViewById(R.id.enter_a_number_logo);
		m_done = (Button) findViewById(R.id.enter_a_number_ok_butt);

		m0 = (Button) findViewById(R.id.enter_a_number_button0);
		m1 = (Button) findViewById(R.id.enter_a_number_button1);
		m2 = (Button) findViewById(R.id.enter_a_number_button2);
		m3 = (Button) findViewById(R.id.enter_a_number_button3);
		m4 = (Button) findViewById(R.id.enter_a_number_button4);
		m5 = (Button) findViewById(R.id.enter_a_number_button5);
		m6 = (Button) findViewById(R.id.enter_a_number_button6);
		m7 = (Button) findViewById(R.id.enter_a_number_button7);
		m8 = (Button) findViewById(R.id.enter_a_number_button8);
		m9 = (Button) findViewById(R.id.enter_a_number_button9);
		mdot = (Button) findViewById(R.id.enter_a_number_button_dot);
		mclear = (Button) findViewById(R.id.enter_a_number_button_clear);

		m_cancel.setOnClickListener(this);
		m_help.setOnClickListener(this);
		m_done.setOnClickListener(this);

		m0.setOnClickListener(this);
		m1.setOnClickListener(this);
		m2.setOnClickListener(this);
		m3.setOnClickListener(this);
		m4.setOnClickListener(this);
		m5.setOnClickListener(this);
		m6.setOnClickListener(this);
		m7.setOnClickListener(this);
		m8.setOnClickListener(this);
		m9.setOnClickListener(this);
		mdot.setOnClickListener(this);
		mclear.setOnClickListener(this);

		m_number_tv = (TextView) findViewById(R.id.enter_a_number_display_tv);

		// A couple of TextViews that need to be filled in
		// programmatically.
		TextView title_tv = (TextView) findViewById(R.id.enter_a_number_title_tv);
		TextView value_tv = (TextView) findViewById(R.id.enter_a_number_value_tv);

		// Now grab the info from the Intent.  Start with
		// the title.
		Intent itt = getIntent();
		title_tv.setText(itt.getStringExtra(ITT_KEY_TITLE));

		// Are we doing an int or float?
		m_float = itt.getBooleanExtra(ITT_KEY_DECIMAL_BOOL, false);

		if (!m_float) {
			// Don't want the decimal point if we're
			// doing ints.
			mdot.setVisibility(View.GONE);
		}

		// If there's an old value, then get it.
		if (itt.getBooleanExtra(ITT_KEY_OLD_VALUE_BOOL, false)) {
			String num_str = itt.getStringExtra(ITT_KEY_OLD_VALUE_STRING);
			String str = getString(R.string.enter_a_number_old_value, num_str);
			value_tv.setText(str);
		}

		// Lastly, save the number that was sent to this
		// Activity.
		m_return_num = itt.getIntExtra(ITT_KEY_RETURN_NUM, -1);

	} // onCreate (.)


	//----------------------------
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
			case R.id.enter_a_number_ok_butt:
				if (m_dirty) {
					CharSequence str = m_number_tv.getText();
					if (str.equals(".")) {
						str = "0";
					}
					Intent itt = new Intent();
					itt.putExtra(ITT_KEY_RETURN_VALUE, str);
					itt.putExtra(ITT_KEY_RETURN_NUM, m_return_num);
					setResult(RESULT_OK, itt);
					finish();
				}
				else {
					setResult(RESULT_CANCELED);
					finish();
				}
				break;

			case R.id.enter_a_number_logo:
				show_help_dialog(R.string.enter_a_number_help_title,
						R.string.enter_a_number_help_msg);
				break;

			case R.id.enter_a_number_cancel_butt:
				if (m_dirty && WGlobals.g_nag) {
					show_yes_no_dialog(R.string.enter_a_number_cancel_warn_title,
						R.string.enter_a_number_cancel_warn_msg,
						new View.OnClickListener() {
							public void onClick(View v) {
								// Yes, cancel!
								setResult(RESULT_CANCELED);
								dismiss_all_dialogs();
								finish();
							}
						});
				}
				else {
					setResult(RESULT_CANCELED);
					finish();
				}
				break;

			case R.id.enter_a_number_button0:
				add_digit('0');
				break;

			case R.id.enter_a_number_button1:
				add_digit('1');
				break;

			case R.id.enter_a_number_button2:
				add_digit('2');
				break;

			case R.id.enter_a_number_button3:
				add_digit('3');
				break;

			case R.id.enter_a_number_button4:
				add_digit('4');
				break;

			case R.id.enter_a_number_button5:
				add_digit('5');
				break;

			case R.id.enter_a_number_button6:
				add_digit('6');
				break;

			case R.id.enter_a_number_button7:
				add_digit('7');
				break;

			case R.id.enter_a_number_button8:
				add_digit('8');
				break;

			case R.id.enter_a_number_button9:
				add_digit('9');
				break;

			case R.id.enter_a_number_button_dot:
				add_digit('.');
				break;

			case R.id.enter_a_number_button_clear:
				m_number_tv.setText("");
				m_dirty = true;
				m_done.setEnabled(true);
				break;

		} // switch

	} // onClick (v)


	/*******************
	 * Adds the givin digit to the m_number_tv.  That is,
	 * if there's room.
	 *
	 * preconditions:
	 * 	m_number_tv		The TextView that displays what the
	 * 					user types.  It holds either "" or
	 * 					a number string.
	 *
	 * @param digit		The digit (or decimal point) to
	 * 					add.  This is a CHAR, not an INT.
	 */
	private void add_digit (char digit) {
		if (m_number_tv.length() == MAX_NUMBER_LENGTH) {
			return;
		}

		String str = m_number_tv.getText().toString();
		char dp = getString(R.string.decimal_point).charAt(0);
		if ((digit == dp) &&
			(str.contains(getString(R.string.decimal_point)))) {
			return;		// Only one decimal point allowed
		}

		if (str.equals("0")) {
			if (digit == '0') {
				return;		// Don't compound zeros!
			}
			m_number_tv.setText("" + digit);
		}
		else {
			str = str + digit;
			m_number_tv.setText(str);
		}
		m_dirty = true;
		m_done.setEnabled(true);
	} // add_digit (digit)


}
