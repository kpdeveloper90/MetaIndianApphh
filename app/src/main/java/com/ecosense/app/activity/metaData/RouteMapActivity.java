package com.ecosense.app.activity.metaData;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.chip.Chip;
import com.sdsmdg.tastytoast.TastyToast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import com.ecosense.app.R;
import com.ecosense.app.activity.EventBusActivity;
import com.ecosense.app.activity.viewModel.RouteMapViewModel;
import com.ecosense.app.adapter.PoiInfoWindowAdapter;
import com.ecosense.app.databinding.ActivityRouteMapBinding;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.Coordinate;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.RouteMapHelper;
import com.ecosense.app.helper.alertDialog.AddEditPoiDialog;
import com.ecosense.app.helper.alertDialog.AlertDialogHelper;
import com.ecosense.app.helper.bottomSheet.PoiBottomSheet;
import com.ecosense.app.pojo.eventBus.CoordinateUpdateMessageEvent;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;
import com.ecosense.app.util.GoogleMapUtil;

/**
 * <h1>Activity class for the route map of metadata module</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteMapActivity extends EventBusActivity
        implements OnMapReadyCallback {

    public static final String KEY_INVOCATION_TYPE_PLOTTING = "map_invocation_type";
    public static final String KEY_ROUTE_ID = "map_route_id";
    private static String currentMarkerTitle;

    public static final float NAV_ZOOM = 22f;
    public static final float ROUTE_ZOOM = 16f;

    private Analytics analytics;

    private ActivityRouteMapBinding rootBinding;
    private RouteMapViewModel viewModel;
    private BitmapDescriptor currentLocationBitmapDescriptor;
    private volatile List<Coordinate> receivedCoordinateList;
    private volatile List<Coordinate> savedCoordinateList;
    private volatile List<PoiPoint> savedPoiList;
    private volatile List<Marker> poiMarkerList;
    private GoogleMap map;
    private PoiBottomSheet poiBottomSheet;
    private RouteMapHelper routeMapHelper;
    private AddEditPoiDialog addEditPoiDialog;
    private Marker currentLocationMarker;
    private Marker dragMarker;
    private LatLng firstNewLatLng;
    private LatLng currentLatLng;
    private LatLng previousLatLng;
    private LatLng beforeDragLatLng;
    private LatLng clickedLatLng;
    private String routeId;
    private boolean isMapReady;
    private boolean isFirstUpdate;
    private boolean isNavEnabled;
    private boolean isUpdatingRoute;
    private boolean isFetchingData;
    private boolean isRoutePlottingInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityRouteMapBinding.inflate(getLayoutInflater());
        setContentView(rootBinding.getRoot());

        initConfigs();
        initViews();
        setListeners();
    }

    private void initConfigs() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        isRoutePlottingInstance = getIntent().getBooleanExtra(KEY_INVOCATION_TYPE_PLOTTING, true);
        currentMarkerTitle = getString(R.string.tv_current_location);

        analytics = Analytics.getInstance();
        viewModel = new ViewModelProvider(this).get(RouteMapViewModel.class);
        routeId = getIntent().getStringExtra(KEY_ROUTE_ID);
        routeMapHelper = new RouteMapHelper(this, rootBinding.includeRouteInfo, routeId, isRoutePlottingInstance);
        addEditPoiDialog = new AddEditPoiDialog(this, isRoutePlottingInstance);
        currentLocationBitmapDescriptor = getBitmapFromVector(this, R.drawable.ic_baseline_directions_car_24,
                ContextCompat.getColor(this, R.color.colorPrimary));
        receivedCoordinateList = new ArrayList<>();
        poiMarkerList = new ArrayList<>();
        isMapReady = false;
        isFirstUpdate = true;
        isNavEnabled = false;
        isUpdatingRoute = false;
        isFetchingData = false;
    }

    private void initViews() {
        if (!isRoutePlottingInstance) {
            rootBinding.fabMyLocation.setVisibility(View.GONE);
            rootBinding.fabNavigate.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        routeMapHelper.setListener((coordinates, poiPoints) -> {
            if (isMapReady && coordinates.size() > 0)
                plotSavedRouteAndPoi(coordinates, poiPoints);
            else {
                savedCoordinateList = coordinates;
                savedPoiList = poiPoints;
            }

            isFetchingData = false;
            poiBottomSheet = new PoiBottomSheet(poiPoints, isRoutePlottingInstance);
            poiBottomSheet.setListener(new PoiBottomSheet.OnPoiStateListener() {
                @Override
                public void onClicked(@NonNull PoiPoint poiPoint) {
                    poiBottomSheet.dismiss();
                    animateCamera(poiPoint.getLatLng(), NAV_ZOOM);
                }

                @Override
                public void onEditClicked(@NonNull PoiPoint poiPoint) {
                    poiBottomSheet.dismiss();
                    addEditPoiDialog.showEditDialog(poiPoint);
                }

                @Override
                public void onDeleteClicked(@NonNull PoiPoint poiPoint, final boolean isDeletionSuccessful) {
                    poiBottomSheet.dismiss();

                    if (isDeletionSuccessful) {
                        runOnUiThread(() -> {
                            removePoiMarker(poiPoint);
                            deletePoiChip(poiPoint);
                            routeMapHelper.decrementPoiCount();
                        });
                    }
                }
            });

            runOnUiThread(() -> rootBinding.fabAdd.setOnClickListener(v -> addEditPoiDialog.showAddDialog(routeId)));
        });

        addEditPoiDialog.setListener(new AddEditPoiDialog.OnStartPlottingDialogListener() {
            @Override
            public void onAddClicked(AlertDialogHelper dialogInstance, @NonNull final PoiPoint poiPoint, final boolean isSuccessful) {
                if (isSuccessful) {
                    runOnUiThread(() -> {
                        plotPoi(poiPoint);
                        routeMapHelper.incrementPoiCount();
                        poiBottomSheet.addPoi(poiPoint);
                        animateCamera(poiPoint.getLatLng(), NAV_ZOOM);
                    });
                }
            }

            @Override
            public void onModifyClicked(AlertDialogHelper dialogInstance, @NonNull PoiPoint poiPoint, final boolean isSuccessful) {
                if (isSuccessful) {
                    runOnUiThread(() -> {
                        modifyPoiMarker(poiPoint);
                        modifyPoiChip(poiPoint);
                        animateCamera(poiPoint.getLatLng(), NAV_ZOOM);
                    });
                }
            }
        });

        rootBinding.fabMyLocation.setOnClickListener(v -> {
            if (currentLocationMarker == null || currentLocationMarker.getPosition() == null)
                TastyToast.makeText(RouteMapActivity.this,
                        getString(R.string.toast_unable_to_fetch_current_location_error),
                        TastyToast.LENGTH_SHORT, TastyToast.CONFUSING);
            else
                animateCamera(currentLocationMarker.getPosition(), NAV_ZOOM);

        });

        rootBinding.fabNavigate.setOnClickListener(v -> {
            if (isNavEnabled) {
                isNavEnabled = false;
                rootBinding.fabNavigate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(RouteMapActivity.this, R.color.grey_400)));
                CrashAnalytics.log("navigation_disabled");
                analytics.logEvent("navigation_disabled");

            } else {
                isNavEnabled = true;
                rootBinding.fabNavigate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(RouteMapActivity.this, R.color.colorPrimary)));
                CrashAnalytics.log("navigation_disabled");
                analytics.logEvent("navigation_enabled");
            }
        });

        rootBinding.fabListPoi.setOnClickListener(v -> poiBottomSheet.show(getSupportFragmentManager(), "poiBottomSheet"));

        rootBinding.fabDirection.setOnClickListener(v -> GoogleMapUtil.startGoogleDirections(this, clickedLatLng));

        rootBinding.includeDragCard.btCancel.setOnClickListener(v -> disableDragMode(dragMarker));
    }

    private void addPoiChip(@NonNull final PoiPoint poiPoint) {
        Chip chip = new Chip(this);
        chip.setText(poiPoint.getName());
        chip.setChipBackgroundColorResource(R.color.colorPrimary);
        chip.setTextColor(getResources().getColor(R.color.white));
        chip.setClickable(true);
        chip.setTag(poiPoint);

        chip.setOnClickListener(v -> {
            PoiPoint chipPoiPoint = (PoiPoint) v.getTag();
            animateCamera(chipPoiPoint.getLatLng(), NAV_ZOOM);
        });

        runOnUiThread(() -> rootBinding.cgPoi.addView(chip, 0));
    }

    private void deletePoiChip(@NonNull final PoiPoint poiPoint) {
        for (int i = 0; i < rootBinding.cgPoi.getChildCount(); i++) {
            PoiPoint chipPoiPoint = (PoiPoint) (rootBinding.cgPoi.getChildAt(i)).getTag();

            if (isRoutePlottingInstance && poiPoint.getId() == chipPoiPoint.getId()) {
                int viewIndex = i;
                runOnUiThread(() -> rootBinding.cgPoi.removeViewAt(viewIndex));
                break;
            } else if (!isRoutePlottingInstance && poiPoint.getServerId().equals(chipPoiPoint.getServerId())) {
                int viewIndex = i;
                runOnUiThread(() -> rootBinding.cgPoi.removeViewAt(viewIndex));
                break;
            }
        }
    }

    private void modifyPoiChip(@NonNull final PoiPoint poiPoint) {
        for (int i = 0; i < rootBinding.cgPoi.getChildCount(); i++) {
            Chip chip = (Chip) rootBinding.cgPoi.getChildAt(i);
            PoiPoint chipPoiPoint = (PoiPoint) chip.getTag();

            if (isRoutePlottingInstance && poiPoint.getId() == chipPoiPoint.getId()) {
                runOnUiThread(() -> {
                    chip.setText(poiPoint.getName());
                    chip.setTag(poiPoint);
                });
                break;
            } else if (!isRoutePlottingInstance && poiPoint.getServerId().equals(chipPoiPoint.getServerId())) {
                runOnUiThread(() -> {
                    chip.setText(poiPoint.getName());
                    chip.setTag(poiPoint);
                });
                break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void modifyPoiMarker(@NonNull final PoiPoint poiPoint) {

        for (Marker marker : poiMarkerList) {
            PoiPoint markerPoiPoint = (PoiPoint) marker.getTag();

            if (markerPoiPoint != null) {
                if (isRoutePlottingInstance && poiPoint.getId() == markerPoiPoint.getId()) {
                    runOnUiThread(() -> {
                        marker.setPosition(poiPoint.getLatLng());
                        marker.setTag(poiPoint);
                    });
                    break;
                } else if (!isRoutePlottingInstance && poiPoint.getServerId().equals(markerPoiPoint.getServerId())) {
                    runOnUiThread(() -> {
                        marker.setPosition(poiPoint.getLatLng());
                        marker.setTag(poiPoint);
                    });
                    break;
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void removePoiMarker(@NonNull final PoiPoint poiPoint) {
        final Marker[] removedMarker = new Marker[1];
        for (Marker marker : poiMarkerList) {
            PoiPoint markerPoiPoint = (PoiPoint) marker.getTag();


            if (markerPoiPoint != null) {
                if (isRoutePlottingInstance && poiPoint.getId() == markerPoiPoint.getId()) {
                    runOnUiThread(() -> {
                        marker.remove();
                        removedMarker[0] = marker;

                    });
                    break;
                } else if (!isRoutePlottingInstance && poiPoint.getServerId().equals(markerPoiPoint.getServerId())) {
                    runOnUiThread(() -> {
                        marker.remove();
                        removedMarker[0] = marker;
                    });
                    break;
                }
            }
        }
        if (removedMarker[0] != null)
            poiMarkerList.remove(removedMarker[0]);
    }

    private void setMapListeners() {
        map.setOnMarkerClickListener(marker -> {
            clickedLatLng = marker.getPosition();
            if (clickedLatLng != null)
                rootBinding.fabDirection.setVisibility(View.VISIBLE);
            return false;
        });

        map.setOnInfoWindowClickListener(marker -> {
            marker.hideInfoWindow();
            addEditPoiDialog.showEditDialog((PoiPoint) Objects.requireNonNull(marker.getTag()));
        });

        map.setOnInfoWindowLongClickListener(this::enableDragMode);

        map.setOnInfoWindowCloseListener(marker -> rootBinding.fabDirection.setVisibility(View.GONE));

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                beforeDragLatLng = marker.getPosition();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                disableDragMode(marker);
                PoiPoint poiPoint = (PoiPoint) marker.getTag();

                if (poiPoint != null) {
                    poiPoint.setCoordinate(marker.getPosition());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (isRoutePlottingInstance) {
                            notifyPoiUpdateStatus(RouteDatabase.getInstance(RouteMapActivity.this).poiPointDao().updatePoint(poiPoint) > 0);
                        } else {
                            BackEndRequestCall.enqueue(RetrofitHelper.updatePoi(poiPoint.getServerId(), poiPoint),
                                    "updatePoi", new BackEndRequestCall.BackendRequestListener() {
                                        @Override
                                        public void onSuccess(String tag, @NonNull Object responseBody) {
                                            notifyPoiUpdateStatus(true);
                                        }

                                        @Override
                                        public void onError(String tag, @NonNull BackendError backendError) {
                                            notifyPoiUpdateStatus(false);
                                        }
                                    });
                        }
                    });

                } else {
                    marker.setPosition(beforeDragLatLng);
                    TastyToast.makeText(RouteMapActivity.this, getString(R.string.toast_modify_poi_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            }
        });
    }

    private void notifyPoiUpdateStatus(final boolean isUpdateSuccessful) {
        runOnUiThread(() -> TastyToast.makeText(RouteMapActivity.this, isUpdateSuccessful ?
                        getString(R.string.toast_modify_poi_success) : getString(R.string.toast_modify_poi_error),
                TastyToast.LENGTH_SHORT, isUpdateSuccessful ? TastyToast.SUCCESS : TastyToast.ERROR));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CrashAnalytics.setMapReady(true);
        analytics.logMapReady();
        map = googleMap;
        isMapReady = true;
        initMapSettings();
        setMapListeners();

//        final LatLng INDIA = new LatLng(20.5937, 78.9629);
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(INDIA, 10.0f));

        if (!isFetchingData && savedCoordinateList.size() > 0)
            plotSavedRouteAndPoi(savedCoordinateList, savedPoiList);
    }

    private void initMapSettings() {
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);

        map.setInfoWindowAdapter(new PoiInfoWindowAdapter(this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCoordinateUpdateEvent(CoordinateUpdateMessageEvent event) {
        Coordinate currentCoordinate = new Coordinate(event.getPosition().getLatitude(), event.getPosition().getLongitude());
        routeMapHelper.onCoordinateUpdated(event);
        receivedCoordinateList.add(currentCoordinate);

        if (isMapReady) {
            updateCurrentPosition(currentCoordinate);

            Iterator<Coordinate> iterator = receivedCoordinateList.iterator();
            while (iterator.hasNext()) {
                updateRoute(iterator.next());
                iterator.remove();
            }
        }
    }

    private void updateCurrentPosition(@NonNull final Coordinate coordinate) {
        if (currentLocationMarker != null)
            currentLocationMarker.remove();

        currentLocationMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(coordinate.getNumericLatitude(), coordinate.getNumericLongitude()))
                .title(currentMarkerTitle)
                .icon(currentLocationBitmapDescriptor)
        );
    }

    private void updateRoute(@NonNull final Coordinate coordinate) {
        currentLatLng = new LatLng(coordinate.getNumericLatitude(), coordinate.getNumericLongitude());

        if (previousLatLng != null)
            drawPolyLine(previousLatLng, currentLatLng);

        previousLatLng = currentLatLng;

        if (isFirstUpdate)
            firstNewLatLng = currentLatLng;

        if (isNavEnabled || isFirstUpdate) {
            animateCamera(currentLatLng, NAV_ZOOM);
            isFirstUpdate = false;
        }
    }

    private void plotSavedRouteAndPoi(@NonNull final List<Coordinate> savedCoordinates, @NonNull final List<PoiPoint> poiPoints) {
        plotRoute(savedCoordinates);
        plotPoi(poiPoints);
    }

    private void plotRoute(@NonNull final List<Coordinate> savedCoordinates) {
        List<LatLng> latLngList = new ArrayList<>(savedCoordinates.size());
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < savedCoordinates.size(); i++) {
            latLngList.add(new LatLng(savedCoordinates.get(i).getNumericLatitude(),
                    savedCoordinates.get(i).getNumericLongitude()));

            builder.include(latLngList.get(i));
        }

        if (firstNewLatLng != null)
            latLngList.add(firstNewLatLng);

        if (latLngList.size() > 2) {
            drawPolyLine(latLngList.toArray(new LatLng[0]));
            CrashAnalytics.log("Saved points fetched and plotted.");
            analytics.logSavedRoutesPlotted();
        }

        if (!isRoutePlottingInstance) {
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            map.setPadding(0, (int) (height * 0.25), 0, 0); //left, top, right, bottom
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, (int) (width * 0.9), (int) (height * 0.9), 0);

            runOnUiThread(() -> map.animateCamera(cameraUpdate));

            map.addMarker(new MarkerOptions()
                    .position(latLngList.get(0))
                    .title(getString(R.string.tv_route_starting_point))
                    .anchor(0.5f, 0.5f)
                    .icon(getBitmapFromVector(this, R.drawable.ic_baseline_circle_24,
                            ContextCompat.getColor(this, R.color.colorPrimary))));

            map.addMarker(new MarkerOptions()
                    .position(latLngList.get(latLngList.size() - 1))
                    .title(getString(R.string.tv_route_ending_point))
                    .anchor(0.5f, 0.5f)
                    .icon(getBitmapFromVector(this, R.drawable.ic_baseline_circle_24,
                            ContextCompat.getColor(this, R.color.danger))));

            addTerminalChip(latLngList.get(latLngList.size() - 1), getString(R.string.tv_route_ending_point));
            addTerminalChip(latLngList.get(0), getString(R.string.tv_route_starting_point));
        }
    }

    private void addTerminalChip(@NonNull final LatLng latLng, @NonNull final String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setTextColor(getResources().getColor(R.color.colorPrimary));
        chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        chip.setChipStrokeWidth(2f);
        chip.setClickable(true);
        chip.setTag(latLng);

        chip.setOnClickListener(v -> {
            animateCamera(latLng, NAV_ZOOM);
        });

        runOnUiThread(() -> rootBinding.cgTerminalPoints.addView(chip, 0));
    }

    private void plotPoi(@NonNull final PoiPoint poiPoint) {
        plotPoi(new ArrayList<PoiPoint>() {{
            add(poiPoint);
        }});
    }

    private void animateCamera(@NonNull final LatLng latLng, final float zoom) {
        runOnUiThread(() -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom)));
    }

    private void plotPoi(@NonNull final List<PoiPoint> poiPoints) {
        runOnUiThread(() -> {
            for (PoiPoint poiPoint : poiPoints) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(poiPoint.getCoordinate().getNumericLatitude(), poiPoint.getCoordinate().getNumericLongitude()))
                );
                marker.setTag(poiPoint);
                addPoiChip(poiPoint);

                poiMarkerList.add(marker);
            }
        });
    }

    private void drawPolyLine(LatLng... latLngList) {
        Polyline polyline = map.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(latLngList));

        stylePolyline(polyline);
    }

    private void stylePolyline(Polyline polyline) {
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(10);
        polyline.setColor(getResources().getColor(R.color.colorPrimary));
        polyline.setJointType(JointType.ROUND);
    }

    private void showStillFetchingData() {
        TastyToast.makeText(this, getString(R.string.toast_still_fetching_data), TastyToast.LENGTH_SHORT, TastyToast.INFO);
    }

    private void enableDragMode(@NonNull final Marker marker) {
        dragMarker = marker;

        marker.hideInfoWindow();
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        marker.setDraggable(true);
        showDragModeOverlays();

        animateCamera(marker.getPosition(), map.getCameraPosition().zoom);
    }

    private void disableDragMode(@NonNull final Marker marker) {
        marker.setDraggable(false);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        showNormalModeOverlays();
    }

    private void showDragModeOverlays() {
        TransitionManager.beginDelayedTransition(rootBinding.getRoot(), new Fade(Fade.MODE_OUT));
        rootBinding.includeRouteInfo.getRoot().setVisibility(View.GONE);
        rootBinding.cgPoi.setVisibility(View.GONE);
        rootBinding.cgTerminalPoints.setVisibility(View.GONE);
        rootBinding.fabListPoi.setVisibility(View.GONE);
        rootBinding.fabNavigate.setVisibility(View.GONE);
        rootBinding.fabMyLocation.setVisibility(View.GONE);
        rootBinding.fabAdd.setVisibility(View.GONE);

        TransitionManager.beginDelayedTransition(rootBinding.getRoot(), new Fade(Fade.MODE_IN));
        rootBinding.includeDragCard.getRoot().setVisibility(View.VISIBLE);
    }

    private void showNormalModeOverlays() {
        TransitionManager.beginDelayedTransition(rootBinding.getRoot(), new Fade(Fade.MODE_IN));
        rootBinding.includeRouteInfo.getRoot().setVisibility(View.VISIBLE);
        rootBinding.cgPoi.setVisibility(View.VISIBLE);
        rootBinding.cgTerminalPoints.setVisibility(View.VISIBLE);
        rootBinding.fabListPoi.setVisibility(View.VISIBLE);
        rootBinding.fabAdd.setVisibility(View.VISIBLE);

        if (isRoutePlottingInstance) {
            rootBinding.fabNavigate.setVisibility(View.VISIBLE);
            rootBinding.fabMyLocation.setVisibility(View.VISIBLE);
        }

        TransitionManager.beginDelayedTransition(rootBinding.getRoot(), new Fade(Fade.MODE_OUT));
        rootBinding.includeDragCard.getRoot().setVisibility(View.GONE);
    }

    public static BitmapDescriptor getBitmapFromVector(@NonNull final Context context,
                                                       @DrawableRes final int vectorResourceId,
                                                       @ColorInt final int tintColor) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResourceId);
        Drawable wrappedDrawable = vectorDrawable.mutate();

        if (wrappedDrawable == null)
            return BitmapDescriptorFactory.defaultMarker();

        wrappedDrawable.setBounds(0, 0, wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        wrappedDrawable = DrawableCompat.wrap(wrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, tintColor);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN);

        wrappedDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        //code here
        CrashAnalytics.log("Map activity: OnPause");
        isFirstUpdate = true;
        firstNewLatLng = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashAnalytics.log("Map activity: OnResume");
        routeMapHelper.refreshCurrentValues();
        rootBinding.fabAdd.setOnClickListener(v -> showStillFetchingData());
        isFetchingData = true;
    }

    @Override
    protected void onStop() {
        //code here
        CrashAnalytics.log("Map activity: OnStop");
        CrashAnalytics.setMapReady(false);
        super.onStop();
    }
}
