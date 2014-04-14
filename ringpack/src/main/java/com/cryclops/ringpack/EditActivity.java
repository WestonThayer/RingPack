package com.cryclops.ringpack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.cryclops.ringpack.view.ToneAdapter;
import com.cryclops.ringpack.viewmodel.EditActivityVm;
import com.cryclops.ringpack.viewmodel.PackVm;

/**
 * Allows a user to check out the contents of a pack, play them, and enable/disable tones.
 */
public class EditActivity extends ActionBarActivity {

    /**
     * Launch this Activity for PackVm result.
     */
    public static final int EDIT_PACK_REQUEST = 0;

    private EditActivityVm vm;
    private ToneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        PackVm packVm = (PackVm) getIntent().getSerializableExtra(RingActivity.PACKVM_EXTRA);
        vm = new EditActivityVm(packVm);

        // Update the ActionBar's title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.activity_edit) + " " + packVm.getName());

        ListView listView = (ListView) findViewById(R.id.activity_edit_listview);
        adapter = new ToneAdapter(getBaseContext(), vm.getTones());
        listView.setAdapter(adapter);

        TextView pathTv = (TextView) findViewById(R.id.activity_edit_pack_path);
        pathTv.setText(vm.getBasePath());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        Intent i = new Intent();
        i.putExtra(RingActivity.PACKVM_EXTRA, vm.getPackVm());
        setResult(RESULT_OK, i);

        super.finish();
    }
}
