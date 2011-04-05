/*
 * RingPack is a 'Notification ringtone' rotator for Android.
 *
 * Copyright (C) 2010 Weston Thayer
 *
 * This file is part of RingPack.
 *
 * RingPack is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * RingPack is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RingPack.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cryclops.ringpack;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

/**
 * The RingAccessService class is a hack for intercepting notifications so that
 * we may rotate based on them. We perform initial setup in onServiceConnected,
 * telling it that we are an audible accessibility service, we'd like to be
 * notified for notifications, and we are only interested in 2 packages.
 * 
 * @author Weston Thayer
 * @version 2.0.0
 * @version 2.1.0
 * 					Compatible with Utilities class
 *
 */
public class RingAccessService extends AccessibilityService {
	
	private boolean firstRun = false;
	
	@Override
	public void onServiceConnected() {
		if (!firstRun) {
			AccessibilityServiceInfo info = new AccessibilityServiceInfo();
			info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
			info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE;
			info.notificationTimeout = 80;
			info.packageNames = new String[] {
					"com.google.android.apps.googlevoice",
					"com.google.android.gsf"};
			setServiceInfo(info);
			firstRun = true;
		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		SharedPreferences prefs = Utilities.getPrefs(getBaseContext());
		boolean accessPref = prefs.getBoolean(RingService.ACCESS, false);
		
		if (Utilities.getEnabled(getBaseContext()) && accessPref) {
			Intent i = new Intent(this, RingService.class);
			i.putExtra(RingService.ACTION, RingService.NEXT_TONE);
			this.startService(i);
		}
	}

	@Override
	public void onInterrupt() {
		//
	}
}