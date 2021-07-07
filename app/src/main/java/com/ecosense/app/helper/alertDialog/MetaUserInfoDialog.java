package com.ecosense.app.helper.alertDialog;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.ecosense.app.R;
import com.ecosense.app.databinding.LayoutDialogBackendProgressBinding;

/**
 * <h1>Helper class for showing progress alert dialog while downloading metadata user info alert dialog.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetaUserInfoDialog extends AlertDialogHelper {

    public interface OnMetadataUserProgressDialogListener {
        void onRetryButtonClicked(AlertDialogHelper dialogInstance);

        void onCancelButtonClicked(AlertDialogHelper dialogInstance);
    }

    private LayoutDialogBackendProgressBinding dialogBinding;
    private final OnMetadataUserProgressDialogListener dialogListener;
    private final Transition transition;
    private boolean isVisible;

    public MetaUserInfoDialog(@NonNull final Activity activity,
                              @NonNull final OnMetadataUserProgressDialogListener dialogListener) {
        super(activity);
        this.dialogListener = dialogListener;
        transition = new Slide();
        isVisible = false;
    }

    @Override
    public void show() {
        if (isVisible)
            showProgress();
        else {
            dialogBinding = LayoutDialogBackendProgressBinding.inflate(activity.getLayoutInflater());
            initViews();
            initListeners();
            createCustomAlertDialog(dialogBinding.getRoot(), false);
            isVisible = true;
        }
    }

    public void showProgress() {
        if (isVisible) {
            dialogBinding.includedError.getRoot().setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), transition);
            dialogBinding.groupProgress.setVisibility(View.VISIBLE);
        } else show();
    }

    public void showError(@NonNull final String problem, @NonNull final String solution, final boolean isContactButtonEnabled) {
        if (isVisible) {
            dialogBinding.includedError.tvProblem.setText(problem);
            dialogBinding.includedError.tvSolution.setText(solution);
            dialogBinding.includedError.mbCancel.setEnabled(isContactButtonEnabled);

            if (problem.equalsIgnoreCase(activity.getString(R.string.dialog_problem_unauthenticated_user))) {
                dialogBinding.includedError.mbRetry.setText(activity.getString(R.string.button_ok));
                dialogBinding.includedError.mbRetry.setOnClickListener(v -> {
                    isVisible = false;
                    dismiss();
                });
            } else {
                dialogBinding.includedError.mbRetry.setText(activity.getString(R.string.button_retry));
                dialogBinding.includedError.mbRetry.setOnClickListener(v -> {
                    if (dialogListener != null)
                        dialogListener.onRetryButtonClicked(this);
                });
            }

            dialogBinding.groupProgress.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), transition);
            dialogBinding.includedError.getRoot().setVisibility(View.VISIBLE);
        } else {
            show();
            showError(problem, solution, isContactButtonEnabled);
        }
    }

    private void initViews() {
        dialogBinding.tvTitle.setText(activity.getString(R.string.dialog_fetching_data_title));
        dialogBinding.tvDescription.setText(activity.getString(R.string.dialog_fetching_metadata_description,
                activity.getString(R.string.dialog_description_this_may_take_a_while)));

        dialogBinding.includedError.tvTitle.setText(activity.getString(R.string.dialog_save_data_fetch_failed_title));
    }

    @Override
    public void initListeners() {
        dialogBinding.includedError.mbRetry.setOnClickListener(v -> {
            if (dialogListener != null)
                dialogListener.onRetryButtonClicked(this);
        });

        dialogBinding.includedError.mbCancel.setOnClickListener(v -> {
            if (dialogListener != null) {
                dialogListener.onCancelButtonClicked(this);
            }
        });
    }
}
