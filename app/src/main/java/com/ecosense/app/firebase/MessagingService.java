package com.ecosense.app.firebase;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * <h1>Service class extending {@link FirebaseMessagingService} to incorporate firebase messaging into this app</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MessagingService extends FirebaseMessagingService {
    private final String TAG = MessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //Message data payload: {surname=mishra, url=https://jhsgg.com, cost=5, name=vivek}
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            // Message Notification Body: test body3
        }
    }

    @Override
    public void onDeletedMessages() {
        Log.d(TAG, "onDelete: ");
    }

    public void getToken(final Activity activity) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        Log.d(TAG, "onComplete: " + token);
                    }
                });
    }

//    public void subscribeToTopic(Activity activity, final NonNull String topic) {
//        EncryptedPreferenceHelper preference = new EncryptedPreferenceHelper(activity);
//        preference.putBoolean("FCM_TOPIC_" + topic, false);
//
//        FirebaseMessaging.getInstance().subscribeToTopic(topic)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())
//                            preference.putBoolean("FCM_TOPIC_" + topic, true);
//                    }
//                });
//    }
//
//    public void unsubscribeToTopic(Activity activity, final NonNull String topic) {
//        EncryptedPreferenceHelper preference = new EncryptedPreferenceHelper(activity);
//
//        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())
//                            preference.putBoolean("FCM_TOPIC_" + topic, false);
//                    }
//                });
//    }
}
