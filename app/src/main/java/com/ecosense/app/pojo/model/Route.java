package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <h1>POJO class of complete route</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Route {
    @NonNull
    @SerializedName("_id")
    private String routeId;
    @NonNull
    @SerializedName("name")
    private String routeName;
    @NonNull
    private Object vehicleId;
    @SerializedName("number")
    private String vehicleNumber;
    private boolean isCaptured;
    @NonNull
    @SerializedName("path")
    List<Path> pathList;

    @NonNull
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(@NonNull String routeId) {
        this.routeId = routeId;
    }

    @NonNull
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(@NonNull String routeName) {
        this.routeName = routeName;
    }

    @NonNull
    public String getVehicleId() {
        return vehicleId.toString();
    }

    public void setVehicleId(@NonNull String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }

    @NonNull
    public List<Path> getPathList() {
        return pathList;
    }

    public void setPathList(@NonNull List<Path> pathList) {
        this.pathList = pathList;
    }
}
