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

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * The RingEditor Activity is started by RingActivity when the user desires to
 * edit which sounds are in a pack or would just like to sample them.
 * 
 * @author Weston Thayer
 * @version 2.0.0
 * @version 2.0.2
 * 				Fixed the volume key control to set to media
 *
 */
public class RingEditor extends Activity {
	
	private static final int SET_TITLE = 0;
	private static final int ADD_TONE = 1;
	
	private DbManager db;
	private LinearLayout container;
	private MediaPlayer mp;
	private ProgressDialog pd;
	private String packName, packPath;
	private int packId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ringeditor);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		packId = getIntent().getIntExtra(RingService.PASSED_PACK, -1);
		if (packId == -1)
			finish();
		
		db = new DbManager(this);
		db.open();
		
		container = (LinearLayout) findViewById(R.id.editCheckHolder);
		
		pd = ProgressDialog.show(this,
        		getString(R.string.initProgressTitle),
        		getString(R.string.initProgressContents), true, false);
		Thread initThread = new Thread() {
			
			@Override
			public void run() {
				//get title and path
				Cursor c = db.fetchPack(packId);
				c.moveToFirst();
				
				int nameCol = c.getColumnIndex(DbManager.pKEY_NAME);
				int pathCol = c.getColumnIndex(DbManager.pKEY_LOCATION);
				
				packName = c.getString(nameCol);
				packPath = c.getString(pathCol);
				
				c.close();
				
				Message msg = Message.obtain();
				msg.arg1 = SET_TITLE;
				initHandler.sendMessage(msg);
				
				//now populate our list
				c = db.fetchPackTones(packId);
				c.moveToFirst();
				
				int songnameCol = c.getColumnIndex(DbManager.tKEY_NAME);
		        int enabledCol = c.getColumnIndex(DbManager.tKEY_ENABLED);
		        int idCol = c.getColumnIndex(DbManager.tKEY_ROWID);
		        boolean b;
		        int id;
		        ArrayList<LinearLayout> layoutList = new ArrayList<LinearLayout>();
		        
		        do {
		        	id = c.getInt(idCol);
		        	
		        	LinearLayout.LayoutParams params =
		        		new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		        			LayoutParams.WRAP_CONTENT);
		        	LinearLayout ll = new LinearLayout(RingEditor.this);
		        	ll.setLayoutParams(params);
		        	ll.setOrientation(LinearLayout.HORIZONTAL);
		        	ll.setId(id);
		        	
		        	CheckBox cb = (CheckBox) CheckBox.inflate(RingEditor.this,
		        			R.layout.ringeditor_item_check, null);
		        	cb.setText(c.getString(songnameCol));
		        	cb.setId(id);
		        	
		        	b = (c.getInt(enabledCol) == 1);
		        	cb.setChecked(b);
		        	
		        	cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							db.updateTone(buttonView.getId(), isChecked);
						}
		        	});
		        	
		        	ll.addView(cb);
		        	
		        	ImageButton iv = (ImageButton) ImageButton.inflate(
		        			RingEditor.this, R.layout.ringeditor_item, null);
		        	iv.setId(id);
		        	iv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							playTone(v.getId());
						}
		        	});
		        	
		        	ll.addView(iv);
		        	
		        	layoutList.add(ll);
		        }
		        while (c.moveToNext());
		        
		        c.close();
		        
		        Message ms = Message.obtain();
	        	ms.arg1 = ADD_TONE;
	        	ms.obj = layoutList;
	        	initHandler.sendMessage(ms);
			}
		};
		initThread.run();
	}
	
	private Handler initHandler = new Handler() {
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case SET_TITLE:
				RingEditor.this.setTitle(packName);
				break;
			case ADD_TONE:
				for (int i = 0; i < ((ArrayList<LinearLayout>) msg.obj).size(); i++)
					container.addView(((ArrayList<LinearLayout>) msg.obj).get(i));
				pd.dismiss();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onStop() {
		db.close();
		if (mp != null) {
			mp.release();
		}
		
		super.onStop();
	}
	
	private void playTone(int id) {
		if (mp != null && mp.isPlaying())
			mp.stop();
		
		Cursor c = db.fetchTone(id);
		c.moveToFirst();
		
		int filenameCol = c.getColumnIndex(DbManager.tKEY_FILENAME);
    	String filename = c.getString(filenameCol);
    	
    	c.close();
    	
    	Uri tone = Uri.parse(packPath + filename);
    	
    	if (mp == null) {
    		mp = MediaPlayer.create(this, tone);
    		mp.start();
    	}
    	else {
    		try {
    			mp.reset();
				mp.setDataSource(this, tone);
				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
			} catch (SecurityException e) {
			} catch (IllegalStateException e) {
			} catch (IOException e) {
			}
    	}
	}
}
