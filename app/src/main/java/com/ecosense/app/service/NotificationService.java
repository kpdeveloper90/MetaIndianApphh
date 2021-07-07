package com.ecosense.app.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.CitizenDashBoard;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.NotificationItem;

/**
 * Created by Bhavesh Patel
 */

public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();
    private boolean isRunning = false;
    private static Timer timer;
    TimerTask timerTask;
    UserSessionManger session = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        Log.e("Service", "Start");
        session = new UserSessionManger(getApplicationContext());
        isRunning = true;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.e("In on startCommand", "Start");

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {

//                Your logic that service will perform will be placed here
                if (isRunning) {
                    Log.e("Reminder Service", "Service running");
                    startTimer();
                }
//                //Stop service once it finishes its task
//                stopSelf();
            }
        }).start();

        return Service.START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }


    public void startTimer() {
        int delay = 5000;
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms(Minute)
        timer.schedule(timerTask, delay, 60000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        String strDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                        Log.e("Reminder Service ", "initializeTimerTask Date is => " + strDate);
                        getNotificationOnServer();
                    }
                });
            }
        };
    }

    public void getNotificationOnServer() {


        try {
            String filters = null;
            String fields = null;

            fields = "[\"*\"]";
            Log.e(TAG, "filters = " + filters + "&fields=" + fields);

//                    String url = session.getMyServerIP() + "/api/resource/Notifications?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + URLEncoder.encode(fields, "UTF-8");
            String url = session.getMyServerIP() + "/api/resource/Notifications?fields=" + URLEncoder.encode(fields, "UTF-8");
            StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        // response
                        if (!response.isEmpty()) {
                            Log.e("Response", response);
                            try {
                                //create ObjectMapper instance
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                JsonNode statusData = rootNode.path("data");
//                                    Log.e(TAG, "statusData = " + statusData);
                                if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                    Log.d(TAG, "Data Available");
                                    Iterator<JsonNode> elements1 = statusData.elements();

                                    while (elements1.hasNext()) {
                                        JsonNode notifi = elements1.next();

                                        if (!notifi.toString().trim().equalsIgnoreCase("{}")) {
                                            NotificationItem notificationItem = objectMapper.readValue(notifi.toString(), NotificationItem.class);
                                            Log.e(TAG, "getNotificationID = " + notificationItem.getNotificationID());
                                            Log.e(TAG, "getNftdoctypename = " + notificationItem.getNftdoctypename());

                                            sendSyncNotification(notificationItem);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, getString(R.string.no_record_found_error));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Response IOException " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Response Error");
                        }
                    }, error -> {
                // error
                Log.e("Error.Response", error.toString());
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    String json;
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        try {
                            json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                        } catch (UnsupportedEncodingException e) {
                            return new VolleyError(e.getMessage());
                        }
                        return new VolleyError(json);
                    }

                    return volleyError;
                }
            };
            String tag_string_req = "string_req";
            ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    public static final String CHANNEL_ID = "com.ecosense.app.ANDROID";

    public void sendSyncNotification(NotificationItem notificationItem) {
//        Notification notification;
        NotificationCompat.Builder mNotificationBuilder;


        final int REG_ID = (int) Calendar.getInstance().getTimeInMillis();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, CitizenDashBoard.class);
        notificationIntent.setAction("notificationIntent");
        notificationIntent.putExtra("notification_id", notificationItem.getNotificationID());


        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // This ensures that the back button follows the recommended convention for the back key.
        android.app.TaskStackBuilder stackBuilder = android.app.TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CitizenDashBoard.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notificationIntent);

        // use System.currentTimeMillis() to have a unique ID for the pending intent as request Code pass
        PendingIntent intent = stackBuilder.getPendingIntent(REG_ID, PendingIntent.FLAG_UPDATE_CURRENT);
//                PendingIntent.getActivity(this, (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_event_available_black_24dp);

        String Notification_Coming_message = "Sync is required  :" + notificationItem.getNotificationID();
        String title = notificationItem.getNftdoctypename();
//        int icon = R.mipmap.ic_reminder_event;
        int icon = R.drawable.ic_event_available_black_24dp;
        long when = System.currentTimeMillis();


        Intent IntentReminderDone = new Intent(this, ConnectionReceiver.class);
        Bundle bundle = new Bundle();
        IntentReminderDone.setAction("IntentDISMISS");
        bundle.putString("notification_id", notificationItem.getNotificationID());
        IntentReminderDone.putExtras(bundle);
        PendingIntent pendingIntentReminderDone = PendingIntent.getBroadcast(this, REG_ID, IntentReminderDone, PendingIntent.FLAG_UPDATE_CURRENT);

// Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true);

// .setContentIntent(intent)
//         .setFullScreenIntent(intent,false)
//         .addAction(R.drawable.ic_clear_black_24dp, "DISMISS", pendingIntentReminderDone)
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon)
                .setLargeIcon(bitmap)
                .setWhen(when)
                .setContentTitle(title)
                .setContentText(notificationItem.getNftdesc())
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(notificationItem.getNftdesc()))
                .setTicker(Notification_Coming_message)
                .setVibrate(new long[]{100, 200, 100, 500})
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setGroupSummary(true)
                .setGroup("KEY_NOTIFICATION_GROUP")
                .extend(wearableExtender)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


      /*  notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(desc)
                .setContentIntent(intent)
                .setDeleteIntent(pendingIntentDelete)
                .setSmallIcon(icon)
                .setLargeIcon(bitmap)
                .setWhen(when)
                .extend(wearableExtender)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(desc).setSummaryText("Reminder Time On :" + reminderdatetime))
//                .setStyle(new NotificationCompat.InboxStyle().addLine(desc).setSummaryText("Reminder Time On :" + reminderdatetime))
                .setTicker(Notification_Coming_message)
                .setVibrate(new long[]{100, 200, 100, 500})
                .setLights(Color.RED, 3000, 3000)
                .addAction(R.drawable.ic_home, "Done", pendingIntentReminderDone)
//                .addAction(R.drawable.ic_reminder_snooze, "Snooze", pendingIntentReminderSnooze)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.green_600))
                .setDefaults(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND)
//                .setNumber(++namMessage)
                .setGroupSummary(true)
                .setGroup("KEY_NOTIFICATION_GROUP")
                .build(); */


        // notificationId is a unique int for each notification that you must define
//        notificationManager.notify(notificationId, mBuilder.build());
        Objects.requireNonNull(notificationManager).notify(REG_ID, mNotificationBuilder.build());

    }

}