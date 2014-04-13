package com.cryclops.ringpack.viewmodel;

import android.content.Context;
import android.media.Ringtone;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.cryclops.ringpack.R;
import com.cryclops.ringpack.model.Tone;
import com.cryclops.ringpack.services.AppServiceLocator;
import com.cryclops.ringpack.services.NotificationService;
import com.cryclops.ringpack.services.PackReaderService;
import com.cryclops.ringpack.services.ResourceService;
import com.cryclops.ringpack.utils.FileUtils;
import com.cryclops.ringpack.utils.ListUtils;
import com.cryclops.ringpack.utils.PropertySelector;
import com.cryclops.ringpack.utils.RingtoneManagerUtils;
import com.cryclops.ringpack.utils.SharedPrefUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 */
public class RingActivityVm extends InitializableActivityVm {

    /**
     * Android/data/com.cryclops.ringpack/packs/
     */
    public static final String SD_PACK_SUBPATH = "Android/data/com.cryclops.ringpack/packs/";

    /**
     * A collection of Packs.
     */
    private ArrayList<PackVm> packVms;
    public ArrayList<PackVm> getPackVms() {
        return packVms;
    }

    public RingActivityVm() {
        super();
        onSelectedPackVmAsyncCompletedListeners = new ArrayList<OnCompletedListener>();
        onDeletePackVmAsyncCompletedListeners = new ArrayList<OnCompletedListener>();
    }

    /**
     * Perform first time setup of the SD card to install our default pack (mallet). Also builds
     * the list of Packs. Runs on a new thread and blocks the user with a progress dialog.
     * @param baseContext The Activity's base Context
     * @return True if the task has started, false if not.
     */
    @Override
    public boolean initializeAsync(Context baseContext) {
        if (!FileUtils.isExternalStorageWritable()) {
            // Tell the user
            NotificationService notificationService = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
            ResourceService resourceService = (ResourceService) AppServiceLocator.getInstance().getService(ResourceService.class);

            notificationService.showInfoDialog(
                    resourceService.getString(R.string.info_dialog_title_error),
                    resourceService.getString(R.string.info_dialog_content_sd)
            );

            return false;
        }

        InitAsyncTask task = new InitAsyncTask();
        task.execute(baseContext);

        return true;
    }

