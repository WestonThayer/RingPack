package com.cryclops.ringpack.services;

import android.content.Context;

import com.cryclops.ringpack.viewmodel.MockPackVm;
import com.cryclops.ringpack.viewmodel.PackVm;

import java.io.File;
import java.util.ArrayList;

/**
 * Serves up fake packs.
 */
public class MockPackReaderService implements PackReaderService {

    @Override
    public ArrayList<PackVm> readPacks(Context ctx, File packPath) {
        ArrayList<PackVm> packVms = new ArrayList<PackVm>();
        packVms.add(new MockPackVm());
        packVms.add(new MockPackVm());
        packVms.add(new MockPackVm());
        packVms.add(new MockPackVm());
        packVms.add(new MockPackVm());
        return packVms;
    }

    @Override
    public File getInfoFile(File rootPath) {
        return null;
    }

    @Override
    public PackVm findEnabledPackVm(Context ctx, ArrayList<PackVm> packs) {
        return null;
    }
}
