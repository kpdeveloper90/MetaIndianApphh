package com.ecosense.app.activity.metaData;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import com.ecosense.app.R;
import com.ecosense.app.Traccar.TrackingControllerNew;
import com.ecosense.app.Traccar.TrackingService;
import com.ecosense.app.activity.LoginActivity;
import com.ecosense.app.activity.SettingActivity;
import com.ecosense.app.activity.viewModel.MetadataDashboardViewModel;
import com.ecosense.app.adapter.ContactListAdapter;
import com.ecosense.app.broadcastReceiver.GpsLocationReceiver;
import com.ecosense.app.databinding.ActivityMetaDataDashBoardNewBinding;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.firebase.MessagingService;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.LocationHelper;
import com.ecosense.app.helper.PermissionHelper;
import com.ecosense.app.helper.RouteSyncHelper;
import com.ecosense.app.helper.alertDialog.AddEditPoiDialog;
import com.ecosense.app.helper.alertDialog.AlertDialogHelper;
import com.ecosense.app.helper.alertDialog.ConfirmationDialogHelper;
import com.ecosense.app.helper.alertDialog.StartPlottingDialog;
import com.ecosense.app.helper.bottomSheet.PoiBottomSheet;
import com.ecosense.app.helper.preference.EncryptedPreferenceHelper;
import com.ecosense.app.helper.view.TextSetter;
import com.ecosense.app.pojo.model.Contact;
import com.ecosense.app.pojo.model.MetadataUser;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.MetadataUserBackend;
import com.ecosense.app.util.AnimUtils;

