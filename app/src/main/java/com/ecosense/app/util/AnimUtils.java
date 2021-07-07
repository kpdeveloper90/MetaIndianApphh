package com.ecosense.app.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;

/**
 * <h1>Class having various wrapper functions for simple animations of the {@link View}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.3
 */
public class AnimUtils {

    /**
     * Method to rotate the view by the given degrees relative to its current rotation.
     *
     * @param view     {@link View} to be rotated.
     * @param duration time duration (in milliseconds) for the animation to take place
     * @param degree   degree (0 to 360) to which the view is to be rotated.
     * @param listener {@link Animator.AnimatorListener} reference.
     * @see #absoluteRotate(View, int, float, Animator.AnimatorListener)
     */
    public static void relativeRotate(@NonNull final View view,
                                      final int duration,
                                      final float degree,
                                      final Animator.AnimatorListener listener) {

        absoluteRotate(view, duration, view.getRotation() + degree, listener);
    }

    /**
     * Method to rotate the view by the given degrees.
     *
     * @param view     {@link View} to be rotated.
     * @param duration time duration (in milliseconds) for the animation to take place
     * @param degree   degree (0 to 360) to which the view is to be rotated.
     * @param listener {@link Animator.AnimatorListener} reference.
     */
    public static void absoluteRotate(@NonNull final View view,
                                      final int duration,
                                      final float degree,
                                      final Animator.AnimatorListener listener) {
        view.animate()
                .setDuration(duration)
                .setListener(listener)
                .rotation(degree);
    }

    /**
     * Method to set flashing animation for the view.
     *
     * @param view     {@link View} to be which the flashing animation is to be applied.
     * @param listener {@link Animator.AnimatorListener} reference.
     */
    public static void setFlashAnimation(@NonNull final View view, @NonNull final Animator.AnimatorListener listener) {
        int duration = 100;

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeIn.setDuration(duration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(duration);

        final AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(fadeIn).after(fadeOut);
        animatorSet.addListener(listener);

        animatorSet.start();
    }

    /**
     * Method to shrink and then expand the view.
     *
     * @param view {@link View} to be which the animation is to be applied.
     */
    public static void shrinkAndExpand(@NonNull final View view) {
        view.getAnimation();
        view.animate()
                .setDuration(300)
                .scaleX(0.3f)
                .scaleY(0.3f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate()
                                .setDuration(300)
                                .scaleX(1f)
                                .scaleY(1f);
                    }
                });
    }

    /**
     * Method to apply infinite blinking animation on given {@link View}
     *
     * @param view {@link View} to be which the animation is to be applied.
     */
    public static void setBlinkingAnimation(@NonNull final View view) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(800);
        anim.setStartOffset(50);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    /**
     * Method to apply infinite rotational animation to the {@code view} which reverses the animation
     * once a complete rotation has been achieved
     *
     * @param view {@link View} to be which the animation is to be applied.
     */
    public static void setRotatingAnimation(@NonNull final View view) {
        RotateAnimation anim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(1600);
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new LinearInterpolator());
        view.startAnimation(anim);
    }
}
