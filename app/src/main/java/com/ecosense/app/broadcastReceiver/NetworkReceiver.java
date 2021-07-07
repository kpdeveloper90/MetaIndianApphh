package com.ecosense.app.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.sdsmdg.tastytoast.TastyToast;

import com.ecosense.app.R;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.ConnactionCheckApplication;

public class NetworkReceiver extends BroadcastReceiver {
    public static boolean isConnectionAvailable = false;
    public static boolean connectionPreviousState = false;
    private boolean isRegistered;

    public NetworkReceiver() {
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
        isOnline(context);

        if (!isConnectionAvailable && (isConnectionAvailable != connectionPreviousState)) {
            showConnectionNotAvailable();
            CrashAnalytics.setNetworkConnectivity(false);
        } else if (isConnectionAvailable != connectionPreviousState) {
            showConnectionAvailable();
            CrashAnalytics.setNetworkConnectivity(true);
        }
    }

    public void isOnline(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            // null = airplane mode
            connectionPreviousState = isConnectionAvailable;
            isConnectionAvailable = (networkInfo != null && networkInfo.isConnected());

        } catch (NullPointerException e) {
            connectionPreviousState = isConnectionAvailable;
            isConnectionAvailable = false;
            CrashAnalytics.logException(e);
        }
    }

    public void checkConnectivityAndShowAppropriateAlert() {
        if (!isConnectionAvailable)
            showConnectionNotAvailable();
    }

    private void showConnectionNotAvailable() {
        TastyToast.makeText(ConnactionCheckApplication.activity,
                ConnactionCheckApplication.activity.getString(R.string.you_are_offline),
                TastyToast.LENGTH_LONG, TastyToast.ERROR);
    }

    private void showConnectionAvailable() {
        TastyToast.makeText(ConnactionCheckApplication.activity,
                ConnactionCheckApplication.activity.getString(R.string.back_online),
                TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

    }
}
