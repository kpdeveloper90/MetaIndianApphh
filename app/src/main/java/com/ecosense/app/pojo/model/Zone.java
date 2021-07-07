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
public class Zone extends Locality {
    @NonNull
    private String eseCityId;

    @NonNull
    public String getEseCityId() {
        return eseCityId;
    }

    public void setEseCityId(@NonNull String eseCityId) {
        this.eseCityId = eseCityId;
    }

    public static List<String> getZoneNameList(List<Zone> zoneList){
        List<String> zoneNameList = new ArrayList<>();

        for(Zone zone: zoneList)
            if (zone.getName() != null)
            zoneNameList.add(zone.getName());

        return zoneNameList;
    }
}
