package com.ecosense.app.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * <h1>Utility class to help in logging logs for crash report using {@link FirebaseCrashlytics}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class CrashAnalytics {
    public static void setUserId(@NonNull String userId){
        FirebaseCrashlytics.getInstance().setUserId(userId);
    }

    public static void setLoggedIn(final boolean isLoggedIn) {
        FirebaseCrashlytics.getInstance().setCustomKey("logged_in", isLoggedIn);
    }

    public static void setLocationPermission(final boolean isAvailable) {
        FirebaseCrashlytics.getInstance().setCustomKey("location_permission", isAvailable);
    }

    public static void setCameraPermission(final boolean isAvailable) {
        FirebaseCrashlytics.getInstance().setCustomKey("camera_permission", isAvailable);
    }

    public static void setGpsEnabled(final boolean isEnabled) {
        FirebaseCrashlytics.getInstance().setCustomKey("gps_enabled", isEnabled);
    }

    public static void setNetworkConnectivity(final boolean isAvailable) {
        FirebaseCrashlytics.getInstance().setCustomKey("network_connectivity", isAvailable);
    }

    public static void setServiceRunning(final boolean isRunning) {
        FirebaseCrashlytics.getInstance().setCustomKey("service_running", isRunning);
    }

    public static void setMapReady(final boolean isReady) {
        FirebaseCrashlytics.getInstance().setCustomKey("map_ready", isReady);
    }

    public static void logException(Exception e){
        FirebaseCrashlytics.getInstance().recordException(e);
    }

    public static void log(@NonNull String logMessage){
        FirebaseCrashlytics.getInstance().log(logMessage);
    }
}
