package com.ecosense.app.Traccar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ecosense.app.R;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.Coordinate;
import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.RouteTimerHelper;
import com.ecosense.app.pojo.eventBus.CoordinateUpdateMessageEvent;

/**
 * <h1>A controller class initiated by {@link TrackingService} to ha </h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.3
 */
public class TrackingControllerNew implements PositionProvider.PositionListener {

    /**
     * The constant TAG.
     */
    private static final String TAG = TrackingControllerNew.class.getSimpleName();

    /**
     * The constant DEFAULT_THREAD_POOL_SIZE.
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    /**
     * The constant TIME_TO_WAIT_FOR_DB_PROCESS_IN_MILLI.
     */
    public static final int TIME_TO_WAIT_FOR_DB_PROCESS_IN_MILLI = 3000;
    /**
     * The constant SAVE_LOCATION_FREQUENCY.
     */
    private static final int SAVE_LOCATION_FREQUENCY = 3; // max allowed value is 5 >> otherwise a lot things may come crashing down :(

    /**
     * The Context.
     */
    private final Context context;
    /**
     * The Route id.
     */
    private final String routeId;
    /**
     * The Route name.
     */
    private final String routeName;
    /**
     * The Zone id.
     */
    private final String zoneId;
    /**
     * The Zone number.
     */
    private final String zoneNumber;
    /**
     * The Ward id.
     */
    private final String wardId;
    /**
     * The Ward number.
     */
    private final String wardNumber;
    /**
     * The Vehicle id.
     */
    private final String vehicleId;
    /**
     * The Vehicle number.
     */
    private final String vehicleNumber;
    /**
     * The Executor service.
     */
    private final ExecutorService executorService;
    /**
     * The Position provider.
     */
    private final PositionProvider positionProvider;
    /**
     * The Route database.
     */
    private final RouteDatabase routeDatabase;
    /**
     * The Route timer helper.
     */
    private final RouteTimerHelper routeTimerHelper;
    /**
     * The Location update count.
     */
    private long locationUpdateCount;

    /**
     * Instantiates a new Tracking controller new.
     *
     * @param context the {@link Activity} context
     */
    public TrackingControllerNew(Context context) {
        this.context = context;
        executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        routeDatabase = RouteDatabase.getInstance(context);
        positionProvider = PositionProviderFactory.create(context, this);
        locationUpdateCount = 0;
        routeTimerHelper = RouteTimerHelper.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        String id = sharedPreferences.getString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, "null");

