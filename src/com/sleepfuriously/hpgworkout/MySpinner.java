/**
 * This is a replacement for the Spinner class.
 *
 * Whoo boy, did this turn out complicated!
 *
 * I'm making a class that replaces the Spinner, which is
 * fairly flawed.  This new one:
 *	1. Does not use Spinners!
 *	2. Allows a no-choice option to be set.
 *	3. Allows custom choices to be made by the user (optional).
 *
 *	Usage:
 *		+	Copy This class to your src directory.  This also
 *			involves changing the package name of this file
 *			(just below these comments) to your the new package.
 *			Usually, Eclipse does this for you.
 *
 *		+	Copy the abstract class OnMySpinnerListener.java to
 *			your src directory (see below).
 *
 *		+	Create your XML layout file for your Activity.
 *			Make a nice Button where you want your MySpinner.
 *			You can put a drop-arrow graphic in your button
 *			with the following code:
 *				android:drawableRight="@android:drawable/arrow_down_float"
 *				android:gravity="left|center_vertical"
 *
 *		+	In the XML file, replace "<Button " with
 *			"[package_name].MySpinner ".  For example, the
 *			demo here uses:
 *				 "<com.sleepfuriously.spinnerreplacement.MySpinner "
 *
 *		+	In onCreate(), get the ID and set it to your
 *			instance of MySpinner.  Pretty typical.  Eg:
 *				MySpinner ms1 = (MySpinner) findViewById(R.id.myspinner1);
 *
 *		+	Set the text to display call setText().  If you
 *			don't do this, then whatever you put as the text
 *			in the XML file will be displayed.
 *
 *		+	Set the array to be shown after the widget has
 *			been touched using set_array().
 *
 *		+	Set any prompt message to be displayed at the top
 *			of the array list with set_prompt().
 *
 *		+	If you want an icon to display, call set_icon(id).
 *			To disable icons, call set_icon(-1), which is
 *			the default.
 *
 *		+	To receive events when the user selects an item
 *			implement OnMySpinnerListener in your Activity.
 *			You'll get an error reminding you to override
 *			onMySpinnerSelected().  That method is called
 *			whenever the user selects something from
 *			MySpinner, but not if they back out.
 *
 *		+	Next, you gotta let the MySpinner know that you
 *			want to get called.  So use setMySpinnerListener(this)
 *			to let the class know you have a callback method.
 *
 *		+	To see what an element of the internal array is
 *			(which is necessary as it can change at any time)
 *			call get_item(pos).
 *
 *		+	NOTE: once a choise is made, you should pro-
 *			bably let the user know about this by changing
 *			display with setText() or setTextFromPos().
 *			Also, change the selection with set_selected().
 *
 *		+	Tell which one of the list is to be selected
 *			(default is none) by called set_selected (pos).
 *
 *		+	If the user changes orientation while the list
 *			is displaying, this will destroy your Activity,
 *			leaving this dialog (the list is displayed as a
 *			dialog) hanging, which is a memory leak.
 *
 *			To fix this, in the onPause() of your Activity,
 *			dismiss the dialog defined here (the variable
 *			m_dialog is public).  Here's an example:
 *
 *			if ((MySpinner.m_dialog != null) && (MySpinner.m_dialog.isShowing())) {
 *				MySpinner.m_dialog.dismiss();
 *				MySpinner.m_dialog = null;
 *			}
 *
 *			~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *		+	If you want to allow the user to add their own
 *			item to the List, call set_user_add(pos, intent, id).
 *			The pos tells which item in the list is the "add"
 *			item.  When the user clicks on it, we go into
 *			add-new-item mode.
 *
 *		+	The intent you pass in needs to have info about
 *			the Activity to open when the user types in their
 *			custom name. Eg:
 *				itt.setClassName (getPackageName(),
 *								  MyAddActivity.class.getName());
 *			This Activity MUST be created by YOU (yes, and it
 *			probably has an xml layout file, too).  See
 *			SampleUserAddActivity.java for more.
 *
 *		+	Don't forget to put this Activity in the manifest.xml.
 *
 *		+	The id is any unique identifying number.  It's used
 *			when your onActivityResult() is called and is passed
 *			into requestCode.  This is how you'll know which
 *			widget activated the onActivityResult().  And conse-
 *			quently, which widget to use to make the activity_result()
 *			callback (explained below).
 *			Just use the widget's id--that should be fine.
 *
 *		+	That Activity needs to return with a value (a String).
 *			That is put and accessed via the key, INTENT_KEY.
 *			Eg:
 *				Intent itt = getIntent();
 *				itt.putExtra(MySpinner.INTENT_KEY, <new_item_string>);
 *				setResult(RESULT_OK, itt);
 *				finish();
 *
 *		+	Wait, there's MORE!  YOU must override in your
 *			main Activity onActivityForResult() which will
 *			receive the result when your add item Activity
 *			finishes.
 *
 *		+	And there you MUST call activity_result (idata)
 *			with the Intent data received in that callback.
 *			The MySpinner class will use that data to build
 *			the list appropriately.
 *
 *		!-	When the user clicks on the "add" item, an Activity
 *			fires, letting them create a new item in the list.
 *			If the user cancels the Activity, then they return
 *			to the select dialog.
 *
 *		!!-	If they say accept the change, the Activity closes
 *			and the dialog closes, firing off an
 *			onMySpinnerSelcted() event.
 *
 *		+	Currently, only one user created item is allowed.
 *			If the user presses on their item during another
 *			round, it will exit normally.  But if they choose
 *			the selection item ("add"), a dialog will come up and
 *			their old item will be replaced with the new one.
 *
 *		+	If you want to add another item to the array (at
 *			the end of the array), call add_to_array().
 *
 *		+	To insert at a location, call insert_to_array(loc).
 *			It'll be added AFTER the specified array index.
 *
 *		+	To remove an item, call remove_from_array (loc).
 *
 *		+	To clear the whole array, call clear_array().
 *
 *		+	The length of the items in the widget's array can
 *			be found through length().
 *
 *		+	You can get a copy of the entire array of strings
 *			with get_array().
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MySpinner extends Button
					implements OnClickListener {

	//-----------------------------------
	//	Constants
	//-----------------------------------

	private static final String tag = "MySpinner";


	/**
	 * This is the key to index the return value for the
	 * Activity that's called for the user addition to
	 * the list.  Whew, what a nasty sentence!
	 */
	public static final String INTENT_KEY = "myspinner_key";


	//-----------------------------------
	//	Data
	//-----------------------------------

	/** The Context that this was called */
	private Context m_context;

	private String m_prompt;

	/**
	 * The Adapter that holds the items that the user
	 * selects from the spinner.
	 */
	private ArrayAdapter<CharSequence> m_aa;

	/**
	 * This is used to create the callback so the programmer
	 * will know what the user selected.
	 */
	private OnMySpinnerListener m_listener;


	/**
	 * The item in the list that is currently selected.
	 * -1 means none.
	 */
	private int m_selection = -1;

	/**
	 * When non-negative, the user is able to to add
	 * a line in our list.  And the array element that
	 * the user selects to add is this position.  The
	 * element that the user actually added is this + 1.
	 */
	private int m_user_add_pos = -1;

	/**
	 * When the user adds their own item, this is the
	 * prompt/title of the dialog.
	 */
	private Intent m_user_add_intent = null;

	/**
	 * This uniquely identifies this widget
	 */
	private int m_activity_id;

	/**
	 * This is true iff the user can and has added a line
	 * to our array of selectable items.
	 */
	private boolean m_user_added = false;

	/**
	 * The icon to show with the list.  -1 means no icon.
	 */
	private int m_icon_id = -1;

	/**
	 * The Dialog for any help messages or whatever.  Having
	 * it as a class variable allows me to dismiss it during
	 * an orientation change.
	 *
	 * Make sure that you dismiss this in your Activity's
	 * onPause() so that there are no memory leaks.  See
	 * above for sample code.
	 */
	public static AlertDialog m_dialog = null;

	//-----------------------------------
	//	Constructors
	//-----------------------------------

	/********************
	 * Constructor
	 *
	 * @param context	The context from the Activity to
	 * 					to show this "spinner."
	 */
	public MySpinner(Context context) {
		super(context);
		init (context);
	} // constructor

	/*********************
	 * Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MySpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init (context);
	}

	/*********************
	 * Constructor
	 *
	 * @param context
	 * @param attrs
	 */
	public MySpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init (context);
	}

	/********************
	 * Does the initialization.
	 *
	 * @param context	Context of the calling Activity.
	 * 					Used later.
	 */
	private void init (Context context) {
		m_context = context;
		setOnClickListener(this);
	}


	//-----------------------------------
	//	Getters
	//-----------------------------------

	/*********************
	 * @return	The number of items in the array.
	 */
	@Override
	public int length() {
		return m_aa.getCount();
	}

	/*********************
	 * Returns the item in the array at the specified position.
	 *
	 * @param pos	Gotta be in range!  [0..length)
	 *
	 * @return	The String at that location.
	 */
	public String get_item (int pos) {
		if ((pos < 0) || (pos >= length())) {
			Toast.makeText(m_context, "Illegal range in MySpinner.get_item()!", Toast.LENGTH_LONG).show();
			Log.e(tag, "Illegal range in MySpinner.get_item()!   pos = " + pos);

			// And let the exception happen.
		}
		return m_aa.getItem(pos).toString();
	}

	/*********************
	 * Attempts to find the given string in our list.  Don't
	 * know if this is case sensitive or not (documentation
	 * sucks).  It probably is.
	 *
	 * @param str	The string to find
	 *
	 * @return	The position in the array [0..length) that this
	 * 			string can be found.
	 * 			-1 if it cannot be found.
	 */
	public int get_pos (String str) {
		return m_aa.getPosition(str);
	}

	/*********************
	 * @return	The position in the list that is currently
	 * 			selected or -1 if nothing is selected.
	 */
	public int get_current_selection() {
		return m_selection;
	}

	/********************
	 * @return	Returns an array of all the strings in the list.
	 */
	public String[] get_array_str() {
		String[] array = new String[m_aa.getCount()];

		for (int i = 0; i < m_aa.getCount(); i++) {
			array[i] = m_aa.getItem(i).toString();
		}
		return array;
	} // get_array_str()

	/********************
	 * Same as above, but returns a CharSequence[]
	 */
	public CharSequence[] get_array_chsq() {
		CharSequence[] array = new CharSequence[m_aa.getCount()];

		for (int i = 0; i < m_aa.getCount(); i++) {
			array[i] = m_aa.getItem(i);
		}
		return array;
	} // get_array_chsq()


	//-----------------------------------
	//	Setters
	//-----------------------------------

	/*******************
	 * This is used to attach the listening code to
	 * this class.
	 *
	 * @param listener	The class that wants to listen.
	 * 					It should have implmented the
	 * 					OnMySpinnerListener interface.
	 */
	public void setMySpinnerListener (OnMySpinnerListener listener) {
		m_listener = listener;
	}

	/*********************
	 * Changes the text on the button.  This is a helper to
	 * simplify a common task: The user selects something and
	 * you want to change the button text to that selection.
	 * In this case, just send in the position they selected.
	 *
	 * Reminder: You should probably also change the item
	 * 	that's automatically selected, too with set_selected().
	 *
	 * @param pos	The position of the List that you want
	 * 				to now display as the button text.  Of
	 * 				course it should be in range: [0..length).
	 */
	public void setTextFromPos (int pos) {
		setText(m_aa.getItem(pos));
	}

	/*********************
	 * Call this to set the prompt for the spinner dialog.
	 * It displays at the top above the list.
	 *
	 * @param res_id		ID for the prompt.  Use -1 to have
	 * 					no prompt.
	 */
	public void set_prompt (int res_id) {
		if (res_id == -1) {
			m_prompt = null;
		}
		else {
			m_prompt = getResources().getString(res_id);
		}
	} // set_prompt (res_id)

	/*********************
	 * Same as above, but allows a String directly to
	 * be used.
	 *
	 * @param prompt		String to use as prompt.  Send
	 * 					in null to disable.
	 */
	public void set_prompt (String prompt) {
		m_prompt = prompt;
	}

	/*********************
	 * Call the provide an icon for the selection dialog.
	 *
	 * @param res_id		The id (R.drawable.my_icon).  Or
	 * 					use -1 to indicate none.
	 */
	public void set_icon (int res_id) {
		m_icon_id = res_id;
	}

	/*********************
	 * Sets the array that denotes the options for the
	 * spinner.
	 *
	 * @param res_id		The ID of the string array.
	 *
	 * @return	true		IFF everything worked okay.
	 */
	public boolean set_array (int res_id) {

		// Hopefully, this will allow additions.
		List<CharSequence> list = new ArrayList<CharSequence>();
		CharSequence[] str_array = getResources().getTextArray(res_id);
		for (CharSequence str : str_array) {
			list.add(str);
		}

		// Trying it...
		m_aa = new ArrayAdapter<CharSequence>
				(m_context,
				android.R.layout.simple_spinner_dropdown_item,
				list);

		return true;
	} // set_array (res_id)


	/*********************
	 * Like the above, but instead of loading from a
	 * resource, it loads a pre-made list into our
	 * spinner.
	 *
	 * @param list	A List of Strings to put in the spinner.
	 *
	 * @return	true IFF we're a-okay.
	 */
	public boolean set_array (ArrayList<CharSequence> list) {
		m_aa = new ArrayAdapter<CharSequence>
				(m_context,
				android.R.layout.simple_spinner_dropdown_item,
				list);
		return true;
	} // set_array (list)


	/*********************
	 * When the user hits the button and the array list pops
	 * up, you might like to have something already selected.
	 * Use this to set it.
	 *
	 * @param pos	The array position to select.  Use -1
	 * 				to indicate no selection.
	 */
	public void set_selected (int pos) {
		m_selection = pos;
	}

	//-----------------------------------
	//	Other
	//-----------------------------------

	/*********************
	 * Adds the given String to the end of the array.
	 *
	 * @param res_id		The ID of the string.
	 */
	public void add_to_array (int res_id) {
		m_aa.add(getResources().getString(res_id));
	} // add_to_array (res_id)

	/*********************
	 * Same as above, but with a charsequence.
	 */
	public void add_to_array (CharSequence charseq) {
		m_aa.add(charseq);
	}

	/*********************
	 * Like add_to_array, but puts it in a specific location.
	 *
	 * @param res_id		The ID of the string to insert.
	 *
	 * @param pos		The location to put this item.
	 * 					Everything at this position and beyond
	 * 					will be incremented by one.
	 * 					NOTE:
	 * 						gotta be in range [0..length)!
	 */
	public void insert_to_array (int res_id, int pos) {
		m_aa.insert(getResources().getString(res_id), pos);
	} // insert_to_array (res_id)

	public void insert_to_array (CharSequence str, int pos) {
		m_aa.insert(str, pos);
	}


	/********************
	 * Removes the specified item from the list.  All items
	 * with a higher position have their positions decremented.
	 *
	 * @param pos	The position of the item to delete.
	 * 				Must be in range [0..length).
	 */
	public void remove_item (int pos) {
		CharSequence str = m_aa.getItem(pos);
		m_aa.remove(str);
	}

	/********************
	 * Like the above, but finds and removes the first occurance
	 * of the given string.  Yes, this IS case sensitive!
	 *
	 * @param str		The string to remove.
	 */
	public void remove_item (CharSequence str) {
		m_aa.remove(str);
	}

	/*********************
	 * Clears the array.  Good for the tabula-rasa sort of
	 * thing.
	 */
	public void clear_array() {
		m_aa.clear();
	}


	//-----------------------------------
	//	For User-Added Custom Line
	//-----------------------------------


	/*********************
	 * Call this to set things up so the user can add a
	 * line to our array.  See the explanation at the top
	 * for details about how it works.
	 *
	 * NOTE:
	 *	If the user position is already set, then calling this
	 *	again resets the active position AND clears any entry
	 *	that the user made.
	 *
	 * @param pos	The array position that the user selects
	 * 				to add their item (usually length - 1).
	 * 				The position of the user's addition will
	 * 				be pos + 1.
	 *
	 * @param intent		The intent to launch for the user
	 * 					to add their own line.  Remember to
	 * 					declare it in the manifest!
	 *
	 * @param id		Some sort of way to identify this widget.
	 * 				It's used later when onActivityResult()
	 * 				is called (it's put in the requestCode).
	 * 				I recommend simply using this widget's
	 * 				id.
	 */
	public void set_user_add (int pos, Intent intent, int id) {
		if (can_user_add()) {
			// Reset
			stop_user_add();
		}

		m_user_add_pos = pos;
		m_user_add_intent = intent;
		m_activity_id = id;
	} // user_add (pos)

	/*********************
	 * Call this method to cease allowing the user to
	 * add lines to the array of selection items.
	 *
	 * Side Effects:
	 * 		Any lines added by the user will be removed.
	 * 		This essentially resets this class.
	 *
	 * Notes:
	 * 		You should probably manually remove the line
	 * 		in the list that indicates to the user that
	 * 		they can add an element.
	 *
	 * @return	-1	if the system already did not allow
	 * 				users to add (ie, this did nothing).
	 * 			[0..length)	to indicate the list element
	 * 				that the user would have clicked on
	 * 				to add a line IF they still could.
	 * 				(So it's a good idea to remove this
	 * 				element.)
	 */
	public int stop_user_add() {
		int old_user_add_pos = m_user_add_pos;

		if (can_user_add() == false) {
			return -1;		// Nothing to do!
		}

		if (get_custom_added()) {
			// Remove their item.
			remove_item(old_user_add_pos + 1);
		}
		m_user_added = false;
		m_user_add_pos = -1;
		m_user_add_intent = null;
		return old_user_add_pos;
	} // stop_user_add()


	/*********************
	 * This tells if the user has added
	 * his/her own custom item to the list.
	 *
	 * @return	false	User has not added anything OR
	 * 					can_user_add() is false.
	 *
	 * 			true		Yes, the user has added an item
	 * 					to the list.  Its position is
	 * 					get_custom_item_pos().
	 */
	public boolean get_custom_added() {
		return m_user_added;
	} // did_user_add()


	/*********************
	 * @return	The position of the item that a user
	 * 			has added to our list.
	 * 			WARNING:
	 * 				This number is not valid if the
	 * 				user has not entered anything!
	 * 				Call get_user_added() first.
	 */
	public int get_custom_item_pos() {
		return m_user_add_pos + 1;
	}

	/*********************
	 * Saves the caller from having to type a bunch of
	 * things here.  This returns whatever custom string
	 * the user added to our list.
	 *
	 * WARNING:
	 * 		This'll crash hard if the user hasn't
	 * 		added a custom item!
	 *
	 * @return	The string the user added.
	 */
	public String get_custom_item() {
		return get_item(get_custom_item_pos());
	}

	/*********************
	 * @return	The position of the item in the list that
	 * 			the user touches to add their own custom item.
	 */
	public int get_user_add_pos() {
		return m_user_add_pos;
	}


	/*********************
	 * @return	Whether or not the list currently supports
	 * 			user adding.
	 */
	public boolean can_user_add() {
		return m_user_add_pos >= 0 ? true : false;
	}

	/*********************
	 * This MUST be called by the Activity that's using
	 * this widget from its onActivityResult() method.
	 * The information here is needed by this class to
	 * properly maintain the list.
	 *
	 * If you are NOT allowing the user to add lines to
	 * the selection list, don't worry about this.
	 *
	 * @param result_code	The result code that was passed
	 * 						to your onActivityResult() method.
	 *
	 * @param itt	The Intent passed to you in your
	 * 				onActivityResult() method.
	 */
	public void activity_result (int result_code, Intent itt) {

		if (result_code == Activity.RESULT_CANCELED) {
			Log.i(tag, "activity_result() sees that it was cancelled.");
			return;		// Don't do anything
		}

		if (can_user_add() == false)
			return;		// Shouldn't have been called.

		CharSequence str = itt.getCharSequenceExtra(INTENT_KEY);

		// Are we adding a new one or replacing an existing
		// user addition?
		m_selection = m_user_add_pos + 1;
		if (m_user_added) {
			// Take out the current one.
			remove_item (m_selection);
		}
		insert_to_array (str, m_selection);
		m_user_added = true;

		m_listener.onMySpinnerSelected(MySpinner.this, m_selection, true);

	} // activity_result (itt)


	/*********************
	 * Override the onClick() and do what needs to be
	 * done.
	 *
	 * @param	v		This.  Not used.
	 */
	@Override
	public void onClick(View v) {
		WGlobals.button_click();

		// Build the dialog...
		AlertDialog.Builder builder =
			new AlertDialog.Builder(m_context);

		// TODO:
		//	Make a custom layout for this, using:
//		builder.setCustomTitle(some_view);
//		builder.setContentView (some_view);
		//	see:  http://developer.android.com/guide/topics/ui/dialogs.html#CustomDialog

		// Title
		if (m_prompt != null)
			builder.setTitle(m_prompt);
		if (m_icon_id != -1)
			builder.setIcon(m_icon_id);

		// The items...
//		MyDialogClickListener dialog = new MyDialogClickListener();
//		builder.setSingleChoiceItems(m_aa, m_selection, dialog);
		builder.setSingleChoiceItems(m_aa, m_selection, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WGlobals.button_click();

				if (which == m_user_add_pos) {
					// Launch the specified Activity and get
					// its result.
					((Activity) m_context)
						.startActivityForResult (m_user_add_intent,
												m_activity_id);
					dialog.dismiss();
				}
				else {
					dialog.dismiss();
					m_listener.onMySpinnerSelected(MySpinner.this, which, false);
				}
			}
		});

		m_dialog = builder.create();
		
		// Set the volume buttons to control the volume of our sounds
		// (music).
		m_dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);


		m_dialog.show();

	} // onClick (v)

} // MySpinner
