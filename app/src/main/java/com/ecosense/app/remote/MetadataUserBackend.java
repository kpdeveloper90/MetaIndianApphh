package com.ecosense.app.remote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.ecosense.app.R;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.helper.alertDialog.AlertDialogHelper;
import com.ecosense.app.helper.alertDialog.MetaUserAuthenticationDialog;
import com.ecosense.app.helper.preference.EncryptedPreferenceHelper;
import com.ecosense.app.pojo.model.MetadataUser;
import com.ecosense.app.pojo.model.User;
import com.ecosense.app.pojo.response.MetadataUserResponse;

/**
 * <h1>Class to authenticate the user for Metadata module
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetadataUserBackend implements MetaUserAuthenticationDialog.OnMetadataUserProgressDialogListener {

    /**
     * The interface On metadata user backend listener.
     */
    public interface OnMetadataUserBackendListener {
        /**
         * On success of the backend request call i.e. when user is authenticated successfully.
         *
         * @param metadataUser the metadata user
         * @see MetadataUser
         */
        void onSuccess(MetadataUser metadataUser);

        /**
         * On error of the backend request call i.e. when user authentication fails either due to
         * invalid credentials or other cause like n/w failure etc.
         *
         * @param backendError the backend error
         * @see BackendError
         */
        default void onFailure(BackendError backendError) {
        }
    }

    /**
     * The Api interface.
     */
    private RetrofitApiInterface apiInterface;
    /**
     * The Activity.
     */
    private final Activity activity;
    /**
     * The Backend listener.
     */
    private final OnMetadataUserBackendListener backendListener;
    /**
     * The User.
     */
    private User user;
    /**
     * The Metadata user.
     */
    private MetadataUser metadataUser;
    /**
     * The Progress dialog helper.
     */
    private MetaUserAuthenticationDialog progressDialogHelper;
    /**
     * The Retry count.
     */
    int retryCount;
    /**
     * The Is cancel button enabled.
     */
    boolean isCancelButtonEnabled;
    /**
     * The Analytics.
     */
    private Analytics analytics;

    /**
     * Instantiates a new Metadata user backend.
     *
     * @param activity the activity
     * @param listener the listener
     */
    public MetadataUserBackend(@NonNull final Activity activity, @NonNull final OnMetadataUserBackendListener listener) {
        this.activity = activity;
        this.backendListener = listener;
        initConfig();
    }

    /**
     * Init configuration needed for proper functioning.
     */
    private void initConfig() {
        apiInterface = new RetrofitHelper().getClient().create(RetrofitApiInterface.class);
        progressDialogHelper = new MetaUserAuthenticationDialog(activity, this);
        analytics = Analytics.getInstance();
        retryCount = 0;
        isCancelButtonEnabled = false;
    }

    /**
     * Method to authenticate the user in the backend using the credentials provided.
     * <p>
     * This method also taken and additional parameter {@code animationDelay} to show animation for
     * minimum of that much time. If no fake emulated animation is needed just pass 0 for this
     * parameter and in that case only the actual time needed for backend query will the animation
     * be shown.
     *
     * @param user           the user
     * @param animationDelay the animation delay
     */
    public void authenticate(@NonNull final User user, final int animationDelay) {
        progressDialogHelper.showProgress();
        retryCount++;
        isCancelButtonEnabled = isCancelButtonEnabled || retryCount % 3 == 0;
        analytics.logSignInAttempt(Analytics.SIGN_IN_METHOD_EMAIL, user.getEmail());
        this.user = user;

        new Handler().postDelayed(() ->
                BackEndRequestCall.enqueue(RetrofitHelper.authenticateMetadataUser(user), "authenticate", new BackEndRequestCall.BackendRequestListener() {
                    @Override
                    public void onSuccess(String tag, @NonNull Object responseBody) {
                        MetadataUserResponse metadataUserResponse = (MetadataUserResponse) responseBody;
                        metadataUser = metadataUserResponse.getMetadataUser();
                        updateMetadataUserPreferences();
                        updateUserSessionManger();
                        updateAppConfig();

                        CrashAnalytics.setLoggedIn(true);
                        analytics.logSignInSuccess(Analytics.SIGN_IN_METHOD_EMAIL, user.getEmail());

                        if (backendListener != null)
                            backendListener.onSuccess(metadataUser);

                        progressDialogHelper.dismiss();
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onError(String tag, @NonNull BackendError backendError) {
                        if (BackendError.INVALID == backendError || BackendError.UNSUCCESSFUL == backendError)
                            progressDialogHelper.showError(activity.getString(R.string.dialog_problem_unauthenticated_user),
                                    activity.getString(R.string.dialog_solution_unauthenticated_user), true);
                        else {
                            Pair<String, String> problemSolutionPair = RetrofitHelper.getProblemSolutionPair(backendError);
                            progressDialogHelper.showError(problemSolutionPair.first, problemSolutionPair.second,
                                    (BackendError.PARSING == backendError ||
                                            (!(BackendError.NETWORK == backendError) && isCancelButtonEnabled)));
                        }

                        if (backendListener != null)
                            backendListener.onFailure(backendError);

                        CrashAnalytics.setLoggedIn(false);
                        CrashAnalytics.log("Sign in failed:" + backendError.toString());
                        analytics.logSignInFailed(Analytics.SIGN_IN_METHOD_EMAIL, user.getEmail(), backendError.toString());
                    }
                }), animationDelay);

    }

    /**
     * Method to update metadata user preferences.
     * <p>
     * All the data is stored is encrypted and hence is safe from malicious manipulation. The data
     * stored using this method is also responsible to deciding on run time whether to show the
     * fingerprint authentication to the user next time they login.
     *
     * @see EncryptedPreferenceHelper
     */
    private void updateMetadataUserPreferences() {
        EncryptedPreferenceHelper preferenceHelper = new EncryptedPreferenceHelper(activity);

        preferenceHelper.setMetaUserId(metadataUser.getId());
        preferenceHelper.setMetaUserName(metadataUser.getName());
        preferenceHelper.setMetaUserEmail(metadataUser.getEmail());
        preferenceHelper.setMetaUserMobile(metadataUser.getMobile());
        preferenceHelper.setPrefMetaUserImageUrl(metadataUser.getImageUrl());
        preferenceHelper.setMetaUserRole(metadataUser.getType());
        preferenceHelper.setMetaUserToken(metadataUser.getToken());

        CrashAnalytics.log("MetaUser preferences updated.");
        analytics.logEvent("meta_user_preferences_updated");
    }

    /**
     * Method to update user session manger.
     */
    private void updateUserSessionManger() {
        UserSessionManger userSessionManger = new UserSessionManger(activity.getApplicationContext());

        userSessionManger.setpsNo(metadataUser.getId());
        userSessionManger.setpsName(metadataUser.getName());
        userSessionManger.setUserProfilePic(metadataUser.getImageUrl());
        userSessionManger.setuserCost_Center(metadataUser.getCostCenter());
        userSessionManger.setMobileNumber(metadataUser.getMobile());
        userSessionManger.setuserSubType(metadataUser.getSubType());
        userSessionManger.setuserType(AppConfig.UType_Team);

        if (!TextUtils.isEmpty(metadataUser.getEmail()))
            userSessionManger.setEmailLogin(metadataUser.getEmail());
    }

    /**
     * Method to update app config.
     */
    private void updateAppConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        sharedPreferences.edit().putString(AppConfig.KEY_DEVICE, metadataUser.getId()).apply();
        sharedPreferences.edit().putBoolean(AppConfig.KEY_STATUS, Connection.KEY_STATUS_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_INTERVAL, Connection.KEY_INTERVAL_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ANGLE, Connection.KEY_ANGLE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_DISTANCE, Connection.KEY_DISTANCE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_URL, Connection.Traccar_url_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ACCURACY, Connection.KEY_ACCURACY_value).apply();
    }

    /**
     * On retry button clicked.
     *
     * @param dialogInstance the dialog instance
     */
    @Override
    public void onRetryButtonClicked(AlertDialogHelper dialogInstance) {
        authenticate(user, 0);
    }

    /**
     * On cancel button clicked.
     *
     * @param dialogInstance the dialog instance
     */
    @Override
    public void onCancelButtonClicked(AlertDialogHelper dialogInstance) {
        dialogInstance.dismiss();
    }
}
