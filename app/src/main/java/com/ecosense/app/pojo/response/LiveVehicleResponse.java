package com.ecosense.app.pojo.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import com.ecosense.app.pojo.LiveVehicle;

/**
 * <h1>POJO class representing a live vehicle response</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class LiveVehicleResponse extends BackendResponse {
    @NonNull
    @SerializedName("data")
    private List<LiveVehicle> liveVehicleList;

    public LiveVehicleResponse(int code, @NonNull String message, @NonNull List<LiveVehicle> liveVehicleList) {
        super(code, message);
        this.liveVehicleList = liveVehicleList;
    }

    public List<String> getVehicleNumberList() {
        List<String> vehicleNumberList = new ArrayList<>();

        for (LiveVehicle liveVehicle : liveVehicleList)
            if (liveVehicle.getVehicleNumber() != null)
                vehicleNumberList.add(liveVehicle.getVehicleNumber());

        return vehicleNumberList;
    }

    @NonNull
    public List<LiveVehicle> getLiveVehicleList() {
        return liveVehicleList;
    }

    public void setLiveVehicleList(@NonNull List<LiveVehicle> liveVehicleList) {
        this.liveVehicleList = liveVehicleList;
    }
}
