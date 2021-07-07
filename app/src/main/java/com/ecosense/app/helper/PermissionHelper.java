package com.ecosense.app.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import com.ecosense.app.R;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;

/**
 * <h1>Helper class having convenience methods for permission management needed for this project</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PermissionHelper {

    public interface OnPermissionListener {
        void onPermissionGranted(@NonNull final String permissionName);

        default void onPermissionDenied(@NonNull final String permissionName) {
        }
    }

    private static final String LOCATION_PERMISSION = "location";
    private static final String CAMERA_PERMISSION = "camera";
    private static final String CALL_PERMISSION = "call";

    private Context context;
    private OnPermissionListener listener;
    private Analytics analytics;

    public PermissionHelper(@NonNull final Context context) {
        this(context, null);
    }

    public PermissionHelper(@NonNull final Context context, OnPermissionListener listener) {
        this.context = context;
        this.listener = listener;
        analytics = Analytics.getInstance();
    }

    public void checkAndRequestLocationPermission() {
        checkAndRequestLocationPermission(null);
    }

    public void checkAndRequestLocationPermission(@Nullable final OnPermissionListener listener) {
        analytics.logPermissionRequested(LOCATION_PERMISSION);
        CrashAnalytics.setLocationPermission(false);
try{
        Dexter.withContext(context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (listener != null)
                            listener.onPermissionGranted(permissionGrantedResponse.getPermissionName());

                        CrashAnalytics.setLocationPermission(true);
                        analytics.logPermissionGranted(LOCATION_PERMISSION);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (listener != null)
                            listener.onPermissionDenied(permissionDeniedResponse.getPermissionName());

                        CrashAnalytics.setLocationPermission(false);
                        analytics.logPermissionDenied(LOCATION_PERMISSION);

                        if (permissionDeniedResponse.isPermanentlyDenied()) {
                            analytics.logPermissionDeniedPermanently(LOCATION_PERMISSION);

                            showSettingsDialog(context,
                                    context.getString(R.string.location_permission_needed),
                                    context.getString(R.string.location_permission_needed_rationale));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();}catch (Exception e){}
    }

    public void checkAndRequestCameraPermission() {
        checkAndRequestCameraPermission(null);
    }

    public void checkAndRequestCameraPermission(@Nullable final OnPermissionListener listener) {
        analytics.logPermissionRequested(CAMERA_PERMISSION);
        CrashAnalytics.setCameraPermission(false);
try{
        Dexter.withContext(context)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (listener != null)
                            listener.onPermissionGranted(permissionGrantedResponse.getPermissionName());

                        CrashAnalytics.setCameraPermission(true);
                        analytics.logPermissionGranted(CAMERA_PERMISSION);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (listener != null)
                            listener.onPermissionDenied(permissionDeniedResponse.getPermissionName());

                        CrashAnalytics.setCameraPermission(false);
                        analytics.logPermissionDenied(CAMERA_PERMISSION);

                        if (permissionDeniedResponse.isPermanentlyDenied()) {
                            analytics.logPermissionDeniedPermanently(CAMERA_PERMISSION);

                            showSettingsDialog(context,
                                    context.getString(R.string.location_permission_needed),
                                    context.getString(R.string.location_permission_needed_rationale));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();}catch (Exception e){}
    }

    public void startLocationSettingIntent(final View viewToAttachSnackbar) {
        startLocationSettingIntent(context, viewToAttachSnackbar);
    }

    public void startLocationSettingIntent(@NonNull final Context context, final View viewToAttachSnackbar) {
        analytics.logGpsAlertShown();
try{
        if (viewToAttachSnackbar == null) {
            new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle(context.getString(R.string.location_services_not_active))
                    .setIcon(ContextCompat.getDrawable(context, R.drawable.ic_warning_black_24dp))
                    .setMessage(context.getString(R.string.please_enable_location))

                    .setPositiveButton(context.getString(R.string.btn_yes), (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }).show();
        } else {
            Snackbar snackbar = Snackbar.make(viewToAttachSnackbar,
                    context.getString(R.string.please_enable_location), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(context.getString(R.string.button_location_settings), view -> {
                if (snackbar.isShown())
                    snackbar.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }).show();
        }}catch (Exception e){}
    }

    public void checkAndRequestCallPermission(@NonNull final Context context, final View viewToAttachSnackbar) {
        analytics.logPermissionRequested(CALL_PERMISSION);
try{
        Dexter.withContext(context)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(viewToAttachSnackbar == null ?
                        DialogOnDeniedPermissionListener.Builder
                                .withContext(context)
                                .withTitle(context.getString(R.string.call_permission_needed))
                                .withMessage(context.getString(R.string.call_permission_needed_rationale))
                                .withButtonText(context.getString(R.string.button_settings))
                                .build() :
                        SnackbarOnDeniedPermissionListener.Builder
                                .with(viewToAttachSnackbar, context.getString(R.string.call_permission_needed_rationale))
                                .withOpenSettingsButton(context.getString(R.string.button_settings))
                                .withDuration(Snackbar.LENGTH_LONG)
                                .build())
                .check();}catch (Exception e){}
    }

    private void showSettingsDialog(@NonNull final Context context, @NonNull final String title, @NonNull final String message) {
      try{
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(context.getString(R.string.button_settings), (dialog, which) -> {
            dialog.cancel();
            if (context instanceof Activity) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                ((Activity) context).startActivityForResult(intent, 101);
            } else throw new IllegalArgumentException("Context passed is not an activity!");
        });
        builder.setNegativeButton(context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, context.getString(R.string.toast_permission_not_granted_error), Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        builder.show();}catch (Exception e){}
    }
}
