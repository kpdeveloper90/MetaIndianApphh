package com.ecosense.app.remote;

import androidx.annotation.NonNull;

import com.ecosense.app.db.models.PoiPoint;
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
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * <h1>Interface containing endpoint used in this app (accessed using Retrofit)</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public interface RetrofitApiInterface {

    /**
     * Method to Authenticate metadata user using the credentials provided.
     *
     * @param user the user object containing the credentials.
     * @param auth the {@link String} authorization token (jwt token of the user for the session)
     * @return the call
     */
    @POST("api/users/login")
    Call<MetadataUserResponse> authenticateMetadataUser(@NonNull @Body User user, @Header("Authorization") String auth,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Method to Send all the route points of a particular route to teh server for permanent persistence.
     *
     * @param routePayload the route payload
     * @param auth         the {@link String} authorization token (jwt token of the user for the session)
     * @return the call
     */
    @POST("api/routes")
    Call<RoutePayloadResponse> sendRoutePoints(@NonNull @Body RoutePayload routePayload, @Header("Authorization") String auth,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Gets zones.
     *
     * @param auth the {@link String} authorization token (jwt token of the user for the session)
     * @param page the page
     * @return the zones
     */
    @GET("api/zones/page/{page}")
    Call<ZoneResponse> getZones(@Header("Authorization") String auth, @Path("page") int page,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

  @GET("api/projects")
    Call<ResponseBody> getAllProject(@Header("Authorization") String auth, @Header("version") String version);

    /**
     * Gets wards.
     *
     * @param auth the {@link String} authorization token (jwt token of the user for the session)
     * @param page the page
     * @return the wards
     */
    @GET("api/wards/page/{page}")
    Call<WardResponse> getWards(@Header("Authorization") String auth, @Path("page") int page,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Gets live vehicles.
     *
     * @param auth the {@link String} authorization token (jwt token of the user for the session)
     * @return the live vehicles
     */
    @GET("api/vehicles")
    Call<LiveVehicleResponse> getLiveVehicles(@Header("Authorization") String auth,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Gets routes.
     *
     * @param auth the {@link String} authorization token (jwt token of the user for the session)
     * @return the routes
     */
    @GET("api/routes")
    Call<RouteListResponse> getRoutes(@Header("Authorization") String auth,@Header("ProjectId") String ProjectId,@Header("version") String version);

    /**
     * Gets route.
     *
     * @param auth    the {@link String} authorization token (jwt token of the user for the session)
     * @param routeId the route id
     * @return the route
     */
    @GET("api/routes/{routeId}")
    Call<RouteResponse> getRoute(@Header("Authorization") String auth, @Path("routeId") String routeId,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Gets pois.
     *
     * @param auth    the {@link String} authorization token (jwt token of the user for the session)
     * @param routeId the route id
     * @return the pois
     */
    @GET("api/pois")
    Call<PoiResponse> getPois(@Header("Authorization") String auth, @Query("routeId") String routeId,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Method to Create poi call.
     *
     * @param auth     the {@link String} authorization token (jwt token of the user for the session)
     * @param poiPoint the poi point
     * @return the call
     */
    @POST("api/pois")
    Call<PoiCreateResponse> createPoi(@Header("Authorization") String auth, @NonNull @Body PoiPoint poiPoint,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Method to Update poi call.
     *
     * @param auth     the {@link String} authorization token (jwt token of the user for the session)
     * @param poiId    the poi id
     * @param poiPoint the poi point
     * @return the call
     */
    @PUT("api/pois/{poiId}")
    Call<BackendResponse> updatePoi(@Header("Authorization") String auth, @Path("poiId") String poiId, @NonNull @Body PoiPoint poiPoint,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);

    /**
     * Method to Delete poi call.
     *
     * @param auth  the {@link String} authorization token (jwt token of the user for the session)
     * @param poiId the poi id
     * @return the call
     */
    @DELETE("api/pois/{poiId}")
    Call<BackendResponse> deletePoi(@Header("Authorization") String auth, @Path("poiId") String poiId,@Header("ProjectId") String ProjectId,@Header("project") String project,@Header("version") String version);
}
