package com.cryclops.ringpack;

import java.io.File;
import java.util.ArrayList;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * The RingService Service sits in the background and swaps the ringtone when
 * asked. It has 3 abilities: change packs, change to the next tone in a pack,
 * and play the current tone.
 * 
 * It cleans up after itself when destroyed.
 * 
 * @author Cryclops
 * @version	2.0.0
 * @version 2.1.0
 * 				Using the Utilities class to store pack, currIndex, enabled, and
 * for other misc tasks. Tones ArrayList is always remade, state is always
 * saved. Modes are removed.
 *
 */
public class RingService extends Service {
	
	public static final String TAG = "com.cryclops.ringpack";
	
	//statics for preferences
	public static final String LOCK = "LOCK_PREF";
	public static final String SHUFFLE = "SHUFFLE_PREF";
	public static final String WIDGET_ALIVE = "WIDGET_ALIVE_PREF";
	public static final String ACCESS = "ACCESS_PREF";
	public static final String SHOW_ABOUT = "SHOW_ABOUT_PREF";
	
	//statics for data extras
	public static final String PASSED_PACK = "PASSED_PACK";
	
	//statics for actions
	public static final String ACTION = "ACTION";
	public static final int PACK_SET = 0;
	public static final int NEXT_TONE = 1;
	public static final int PLAY_TONE = 2;
	public static final int WIDGET_STATUS = 3;
	
	//private local variables
	private boolean lockPref, shufflePref, widgetEnabled;
	private DbManager db;
	private Vibrator vib;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		db = new DbManager(this);
		db.open();
		
		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	    
	    //figure out the widget's status
	    widgetEnabled = Utilities.getPrefs(getBaseContext()).getBoolean(WIDGET_ALIVE, false);
	    
	    //save the ringtone for playing for the widget
        Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		r = RingtoneManager.getRingtone(getBaseContext(), u);
		playAfterSet = false;
		
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		db.close();
		
