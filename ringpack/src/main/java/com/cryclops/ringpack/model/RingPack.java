package com.cryclops.ringpack.model;

import java.io.File;
import java.util.ArrayList;

/**
 * Represents a collection of tones, e.g. a RingPack.
 */
public class RingPack implements Pack {

    private ArrayList<Tone> tones;
    private String name;
    private File rootPath;
    private Tone currentTone;

    public RingPack(String name, String rootPath) {
        this.tones = new ArrayList<Tone>();
        this.name = name;
        this.rootPath = new File(rootPath);
        this.currentTone = null;
    }

    public RingPack(String name) {
        this.name = name;
    }

    @Override
    public ArrayList<Tone> getTones() {
        return tones;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getRootPath() {
        return rootPath;
    }

    @Override
    public Tone getCurrentTone() {
        return currentTone;
    }

    @Override
    public void setCurrentTone(Tone value) {
        currentTone = value;
    }
}
