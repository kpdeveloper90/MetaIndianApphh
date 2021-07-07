package com.ecosense.app.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Helper class to assist in managing and persisting route timer.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PersistentRouteTimerHelper {

    public interface TimeUpdateListener {
        void onUpdate(String formattedTime);
    }

    private static final String TIMER_FORMAT = "%02d:%02d:%02d";
    private static PersistentRouteTimerHelper instance = null;

    private final Handler handler;
    private final DateFormat formatter;
    private TimeUpdateListener listener;
    private final SharedPreferences sharedPreferences;

    public static PersistentRouteTimerHelper getInstance(@NonNull final Context context) {
        if (instance == null)
            synchronized (PersistentRouteTimerHelper.class) {
                if (instance == null)
                    instance = new PersistentRouteTimerHelper(context);
            }
        return instance;
    }

    public void setListener(TimeUpdateListener listener) {
        this.listener = listener;
    }

    @SuppressLint("SimpleDateFormat")
    private PersistentRouteTimerHelper(@NonNull final Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        handler = new Handler();
        formatter = new SimpleDateFormat(TIMER_FORMAT);
    }

    public void startTimer() {
        if (!sharedPreferences.getBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, false)) {
            sharedPreferences.edit().putLong(AppConfig.KEY_PLOTTING_START_TIME, System.currentTimeMillis()).apply();
            sharedPreferences.edit().putLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, 0).apply();
            sharedPreferences.edit().putBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, true).apply();

            startUpdating();
        }
    }

    public void pauseTimer() {
        if (sharedPreferences.getBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, false)) {

            long currentTotalTime = sharedPreferences.getLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, System.currentTimeMillis());

            if (currentTotalTime > 0) {
                currentTotalTime += (System.currentTimeMillis() - sharedPreferences.getLong(AppConfig.KEY_PLOTTING_START_TIME, System.currentTimeMillis()));
                sharedPreferences.edit().putLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, currentTotalTime).apply();
            } else {
                sharedPreferences.edit().putLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, System.currentTimeMillis() -
                        sharedPreferences.getLong(AppConfig.KEY_PLOTTING_START_TIME, System.currentTimeMillis())).apply();
            }

            sharedPreferences.edit().putBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, false).apply();
            stopUpdating();
        }
    }

    public void resumeTimer() {
        if (!sharedPreferences.getBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, false)) {
            sharedPreferences.edit().putLong(AppConfig.KEY_PLOTTING_START_TIME, System.currentTimeMillis()).apply();
            sharedPreferences.edit().putBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, true).apply();
            startUpdating();
        }
    }

    public void stopTimer() {
        pauseTimer();
    }

    public String getTotalTime() {
        long totalElapsedTime;

        if (sharedPreferences.getBoolean(AppConfig.KEY_PLOTTING_TIMER_RUNNING, false))
            totalElapsedTime = sharedPreferences.getLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, System.currentTimeMillis())
                    + (System.currentTimeMillis() - sharedPreferences.getLong(AppConfig.KEY_PLOTTING_START_TIME, System.currentTimeMillis()));
        else
            totalElapsedTime = sharedPreferences.getLong(AppConfig.KEY_PLOTTING_TOTAL_TIME, System.currentTimeMillis());

        return String.format(Locale.getDefault(),
                TIMER_FORMAT,
                TimeUnit.MILLISECONDS.toHours(totalElapsedTime),
                TimeUnit.MILLISECONDS.toMinutes(totalElapsedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalElapsedTime)),
                TimeUnit.MILLISECONDS.toSeconds(totalElapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalElapsedTime)));
    }

    private void startUpdating() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onUpdate(getTotalTime());

                handler.postDelayed(this, 1000);
            }
        });
    }

    private void stopUpdating() {
        handler.removeCallbacksAndMessages(null);
    }
}
