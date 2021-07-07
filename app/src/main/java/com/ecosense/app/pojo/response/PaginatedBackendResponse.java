package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.ecosense.app.pojo.model.PaginatedData;

/**
 * <h1>POJO class representing a abstract paginated backend response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PaginatedBackendResponse extends BackendResponse{
    @SerializedName("data")
    @NonNull
    private PaginatedData paginatedData;

    public PaginatedBackendResponse(int code, @NonNull String message) {
        super(code, message);
    }

    @NonNull
    public PaginatedData getPaginatedData() {
        return paginatedData;
    }

    public void setPaginatedData(@NonNull PaginatedData paginatedData) {
        this.paginatedData = paginatedData;
    }
}
