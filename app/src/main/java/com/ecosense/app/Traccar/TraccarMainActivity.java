package com.ecosense.app.Traccar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.Connection;

public class TraccarMainActivity extends AppCompatActivity {


    private static final String TAG = TraccarMainActivity.class.getSimpleName();

    private static final int ALARM_MANAGER_INTERVAL = 15000;


    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    private SharedPreferences sharedPreferences;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traccar_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);

//        if (!sharedPreferences.contains(KEY_DEVICE)) {
//            String id = String.valueOf(new Random().nextInt(900000) + 100000);
//
//        }
        sharedPreferences.edit().putString(AppConfig.KEY_DEVICE, "547254").apply();
        sharedPreferences.edit().putBoolean(AppConfig.KEY_STATUS, true).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_INTERVAL, "15").apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ANGLE, "0").apply();
        sharedPreferences.edit().putString(AppConfig.KEY_DISTANCE, "0").apply();
        sharedPreferences.edit().putString(AppConfig.KEY_URL, Connection.Traccar_url_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ACCURACY, this.getString(R.string.settings_accuracy_values_high)).apply();


//        if (isServiceRunning() == false) {
            //        if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
//            startTrackingService(true, false);
//        }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
//        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");
//        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
//        if (isServiceRunning() == true) {
//            stopTrackingService();
//        }
    }
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.Traccar.TrackingService".equals(service.service.getClassName())) {
                Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " SyncService not Running  return false");
        return false;
    }

    private void startTrackingService(boolean checkPermission, boolean permission) {
        if (checkPermission) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                permission = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
        }

        if (permission) {
            ContextCompat.startForegroundService(this, new Intent(this, TrackingService.class));
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL, ALARM_MANAGER_INTERVAL, alarmIntent);
        } else {
//            sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply();
//            TwoStatePreference preference = (TwoStatePreference) findPreference(KEY_STATUS);
//            preference.setChecked(false);
//            if (isServiceRunning() == true) {
//                stopTrackingService();
//            }
        }
    }

    private void stopTrackingService() {
        alarmManager.cancel(alarmIntent);
        stopService(new Intent(this, TrackingService.class));
//        setPreferencesEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (isServiceRunning() == false) {
                // if start tracking Uncomment condition
                if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
                    startTrackingService(true, false);
                }
            }
        }
    }

    private boolean validateServerURL(String userUrl) {
        int port = Uri.parse(userUrl).getPort();
        if (URLUtil.isValidUrl(userUrl) && (port == -1 || (port > 0 && port <= 65535))
                && (URLUtil.isHttpUrl(userUrl) || URLUtil.isHttpsUrl(userUrl))) {
            return true;
        }
        Toast.makeText(this, R.string.error_msg_invalid_url, Toast.LENGTH_LONG).show();
        return false;
    }
}
