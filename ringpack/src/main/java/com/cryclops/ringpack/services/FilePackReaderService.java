package com.cryclops.ringpack.services;

import android.content.Context;
import android.net.Uri;

import com.cryclops.ringpack.R;
import com.cryclops.ringpack.utils.ListUtils;
import com.cryclops.ringpack.utils.MediaStoreObject;
import com.cryclops.ringpack.utils.MediaStoreUtils;
import com.cryclops.ringpack.utils.PropertySelector;
import com.cryclops.ringpack.utils.RingtoneManagerUtils;
import com.cryclops.ringpack.utils.ServiceUtils;
import com.cryclops.ringpack.viewmodel.PackVm;
import com.cryclops.ringpack.viewmodel.RingActivityVm;
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
                try {
                    RingPackVm ringPackVm = new RingPackVm(infoFile);
                    packVms.add(ringPackVm);
                }
                catch (Exception ex) {
                    ServiceUtils.getNotification().showInfoDialog(
                            ServiceUtils.getResource().getString(R.string.info_dialog_title_parsing_error),
                            ex.getMessage()
                    );
                }
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
        // Get the current tone's URI
        Uri toneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);

        if (toneUri != null) {
            // Query the ContentProvider for it
            MediaStoreObject result = MediaStoreUtils.queryForRow(ctx, toneUri);

            if (result != null) {
                if (!result.data.contains("com.cryclops.ringpack")) {
                    // This isn't a RingPack tone, probably the user's tone
                    return null;
                }

                PackVm currentPackVm = null;
                final File tonePath = new File(result.data);

                if (packs != null) {
                    // To preserve pointers, look for an existing Pack rather than create a new one.
                    currentPackVm = ListUtils.firstOrDefault(packs, new PropertySelector<PackVm>() {
                        @Override
                        public boolean test(PackVm item) {
                            return item.setCurrentToneIfMatch(tonePath);
                        }
                    });

                    if (currentPackVm == null) {
                        // Hmm. The current ringtone is a RingPack tone, but we don't know about it.
                        // Disable RingPack so that the user gets a valid notification tone back
                        // and we clean up the MediaStore.
                        RingActivityVm.disableRingPack(ctx);
                        return null;
                    }
                }
                else {
                    // Caller would like us to create a new RingPack.
                    File rootPath = tonePath.getParentFile();
                    File infoFilePath = getInfoFile(rootPath);

                    if (infoFilePath != null) {
                        try {
                            currentPackVm = new RingPackVm(infoFilePath);
                            currentPackVm.setCurrentToneIfMatch(tonePath);
                        }
                        catch (Exception ex) {
                            // We failed to read it one way or another
                            ServiceUtils.getLog().exception(ex, false);

                            return null;
                        }
                    }
                    else {
                        // It's a RingPack tone with no info file. Get rid of it.
                        ServiceUtils.getLog().exception(new UnsupportedOperationException("Missing info file"), false);

                        RingActivityVm.disableRingPack(ctx);
                        return null;
                    }
                }

                currentPackVm.setIsSelected(true);

                return currentPackVm;
            }
            else {
                // It's a URI that's not in the MediaStore. If it's us, get rid of it.
                RingActivityVm.disableRingPack(ctx);
            }
        }

        return null;
    }
}
