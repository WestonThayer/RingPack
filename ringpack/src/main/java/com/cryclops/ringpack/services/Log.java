package com.cryclops.ringpack.services;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import com.cryclops.ringpack.viewmodel.PackVm;

/**
 * Describes structured events that should be recorded.
 */
public interface Log {

    // categories and actions for the MediaStore ContentProvider
    static final String CATEGORY_MEDIA_STORE = "content_provider_media_store";
    static final String ACTION_MEDIA_STORE_DELETE = "delete";
    static final String ACTION_MEDIA_STORE_INSERT = "insert";

    // categories and actions for Ringtones
    static final String CATEGORY_RINGTONE = "ringtone";
    static final String ACTION_RINGTONE_ROTATE_FAIL = "rotate_fail";

    // categories and actions for RingPacks
    static final String CATEGORY_RINGPACK = "ringpack";
    static final String ACTION_RINGPACK_READ = "read_pack";
    static final String ACTION_RINGPACK_SET_STARTED = "set_pack_started";
    static final String ACTION_RINGPACK_SET_COMPLETED = "set_pack_completed";

    // categories and actions for UI
    static final String CATEGORY_UI = "ui";
    static final String ACTION_UI_LONGPRESS = "longpress";
    static final String ACTION_UI_TAP = "tap";

    void activityStart(Activity activity);
    void activityStop(Activity activity);

    void exception(ActivityNotFoundException ex, boolean isFatal);
    void exception(Exception ex, boolean isFatal);

    /**
     * Log when an attempt to delete a specific row from a ContentProvider fails.
     * @param uri The URI of the row
     */
    void failContentProviderDeleteRow(Uri uri);

    /**
     * Log when RingPack tried to rotate Ringtones, but couldn't because the SD card wasn't
     * mounted.
     */
    void failRotateSd();

    /**
     * Log when a RingPack directory doesn't have an info file.
     */
    void missingInfoFile();

    /**
     * Log an event when the user requests for a RingPack to be set.
     * @param packVm
     */
    void setRingPackStarted(PackVm packVm);

    /**
     * Log an event when a new RingPack is selected for use.
     * @param packVm
     */
    void setRingPackCompleted(PackVm packVm);

    /**
     * Log when inserting a Ringtone for the first time because the MediaStore.Files table wasn't
     * initialized. Uncommon case, only API >= 11.
     */
    void insertRingtoneFiles();

    /**
     * Log when we've accidentally added way too many RingPack ringtones to the MediaStore and we
     * have to delete them all.
     */
    void deleteRingtoneCleanup();

    /**
     * Log when the user had used the press-and-hold gesture.
     * @param descriptor What they used the gesture on
     */
    void longPress(String descriptor);

    /**
     * Log when the user tapped a pack that was already selected, maybe expecting it to
     * deactivate itself.
     */
    void doubleSetRingPack();
}
