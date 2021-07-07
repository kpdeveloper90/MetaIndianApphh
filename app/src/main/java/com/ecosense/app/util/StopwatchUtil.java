package com.ecosense.app.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>Utility class for running and get timer updates emulating that of a stopwatch</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class StopwatchUtil {

    /**
     * The interface Time update listener.
     */
    public interface TimeUpdateListener {
        /**
         * On update. It is invoked every time the stopwatch timer is updated
         *
         * @param formattedTime the formatted time
         * @param currentTime   the current time
         * @param totalTime     the total time
         */
        void onUpdate(String formattedTime, long currentTime, long totalTime);

        /**
         * On start. It is invoked when the timer is started, the first time.
         *
         * @param startTime   the start time
         * @param refreshRate the refresh rate
         */
        default void onStart(long startTime, int refreshRate) {
        }

        /**
         * On pause. It is invoked when the timer is paused.
         *
         * @param startTime  the start time
         * @param pauseCount the pause count
         * @param totalTime  the total time
         */
        default void onPause(long startTime, long pauseCount, long totalTime) {
        }

        /**
         * On resume. It is invoked when the timer is resumed after it has been paused.
         *
         * @param startTime  the start time
         * @param pausedTime the paused time
         * @param totalTime  the total time
         */
        default void onResume(long startTime, long pausedTime, long totalTime) {
        }

        /**
         * On stop. It is invoked when the timer is stopped. (the timer can't be resumed after this)
         *
         * @param startTime the start time
         * @param totalTime the total time
         */
        default void onStop(long startTime, long totalTime) {
        }

        /**
         * On reset. It is invoked when the timer is reset.
         */
        default void onReset() {
        }
    }

    /**
     * The enum Mode.
     */
    private enum Mode {
        /**
         * Start mode.
         */
        START,
        /**
         * Pause mode.
         */
        PAUSE,
        /**
         * Resume mode.
         */
        RESUME,
        /**
         * Stop mode.
         */
        STOP,
        /**
         * Update mode.
         */
        UPDATE;
    }

    /**
     * The constant HOUR_FORMAT.
     */
    public static final String HOUR_FORMAT = "%02d:%02d:%02d";
    /**
     * The constant MINUTE_FORMAT.
     */
    public static final String MINUTE_FORMAT = "%02d:%02d";
    /**
     * The constant SECOND_FORMAT.
     */
    public static final String SECOND_FORMAT = "%02d sec";

    /**
     * The Start time.
     */
    private long startTime;
    /**
     * The Resume time.
     */
    private long resumeTime;
    /**
     * The Pause time.
     */
    private long pauseTime;
    /**
     * The Pause count.
     */
    private long pauseCount;
    /**
     * The Stop time.
     */
    private long stopTime;
    /**
     * The Refresh rate.
     */
    private int refreshRate;
    /**
     * The Is running.
     */
    private boolean isRunning;
    /**
     * The Pause time list.
     */
    private List<Long> pauseTimeList;
    /**
     * The Resume time list.
     */
    private List<Long> resumeTimeList;

    /**
     * The Formatter.
     */
    private final DateFormat formatter;
    /**
     * The Handler.
     */
    private final Handler handler;
    /**
     * The Listener.
     */
    private TimeUpdateListener listener;

    /**
     * Instantiates a new Stopwatch util.
     *
     * @param refreshRate the refresh rate
     * @param timeFormat  the time format
     * @param listener    the listener
     */
    @SuppressLint("SimpleDateFormat")
    public StopwatchUtil(final int refreshRate, final String timeFormat, final TimeUpdateListener listener) {
        if (refreshRate >= 1)
            this.refreshRate = refreshRate;
        else this.refreshRate = 1000;

        if (!TextUtils.isEmpty(timeFormat))
            formatter = new SimpleDateFormat(timeFormat);
        else formatter = new SimpleDateFormat(HOUR_FORMAT);

        this.listener = listener;
        handler = new Handler();

        reset();
    }

    /**
     * Reset.
     */
    public void reset() {
        startTime = 0;
        resumeTime = 0;
        pauseTime = 0;
        pauseCount = 0;
        stopTime = 0;
        isRunning = false;

        pauseTimeList = new ArrayList<>();
        resumeTimeList = new ArrayList<>();
        stopUpdating();

        if (listener != null)
            listener.onReset();
    }

    /**
     * Start.
     */
    public void start() {
        reset();
        this.startTime = System.currentTimeMillis();
        this.isRunning = true;

        if (listener != null)
            listener.onStart(startTime, refreshRate);

        startUpdating();
    }

    /**
     * Pause.
     */
    public void pause() {
        if (isRunning) {
            if (resumeTime > 0)
                pauseTime = System.currentTimeMillis() - resumeTime;
            else pauseTime = System.currentTimeMillis() - startTime;

            pauseTimeList.add(pauseTime);
            pauseCount++;
            this.isRunning = false;

            if (listener != null)
                listener.onPause(startTime, pauseCount, getTotalElapsedTime());
        }
    }

    /**
     * Resume.
     */
    public void resume() {
        if (!isRunning) {
            if (startTime == 0) {
                this.isRunning = true;
                return;
            }
            this.resumeTime = System.currentTimeMillis() - pauseTime;

            resumeTimeList.add(resumeTime);
            this.isRunning = true;

            if (listener != null)
                listener.onResume(startTime, pauseTime, getTotalElapsedTime());
        }
    }

    /**
     * Stop.
     */
    public void stop() {
        this.isRunning = false;

        if (listener != null)
            listener.onStop(startTime, getTotalElapsedTime());
    }

    /**
     * Sets listener.
     *
     * @param listener the listener
     */
    public void setListener(@NonNull final TimeUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Is running boolean.
     *
     * @return the boolean
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets resume time.
     *
     * @return the resume time
     */
    public long getResumeTime() {
        return resumeTime;
    }

    /**
     * Gets pause time.
     *
     * @return the pause time
     */
    public long getPauseTime() {
        return pauseTime;
    }

    /**
     * Gets pause count.
     *
     * @return the pause count
     */
    public long getPauseCount() {
        return pauseCount;
    }

    /**
     * Gets current elapsed time.
     *
     * @return the current elapsed time
     */
    public long getCurrentElapsedTime() {
        return System.currentTimeMillis() - resumeTime;
    }

    /**
     * Gets total elapsed time.
     *
     * @return the total elapsed time
     */
    public long getTotalElapsedTime() {
        long totalTime = System.currentTimeMillis() - startTime;

        if (!pauseTimeList.isEmpty()) {
            for (int i = 0; i < pauseTimeList.size(); i++) {
                if (resumeTimeList.size() > i)
                    totalTime -= resumeTimeList.get(i) - pauseTimeList.get(i);
                else totalTime -= System.currentTimeMillis() - pauseTimeList.get(i);
            }
        }
        return totalTime;
    }


    /**
     * Start updating.
     */
    private void startUpdating() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                long totalElapsedTime = getTotalElapsedTime();

                if (listener != null)
                    listener.onUpdate(formatter.format(new Date(totalElapsedTime)),
                            getCurrentElapsedTime(), totalElapsedTime);

                handler.postDelayed(this, refreshRate);
            }
        });
    }

    /**
     * Stop updating.
     */
    private void stopUpdating() {
        handler.removeCallbacksAndMessages(null);
    }
}
