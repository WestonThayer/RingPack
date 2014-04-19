package com.cryclops.ringpack.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Easily work with files in the MediaStore.
 */
public class MediaStoreUtils {

    /**
     * ONLY CALL FROM API >= 11!
     *
     * Query MediaStore.Files external table for a single row matching the given DATA value.
     * @param ctx
     * @param data
     * @return
     */
    public static MediaStoreObject queryForFilesRingPackTone(Context ctx, String data) {
        if (Build.VERSION.SDK_INT >= 11) {
            return queryForRow(ctx, MediaStore.Files.getContentUri("external"), data);
        }
        else {
            throw new UnsupportedOperationException("Can't call this method if API < 11!");
        }
    }

    public static ArrayList<MediaStoreObject> queryForFilesRingPackTones(Context ctx) {
        if (Build.VERSION.SDK_INT >= 11) {
            return queryForRingPackTones(ctx, MediaStore.Files.getContentUri("external"));
        }
        else {
            throw new UnsupportedOperationException("Can't call this method if API < 11!");
        }
    }

    public static ArrayList<MediaStoreObject> queryForInternalRingPackTones(Context ctx) {
        return queryForRingPackTones(ctx, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    }

    public static ArrayList<MediaStoreObject> queryForExternalRingPackTones(Context ctx) {
        return queryForRingPackTones(ctx, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    private static MediaStoreObject queryForRow(Context ctx, Uri tableUri, String data) {
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA};
        String selection = MediaStore.MediaColumns.DATA + " = ?";
        String[] args = {data};
        ArrayList<MediaStoreObject> results = query(ctx, tableUri, projection, selection, args);

        if (results.size() == 1) {
            return results.get(0);
        }
        else if (results.size() == 0) {
            return null;
        }
        else {
            throw new IllegalArgumentException("DATA is not unique!");
        }
    }

    /**
     * Find a MediaStoreObject for the given row in the given table.
     * @param ctx
     * @param rowUri A table and row ID.
     * @return The object, if found - otherwise null.
     * @throws java.lang.IllegalArgumentException If there is more than one result for the row
     */
    public static MediaStoreObject queryForRow(Context ctx, Uri rowUri) {
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA};
        ArrayList<MediaStoreObject> results = query(ctx, rowUri, projection, null, null);

        if (results.size() == 1) {
            return results.get(0);
        }
        else if (results.size() == 0) {
            return null;
        }
        else {
            throw new IllegalArgumentException("rowUri is not unique!");
        }
    }

    /**
     * Query the given MediaStore table for all rows where DATA matches our package name.
     * @param ctx
     * @param tableUri
     * @return
     */
    private static ArrayList<MediaStoreObject> queryForRingPackTones(Context ctx, Uri tableUri) {
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA};
        String selection = MediaStore.MediaColumns.DATA + " LIKE ?";
        String[] args = {"%com.cryclops.ringpack%"};
        return query(ctx, tableUri, projection, selection, args);
    }

    private static ArrayList<MediaStoreObject> query(Context ctx, Uri tableUri, String[] projection, String selection, String[] args) {
        Cursor c = ctx.getContentResolver().query(tableUri, projection, selection, args, null);

        if (c == null) throw new UnsupportedOperationException("Query failed to return a Cursor!");

        ArrayList<MediaStoreObject> list = new ArrayList<MediaStoreObject>();

        while (c.moveToNext()) {
            MediaStoreObject o = new MediaStoreObject();

            int idIndex = c.getColumnIndex(MediaStore.MediaColumns._ID);
            o.id = c.getLong(idIndex);

            int dataIndex = c.getColumnIndex(MediaStore.MediaColumns.DATA);
            o.data = c.getString(dataIndex);

            o.uri = Uri.withAppendedPath(tableUri, Long.toString(o.id));

            list.add(o);
        }

        c.close();

        return list;
    }

    public static MediaStoreObject insertFiles(Context ctx, MediaStoreObject o) {
        if (Build.VERSION.SDK_INT >= 11) {
            return insert(ctx, MediaStore.Files.getContentUri("external"), o);
        }
        else {
            throw new UnsupportedOperationException("Can only be called from API >= 11!");
        }
    }

    public static MediaStoreObject insertExternal(Context ctx, MediaStoreObject o) {
        return insert(ctx, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, o);
    }

    private static MediaStoreObject insert(Context ctx, Uri tableUri, MediaStoreObject o) {
        ContentValues values = buildValues(o.data, o.size, o.displayName);

        Uri u = ctx.getContentResolver().insert(tableUri, values);

        if (u == null) {
            return null;
        }
        else {
            o.uri = u;
            return o;
        }
    }

    public static boolean update(Context ctx, MediaStoreObject o) {
        ContentValues values = buildValues(o.data, o.size, o.displayName);

        return ctx.getContentResolver().update(o.uri, values, null, null) == 1;
    }

    private static ContentValues buildValues(String data, long size, String displayName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, data);
        values.put(MediaStore.MediaColumns.SIZE, size);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg"); // consider application/ogg

        return values;
    }

    public static int deleteInternalRingPackTones(Context ctx) {
        return deleteRingPackTones(ctx, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    }

    public static int deleteExternalRingPackTones(Context ctx) {
        return deleteRingPackTones(ctx, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    private static int deleteRingPackTones(Context ctx, Uri tableUri) {
        String selection = MediaStore.MediaColumns.DATA + " LIKE ?";
        String[] args = {"%com.cryclops.ringpack%"};

        return ctx.getContentResolver().delete(tableUri, selection, args);
    }
}
