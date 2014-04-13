package com.cryclops.ringpack.services;

import android.content.Context;

import com.cryclops.ringpack.viewmodel.PackVm;

import java.io.File;
import java.util.ArrayList;

/**
 * Use this to obtain a list of Packs.
 */
public interface PackReaderService {

    /**
     * Parses available RingPacks from the SD card, in the packs/ directory. To be
     * recognized, a RingPack must follow the following convention:
     *
     * packs/
     *     your_custom_pack/
     *         info.txt - see comment in RingPackVm for the expected format
     *         custom_tone1.ogg
     *         custom_tone2.ogg
     *
     * Once this method completes, the packVms variable is up to date and valid.
     *
     * @param ctx The hosting Activity's base Context
     * @param packPath The packs/ directory
     * @return A list of PackVms
     */
    ArrayList<PackVm> readPacks(Context ctx, File packPath);

    /**
     * Look for an "info.txt" file.
     * @param rootPath The directory to search
     * @return The File or null if not found or if there are multiple.
     */
    File getInfoFile(File rootPath);

    /**
     * Determine which Pack is currently set in the system (if any) and initialize it properly.
     * @param ctx The hosting Activity's base Context
     * @param packs An optional list of existing PackVms to look through
     * @return The currently set Pack (that knows it's set), or null if nothing is set.
     */
    PackVm findEnabledPackVm(Context ctx, ArrayList<PackVm> packs);
}