    /**
     * Sets up the directory structure for RingPack on the SD card and copies over the default pack.
     * Then it parses the Pack directory and initializes the list of RingPacks.
     * Works on a new thread and shows a progress dialog as needed.
     */
    private class InitAsyncTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            NotificationService progressService =
                    (NotificationService)AppServiceLocator.getInstance().getService(NotificationService.class);
            progressService.showIndeterminateProgressDialog(
                    AppServiceLocator.getInstance().getResString(R.string.progress_title_working),
                    AppServiceLocator.getInstance().getResString(R.string.progress_contents_finding)
            );
        }

        @Override
        protected Boolean doInBackground(Context... contexts) {
            Context ctx = contexts[0];

            File packPath = new File(Environment.getExternalStorageDirectory(), SD_PACK_SUBPATH);

            setupSdCard(ctx, packPath);

            PackReaderService readerService = (PackReaderService) AppServiceLocator.getInstance().getService(PackReaderService.class);
            packVms = readerService.readPacks(ctx, packPath);

            // Don't call setSelectedPackVm(), it'll crash because it'll try to throw up
            // an extra ProgressDialog.
            PackVm enabledPackVm = readerService.findEnabledPackVm(ctx, packVms);
            if (enabledPackVm != null) enabledPackVm.setIsSelected(true);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            NotificationService progressService =
                    (NotificationService)AppServiceLocator.getInstance().getService(NotificationService.class);
            progressService.hideIndeterminateProgressDialog();

            if (!result) {
                // alert the user
                NotificationService notificationService = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
                ResourceService resourceService = (ResourceService) AppServiceLocator.getInstance().getService(ResourceService.class);

                notificationService.showInfoDialog(
                        resourceService.getString(R.string.info_dialog_title_error),
                        resourceService.getString(R.string.info_dialog_content_init)
                );
            }
            else {
                fireOnInitialized();
            }
        }

        /**
         * Performs first time setup checks for our directory on the SD card. If needed, it will:
         *
         * - Create the packs/ directory
         * - Copy over the included 'Mallet Pack'
         *
         * @param ctx The hosting Activity's base Context
         * @param packPath The packs/ directory
         */
        private void setupSdCard(Context ctx, File packPath) {
            // Create root path
            if (!packPath.isDirectory()) {
                if (!packPath.mkdirs()) {
                    throw new UnsupportedOperationException();
                }
            }

            // Create .nomedia file to exclude us from Media Scans
            File nomedia = new File(packPath, ".nomedia");
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                }
                catch (IOException ex) {
                    throw new UnsupportedOperationException(ex);
                }
            }

            //move over the first sample pack if it's not already there
            String filenames[] = null;

            File malletPackPath = new File(packPath, "mallet_pack/");
            if (!malletPackPath.isDirectory()) {
                if (!malletPackPath.mkdirs()) {
                    throw new UnsupportedOperationException();
                }

                filenames = new String[] {
                        "info.txt",
                        "mallet_1.ogg",
                        "mallet_2.ogg",
                        "mallet_3.ogg",
                        "mallet_4.ogg"
                };
                int resource[] = {
                        com.cryclops.ringpack.R.raw.info1,
                        com.cryclops.ringpack.R.raw.mallet_1,
                        com.cryclops.ringpack.R.raw.mallet_2,
                        com.cryclops.ringpack.R.raw.mallet_3,
                        com.cryclops.ringpack.R.raw.mallet_4
                };

                // Copy them
                byte[] buffer = null;

                for (int i = 0; i < filenames.length; i++) {
                    InputStream fIn = ctx.getResources().openRawResource(resource[i]);

                    // Copy into byte buffer
                    try {
                        int size = fIn.available();
                        buffer = new byte[size];
                        fIn.read(buffer);
                        fIn.close();
                    } catch (IOException ex) {
                        throw new UnsupportedOperationException(ex);
                    }

                    // Write to buffer to disk
                    if (buffer != null) {
                        try {
                            FileOutputStream save = new FileOutputStream(new File(malletPackPath, filenames[i]));
                            save.write(buffer);
                            save.flush();
                            save.close();
                        } catch (FileNotFoundException ex) {
                            throw new UnsupportedOperationException(ex);
                        } catch (IOException ex) {
                            throw new UnsupportedOperationException(ex);
                        }
                    }
                    else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
    }

    public PackVm getSelectedPackVm() {
        return ListUtils.firstOrDefault(getPackVms(), new PropertySelector<PackVm>() {
            @Override
            public boolean test(PackVm item) {
                return item.isSelected();
            }
        });
    }

    private ArrayList<OnCompletedListener> onDeletePackVmAsyncCompletedListeners;
    /**
     * Register to be notified when the the deletePackVmAsync method has completed.
     * @param e
     */
    public void setOnDeletePackVmAsyncCompletedListener(OnCompletedListener e) {
        onDeletePackVmAsyncCompletedListeners.add(e);
    }
    public void removeOnDeletePackVmAsyncCompletedListener(OnCompletedListener e) {
        onDeletePackVmAsyncCompletedListeners.remove(e);
    }
    private void fireOnDeletePackVmAsyncCompleted() {
        for (OnCompletedListener e : onDeletePackVmAsyncCompletedListeners) {
            e.onCompleted(this);
        }
    }

    /**
     * Delete the given PackVm from disk if the user confirms the action. Register for
     * OnDeletePackVmAsyncCompleted.
     * @param packVm
     */
    public void deletePackVmAsync(final PackVm packVm, final Context ctx) {
        NotificationService notificationService = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
        ResourceService resourceService = (ResourceService) AppServiceLocator.getInstance().getService(ResourceService.class);

        notificationService.showConfirmationDialog(
                resourceService.getString(R.string.confirmation_dialog_title_warning),
                resourceService.getString(R.string.confirmation_dialog_content_pack_delete),
                new OnCompletedListener() {
                    @Override
                    public void onCompleted(Object sender) {
                        if (getSelectedPackVm() != null && getSelectedPackVm().equals(packVm)) {
                            setSelectedPackVmAsync(null, ctx);
                        }

                        packVm.delete();
                        packVms.remove(packVm);
                        fireOnDeletePackVmAsyncCompleted();
                    }
                },
                new OnCompletedListener() {
                    @Override
                    public void onCompleted(Object sender) {
                        fireOnDeletePackVmAsyncCompleted();
                    }
                }
        );
    }

    /**
     * Activates a RingPack for usage on a new thread. Register for the
     * OnSelectedPackVmAsyncCompleted event.
     * @param value The PackVm to activate
     */
    public void setSelectedPackVmAsync(PackVm value, Context ctx) {
        PackVm selectedPackVm = getSelectedPackVm();

        if (selectedPackVm != value) {
            if (selectedPackVm != null) {
                selectedPackVm.setIsSelected(false);
                selectedPackVm.clearCurrentTone();

                if (value == null) {
                    disableRingPack(ctx);
                }
            }

            selectedPackVm = value;

            if (selectedPackVm != null) {
                selectedPackVm.setIsSelected(true);

                ActivatePackVmAsyncTask task = new ActivatePackVmAsyncTask();
                task.execute(ctx);
            }
        }
    }

    /**
     * Activates the selectedPackVm by setting the first tone as the current RingTone,
     * displaying a Toast to the user, and playing the tone.
     */
    private class ActivatePackVmAsyncTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            NotificationService progressService =
                    (NotificationService)AppServiceLocator.getInstance().getService(NotificationService.class);
            progressService.showIndeterminateProgressDialog(
                    AppServiceLocator.getInstance().getResString(R.string.progress_title_working),
                    AppServiceLocator.getInstance().getResString(R.string.progress_contents_activating)
            );
        }

        @Override
        protected Boolean doInBackground(Context... contexts) {
            Context ctx = contexts[0];

            // If the current Default Notification Ringtone is not a RingPack tone, save the URI
            // so that we can re-enable it if the user chooses to disable RingPack.
            Uri curToneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);
            Ringtone curTone = RingtoneManagerUtils.getDefaultNotificationRingtone(ctx);

            if (curTone != null) {
                if (!curTone.getTitle(ctx).equals(Tone.DEFAULT_TONE_NAME)) {
                    SharedPrefUtils.setRingtoneRestoreUri(ctx, curToneUri);
                }
            }
            else {
                // The user doesn't have one set
                SharedPrefUtils.setRingtoneRestoreUri(ctx, null);
            }

            getSelectedPackVm().moveToNextTone(ctx);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            NotificationService progressService =
                    (NotificationService)AppServiceLocator.getInstance().getService(NotificationService.class);
            progressService.hideIndeterminateProgressDialog();

            if (!result) {
                // alert the user
                NotificationService notificationService = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
                ResourceService resourceService = (ResourceService) AppServiceLocator.getInstance().getService(ResourceService.class);

                notificationService.showInfoDialog(
                        resourceService.getString(R.string.info_dialog_title_error),
                        resourceService.getString(R.string.info_dialog_content_packset)
                );
            }
            else {
                progressService.showShortToast(R.string.toast_pack_set);
                fireOnSelectedPackVmAsyncCompleted();
            }
        }
    }

    private ArrayList<OnCompletedListener> onSelectedPackVmAsyncCompletedListeners;
    /**
     * Register to be notified when the the setSelectedPackVmAsync method has completed.
     * @param e
     */
    public void setOnSelectedPackVmAsyncCompletedListener(OnCompletedListener e) {
        onSelectedPackVmAsyncCompletedListeners.add(e);
    }
    public void removeOnSelectedPackVmAsyncCompletedListener(OnCompletedListener e) {
        onSelectedPackVmAsyncCompletedListeners.remove(e);
    }
    private void fireOnSelectedPackVmAsyncCompleted() {
        for (OnCompletedListener e : onSelectedPackVmAsyncCompletedListeners) {
            e.onCompleted(this);
        }
    }

    /**
     * Remove our MediaStore entries, restore the old Ringtone.
     * @param ctx
     */
    private void disableRingPack(Context ctx) {
        // Delete our MediaStore entry (assuming we have one)
        Uri currentToneUri = RingtoneManagerUtils.getDefaultNotificationRingtoneUri(ctx);
        Ringtone currentTone = RingtoneManagerUtils.getDefaultNotificationRingtone(ctx);

        if (currentTone.getTitle(ctx).equals(Tone.DEFAULT_TONE_NAME)) {
            if (ctx.getContentResolver().delete(currentToneUri, null, null) != 1) {
                // Report it
            }
        }

        // Restore the old Ringtone
        Uri oldToneUri = SharedPrefUtils.getRingtoneRestoreUri(ctx);
        RingtoneManagerUtils.setDefaultNotificationRingtone(ctx, oldToneUri);

        // Toast the user
        NotificationService service = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
        service.showShortToast(R.string.toast_disabled);

        // Play the old tone
        Ringtone oldTone = RingtoneManagerUtils.getDefaultNotificationRingtone(ctx);
        if (oldTone != null) oldTone.play();
    }

    /**
     * Call to perform the rotate action. It will silently fail if the SD card is not writable.
     * @param context
     */
    public static void tryPerformRotate(Context context) {
        if (FileUtils.isExternalStorageWritable()) {
            PackReaderService readerService = (PackReaderService) AppServiceLocator.getInstance().getService(PackReaderService.class);

            PackVm currentPackVm = readerService.findEnabledPackVm(context, null);

            if (currentPackVm != null) {
                currentPackVm.moveToNextTone(context);
            }
        }
        else {
            // Note it
        }
    }
}
