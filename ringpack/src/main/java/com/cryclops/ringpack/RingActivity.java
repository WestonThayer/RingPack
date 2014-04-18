package com.cryclops.ringpack;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.cryclops.ringpack.services.AppServiceLocator;
import com.cryclops.ringpack.services.NotificationService;
import com.cryclops.ringpack.services.ResourceService;
import com.cryclops.ringpack.utils.ListUtils;
import com.cryclops.ringpack.utils.ServiceUtils;
import com.cryclops.ringpack.utils.SharedPrefUtils;
import com.cryclops.ringpack.view.ConfirmationDialogFragment;
import com.cryclops.ringpack.view.InfoDialogFragment;
import com.cryclops.ringpack.view.PackVmAdapter;
import com.cryclops.ringpack.viewmodel.OnCompletedListener;
import com.cryclops.ringpack.viewmodel.OnInitializedListener;
import com.cryclops.ringpack.viewmodel.PackVm;
import com.cryclops.ringpack.viewmodel.RingActivityVm;


public class RingActivity extends ActionBarActivity implements NotificationService, ResourceService {

    /**
     * Identifier for a Serializable intent extra containing a PackVm that we send to the
     * EditActivity.
     */
    public static final String PACKVM_EXTRA = "com.cryclops.ringpack.packExtra";

    private ProgressDialog progressDialog;
    private RingActivityVm vm;
    private PackVmAdapter packVmAdapter;
    private Button getMoreButton;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);

        getMoreButton = (Button) findViewById(R.id.activity_ring_get_more_button);
        gridView = (GridView) findViewById(R.id.activity_ring_packs_gridview);

        // Change out the ActionBar's title (it has to be 'RingPack' in the manifest, otherwise
        // the app's label is wrong on the home screen).
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.activity_ring);

        // Ensure context menu shows on long press
        registerForContextMenu(gridView);

        // Change selection on tap
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                // Update the ViewModel
                PackVm selectedPackVm = vm.getPackVms().get(i);

                if (selectedPackVm.isSelected()) {
                    // The user tapped on an already selected pack
                    ServiceUtils.getLog().doubleSetRingPack();
                }

                vm.setSelectedPackVmAsync(selectedPackVm, getBaseContext());
            }
        });

        getMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=RingPack"));
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    startActivity(i);
                }
                catch (ActivityNotFoundException e) {
                    // There's no market? Weird
                    ServiceUtils.getLog().exception(e, false);
                }
            }
        });

        // Possibly show new version dialog
        try {
            int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

            if (version > SharedPrefUtils.getPreviousVersion(getBaseContext())) {
                SharedPrefUtils.setPreviousVersion(getBaseContext(), version);

                showInfoDialog(
                        getString(R.string.info_dialog_title_version),
                        getString(R.string.info_dialog_content_version)
                );
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServiceUtils.getLog().activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppServiceLocator.getInstance().addService(NotificationService.class, this);
        AppServiceLocator.getInstance().addService(ResourceService.class, this);

        if (vm == null) {
            vm = new RingActivityVm();

            vm.setOnInitializedListener(new OnInitializedListener() {
                @Override
                public void onInitialized(Object sender) {
                    RingActivityVm ringVm = (RingActivityVm) sender;

                    // Hook up the data to the GridView of packs
                    packVmAdapter = new PackVmAdapter(getBaseContext(), ringVm);
                    gridView.setAdapter(packVmAdapter);
                }
            });

            vm.setOnDeletePackVmAsyncCompletedListener(new OnCompletedListener() {
                @Override
                public void onCompleted(Object sender) {
                    packVmAdapter.notifyDataSetChanged();
                }
            });

            vm.setOnSelectedPackVmAsyncCompletedListener(new OnCompletedListener() {
                @Override
                public void onCompleted(Object sender) {
                    packVmAdapter.notifyDataSetChanged();
                }
            });

            vm.initializeAsync(this.getBaseContext());
        }
    }

    @Override
    protected void onPause() {
        AppServiceLocator.getInstance().removeService(NotificationService.class);
        AppServiceLocator.getInstance().removeService(ResourceService.class);
        super.onPause();
    }

    @Override
    protected void onStop() {
        ServiceUtils.getLog().activityStop(this);
        super.onStop();
    }

    /**
     * There's no container to hold which PackVm was long-pressed, so we'll store it here.
     */
    private PackVm packVmForContextMenu = null;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.ring_pack_context, menu);

        AdapterView.AdapterContextMenuInfo a = (AdapterView.AdapterContextMenuInfo) menuInfo;
        packVmForContextMenu = vm.getPackVms().get(a.position);

        ServiceUtils.getLog().longPress("pack_context_menu");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ring_pack_context_edit:
                Intent i = new Intent(this, EditActivity.class);
                i.putExtra(PACKVM_EXTRA, packVmForContextMenu);
                startActivityForResult(i, EditActivity.EDIT_PACK_REQUEST);

                return true;
            case R.id.ring_pack_context_delete:
                vm.deletePackVmAsync(packVmForContextMenu, getBaseContext());

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == EditActivity.EDIT_PACK_REQUEST) {
            PackVm resultPackVm = (PackVm) data.getSerializableExtra(PACKVM_EXTRA);

            // Replace it in our ViewModel
            ListUtils.replace(vm.getPackVms(), packVmForContextMenu, resultPackVm);

            packVmAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ring, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_disable:
                vm.setSelectedPackVmAsync(null, getBaseContext());
                packVmAdapter.notifyDataSetChanged();

                return true;
            case R.id.action_settings:
                startActivity(new Intent(getBaseContext(), SettingsActivity.class));

                return true;
            case R.id.action_refresh:
                vm.initializeAsync(getBaseContext());

                return true;
            case R.id.action_about:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://cryclops.com/apps/ringpack/"));
                startActivity(i);

                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showIndeterminateProgressDialog(String title, String message) {
        progressDialog = ProgressDialog.show(this, title, message, true, false);
    }

    @Override
    public void hideIndeterminateProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showShortToast(int resource) {
        Toast.makeText(getBaseContext(), resource, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showInfoDialog(String title, String content) {
        InfoDialogFragment dialog = InfoDialogFragment.newInstance(title, content);
        dialog.show(getSupportFragmentManager(), "infoDialog");
    }

    @Override
    public void showConfirmationDialog(
            String title,
            String content,
            final OnCompletedListener onPositiveCompletedListener,
            final OnCompletedListener onNegativeCompletedListener) {
        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(title, content);

        dialog.setClickListeners(
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPositiveCompletedListener.onCompleted(dialogInterface);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onNegativeCompletedListener.onCompleted(dialogInterface);
                    }
                }
        );

        dialog.show(getSupportFragmentManager(), "confirmationDialog");
    }
}
