package com.ecosense.app.db.models;

/**
 * <h1>POJO class to update the {@link RoutePoint#getMetaRouteInfo()} of the {@link RoutePoint}
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RoutePointStatus {
    private long id;
    private boolean isUploaded;

    public RoutePointStatus(long id, boolean isUploaded) {
        this.id = id;
        this.isUploaded = isUploaded;
    }

    public RoutePointStatus(long id) {
        this.id = id;
        this.isUploaded = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }
}
