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

    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    public static InfoDialogFragment newInstance(String title, String content) {
        InfoDialogFragment f = new InfoDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(CONTENT, content);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String title = getArguments().getString(TITLE);
        String content = getArguments().getString(CONTENT);

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
