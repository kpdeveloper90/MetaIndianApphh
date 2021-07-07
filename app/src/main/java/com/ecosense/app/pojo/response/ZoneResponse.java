package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.pojo.model.Zone;

/**
 * <h1>Class representing a zone response.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ZoneResponse extends BackendResponse {
    @SerializedName("data")
    @NonNull
    private ZonePage zonePage;

    public ZoneResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public ZonePage getZonePage() {
        return zonePage;
    }

    public void setZonePage(@NonNull ZonePage zonePage) {
        this.zonePage = zonePage;
    }

    public List<Zone> getZoneList() {
        return zonePage.getZoneList();
    }
}