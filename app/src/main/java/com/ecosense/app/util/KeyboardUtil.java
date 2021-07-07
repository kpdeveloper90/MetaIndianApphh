package com.ecosense.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

/**
 * <h1>Class to handle the hiding of soft keyboard.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class KeyboardUtil {

    /**
     * Method to hide the soft keyboard from activity context.
     * <p>
     * Method first tries to find the currently focused view, so correct window token can be grabbed from it.
     * If no view currently has focus, a new one is created to grab a window token from it
     *
     * @param activity current activity where soft keyboard is drawn
     * @see KeyboardUtil#hideKeyboardFromFragment(Context, View) to hide keyboard from fragments (including
     * when dialog is used)
     */
    public static void hideKeyboard(@NonNull final Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();

        if (view == null) view = new View(activity);

        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Method to hide the soft keyboard from dialog context.
     *
     * @param context parent activity context
     * @param view    current focused view
     */
    public static void hideKeyboardFromDialog(@NonNull final Context context, @NonNull final View view) {
        hideKeyboardFromFragment(context, view);
    }

    /**
     * Method to hide the soft keyboard from fragment context.
     *
     * @param context parent activity context
     * @param view    current focused view
     */
    public static void hideKeyboardFromFragment(@NonNull final Context context, @NonNull final View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
