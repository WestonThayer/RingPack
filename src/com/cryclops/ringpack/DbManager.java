package com.cryclops.ringpack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The DbManager class helps with managing RingPacks SQLite database.
 * 
 * @author Cryclops
 * @version 1.0
 * @version 2.0.0 converted everything to integers rather than longs
 *
 */
public class DbManager {
	
	private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_1 = "packs";
    private static final String DATABASE_TABLE_2 = "tones";
    private static final int DATABASE_VERSION = 2;
    
    private static final String TAG = "RingPackDbManager";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private final Context mCtx;

	//for the "packs" table
    public static final String pKEY_ROWID = "_id";
    public static final String pKEY_NAME = "name";
    public static final String pKEY_LOCATION = "location";
    public static final String pKEY_ACTIVE = "active";
    public static final String pKEY_SIZE = "size";
    
    private static final String pDATABASE_CREATE = "create table " +
    	DATABASE_TABLE_1 + " (" +
    	pKEY_ROWID + " integer primary key autoincrement, " +
    	pKEY_NAME + " text not null, " +
    	pKEY_LOCATION + " text not null, " +
    	pKEY_ACTIVE + " integer not null, " +
    	pKEY_SIZE + " integer not null);";
    
    //for the "tones" table
    public static final String tKEY_ROWID = "_id";
    public static final String tKEY_PACKID = "pack_id";
    public static final String tKEY_NAME = "name";
    public static final String tKEY_FILENAME = "filename";
    public static final String tKEY_ENABLED = "enabled";
    
    private static final String tDATABASE_CREATE = "create table " +
		DATABASE_TABLE_2 + " (" +
		tKEY_ROWID + " integer primary key autoincrement, " +
		tKEY_PACKID + " text not null, " +
		tKEY_NAME + " text not null, " +
		tKEY_FILENAME + " text not null, " +
		tKEY_ENABLED + " integer not null);";
    
