package com.ecosense.app.helper.alertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mvivekanandji.validatingtextinputlayout.TextInputLayoutValidator;
import com.mvivekanandji.validatingtextinputlayout.ValidatingTextInputLayout;
import com.sdsmdg.tastytoast.TastyToast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ecosense.app.R;
import com.ecosense.app.activity.metaData.RouteMapActivity;
import com.ecosense.app.broadcastReceiver.NetworkReceiver;
import com.ecosense.app.databinding.LayoutDialogAddPoiBinding;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.Coordinate;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.LocationHelper;
import com.ecosense.app.helper.PermissionHelper;
import com.ecosense.app.pojo.response.PoiCreateResponse;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;
import com.ecosense.app.util.AnimUtils;

/**
 * <h1>Helper class for showing dialog to add poi along the route.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class AddEditPoiDialog extends AlertDialogHelper
        implements TextInputLayoutValidator.ValidatorListener {

    public interface OnStartPlottingDialogListener {
        default void onAddClicked(AlertDialogHelper dialogInstance, @NonNull final PoiPoint poiPoint, final boolean isSuccessful) {
        }

        default void onModifyClicked(AlertDialogHelper dialogInstance, @NonNull final PoiPoint poiPoint, final boolean isSuccessful) {
        }

        default void onCancelClicked(AlertDialogHelper dialogInstance) {
            dialogInstance.dismiss();
            popupopen = false;
        }
    }

    private static String currentPoiTitle;

    private LayoutDialogAddPoiBinding dialogBinding;
    private final ExecutorService executorService;
    private final RouteDatabase routeDatabase;
    private OnStartPlottingDialogListener dialogListener;
    private TextInputLayoutValidator inputLayoutValidator;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PermissionHelper permissionHelper;
    private CodeScanner codeScanner;
    private GoogleMap map;
    private Marker currentPoiMarker;
    private PoiPoint poiPoint;
    private String poiType;
    private String poiName;
    private boolean isAddInvoked;
    private boolean isRoutePlottingInstance;
    public static boolean popupopen = false;

    private Analytics analytics;

    public AddEditPoiDialog(@NonNull final Activity activity, final boolean isRoutePlottingInstance) {
        this(activity, null);
        this.isRoutePlottingInstance = isRoutePlottingInstance;
    }

    public AddEditPoiDialog(@NonNull final Activity activity) {
        this(activity, null);
    }

    public AddEditPoiDialog(@NonNull final Activity activity,
                            final OnStartPlottingDialogListener dialogListener) {
        super(activity);
        this.dialogListener = dialogListener;
        this.executorService = Executors.newSingleThreadExecutor();
        routeDatabase = RouteDatabase.getInstance(activity.getApplicationContext());
        permissionHelper = new PermissionHelper(activity);
        currentPoiTitle = activity.getString(R.string.tv_current_location);
        analytics = Analytics.getInstance();
    }

    public void setRoutePlottingInstance(boolean routePlottingInstance) {
        isRoutePlottingInstance = routePlottingInstance;
    }

    public void setListener(OnStartPlottingDialogListener listener) {
        this.dialogListener = listener;
    }

    @Deprecated
    @Override
    public void show() {
        showAddDialog(null);
    }

    public void showAddDialog(@Nullable final String routeId) {
        if (isRoutePlottingInstance && !popupopen)
            executorService.execute(() -> {
                popupopen = true;
                RoutePoint lastRoutePoint = routeDatabase.routePointDao().getLastRoutePoint();
                poiPoint = new PoiPoint();
                poiPoint.setCoordinate(lastRoutePoint != null ? lastRoutePoint.getCoordinate() : null);

                activity.runOnUiThread(() -> show(true));
            });
        else {
            if(!popupopen){
            popupopen = true;
            poiPoint = new PoiPoint();
            poiPoint.setServerRouteId(routeId);
            show(true);}
        }
    }

    public void showEditDialog(@NonNull final PoiPoint poiPoint) {
        this.poiPoint = poiPoint;
        show(false);
    }

    private void show(boolean isAddInvoked) {
        this.isAddInvoked = isAddInvoked;
        dialogBinding = LayoutDialogAddPoiBinding.inflate(activity.getLayoutInflater());
        codeScanner = new CodeScanner(activity, dialogBinding.qrScanner);
        initViews(poiPoint);
        initListeners();
        createCustomAlertDialog(dialogBinding.getRoot(), false);
    }

    private void initViews(@Nullable final PoiPoint poiPoint) {
        try {
            MapsInitializer.initialize(activity);
            dialogBinding.map.onCreate(onSaveInstanceState());
            dialogBinding.map.onResume();

            AnimUtils.setRotatingAnimation(dialogBinding.ivLoading);
            dialogBinding.ivLoading.setVisibility(View.VISIBLE);
            dialogBinding.map.setVisibility(View.GONE);
            initTypeAdapter();

            if (!isRoutePlottingInstance && isAddInvoked) {
                retrieveLastKnownLocation(true);
            }

            if (isAddInvoked) {
                dialogBinding.tvTitle.setText(activity.getString(R.string.dialog_add_poi_title));
                dialogBinding.btYes.setText(activity.getString(R.string.button_add));
            } else {
                dialogBinding.tvTitle.setText(activity.getString(R.string.dialog_modify_poi_title));
                dialogBinding.btYes.setText(activity.getString(R.string.button_modify));

                this.poiPoint = poiPoint;
                poiType = poiPoint.getType();
                poiName = poiPoint.getName();
            }

            if (!TextUtils.isEmpty(poiType))
                dialogBinding.etPoiType.setText(poiType, false);

            if (!TextUtils.isEmpty(poiName))
                dialogBinding.etPoiName.setText(poiName);

            if (isRoutePlottingInstance || !isAddInvoked) {
                dialogBinding.cpLat.setText(activity.getString(R.string.tv_lat_with_data, poiPoint.getCoordinate().getNumericLatitude()));
                dialogBinding.cpLon.setText(activity.getString(R.string.tv_lon_with_data, poiPoint.getCoordinate().getNumericLongitude()));
            }
        } catch (Exception e) {
        }

    }

    private void initTypeAdapter() {
        inputLayoutValidator = new TextInputLayoutValidator(dialogBinding.getRoot(), this);

        ArrayAdapter<String> areaUnitsAdapter = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_dropdown_item, getPoiType());

        dialogBinding.etPoiType.setAdapter(areaUnitsAdapter);
    }

    private void updateMarker() {
        if (currentPoiMarker != null)
            currentPoiMarker.remove();

        currentPoiMarker = map.addMarker(new MarkerOptions()
                .position(poiPoint.getLatLng())
                .title(currentPoiTitle));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPoiMarker.getPosition(), RouteMapActivity.NAV_ZOOM - 2f));
    }

    private void setupScanner() {
        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);
    }

    @Override
    public void initListeners() {
        try {
            dialogBinding.map.getMapAsync(googleMap -> {
                map = googleMap;
                dialogBinding.ivLoading.clearAnimation();
                dialogBinding.ivLoading.setVisibility(View.GONE);
                dialogBinding.map.setVisibility(View.VISIBLE);

                if (isRoutePlottingInstance || !isAddInvoked)
                    updateMarker();
            });

            dialogBinding.btFetchCoordinates.setOnClickListener(v -> {
                dialogBinding.btYes.setEnabled(false);
                retrieveLastKnownLocation(true);
            });

            dialogBinding.btScanQr.setOnClickListener(v -> {
                permissionHelper.checkAndRequestCameraPermission(permissionName -> {
                    setupScanner();
                    dialogBinding.cdScanner.setVisibility(View.VISIBLE);
                    codeScanner.startPreview();
                    dialogBinding.nsRoot.smoothScrollTo(0, dialogBinding.nsRoot.getChildAt(0).getHeight());
                });
            });

            codeScanner.setDecodeCallback(result -> activity.runOnUiThread(() -> {
                dialogBinding.etPoiQr.setText(result.getText());
                dialogBinding.cdScanner.setVisibility(View.GONE);
            }));

            dialogBinding.btYes.setOnClickListener(v -> {
                hideKeyboard(dialogBinding.getRoot().getFocusedChild());

                executorService.execute(() -> {
                    if (isAddInvoked) {
                        activity.runOnUiThread(() -> {
                            if (poiPoint.getCoordinate() == null) {
                                TastyToast.makeText(activity, activity.getString(R.string.toast_add_poi_error),
                                        TastyToast.LENGTH_SHORT, TastyToast.ERROR);

                                analytics.logPoiAddFailed("No last route point available!");
                                this.dismiss();
                                popupopen = false;
                            }
                        });

                        if (poiPoint.getCoordinate() != null) {
                            PoiPoint poiPoint = new PoiPoint();
                            poiPoint.setTimestamp(System.currentTimeMillis());
                            poiPoint.setCoordinate(this.poiPoint.getCoordinate());
                            poiPoint.setType(dialogBinding.etPoiType.getText().toString());
                            poiPoint.setName(dialogBinding.etPoiName.getText().toString());
                            poiPoint.setQrCode(dialogBinding.etPoiQr.getText().toString());

                            if (isRoutePlottingInstance) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                                poiPoint.setRouteId(sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "null"));
                                poiPoint.setId(routeDatabase.poiPointDao().insertPoint(poiPoint));

                                finalizeAddRequest(poiPoint, poiPoint.getId() > 0);

                            } else { //route already saved, save poi directly to the server
                                poiPoint.setServerRouteId(this.poiPoint.getServerRouteId());
                                BackEndRequestCall.enqueue(RetrofitHelper.createPoi(poiPoint), "createPoi", new BackEndRequestCall.BackendRequestListener() {
                                    @Override
                                    public void onSuccess(String tag, @NonNull Object responseBody) {
                                        finalizeAddRequest(((PoiCreateResponse) responseBody).getPoiPoint(), true);
                                    }

                                    @Override
                                    public void onError(String tag, @NonNull BackendError backendError) {
                                        finalizeAddRequest(poiPoint, false);
                                    }
                                });

                            }

                        }
                    } else {//edit poi invoked
                        poiPoint.setType(dialogBinding.etPoiType.getText().toString());
                        poiPoint.setName(dialogBinding.etPoiName.getText().toString());
                        poiPoint.setQrCode(dialogBinding.etPoiQr.getText().toString());
                        if (isRoutePlottingInstance) {//update poi on device
                            finalizeModifyRequest(routeDatabase.poiPointDao().updatePoint(poiPoint) > 0);

                        } else {//send update request to the server
                            BackEndRequestCall.enqueue(RetrofitHelper.updatePoi(poiPoint.getServerId(), poiPoint),
                                    "updatePoi", new BackEndRequestCall.BackendRequestListener() {
                                        @Override
                                        public void onSuccess(String tag, @NonNull Object responseBody) {
                                            finalizeModifyRequest(true);
                                        }

                                        @Override
                                        public void onError(String tag, @NonNull BackendError backendError) {
                                            finalizeModifyRequest(false);
                                        }
                                    });
                        }
                    }
                });
            });

            dialogBinding.btCancel.setOnClickListener(v -> {
                if (isAddInvoked) {
                    poiType = dialogBinding.etPoiType.getText().toString();
                    poiName = dialogBinding.etPoiName.getText().toString();
                }

                if (dialogListener != null)
                    dialogListener.onCancelClicked(this);

                dismiss();
                popupopen = false;
            });
        } catch (Exception e) {
        }
    }

    private void finalizeAddRequest(@NonNull final PoiPoint poiPoint, final boolean isSuccessful) {
        if (dialogListener != null)
            dialogListener.onAddClicked(AddEditPoiDialog.this, poiPoint, isSuccessful);

        finalizeRequest(activity.getString(R.string.toast_add_poi_success),
                activity.getString(R.string.toast_add_poi_error), isSuccessful);
    }

    private void finalizeModifyRequest(final boolean isSuccessful) {
        if (dialogListener != null)
            dialogListener.onModifyClicked(AddEditPoiDialog.this, poiPoint, isSuccessful);

        poiType = null;
        poiName = null;

        finalizeRequest(activity.getString(R.string.toast_modify_poi_success),
                activity.getString(R.string.toast_modify_poi_error), isSuccessful);
    }

    private void finalizeRequest(@NonNull final String successMessage, @NonNull final String errorMessage, final boolean isSuccessful) {
        activity.runOnUiThread(() -> {
            TastyToast.makeText(activity, isSuccessful ? successMessage : errorMessage,
                    TastyToast.LENGTH_SHORT, isSuccessful ? TastyToast.SUCCESS : TastyToast.ERROR);

            dialogBinding.etPoiType.setText(null, false);
            dialogBinding.etPoiName.setText(null);
            analytics.logPoiAdded(poiPoint);
            this.dismiss();
            popupopen = false;
        });
    }

    private List<String> getPoiType() {
        return new ArrayList<String>() {{
            add("Residential");
            add("Open Spot");
            add("Bin");
            add("Commercial");
            add("Dump yard");
            add("Rotation Bins");
            add("Open Spot (Sweeping)");
            add("End Point");
        }};
    }

    //fetching location stuff
    @SuppressLint("MissingPermission")
    private void retrieveLastKnownLocation(boolean isViewReady) {
        permissionHelper.checkAndRequestLocationPermission(permissionName -> {
            if (LocationHelper.isGpsLocationEnabled(activity, permissionHelper)) { //checks as well force the user the switch on the gps
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(activity, location -> {
                            if (location != null && isViewReady)
                                updateCoordinates(location, false);

                            requestNewLocationData(isViewReady);
                        });
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(boolean isViewReady) {
        final int[] totalNetworkUpdatesNeeded = {5};
        final int[] totalNetworkUpdatesReceived = {1};

        new NetworkReceiver().checkConnectivityAndShowAppropriateAlert();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(5);
        locationRequest.setNumUpdates(totalNetworkUpdatesNeeded[0]);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (++totalNetworkUpdatesReceived[0] >= totalNetworkUpdatesNeeded[0] && isViewReady)
                    updateCoordinates(location, true);
            }
        }, Looper.myLooper());
    }

    @SuppressLint("SetTextI18n")
    private void updateCoordinates(@NonNull final Location location, final boolean isFreshLocation) {
        poiPoint.setCoordinate(new Coordinate(location.getLatitude(), location.getLongitude()));
        dialogBinding.cpLat.setText(activity.getString(R.string.tv_lat_with_data, location.getLongitude()));
        dialogBinding.cpLon.setText(activity.getString(R.string.tv_lon_with_data, location.getLatitude()));
        updateMarker();

        if (isFreshLocation) {
            updateFetchingCoordinateMessage(R.string.tv_coordinates_fetched, R.color.colorPrimary);
            dialogBinding.tvFetchingCoordinates.clearAnimation();

            if (inputLayoutValidator.isValid())
                dialogBinding.btYes.setEnabled(true);
        } else {
            dialogBinding.tvFetchingCoordinates.setVisibility(View.VISIBLE);
            updateFetchingCoordinateMessage(R.string.tv_fetching_coordinates, R.color.black);
            AnimUtils.setBlinkingAnimation(dialogBinding.tvFetchingCoordinates);
        }
    }

    private void updateFetchingCoordinateMessage(final @StringRes int textId, final @ColorRes int colorId) {
        dialogBinding.tvFetchingCoordinates.setText(activity.getString(textId));
        dialogBinding.tvFetchingCoordinates.setTextColor(activity.getResources().getColor(colorId));
        dialogBinding.tvFetchingCoordinates.setTypeface(dialogBinding.tvFetchingCoordinates.getTypeface(), Typeface.BOLD);
    }

    @Override
    public void onValidateErrors(List<ValidatingTextInputLayout> errorLayoutList, List<TextInputLayoutValidator.ValidationError> validationErrorList) {

    }

    @Override
    public void onError(ValidatingTextInputLayout inputLayout, TextInputLayoutValidator.ValidationError validationError, boolean isErrorOnValidate) {
        dialogBinding.btYes.setEnabled(false);
    }

    @Override
    public void onErrorResolved(ValidatingTextInputLayout inputLayout) {

    }

    @Override
    public void onSuccess() {
        dialogBinding.btYes.setEnabled(true);
    }
}
