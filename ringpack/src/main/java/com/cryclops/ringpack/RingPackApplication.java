package com.cryclops.ringpack;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import com.cryclops.ringpack.services.AppServiceLocator;
import com.cryclops.ringpack.services.Log;
import com.cryclops.ringpack.viewmodel.PackVm;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.util.Map;

/**
 *
 */
public class RingPackApplication extends Application implements Log {

    private EasyTracker easyTracker;
    private StandardExceptionParser exceptionParser;

    @Override
    public void onCreate() {
        super.onCreate();
        AppServiceLocator.getInstance().addService(Log.class, this);

        easyTracker = EasyTracker.getInstance(this);
        exceptionParser = new StandardExceptionParser(this, null);
    }

    // No real need to deregister, we'll have left memory

    @Override
    public void activityStart(Activity activity) {
        if (activity != null) {
            easyTracker.activityStart(activity);
        }
    }

    @Override
    public void activityStop(Activity activity) {
        if (activity != null) {
            easyTracker.activityStop(activity);
        }
    }

    @Override
    public void exception(ActivityNotFoundException ex, boolean isFatal) {
        if (ex != null) {
            String desc = exceptionParser.getDescription(Thread.currentThread().getName(), ex);
            Map<String, String> map = MapBuilder.createException(
                    desc + " || msg: " + ex.getMessage(),
                    isFatal
            ).build();

            easyTracker.send(map);
        }
    }

    @Override
    public void failContentProviderDeleteRow(Uri uri) {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_MEDIA_STORE,
                ACTION_MEDIA_STORE_DELETE,
                "single_row",
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void failRotateSd() {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_RINGTONE,
                ACTION_RINGTONE_ROTATE_FAIL,
                "no_sd",
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void missingInfoFile() {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_RINGPACK,
                ACTION_RINGPACK_READ,
                "missing_info_file",
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void setRingPackStarted(PackVm packVm) {
        String packName = packVm == null ? "null" : packVm.getName();

        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_RINGPACK,
                ACTION_RINGPACK_SET_STARTED,
                packName,
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void setRingPackCompleted(PackVm packVm) {
        String packName = packVm == null ? "null" : packVm.getName();

        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_RINGPACK,
                ACTION_RINGPACK_SET_COMPLETED,
                packName,
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void insertRingtoneFiles() {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_MEDIA_STORE,
                ACTION_MEDIA_STORE_INSERT,
                "insert_ringtone_files",
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void deleteRingtoneCleanup() {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_MEDIA_STORE,
                ACTION_MEDIA_STORE_DELETE,
                "delete_ringtone_cleanup",
                null
        ).build();

        easyTracker.send(map);
    }

    @Override
    public void longPress(String descriptor) {
        if (descriptor != null) {
            Map<String, String> map = MapBuilder.createEvent(
                    CATEGORY_UI,
                    ACTION_UI_LONGPRESS,
                    descriptor,
                    null
            ).build();

            easyTracker.send(map);
        }
    }

    @Override
    public void doubleSetRingPack() {
        Map<String, String> map = MapBuilder.createEvent(
                CATEGORY_UI,
                ACTION_UI_TAP,
                "double_set_ringpack",
                null
        ).build();

        easyTracker.send(map);
    }
}
