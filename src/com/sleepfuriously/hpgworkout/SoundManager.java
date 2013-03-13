/**
 * This class manages sounds.
 *
 * see: http://www.androidsnippets.com/play-single-or-multiple-sounds-in-any-class
 *
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;


public class SoundManager {

	private static final String tag = "SoundManager";

	/**
	 * The maximum number of sounds this class can handle.  If
	 * you don't like it, just change this number.
	 */
	public static final int MAX_SOUNDS = 4;

	/** To make sure that there's just one instance of this class */
	private static SoundManager _instance = null;

	/*
	 * The Android provided object we use to create and
	 * play sounds.
	 */
	private static SoundPool mSoundPool;

	/*
	 *  A Hashmap to store the sounds once they are loaded.
	 */
	private static HashMap<Integer, Integer> mSoundPoolMap;

	/*
	 *  A handle to the service that plays the sounds we want.
	 */
	private static AudioManager  mAudioManager;

	/*
	 *  A handle to the application Context.
	 */
	private static Context mContext;



	//------------------------
	//	Methods
	//------------------------

	/** Constructor that doesn't do anything. */
	private SoundManager() {
	}

	/**************
	 * Requests the instance of the Sound Manager and creates it
	 * if it does not exist.
	 *
	 * @return Returns the single instance of the SoundManager
	 */
	static synchronized public SoundManager getInstance()
	{
		if (_instance == null)
		_instance = new SoundManager();
		return _instance;
	}



	/**************
	 * Initialises the storage for the sounds
	 *
	 * @param context 	The Application context
	 */
	public static void initSounds(Context context) {
		mContext = context;
		mSoundPool = new SoundPool (MAX_SOUNDS,
									AudioManager.STREAM_MUSIC,
									0);
		mSoundPoolMap = new HashMap<Integer, Integer>();
		mAudioManager = (AudioManager)
					mContext.getSystemService(Context.AUDIO_SERVICE);
	} // initSound (context)


	/**************
	 * Deallocates the resources and Instance of SoundManager.
	 *
	 * Try calling during onDestroy() of the main Activity.
	 */
	public static void cleanup() {
		mSoundPool.release();
		mSoundPool = null;
		mSoundPoolMap.clear();
		mAudioManager.unloadSoundEffects();
		_instance = null;
	}

	/**************
	 * Add a new Sound to the SoundPool.
	 *
	 * @param index		A number to associate this particular
	 * 					sound with.  Remember it!
	 *
	 * @param SoundID	The resource file id for the sound file.
	 *					eg: R.raw.mysong
	 */
	public static void addSound (int index, int SoundID) {
		mSoundPoolMap.put (index,
						mSoundPool.load(mContext, SoundID, 1));
	} // addSound (index, SoundID)


	/**************
	 * Plays a sound
	 *
	 * @param index - The Index of the Sound to be played
	 */
	public static void playSound(int index) {
		// There's some weirdness, as the volume of play() requires
		// fraction of max volume (left & right channels).
		float streamVolume = (float) get_current_music_volume();
		streamVolume = streamVolume / get_max_volume();

		mSoundPool.play (mSoundPoolMap.get(index),		// see if this works...
						streamVolume, streamVolume, 1, 0, 1f);
	} // playSound

	/**************
	 * Plays a sound until stopped.
	 *
	 * @param index - The Index of the Sound to be played
	 */
	public static void playLoopedSound(int index) {
		float streamVolume = (float) get_current_music_volume();
		streamVolume = streamVolume / get_max_volume();

		mSoundPool.play (index,
						streamVolume, streamVolume, 1, -1, 1f);
	} // playLoopedSound (index)


	/*****************
	 * Stops a sound
	 *
	 * @param index - index of the sound to be stopped
	 */
	public static void stopSound(int index) {
		mSoundPool.stop(mSoundPoolMap.get(index));
	}

	/*****************
	 * Returns the current media volume of the sound system.
	 * Or -1 on error.
	 */
	public static int get_current_music_volume() {
		if (mAudioManager == null) {
			Log.e(tag, "mAudioManager is NULL when calling get_volume()! Aborting!");
			return -1;
		}
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}


	/*****************
	 * Returns the maximum volume possible for this app.
	 * Or -1 on error.
	 */
	public static int get_max_volume() {
		if (mAudioManager == null) {
			Log.e(tag, "mAudioManager is NULL when calling get_volume()! Aborting!");
			return -1;
		}
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

}
