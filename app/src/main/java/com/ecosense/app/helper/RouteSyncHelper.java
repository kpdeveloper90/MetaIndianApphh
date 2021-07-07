package com.ecosense.app.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.db.models.RoutePoint;
import com.ecosense.app.pojo.model.DeviceInfo;
import com.ecosense.app.pojo.payload.RoutePayload;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RouteBackend;

/**
 * <h1>Helper class to assist in route synchronization to the server </h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteSyncHelper implements RouteBackend.OnRoutePointBackendListener {

    public interface RouteSyncListener {
        void onSyncSuccess(@NonNull String routeId);

        void onSyncError(@NonNull String routeId, @NonNull BackendError backendError);

        void onAllSynced(boolean alreadyUpToDate);
    }

    private static RouteSyncHelper instance = null;

    private final ExecutorService executorService;
    private final RouteDatabase routeDatabase;
    private final RouteBackend routeBackend;
    private RouteSyncListener listener;
    private volatile int requestCount;
    private volatile int responseCount;

    public static RouteSyncHelper getInstance(@NonNull final Context context) {
        if (instance == null)
            synchronized (PersistentRouteTimerHelper.class) {
                if (instance == null)
                    instance = new RouteSyncHelper(context);
            }
        return instance;
    }

    public void setListener(RouteSyncListener listener) {
        this.listener = listener;
    }

    private RouteSyncHelper(@NonNull final Context context) {
        executorService = Executors.newSingleThreadExecutor();
        routeDatabase = RouteDatabase.getInstance(context.getApplicationContext());
        routeBackend = new RouteBackend(context.getApplicationContext(), this);
        requestCount = 0;
        responseCount = 0;
    }

    public void sendUnSyncedRoutes(String p_id) {
        executorService.execute(() -> {
            List<MetaRouteInfo> metaRouteInfoList = routeDatabase.routePointDao().getUnSyncedRoutes();

            if (metaRouteInfoList != null && metaRouteInfoList.size() > 0) {
                requestCount += metaRouteInfoList.size();

                for (MetaRouteInfo metaRouteInfo : metaRouteInfoList)
                    sendRoute(metaRouteInfo.getVehicleId(), metaRouteInfo.getRouteName(), p_id);
            } else {
                if (listener != null)
                    listener.onAllSynced(true);
            }
        });
    }

    public void sendUnSyncedRoute(@NonNull final String vehicleNumber, @NonNull final String routeName, @NonNull final String p_id) {
        requestCount++;
        sendRoute(vehicleNumber, routeName, p_id);
    }

    private void sendRoute(@NonNull final String vehicleNumber, @NonNull final String routeName, @NonNull final String p_id) {
        executorService.execute(() -> {
            List<RoutePoint> routePoints = routeDatabase.routePointDao().getAllUnSyncedRoutePoints(vehicleNumber, routeName);

            if (routePoints != null && routePoints.size() > 0) {
                RoutePayload routePayload = new RoutePayload();
                routePayload.setP_id(p_id);
                routePayload.setRouteInfo(routePoints.get(0).getMetaRouteInfo());
                routePayload.setUserId("603cd63d7fd5493e28c6e86e");
                routePayload.setDeviceInfo(new DeviceInfo());
                routePayload.setCoordinateList(new ArrayList<>());

                for (RoutePoint routePoint : routePoints)
                    routePayload.getCoordinateList().add(routePoint.getCoordinate());

                routePayload.setPoiPointList(
                        routeDatabase.poiPointDao().getAllPoints(routePoints.get(0).getMetaRouteInfo().getRouteClientId()));

                routeBackend.sendRoute(routePayload);
            } else if (listener != null)
                listener.onAllSynced(true);
        });
    }

    @Override
    public void onSuccess(@NonNull String routeId) {
        responseCount++;
        if (listener != null) {
            listener.onSyncSuccess(routeId);
            if (responseCount == requestCount)
                listener.onAllSynced(false);
        }
    }

    @Override
    public void onFailure(@NonNull String routeId, BackendError backendError) {
        responseCount++;
        if (listener != null) {
            listener.onSyncError(routeId, backendError);
            if (responseCount == requestCount)
                listener.onAllSynced(false);
        }
    }

}
