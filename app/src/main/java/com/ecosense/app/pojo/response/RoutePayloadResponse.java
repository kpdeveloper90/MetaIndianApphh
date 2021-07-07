package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * <h1>POJO class representing a user authentication response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RoutePayloadResponse extends BackendResponse {
    //    {
//        "code": 200,
//            "message": "Login success",
//            "data": [
//         "603e4c9a43633e4148f1b049",
//
//    ]
//    }
    @SerializedName("data")
    @NonNull
    private String id;

    public RoutePayloadResponse(int code, @NonNull String message, @NonNull String id) {
        super(code, message);
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }
}
