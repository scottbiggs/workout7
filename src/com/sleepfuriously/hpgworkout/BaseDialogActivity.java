/**
 * This is a replacement for the regular Activity class that
 * all the screens normally inherit from.
 *
 * Why?  What's wrong with the regular Activity?
 *
 * Dialogs.  Regular Activities can't handle dialogs well
 * during an orientation change.  That's all.  And while I'm
 * at it, I'm adding a few other goodies that I find useful
 * in Activities.
 *
 * CAVEATS:
 * 		- IF your inherited Activity needs to use onPause(),
 * 		  you MUST call super.onPause() at some time!!!! This
 * 		  allows the dialogs to be dismissed properly (which
 * 		  is the whole point of this class).
 *
 * Added:
 * 		An m_db variable.  This cleans up automatically
 * 		when the Activity is destroyed.
 *
 * 		NOTE:	YOU MUST instantiate it yourself in the
 * 				onCreate() or onResume() methods.
 *
 * MORE!
 * 		Now this does custom Toasts!  Use it similarly
 * 		to the regular Toast, except that you now don't
 * 		have to call show()!  Weeeeee!
 */
package com.sleepfuriously.hpgworkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BaseDialogActivity extends Activity {

	//----------------------
	//	Class Variables
	//----------------------

	private static final String tag = "BaseDialogActivity";

	/**
	 * The Dialog for any help messages or whatever.  Having
	 * it as a class variable allows me to dismiss it during
	 * an orientation change.
	 */
	private AlertDialog m_dialog = null;

	/**
	 * The dialog for all the custom built dialogs (not
	 * AlertDialogs).  Having it as a class member allows
	 * me to dismiss during an orientation change.
	 */
	private Dialog m_custom_dialog = null;

	/** Special for progress Dialogs */
	private ProgressDialog m_prog_dialog = null;


	/** Actually accesses the database */
	protected SQLiteDatabase m_db = null;


	//----------------------
	//	Doesn't do much, but it IS important that all Activities
	//	set the preferences correctly.  So might as well do
	//	it here.
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		WGlobals.tryFullScreen(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Do the preferences.
		WGlobals.load_prefs(this);
		WGlobals.act_on_prefs (this);
	} // onCreate(.)


	//----------------------
	@Override
	protected void onPause() {
		dismiss_all_dialogs();
		super.onPause();
	} // onPause()


	//----------------------
	//	Make sure that the database is closed.  It should be;
	//	this is a "just in case" scenario.
	//
	@Override
	protected void onDestroy() {
		if (m_db != null) {
			m_db.close();
			m_db = null;
		}
		super.onDestroy();
	}


	//--------------------------------------
	//	Dialog Methods
	//--------------------------------------

	/*******************
	 * Dismisses all open dialogs.
	 */
	protected void dismiss_all_dialogs() {
		if ((m_dialog != null) && (m_dialog.isShowing())) {
			m_dialog.dismiss();
			m_dialog = null;
		}
		if (m_prog_dialog != null) {
			m_prog_dialog.dismiss();
			m_prog_dialog = null;
		}

		if ((m_custom_dialog != null) && (m_custom_dialog.isShowing())) {
			m_custom_dialog.dismiss();
			m_custom_dialog = null;
		}

	} // dismiss_all_dialogs()


	/*******************
	 * Displays a custom help dialog with an "okay" button.
	 *
	 * side effects:
	 * 		m_dialog		I use a class member to hold this dialog
	 * 					so that it can be properly dismissed
	 * 					during an orientation change in onPause().
	 *
	 * @param	title_id		The resource ID of the title.  Use
	 * 						-1 for no title.
	 *
	 * @param	msg_id		The ID of the message.  Again, use
	 * 						-1 for no message.
	 *
	 */
	protected void show_help_dialog (int title_id,
									int msg_id) {
		// Start a new dialog.
		m_custom_dialog = new Dialog(this);

		// This prevents the automatic title area from being made.
		// And it has to be the first thing, too.
		m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_custom_dialog.setContentView(R.layout.dialog_help);

		// Fill in the Views (title & msg).
		TextView title = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_title_tv);
		if (title_id == -1)
			title.setText("");
		else
			title.setText(title_id);

		TextView msg = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_msg_tv);
		if (msg_id == -1)
			msg.setText("");
		else
			msg.setText(msg_id);

		Button ok_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_help_ok_butt);
		ok_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_custom_dialog.dismiss();
			}
		});
		m_custom_dialog.show();
	} // show_help_dialog (title_id, msg_id)

	/*******************
	 * String version of show_custom_help_dialog().
	 *
	 * Note:  Supply null to not use that param.
	 */
	protected void show_help_dialog (String title, String msg) {
		// Start a new dialog.
		m_custom_dialog = new Dialog(this);

		// This prevents the automatic title area from being made.
		// And it has to be the first thing, too.
		m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_custom_dialog.setContentView(R.layout.dialog_help);

		// Fill in the Views (title & msg).
		TextView title_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_title_tv);
		title_tv.setText(title);
		TextView msg_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_msg_tv);
		msg_tv.setText(msg);

		Button ok_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_help_ok_butt);
		ok_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_custom_dialog.dismiss();
			}
		});
		m_custom_dialog.show();
	} // show_help_dialog (title, msg)


	/*******************
	 * Shows a custom dialog.  This version allows arguments
	 * to be in the title and message.  Unfortunately, all
	 * the arguments must be Strings (no ints, booleans, etc.).
	 *
	 * @param title_id		The string for the title.  Use -1 for
	 * 						no title.
	 *
	 * @param title_args		Array of arguments to be used with the
	 * 						title string. It's a lot like the C
	 * 						printf statement.  Sorry, only allows
	 * 						String (%s) args.
	 *
	 * @param msg_id			String for the message. -1 = no msg.
	 *
	 * @param msg_ags		Array of arguments for the message.
	 */
	protected void show_help_dialog (int title_id, String[] title_args,
									int msg_id, String[] msg_args) {
		m_custom_dialog = new Dialog (this);
		m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_custom_dialog.setContentView(R.layout.dialog_help);

		TextView title_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_title_tv);
		if (title_id == -1)
			title_tv.setText(null);
		else {
			String title_str = getString(title_id, (Object[])title_args);
			title_tv.setText(title_str);
		}

		TextView msg_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_help_msg_tv);
		if (msg_id == -1)
			msg_tv.setText(null);
		else {
			String msg_str = getString(msg_id, (Object[])msg_args);
			msg_tv.setText(msg_str);
		}

		Button ok_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_help_ok_butt);
		ok_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_custom_dialog.dismiss();
			}
		});
		m_custom_dialog.show();
	} // show_help_dialog (title_id, title_args, msg_id, msg_args)


	/******************
	 * This shows a yes/no dialog, prompting the user to choose
	 * one.  If they choose yes, then the listener is activated.
	 * If they choose no, then nothing is done.
	 *
	 * NOTE:		The title AND the message should be in the
	 * 			form of yes or no question.
	 *
	 * @param title_id		The ID for the title.  Use -1 for
	 * 						no title.
	 *
	 * @param msg_id			ID for the message. -1 for none.
	 *
	 * @param listener		The listener to activate when YES
	 * 						is chosen.
	 * 					NOTE:
	 * 						This listener MUST call dismiss_all_dialogs()
	 * 						if you want the dialog to close!!!
	 */
	protected void show_yes_no_dialog (int title_id, int msg_id,
							View.OnClickListener listener) {

		// Build the dialog.
		m_custom_dialog = new Dialog (this);
		m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_custom_dialog.setContentView(R.layout.dialog_yes_no);

		TextView title_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_title_tv);
		if (title_id == -1)
			title_tv.setText("");
		else
			title_tv.setText(title_id);

		TextView msg_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_msg_tv);
		if (msg_id == -1)
			msg_tv.setText("");
		else
			msg_tv.setText(msg_id);

		Button no_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_negative_butt);
		no_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_custom_dialog.dismiss();
			}
		});

		Button yes_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_affirmative_butt);
		yes_butt.setOnClickListener(listener);

		m_custom_dialog.show();
	} // show_yes_no_dialog (title_id, msg_id, listener)

	/******************
	 * This shows a yes/no dialog, prompting the user to choose
	 * one.  If they choose yes, then the listener is activated.
	 * If they choose no, then the dialog is closed and nothing
	 * else is done.
	 *
	 * NOTE:		When YES is selected, it's up to the listener
	 * 			to dismiss the dialog with dismiss_all_dialogs().
	 *
	 * NOTE:		The title AND the message should be in the
	 * 			form of yes or no question.
	 *
	 * @param title			String for title.  Use null for
	 * 						no title.
	 *
	 * @param msg			String for message.  Null for none.
	 *
	 * @param listener		The listener to activate when YES
	 * 						is chosen.
	 */
	protected void show_yes_no_dialog (String title, String msg,
									View.OnClickListener listener) {
				// Build the dialog.
				m_custom_dialog = new Dialog (this);
				m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				m_custom_dialog.setContentView(R.layout.dialog_yes_no);

				TextView title_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_title_tv);
				title_tv.setText(title);

				TextView msg_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_msg_tv);
				msg_tv.setText(msg);

				Button no_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_negative_butt);
				no_butt.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						m_custom_dialog.dismiss();
					}
				});

				Button yes_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_affirmative_butt);
				yes_butt.setOnClickListener(listener);

				m_custom_dialog.show();
			} // show_yes_no_dialog (title_id, msg_id, listener)


	/******************
	 * This shows a yes/no dialog, prompting the user to choose
	 * one.  If they choose yes, then the listener is activated.
	 * If they choose no, then nothing is done.
	 *
	 * NOTE:		The title AND the message should be in the
	 * 			form of yes or no question.
	 *
	 * @param title_id		The string for the title.  Use -1 for
	 * 						no title.
	 *
	 * @param title_args		Array of arguments to be used with the
	 * 						title string. It's a lot like the C
	 * 						printf statement.  Sorry, only allows
	 * 						String (%s) args.
	 *
	 * @param msg_id			String for the message.
	 *
	 * @param msg_ags		Array of arguments for the message.
	 *
	 * @param listener		The listener to activate when YES
	 * 						is chosen.
	 * 					NOTE:
	 * 						This listener MUST call dismiss_all_dialogs()
	 * 						if you want the dialog to close!!!
	 */
	protected void show_yes_no_dialog (int title_id, String[] title_args,
									int msg_id, String[] msg_args,
									View.OnClickListener listener) {

		// Build the dialog.
		m_custom_dialog = new Dialog (this);
		m_custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_custom_dialog.setContentView(R.layout.dialog_yes_no);

		TextView title_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_title_tv);
		if (title_id == -1)
			title_tv.setText("");
		else {
			String title = getString(title_id, (Object[])title_args);
			title_tv.setText(title);
		}

		TextView msg_tv = (TextView) m_custom_dialog.findViewById(R.id.dialog_yes_no_msg_tv);
		if (msg_id == -1)
			msg_tv.setText("");
		else {
			String msg = getString(msg_id, (Object[])msg_args);
			msg_tv.setText(msg);
		}

		Button no_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_negative_butt);
		no_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_custom_dialog.dismiss();
			}
		});

		Button yes_butt = (Button) m_custom_dialog.findViewById(R.id.dialog_yes_no_affirmative_butt);
		yes_butt.setOnClickListener(listener);

		m_custom_dialog.show();
	} // show_yes_no_dialog (title_id, title_args, msg_id, msg_args, listener)


	/********************
	 * Call this to dismiss the progress dialog.  PLEASE!
	 * You most likely won't be able to get rid of
	 * it otherwise.
	 */
	public void stop_progress_dialog() {
		if (m_prog_dialog != null) {
			m_prog_dialog.dismiss();
			m_prog_dialog = null;
		}
		else {
			Log.e(tag, "Tried to stop a dialog that was already null!");
		}
	} // stop_progress_dialog()


	/*********************
	 * Starts a progress dialog that continues until it
	 * is specifically stopped via stop_progress_dialog().
	 *
	 * This version has no string, just the animation.
	 */
	public void start_progress_dialog() {
		start_progress_dialog(null);
	}

	/*********************
	 * Starts a progress dialog that continues until it
	 * is specifically stopped via stop_progress_dialog().
	 *
	 * @param res_id		The ID of the string.
	 */
	public void start_progress_dialog (int res_id) {
		start_progress_dialog(getString(res_id));
	}

	/*********************
	 * Starts a progress dialog that continues until it
	 * is specifically stopped via stop_progress_dialog().
	 *
	 * @param	str		The string to display next to the
	 * 					load animation.  Use null for no string.
	 */
	public void start_progress_dialog (String str) {
		m_prog_dialog = new ProgressDialog(this);
		m_prog_dialog.setMessage(getString(R.string.loading_str));
		m_prog_dialog.setIndeterminate(true);
		m_prog_dialog.setCancelable(false);
		m_prog_dialog.show();
	} // start_progress_dialog()


	//--------------------------------------
	//	Custom Toasts
	//--------------------------------------

	/************************
	 * Makes a custom Toast and shows it.  Just like
	 * the standard one, but even easier as you don't
	 * have to call show().
	 *
	 * todo:
	 * 		Add sound effects (if sound is turned on)
	 *
	 * @param ctx	The Context of the Activity (use
	 * 				'this' or getApplicationContext()
	 * 				or whatever).
	 *
	 * @param msg	The STRING of the message you want
	 * 				to Toast!
	 */
	void my_toast (Context ctx, String msg) {
		// Make the layout.
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast2,
				(ViewGroup) findViewById(R.id.toast2_layout_root));
		TextView toast_tv = (TextView) layout.findViewById(R.id.toast2_tv);
		toast_tv.setText(msg);

		// Setup the toast and show it.
		Toast toast = new Toast(ctx);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, -40);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);	// Connects this Toast to the inflated View
		toast.show();
	}

	/**************************
	 * Same as the other toast, except that this takes
	 * a Res ID instead of a string.
	 *
	 * @param ctx	The Context of the Activity (use
	 * 				'this' or getApplicationContext()
	 * 				or whatever).
	 *
	 * @param res_id		ID of a String to display.
	 */
	void my_toast (Context ctx, int res_id) {
		String str = ctx.getString(res_id);
		my_toast (ctx, str);
	}

	/**************************
	 * Same as the other toast, except that this takes
	 * a Res ID with arguments.
	 *
	 * @param ctx	The Context of the Activity (use
	 * 				'this' or getApplicationContext()
	 * 				or whatever).
	 *
	 * @param res_id		ID of a String to display.
	 *
	 * @param args		Arguments to fill in the variables
	 * 					in the res_id's string.
	 */
	void my_toast (Context ctx, int res_id, String args[]) {
		String msg = getString(res_id, (Object[])args);
		my_toast(ctx, msg);
	}


	/********************
	 * This is a replacement for the regular setResult().
	 * Unlike the regular one, this should work for Activities
	 * that reside within a parent Activity, like a TabHost.
	 *
	 * @param result_code	The result code to pass along
	 * 						when terminating.
	 */
	public void tabbed_set_result (int result_code) {
		if (getParent() == null) {
			setResult(result_code);
		} else {
			getParent().setResult(result_code);
		}
	} // my_set_result (result_code)

	/*********************
	 * This is a replacement for the regular setResult().
	 * Unlike the regular one, this should work for Activities
	 * that reside within a parent Activity, like a TabHost.
	 *
	 * @param result_code	The result code to pass along
	 * 						when terminating.
	 *
	 * @param itt			The Intent to set with the result.
	 */
	public void tabbed_set_result (int result_code, Intent itt) {
		if (getParent() == null) {
			setResult(result_code, itt);
		} else {
			getParent().setResult(result_code, itt);
		}
	}



}
