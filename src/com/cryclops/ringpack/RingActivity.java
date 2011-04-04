package com.cryclops.ringpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.cryclops.ringpack.actionitems.ActionItem;
import com.cryclops.ringpack.actionitems.QuickAction;

/**
 * The RingActivity class is an Activity that allows the user to change their
 * current pack, scan for new packs, see more information, change settings,
 * and get more packs.
 * 
 * The basic chain of calls is:
 * 	onCreate
 * 	checkSd
 * 	postSd (thread)
 * 	layout (thread)
 * 
 * @author Cryclops
 * @version 2.0.0
 * @version 2.0.1
 * 				Fixed the Get More! button to base on publisher
 * @version 2.0.2
 * 				Moved the about dialog code to show on an update
 * @versino 2.1.0
 * 				Utilities class, no more having to shut off all the time, no
 * more about dialog on scanning.
 *
 */
public class RingActivity extends Activity {
	
	public static final String PACK_PATH = "/sdcard/Android/data/com.cryclops.ringpack/packs/";
	public static final String FIRST_RUN = "FIRST_RUN_PREF";
	public static final String NEW_PACK_INSTALL = "com.cryclops.ringpack.NEW_PACK_INSTALL";
	
	private DbManager db;
	
	private AlertDialog.Builder mBuilder, mBuilder2;
	private ProgressDialog progressDialog;
	private Dialog dialog;
	
