package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.db.models.PoiPoint;

/**
 * <h1>POJO class representing response of all poi of a route.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PoiResponse extends BackendResponse {

    @SerializedName("data")
    @NonNull
    private List<PoiPoint> poiList;

    public PoiResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public List<PoiPoint> getPoiList() {
        return poiList;
    }

    public void setPoiList(@NonNull List<PoiPoint> poiList) {
        this.poiList = poiList;
    }
}
