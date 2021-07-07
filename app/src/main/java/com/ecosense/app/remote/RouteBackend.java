package com.ecosense.app.remote;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.pojo.payload.RoutePayload;
import com.ecosense.app.pojo.response.RoutePayloadResponse;

public class RouteBackend {

    public interface OnRoutePointBackendListener {
        void onSuccess(@NonNull String routeId);

        void onFailure(@NonNull String routeId, BackendError backendError);
    }

    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private RetrofitApiInterface apiInterface;
    private final Context context;
    private final OnRoutePointBackendListener backendListener;
    private RouteDatabase routeDatabase;
    private ExecutorService executorService;
    private String routeId;

    public RouteBackend(@NonNull final Context context, @NonNull final OnRoutePointBackendListener backendListener) {
        this.context = context;
        this.backendListener = backendListener;
        initConfig();
    }

    private void initConfig() {
        routeDatabase = RouteDatabase.getInstance(context);
        apiInterface = new RetrofitHelper().getClient().create(RetrofitApiInterface.class);
        executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    public void sendRoute(@NonNull RoutePayload routePayload){
        routeId = routePayload.getRouteClientId();
       

        BackEndRequestCall.enqueue(RetrofitHelper.sendRoutePoints(routePayload), "sendRoute", new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                executorService.execute(() -> {
                    RoutePayloadResponse routePayloadResponse = (RoutePayloadResponse) responseBody;
                    routeDatabase.routePointDao().updateRouteUploadedStatus(routePayloadResponse.getId());

                    if (backendListener != null)
                        backendListener.onSuccess(routeId);
                });
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                if (backendListener != null)
                    backendListener.onFailure(routeId, backendError);
            }
        });
    }
}
