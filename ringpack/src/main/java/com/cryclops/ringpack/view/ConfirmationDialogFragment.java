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

    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    DialogInterface.OnClickListener onPositiveClickListener, onNegativeClickListener;

    public static ConfirmationDialogFragment newInstance(String title, String content) {
        ConfirmationDialogFragment f = new ConfirmationDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(CONTENT, content);
        f.setArguments(args);

        return f;
    }

    public void setClickListeners(
            DialogInterface.OnClickListener onPositiveClickListener,
            DialogInterface.OnClickListener onNegativeClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        this.onNegativeClickListener = onNegativeClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getArguments().getString(TITLE));
        builder.setMessage(getArguments().getString(CONTENT));

        builder.setPositiveButton(R.string.confirmation_dialog_yes, onPositiveClickListener);
        builder.setNegativeButton(R.string.confirmation_dialog_no, onNegativeClickListener);

        return builder.create();
    }
}
