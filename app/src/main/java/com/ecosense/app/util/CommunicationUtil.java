package com.ecosense.app.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

/**
 * <h1>Utility class for communication (messsage, email, call etc.) related functionality </h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class CommunicationUtil {

    /**
     * Method to send <code>message</code> to the <code>number</code> using {@link SmsManager}
     *
     * @param number  {@link String} phone number to which the sms is to be sent.
     * @param message {@link String} text message to be sent.
     */
    public static void sendSms(@NonNull final String number, @NonNull final String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }

    /**
     * Method to open sms app (based on user selection, if more than one is available), preloaded with
     * <code>message</code> and <code>number</code>, using intents.
     *
     * @param context Activity or Application context.
     * @param number  {@link String} phone number to which the sms is to be sent.
     * @param message {@link String} text message to be sent.
     * @return {@code true} if the some app is available to handle the sms intent else {@code false}
     */
    public static boolean sendSmsUsingIntent(@NonNull final Context context,
                                             @NonNull final String number,
                                             @NonNull final String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + number));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        } else return false;
    }

    /**
     * Method to open sms app (based on user selection, if more than one is available), preloaded with
     * <code>number</code>, using intents.
     *
     * @param context Activity or Application context.
     * @param number  {@link String} phone number to which the sms is to be sent.
     * @return {@code true} if the some app is available to handle the sms intent else {@code false}
     */
    public static boolean sendSmsUsingIntent(@NonNull final Context context,
                                             @NonNull final String number) {
        return sendSmsUsingIntent(context, number, "");
    }

    /**
     * Method to initiate call to the given number if possible else open the dialer with the number
     * pasted on to dialer.
     *
     * @param context Activity or Application context.
     * @param number  number to which the call is to be initiated.
     * @return {@code true} if the app had sufficient permission and call was initiated.<br>
     * {@code false} if the call failed and the function opened the dialer instead.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.CALL_PHONE)
    public static boolean callOrOpenDialer(@NonNull final Context context, @NonNull final String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        boolean status = true;

        if (intent.resolveActivity(context.getPackageManager()) != null)
            if (context.checkSelfPermission(Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                intent = new Intent(Intent.ACTION_DIAL);
                status = false;
            }

        intent.setData(Uri.parse("tel:" + number));
        context.startActivity(intent);
        return status;
    }

    /**
     * Method to send mail using the existing email client app on th device.
     * <i>Method shows an error message as toast if any appropriate app to handle the explicit
     * intent is not found.</i>
     *
     * @param context Activity or Application context.
     * @param address email address gto which the email is to be sent.
     * @param subject subject of the email.
     * @param message message/body of the email.
     * @return {@code true} if the device has appropriate email client to send mail and user is
     * directed to that app else {@code false}
     */
    public static boolean sendEmail(@NonNull final Context context, @NonNull final String address,
                                    @NonNull final String subject, @NonNull final String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));

        intent.putExtra(Intent.EXTRA_EMAIL, address);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else
            return false;
    }
}
