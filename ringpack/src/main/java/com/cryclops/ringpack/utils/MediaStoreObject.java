package com.cryclops.ringpack.utils;

import android.net.Uri;

/**
 * An item in the MediaStore that we're interested in.
 */
public class MediaStoreObject {
    /**
     * The MediaStore.MediaColumn._ID
     */
    public long id;
    /**
     * The MediaStore.MediaColumns.DATA
     */
    public String data;

    /**
     * The MediaStore.MediaColumns.SIZE
     */
    public long size;

    /**
     * The MediaStore.MediaColumns.DISPLAY_NAME
     */
    public String displayName;

    /**
     * The unique URI for this row.
     */
    public Uri uri;

    public boolean isRingPackRow() {
        return data.contains("com.cryclops.ringpack");
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("_ID: ");
        b.append(id);
        b.append("\n");
        b.append("DATA: ");
        b.append(data);
        b.append("\n");
        b.append("SIZE: ");
        b.append(size);
        b.append("\n");
        b.append("DISPLAY_NAME: ");
        b.append(displayName);
        b.append("\n");
        b.append("URI: ");
        b.append(uri.toString());

        return b.toString();
    }
}
