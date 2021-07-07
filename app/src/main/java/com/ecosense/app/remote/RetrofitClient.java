package com.ecosense.app.remote;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * <h1>Class to get instance of Retrofit using Singleton Pattern</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 2.0
 */
public class RetrofitClient {

    /**
     * Instance variable to store the {@link Retrofit object}.
     */
    private static Retrofit instance = null;

    /**
     * Method to get {@link RetrofitClient#instance} instance.<br>
     * If the instance is not initialized, new instance is created in returned else the already
     * created instance is returned.
     *
     * @param baseUrl Base url of the rest endpoint to make connection.
     * @return {@link RetrofitClient#instance} instance
     */
    public Retrofit getClient(@NonNull final String baseUrl) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                    interceptor.level(HttpLoggingInterceptor.Level.BODY);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(interceptor)
                            .build();

                    instance = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return instance;
    }
}
