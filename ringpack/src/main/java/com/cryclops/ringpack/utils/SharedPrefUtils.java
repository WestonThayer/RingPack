package com.cryclops.ringpack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Helpers for dealing with SharedPreferences.
 */
public class SharedPrefUtils {

    /**
     * Identifier for an int that indicates what tone rotation mode the app is in.
     */
    private static final String ROTATION_MODE = "RotationMode";

    /**
     * Normal, linear tone rotation.
     */
    public static final int MODE_NORMAL = 0;
    /**
     * Rotation to any tone in the pack except for the current tone.
     */
    public static final int MODE_SHUFFLE = 1;
    /**
     * Do not allow rotation.
     */
    public static final int MODE_LOCKED = 2;

    /**
     * Key for String URI value of the Default Notification Ringtone before we started
     * meddling about.
     */
    private static final String RINGTONE_RESTORE_URI = "RingtoneRestoreUri";
    /**
     * Key for a boolean value of whether the SMS BroadcastReceiver should work or not. It should
     * not work if the AccessibilityService or the NotificationListenerService is running.
     */
    private static final String IS_SMS_ROTATION_ON = "IsSmsRotationOn";
    /**
     * Key for an integer value of what the app version code is.
     */
    private static final String PREVIOUS_VERSION = "PreviousVersion";

    /**
     * Getter for the default SharedPreferences.
     * @return
     */
    public static SharedPreferences getSharedPrefs(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        return prefs;
    }

    /**
     * Getter for the URI of the original Default Notification Ringtone we're saving.
     * @param ctx
     * @return
     */
    public static Uri getRingtoneRestoreUri(Context ctx) {
        SharedPreferences prefs = getSharedPrefs(ctx);
        String u = prefs.getString(RINGTONE_RESTORE_URI, null);

        if (u == null) {
            return null;
        }

        return Uri.parse(u);
    }

    /**
     * Set the URI of the original Default Notification Ringtone to save.
     * @param ctx
     * @param uri
     */
    public static void setRingtoneRestoreUri(Context ctx, Uri uri) {
        SharedPreferences.Editor editor = getSharedPrefs(ctx).edit();

        if (uri != null) {
            editor.putString(RINGTONE_RESTORE_URI, uri.toString());
        }
        else {
            editor.remove(RINGTONE_RESTORE_URI);
        }

        editor.commit();
    }

    /**
     * Getter for the current app-wide tone rotation mode.
     * @param ctx
     * @return
     */
    public static int getRotationMode(Context ctx) {
        SharedPreferences prefs = getSharedPrefs(ctx);
        String value = prefs.getString(ROTATION_MODE, Integer.toString(MODE_NORMAL));
        return Integer.parseInt(value);
    }

    /**
     * Getter for whether RingPack should listen for SMS received events.
     * @param ctx
     * @return
     */
    public static boolean getIsSmsRotationOn(Context ctx) {
        SharedPreferences prefs = getSharedPrefs(ctx);
        return prefs.getBoolean(IS_SMS_ROTATION_ON, true);
    }

    /**
     * Set whether RingPack should listen for SMS received events.
     * @param ctx
     * @param value
     */
    public static void setIsSmsRotationOn(Context ctx, boolean value) {
        SharedPreferences.Editor editor = getSharedPrefs(ctx).edit();
        editor.putBoolean(IS_SMS_ROTATION_ON, value);
        editor.commit();
    }

    /**
     * Getter for the app version.
     * @param ctx
     * @return
     */
    public static int getPreviousVersion(Context ctx) {
        SharedPreferences prefs = getSharedPrefs(ctx);
        return prefs.getInt(PREVIOUS_VERSION, 0);
    }

    /**
     * Set the app version.
     * @param ctx
     * @param value
     */
    public static void setPreviousVersion(Context ctx, int value) {
        SharedPreferences.Editor editor = getSharedPrefs(ctx).edit();
        editor.putInt(PREVIOUS_VERSION, value);
        editor.commit();
    }
}