    ////////////////////////////////begin private classes//////////////////////
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(pDATABASE_CREATE);
            db.execSQL(tDATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
        		int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_1);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_2);
            onCreate(db);
        }
    }
    ////////////////////////////////end private classes////////////////////////
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbManager(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbManager open() throws SQLException {
        dbHelper = new DatabaseHelper(mCtx);
        db = dbHelper.getWritableDatabase();
        return this;
    }
    
    /**
     * The close method just releases this access to the database.
     */
    public void close() {
        dbHelper.close();
    }
    
    /**
     * The deleteAll method will delete both tables from our database.
     */
    public void deleteAll() {
    	db.delete(DATABASE_TABLE_1, null, null);
    	db.delete(DATABASE_TABLE_2, null, null);
    	dbHelper.close();
    }
    
    /**
     * Create a new pack using the name and location provided. If the pack is
     * successfully created, return the new rowId for that pack, otherwise
     * return a -1 to indicate failure.
     * 
     * @param name		A pack name
     * @param location	The location on disk,
     * 						e.g. /sdcard/Android/data/your.package.name/
     * @return			rowId or -1 if failed
     */
    public int createPack(String name, String location, boolean active,
    		int size) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(pKEY_NAME, name);
    	initialValues.put(pKEY_LOCATION, location);
    	
    	int b = active ? 1 : 0;
    	initialValues.put(pKEY_ACTIVE, b);
    	initialValues.put(pKEY_SIZE, size);
    	
    	return (int) db.insert(DATABASE_TABLE_1, null, initialValues);
    }
    
    /**
     * Create a new tone using the packid, filename, and status provided. If the
     * tone is successfully created, return the new rowId for that tone,
     * otherwise return a -1 to indicate failure.
     * 
     * @param packid	The rowId of the tone's parent pack
     * @param filename	The name of the tone, e.g. sound.mp3
     * @param enabled	True if the tone is currently enabled, false if else
     * @return			rowId or -1 if failed
     */
    public int createTone(long packid, String name, String filename,
    		boolean enabled) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(tKEY_PACKID, packid);
    	initialValues.put(tKEY_NAME, name);
    	initialValues.put(tKEY_FILENAME, filename);
    	
    	int b = enabled ? 1 : 0;
    	initialValues.put(tKEY_ENABLED, b);
    	
    	return (int) db.insert(DATABASE_TABLE_2, null, initialValues);
    }
    
    /**
     * The deletePack method will delete the pack at the given row ID.
     * 
     * @param rowId		The rowId of the pack to be deleted.
     * @return			True if successful, false if else.
     */
    public boolean deletePack(int rowId) {
    	//delete all tones belonging to this pack
    	int tonesDel = db.delete(DATABASE_TABLE_2, tKEY_PACKID + "=" + rowId,
    			null);
    	if (tonesDel < 1)
    		return false;
    	
    	//now delete the pack
    	return db.delete(DATABASE_TABLE_1, pKEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all packs in the database.
     * 
     * @return	Cursor over all packs
     */
    public Cursor fetchAllPacks() {
    	String[] s = new String[] {pKEY_ROWID, pKEY_NAME, pKEY_LOCATION,
    			pKEY_ACTIVE, pKEY_SIZE};
    	
    	return db.query(DATABASE_TABLE_1, s, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor over the list of all tones in a specific pack.
     * 
     * @param rowId		The rowId of the pack wanted
     * @return			Cursor of all pack tones
     */
    public Cursor fetchPackTones(int rowId) {
    	String[] s = new String[] {tKEY_ROWID, tKEY_NAME, tKEY_PACKID,
    			tKEY_FILENAME, tKEY_ENABLED};
    	
    	return db.query(DATABASE_TABLE_2, s, tKEY_PACKID + "=" + rowId, null,
    			null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the pack that matches the given rowID.
     * 
     * @param rowId		The rowId of the pack to retrieve
     * @return			Cursor positioned to matching pack, if found
     * @throws SQLException
     */
    public Cursor fetchPack(int rowId) throws SQLException {
    	String[] s = new String[] {pKEY_ROWID, pKEY_NAME, pKEY_LOCATION,
    			pKEY_ACTIVE, pKEY_SIZE};
    	
    	Cursor c = db.query(true, DATABASE_TABLE_1, s, pKEY_ROWID + "=" +
    			rowId, null, null, null, null, null);
    	
    	if (c != null)
    		c.moveToFirst();
    	return c;
    }
    
    /**
     * The fetchPack method finds a pack who's name matches the input.
     * 
     * @param path		The name of the pack (full path).
     * @return			Returns a Cursor positioned at matching pack, if found.
     * @throws SQLException
     */
    public Cursor fetchPack(String path) throws SQLException {
    	String[] s = new String[] {pKEY_ROWID, pKEY_NAME, pKEY_LOCATION,
    			pKEY_ACTIVE, pKEY_SIZE};
    	
    	Cursor c = db.query(true, DATABASE_TABLE_1, s, pKEY_LOCATION + "='" +
    			path + "'", null, null, null, null, null);
    	
    	if (c != null)
    		c.moveToFirst();
    	
    	return c;
    }
    
    /**
     * Return a Cursor positioned at the tone that matches the given rowID.
     * 
     * @param rowId		The rowId of the tone to retrieve
     * @return			Cursor positioned to matching pack, if found
     * @throws SQLException
     */
    public Cursor fetchTone(int rowId) throws SQLException {
    	String[] s = new String[] {tKEY_ROWID, tKEY_NAME, tKEY_PACKID,
    			tKEY_FILENAME, tKEY_ENABLED};
    	
    	Cursor c = db.query(true, DATABASE_TABLE_2, s, tKEY_ROWID + "=" +
    			rowId, null, null, null, null, null);
    	
    	if (c != null)
    		c.moveToFirst();
    	return c;
    }
    
    /**
     * Update the pack's active status.
     * 
     * @param rowId		The rowId of the pack to update
     * @param active	True if active, false if else
     * @return			True if the tone was updated, false if else
     */
    public boolean updatePack(int rowId, boolean active) {
    	ContentValues args = new ContentValues();
    	
    	int b = active ? 1 : 0;
    	args.put(pKEY_ACTIVE, b);
    	
    	return db.update(DATABASE_TABLE_1, args, pKEY_ROWID + "=" + rowId,
    			null) > 0;
    }
    
    /**
     * Update the tone's enabled status.
     * 
     * @param rowId		The rowId of the tone to update
     * @param enabled	True if enabled, false if else
     * @return			True if the tone was updated, false if else
     */
    public boolean updateTone(int rowId, boolean enabled) {
    	ContentValues args = new ContentValues();
    	
    	int b = enabled ? 1 : 0;
    	args.put(tKEY_ENABLED, b);
    	
    	return db.update(DATABASE_TABLE_2, args, tKEY_ROWID + "=" + rowId,
    			null) > 0;
    }
}