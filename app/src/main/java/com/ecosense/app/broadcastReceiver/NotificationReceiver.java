package com.ecosense.app.broadcastReceiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Bhavesh Patel
 */

public class NotificationReceiver extends BroadcastReceiver {
    private int notification_Id;

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        this.context = context;
        if (extras == null) {
            notification_Id = 0;
        } else {
            notification_Id = extras.getInt("notification_id");
        }
        String intentAction = intent.getAction();
        Log.e("Brodcast", "intentAction => " + intentAction + "  >> " + notification_Id);

    }
}
