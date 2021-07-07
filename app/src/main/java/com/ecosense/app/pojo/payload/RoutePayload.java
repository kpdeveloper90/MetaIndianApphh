package com.ecosense.app.pojo.payload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.db.models.Coordinate;
import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.pojo.model.DeviceInfo;

public class RoutePayload {
    @Nullable
    @SerializedName("_id")
    private String routeId;
    @NonNull
    private String routeClientId;
     @NonNull
    private String p_id;
    @NonNull
    private String userId;
    @NonNull
    private String zoneId;
    @NonNull
    private String wardId;
    @NonNull
    private String vehicleId;
    @NonNull
    @SerializedName("name")
    private String routeName;
    @NonNull
    DeviceInfo deviceInfo;
    @NonNull
    @SerializedName("path")
    public List<Coordinate> coordinateList;
    @NonNull
    @SerializedName("pois")
    public List<PoiPoint> poiPointList;

    public void setRouteInfo(MetaRouteInfo routeInfo){
        routeClientId = routeInfo.getRouteClientId();
        zoneId = routeInfo.getZoneId();
        wardId = routeInfo.getWardId();
        vehicleId = routeInfo.getVehicleId();
        routeName = routeInfo.getRouteName();
    }

    @Nullable
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(@Nullable String routeId) {
        this.routeId = routeId;
    }

    @NonNull
    public String getRouteClientId() {
        return routeClientId;
    }

    public void setRouteClientId(@NonNull String routeClientId) {
        this.routeClientId = routeClientId;
    }

    @NonNull
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(@NonNull String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @NonNull
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(@NonNull String routeName) {
        this.routeName = routeName;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getP_id() {
        return p_id;
    }

    public void setP_id(@NonNull String p_id) {
        this.p_id = p_id;
    }

    @NonNull
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(@NonNull DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @NonNull
    public List<Coordinate> getCoordinateList() {
        return coordinateList;
    }

    public void setCoordinateList(@NonNull List<Coordinate> coordinateList) {
        this.coordinateList = coordinateList;
    }

    @NonNull
    public List<PoiPoint> getPoiPointList() {
        return poiPointList;
    }

    public void setPoiPointList(@NonNull List<PoiPoint> poiPointList) {
        this.poiPointList = poiPointList;
    }
}
