package com.oursaviorgames.android.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for shared preferences.
 * All preferences keys are defined in this class.
 */
public class PrefUtils {

    /* Shared preferences file key */
    public static final String DEFAULT_PREF_FILE_KEY  = "default_prefs";

    ////////////////////////////////////////////////////////
    // List of App wide preference keys and default values
    ////////////////////////////////////////////////////////

    public static final String  PREF_NEW_GAME_NOTIFICATION     = "new_games_notification";
    public static final boolean PREF_NEW_GAME_NOTIFICATION_DEF = true;

    public static final String  PREF_LOOP_VIDEO     = "loop_video";
    public static final boolean PREF_LOOP_VIDEO_DEF = true;

    public static final String PREF_FEEDBACK_SAVED_EMAIL     = "feedback_email";
    public static final String PREF_FEEDBACK_SAVED_EMAIL_DEF = null;

    public static final String PREF_FEEDBACK_SAVED_MESSAGE     = "feedback_message";
    public static final String PREF_FEEDBACK_SAVED_MESSAGE_DEF = null;

    /**
     * Helper method that returns app-wide SharedPreferences.
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(DEFAULT_PREF_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(DEFAULT_PREF_FILE_KEY, Context.MODE_PRIVATE);
        return prefs.edit();
    }

    /**
     * Returns {@link SharedPreferences} local to cls SharedPreferences.
     */
    public static SharedPreferences getLocalPrefs(Context context, Class cls) {
        return context.getSharedPreferences(cls.getCanonicalName(), Context.MODE_PRIVATE);
    }

    public static boolean getLoopVideo(Context context) {
        return getSharedPrefs(context).getBoolean(PREF_LOOP_VIDEO, PREF_LOOP_VIDEO_DEF);
    }



}
