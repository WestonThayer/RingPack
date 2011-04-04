package com.cryclops.ringpack;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * The Utilities class helps with shared tasks as well as remembering state
 * information so that we may restore at any time.
 * 
 * @author Cryclops
 * @version 2.1.0
 *
 */
public class Utilities {

	public static final String SAVED_URI = "SAVED_URI_PREF";
	public static final String SAVED_PACK_ID = "SAVED_PACK_ID_PREF";
	public static final String SAVED_TONE_INDEX = "SAVED_TONE_INDEX_PREF";
	public static final String SAVED_PHONE_URI = "SAVED_PHONE_URI_PREF";
	public static final String ENABLED = "ENABLED_PREF";
	
	private static Uri savedUri;
	private static boolean enabled;
	private static int savedPackId, savedToneIndex;
	
	private static String life = null;
	
	static int getSavedToneIndex(Context ctx) {
		testLife(ctx);
		return savedToneIndex;
	}
	
	static void setSavedToneIndex(Context ctx, int id) {
		SharedPreferences.Editor editor = getPrefs(ctx).edit();
		editor.putInt(SAVED_TONE_INDEX, id);
		editor.commit();
		
		savedToneIndex = id;
	}
	
	static int getSavedPackId(Context ctx) {
		testLife(ctx);
		return savedPackId;
	}
	
	static void setSavedPackId(Context ctx, int id) {
		SharedPreferences.Editor editor = getPrefs(ctx).edit();
		editor.putInt(SAVED_PACK_ID, id);
		editor.commit();
		
		savedPackId = id;
	}
	
	static Uri getSavedUri(Context ctx) {
		testLife(ctx);
		return savedUri;
	}
	
	static void setSavedUri(Context ctx, Uri uri) {
		SharedPreferences.Editor editor = getPrefs(ctx).edit();
		editor.putString(SAVED_URI, uri.toString());
		editor.commit();
		
		savedUri = uri;
	}
	
	static void removeUriFromMediaStore(Context ctx) {
		testLife(ctx);
    	if (savedUri == null) {}
    	else
    		ctx.getContentResolver().delete(savedUri, null, null);
	}
	
	static boolean getEnabled(Context ctx) {
		testLife(ctx);
		return enabled;
	}
	
	static void setEnabled(Context ctx, boolean value) {
		SharedPreferences prefs = getPrefs(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLED, value);
		
		enabled = value;
		
		//try to reinstate the old tone, update widget
		if (!enabled) {
			removeUriFromMediaStore(ctx);
			
			if (prefs.getBoolean(RingService.WIDGET_ALIVE, false))
	    		RingWidget.disableUpdate(ctx, ctx.getString(R.string.disabled));
			
			String savedPhoneUri = prefs.getString(SAVED_PHONE_URI, null);
			if (savedPhoneUri != null)
				RingtoneManager.setActualDefaultRingtoneUri(ctx,
	    				RingtoneManager.TYPE_NOTIFICATION,
	    				Uri.parse(savedPhoneUri));
		}
		//remember users tone
		else {
			Uri u = RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION);
	    	if (u != null)
				editor.putString(SAVED_PHONE_URI, u.toString());
		}
		
		editor.commit();
	}
	
	static SharedPreferences getPrefs(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	private static void testLife(Context ctx) {
		if (life == null) {
			SharedPreferences prefs = getPrefs(ctx);
			savedPackId = prefs.getInt(SAVED_PACK_ID, -1);
			savedToneIndex = prefs.getInt(SAVED_TONE_INDEX, 0);
			
			String uri = prefs.getString(SAVED_URI, null);
			if (uri != null)
				savedUri = Uri.parse(uri);
			else
				savedUri = null;
			
			enabled = prefs.getBoolean(ENABLED, false);
			
			life = "alive";
		}
	}
}