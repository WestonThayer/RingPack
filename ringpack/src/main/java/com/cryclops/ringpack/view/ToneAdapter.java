package com.cryclops.ringpack.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cryclops.ringpack.R;
import com.cryclops.ringpack.model.Tone;

import java.util.List;

/**
 *
 */
public class ToneAdapter extends ArrayAdapter<Tone> {

    private Context ctx;
    private LayoutInflater inflater;
    private List<Tone> tones;

    public ToneAdapter(Context context, List<Tone> tones) {
        super(context, R.layout.griditem_pack, tones);
        this.ctx = context;
        inflater = LayoutInflater.from(context);
        this.tones = tones;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        Tone tone = tones.get(position);
        boolean isNewView;

        if (convertView == null) {
            itemView = inflater.inflate(R.layout.listitem_tone, parent, false);
            isNewView = true;
        }
        else {
            itemView = convertView;
            isNewView = false;
        }

        // CheckBox should be checked if tone is enabled
        CheckBox enabledCheckBox = (CheckBox) itemView.findViewById(R.id.listitem_tone_checkbox);
        enabledCheckBox.setChecked(tone.isEnabled());
        enabledCheckBox.setTag(tone);

        TextView nameTv = (TextView)itemView.findViewById(R.id.listitem_tone_textview);
        nameTv.setText(tone.getName());

        TextView filenameTv = (TextView) itemView.findViewById(R.id.listitem_tone_sub_textview);
        filenameTv.setText(tone.getPathFile().getName());

        // TextView should be grayed out if tone is disabled
        setEnabled(tone.isEnabled(), nameTv, filenameTv);

        if (isNewView) {
            //Set click listener for the checkbox, which enables/disables the Tone
            enabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Tone tone = (Tone) compoundButton.getTag();
                    tone.setIsEnabled(b);

                    // Update the View
                    View parent = (View) compoundButton.getParent();
                    TextView nameTv = (TextView) parent.findViewById(R.id.listitem_tone_textview);
                    TextView filenameTv = (TextView) parent.findViewById(R.id.listitem_tone_sub_textview);
                    filenameTv.setText(tone.getPathFile().getName());
                    setEnabled(b, nameTv, filenameTv);
                }
            });

            // Set click listener on text, which plays the Tone
            RelativeLayout wrapper = (RelativeLayout) itemView.findViewById(R.id.listitem_tone_wrapper);
            wrapper.setTag(tone);
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tone tone = (Tone) view.getTag();
                    tone.play(ctx);
                }
            });
        }

        return  itemView;
    }

    private void setEnabled(boolean value, TextView nameTv, TextView filenameTv) {
        if (value) {
            nameTv.setTextColor(ctx.getResources().getColor(R.color.text_normal));
            filenameTv.setTextColor(ctx.getResources().getColor(R.color.text_sub_normal));
        }
        else {
            nameTv.setTextColor(ctx.getResources().getColor(R.color.text_disabled));
            filenameTv.setTextColor(ctx.getResources().getColor(R.color.text_sub_disabled));
        }
    }
}