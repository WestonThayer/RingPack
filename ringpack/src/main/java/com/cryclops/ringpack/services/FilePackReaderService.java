package com.cryclops.ringpack.services;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.cryclops.ringpack.utils.ListUtils;
import com.cryclops.ringpack.utils.PropertySelector;
import com.cryclops.ringpack.utils.RingtoneManagerUtils;
import com.cryclops.ringpack.utils.ServiceUtils;
import com.cryclops.ringpack.viewmodel.PackVm;
import com.cryclops.ringpack.viewmodel.RingPackVm;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Reads Packs from whatever File path is supplied (SD card or local storage).
 */
public class FilePackReaderService implements PackReaderService {

    @Override
    public ArrayList<PackVm> readPacks(Context ctx, File packPath) {
        ArrayList<PackVm> packVms = new ArrayList<PackVm>();

        // All packs are directories
        File[] packDirs = packPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File packDir : packDirs) {
            File infoFile = getInfoFile(packDir);

            if (infoFile != null) {
                RingPackVm ringPackVm = new RingPackVm(infoFile);
                packVms.add(ringPackVm);
            }
            else {
                // yell at the user maybe, but just ignore the pack
                ServiceUtils.getLog().missingInfoFile();
            }
        }

        return packVms;
    }

    @Override
    public File getInfoFile(File rootPath) {
        // Determine if there's an info.txt file, otherwise ignore
        File[] infoFiles = rootPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals("info.txt");
            }
        });

        if (infoFiles.length == 1) {
            File infoFile = infoFiles[0];

            return infoFile;
        }
        else {
            return null;
        }
    }

    @Override
    public PackVm findEnabledPackVm(Context ctx, ArrayList<PackVm> packs) {
        PackVm currentPackVm = null;

        // Get the current tone's URI
        Uri toneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);

        if (toneUri != null) {
            // Query the ContentProvider for it
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor c = ctx.getContentResolver().query(
                    toneUri,
                    projection,
                    null,
                    null,
                    null
            );

            if (c.getCount() == 1) {
                c.moveToFirst();
                String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                if (!path.contains("com.cryclops.ringpack")) {
                    // This isn't a RingPack tone, probably the user's tone
                    return null;
                }

                final File tonePath = new File(path);

                if (packs != null) {
                    currentPackVm = ListUtils.firstOrDefault(packs, new PropertySelector<PackVm>() {
                        @Override
                        public boolean test(PackVm item) {
                            return item.setCurrentToneIfMatch(tonePath);
                        }
                    });
                }
                else {
                    File rootPath = tonePath.getParentFile();
                    File infoFilePath = getInfoFile(rootPath);

                    if (infoFilePath == null) {
                        throw new UnsupportedOperationException();
                    }

                    currentPackVm = new RingPackVm(infoFilePath);
                    currentPackVm.setCurrentToneIfMatch(tonePath);
                }

                currentPackVm.setIsSelected(true);
            } else {
                throw new UnsupportedOperationException();
            }

            c.close();
        }

        return currentPackVm;
    }
}
