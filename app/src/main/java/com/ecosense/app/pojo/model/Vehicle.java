package com.ecosense.app.pojo.model;

import com.google.gson.annotations.SerializedName;

/**
 * <h1>POJO class of a vehicle</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Vehicle {
    @SerializedName("_id")
    private String id;
    private String number;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
