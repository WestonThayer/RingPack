package com.cryclops.ringpack.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helpers for dealing with Files.
 */
public class FileUtils {

    /**
     * Copies a File. The File can be a directory or a single file, the method works recursively.
     * @param sourceLocation The current location of the file
     * @param targetLocation The desired copy location
     * @throws IOException
     */
    public static void copy(File sourceLocation, File targetLocation)
        throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();

            for (String child : children) {
                copy(new File(sourceLocation, child), new File(targetLocation, child));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    /**
     * Recursively delete a directory and all it's contents.
     * @param location A directory to delete
     * @return True if delete, false if there was an error
     */
    public static boolean deleteAll(File location) {
        if (location.isDirectory()) {
            for (File file : location.listFiles()) {
                if (!deleteAll(file)) {
                    return false;
                }
            }
        }

        if (!location.delete()) {
            return false;
        }

        return true;
    }

    /**
     * Checks to see if the SD card is good to go. Notifies the user if it's not.
     * @return True if we can read/write to it
     */
    public static boolean isExternalStorageWritable() {
        boolean isWritable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        return isWritable;
    }
}
