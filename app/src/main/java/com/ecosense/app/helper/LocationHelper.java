package com.ecosense.app.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ecosense.app.util.LocationUtil;

/**
 * <h1>Helper class for location related functionality</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class LocationHelper {
    public static boolean isGpsLocationEnabled(@NonNull final Context context,
                                                @NonNull final PermissionHelper permissionHelper) {
        if (LocationUtil.isGPSLocationEnabled(context))
            return true;

        permissionHelper.startLocationSettingIntent(null);
        return false;
    }
}
