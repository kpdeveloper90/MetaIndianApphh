package com.ecosense.app.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.ecosense.app.R;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.preference.EncryptedPreferenceHelper;
import com.ecosense.app.pojo.model.User;
import com.ecosense.app.pojo.payload.RoutePayload;
import com.ecosense.app.pojo.response.BackendResponse;
import com.ecosense.app.pojo.response.LiveVehicleResponse;
import com.ecosense.app.pojo.response.MetadataUserResponse;
import com.ecosense.app.pojo.response.PoiCreateResponse;
import com.ecosense.app.pojo.response.PoiResponse;
import com.ecosense.app.pojo.response.RouteListResponse;
import com.ecosense.app.pojo.response.RoutePayloadResponse;
import com.ecosense.app.pojo.response.RouteResponse;
import com.ecosense.app.pojo.response.WardResponse;
import com.ecosense.app.pojo.response.ZoneResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * <h1>Class to get api instance fo the server for this app using retrofit</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
//richa 60acb1725844da47575f1da9
public class RetrofitHelper {
    private static final String BASE_URL = "https://app.ecosense-enviro.com/";

    public Retrofit getClient() {
        return new RetrofitClient().getClient(BASE_URL);
    }

    public static RetrofitApiInterface create() {
        return new RetrofitClient().getClient(BASE_URL).create(RetrofitApiInterface.class);
    }

    public static Call<MetadataUserResponse> authenticateMetadataUser(@NonNull User user) {
        return create().authenticateMetadataUser(user, getToken(), getProjectId(), getProjectId(), "1.0");
    }

    public static Call<RoutePayloadResponse> sendRoutePoints(@NonNull RoutePayload routePayload) {
        return create().sendRoutePoints(routePayload, getToken(), getProjectId(), getProjectId(), "1.0");
    }

    public static Call<ResponseBody> getAllProject() {
        return create().getAllProject(getToken(), "1.0");
    }

    public static Call<ZoneResponse> getZoneList() {
        return create().getZones(getToken(), 1, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<WardResponse> getWardList() {
        Log.e( "getWardList: ",getToken() );
        Log.e( "getWardList: ",getProjectId() );
        return create().getWards(getToken(), 1, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<ZoneResponse> getZoneList(final int page) {
        return create().getZones(getToken(), page, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<WardResponse> getWardList(final int page) {
        return create().getWards(getToken(), page, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<LiveVehicleResponse> getLiveVehicleList() {
        return create().getLiveVehicles(getToken(), getProjectId(), getProjectId(), "1.0");
    }

    public static Call<RouteListResponse> getRouteList() {
        return create().getRoutes(getToken(), getProjectId(), "1.0");
    }

    public static Call<RouteResponse> getRoute(final String routeId) {
        return create().getRoute(getToken(), routeId, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<PoiResponse> getPoiList(final String routeId) {
        return create().getPois(getToken(), routeId, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<PoiCreateResponse> createPoi(@NonNull final PoiPoint poiPoint) {
        return create().createPoi(getToken(), poiPoint, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<BackendResponse> updatePoi(@NonNull final String poiId, @NonNull final PoiPoint poiPoint) {
        return create().updatePoi(getToken(), poiId, poiPoint, getProjectId(), getProjectId(), "1.0");
    }

    public static Call<BackendResponse> deletePoi(@NonNull final String poiId) {
        return create().deletePoi(getToken(), poiId, getProjectId(), getProjectId(), "1.0");
    }

    private static String getToken() {
        return "Bearer " + new EncryptedPreferenceHelper(ConnactionCheckApplication.getInstance()).getMetaUserToken();
    }

    private static String getProjectId() {
        return new EncryptedPreferenceHelper(ConnactionCheckApplication.getInstance()).getCurrentProject();
    }

    public static Pair<String, String> getProblemSolutionPair(@NonNull BackendError backendError) {
        Pair<String, String> problemSolutionPair;

        switch (backendError) {
            case UNSUCCESSFUL:
                problemSolutionPair = new Pair<>(ConnactionCheckApplication.getInstance().getString(R.string.dialog_problem_server_down),
                        ConnactionCheckApplication.getInstance().getString(R.string.dialog_solution_server_down));
                break;
            case PARSING:
                problemSolutionPair = new Pair<>(ConnactionCheckApplication.getInstance().getString(R.string.dialog_problem_parsing_error),
                        ConnactionCheckApplication.getInstance().getString(R.string.dialog_solution_parsing_error));
                break;
            case NETWORK:
                problemSolutionPair = new Pair<>(ConnactionCheckApplication.getInstance().getString(R.string.dialog_problem_network_failure),
                        ConnactionCheckApplication.getInstance().getString(R.string.dialog_solution_network_failure));
                break;
            default:
                problemSolutionPair = new Pair<>(ConnactionCheckApplication.getInstance().getString(R.string.dialog_problem_invalid_request),
                        ConnactionCheckApplication.getInstance().getString(R.string.dialog_solution_invalid_request));
        }

        return problemSolutionPair;
    }
}
