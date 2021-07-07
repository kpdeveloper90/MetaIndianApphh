/*
 * Copyright 2013 - 2017 Anton Tananaev (anton@traccar.org)
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

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ecosense.app.helper.AppConfig;

import static android.content.Context.ACTIVITY_SERVICE;

public class AutostartReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = AutostartReceiver.class.getSimpleName();
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (!isServiceRunning()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
                startWakefulForegroundService(context, new Intent(context, TrackingService.class));
            }
        }else{
            Log.e(TAG, "AutostartReceiver TrackingService Already Running");
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.Traccar.TrackingService".equals(service.service.getClassName())) {
                Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " SyncService not Running  return false");
        return false;
    }
}
