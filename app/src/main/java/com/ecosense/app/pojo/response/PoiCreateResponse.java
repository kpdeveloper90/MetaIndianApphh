package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.ecosense.app.db.models.PoiPoint;

/**
 * <h1>POJO class representing response of poi creation.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PoiCreateResponse extends BackendResponse {

    @SerializedName("data")
    @NonNull
    private PoiPoint poiPoint;

    public PoiCreateResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public PoiPoint getPoiPoint() {
        return poiPoint;
    }

    public void setPoiPoint(@NonNull PoiPoint poiPoint) {
        this.poiPoint = poiPoint;
    }
}
