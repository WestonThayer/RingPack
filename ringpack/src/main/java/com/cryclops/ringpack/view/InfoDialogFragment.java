package com.cryclops.ringpack.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.cryclops.ringpack.R;

/**
 * Provides a simple informational modal dialog.
 */
public class InfoDialogFragment extends DialogFragment {

    private String title, content;

    public InfoDialogFragment(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(title);
        builder.setMessage(content);

        builder.setNegativeButton(R.string.info_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // No action necessary
            }
        });

        return builder.create();
    }
}
