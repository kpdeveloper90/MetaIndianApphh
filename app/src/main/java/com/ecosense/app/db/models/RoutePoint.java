package com.ecosense.app.db.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * <h1>POJO class of route item (aka root point) to stored as entry in route table of the db</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@Entity(tableName = "route_point", indices = {@Index("timestamp")})
public class RoutePoint {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    @Embedded
    private MetaRouteInfo metaRouteInfo;
    @NonNull
    @Embedded
    private Coordinate coordinate;
    @NonNull
    private String accuracy;
    @NonNull
    private String speed;
    private String address;
    private boolean isMock;
    private boolean isUploaded;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MetaRouteInfo getMetaRouteInfo() {
        return metaRouteInfo;
    }

    public void setMetaRouteInfo(MetaRouteInfo metaRouteInfo) {
        this.metaRouteInfo = metaRouteInfo;
    }

    @NonNull
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(@NonNull Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @NonNull
    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(@NonNull String accuracy) {
        this.accuracy = accuracy;
    }

    @NonNull
    public String getSpeed() {
        return speed;
    }

    public void setSpeed(@NonNull String speed) {
        this.speed = speed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }
}
