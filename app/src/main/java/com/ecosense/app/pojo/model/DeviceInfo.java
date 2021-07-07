package com.ecosense.app.pojo.model;

import android.os.Build;

import androidx.annotation.NonNull;

import com.ecosense.app.BuildConfig;

/**
 * <h1>POJO class of device info</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class DeviceInfo {
    @NonNull
    private String versionCode;
    @NonNull
    private String versionName;
    @NonNull
    private String sdk;
    @NonNull
    private String sdkIncremental;
    @NonNull
    private String baseOs;
    @NonNull
    private String device;
    @NonNull
    private String model;
    @NonNull
    private String brand;
    @NonNull
    private String manufacturer;
    @NonNull
    private String bootloader;

    public DeviceInfo() {
        this.versionCode = Integer.toString(BuildConfig.VERSION_CODE);
        this.versionName = BuildConfig.VERSION_NAME;
        this.sdk = Integer.toString(Build.VERSION.SDK_INT);
        this.sdkIncremental = Build.VERSION.INCREMENTAL;
        this.device = Build.DEVICE;
        this.model = Build.MODEL;
        this.brand = Build.BRAND;
        this.manufacturer = Build.MANUFACTURER;
        this.bootloader = Build.BOOTLOADER;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.baseOs = Build.VERSION.BASE_OS;
    }

    @NonNull
    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(@NonNull String versionCode) {
        this.versionCode = versionCode;
    }

    @NonNull
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(@NonNull String versionName) {
        this.versionName = versionName;
    }

    @NonNull
    public String getSdk() {
        return sdk;
    }

    public void setSdk(@NonNull String sdk) {
        this.sdk = sdk;
    }

    @NonNull
    public String getSdkIncremental() {
        return sdkIncremental;
    }

    public void setSdkIncremental(@NonNull String sdkIncremental) {
        this.sdkIncremental = sdkIncremental;
    }

    @NonNull
    public String getBaseOs() {
        return baseOs;
    }

    public void setBaseOs(@NonNull String baseOs) {
        this.baseOs = baseOs;
    }

    @NonNull
    public String getDevice() {
        return device;
    }

    public void setDevice(@NonNull String device) {
        this.device = device;
    }

    @NonNull
    public String getModel() {
        return model;
    }

    public void setModel(@NonNull String model) {
        this.model = model;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    public void setBrand(@NonNull String brand) {
        this.brand = brand;
    }

    @NonNull
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(@NonNull String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @NonNull
    public String getBootloader() {
        return bootloader;
    }

    public void setBootloader(@NonNull String bootloader) {
        this.bootloader = bootloader;
    }
}