	private int currentPackId, pid;
	private RadioGroup installedPacks;
	private Button getMoreButton;
	private boolean settingUp, firstRunOverride, showAboutOverride;
	private QuickAction qa;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ringactivity);
        
        //open up our database
        db = new DbManager(this);
        db.open();
        
        firstRunOverride = false;
        showAboutOverride = false;
        
        //check to see if an installed pack started us
        if (getIntent().getBooleanExtra(NEW_PACK_INSTALL, false)) {
        	setPackToNone();
        	firstRunOverride = true;
        }
        
        settingUp = true;
        
        mBuilder = new AlertDialog.Builder(this);
        mBuilder2 = new AlertDialog.Builder(this);
        
        //check the status of our SD card to see if we may continue
        checkSd();
    }
    
    /**
     * We continue here if the SD check passes. If we should move over our packs
     * and rebuild our database, we spawn the firstRun thread. If not, we move
     * straight to the layout thread.
     */
    private void postSd() {       	
        //check to see if we need to make included pack copy    	
        if (firstRunOverride || isFirstRun()) {
        	//display the about dialog if necessary
            SharedPreferences prefs = Utilities.getPrefs(getBaseContext());
            if (showAboutOverride || prefs.getBoolean(RingService.SHOW_ABOUT, true)) {
            	showAboutDialog();
            	showAboutOverride = false;
            }
        	
        	progressDialog = ProgressDialog.show(this,
            		getString(R.string.firstRunProgressTitle),
            		getString(R.string.firstRunProgressContents), true, false);
            Thread firstRunThread = new Thread() {
            	@Override
            	public void run() {
            		//move over the included pack, establish our folders
            		initPaths();
            		
            		//build our database
            		scanSd();
            		
            		firstRunHandler.sendEmptyMessage(0);
            	}
            };
            firstRunThread.start();
        }
        else {
        	initLayout();
        }
    }
    
    private Handler firstRunHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			
			initLayout();
		}
	};
    
    /**
     * The private initLayout method builds the layout off of the packs found
     * in the database. It does this in a thread so as to avoid ANR timeouts.
     */
    private void initLayout() {
    	installedPacks = (RadioGroup) RingActivity.this.findViewById(R.id.installedPacks);
    	installedPacks.removeAllViews();
    	
    	installedPacks.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Cursor c = db.fetchPack(checkedId);
				RingActivity.this.startManagingCursor(c);
				c.moveToFirst();
				
				int nameCol = c.getColumnIndex(DbManager.pKEY_NAME);
				String name = c.getString(nameCol);
				
				c.close();
				RingActivity.this.stopManagingCursor(c);
				
				db.updatePack(currentPackId, false);
				currentPackId = checkedId;
				db.updatePack(currentPackId, true);
				
				//now decide whether to enable or disable
				if (!settingUp && name.equals(RingActivity.this.getString(R.string.nonePack))) {							
					Intent i = new Intent(RingActivity.this, RingService.class);
					RingActivity.this.stopService(i);
					
					Utilities.setEnabled(getBaseContext(), false);
					
					Toast.makeText(RingActivity.this.getBaseContext(),
							RingActivity.this.getString(R.string.ringPackDisabled),
							Toast.LENGTH_SHORT).show();
				}
				else if (!settingUp) {
					Intent i = new Intent(RingActivity.this, RingService.class);
					i.putExtra(RingService.ACTION, RingService.PACK_SET);
					i.putExtra(RingService.PASSED_PACK, currentPackId);
					RingActivity.this.startService(i);
				}
			}
        });
    	
    	progressDialog = ProgressDialog.show(this,
        		getString(R.string.layoutProgressTitle),
        		getString(R.string.layoutProgressContents), true, false);
    	
    	Thread layoutThread = new Thread() {
    		@Override
    		public void run() {   			
    			Cursor c = db.fetchAllPacks();
    	        RingActivity.this.startManagingCursor(c);
    	        c.moveToFirst();
    	        
    	        int idCol = c.getColumnIndex(DbManager.pKEY_ROWID);
    	        int nameCol = c.getColumnIndex(DbManager.pKEY_NAME);
    	        int activeCol = c.getColumnIndex(DbManager.pKEY_ACTIVE);
    	        
    	        for (int i = 0; i < c.getCount(); i++) {
    	        	RadioButton r = (RadioButton) RadioButton.inflate(RingActivity.this,
    	        			R.layout.radio_pack, null);
    	        	int id = c.getInt(idCol);
    	        	String name = c.getString(nameCol);
    	        	r.setId(id);
    	        	r.setText(name);
    	        	if (c.getInt(activeCol) == 1) {
    	        		r.setChecked(true);
    	        		currentPackId = id;
    	        	}
    	        	
    	        	//only allow deletions of packs other than none
    	        	if (!name.equals(getString(R.string.nonePack)) &&
    	        			!name.equals(getString(R.string.malletPack))) {
	    	        	r.setOnLongClickListener(new OnLongClickListener() {
	
							@Override
							public boolean onLongClick(View v) {
								qa = new QuickAction(v);
								
								//for edit
								ActionItem editAi = new ActionItem();
								
								editAi.setTitle(getString(R.string.aiEdit));
								editAi.setIcon(getResources().getDrawable(R.drawable.edit_contents_icon));
								editAi.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										if (qa != null)
											qa.dismiss();
										handlePackEdit(((View) v.getParent()).getId());
									}
								});
								
								//for delete
								ActionItem ai = new ActionItem();
								
								ai.setTitle(getString(R.string.aiDelete));
								ai.setIcon(getResources().getDrawable(R.drawable.delete_icon));
								ai.setOnClickListener(new OnClickListener() {
	
									@Override
									public void onClick(View v) {
										if (qa != null)
											qa.dismiss();
										handlePackDelete(((View) v.getParent()).getId());
									}
								});
								
								qa.addActionItem(editAi);
								qa.addActionItem(ai);
								qa.show();
								
								return true;
							}
	    	        	});
    	        	}
    	        	
    	        	Message msg = Message.obtain();
    	        	msg.obj = r;
    	        	radioHandler.sendMessage(msg);
    	        	
    	        	c.moveToNext();
    	        }
    	        
    	        c.close();
    	        RingActivity.this.stopManagingCursor(c);
    			
    			layoutHandler.sendEmptyMessage(0);
    		}
    	};
    	layoutThread.start();
    }
    
    /**
     * Necessary for adding view's in the UI thread.
     */
    private Handler radioHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		installedPacks.addView((RadioButton) msg.obj);
    	}
    };
    
    /**
     * settingUp ensures that we don't accidentally think that the checked
     * RadioButton is changing during set-up.
     */
    private Handler layoutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//The bottom "Get More!" button
	        getMoreButton = (Button) RingActivity.this.findViewById(R.id.getMore);
	        getMoreButton.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	Intent i = new Intent(Intent.ACTION_VIEW,
	                		Uri.parse("market://search?q=RingPack"));
	            	i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	                RingActivity.this.startActivity(i);
	            }
	        });
			
			progressDialog.dismiss();
			
			settingUp = false;
		}
	};
    
    @Override
    public void onDestroy() {
    	if (db != null)
    		db.close();
    	super.onDestroy();
    }
    
    //Options Menu/////////////////////////////////////////////////////////////
    public static final int MENU_SCAN = 0;
    public static final int MENU_ABOUT = 1;
    public static final int MENU_SETTINGS = 2;
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu){
    	menu.add(0, MENU_SCAN, 0, getString(
    			R.string.optionScan)).setIcon(R.drawable.ic_menu_refresh);
    	menu.add(0, MENU_ABOUT, 0, getString(
    			R.string.optionAbout)).setIcon(R.drawable.about_icon);
		menu.add(0, MENU_SETTINGS, 0, getString(
				R.string.optionSettings)).setIcon(R.drawable.settings);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    	case MENU_SCAN:
    		setPackToNone();
    		firstRunOverride = true;
    		settingUp = true;
    		postSd();
    		return true;
    	case MENU_ABOUT:
    		showAboutDialog();
        	return true;
    	case MENU_SETTINGS:
    		Intent settingsActivity = new Intent(getBaseContext(),
    				Preferences.class);
    		startActivity(settingsActivity);
    		return true;
    	default:
    		return false;
    	}
    }
    
    //Begin private helper methods/////////////////////////////////////////////
    /**
     * The private checkSd method displays an error dialog if the SD card is not
     * present before continuing with postSd().
     */
    private void checkSd() {
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY) ||
    			status.equals(Environment.MEDIA_SHARED) ||
    			status.equals(Environment.MEDIA_REMOVED) ||
    			status.equals(Environment.MEDIA_UNMOUNTED)) {
    		mBuilder.setTitle(getString(R.string.sdError));
    		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
    		mBuilder.setMessage(getString(R.string.sdErrorContents));
    		mBuilder.setCancelable(false);
    		mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                RingActivity.this.finish();
    	           }
    	    	});
    	    	mBuilder.show();
    	}
    	else {
    		postSd();
    	}
    }
    	
    /**
     * The private isFirstRun method checks to see if this is a new install or
     * if we've been updated.
     * 
     * @return	True if they have not, false if they have.
     */
	private boolean isFirstRun() {
		showAboutOverride = true;
		
		int vC = 0;
		try {
			vC = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			//
		}
		
    	SharedPreferences prefs = Utilities.getPrefs(getBaseContext());
    	int oldVc = prefs.getInt(FIRST_RUN, -1);
    	
    	if (vC > oldVc) {
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putInt(FIRST_RUN, vC);
    		editor.commit();
    		
    		return true;
    	}
    	else {
    		return false;
    	}
    }
	
	/**
     * initPaths is a private helper method to instantiate the necessary
     * directories and files if they are not already in existence, as well as
     * copying over our included sample packs.
     */
    private void initPaths() {
    	//create the directories as well as our "None" option
    	boolean exists = (new File(PACK_PATH)).exists();
        if (!exists)
            new File(PACK_PATH).mkdirs();
        
        //move over the first sample pack if it's not already there
        String filename1[] = null;
        
        exists = (new File(PACK_PATH + "mallet_pack/")).exists();
        if (!exists) {
        	new File(PACK_PATH + "mallet_pack/").mkdirs();
        	
        	filename1 = new String[] {"info.txt", "mallet_1.ogg",
        			"mallet_2.ogg", "mallet_3.ogg", "mallet_4.ogg"};
        	int resource[] = {R.raw.info1, R.raw.mallet_1,
        			R.raw.mallet_2, R.raw.mallet_3, R.raw.mallet_4};
        	
        	for (int i = 0; i < filename1.length; i++) {
        		byte[] buffer = null;
    	        InputStream fIn = getBaseContext().
    	        	getResources().openRawResource(resource[i]);
    	        int size = 0;
    	
    	        try {
    	             size = fIn.available();
    	             buffer = new byte[size];
    	             fIn.read(buffer);
    	             fIn.close();
    	        } catch (IOException e) {
    	        	Log.e(RingService.TAG, "IOException: fIn not found!");
    	        }
    	        
    	        FileOutputStream save;
    	        try {
    	             save = new FileOutputStream(PACK_PATH +
    	            		 "mallet_pack/" + filename1[i]);
    	             save.write(buffer);
    	             save.flush();
    	             save.close();
    	        } catch (FileNotFoundException e) {
    	        	Log.e(RingService.TAG, "File not found when copying over a pack");
		        }
		        catch (IOException e) {
		        	Log.e(RingService.TAG, "Unknown IOException copying over a pack.");
		        }
        	}
        }
        
        try {
			new File(PACK_PATH + "mallet_pack/.nomedia").createNewFile();
		} catch (IOException e) {
			Log.e(RingService.TAG, "creation of .nomedia file has failed!");
		}
    }
    
    /**
     * The private scanSD method loops through all sub-directories within the
     * pack folder, adding new packs as they become available. It will not
     * touch existing packs that have already been created in the database.
     */
    private void scanSd() {
    	//first ready the database for rebuilding
    	db.deleteAll();
    	db.open();
    	
    	//add the "None" pack
    	db.createPack(getString(R.string.nonePack), "none", true, 0);
    	
    	File dir = new File(PACK_PATH);
    	String[] subFolders = dir.list();
    	
    	for (int i = 0; i < subFolders.length; i++) {
    		String path = PACK_PATH + subFolders[i] + "/";
    		Cursor c = db.fetchPack(path);
    		
    		//if the pack hasn't yet been added, parse the info.txt file and
    		//add it
    		if (c.getCount() == 0) {
    			try {
    				BufferedReader in = new BufferedReader(new FileReader(path +
					"info.txt"));
					
					String name = in.readLine();
					int size = Integer.parseInt(in.readLine());
					
					//create our pack entry
					int packId = db.createPack(name, path, false, size);
					
					//create our tone entries
					for (int j = 0; j < size; j++) {
						StringTokenizer tok = new StringTokenizer(in.readLine(),
								"\\|");
						
						String filename = tok.nextToken();
						String songName = tok.nextToken();
						
						db.createTone(packId, songName, filename, true);
					}
					
					in.close();
				} catch (FileNotFoundException e) {
					//info.txt does not exist
					mBuilder.setTitle(getString(R.string.scanInfoError));
		    		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		    		mBuilder.setMessage(getString(R.string.scanInfoErrorContents) + path);
		    		mBuilder.setCancelable(false);
		    		mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                RingActivity.this.finish();
		    	           }
		    	    	});
		    	    	mBuilder.show();
				}
				catch (IOException e) {
					//size probably doesn't match up
					mBuilder.setTitle(getString(R.string.scanSizeError));
		    		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		    		mBuilder.setMessage(getString(R.string.scanSizeErrorContents) + path);
		    		mBuilder.setCancelable(false);
		    		mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	                RingActivity.this.finish();
		    	           }
		    	    	});
		    	    	mBuilder.show();
				}
				finally {
					if (c != null)
		    			c.close();
				}
    		}
    	}
    }
    
    private void handlePackDelete(int packId) {
    	pid = packId;
    	
    	//confirm delete
		mBuilder2.setTitle(getString(R.string.deleteConfirmDialog));
		mBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
		mBuilder2.setMessage(getString(R.string.deleteConfirmDialogContents));
		mBuilder2.setCancelable(true);
		mBuilder2.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   handlePackDelete2(pid);
	           }
	    	});
		mBuilder2.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   //
	           }
	    	});
	    mBuilder2.show();
    }
        
    /**
     * The private handlePackDelete method disables RingService, then deletes
     * the specified pack from the SD card.
     * 
     * @param packId	The Integer packId to delete
     */
    private void handlePackDelete2(int packId) {
    	pid = packId;
    	
    	progressDialog = ProgressDialog.show(this,
        		getString(R.string.deleteProgressTitle),
        		getString(R.string.deleteProgressContents), true, false);
    	
    	Thread packDeleteThread = new Thread() {
    		@Override
    		public void run() {
    			setPackToNone();
    			
    			Cursor c = db.fetchPack(pid);
    			c.moveToFirst();
    			
    			int pathCol = c.getColumnIndex(DbManager.pKEY_LOCATION);
    			String path = c.getString(pathCol);
    			
    			c.close();
    			
    			File dir = new File(path);
    			int returnCode = 0;
    			Message msg = Message.obtain();
    			if (dir.exists()) {
    				returnCode = (deleteDirectory(dir)) ? 0 : 2;
    				
    				msg.arg1 = returnCode;
    				packDeleteHandler.sendMessage(msg);
				}
    			else {
    				returnCode = 1;
    				msg.arg1 = returnCode;
    				packDeleteHandler.sendMessage(msg);
    			}
    		}
    	};
    	packDeleteThread.run();
    }
    
    /**
     * Displays errors or rescans.
     */
    private Handler packDeleteHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		progressDialog.dismiss();
    		
    		if (msg.arg1 == 1) {
    			//deletion failed
    			mBuilder.setTitle(getString(R.string.deleteError));
	    		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
	    		mBuilder.setMessage(getString(R.string.deleteErrorContents));
	    		mBuilder.setCancelable(false);
	    		mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   RingActivity.this.finish();
	    	           }
	    	    	});
	    	    	mBuilder.show();
    		}
    		else if (msg.arg1 == 2) {
    			//deletion failed
    			mBuilder.setTitle(getString(R.string.deleteError));
	    		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
	    		mBuilder.setMessage(getString(R.string.deleteErrorContents2));
	    		mBuilder.setCancelable(false);
	    		mBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   RingActivity.this.finish();
	    	           }
	    	    	});
	    	    	mBuilder.show();
    		}
    		else {
				firstRunOverride = true;
				settingUp = true;
	    		postSd();
    		}
    	}
    };
    
    /**
     * The private deleteDirectory method recursively deletes a directory and
     * its contents since Java doesn't allow for delete() to be called on a
     * non-empty directory.
     * 
     * @param path	The directory to start with
     * @return		Returns true if it succeeds
     */
    private boolean deleteDirectory(File path) {
        if( path.exists() ) {
          File[] files = path.listFiles();
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
               deleteDirectory(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() );
      }
    
    /**
     * The private setPackToNone method disables RingService and brings us back
     * to our near-install state.
     */
    private void setPackToNone() {
    	/*Cursor c = db.fetchPack("none");
		startManagingCursor(c);
		c.moveToFirst();
		
		int idCol = c.getColumnIndex(DbManager.pKEY_ROWID);
		int id = c.getInt(idCol);
		
		c.close();
		stopManagingCursor(c);*/
		
		db.updatePack(currentPackId, false);
		db.updatePack(0, true);
		
		Intent i = new Intent(RingActivity.this, RingService.class);
		RingActivity.this.stopService(i);
		
		Utilities.setEnabled(getBaseContext(), false);
		
		Toast.makeText(RingActivity.this.getBaseContext(),
				RingActivity.this.getString(R.string.ringPackDisabled),
				Toast.LENGTH_SHORT).show();
    }
    
    /**
     * The private showAboutDialog method displays the welcome/about/help
     * dialog.
     */
    private void showAboutDialog() {	    	
	    dialog = new Dialog(RingActivity.this);
	    dialog.setContentView(R.layout.about_dialog);
	    dialog.setTitle(getString(R.string.aboutDialog));
	    dialog.setCancelable(true);
	    
	    
	    SharedPreferences prefs = Utilities.getPrefs(getBaseContext());
	    CheckBox c = (CheckBox) dialog.findViewById(R.id.aboutCheckBox);
	    c.setChecked(prefs.getBoolean(RingService.SHOW_ABOUT, true));
	    c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor =
					Utilities.getPrefs(RingActivity.this.getBaseContext()).edit();
				
				editor.putBoolean(RingService.SHOW_ABOUT, isChecked);
				editor.commit();
			}
	    });
	    
	    Button b = (Button) dialog.findViewById(R.id.aboutOkButton);
	    b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
	    });
	    Button b1 = (Button) dialog.findViewById(R.id.aboutWebButton);
	    b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW,
                		Uri.parse("http://cryclops.com/apps/ringpack/"));
                RingActivity.this.startActivity(i);
			}
	    });
	    dialog.show();
    }
    
    /**
     * The private handlePackEdit method has been changed not disable.
     * 
     * @param id
     */
    private void handlePackEdit(int id) {
    	/*if (id == currentPackId)
    		setPackToNone();*/
    	Intent i = new Intent(this, RingEditor.class);
    	i.putExtra(RingService.PASSED_PACK, id);
    	startActivity(i);
    }
}