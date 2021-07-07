package com.ecosense.app.util;

import android.content.Context;
import android.location.LocationManager;

import androidx.annotation.NonNull;

/**
 * <h1>Utility class for location related functionality</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class LocationUtil {
    /**
     * Method to check whether location service (GPS) is enabled or not.
     *
     * @param context Current context (or context)
     * @return {@code true} if the location service is enabled else {@code false}
     */
    public static boolean isGPSLocationEnabled(@NonNull Context context) {
        return isLocationEnabled(context, true);
    }

    /**
     * Method to check whether location service (GPS or Network) is enabled or not.
     *
     * @param context Current context (or context)
     * @return {@code true} if the location service is enabled else {@code false}
     */
    public static boolean isLocationEnabled(@NonNull Context context) {
        return isLocationEnabled(context, false);
    }

    /**
     * Method to check whether location service (GPS or Network - depending on {@code isOnlyGps})
     * is enabled or not.
     *
     * @param context
     * @param isOnlyGps
     * @return
     */
    private static boolean isLocationEnabled(@NonNull Context context, boolean isOnlyGps) {
        LocationManager locationManager = (LocationManager) context.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null)
            return isOnlyGps ?
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) :
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return false;
    }
}
