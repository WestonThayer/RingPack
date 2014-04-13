package com.cryclops.ringpack.services;

import com.cryclops.ringpack.viewmodel.OnCompletedListener;

/**
 * Show and hide a progress dialog.
 */
public interface NotificationService {

    void showIndeterminateProgressDialog(String title, String message);
    void hideIndeterminateProgressDialog();

    void showShortToast(int resource);

    void showInfoDialog(String title, String content);

    void showConfirmationDialog(
            String title,
            String content,
            OnCompletedListener onPositiveCompletedListener,
            OnCompletedListener onNegativeCompletedListener);
}