/**
 * <h1>Activity class for the dashboard of metadata module</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetaDataDashBoard extends BaseConnectionActivity implements
        MetadataUserBackend.OnMetadataUserBackendListener,
        StartPlottingDialog.OnStartPlottingDialogListener,
        RouteSyncHelper.RouteSyncListener {

    private Analytics analytics;

    private ActivityMetaDataDashBoardNewBinding rootBinding;
    private MetadataDashboardViewModel viewModel;
    private ArrayList<Contact> contactNumberList;
    private ContactListAdapter contactListAdapter;
    private PoiBottomSheet poiBottomSheet;
    private PermissionHelper permissionHelper;
    private GpsLocationReceiver gpsLocationReceiver;
    private StartPlottingDialog startPlottingDialog;
    private AddEditPoiDialog addEditPoiDialog;
    private RouteSyncHelper routeSyncHelper;
    private Handler syncStatusHandler;
    private boolean isSyncFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityMetaDataDashBoardNewBinding.inflate(getLayoutInflater());
        setContentView(rootBinding.getRoot());
        initToolbar(rootBinding.includedToolbar.toolbar, getString(R.string.toolbar_metadata__dashboard_title));

        initConfigs();
        initViews();
        setListeners();
        syncRoute(null, null, 0, null);
//        getUserInfo();

        new MessagingService().getToken(this);
    }

    private void initConfigs() {
        analytics = Analytics.getInstance();
        viewModel = new ViewModelProvider(this).get(MetadataDashboardViewModel.class);
        routeSyncHelper = RouteSyncHelper.getInstance(this);
        syncStatusHandler = new Handler();
        isSyncFailed = false;
        gpsLocationReceiver = new GpsLocationReceiver();
        permissionHelper = new PermissionHelper(this);
        startPlottingDialog = new StartPlottingDialog(this, this);
        addEditPoiDialog = new AddEditPoiDialog(this, true);
        poiBottomSheet = new PoiBottomSheet();
        contactNumberList = new ArrayList<>();
        contactListAdapter = new ContactListAdapter(contactNumberList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        rootBinding.rvContactList.setHasFixedSize(true);
        rootBinding.rvContactList.setLayoutManager(layoutManager);
        rootBinding.rvContactList.setItemAnimator(new DefaultItemAnimator());
        rootBinding.rvContactList.setAdapter(contactListAdapter);
    }

    private void initViews() {
        EncryptedPreferenceHelper preferenceHelper = new EncryptedPreferenceHelper(this);

        if (preferenceHelper.getPrefMetaUserImageUrl() != null)
            Glide.with(this).load(preferenceHelper.getPrefMetaUserImageUrl())
                    .apply(RequestOptions.centerCropTransform())
                    .into(rootBinding.imgUser);

        TextSetter.setText(rootBinding.tvName, preferenceHelper.getMetaUserName());
        TextSetter.setText(rootBinding.tvMobile, preferenceHelper.getMetaUserMobile());
        TextSetter.setText(rootBinding.tvEmail, preferenceHelper.getMetaUserEmail());

//        if (metadataUser.getContactList() != null && !metadataUser.getContactList().isEmpty()) {
//            contactNumberList.clear();
//            contactNumberList.addAll(metadataUser.getContactList());
//            contactListAdapter.notifyDataSetChanged();
//        }

        updateRouteCardStatus(isServiceRunning());
    }

    private void setListeners() {
        routeSyncHelper.setListener(this);

        rootBinding.btnStartRound.setOnClickListener(v -> {
            if (rootBinding.btnStartRound.getText().toString().equalsIgnoreCase(getString(R.string.button_start_round))) {
                analytics.logButtonClick(rootBinding.btnStartRound);
                startPlottingDialog.show();

            } else {
                ConfirmationDialogHelper.Builder builder = new ConfirmationDialogHelper.Builder(this);

                builder.setTitle(getString(R.string.dialog_stop_plotting_title));
                builder.setMessage(getString(R.string.dialog_stop_plotting_description));
                builder.setPositiveButton(null, dialogInstance -> stopTrackingService());
                builder.show();
            }
        });

        rootBinding.btnViewMap.setOnClickListener(v -> {
            analytics.logButtonClick(rootBinding.btnViewMap);
            startActivity(new Intent(this, RouteMapActivity.class));
        });

        rootBinding.setting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
        });

        rootBinding.fabAdd.setOnClickListener(v -> {
            analytics.logButtonClick(rootBinding.btnViewMap);
            addEditPoiDialog.showAddDialog(null);
            analytics.logDialogShow(addEditPoiDialog.getClass());
        });

        rootBinding.fabListPoi.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String routeId = sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "");
                List<PoiPoint> poiPointList = RouteDatabase.getInstance(MetaDataDashBoard.this).poiPointDao().getAllPoints(routeId);

                runOnUiThread(() -> {
                    poiBottomSheet.setPoiPointList(poiPointList);
                    poiBottomSheet.setRoutePlottingInstance(true);
                    poiBottomSheet.show(getSupportFragmentManager(), "poiBottomSheet");
                });
            });

        });

        rootBinding.btViewRoutes.setOnClickListener(v -> {
            startActivity(new Intent(this, RouteListActivity.class));
        });

        addEditPoiDialog.setListener(new AddEditPoiDialog.OnStartPlottingDialogListener() {
            @Override
            public void onAddClicked(AlertDialogHelper dialogInstance, @NonNull final PoiPoint poiPoint, final boolean isSuccessful) {
                if (isSuccessful) poiBottomSheet.addPoi(poiPoint);
            }
        });
    }

    private void getUserInfo() {
        // TODO: 18-03-2021 remove this whole sharedPreferences initialization once user info is being fetched
        //region REMOVE THIS
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(AppConfig.KEY_DEVICE, "vivek").apply();
        sharedPreferences.edit().putBoolean(AppConfig.KEY_STATUS, Connection.KEY_STATUS_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_INTERVAL, Connection.KEY_INTERVAL_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ANGLE, Connection.KEY_ANGLE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_DISTANCE, Connection.KEY_DISTANCE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_URL, Connection.Traccar_url_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ACCURACY, Connection.KEY_ACCURACY_value).apply();
        //endregion
//        new MetadataUserBackend(this, this)
//                .getMetadataUserInfo(new User(userSessionManger.getpsNo(), null), 1500);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        try {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (TrackingService.class.getName().equals(service.service.getClassName())) {
                    Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                    return true;
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, " TrackingService NULL-POINTER return false");
            return false;
        }

        Log.e(TAG, " TrackingService not Running  return false");
        return false;
    }

    private void startTrackingService() {
        if (!isServiceRunning()) {
            ContextCompat.startForegroundService(MetaDataDashBoard.this,
                    new Intent(MetaDataDashBoard.this, TrackingService.class));

            updateRouteCardStatus(true);
            gpsLocationReceiver.register(this);
            CrashAnalytics.setServiceRunning(true);
            analytics.logServiceStarted();
        }
    }

    private void stopTrackingService() {
        if (isServiceRunning()) {
            stopService(new Intent(this, TrackingService.class));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            syncRoute(sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_ID, "null"),
                    sharedPreferences.getString(AppConfig.KEY_CURRENT_ROUTE_NAME, "null"),
                    TrackingControllerNew.TIME_TO_WAIT_FOR_DB_PROCESS_IN_MILLI, sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "null"));

            gpsLocationReceiver.unRegister(this);
            updateRouteCardStatus(false);

            TastyToast.makeText(getApplicationContext(), getString(R.string.route_Plotting_Stop), TastyToast.LENGTH_SHORT, TastyToast.INFO);
            analytics.logToast(getString(R.string.route_Plotting_Stop), Analytics.TOAST_ERROR);

            CrashAnalytics.setServiceRunning(false);
            analytics.logServiceStopped();
        }
    }

    private void showExitDialog() {
        ConfirmationDialogHelper.Builder builder = new ConfirmationDialogHelper.Builder(this);

        builder.setTitle(getString(R.string.dialog_logout_title));
        builder.setMessage(isServiceRunning() ?
                getString(R.string.dialog_route_plotting_logout_description) :
                getString(R.string.dialog_logout_description));

        builder.setPositiveButton(null, dialogInstance -> {
            stopTrackingService();
            dialogInstance.dismiss();

            CrashAnalytics.setLoggedIn(false);
            analytics.logLogout(dialogInstance.getMessage().equals(getString(R.string.dialog_route_plotting_logout_description)));
            new EncryptedPreferenceHelper(this).setMetaUserId("");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.show();
    }

    private void updateRouteCardStatus(final boolean isServiceRunning) {
        if (isServiceRunning) {
            rootBinding.cardRoute.setStrokeColor(ContextCompat.getColor(this, R.color.danger));
            rootBinding.btnStartRound.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MetaDataDashBoard.this, R.color.danger)));
            rootBinding.tvRoutePlotting.setTextColor(ContextCompat.getColor(this, R.color.danger));
            rootBinding.btnStartRound.setText(getString(R.string.button_stop_round));
            rootBinding.btnViewMap.setVisibility(View.VISIBLE);
            rootBinding.groupFab.setVisibility(View.VISIBLE);
            rootBinding.fabAdd.setVisibility(View.VISIBLE);

        } else {
            rootBinding.cardRoute.setStrokeColor(ContextCompat.getColor(this, R.color.colorPrimary));
            rootBinding.btnStartRound.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MetaDataDashBoard.this, R.color.colorPrimary)));
            rootBinding.tvRoutePlotting.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            rootBinding.btnStartRound.setText(getString(R.string.button_start_round));
            rootBinding.btnViewMap.setVisibility(View.GONE);
            rootBinding.groupFab.setVisibility(View.GONE);
        }
    }

    private void updateSyncingStatus(boolean isVisible) {
        if (isVisible) {
            rootBinding.groupSync.setVisibility(View.VISIBLE);
            AnimUtils.setRotatingAnimation(rootBinding.ivSync);
            AnimUtils.setBlinkingAnimation(rootBinding.tvSync);
        } else {
            rootBinding.groupSync.setVisibility(View.GONE);
            rootBinding.ivSync.clearAnimation();
            rootBinding.tvSync.clearAnimation();
        }
    }

    private void syncRoute(final String vehicleId, final String routeName, final int delay, String p_id) {
        updateSyncingStatus(true);

        new Handler().postDelayed(() -> {
            if (vehicleId == null || routeName == null)
                routeSyncHelper.sendUnSyncedRoutes(p_id);
            else routeSyncHelper.sendUnSyncedRoute(vehicleId, routeName, p_id);
        }, delay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_driver_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigation_logout) {
            showExitDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(MetadataUser metadataUser) {
//        initViews(metadataUser);
    }

    @Override
    public void onFailure(BackendError backendError) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isServiceRunning()) {
            permissionHelper.checkAndRequestLocationPermission();
            LocationHelper.isGpsLocationEnabled(this, permissionHelper);
            gpsLocationReceiver.register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gpsLocationReceiver.unRegister(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSyncSuccess(@NonNull String routeId) {
        analytics.logRouteSynced(routeId);
    }

    @Override
    public void onAllSynced(boolean alreadyUpToDate) {
        runOnUiThread(() -> {
            updateSyncingStatus(false);

            if (!isSyncFailed) {
                TastyToast.makeText(this, alreadyUpToDate ?
                                getString(R.string.toast_already_synced) : getString(R.string.toast_sync_complete),
                        TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                if (alreadyUpToDate)
                    analytics.logRouteAlreadySynced();
                else analytics.logRouteAllSynced();

            } else
                TastyToast.makeText(this, getString(R.string.toast_sync_error),
                        TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        });
    }

    @Override
    public void onSyncError(@NonNull String routeId, BackendError backendError) {
        isSyncFailed = true;
        syncStatusHandler.removeCallbacksAndMessages(null);
        syncStatusHandler.postDelayed(() -> isSyncFailed = false, 2000);

        CrashAnalytics.log("Route Sync Error:" + backendError.toString());
        analytics.logRouteSyncFailed(routeId, backendError.toString());
    }

    @Override
    public void onStartButtonClicked(AlertDialogHelper dialogInstance) {
        startTrackingService();
    }

    @Override
    public void onCountdownComplete() {
        TastyToast.makeText(this, getString(R.string.route_Plotting_Start),
                TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
    }
}