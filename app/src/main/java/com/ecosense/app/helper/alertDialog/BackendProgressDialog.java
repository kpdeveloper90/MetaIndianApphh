package com.ecosense.app.helper.alertDialog;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.ecosense.app.R;
import com.ecosense.app.databinding.LayoutDialogBackendProgressBinding;

/**
 * <h1>Helper class for showing progress and error related to the backend request calls.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class BackendProgressDialog extends AlertDialogHelper {
    public interface BackendProgressDialogListener {
        void onRetryClicked(AlertDialogHelper dialogInstance);

        void onCancelClicked(AlertDialogHelper dialogInstance);
    }

    private LayoutDialogBackendProgressBinding dialogBinding;
    private BackendProgressDialogListener dialogListener;
    private final Transition transition;

    private String progressTitle;
    private String errorTitle;
    private String progressDescription;
    private boolean isCancelable;

    protected BackendProgressDialog(@NonNull Activity activity) {
        super(activity);
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
            createCustomAlertDialog(dialogBinding.getRoot(), isCancelable);
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

    public void showError(@NonNull final String problem, @NonNull final String solution, final boolean isCancelButtonEnabled) {
        if (isVisible) {
            dialogBinding.includedError.tvProblem.setText(problem);
            dialogBinding.includedError.tvSolution.setText(solution);
            dialogBinding.includedError.mbCancel.setEnabled(isCancelButtonEnabled);


            dialogBinding.groupProgress.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), transition);
            dialogBinding.includedError.getRoot().setVisibility(View.VISIBLE);
        } else {
            show();
            showError(problem, solution, isCancelButtonEnabled);
        }
    }

    private void initViews() {
        dialogBinding.tvTitle.setText(progressTitle);
        dialogBinding.tvDescription.setText(progressDescription);
        dialogBinding.includedError.tvTitle.setText(errorTitle);
    }

    @Override
    public void initListeners() {
        dialogBinding.includedError.mbRetry.setOnClickListener(v -> {
            if (dialogListener != null)
                dialogListener.onRetryClicked(this);
        });

        dialogBinding.includedError.mbCancel.setOnClickListener(v -> {
            if (dialogListener != null) {
                dialogListener.onCancelClicked(this);
            }
        });
    }

    public static class Builder {
        private final Activity activity;
        private String progressTitle;
        private String errorTitle;
        private String progressDescription;
        private BackendProgressDialogListener listener;
        private boolean isCancelable;

        public Builder(@NonNull final Activity activity) {
            this.activity = activity;
            isCancelable = false;
        }

        public Builder setProgressTitle(String progressTitle) {
            this.progressTitle = progressTitle;
            return this;
        }

        public Builder setErrorTitle(String errorTitle) {
            this.errorTitle = errorTitle;
            return this;
        }

        public Builder setProgressDescription(String progressDescription) {
            this.progressDescription = progressDescription;
            return this;
        }

        public Builder setListener(BackendProgressDialogListener listener) {
            this.listener = listener;
            return this;
        }

        private Builder setCancelable(final boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        public BackendProgressDialog build() {
            BackendProgressDialog dialogHelper = new BackendProgressDialog(activity);

            if (TextUtils.isEmpty(progressTitle))
                progressTitle = activity.getString(R.string.dialog_backend_title);
            if (TextUtils.isEmpty(errorTitle))
                errorTitle = activity.getString(R.string.dialog_backend_failed_title);
            if (TextUtils.isEmpty(progressDescription))
                progressDescription = activity.getString(R.string.dialog_backend_description, activity.getString(R.string.dialog_description_this_may_take_a_while));

            dialogHelper.progressTitle = progressTitle;
            dialogHelper.errorTitle = errorTitle;
            dialogHelper.progressDescription = progressDescription;
            dialogHelper.dialogListener = listener;
            dialogHelper.isCancelable = isCancelable;

            return dialogHelper;
        }

        public void show() {
            build().show();
        }
    }
}
