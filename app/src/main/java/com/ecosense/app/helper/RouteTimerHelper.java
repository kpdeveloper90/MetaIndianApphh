package com.ecosense.app.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Helper class to assist in managing route timer.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteTimerHelper {

    public interface TimeUpdateListener {
        void onUpdate(String formattedTime);
    }

    private static final String TIMER_FORMAT = "%02d:%02d:%02d";
    private static RouteTimerHelper instance = null;

    private final Handler handler;
    private TimeUpdateListener listener;

    private boolean isTimerRunning;
    private long startTime;
    private long totalTime;


    public static RouteTimerHelper getInstance() {
        if (instance == null)
            synchronized (RouteTimerHelper.class) {
                if (instance == null)
                    instance = new RouteTimerHelper(ConnactionCheckApplication.getInstance());
            }
        return instance;
    }

    public void setListener(TimeUpdateListener listener) {
        this.listener = listener;
    }

    @SuppressLint("SimpleDateFormat")
    private RouteTimerHelper(@NonNull final Context context) {
        handler = new Handler();
        isTimerRunning = false;
        startTime = 0;
        totalTime = 0;
    }

    public void startTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis();
            totalTime = 0;
            isTimerRunning = true;
            startUpdating();
        }
    }

    public void pauseTimer() {
        if (isTimerRunning) {
            if (totalTime > 0)
                totalTime += System.currentTimeMillis() - startTime;
            else
                totalTime = System.currentTimeMillis() - startTime;

            isTimerRunning = false;
            stopUpdating();
        }
    }

    public void resumeTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis();
            isTimerRunning = true;
            startUpdating();
        }
    }

    public void stopTimer() {
        pauseTimer();
    }

    public String getTotalTime() {
        long totalElapsedTime = totalTime;

        if (isTimerRunning)
            totalElapsedTime +=  System.currentTimeMillis() - startTime;

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
