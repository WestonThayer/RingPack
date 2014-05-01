package com.cryclops.ringpack.viewmodel;

import android.content.Context;

import com.cryclops.ringpack.model.Tone;

import java.io.File;
import java.util.ArrayList;

/**
 * A very fake pack.
 */
public class MockPackVm implements PackVm {

    @Override
    public String getName() {
        return "Testing";
    }

    @Override
    public File getRootPath() {
        return null;
    }

    @Override
    public boolean setCurrentToneIfMatch(File absolutePath) {
        return false;
    }

    @Override
    public void clearCurrentTone() {

    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setIsSelected(boolean value) {

    }

    @Override
    public File tryCopy(File destinationPath) {
        return null;
    }

    @Override
    public void moveToNextTone(Context ctx) {

    }

    @Override
    public void delete() {

    }

    @Override
    public ArrayList<Tone> getTones() {
        return null;
    }

    @Override
    public boolean hasEnabledTones() {
        return false;
    }
}
