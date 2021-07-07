package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.ecosense.app.pojo.model.MetadataUser;

/**
 * <h1>POJO class representing a user authentication response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetadataUserResponse extends BackendResponse {
//    {
//        "code": 200,
//        "message": "Login success",
//        "data": {
//                "_id": "60544243a9665845e16e22d0",
//                "name": "test4",
//                "email": "test4@test.com",
//                "contact": "9988776655",
//                "role": "Admin",
//                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjoidGVzdDQiLCJpYXQiOjE2MTY0OTY0NTUsImV4cCI6MzIzMjk5NjUxMH0._KqSNiEhI_J2sWfGl8ERhGWPYUZadQD-lEtLyg5IlS0"
//                 }
//    }
    @SerializedName("data")
    @NonNull
    private MetadataUser metadataUser;

    public MetadataUserResponse(int code, @NonNull String message, @NonNull MetadataUser user) {
        super(code, message);
        this.metadataUser = user;
    }

    @NonNull
    public MetadataUser getMetadataUser() {
        return metadataUser;
    }

    public void setMetadataUser(@NonNull MetadataUser metadataUser) {
        this.metadataUser = metadataUser;
    }
}
