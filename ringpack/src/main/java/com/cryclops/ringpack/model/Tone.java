package com.cryclops.ringpack.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import com.cryclops.ringpack.utils.MediaStoreObject;
import com.cryclops.ringpack.utils.MediaStoreUtils;
import com.cryclops.ringpack.utils.RingtoneManagerUtils;
import com.cryclops.ringpack.utils.ServiceUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A single ringtone that belongs to a Pack.
 */
public class Tone implements Serializable {

    private boolean isEnabled;
    private String name;
    private File path;

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
     */
    public void setDefaultNotificationRingtone(Context ctx) {
        // SDK 11+ has the Files store, which already indexed... everything. Worse yet, if we
        // attempt to insert a duplicate record, it'll fail.
        // Simply delete the tone if it's in Files.
        if (Build.VERSION.SDK_INT >= 11) {
            int rowsDeleted = MediaStoreUtils.deleteFilesRingPackTone(ctx, path.getAbsolutePath());

            if (rowsDeleted > 1) {
                throw new UnsupportedOperationException();
            }
        }

        // Keep a single entry in Audio.Media and change it's path
        MediaStoreObject tone = null;

        ArrayList<MediaStoreObject> results = MediaStoreUtils.queryForExternalRingPackTones(ctx);

        if (results.size() == 1) {
            // Best case, there's only one entry there that we're maintaining, we just have to
            // update it to point to a new ringtone on disk
            tone = results.get(0);

            tone.data = path.getAbsolutePath();
            tone.size = path.length();
            tone.displayName = path.getName();

            if (!MediaStoreUtils.update(ctx, tone)) {
                throw new UnsupportedOperationException();
            }
        }
        else if (results.size() == 0) {
            // First time, we gotta add it
            tone = new MediaStoreObject();
            tone.data = path.getAbsolutePath();
            tone.size = path.length();
            tone.displayName = path.getName();

            tone = MediaStoreUtils.insertExternal(ctx, tone);

            if (tone == null) {
                throw new UnsupportedOperationException();
            }
        }
        else {
            // We put more than one in? Bad on us, let's clean up the external store.
            ServiceUtils.getLog().deleteRingtoneCleanup();

            if (MediaStoreUtils.deleteExternalRingPackTones(ctx) < 2) {
                throw new UnsupportedOperationException();
            }

            // call ourselves to do a proper set
            setDefaultNotificationRingtone(ctx);
            return;
        }

        Uri currentToneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);

        // Assuming that if we just updated the tone _data column, we don't need to reset
        // anything, Android should just call the new one
        if (currentToneUri == null || !tone.uri.equals(currentToneUri)) {
            RingtoneManagerUtils.setDefaultNotificationRingtone(ctx, tone.uri);
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
