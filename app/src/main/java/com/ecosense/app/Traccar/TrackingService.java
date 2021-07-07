/*
 * Copyright 2012 - 2017 Anton Tananaev (anton@traccar.org)
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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ecosense.app.R;
import com.ecosense.app.activity.SplashScreen;

/**
 * The class Tracking service.
 */
public class TrackingService extends Service {

    /**
     * The constant TAG.
     */
    private static final String TAG = TrackingService.class.getSimpleName();
    /**
     * The constant NOTIFICATION_ID.
     */
    public static final int NOTIFICATION_ID = 1;

    /**
     * The Tracking controller.
     */
    private TrackingControllerNew trackingController;

    /**
     * Method to create notification in system status bar.
     *
     * @param context the context
     * @return the notification
     */
    public void createNotification(Context context) {
     /*   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConnactionCheckApplication.PRIMARY_CHANNEL)
                .setSmallIcon(R.mipmap.ic_dwms)
                .setContentTitle(context.getString(R.string.notification_location_tracking_title))
                .setContentText(context.getString(R.string.notification_location_tracking_description))
                .setTicker(context.getString(R.string.notification_location_tracking_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE);

        return builder.build();*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        } else {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, SplashScreen.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    0);

            NotificationCompat.BigTextStyle appCompatStyle = new NotificationCompat.BigTextStyle();
            appCompatStyle.bigText(getString(R.string.app_name));

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(getString(R.string.app_name))
                    .setStyle(appCompatStyle)
                    .setTicker("Location")
                    .setContentText("Location")
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION, notification);
        }


    }

    private NotificationManager notificationManager;
    private static final int NOTIFICATION = 3;

    /**
     * The class Hide notification service.
     */
    public static class HideNotificationService extends Service {


        public void createNotification(Context context) {
     /*   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConnactionCheckApplication.PRIMARY_CHANNEL)
                .setSmallIcon(R.mipmap.ic_dwms)
                .setContentTitle(context.getString(R.string.notification_location_tracking_title))
                .setContentText(context.getString(R.string.notification_location_tracking_description))
                .setTicker(context.getString(R.string.notification_location_tracking_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE);

        return builder.build();*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
                String channelName = "My Background Service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("App is running in background")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
                startForeground(2, notification);
            } else {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, SplashScreen.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        0);

                NotificationCompat.BigTextStyle appCompatStyle = new NotificationCompat.BigTextStyle();
                appCompatStyle.bigText(getString(R.string.app_name));

                Notification notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(appCompatStyle)
                        .setTicker("Location")
                        .setContentText("Location")
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build();

                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION, notification);
            }


        }

        private NotificationManager notificationManager;
        private static final int NOTIFICATION = 3;


        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        /**
         * On create.
         */
        @Override
        public void onCreate() {
            createNotification(this);
            //  startForeground(NOTIFICATION_ID, );
            stopForeground(true);
        }

        /**
         * On start command int.
         *
         * @param intent  the intent
         * @param flags   the flags
         * @param startId the start id
         * @return the int
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }
    }

    /**
     * On create.
     */
    @Override
    public void onCreate() {
        Log.e(TAG, "service = > onCreate");

        // startForeground(NOTIFICATION_ID, );
        createNotification(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trackingController = new TrackingControllerNew(this);
            trackingController.start();
            Log.e(TAG, "service = > controller called");
        }

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            ContextCompat.startForegroundService(this, new Intent(this, HideNotificationService.class));
//        }
    }

    /**
     * On bind binder.
     *
     * @param intent the intent
     * @return the binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * On start command int.
     *
     * @param intent  the intent
     * @param flags   the flags
     * @param startId the start id
     * @return the int
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            AutostartReceiver.completeWakefulIntent(intent);
        }
        Log.e(TAG, "service = > start called");
        return START_STICKY;
    }

    /**
     * On destroy.
     */
    @Override
    public void onDestroy() {
        Log.e(TAG, "service destroy = " + getString(R.string.status_service_destroy));

        stopForeground(true);

        if (trackingController != null) {
            trackingController.stop();
        }
    }

}
