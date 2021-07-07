package com.ecosense.app.helper.alertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

import com.ecosense.app.firebase.Analytics;

/**
 * <h1>Helper class to show alert dialogs</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public abstract class AlertDialogHelper extends AlertDialog implements AlertDialogUsable {

    private AlertDialog alertDialog;
    protected Activity activity;
    protected Analytics analytics;
    protected boolean isVisible;

    protected AlertDialogHelper(@NonNull final Activity activity) {
        super(activity);
        this.activity = activity;
        analytics = Analytics.getInstance();
    }

    protected void createCustomAlertDialog(@NonNull final View customView, final boolean cancellable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(customView);
try{
        alertDialog = builder.create();

        Objects.requireNonNull(alertDialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        initListeners();
        alertDialog.setCancelable(cancellable);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                analytics.logDialogDismiss(this.getClass());
            }
        });
        alertDialog.show();
        hideKeyboard();
        analytics.logDialogShow(this.getClass());}catch (Exception e){}
    }

    protected void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();

        if (view == null) view = new View(activity);

        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void hideKeyboard(@NonNull View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (view == null) view = new View(activity);

        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void dismiss() {
        if (alertDialog != null) {
            isVisible = false;
            hideKeyboard();
            alertDialog.dismiss();
        }
    }
}
