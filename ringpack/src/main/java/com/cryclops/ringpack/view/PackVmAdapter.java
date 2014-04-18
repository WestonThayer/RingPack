package com.cryclops.ringpack.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

        TextView nameTv = (TextView) gridItemView.findViewById(R.id.griditem_pack_name);
        nameTv.setText(packVm.getName());

        // Set the image to look pretty
        ImageView imageView = (ImageView) gridItemView.findViewById(R.id.griditem_pack_image);
        int packImageId;

        if (packVm.getName().equals("Guitar in A Pack")) {
            packImageId = R.drawable.guitar_pack;
        }
        else if (packVm.getName().equals("Legend of Zelda: OOT General")) {
            packImageId = R.drawable.zelda_pack;
        }
        else if (packVm.getName().equals("Legend of Zelda: OOT Navi")) {
            packImageId = R.drawable.navi_pack;
        }
        else if (packVm.getName().equals("Delayed Piano in C") ||
                packVm.getName().equals("Piano Riff Pack")) {
            packImageId = R.drawable.piano_pack;
        }
        else {
            packImageId = R.drawable.unknown_pack;
        }

        imageView.setImageResource(packImageId);

        return  gridItemView;
    }
}
