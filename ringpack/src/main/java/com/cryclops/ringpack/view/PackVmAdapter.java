package com.cryclops.ringpack.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cryclops.ringpack.R;
import com.cryclops.ringpack.viewmodel.PackVm;
import com.cryclops.ringpack.viewmodel.RingActivityVm;

/**
 *
 */
public class PackVmAdapter extends ArrayAdapter<PackVm> {

    private LayoutInflater inflater;
    private RingActivityVm vm;

    public PackVmAdapter(Context context, RingActivityVm vm) {
        super(context, R.layout.griditem_pack, vm.getPackVms());
        inflater = LayoutInflater.from(context);
        this.vm = vm;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItemView;
        PackVm packVm = vm.getPackVms().get(position);

        if (convertView == null) {
            gridItemView = inflater.inflate(R.layout.griditem_pack, parent, false);
        }
        else {
            gridItemView = convertView;
        }

        // Set initial visual selection state
        if (packVm.isSelected()) {
            gridItemView.setBackgroundResource(R.color.blue);
        }
        else {
            gridItemView.setBackgroundResource(R.color.transparent);
        }

        TextView nameTv = (TextView)gridItemView.findViewById(R.id.griditem_pack_name);
        nameTv.setText(packVm.getName());

        return  gridItemView;
    }
}
