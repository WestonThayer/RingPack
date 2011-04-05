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
 * The RingReceiver class takes care of maintaining RingPack over a restart by
 * making sure we don't litter URI's in MediaStore (although it may not be that
 * large of a deal since MediaStore seems to clean itself).
 * 
 * @author Weston Thayer
 * @version 2.0.0
 * @version 2.0.1
 * 					Fixed recovery on boot
 * @version 2.1.0
 * 					Compatibility with Utilities class. On shutdown, just remove
 * the Uri from MediaStore, and shutdown RingService so it doesn't immediately
 * restart (on the slim chance that its currently running). No action is needed
 * on boot completion since the first message will cause everything to be
 * rebuilt anyway.
 *
 */
public class RingReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (action.equals(Intent.ACTION_SHUTDOWN)) {		
			Intent i = new Intent();
			i.setClass(context, RingService.class);
			context.stopService(i);
			
			Utilities.removeUriFromMediaStore(context);
		}
	}
}