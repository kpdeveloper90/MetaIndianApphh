package com.ecosense.app.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.PermissionHelper;
import com.ecosense.app.util.LocationUtil;

/**
 * <h1>Broadcast receiver to handle and notify GPS state change</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class GpsLocationReceiver extends BroadcastReceiver {

    private final Handler handler;
    private static final int DELAY_BEFORE_BROADCASTING = 1000; //just a hacky way to counteract the multiple identical callbacks
    private boolean isRegistered;

    public GpsLocationReceiver() {
        handler = new Handler();
        isRegistered = false;
    }

    public void register(@NonNull final Context context) {
        try {
            if (!isRegistered)
                context.registerReceiver(this, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        } finally {
            isRegistered = true;
        }
    }

    public void unRegister(@NonNull final Context context) {
        try {
            if (isRegistered) context.unregisterReceiver(this);
        } finally {
            isRegistered = false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
            if (!LocationUtil.isGPSLocationEnabled(context)) { //location disabled
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() ->
                {
                    CrashAnalytics.setGpsEnabled(false);
                    new PermissionHelper(context).startLocationSettingIntent(null);

                }, DELAY_BEFORE_BROADCASTING);
            }
            else CrashAnalytics.setGpsEnabled(true);
        }
    }
}
