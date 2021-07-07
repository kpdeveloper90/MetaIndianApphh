package com.ecosense.app.db.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

/**
 * <h1>POJO class of route info of the {@link RoutePoint}
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetaRouteInfo {
    @Nullable
    @SerializedName("_id")
    private String routeId;
    @NonNull
    private String routeClientId;
    private String userId;
    private String projectId;
    private String projectName;
    @NonNull
    private String vehicleId;
    private String vehicleNumber;
    @NonNull
    @SerializedName("name")
    private String routeName;
    @NonNull
    private String wardId;
    @NonNull
    private String wardNumber;
    private String zoneId;
    @NonNull
    private String zoneName;
    @Ignore
    private String updatedAt;
    @Ignore
    private String createdAt;

    @NonNull
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(@NonNull String routeId) {
        this.routeId = routeId;
    }

    @NonNull
    public String getRouteClientId() {
        return routeClientId;
    }

    public void setRouteClientId(@NonNull String routeClientId) {
        this.routeClientId = routeClientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @NonNull
    public String getVehicleId() {
        return vehicleId;
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

    @NonNull
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(@NonNull String routeName) {
        this.routeName = routeName;
    }

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    @NonNull
    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(@NonNull String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @NonNull
    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(@NonNull String zoneName) {
        this.zoneName = zoneName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
