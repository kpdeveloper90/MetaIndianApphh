package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * <h1>POJO class of Loc aka location</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Loc {
    @NonNull
    private List<String> coordinates;
    @NonNull
    private String type;

    @NonNull
    public String getLatitude() {
        return coordinates.get(0);
    }

    @NonNull
    public double getNumericLatitude() {
        return Double.parseDouble(coordinates.get(0));
    }

    @NonNull
    public String getLongitude() {
        return coordinates.get(1);
    }

    @NonNull
    public double getNumericLongitude() {
        return Double.parseDouble(coordinates.get(1));
    }

    @NonNull
    public List<String> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(@NonNull List<String> coordinates) {
        this.coordinates = coordinates;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }
}
