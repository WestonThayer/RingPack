package com.cryclops.ringpack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The RingReceiver class takes care of maintaining RingPack over a restart.
 * 
 * @author Cryclops
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
	
	//private static final String RESTORE_ON_BOOT = "RESTORE_ON_BOOT";
	
	//private int packId;
	//private Context ctx;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			/*SharedPreferences prefs = Utilities.getPrefs(context);
			boolean restore = prefs.getBoolean(RESTORE_ON_BOOT, false);
			if (restore) {
				packId = prefs.getInt(RingService.SAVED_PACKID, 0);
				ctx = context;
				
				//Timer will poll every 8 seconds to see if the SD card is ready
				if (packId != 0) {
					Timer timer = new Timer();
					timer.schedule(new StarterTask(), 0, 8000);
				}
			}*/
		}
		else if (action.equals(Intent.ACTION_SHUTDOWN)) {
			/*if (Utilities.getEnabled(context)) {
				SharedPreferences.Editor editor = Utilities.getPrefs(context).edit();
				editor.putBoolean(RESTORE_ON_BOOT, true);
				editor.commit();
			}*/
			
			Intent i = new Intent();
			i.setClass(context, RingService.class);
			context.stopService(i);
			
			Utilities.removeUriFromMediaStore(context);
		}
	}
	
	/**
	 * The StarterTask runs every 8 seconds until the SD card checks out, then
	 * it starts up RingService and sets the pack.
	 * 
	 * @author Cryclops
	 * @version 2.0.0
	 *
	 */
	/*private class StarterTask extends TimerTask {

		@Override
		public void run() {
			if (isSdOk()) {
				Intent i = new Intent();
				i.setClass(ctx, RingService.class);
				i.putExtra(RingService.ACTION, RingService.PACK_SET);
				i.putExtra(RingService.PASSED_PACK, packId);
				ctx.startService(i);
				
				this.cancel();
			}
		}
	}
	
	private boolean isSdOk() {
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
    		return false;
    	else if (status.equals(Environment.MEDIA_SHARED))
    		return false;
    	else if (status.equals(Environment.MEDIA_REMOVED))
    		return false;
    	else if (status.equals(Environment.MEDIA_UNMOUNTED))
    		return false;
    	
    	return true;
    }*/
}