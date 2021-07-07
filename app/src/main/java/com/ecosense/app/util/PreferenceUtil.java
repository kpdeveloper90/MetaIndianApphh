package com.ecosense.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <h1>Preference util class to help save and retrieve preferences easily and effectively</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public abstract class PreferenceUtil {
    /**
     * The Shared preferences.
     */
    protected SharedPreferences sharedPreferences;
    /**
     * The Editor.
     */
    protected SharedPreferences.Editor editor;
    /**
     * The Context.
     */
    protected Context context;

    /**
     * Instantiates a new Preference util.
     *
     * @param context the context
     */
    protected PreferenceUtil(@NonNull Context context) {
        this.context = context;
        initSharedPreferences();
        editor = sharedPreferences.edit();
    }

    /**
     * Gets boolean.
     *
     * @param preferenceKey the preference key
     * @param defaultValue  the default value
     * @return the boolean
     */
    public boolean getBoolean(@NonNull final String preferenceKey, final boolean defaultValue) {
        return sharedPreferences.getBoolean(preferenceKey, defaultValue);
    }

    /**
     * Gets int.
     *
     * @param preferenceKey the preference key
     * @param defaultValue  the default value
     * @return the int
     */
    public int getInt(@NonNull final String preferenceKey, final int defaultValue) {
        return sharedPreferences.getInt(preferenceKey, defaultValue);
    }

    /**
     * Gets long.
     *
     * @param preferenceKey the preference key
     * @param defaultValue  the default value
     * @return the long
     */
    public long getLong(@NonNull final String preferenceKey, final long defaultValue) {
        return sharedPreferences.getLong(preferenceKey, defaultValue);
    }

    /**
     * Gets string.
     *
     * @param preferenceKey the preference key
     * @param defaultValue  the default value
     * @return the string
     */
    public String getString(@NonNull final String preferenceKey, final String defaultValue) {
        return sharedPreferences.getString(preferenceKey, defaultValue);
    }

    /**
     * Gets string set.
     *
     * @param preferenceKey the preference key
     * @param defaultValues the default values
     * @return the string set
     */
    public Set<String> getStringSet(@NonNull final String preferenceKey, final Set<String> defaultValues) {
        return sharedPreferences.getStringSet(preferenceKey, defaultValues);
    }

    /**
     * Gets boolean list.
     *
     * @param preferenceKey the preference key
     * @param count         the count
     * @return the boolean list
     */
    public List<Boolean> getBooleanList(@NonNull final String preferenceKey, final int count) {
        List<Boolean> booleanList = new ArrayList<>(count);

        for(int i=0;i<count;i++)
            booleanList.add(sharedPreferences.getBoolean(preferenceKey+i, false));

        return booleanList;
    }


    /**
     * Put boolean.
     *
     * @param preferenceKey the preference key
     * @param value         the value
     */
//editor
    public void putBoolean(@NonNull final String preferenceKey, final boolean value) {
        editor.putBoolean(preferenceKey, value);
        editor.apply();
    }

    /**
     * Put int.
     *
     * @param preferenceKey the preference key
     * @param value         the value
     */
    public void putInt(@NonNull final String preferenceKey, final int value) {
        editor.putInt(preferenceKey, value);
        editor.apply();
    }

    /**
     * Put long.
     *
     * @param preferenceKey the preference key
     * @param value         the value
     */
    public void putLong(@NonNull final String preferenceKey, final long value) {
        editor.putLong(preferenceKey, value);
        editor.apply();
    }

    /**
     * Put string.
     *
     * @param preferenceKey the preference key
     * @param value         the value
     */
    public void putString(@NonNull final String preferenceKey, @NonNull final String value) {
        editor.putString(preferenceKey, value);
        editor.apply();
    }

    /**
     * Put string set.
     *
     * @param preferenceKey the preference key
     * @param values        the values
     */
    public void putStringSet(@NonNull final String preferenceKey, @NonNull final Set<String> values) {
        editor.putStringSet(preferenceKey, values);
        editor.apply();
    }

    /**
     * Put boolean list.
     *
     * @param preferenceKey the preference key
     * @param booleanList   the boolean list
     */
    public void putBooleanList(@NonNull final String preferenceKey, final List<Boolean> booleanList) {
        for(int i=0;i<booleanList.size();i++)
            editor.putBoolean(preferenceKey+i, booleanList.get(i));

        editor.apply();
    }

    /**
     * Init shared preferences.
     */
    protected abstract void initSharedPreferences();
}
