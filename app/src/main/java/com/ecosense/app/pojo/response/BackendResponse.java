package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

/**
 * <h1>POJO class representing a code and message part of the backend response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class BackendResponse {
    //   "code": 200,
    //    "message": "success message",

    private int code;
    @NonNull
    private String message;

    public BackendResponse(int code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }
}
