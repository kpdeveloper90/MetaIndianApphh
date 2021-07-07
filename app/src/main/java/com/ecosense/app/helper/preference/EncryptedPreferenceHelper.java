package com.ecosense.app.helper.preference;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ecosense.app.util.EncryptedPreferenceUtil;

/**
 * <h1>Preference helper class to store and retrieve encrypted shared preferences</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class EncryptedPreferenceHelper extends EncryptedPreferenceUtil {

    private static final String PREF_APP_VERSION = "appVersion";
    private static final String PREF_META_USER_ID = "metaUserId";
    private static final String PREF_META_USER_NAME = "metaUserName";
    private static final String PREF_META_USER_EMAIL = "metaUserEmail";
    private static final String PREF_META_USER_MOBILE = "metaUserMobile";
    private static final String PREF_META_USER_IMAGE_URL = "metaUserImageUrl";
    private static final String PREF_META_USER_ROLE = "metaUserRole";
    private static final String PREF_META_USER_TOKEN = "metaUserToken";
    private static final String ALL_PROJECT_ID = "allprojectid";
  private static final String CURRENT_PROJECT_ID = "currentprojectid";

    public EncryptedPreferenceHelper(@NonNull Context context) {
        super(context);
    }

    public String getAppVersion() {
        return getString(PREF_APP_VERSION, "1.0");
    }

    public void setAppVersion(final String appVersion) {
        putString(PREF_APP_VERSION, appVersion);
    }

    public String getMetaUserId() {
        return getString(PREF_META_USER_ID, null);
    }

    public void setMetaUserId(final String id) {
        putString(PREF_META_USER_ID, id);
    }

    public String getMetaUserName() {
        return getString(PREF_META_USER_NAME, null);
    }

    public void setAllProject(final String id) {
        putString(ALL_PROJECT_ID, id);
    }

    public String getAllProject() {
        return getString(ALL_PROJECT_ID, null);
    }
  public void setCurrentProject(final String id) {
        putString(CURRENT_PROJECT_ID, id);
    }

    public String getCurrentProject() {
        return getString(CURRENT_PROJECT_ID, "60a4e91bd6300c39aaad0775");
    }

    public void setMetaUserName(final String name) {
        putString(PREF_META_USER_NAME, name);
    }

    public String getMetaUserEmail() {
        return getString(PREF_META_USER_EMAIL, null);
    }

    public void setMetaUserEmail(final String email) {
        putString(PREF_META_USER_EMAIL, email);
    }

    public String getMetaUserMobile() {
        return getString(PREF_META_USER_MOBILE, null);
    }

    public void setMetaUserMobile(final String contactNumber) {
        putString(PREF_META_USER_MOBILE, contactNumber);
    }

    public String getPrefMetaUserImageUrl() {
        return getString(PREF_META_USER_IMAGE_URL, null);
    }

    public void setPrefMetaUserImageUrl(final String prefMetaUserImageUrl) {
        putString(PREF_META_USER_IMAGE_URL, prefMetaUserImageUrl);
    }

    public String getMetaUserRole() {
        return getString(PREF_META_USER_ROLE, null);
    }

    public void setMetaUserRole(final String role) {
        putString(PREF_META_USER_ROLE, role);
    }

    public String getMetaUserToken() {
        return getString(PREF_META_USER_TOKEN, null);
    }

    public void setMetaUserToken(final String token) {
        putString(PREF_META_USER_TOKEN, token);
    }

}
