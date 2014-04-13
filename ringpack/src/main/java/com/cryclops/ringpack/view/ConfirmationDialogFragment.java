package com.cryclops.ringpack.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.cryclops.ringpack.R;

/**
 * Displays a dialog that allows the user to proceed or cancel.
 */
public class ConfirmationDialogFragment extends DialogFragment {

    private String title, content;
    DialogInterface.OnClickListener onPositiveClickListener, onNegativeClickListener;

    public ConfirmationDialogFragment(
            String title,
            String content,
            DialogInterface.OnClickListener onPositiveClickListener,
            DialogInterface.OnClickListener onNegativeClickListener) {
        this.title = title;
        this.content = content;
        this.onPositiveClickListener = onPositiveClickListener;
        this.onNegativeClickListener = onNegativeClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(title);
        builder.setMessage(content);

        builder.setPositiveButton(R.string.confirmation_dialog_yes, onPositiveClickListener);
        builder.setNegativeButton(R.string.confirmation_dialog_no, onNegativeClickListener);

        return builder.create();
    }
}
