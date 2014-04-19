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
}
