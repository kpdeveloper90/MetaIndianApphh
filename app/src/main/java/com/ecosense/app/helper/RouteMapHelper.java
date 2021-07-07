package com.ecosense.app.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ecosense.app.R;
import com.ecosense.app.Traccar.Position;
import com.ecosense.app.activity.metaData.RouteMapActivity;
import com.ecosense.app.databinding.LayoutRouteInfoBinding;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.Coordinate;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.view.TextSetter;
import com.ecosense.app.pojo.eventBus.CoordinateUpdateMessageEvent;
import com.ecosense.app.pojo.model.Path;
import com.ecosense.app.pojo.response.PoiResponse;
import com.ecosense.app.pojo.response.RouteResponse;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Helper class to assist in db queries and other functionality needed by {@link RouteMapActivity}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteMapHelper {

    public interface Listener {
        void onDataFetchComplete(@NonNull final List<Coordinate> coordinates, @NonNull final List<PoiPoint> poiPoints);
    }

    private final LayoutRouteInfoBinding infoBinding;
    private Listener listener;
    private final Activity activity;
    private final ExecutorService executorService;
    private final RouteDatabase routeDatabase;
    private final RouteTimerHelper routeTimerHelper;
    private Position previousPosition;
    private double totalDistance;
    private String routeName;
    private String vehicleNumber;
    private String routeId;
    private boolean isRoutePlottingRunning;

    public RouteMapHelper(@NonNull final Activity activity, LayoutRouteInfoBinding infoBinding, @Nullable String routeId, boolean isRoutePlottingRunning) {
        this.activity = activity;
        this.infoBinding = infoBinding;
        this.routeId = routeId;
        this.isRoutePlottingRunning = isRoutePlottingRunning;
        routeTimerHelper = RouteTimerHelper.getInstance();
        executorService = Executors.newFixedThreadPool(4);
        routeDatabase = RouteDatabase.getInstance(activity.getApplicationContext());
        totalDistance = 0;

        initViews();
        setListeners();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void initViews() {
        if (isRoutePlottingRunning) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            routeName = sharedPreferences.getString(AppConfig.KEY_CURRENT_ROUTE_NAME, "");
            vehicleNumber = sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_NUMBER, "");
            routeId = sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "");

            TextSetter.setText(infoBinding.tvRouteName, routeName);
            TextSetter.setText(infoBinding.tvVehicleNumber, vehicleNumber);
            TextSetter.setText(infoBinding.tvTime, routeTimerHelper.getTotalTime());
        } else {
            infoBinding.ivTime.setVisibility(View.GONE);
            infoBinding.ivSpeed.setVisibility(View.GONE);
            infoBinding.tvTime.setVisibility(View.GONE);
            infoBinding.tvSpeed.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        if (isRoutePlottingRunning)
            routeTimerHelper.setListener(formattedTime -> activity.runOnUiThread(() ->
                    infoBinding.tvTime.setText(formattedTime)));

        infoBinding.ivBack.setOnClickListener(v -> activity.onBackPressed());
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void refreshCurrentValues() {
        if (isRoutePlottingRunning) { // plotting in progress, get previous data from db
            executorService.execute(() -> {
                List<RoutePoint> routePoints = routeDatabase.routePointDao().getAllRoutePoints(routeId);
                List<PoiPoint> poiPoints = routeDatabase.poiPointDao().getAllPoints(routeId);
                List<Coordinate> coordinates = new ArrayList<>();
                double distanceTillNow = 0;

                if (routePoints.size() > 1) {
                    for (int i = 0; i < routePoints.size() - 1; i++) {
                        distanceTillNow += distanceInKm(
                                routePoints.get(i).getCoordinate().getNumericLatitude(),
                                routePoints.get(i).getCoordinate().getNumericLongitude(),
                                routePoints.get(i).getCoordinate().getNumericAltitude(),
                                routePoints.get(i + 1).getCoordinate().getNumericLatitude(),
                                routePoints.get(i + 1).getCoordinate().getNumericLongitude(),
                                routePoints.get(i + 1).getCoordinate().getNumericAltitude()
                        );
                        coordinates.add(routePoints.get(i).getCoordinate());
                    }
                    totalDistance += distanceTillNow;
                    CrashAnalytics.log("RouteMapHelper: routePoints > 1");
                } else if (routePoints.size() == 1) {
                    coordinates.add(routePoints.get(0).getCoordinate());
                    CrashAnalytics.log("RouteMapHelper: routePoints == 1");
                }

                if (coordinates.size() > 1)
                    coordinates.add(routePoints.get(routePoints.size() - 1).getCoordinate());

                activity.runOnUiThread(() -> {
                    if (listener != null)
                        listener.onDataFetchComplete(coordinates, poiPoints);

                    infoBinding.tvPoiMarked.setText(Integer.toString(poiPoints.size()));
                    infoBinding.tvDistance.setText(activity.getString(R.string.tv_distance_value, String.format("%.2f", totalDistance)));
                });
            });
        } else { // get data from server
            BackEndRequestCall.enqueue(RetrofitHelper.getRoute(routeId), "getSavedRoute", new BackEndRequestCall.BackendRequestListener() {
                @Override
                public void onSuccess(String tag, @NonNull Object responseBody) {
                    RouteResponse routeResponse = ((RouteResponse) responseBody);
                    List<Path> pathList = routeResponse.getRoute().getPathList();
                    BackEndRequestCall.enqueue(RetrofitHelper.getPoiList(routeId), "getSavedPoi", new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            List<PoiPoint> poiPoints = ((PoiResponse) responseBody).getPoiList();
                            List<Coordinate> coordinates = new ArrayList<>();
                            double distanceTillNow = 0;

                            if (pathList.size() > 1) {
                                for (int i = 0; i < pathList.size() - 1; i++) {
                                    distanceTillNow += distanceInKm(
                                            pathList.get(i).getLoc().getNumericLatitude(),
                                            pathList.get(i).getLoc().getNumericLongitude(),
                                            0,
                                            pathList.get(i + 1).getLoc().getNumericLatitude(),
                                            pathList.get(i + 1).getLoc().getNumericLongitude(),
                                            0
                                    );
                                    coordinates.add(new Coordinate(pathList.get(i).getLoc().getLatitude(), pathList.get(i).getLoc().getLongitude()));
                                }
                                totalDistance += distanceTillNow;
                                CrashAnalytics.log("RouteMapHelper: routePoints > 1");
                            } else if (pathList.size() == 1) {
                                coordinates.add(new Coordinate(pathList.get(0).getLoc().getLatitude(), pathList.get(0).getLoc().getLongitude()));
                                CrashAnalytics.log("RouteMapHelper: routePoints == 1");
                            }

                            if (coordinates.size() > 1)
                                coordinates.add(new Coordinate(pathList.get(pathList.size() - 1).getLoc().getLatitude(),
                                        pathList.get(pathList.size() - 1).getLoc().getLongitude()));

                            if (listener != null)
                                listener.onDataFetchComplete(coordinates, poiPoints);

                            activity.runOnUiThread(() -> {
                                TextSetter.setText(infoBinding.tvRouteName, routeResponse.getRoute().getRouteName());
                                TextSetter.setText(infoBinding.tvVehicleNumber, routeResponse.getRoute().getVehicleNumber());
                                TextSetter.setText(infoBinding.tvPoiMarked, Integer.toString(poiPoints.size()));
                                TextSetter.setText(infoBinding.tvDistance, activity.getString(R.string.tv_distance_value, String.format("%.2f", totalDistance)));
                            });
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            TastyToast.makeText(activity, "Error(POI):" + backendError.toString(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        }
                    });
                }

                @Override
                public void onError(String tag, @NonNull BackendError backendError) {
                    TastyToast.makeText(activity, "Error(Route):" + backendError.toString(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            });
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void onCoordinateUpdated(@NonNull final CoordinateUpdateMessageEvent event) {
        executorService.execute(() -> {
            synchronized (RouteMapHelper.class) {
                totalDistance += previousPosition == null ? 0 : RouteMapHelper.distanceInKm(
                        previousPosition.getLatitude(), previousPosition.getLongitude(), previousPosition.getAltitude(),
                        event.getPosition().getLatitude(), event.getPosition().getLongitude(), event.getPosition().getAltitude());

                previousPosition = event.getPosition();
            }

            activity.runOnUiThread(() -> {
                infoBinding.tvSpeed.setText(activity.getString(R.string.tv_speed_value, String.valueOf((int) event.getPosition().getSpeed())));
                infoBinding.tvDistance.setText(activity.getString(R.string.tv_distance_value, String.format("%.2f", totalDistance)));
            });
        });
    }

    @SuppressLint("SetTextI18n")
    public void incrementPoiCount() {
        activity.runOnUiThread(() ->
                infoBinding.tvPoiMarked.setText(
                        Integer.toString(Integer.parseInt(infoBinding.tvPoiMarked.getText().toString()) + 1)));
    }

    @SuppressLint("SetTextI18n")
    public void decrementPoiCount() {
        activity.runOnUiThread(() ->
                infoBinding.tvPoiMarked.setText(
                        Integer.toString(Integer.parseInt(infoBinding.tvPoiMarked.getText().toString()) - 1)));
    }


    /**
     * Method to calculate distance between two points in latitude and longitude taking
     * into account height difference. Uses Haversine method as its base)
     * If you are not interested in height difference pass  for
     *
     * @param lat1 latitude of first coordinate
     * @param lon1 longitude of first coordinate
     * @param al1  height or altitude of first coordinate
     * @param lat2 latitude of first coordinate
     * @param lon2 longitude of first coordinate
     * @param al2  height or altitude of second coordinate
     * @return distance in kilometers
     */
    public static double distanceInKm(double lat1, double lon1, double al1,
                                      double lat2, double lon2, double al2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        double height = al1 - al2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
