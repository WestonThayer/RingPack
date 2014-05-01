package com.cryclops.ringpack.viewmodel;

import com.cryclops.ringpack.model.Tone;

import java.util.ArrayList;

/**
 * Logic for editing a RingPack.
 */
public class EditActivityVm {

    private PackVm packVm;

    public EditActivityVm(PackVm packVm) {
        super();
        this.packVm = packVm;
    }

    public PackVm getPackVm() {
        return packVm;
    }

    public ArrayList<Tone> getTones() {
        return packVm.getTones();
    }

    /**
     * Let the user know where the Pack is stored on the SD card.
     * @return
     */
    public String getBasePath() {
        return packVm.getRootPath().getPath();
    }

    /**
     * Set all tones to Enabled.
     */
    public void enableAll() {
        switchAll(true);
    }

    /**
     * Set all tones to Disabled.
     */
    public void disableAll() {
        switchAll(false);
    }

    private void switchAll(boolean value) {
        for (Tone t : packVm.getTones()) {
            t.setIsEnabled(value);
        }
    }
}
