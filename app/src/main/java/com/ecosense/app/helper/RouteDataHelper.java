package com.ecosense.app.helper;

import androidx.annotation.NonNull;

import com.ecosense.app.pojo.payload.RoutePayload;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Helper class to assist in route data fetchimg and cachin</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteDataHelper {

    public interface RouteDateHelperListener {
        void onCacheComplete(RoutePayload routePayload);

        void onCachingError(BackendError backendError);
    }

    private static RouteDataHelper instance = null;
    private RouteDateHelperListener listener;
    private RoutePayload routePayload;
    private String cachedRouteId;
    private boolean isCaching;

    public static RouteDataHelper getInstance() {
        if (instance == null)
            synchronized (RouteDataHelper.class) {
                if (instance == null)
                    instance = new RouteDataHelper();
            }
        return instance;
    }

    public void setListener(RouteDateHelperListener listener) {
        this.listener = listener;
    }

    private RouteDataHelper() {
    }

    public void cacheRoute(@NonNull final String routeId) {
        if (!routeId.equals(cachedRouteId)) {
            isCaching = true;

            BackEndRequestCall.enqueue(RetrofitHelper.getRoute(routeId), null, new BackEndRequestCall.BackendRequestListener() {
                @Override
                public void onSuccess(String tag, @NonNull Object responseBody) {
                    routePayload = (RoutePayload) responseBody;
                    cachedRouteId = routeId;
                    isCaching = false;

                    if (listener != null)
                        listener.onCacheComplete(routePayload);
                }

                @Override
                public void onError(String tag, @NonNull BackendError backendError) {
                    isCaching = false;
                    if (listener != null)
                        listener.onCachingError(backendError);
                }
            });
        }
    }

    public RoutePayload getRoute(@NonNull final String routeId) {
        if (routeId.equals(cachedRouteId))
            return routePayload;
        else return null;
    }

    public void getRoute(RouteDateHelperListener listener) {
        this.listener = listener;
        if (!isCaching && listener!=null)
            listener.onCacheComplete(routePayload);
    }

    public boolean isCaching() {
        return isCaching;
    }
}
