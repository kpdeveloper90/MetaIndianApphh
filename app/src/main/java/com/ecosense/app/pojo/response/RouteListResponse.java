package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.db.models.MetaRouteInfo;

/**
 * <h1>POJO class representing a route list response o the logged in user.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteListResponse extends BackendResponse {

    @NonNull
    @SerializedName("data")
    private List<MetaRouteInfo> metaRouteInfoList;

    public RouteListResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public List<MetaRouteInfo> getMetaRouteInfoList() {
        return metaRouteInfoList;
    }

    public void setMetaRouteInfoList(@NonNull List<MetaRouteInfo> metaRouteInfoList) {
        this.metaRouteInfoList = metaRouteInfoList;
    }
}
