package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.pojo.model.Ward;

/**
 * <h1>Class representing a ward response.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class WardResponse extends BackendResponse {

    @SerializedName("data")
    @NonNull
    private WardPage wardPage;

    public WardResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public WardPage getWardPage() {
        return wardPage;
    }

    public void setWardPage(@NonNull WardPage wardPage) {
        this.wardPage = wardPage;
    }

    public List<Ward> getWardList() {
        return wardPage.getWardList();
    }
}