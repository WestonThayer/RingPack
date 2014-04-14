package com.cryclops.ringpack.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.cryclops.ringpack.utils.RingtoneManagerUtils;

import java.io.File;
import java.io.Serializable;

/**
 * A single ringtone that belongs to a Pack.
 */
public class Tone implements Serializable {

    private boolean isEnabled;
    private String name;
    private File path;

    /**
     * The name all RingPack tones will have.
     */
    public static final String DEFAULT_TONE_NAME = "RingPack Tone";

    public Tone(boolean isEnabled, String name, String path) {
        this(isEnabled, name, new File(path));
    }

    public Tone(boolean isEnabled, String name, File path) {
        this.isEnabled = isEnabled;
        this.name = name;
        this.path = path;
    }

    /**
     * Does the user want this tone to play or did they disable it via the UI?
     * @return
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Enable or disable this Ringtone on disk.
     * @param value
     */
    public void setIsEnabled(boolean value) {
        if (isEnabled != value) {
            String fullPath = getPathFile().getPath();
            fullPath = value ? fullPath.replace(".disabled.ogg", ".ogg") : fullPath.replace(".ogg", ".disabled.ogg");
            File fullPathFile = new File(fullPath);

            if (!getPathFile().renameTo(fullPathFile)) {
                throw new UnsupportedOperationException();
            }

            path = fullPathFile;
            isEnabled = value;
        }
    }

    public String getName() {
        return name;
    }

    public File getPathFile() {
        return path;
    }

    /**
     * Update the current Notification Ringtone to this one.
     *
     * Involves ContentProvider queries. Call from a non-UI thread.
     *
     * @param ctx
     * @return The URI of the tone in MediaStore
     */
    public Uri setDefaultNotificationRingtone(Context ctx) {
        // SDK 11+ has the Files store, which already indexed... everything. Worse yet, if we
        // attempt to insert a duplicate record, it'll fail.
        // We're forced to always query for the URI of this tone
        if (Build.VERSION.SDK_INT >= 11) {
            Uri toneUri = null;

            Uri filesUri = MediaStore.Files.getContentUri("external");
            String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE};
            String selection = MediaStore.MediaColumns.DATA + " = ?";
            String[] args = {path.getAbsolutePath()};
            Cursor c = ctx.getContentResolver().query(filesUri, projection, selection, args, null);

            if (c.getCount() == 1) {
                c.moveToFirst();
                long rowId = c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID));
                String title = c.getString(c.getColumnIndex(MediaStore.MediaColumns.TITLE));
                toneUri = MediaStore.Files.getContentUri("external", rowId);

                // Check to see if we've given the proper metadata yet
                if (!title.equals(DEFAULT_TONE_NAME)) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.TITLE, DEFAULT_TONE_NAME);
                    values.put(MediaStore.Audio.Media.ARTIST, "RingPack");
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                    if (ctx.getContentResolver().update(toneUri, values, null, null) != 1) {
                        throw new UnsupportedOperationException();
                    }
                }
            }
            else if (c.getCount() == 0) {
                // I suppose the MediaScanner hasn't run yet, we'll insert it
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, path.getAbsolutePath());
                values.put(MediaStore.MediaColumns.SIZE, path.length());
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, path.getName());
                values.put(MediaStore.MediaColumns.TITLE, DEFAULT_TONE_NAME);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg"); // consider application/ogg
                values.put(MediaStore.Audio.Media.ARTIST, "RingPack");
                values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                toneUri = ctx.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

                if (toneUri == null) {
                    throw new UnsupportedOperationException();
                }
            }
            else {
                // This is truly unexpected.
                throw new UnsupportedOperationException();
            }

            c.close();

            RingtoneManagerUtils.setDefaultNotificationRingtone(ctx, toneUri);

            return toneUri;
        }
        // The legacy way lets us keep a single entry and change it's path
        else {
            Uri toneUri = null;

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, path.length());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, path.getName());

            // Search for anything with our Title
            String[] projection = {MediaStore.MediaColumns._ID};
            String selection = MediaStore.MediaColumns.TITLE + " = ?";
            String[] args = {DEFAULT_TONE_NAME};
            Cursor c = ctx.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    args,
                    null);

            if (c.getCount() == 1) {
                c.moveToFirst();
                String rowId = c.getString(c.getColumnIndex(MediaStore.MediaColumns._ID));
                toneUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, rowId);

                if (ctx.getContentResolver().update(toneUri, values, null, null) != 1) {
                    throw new UnsupportedOperationException();
                }
            }
            else if (c.getCount() == 0) {
                // First time, we gotta add it
                values.put(MediaStore.MediaColumns.TITLE, DEFAULT_TONE_NAME);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg"); // consider application/ogg
                values.put(MediaStore.Audio.Media.ARTIST, "RingPack");
                values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                toneUri = ctx.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

                if (toneUri == null) {
                    throw new UnsupportedOperationException();
                }
            }
            else {
                // That's bad. Delete all of our entries.
                int rowsDeleted = ctx.getContentResolver().delete(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        selection,
                        args
                );

                if (rowsDeleted < 2) {
                    throw new UnsupportedOperationException();
                }

                // call ourselves to do a proper set
                c.close(); // don't leak
                return setDefaultNotificationRingtone(ctx);
            }

            c.close();

            Uri currentToneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);

            // Assuming that if we just updated the tone _data column, we don't need to reset
            // anything, Android should just call the new one
            if (currentToneUri == null || !toneUri.equals(currentToneUri)) {
                // Apparently this is best practice, although I have no idea what the Media Scanner
                // does with the new data
                ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, toneUri));

                RingtoneManagerUtils.setDefaultNotificationRingtone(ctx, toneUri);
            }

            return toneUri;
        }
    }

    /**
     * Uses a MediaPlayer to play the tone.
     */
    public void play(Context ctx) {
        final MediaPlayer mp = MediaPlayer.create(ctx, Uri.parse(getPathFile().getAbsolutePath()));
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
            }
        });
        mp.start();
    }
}
