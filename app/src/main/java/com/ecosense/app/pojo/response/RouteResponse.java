package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.ecosense.app.pojo.model.Route;

/**
 * <h1>POJO class representing a response of complete route.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteResponse extends BackendResponse {

    @SerializedName("data")
    @NonNull
    private Route route;

    public RouteResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public Route getRoute() {
        return route;
    }

    public void setRoute(@NonNull Route route) {
        this.route = route;
    }
}
