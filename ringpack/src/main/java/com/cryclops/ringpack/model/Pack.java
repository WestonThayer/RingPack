package com.cryclops.ringpack.model;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a generic RingPack.
 */
public interface Pack extends Serializable {

    String getName();
    List<Tone> getTones();
    File getRootPath();
    Tone getCurrentTone();
    void setCurrentTone(Tone value);
}
