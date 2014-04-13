package com.cryclops.ringpack;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.cryclops.ringpack.utils.SharedPrefUtils;
import com.cryclops.ringpack.viewmodel.RingActivityVm;

/**
 * The RingAccessService class is a hack for intercepting all notifications so that
 * we may rotate based on them. We perform initial setup in onServiceConnected,
 * telling it that we are an audible accessibility service, we'd like to be
 * notified for notifications. If this is on, then Sms receiver does nothing.
 */
public class RingAccessService extends AccessibilityService {

    private boolean isInitialized = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        SharedPrefUtils.setIsSmsRotationOn(getBaseContext(), false);

        if (!isInitialized) {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE;
            info.notificationTimeout = 20;
            info.packageNames = null;
            setServiceInfo(info);

            isInitialized = true;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        RingActivityVm.tryPerformRotate(getBaseContext());
    }

    @Override
    public void onInterrupt() {
        // We don't need to stop anything
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SharedPrefUtils.setIsSmsRotationOn(getBaseContext(), true);

        if (isInitialized) {
            isInitialized = false;
        }

        return super.onUnbind(intent);
    }
}
