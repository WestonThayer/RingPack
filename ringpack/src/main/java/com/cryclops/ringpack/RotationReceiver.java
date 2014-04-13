package com.cryclops.ringpack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cryclops.ringpack.utils.SharedPrefUtils;
import com.cryclops.ringpack.viewmodel.RingActivityVm;

/**
 * Rotate ringtones.
 */
public class RotationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SharedPrefUtils.getIsSmsRotationOn(context)) {
            RingActivityVm.tryPerformRotate(context);
        }
    }
}
