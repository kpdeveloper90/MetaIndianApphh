package com.ecosense.app.remote;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.activity.LoginActivity;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.pojo.model.PaginatedData;
import com.ecosense.app.pojo.response.BackendResponse;
import com.ecosense.app.pojo.response.PaginatedBackendResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <h1>Class to make easy paginated  backend call using retrofit and get response of all the pages at once [EXPERIMENTAL]
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class BackEndPaginatedRequestCall {
    /**
     * The constant sessionExpiredToast.
     */
    private static Toast sessionExpiredToast;
    /**
     * The Complete paginated data.
     * @see PaginatedData
     */
    private PaginatedData completePaginatedData;

    /**
     * The interface Backend request listener.
     */
    public interface BackendRequestListener {
        /**
         * On success of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackEndRequestCall.BackendRequestListener)}}
         * @param paginatedData the paginated data (response body)
         */
        void onSuccess(String tag, @NonNull PaginatedData paginatedData);

        /**
         * On error of the backend request call.
         *
         * @param tag          the {@link String} tag used while invoking
         *                     {{@link #enqueue(Call, String, BackEndRequestCall.BackendRequestListener)}}
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
     *
     * {@link BackendRequestListener#onSuccess(String, PaginatedData)} is invoked if the api hit is
     * successfull ie if the api was reachable and the the server responds success with all the
     * paginated data and the response payload {@link BackendResponse} has the code 200.
     *
     * {@link BackendRequestListener#onError(String, BackendError)} is invoked in all other cases
     *
     * @param <T>         the type parameter
     * @param requestCall the {@link Call<T>} request call
     * @param tag         the {@link String} tag to identify/mark this request
     * @param listener    the {@link BackEndRequestCall.BackendRequestListener} listener
     * @see BackEndRequestCall.BackendRequestListener
     * @see BackendResponse
     * @see BackendError
     */
    public <T> void enqueue(Call<T> requestCall, @Nullable String tag, @Nullable final BackEndRequestCall.BackendRequestListener listener) {
        Analytics.getInstance().logBackendRequest(requestCall.request().url().toString());
        CrashAnalytics.log("RequestCall:" + requestCall.request().url().toString());

        requestCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                // server responded
                if (response.isSuccessful()) {
                    //successful response form server
                    if (((BackendResponse) (response.body())).getCode() == 200) {

                        call.clone();

                        int totalPages = Integer.parseInt(((PaginatedBackendResponse) response.body()).getPaginatedData().getTotalPages());

                        for(int i=0;i<totalPages;i++){

                        }

                            //reliable
                        PaginatedData paginatedData = ((PaginatedBackendResponse) response.body()).getPaginatedData();

                        if (paginatedData.getPage().equals(paginatedData.getTotalPages())) {
                            if (listener != null)
                                listener.onSuccess(tag, paginatedData);

                            Analytics.getInstance().logBackendResponse(requestCall.request().url().toString(),
                                    Analytics.BACKEND_STATUS_SUCCESS, null);

                            CrashAnalytics.log("Response(SUCCESSFUL - PAGINATED):" + requestCall.request().url().toString());
                        } else{
//                            completePaginatedData.setPaginationInfo(paginatedData.getPaginationInfo());
                            completePaginatedData.addData((List<Object>) paginatedData.getData());
                        }

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
                                Analytics.BACKEND_STATUS_SUCCESS, null);

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
                if (t.getMessage() != null && t.getMessage().contains("IllegalStateException")) {// model parsing error
                    if (listener != null)
                        listener.onError(tag, BackendError.PARSING);

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
