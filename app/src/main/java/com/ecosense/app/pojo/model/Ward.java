package com.ecosense.app.pojo.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>POJO class of a ward</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class Ward extends Locality {
    @NonNull
    private Object zoneId;

    @NonNull
    public Object getZoneId() {
        return zoneId;
    }

    public void setZoneId(@NonNull String zoneId) {
        this.zoneId = zoneId;
    }

    public static List<String> getWardNumberList(List<Ward> wardList) {
        List<String> wardNumber = new ArrayList<>();

        for (Ward ward : wardList)
            if (ward.getNumber() != null)
                wardNumber.add(ward.getNumber());

        return wardNumber;
    }
}
