package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>POJO class representing pagination backend response data</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PaginatedData {
    @NonNull
    private String page;
    @NonNull
    private String hitsPerPage;
    @NonNull
    private String totalDataCount;
    @NonNull
    private String totalPages;
    @NonNull
    private List<Object> data;

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
    public List<?> getData() {
        return data;
    }

    public void setData(@NonNull List<Object> data) {
        this.data = data;
    }

    public void addData(@NonNull List<Object> data) {
        if(data == null)
            data = new ArrayList<>();
        this.data.addAll(data);
    }
}
