package com.ecosense.app.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * <h1>Preference util class to help save and retrieve encrypted preferences easily and effectively</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class EncryptedPreferenceUtil extends PreferenceUtil {

    /**
     * Constructor.
     *
     * @param context {@link Activity} context
     */
    public EncryptedPreferenceUtil(@NonNull Context context) {
        super(context);
    }

    /**
     * Method to initialise the encrypted preference file with appropriate encryption.
     */
    @Override
    protected void initSharedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "encrypted_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception  ignore) {
        }
    }

}