		super.onDestroy();
	}
	
	//for API < version 5
    @Override
    public void onStart(Intent intent, int startId) {
        handleStart(intent);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	handleStart(intent);
    	
    	return START_NOT_STICKY;
    }
    
    /**
     * Make sure the SD card is accessible, then do 
     * 
     * @param intent	The starting Intent.
     */
    private void handleStart(Intent intent) {    	
    	final Intent i = intent;
    	
    	if (isSdOk()) {
    		int action = i.getIntExtra(ACTION, -1);
    		
    		switch(action) {
    		case PACK_SET:
    			playAfterSet = true;
    			
    	    	Thread packSetThread = new Thread() {
    	    		@Override
    	    		public void run() {
    	    			int passedPackId = i.getIntExtra(PASSED_PACK, -1);
    	    			//we were passed the id
    	    			if (passedPackId != -1) {		    	    			
	    	    			if (!Utilities.getEnabled(getBaseContext()))
	    	    				initControl(passedPackId);
	    	    			else
	    	    				setPack(passedPackId);
	    	    			
	    	    			packSetHandler.sendEmptyMessage(0);
    	    			}
    	    		}
    	    	};
    	    	packSetThread.start();
    			break;
    		case NEXT_TONE:
    			checkPrefs();
    			swapTone();
    			break;
    		case PLAY_TONE:
    			playCurrentTone();
    			break;
    		case WIDGET_STATUS:
    			widgetEnabled = intent.getBooleanExtra(WIDGET_ALIVE, false);
    			if (toneName != null)
    				RingWidget.update(getBaseContext(), toneName);
    			break;
    		default:
    			break;
    		}
    	}
    }
    
    /**
     * Toast and play tone.
     */
    private Handler packSetHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		//notify the user
    		Toast.makeText(RingService.this,
    				getResources().getString(R.string.packSet),
    				Toast.LENGTH_SHORT).show();
    	}
    };
    
    /**
     * The private initControl method enables the service and saved the old
     * ringtone's uri that we're hijacking.
     * 
     * @param packId	Shouldn't have really done it like this
     */
    private void initControl(int packId) {
    	Utilities.setEnabled(getBaseContext(), true);
    	
    	setPack(packId);
    }
    
    /**
     * The private setPack method finds the pack's path on the SD card,
     * populates the tones ArrayList with the pack's tones, then set the
     * notification tone to the first one in the pack.
     * 
     * @param packId	The id of the pack to set.
     */
    private void setPack(int packId) {
    	Utilities.setSavedPackId(getBaseContext(), packId);
    	
    	//get the info we need to work with this pack
    	//it's path on the SD
    	//build the tones ArrayList to work from
    	grabPath(packId);
		if (tones == null)
			tones = new ArrayList<Integer>();
		else
			tones.clear();
		mapTones(packId);
		Utilities.setSavedToneIndex(getBaseContext(), 0);
		setNotificationTone(tones.get(Utilities.getSavedToneIndex(getBaseContext())));
    }
    
    //more private variables that matter less
    private String packPath, toneName;
    private boolean playAfterSet;
    private ArrayList<Integer> tones;
    private Ringtone r;
    
    /**
     * The private swapTone method goes to the next tone based on the lock
     * and shuffle preferences.
     */
    private void swapTone() {
    	//get the info we need to work with this pack
    	//it's path on the SD
    	//build the tones ArrayList to work from
    	int id = Utilities.getSavedPackId(getBaseContext());
    	grabPath(id);
		if (tones == null)
			tones = new ArrayList<Integer>();
		else
			tones.clear();
		mapTones(id);
    	
		int currIndex = Utilities.getSavedToneIndex(getBaseContext());
		
    	//locked
    	if (lockPref)
    		return;
    	//shuffle on
    	else if (shufflePref) {
    		int randIndex = currIndex;
    		
    		while (randIndex == currIndex)
    			randIndex = (int) Math.floor(Math.random() * tones.size());
    		currIndex = randIndex;
    	}
    	//shuffle off
    	else {
    		if (currIndex < (tones.size() - 1))
				currIndex++;
			else
				currIndex = 0;
    	}
    	
    	Utilities.setSavedToneIndex(getBaseContext(), currIndex);
    	setNotificationTone(tones.get(currIndex));
    }
    
    /**
     * The private isSdOk method displays a Toast if the service wasn't able to
     * perform the requested task. The tone should still be able to be played,
     * but if the phone can't find the tone, it may go to that default tone
     * until it becomes available again.
     * 
     * The idea is to let the user know how to fix it if something goes wrong.
     * 
     * @return	Returns true if the SD checks out
     */
    private boolean isSdOk() {
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
    		Toast.makeText(this, getString(R.string.mediaMountedReadOnly), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	else if (status.equals(Environment.MEDIA_SHARED)) {
    		Toast.makeText(this, getString(R.string.mediaShared), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	else if (status.equals(Environment.MEDIA_REMOVED)) {
    		Toast.makeText(this, getString(R.string.mediaRemoved), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	else if (status.equals(Environment.MEDIA_UNMOUNTED)) {
    		Toast.makeText(this, getString(R.string.mediaUnmounted), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * The private checkPrefs method updates all of our necessary preferences
     */
    private void checkPrefs() {
    	SharedPreferences prefs = Utilities.getPrefs(getBaseContext());
    	lockPref = prefs.getBoolean(LOCK, false);
    	shufflePref = prefs.getBoolean(SHUFFLE, true);
    }

    /**
     * The private grabPath method happens when a new pack is set. Just collects
     * the path field from the database.
     * 
     * @param packId	The rowId of the pack to grab
     */
    private void grabPath(int packId) {
    	Cursor c = db.fetchPack(packId);
    	c.moveToFirst();
    	
    	int locationCol = c.getColumnIndex(DbManager.pKEY_LOCATION);
    	packPath = c.getString(locationCol);
    	
    	c.close();
    }
    
    /**
     * The private mapTones method takes a packId and finds all tones belonging
     * to that pack, putting their access ids into an ArrayList.
     * 
     * @param packId	The id of the pack to map
     */
    private void mapTones(int packId) {
        //grab a list of all pack tones
        Cursor c = db.fetchPackTones(packId);
        c.moveToFirst();
        
        int idCol = c.getColumnIndex(DbManager.tKEY_ROWID);
        int enabledCol = c.getColumnIndex(DbManager.tKEY_ENABLED);
        
        int tid;
        
        do {
        	tid = c.getInt(idCol);
        	if (c.getInt(enabledCol) == 1)
        		tones.add(tid);
        }
        while (c.moveToNext());
        
        c.close();
    }

    /**
     * The private setNotificationTone method takes the toneId of a tone from
     * the database, then finds it on SD, and sets it as the default
     * notification tone.
     * 
     * @param toneId	The integer id of the tone
     */
    private void setNotificationTone(int toneId) {
    	//remove the old tone from our library
    	Utilities.removeUriFromMediaStore(getBaseContext());
    	
    	//get the filename & name of the tone
    	Cursor c = db.fetchTone(toneId);
    	c.moveToFirst();
    	
    	int nameCol = c.getColumnIndex(DbManager.tKEY_NAME);
    	int filenameCol = c.getColumnIndex(DbManager.tKEY_FILENAME);
    	
    	toneName = c.getString(nameCol);
    	String filename = c.getString(filenameCol);
    	
    	c.close();
    	
    	//add it to MediaStore and set it
    	File file = new File(packPath, filename);
    	
    	ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, "RingPack Tone");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg");
        values.put(MediaStore.Audio.Media.ARTIST, "RingPack");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        
        Utilities.setSavedUri(getBaseContext(), getContentResolver().insert(
        		MediaStore.Audio.Media.getContentUriForPath(
        				file.getAbsolutePath()), values));
        
        //wait 200ms before setting ringtone so that it has a chance to play
        delayHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				RingtoneManager.setActualDefaultRingtoneUri(RingService.this,
						RingtoneManager.TYPE_NOTIFICATION,
						Utilities.getSavedUri(getBaseContext()));
				
				//now shoot the name over to the widget
		        if (widgetEnabled && toneName != null)
		        	RingWidget.update(getBaseContext(), toneName);
		        
		        //save the ringtone for playing so we don't do too many calls
		        Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				r = RingtoneManager.getRingtone(RingService.this.getBaseContext(), u);
				
				Message m = Message.obtain();
				if (playAfterSet) {
					m.arg1 = 0;
					delayHandler.sendMessage(m);
				}
				else {
					m.arg1 = 1;
					delayHandler.sendMessage(m);
				}
			}
        }, 200);
    }
    
    private Handler delayHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		if (msg.arg1 == 0) {
    			playCurrentTone();
        		playAfterSet = false;
    		}
    		
    		RingService.this.stopSelf();
    	}
    };

    private void playCurrentTone() {
		if (r != null && !r.isPlaying()) {
			r.play();
			vib.vibrate(300);
		}
    }
}