package com.ecosense.app.helper.view;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ecosense.app.R;
import com.ecosense.app.helper.ConnactionCheckApplication;

/**
 * <h1>Helper class to simplify setting text to views</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class TextSetter {

    public static void setText(@NonNull final TextView textView, final String text) {
        if (TextUtils.isEmpty(text)) {
            textView.setText(ConnactionCheckApplication.getInstance().getString(R.string.na_tag));
        } else {
            textView.setText(text);
        }
    }

}
