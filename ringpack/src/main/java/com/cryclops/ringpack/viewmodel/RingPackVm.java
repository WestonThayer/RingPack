package com.cryclops.ringpack.viewmodel;

import android.content.Context;
import android.media.Ringtone;

import com.cryclops.ringpack.R;
import com.cryclops.ringpack.model.RingPack;
import com.cryclops.ringpack.model.Tone;
import com.cryclops.ringpack.utils.ArrayUtils;
import com.cryclops.ringpack.utils.FileUtils;
import com.cryclops.ringpack.utils.ListUtils;
import com.cryclops.ringpack.utils.PropertySelector;
import com.cryclops.ringpack.utils.RingtoneManagerUtils;
import com.cryclops.ringpack.utils.ServiceUtils;
import com.cryclops.ringpack.utils.SharedPrefUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * RingPack View Model.
 */
public class RingPackVm implements PackVm {

    private RingPack ringPack;

    private boolean isSelected;

    public boolean isSelected() { return isSelected; }
    public void setIsSelected(boolean value) {
        if (isSelected != value) {
            isSelected = value;
        }
    }

    /**
     * Parses a new RingPackVm based on the pack descriptor file with this format:
     *
     * Example Pack Name
     * 2
     * tone_1.ogg|First Tone Name
     * tone_2.ogg|Second Tone Name
     *
     * See res/raw/info1.txt for an example.
     *
     * Performs disk IO. Consider running this on another thread.
     *
     * @param descriptor The info.txt file for the RingPack
     * @throws java.io.IOException Disk operations could fail
     */
    public RingPackVm(File descriptor) throws IOException {
        isSelected = false;

        BufferedReader in = new BufferedReader(new FileReader(descriptor));

        String name = in.readLine();

        if (name == null) {
            String message = ServiceUtils.getResource().getString(
                    R.string.info_dialog_content_info_missing_name,
                    descriptor.getPath());

            throw new UnsupportedOperationException(message);
        }

        in.readLine(); // Throw away the size line, if there is one

        ringPack = new RingPack(name, descriptor.getParent());

        // Get list of tone files
        File rootDir = new File(descriptor.getParent());
        File[] tf = rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".ogg");
            }
        });
        final ArrayList<File> toneFiles = ArrayUtils.toArrayList(tf);

        // Read the file for a list of tones with names
        ArrayList<TempTone> tempToneList = new ArrayList<TempTone>();

        String line = in.readLine();
        while (line != null) {
            StringTokenizer tok = new StringTokenizer(line, "\\|");

            String filename = null;
            String songName = null;

            if (tok.hasMoreTokens()) {
                filename = tok.nextToken();
            }
            if (tok.hasMoreTokens()) {
                songName = tok.nextToken();
            }

            if (filename != null && songName != null) {
                tempToneList.add(new TempTone(filename, songName));
            }

            line = in.readLine();
        }

        in.close(); // All done reading the file

        if (toneFiles.size() != tempToneList.size()) {
            // We don't actually care, the users don't have to name every tone
        }

        // Start making the full list, starting with the TempTones to maintain the order the
        // author intended.
        for (final TempTone tempTone : tempToneList) {
            File match = ListUtils.firstOrDefault(toneFiles, new PropertySelector<File>() {
                @Override
                public boolean test(File item) {
                    boolean isEnabled = !item.getName().endsWith(".disabled.ogg");
                    String trueFileName = isEnabled ? item.getName() : item.getName().replace(".disabled.ogg", ".ogg");

                    return trueFileName.equals(tempTone.filename);
                }
            });

            if (match != null) {
                boolean isEnabled = !match.getName().endsWith(".disabled.ogg");

                ringPack.getTones().add(new Tone(isEnabled, tempTone.name, match));
                toneFiles.remove(match); // All done with it now
            }
        }

        // Loop through what's left
        for (File toneFile : toneFiles) {
            boolean isEnabled = !toneFile.getName().endsWith(".disabled.ogg");

            String toneName = isEnabled ? toneFile.getName() : toneFile.getName().replace(".disabled.ogg", ".ogg");
            ringPack.getTones().add(new Tone(isEnabled, toneName, toneFile));
        }
    }

    /**
     * Just a quick package to help with building a tone list. Quickly thrown away.
     */
    private class TempTone implements Serializable {

        public String filename;
        public String name;

        public TempTone(String filename, String name) {
            this.filename = filename;
            this.name = name;
        }
    }

    /**
     * Getter for the Pack's name.
     * @return
     */
    public String getName() {
        return ringPack.getName();
    }

    /**
     * Getter for the Pack's root path on the SD card.
     * @return
     */
    public File getRootPath() { return ringPack.getRootPath(); }

    /**
     * Copies the RingPack from its original SD card location to a new one. For example, if this
     * @param destinationPath The root pack folder
     * @return The new root path, or null on failure
     */
    public File tryCopy(File destinationPath) {
        try {
            File newRootPath = new File(destinationPath, ringPack.getRootPath().getName());

            FileUtils.copy(ringPack.getRootPath(), newRootPath);
            return newRootPath;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean setCurrentToneIfMatch(final File absolutePath) {
        Tone match = ListUtils.firstOrDefault(ringPack.getTones(), new PropertySelector<Tone>() {
            @Override
            public boolean test(Tone item) {
                return item.getPathFile().equals(absolutePath);
            }
        });

        if (match != null) {
            ringPack.setCurrentTone(match);
            return true;
        }
        else {
            ringPack.setCurrentTone(null);
            return false;
        }
    }

    @Override
    public void clearCurrentTone() {
        ringPack.setCurrentTone(null);
    }

    @Override
    public void moveToNextTone(Context ctx) {
        if (ringPack.getCurrentTone() == null) {
            // Set to the first enabled tone and play it
            ringPack.setCurrentTone(findNextEnabledTone(0));
            ringPack.getCurrentTone().setDefaultNotificationRingtone(ctx);

            Ringtone r = RingtoneManagerUtils.getDefaultNotificationRingtone(ctx);

            if (r != null) {
                r.play();
            }
            else {
                // Why the heck did we fail to set the ringtone
                ServiceUtils.getLog().badRingtoneUri(RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx));

                RingActivityVm.disableRingPack(ctx);
            }
        }
        else {
            int curIndex = ringPack.getTones().indexOf(ringPack.getCurrentTone());

            switch (SharedPrefUtils.getRotationMode(ctx)) {
                case SharedPrefUtils.MODE_NORMAL:
                    if (curIndex < ringPack.getTones().size() - 1) {
                        ringPack.setCurrentTone(findNextEnabledTone(++curIndex));
                    }
                    else {
                        ringPack.setCurrentTone(findNextEnabledTone(0));
                    }

                    break;
                case SharedPrefUtils.MODE_SHUFFLE:
                    int randIndex = curIndex;

                    while (randIndex == curIndex || !ringPack.getTones().get(randIndex).isEnabled()) {
                        randIndex = (int) Math.floor(Math.random() * ringPack.getTones().size());
                    }

                    ringPack.setCurrentTone(ringPack.getTones().get(randIndex));

                    break;
                case SharedPrefUtils.MODE_LOCKED:
                    // Do nothing, BAIL
                    return;
                default:
                    throw new UnsupportedOperationException();
            }

            ringPack.getCurrentTone().setDefaultNotificationRingtone(ctx);
        }
    }

    /**
     * Helper to return the next enabled Tone.
     * @param curIndex Index of a new tone. If it's not enabled, keep going
     * @return The tone, or null if there are no enabled tones
     */
    private Tone findNextEnabledTone(int curIndex) {
        int startingIndex = curIndex;

        if (curIndex >= ringPack.getTones().size()) {
            curIndex = 0;
        }

        while (!ringPack.getTones().get(curIndex).isEnabled()) {
            curIndex++;

            if (curIndex >= ringPack.getTones().size()) {
                curIndex = 0;
            }

            if (curIndex == startingIndex) {
                // We've completed a full loop. Bail so we don't go forever.
                return null;
            }
        }

        return ringPack.getTones().get(curIndex);
    }

    @Override
    public void delete() {
        FileUtils.deleteAll(ringPack.getRootPath());
    }

    @Override
    public ArrayList<Tone> getTones() {
        return ringPack.getTones();
    }

    @Override
    public boolean hasEnabledTones() {
        return findNextEnabledTone(0) != null;
    }

    @Override
    public boolean hasAllEnabledTones() {
        for (Tone t : ringPack.getTones()) {
            if (!t.isEnabled()) {
                return false;
            }
        }

        return true;
    }
}
