/*
 * Copyright 2013 - 2019 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ecosense.app.Traccar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.UserSessionManger;

public abstract class PositionProvider {

    private static final String TAG = PositionProvider.class.getSimpleName();

    protected static final int MINIMUM_INTERVAL = 1000;

    public interface PositionListener {
        void onPositionUpdate(Position position);

        void onPositionError(Throwable error);
    }

    protected final PositionListener listener;

    protected final Context context;
    protected SharedPreferences preferences;

    protected String deviceId;
    protected long interval;
    protected double distance;
    protected double angle;

    protected Location lastLocation;
    UserSessionManger session = null;

    public PositionProvider(Context context, PositionListener listener) {
        this.context = context;
        this.listener = listener;
        session = new UserSessionManger(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        deviceId = preferences.getString(AppConfig.KEY_DEVICE, "undefined");
        interval = Long.parseLong(preferences.getString(AppConfig.KEY_INTERVAL, "1")) * 1000;
        distance = Integer.parseInt(preferences.getString(AppConfig.KEY_DISTANCE, "0"));
        angle = Integer.parseInt(preferences.getString(AppConfig.KEY_ANGLE, "0"));
    }

    public abstract void startUpdates();

    public abstract void stopUpdates();

    public abstract void requestSingleLocation();

    protected void processLocation(Location location) {
        if (location != null && (lastLocation == null
                || location.getTime() - lastLocation.getTime() >= interval
                || distance > 0 && location.distanceTo(lastLocation) >= distance
                || angle > 0 && Math.abs(location.getBearing() - lastLocation.getBearing()) >= angle)) {
            Log.e(TAG, "location new");
            lastLocation = location;
            String address = getAddress(location);
            listener.onPositionUpdate(new Position(deviceId, location, getBatteryLevel(context), address, session.getpsNo(), "RoutePlot"));
        } else {
            Log.e(TAG, location != null ? "location ignored" : "location nil");
        }
    }

    public String getAddress(Location location) {


        Geocoder geocoder = new Geocoder(context, Locale.getDefault());


        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = null;

            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
            }

            //addMarker();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static double getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            return (level * 100.0) / scale;
        }
        return 0;
    }

}
