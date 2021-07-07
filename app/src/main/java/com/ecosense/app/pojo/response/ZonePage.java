package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import com.ecosense.app.pojo.model.Zone;

/**
 * <h1>POJO class representing one ward paginated response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class ZonePage {
    @NonNull
    private String page;
    @NonNull
    private String hitsPerPage;
    @NonNull
    private String totalDataCount;
    @NonNull
    private String totalPages;
    @NonNull
    @SerializedName("data")
    private List<Zone> zoneList;

    @NonNull
    public String getPage() {
        return page;
    }

    public void setPage(@NonNull String page) {
        this.page = page;
    }

    @NonNull
    public String getHitsPerPage() {
        return hitsPerPage;
    }

    public void setHitsPerPage(@NonNull String hitsPerPage) {
        this.hitsPerPage = hitsPerPage;
    }

    @NonNull
    public String getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(@NonNull String totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    @NonNull
    public String getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(@NonNull String totalPages) {
        this.totalPages = totalPages;
    }

    @NonNull
    public List<Zone> getZoneList() {
        return zoneList;
    }

    public void setZoneList(@NonNull List<Zone> zoneList) {
        this.zoneList = zoneList;
    }
}
