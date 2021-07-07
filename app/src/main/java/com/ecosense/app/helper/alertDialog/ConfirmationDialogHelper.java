package com.ecosense.app.helper.alertDialog;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ecosense.app.databinding.LayoutDialogBackendProgressBinding;
import com.ecosense.app.databinding.LayoutDialogConfirmationBinding;

/**
 * <h1>Helper class for showing confirmation alert dialog.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class ConfirmationDialogHelper extends AlertDialogHelper {

    public interface OnConfirmationDialogListener {
        void onClicked(ConfirmationDialogHelper dialogInstance);
    }

    private LayoutDialogConfirmationBinding dialogBinding;
    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;
    private OnConfirmationDialogListener positiveDialogListener;
    private OnConfirmationDialogListener negativeDialogListener;
    private boolean isCancelable;

    protected ConfirmationDialogHelper(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public void show() {
        dialogBinding = LayoutDialogConfirmationBinding.inflate(activity.getLayoutInflater());
        initViews();
        initListeners();
        createCustomAlertDialog(dialogBinding.getRoot(), isCancelable);
    }

    private void initViews(){
        if(!TextUtils.isEmpty(title))
            dialogBinding.tvTitle.setText(title);

        if(!TextUtils.isEmpty(message))
            dialogBinding.tvDescription.setText(message);

        if(!TextUtils.isEmpty(positiveButtonText))
            dialogBinding.btPositive.setText(positiveButtonText);

        if(!TextUtils.isEmpty(negativeButtonText))
            dialogBinding.btPositive.setText(negativeButtonText);
    }

    @Override
    public void initListeners() {
        dialogBinding.btPositive.setOnClickListener(v -> {
            if(positiveDialogListener != null)
                positiveDialogListener.onClicked(this);

            dismiss();
        });

        dialogBinding.btNegative.setOnClickListener(v -> {
            if(negativeDialogListener != null)
                negativeDialogListener.onClicked(this);

            dismiss();
        });
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public static class Builder{

        private final Activity activity;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private OnConfirmationDialogListener positiveDialogListener;
        private OnConfirmationDialogListener negativeDialogListener;
        private boolean isCancelable = true;

        public Builder(Activity activity){
            this.activity = activity;
        }

        public Builder setTitle(@NonNull final String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(@NonNull final String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(final String text, final OnConfirmationDialogListener dialogListener){
            this.positiveButtonText = text;
            this.positiveDialogListener = dialogListener;
            return this;
        }

        public Builder setNegativeButton(final String text, final OnConfirmationDialogListener dialogListener){
            this.negativeButtonText = text;
            this.negativeDialogListener = dialogListener;
            return this;
        }

        private Builder setCancelable(final boolean isCancelable){
           this.isCancelable = isCancelable;
            return this;
        }

        public ConfirmationDialogHelper build(){
            ConfirmationDialogHelper dialogHelper = new ConfirmationDialogHelper(activity);

            dialogHelper.title = title;
            dialogHelper.message = message;
            dialogHelper.positiveButtonText = positiveButtonText;
            dialogHelper.negativeButtonText = negativeButtonText;
            dialogHelper.positiveDialogListener = positiveDialogListener;
            dialogHelper.negativeDialogListener = negativeDialogListener;
            dialogHelper.isCancelable = isCancelable;

            return dialogHelper;
        }

        public void show(){
            build().show();
        }
    }


}
