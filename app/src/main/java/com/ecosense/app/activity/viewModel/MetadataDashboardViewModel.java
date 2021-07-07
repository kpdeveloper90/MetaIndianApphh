package com.ecosense.app.activity.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecosense.app.activity.metaData.MetaDataDashBoard;

/**
 * <h1>{@link ViewModel} class for the {@link MetaDataDashBoard}</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class MetadataDashboardViewModel extends ViewModel {
    private String vehicleNumber;
    private String routeName;

    private MutableLiveData<String> userName;

    public MutableLiveData<String> getUserName() {
        if (userName == null) {
            userName = new MutableLiveData<String>();
        }
        return userName;
    }
}
