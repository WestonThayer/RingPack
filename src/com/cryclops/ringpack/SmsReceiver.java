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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The SmsReceiver class goes to the next tone if it was an SMS and RingService
 * is enabled and it's in SMS mode.
 * 
 * This can, and probably should, be merged with RingReceiver for clarity.
 * 
 * @author Weston Thayer
 * @version 2.0.0
 * @version 2.1.0
 * 					Compatibility with Utilities class
 *
 */
public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {		
		if (Utilities.getEnabled(context)) {
			Intent i = new Intent();
			i.setClass(context, RingService.class);
			i.putExtra(RingService.ACTION, RingService.NEXT_TONE);
			context.startService(i);
		}
	}
}