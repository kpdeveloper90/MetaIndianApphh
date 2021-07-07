package com.ecosense.app.helper;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ecosense.app.util.CommunicationUtil;

/**
 * <h1>Helper class having convenience methods for to work in tandem with {@link CommunicationUtil} </h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class CommunicationHelper {
    /**
     * Class variable storing the email address to which the contact support mail will be sent.
     */
    final private static String EMAIL_ADDRESS = "mvivekanandji@gmail.com";

    /**
     * Class variable storing the subject of the contact support mail.
     */
    final private static String EMAIL_SUBJECT = "Metadata User";

    /**
     * Class variable storing the stating sentence of the contact support mail.
     */
    final private static String EMAIL_MESSAGE = "Hi! I have been using your app and ...";


    public static boolean sendContactMail(@NonNull final Context context) {
        return CommunicationUtil.sendEmail(context.getApplicationContext(), EMAIL_ADDRESS, EMAIL_SUBJECT, EMAIL_MESSAGE);
    }

    @SuppressLint("NewApi")
    public static boolean callOrOpenDialer(@NonNull final Context context, @NonNull final String number){
        new PermissionHelper(context).checkAndRequestCallPermission(context, null);
        return CommunicationUtil.callOrOpenDialer(context.getApplicationContext(), number);
    }

}
