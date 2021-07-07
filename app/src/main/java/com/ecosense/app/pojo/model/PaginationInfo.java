package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

/**
 * <h1>POJO class representing pagination info</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PaginationInfo {
    @NonNull
    private String page;
    @NonNull
    private String hitsPerPage;
    @NonNull
    private String totalDataCount;
    @NonNull
    private String totalPages;

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
}
