package com.ecosense.app.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executor;

import com.ecosense.app.R;

/**
 * <h1>Helper class having convenience methods to base framework fingerprint functionality</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class FingerprintHelper {

    /**
     * Interface to enable subscribing methods to get intimated about biometric success or failure;
     */
    public interface OnAuthenticationListener {
        public void onSuccess();

        public void onFailure(int errorCode, String errorMsg);
    }

    private OnAuthenticationListener onAuthenticationListener;
    private BiometricPrompt biometricPrompt;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public FingerprintHelper(@NonNull final Context context,
                             @NonNull final OnAuthenticationListener onAuthenticationListener) {
        this.context = context;
        this.onAuthenticationListener = onAuthenticationListener;
        biometricPrompt = getInstanceOfBiometricPrompt(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean canAuthenticate(@NonNull Context context, View view, boolean showErrorSnackbar) {
        BiometricManager biometricManager = BiometricManager.from(context);
        String errorMessage = "";

        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                errorMessage = context.getString(R.string.snackbar_fingerprint_hardware_not_found);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                errorMessage = context.getString(R.string.snackbar_fingerprint_hardware_not_available);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                errorMessage = context.getString(R.string.snackbar_fingerprint_not_enrolled);
                if (showErrorSnackbar)
                    showSecuritySettingsSnackBar(view, errorMessage);
                return false;
        }

        if (showErrorSnackbar)
            showSnackBar(view, errorMessage);

        return false;
    }

    public void authenticate(String dialogTitle, String dialogSubtitle, String dialogDescription) {

        String title = context.getString(R.string.fingerprint_dialog_title);
        String subtitle = context.getString(R.string.fingerprint_dialog_subtitle);
        String description = context.getString(R.string.fingerprint_dialog_description);

        if (dialogTitle != null)
            title = dialogTitle;

        if (dialogSubtitle != null)
            subtitle = dialogSubtitle;

        if (dialogDescription != null)
            description = dialogDescription;

        biometricPrompt.authenticate(
                getPromptInfo(title, subtitle, description, context.getString(R.string.button_cancel)));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private BiometricPrompt getInstanceOfBiometricPrompt(@NonNull Context context) {
        Executor executor = ContextCompat.getMainExecutor(context);

        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                onAuthenticationListener.onSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                onAuthenticationListener.onFailure(errorCode, errString.toString());
            }
        };

        return new BiometricPrompt((FragmentActivity) context, executor, callback);
    }

    private BiometricPrompt.PromptInfo getPromptInfo(String title, String subtitle,
                                                     String description, String negativeButtonText) {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(negativeButtonText)
                .build();
    }

    private static void showSnackBar(View view, String message) {
        final Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(view.getContext().getString(R.string.snackbar_button_dismiss), v -> snackBar.dismiss());
        snackBar.show();
    }

    private static void showSecuritySettingsSnackBar(View view, String message) {
        final Snackbar snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(view.getContext().getString(R.string.snackbar_button_settings), v -> {
            snackBar.dismiss();
            view.getContext().startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
        });
        snackBar.show();
    }
}
