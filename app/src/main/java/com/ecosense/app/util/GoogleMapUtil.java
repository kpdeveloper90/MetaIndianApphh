package com.ecosense.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * <h1>Class to handle the hiding of soft keyboard.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class GoogleMapUtil {

    /**
     * Method to open Google maps and start the direction from user's current location to the given location.
     *
     * @param context Activity or application context
     * @param latLng
     * @see GoogleMapUtil#startGoogleDirections(Context, double, double)
     */
    public static void startGoogleDirections(@NonNull final Context context, @NonNull final LatLng latLng) {
        startGoogleDirections(context, latLng.latitude, latLng.longitude);
    }

    /**
     * Method to open Google maps and start the direction from user's current location to the given location.
     *
     * @param context Activity or application context
     * @param lat     latitude of the location to which the direction is to be plotted
     * @param lon     longitude of the location to which the direction is to be plotted
     */
    public static void startGoogleDirections(@NonNull final Context context, final double lat, final double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }
}
