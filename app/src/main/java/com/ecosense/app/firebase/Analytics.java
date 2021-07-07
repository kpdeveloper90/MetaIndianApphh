package com.ecosense.app.firebase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.annotation.Retention;
import java.util.concurrent.Executors;

import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.remote.BackendError;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <h1>Utility class to help in logging analytical data using {@link FirebaseAnalytics}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Analytics {
    private static Analytics instance;
    private static FirebaseAnalytics firebaseAnalytics;

    @Retention(SOURCE)
    @StringDef({TOAST_SUCCESS, TOAST_WARNING, TOAST_ERROR, TOAST_INFO, TOAST_DEFAULT, TOAST_CONFUSING})
    public @interface ToastType {
    }

    public static final String TOAST_SUCCESS = "success";
    public static final String TOAST_WARNING = "warning";
    public static final String TOAST_ERROR = "error";
    public static final String TOAST_INFO = "info";
    public static final String TOAST_DEFAULT = "default";
    public static final String TOAST_CONFUSING = "confusing";

    @Retention(SOURCE)
    @StringDef({SIGN_IN_METHOD_EMAIL, SIGN_IN_METHOD_FINGERPRINT})
    public @interface SigInMethod {
    }

    public static final String SIGN_IN_METHOD_EMAIL = "email";
    public static final String SIGN_IN_METHOD_FINGERPRINT = "fingerprint";

    @Retention(SOURCE)
    @StringDef({BACKEND_STATUS_SUCCESS, BACKEND_STATUS_FAILED})
    public @interface BackendStatus {
    }

    public static final String BACKEND_STATUS_SUCCESS = "success";
    public static final String BACKEND_STATUS_FAILED = "failed";


    public static Analytics getInstance() {
        if (instance == null)
            synchronized (Analytics.class) {
                if (instance == null)
                    instance = new Analytics();
            }
        return instance;
    }

    private Analytics() {
        initFirebaseAnalyticsInstance();
    }

    private void initFirebaseAnalyticsInstance() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(ConnactionCheckApplication.getInstance());
    }

    public static void setUserId(@NonNull String userId) {
        FirebaseAnalytics.getInstance(ConnactionCheckApplication.getInstance()).setUserId(userId);
    }

    public void logDialogShow(@NonNull final Class<?> clazz) {
        logDialog(clazz, "dialog_show");
    }

    public void logDialogDismiss(@NonNull final Class<?> clazz) {
        logDialog(clazz, "dialog_dismiss");
    }

    private void logDialog(@NonNull final Class<?> clazz, @NonNull final String action) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, clazz.getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, clazz.getName());
        bundle.putString("action", action);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logScreen(@NonNull final Class<?> clazz) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, clazz.getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, clazz.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logToast(@NonNull final String toastText, @NonNull @ToastType final String toastType) {
        Bundle bundle = new Bundle();
        bundle.putString("toast_text", toastText);
        bundle.putString("toast_type", toastType);
        firebaseAnalytics.logEvent("toast_shown", bundle);
    }

    public void logButtonClick(@NonNull final Button button) {
        logViewClick(button, button.getText().toString(), "button");
    }

    public void logViewClick(@NonNull final View view, @NonNull final String viewName, @NonNull final String viewType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(view.getId()));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, viewName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, viewType);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void logServiceStarted() {
        Bundle bundle = getCurrentRouteBundle();
        firebaseAnalytics.logEvent("service_started", bundle);
    }

    public void logServiceStopped() {
        Bundle bundle = getCurrentRouteBundle();

        Executors.newSingleThreadExecutor().execute(() -> {
            RouteDatabase routeDatabase = RouteDatabase.getInstance(ConnactionCheckApplication.getInstance());
            String routeId = bundle.getString("route_id");

            bundle.putInt("total_points_captured", routeDatabase.routePointDao().getAllUnSyncedRoutePointsCount(routeId));
            bundle.putInt("total_poi_captured", routeDatabase.poiPointDao().getCount(routeId));

            firebaseAnalytics.logEvent("service_stopped", bundle);
        });
    }

    public void logSignInAttempt(@NonNull @SigInMethod String method, @NonNull String email) {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        firebaseAnalytics.logEvent("login_attempt", bundle);
    }

    public void logSignInSuccess(@NonNull @SigInMethod String method, @NonNull String email) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    public void logSignInFailed(@NonNull @SigInMethod String method, @NonNull String email, @NonNull final String reason) {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("reason", reason);
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        firebaseAnalytics.logEvent("login_failed", bundle);
    }

    public void logLogout(@NonNull final boolean isServiceRunning) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isServiceRunning", isServiceRunning);
        firebaseAnalytics.logEvent("logged_out", bundle);
    }

    public void logSessionExpired() {
        logEvent("session_expired");
    }

    public void logRouteSynced(@NonNull String routeId) {
        logRouteSyncStatus(routeId, null);
    }

    public void logRouteAlreadySynced() {
        firebaseAnalytics.logEvent("route_sync_up_to_date", null);
    }

    public void logRouteAllSynced() {
        firebaseAnalytics.logEvent("route_sync_complete", null);
    }

    public void logRouteSyncFailed(@NonNull String routeId, @NonNull final String reason) {
        logRouteSyncStatus(routeId, reason);
    }

    public void logBackendRequest(@NonNull final String endPoint) {
        Bundle bundle = new Bundle();
        bundle.putString("endpoint", endPoint);
        firebaseAnalytics.logEvent("backend_request", bundle);
    }

    public void logBackendResponse(@NonNull final String endPoint,
                                   @NonNull @BackendStatus final String backendStatus,
                                   @Nullable final BackendError failureReason) {
        Bundle bundle = new Bundle();
        bundle.putString("endpoint", endPoint);
        bundle.putString("status", backendStatus);

        if (failureReason != null)
            bundle.putString("failureReason", failureReason.toString());

        firebaseAnalytics.logEvent("backend_response", bundle);
    }

    public void logMapReady() {
        firebaseAnalytics.logEvent("map_ready", null);
    }

    public void logSavedRoutesPlotted() {
        firebaseAnalytics.logEvent("saved_route_plotted", null);
    }

    public void logPoiAdded(@NonNull final PoiPoint poiPoint) {
        Bundle bundle = new Bundle();
        bundle.putString("poi_name", poiPoint.getName());
        bundle.putString("poi_type", poiPoint.getType());
        bundle.putString("poi_lat", poiPoint.getCoordinate().getLatitude());
        bundle.putString("poi_lng", poiPoint.getCoordinate().getLongitude());

        firebaseAnalytics.logEvent("poi_added", bundle);
    }

    public void logPoiAddFailed(@NonNull final String reason) {
        Bundle bundle = new Bundle();
        bundle.putString("reason", reason);
        firebaseAnalytics.logEvent("poi_addition_failed", bundle);
    }

    public void logPermissionRequested(@NonNull String permissionName) {
        Bundle bundle = new Bundle();
        bundle.putString("name", permissionName);
        firebaseAnalytics.logEvent("permission_requested", bundle);
    }

    public void logPermissionGranted(@NonNull String permissionName) {
        Bundle bundle = new Bundle();
        bundle.putString("name", permissionName);
        firebaseAnalytics.logEvent("permission_granted", bundle);
    }

    public void logPermissionDenied(@NonNull String permissionName) {
        Bundle bundle = new Bundle();
        bundle.putString("name", permissionName);
        firebaseAnalytics.logEvent("permission_denied", bundle);
    }

    public void logPermissionDeniedPermanently(@NonNull String permissionName) {
        Bundle bundle = new Bundle();
        bundle.putString("name", permissionName);
        firebaseAnalytics.logEvent("permission_denied_permanently", bundle);
    }

    public void logGpsAlertShown() {
        firebaseAnalytics.logEvent("gps_alert_shown", null);
    }

    public void logEvent(@NonNull final String key) {
        logEvent(key, null);
    }

    public void logEvent(@NonNull final String key, @Nullable final Bundle bundle) {
        firebaseAnalytics.logEvent(key, bundle);
    }

    private Bundle getCurrentRouteBundle() {
        synchronized (Analytics.class) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    ConnactionCheckApplication.getInstance().getApplicationContext());
            Bundle bundle = new Bundle();

            bundle.putString("vehicle_id", sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_ID, "NA"));
            bundle.putString("route_id", sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "NA"));
            bundle.putString("vehicle_number", sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_NUMBER, "NA"));
            bundle.putString("route_name", sharedPreferences.getString(AppConfig.KEY_CURRENT_ROUTE_NAME, "NA"));

            return bundle;
        }
    }

    private void logRouteSyncStatus(@NonNull final String routeId, @Nullable final String reason) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Bundle bundle = new Bundle();
            RoutePoint routePoint = RouteDatabase.getInstance(ConnactionCheckApplication.getInstance()).routePointDao().getRoutePoint(routeId);

            if (routePoint != null) {
                bundle.putString("vehicle_id", routePoint.getMetaRouteInfo().getVehicleId());
                bundle.putString("route_id", routePoint.getMetaRouteInfo().getRouteClientId());
                bundle.putString("vehicle_number", routePoint.getMetaRouteInfo().getVehicleId());
                bundle.putString("route_name", routePoint.getMetaRouteInfo().getRouteName());
            } else {
                bundle.putString("vehicle_id", "");
                bundle.putString("route_id", "");
                bundle.putString("vehicle_number", "");
                bundle.putString("route_name", "");
            }

            if (reason != null) {
                bundle.putString("reason", reason);
                firebaseAnalytics.logEvent("route_sync_failed", bundle);
            } else
                firebaseAnalytics.logEvent("route_sync_success", bundle);
        });

    }

}
