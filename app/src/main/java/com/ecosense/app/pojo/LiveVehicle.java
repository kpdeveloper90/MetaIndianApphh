package com.ecosense.app.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * <h1>POJO class representing a live vehicle.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class LiveVehicle {
    @NonNull
    @SerializedName("_id")
    private String id;
    @NonNull
    @SerializedName("number")
    private String vehicleNumber;
    @NonNull
    private String category;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(@NonNull String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }
}