        if (id.equals("null")) {
            id = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID, id).apply();
        }
        routeId = id;
        routeName = sharedPreferences.getString(AppConfig.KEY_CURRENT_ROUTE_NAME, "null");
        zoneId = sharedPreferences.getString(AppConfig.KEY_CURRENT_ZONE_ID, "null");
        zoneNumber = sharedPreferences.getString(AppConfig.KEY_CURRENT_ZONE_NUMBER, "null");
        wardId = sharedPreferences.getString(AppConfig.KEY_CURRENT_WARD_ID, "null");
        wardNumber = sharedPreferences.getString(AppConfig.KEY_CURRENT_WARD_NUMBER, "null");
        vehicleId = sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_ID, "null");
        vehicleNumber = sharedPreferences.getString(AppConfig.KEY_CURRENT_VEHICLE_NUMBER, "null");
    }

    /**
     * Method to start the location updates and the route timer.
     *
     * @see PositionProvider
     * @see GooglePositionProvider
     */
    public void start() {
        routeTimerHelper.startTimer();
        try {
            positionProvider.startUpdates();
        } catch (SecurityException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Method to stop the location updates and the route timer. All the pending db operations will
     * have {@link #TIME_TO_WAIT_FOR_DB_PROCESS_IN_MILLI} to complete its process or will be simply
     * discarded.
     */
    public void stop() {
        executorService.shutdown();
        routeTimerHelper.stopTimer();
        try {
            executorService.awaitTermination(TIME_TO_WAIT_FOR_DB_PROCESS_IN_MILLI, TimeUnit.MILLISECONDS);
            positionProvider.stopUpdates();
        } catch (SecurityException | InterruptedException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Save route point to the device database.
     *
     * @param routePoint the route point
     * @see RoutePoint
     */
    private void saveRoutePoint(RoutePoint routePoint) {
       try{
        executorService.execute(() -> {
            routeDatabase.routePointDao().insertRoutePoint(routePoint);
        });}catch (Exception e){}
    }

    /**
     * Log the data for debugging purpose.
     *
     * @param action   the action
     * @param position the position
     */
    private void log(String action, Position position) {
//        if (position != null) {
//            action += " (" +
//                    "id:" + position.getId() +
//                    " time:" + position.getTime().getTime() / 1000 +
//                    " lat:" + position.getLatitude() +
//                    " lon:" + position.getLongitude() +
//                    " accu:" + position.getAccuracy() +
//                    " speed:" + position.getSpeed() +
//                    ") \t " + Connection.getCurrentDateTime12Hours();
//        }
//        Log.e(TAG, action);
    }

    /**
     * Update notification text with the newly captured latitude and longitude.
     * (even if they are same as previous)
     *
     * @param text the text (containing the lat and lng in prescribed format)
     */
    private void updateNotificationText(@NonNull final String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConnactionCheckApplication.PRIMARY_CHANNEL)
                .setSmallIcon(R.mipmap.ic_dwms)
                .setContentText(text);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(TrackingService.NOTIFICATION_ID, builder.build());
    }

    /**
     * On position update.
     *
     * @param position the position
     * @see Position
     */
    @Override
    public void onPositionUpdate(Position position) {
        log("NEW", position);

        if (position != null) {

            if (++locationUpdateCount % SAVE_LOCATION_FREQUENCY == 0) {
                updateNotificationText(context.getString(R.string.notification_lat_lon, position.getLatitude(), position.getLongitude()));
                EventBus.getDefault().post(new CoordinateUpdateMessageEvent(position, true));

                log("SAVE", null);

                RoutePoint routePoint = new RoutePoint();
                routePoint.setCoordinate(new Coordinate());
                routePoint.setMetaRouteInfo(new MetaRouteInfo());

                routePoint.setTimestamp(System.currentTimeMillis());
                routePoint.getMetaRouteInfo().setRouteClientId(routeId);
                routePoint.getMetaRouteInfo().setRouteName(routeName);
                routePoint.getMetaRouteInfo().setZoneId(zoneId);
                routePoint.getMetaRouteInfo().setZoneName(zoneNumber);
                routePoint.getMetaRouteInfo().setWardId(wardId);
                routePoint.getMetaRouteInfo().setWardNumber(wardNumber);
                routePoint.getMetaRouteInfo().setVehicleId(vehicleId);
                routePoint.getMetaRouteInfo().setVehicleNumber(vehicleNumber);
                routePoint.getCoordinate().setLatitude(Double.toString(position.getLatitude()));
                routePoint.getCoordinate().setLongitude(Double.toString(position.getLongitude()));
                routePoint.getCoordinate().setAltitude(Double.toString(position.getAltitude()));
                routePoint.getCoordinate().setRecordedTime(new Date(System.currentTimeMillis()));
                routePoint.setAccuracy(Double.toString(position.getAccuracy()));
                routePoint.setSpeed(Double.toString(position.getSpeed()));
                routePoint.setAddress(position.getAddress());
                routePoint.setMock(position.getMock());
                routePoint.setUploaded(false);
                saveRoutePoint(routePoint);
            } else EventBus.getDefault().post(new CoordinateUpdateMessageEvent(position, false));
        }
    }

    /**
     * On position error.
     *
     * @param error the error
     */
    @Override
    public void onPositionError(Throwable error) {
        log("Location Error", null);
    }

}
