package com.cryclops.ringpack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The SmsReceiver class goes to the next tone if it was an SMS and RingService
 * is enabled and it's in SMS mode.
 * 
 * @author Cryclops
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