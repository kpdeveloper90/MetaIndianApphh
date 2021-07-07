package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import com.ecosense.app.databinding.LayoutPoiInfoBinding;
import com.ecosense.app.db.models.PoiPoint;

public class PoiInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutPoiInfoBinding rootBinding;

    public PoiInfoWindowAdapter(@NonNull final Context context) {
        rootBinding = LayoutPoiInfoBinding.inflate(LayoutInflater.from(context));
    }

    @Override
    public View getInfoWindow(Marker marker) {
        PoiPoint poiPoint = (PoiPoint) marker.getTag();

        if (poiPoint == null)
            return null;
        else {
            rootBinding.tvPoiName.setText(poiPoint.getName());
            rootBinding.cpPoiType.setText(poiPoint.getType());
            rootBinding.tvLat.setText(poiPoint.getCoordinate().getLatitude());
            rootBinding.tvLon.setText(poiPoint.getCoordinate().getLongitude());
            return rootBinding.getRoot();
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
