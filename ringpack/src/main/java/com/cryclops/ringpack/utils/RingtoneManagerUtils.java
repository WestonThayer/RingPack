package com.cryclops.ringpack.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Helpers for the RingtoneManager.
 */
public class RingtoneManagerUtils {

    /**
     * Getter for the current Default Notification Ringtone URI.
     * @param ctx
     * @return
     */
    public static Uri getDefaultNotificationRingtoneUri(Context ctx) {
        return RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Getter for the current Default Notification Ringtone object.
     * @param ctx
     * @return The Ringtone, or null if there isn't one set.
     */
    public static Ringtone getDefaultNotificationRingtone(Context ctx) {
        Uri currentNotificationRingtoneUri = getDefaultNotificationRingtoneUri(ctx);

        if (currentNotificationRingtoneUri == null) {
            return null;
        }

        Ringtone r = RingtoneManager.getRingtone(ctx, currentNotificationRingtoneUri);

        return r;
    }

    /**
     * Set the Default Notification Ringtone to the given URI.
     * @param ctx
     * @param uri URI of a row in MediaStore
     */
    public static void setDefaultNotificationRingtone(Context ctx, Uri uri) {
        RingtoneManager.setActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION, uri);
    }
}
