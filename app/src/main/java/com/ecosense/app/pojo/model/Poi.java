package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.ecosense.app.db.models.Coordinate;

/**
 * <h1>POJO class of poi</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Poi {
    @NonNull
    @SerializedName("_id")
    private String id;
    @NonNull
    private String routeId;
    @NonNull
    private String vehicleId;
    @NonNull
    private String name;
    @NonNull
    private String type;
    @NonNull
    private Coordinate coordinate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(@NonNull String routeId) {
        this.routeId = routeId;
    }

    @NonNull
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(@NonNull String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(@NonNull Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
