package com.cryclops.ringpack.viewmodel;

import android.content.Context;

import java.util.ArrayList;

/**
 * An base ViewModel that knows how to be initialized.
 */
public abstract class InitializableActivityVm {

    private ArrayList<OnInitializedListener> onInitializedListeners;

    public InitializableActivityVm() {
        onInitializedListeners = new ArrayList<OnInitializedListener>();
    }

    public abstract boolean initializeAsync(Context baseContext);

    /**
     * Register to be notified when the object has completed initialization.
     * @param e
     */
    public void setOnInitializedListener(OnInitializedListener e) {
        onInitializedListeners.add(e);
    }
    public void removeOnInitializedListener(OnInitializedListener e) { onInitializedListeners.remove(e); }
    protected void fireOnInitialized() {
        for (OnInitializedListener e : onInitializedListeners) {
            e.onInitialized(this);
        }
    }
}
