package com.cryclops.ringpack.viewmodel;

import android.content.Context;

import com.cryclops.ringpack.model.Tone;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 */
public interface PackVm extends Serializable {

    String getName();
    File getRootPath();

    /**
     * Given the Path of a Tone, update this PackVm's current Tone if it's a match.
     * @param absolutePath The absolute path on disk of the Tone
     * @return True if the current Tone was set
     */
    boolean setCurrentToneIfMatch(File absolutePath);
    void clearCurrentTone();

    boolean isSelected();
    void setIsSelected(boolean value);
    File tryCopy(File destinationPath);

    /**
     * Ask the Pack to switch to the next Ringtone in it's pack. This should be the only
     * method with which you set the Default Notification Ringtone.
     * @param ctx
     */
    void moveToNextTone(Context ctx);

    /**
     * Delete this PackVm from disk.
     */
    void delete();

    /**
     * Caution: you only need this method if you're going to visually display the tones. It should
     * not be used for manipulating them in any way.
     * @return
     */
    ArrayList<Tone> getTones();
}
