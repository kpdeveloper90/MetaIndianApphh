package com.ecosense.app.remote;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdsmdg.tastytoast.TastyToast;

import com.ecosense.app.R;
import com.ecosense.app.activity.LoginActivity;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.pojo.response.BackendResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <h1>Class to make quick backend call using retrofit
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class BackEndRequestCall {

    /**
     * The constant sessionExpiredToast.
     */
    private static Toast sessionExpiredToast;

    /**
     * e
     * The interface Backend request listener.
     */
    public interface BackendRequestListener {
        /**
         * On success of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackendRequestListener)}}
         * @param responseBody the response body
         */
        void onSuccess(String tag, @NonNull Object responseBody);


        /**
         * On error of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackendRequestListener)}}
         * @param backendError the backend error
         * @see BackendError
         */
        default void onError(String tag, @NonNull final BackendError backendError) {
        }
    }

    public interface BackendRequestListeners {
        /**
         * On success of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackendRequestListener)}}
         * @param responseBody the response body
         */
        void onSuccess(String tag, @NonNull ResponseBody responseBody);



        /**
         * On error of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackendRequestListener)}}
         * @param backendError the backend error
         * @see BackendError
         */
        default void onError(String tag, @NonNull final BackendError backendError) {
        }
    }

    /**
     * Method to hit the backend api (defined by {@code requestCall }) asynchronously, and
     * return the result of the api call (success or failure) using the help of
     * {@link BackendRequestListener}.
     * <p>
     * {@link BackendRequestListener#onSuccess(String, Object)} is invoked if the api hit is
     * successfull ie if the api was reachable and the the server responds success and the response
     * payload {@link BackendResponse} has the code 200.
     * <p>
     * {@link BackendRequestListener#onError(String, BackendError)} is invoked in all other cases
     *
     * @param <T>         the type parameter
     * @param requestCall the {@link Call<T>} request call
     * @param tag         the {@link String} tag to identify/mark this request
     * @param listener    the {@link BackendRequestListener} listener
     * @see BackendRequestListener
     * @see BackendResponse
     * @see BackendError
     * ok
     */
    public static <T> void enqueue(Call<T> requestCall, @Nullable String tag, @Nullable final BackendRequestListener listener) {
        Analytics.getInstance().logBackendRequest(requestCall.request().url().toString());
        CrashAnalytics.log("RequestCall:" + requestCall.request().url().toString());

        requestCall.enqueue(new Callback<T>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                // server responded
                if (response.isSuccessful()) {
                    //successful response form server
                    if (((BackendResponse) (response.body())).getCode() == 200) {
                        if (listener != null)
                            listener.onSuccess(tag, response.body());

                        Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                                Analytics.BACKEND_STATUS_SUCCESS, null);

                        CrashAnalytics.log("Response(SUCCESSFUL):" + requestCall.request().url().toString());

                    } else {
                        if (listener != null)
                            listener.onError(tag, BackendError.INVALID);

                        Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                                Analytics.BACKEND_STATUS_FAILED, BackendError.INVALID);

                        CrashAnalytics.log("Response(INVALID):" + requestCall.request().url().toString());
                    }

                } else {
                    //UNSUCCESSFUL response form server
                    if (response.code() == 403) {// jwt token expired or not found
                        Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                                Analytics.BACKEND_STATUS_FAILED, BackendError.TOKEN_EXPIRED);

                        Analytics.getInstance().logSessionExpired();
                        CrashAnalytics.log("Response(SESSION_EXPIRED):" + requestCall.request().url().toString());

                        showSessionExpiredToast();

                        Intent intent = new Intent(ConnactionCheckApplication.getInstance(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(LoginActivity.SESSION_EXPIRED_KEY, true);
                        ConnactionCheckApplication.getInstance().startActivity(intent);
                        ConnactionCheckApplication.activity.finish();
                        return;
                    }

                    if (listener != null)
                        listener.onError(tag, BackendError.UNSUCCESSFUL);

                    Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                            Analytics.BACKEND_STATUS_FAILED, BackendError.UNSUCCESSFUL);

                    CrashAnalytics.log("Response(UNSUCCESSFUL):" + requestCall.request().url().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("Exception")) {// model parsing error
                    if (listener != null)
                        listener.onError(tag, BackendError.PARSING);

                    Log.e("BACKEND", "onFailure:(Parsing - MSG:" + tag + ")" + t.getMessage());

                    Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                            Analytics.BACKEND_STATUS_FAILED, BackendError.PARSING);
                    CrashAnalytics.log("Response(FAILED:PARSING):" + requestCall.request().url().toString());

                } else {// unable to reach server
                    if (listener != null)
                        listener.onError(tag, BackendError.NETWORK);

                    Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                            Analytics.BACKEND_STATUS_FAILED, BackendError.NETWORK);
                    CrashAnalytics.log("Response(FAILED:NETWORK):" + requestCall.request().url().toString());
                }
            }
        });
    }

    /**
     * Method to show session expired toast.
     */
    private static void showSessionExpiredToast() {
        if (sessionExpiredToast == null || sessionExpiredToast.getView() == null || !sessionExpiredToast.getView().isShown())
            sessionExpiredToast = TastyToast.makeText(ConnactionCheckApplication.getInstance(),
                    ConnactionCheckApplication.getInstance().getString(R.string.toast_session_expired),
                    TastyToast.LENGTH_LONG, TastyToast.ERROR);
    }
}
