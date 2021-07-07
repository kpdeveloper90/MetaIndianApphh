package com.ecosense.app.db.models;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

/**
 * <h1>POJO class of coordinate</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Coordinate {
    @NonNull
    @SerializedName("lat")
    private String latitude;
    @NonNull
    @SerializedName("lng")
    private String longitude;
    @NonNull
    @SerializedName("alt")
    private String altitude;
    private Date recordedTime;

    @Ignore
    public Coordinate(){
    }

    @Ignore
    public Coordinate(@NonNull String latitude, @NonNull String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public Coordinate(@NonNull LatLng latLng) {
        this(latLng.latitude, latLng.longitude);
    }

    @Ignore
    public Coordinate(@NonNull double latitude, @NonNull double longitude) {
        this.latitude = Double.toString(latitude);
        this.longitude = Double.toString(longitude);
    }

    public Coordinate(@NonNull String latitude, @NonNull String longitude, @NonNull String altitude, Date recordedTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.recordedTime = recordedTime;
    }

    @NonNull
    public String getLatitude() {
        return latitude;
    }

    @NonNull
    public double getNumericLatitude() {
        return Double.parseDouble(latitude);
    }

    public void setLatitude(@NonNull String latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = Double.toString(latitude);
    }

    @NonNull
    public String getLongitude() {
        return longitude;
    }

    @NonNull
    public double getNumericLongitude() {
        return Double.parseDouble(longitude);
    }

    public void setLongitude(@NonNull String longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = Double.toString(longitude);
    }

    @NonNull
    public String getAltitude() {
        return altitude;
    }

    @NonNull
    public double getNumericAltitude() {
        return Double.parseDouble(altitude);
    }

    public void setAltitude(@NonNull String altitude) {
        this.altitude = altitude;
    }

    public Date getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(Date recordedTime) {
        this.recordedTime = recordedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return latitude.equals(that.latitude) &&
                longitude.equals(that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
