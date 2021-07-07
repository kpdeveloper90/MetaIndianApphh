package com.ecosense.app.db.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * <h1>POJO class of poi item (aka poi point) to stored as entry in poi table of the db</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
@Entity(tableName = "poi_point", indices = {@Index("timestamp")})
public class PoiPoint {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    @SerializedName("routeClientId")
    private String routeId;
    @Embedded
    private Coordinate coordinate;
    @NonNull
    private String name;
    @NonNull
    private String type;
    @NonNull
    private String qrCode;

    @NonNull
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(@NonNull String qrCode) {
        this.qrCode = qrCode;
    }

    @Ignore
    @SerializedName("_id")
    private String serverId;
    @Ignore
    @SerializedName("routeId")
    private String serverRouteId;
    @Ignore
    private String vehicleId;

    @Ignore
    public PoiPoint(){
    }

    public PoiPoint(long id, long timestamp, String routeId, Coordinate coordinate, @NonNull String name, @NonNull String type) {
        this.id = id;
        this.timestamp = timestamp;
        this.routeId = routeId;
        this.coordinate = coordinate;
        this.name = name;
        this.type = type;
    }

    @Ignore
    public PoiPoint(long id, long timestamp, String routeId, Coordinate coordinate, @NonNull String name,
                    @NonNull String type, String serverId, String serverRouteId, String vehicleId) {
        this.id = id;
        this.timestamp = timestamp;
        this.routeId = routeId;
        this.coordinate = coordinate;
        this.name = name;
        this.type = type;
        this.serverId = serverId;
        this.serverRouteId = serverRouteId;
        this.vehicleId = vehicleId;
    }

    @Ignore
    public PoiPoint(Coordinate coordinate, @NonNull String name, @NonNull String type) {
        this.coordinate = coordinate;
        this.name = name;
        this.type = type;
    }

    public LatLng getLatLng(){
        try{
        return new LatLng(getCoordinate().getNumericLatitude(), getCoordinate().getNumericLongitude());
        }catch (Exception e){
            return new LatLng(0,0);
        }
    }

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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setCoordinate(LatLng latLng) {
        this.coordinate = new Coordinate(latLng);
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerRouteId() {
        return serverRouteId;
    }

    public void setServerRouteId(String serverRouteId) {
        this.serverRouteId = serverRouteId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
