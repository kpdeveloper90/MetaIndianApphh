package com.ecosense.app.helper.alertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.mvivekanandji.validatingtextinputlayout.TextInputLayoutValidator;
import com.mvivekanandji.validatingtextinputlayout.ValidatingTextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ecosense.app.R;
import com.ecosense.app.databinding.LayoutDialogStartRouteBinding;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.LocationHelper;
import com.ecosense.app.helper.PermissionHelper;
import com.ecosense.app.pojo.LiveVehicle;
import com.ecosense.app.pojo.model.Ward;
import com.ecosense.app.pojo.model.Zone;
import com.ecosense.app.pojo.response.LiveVehicleResponse;
import com.ecosense.app.pojo.response.WardPage;
import com.ecosense.app.pojo.response.WardResponse;
import com.ecosense.app.pojo.response.ZonePage;
import com.ecosense.app.pojo.response.ZoneResponse;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Helper class for showing the route plotting start dialog.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class StartPlottingDialog extends AlertDialogHelper
        implements TextInputLayoutValidator.ValidatorListener,
        BackEndRequestCall.BackendRequestListener {

    public interface OnStartPlottingDialogListener {
        void onStartButtonClicked(AlertDialogHelper dialogInstance);

        void onCountdownComplete();

        default void onCancelButtonClicked(AlertDialogHelper dialogInstance) {
            dialogInstance.dismiss();
        }
    }

    private static final String VEHICLES_API_TAG = "vehicles";
    private static final String WARDS_API_TAG = "wards";
    private static final String ZONES_API_TAG = "zones";

    private LayoutDialogStartRouteBinding dialogBinding;
    private final OnStartPlottingDialogListener dialogListener;
    private TextInputLayoutValidator inputLayoutValidator;
    private List<Zone> zoneList;
    private List<Ward> wardList;
    private List<LiveVehicle> liveVehicleList;
    private List<String> zoneNameList;
    private List<String> wardNumberList;
    private List<String> vehicleNumberList;
    //    private ArrayAdapter<String> zoneNumberAdapter;
//    private ArrayAdapter<String> wardNumberAdapter;
//    private ArrayAdapter<String> vehicleNumberAdapter;
    private PermissionHelper permissionHelper;
    private String zoneNumber;
    private String wardNumber;
    private String vehicleNumber;
    private String routeName;
    private Analytics analytics;

    public StartPlottingDialog(@NonNull final Activity activity,
                               @NonNull final OnStartPlottingDialogListener dialogListener) {
        super(activity);
        this.dialogListener = dialogListener;
        initConfigs();
    }

    private void initConfigs() {
        permissionHelper = new PermissionHelper(activity);
        analytics = Analytics.getInstance();
        zoneList = new ArrayList<>();
        wardList = new ArrayList<>();
        liveVehicleList = new ArrayList<>();
        zoneNameList = new ArrayList<>();
        wardNumberList = new ArrayList<>();
        vehicleNumberList = new ArrayList<>();

//        getZones(false);
//        getWards(false);
//        getLiveVehicles(false);
    }

    @Override
    public void show() {
        dialogBinding = LayoutDialogStartRouteBinding.inflate(activity.getLayoutInflater());
        initViews();
        initListeners();
        createCustomAlertDialog(dialogBinding.getRoot(), false);
    }

    private boolean isDataFetched(List<?> list, TextInputLayout textInputLayout) {
        if (list == null || list.isEmpty()) {
            textInputLayout.setError(activity.getString(R.string.dialog_fetching_data_title));
            textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.danger)));
            return false;
        }
        return true;
    }

    private void initViews() {
        inputLayoutValidator = new TextInputLayoutValidator(dialogBinding.getRoot(), this);

        if (isDataFetched(zoneList, dialogBinding.ilZoneName))
            setAdapter(Zone.getZoneNameList(zoneList), dialogBinding.ilZoneName);

        if (isDataFetched(wardList, dialogBinding.ilWardNumber))
            setAdapter(Ward.getWardNumberList(wardList), dialogBinding.ilWardNumber);

        if (isDataFetched(vehicleNumberList, dialogBinding.ilVehicleNumber))
            setAdapter(vehicleNumberList, dialogBinding.ilVehicleNumber);

        getZones(true);
        getWards(true);
        getLiveVehicles(true);

//        if (!TextUtils.isEmpty(zoneNumber)) {
//            dialogBinding.etZoneNumber.setText(zoneNumber, false);
//        }
//
//        if (!TextUtils.isEmpty(wardNumber))
//            dialogBinding.etWardNumber.setText(wardNumber, false);

        if (!TextUtils.isEmpty(vehicleNumber))
            dialogBinding.etVehicleNumber.setText(vehicleNumber, false);

        if (!TextUtils.isEmpty(routeName))
            dialogBinding.etRouteName.setText(routeName);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void initListeners() {
        dialogBinding.btStart.setOnClickListener(v -> {
            hideKeyboard(dialogBinding.getRoot().getFocusedChild());

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            String zoneId = "null";
            String wardId = "null";
            String vehicleId = "null";
            String selectedZoneName = dialogBinding.etZoneName.getText().toString();
            String selectedWardNumber = dialogBinding.etWardNumber.getText().toString();
            String selectedVehicleNumber = dialogBinding.etVehicleNumber.getText().toString();

            for (Zone zone : zoneList)
                if (zone != null && zone.getName().equalsIgnoreCase(selectedZoneName)) {
                    zoneId = zone.getId();
                    break;
                }

            for (Ward ward : wardList)
                if (ward != null && ward.getNumber().equalsIgnoreCase(selectedWardNumber)) {
                    wardId = ward.getId();
                    break;
                }

            for (LiveVehicle liveVehicle : liveVehicleList)
                if (liveVehicle != null && liveVehicle.getVehicleNumber().equalsIgnoreCase(selectedVehicleNumber)) {
                    vehicleId = liveVehicle.getId();
                    break;
                }

            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_CLIENT_ROUTE_ID,
                    UUID.randomUUID().toString()).apply();
            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_ROUTE_NAME,
                    dialogBinding.etRouteName.getText().toString()).apply();

            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_ZONE_ID, zoneId).apply();
            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_ZONE_NUMBER, selectedZoneName).apply();

            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_WARD_ID, wardId).apply();
            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_WARD_NUMBER, selectedWardNumber).apply();

            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_VEHICLE_ID, vehicleId).apply();
            sharedPreferences.edit().putString(AppConfig.KEY_CURRENT_VEHICLE_NUMBER, selectedVehicleNumber).apply();

            permissionHelper.checkAndRequestLocationPermission(permissionName -> {
                if (LocationHelper.isGpsLocationEnabled(activity, permissionHelper)) { //checks as well force the user the switch on the gps

                    if (dialogListener != null)
                        dialogListener.onStartButtonClicked(this);

                    dialogBinding.groupStartInfo.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), new Slide());
                    dialogBinding.tvTitle.setText(activity.getString(R.string.dialog_plotting_starting_in_title));
                    dialogBinding.tvCounter.setVisibility(View.VISIBLE);

                    startCounterAnimation();
                }
            });
        });

        dialogBinding.btNo.setOnClickListener(v -> {
            if (dialogListener != null)
                dialogListener.onCancelButtonClicked(this);

            zoneNumber = dialogBinding.etZoneName.getText().toString();
            wardNumber = dialogBinding.etWardNumber.getText().toString();
            vehicleNumber = dialogBinding.etVehicleNumber.getText().toString();
            routeName = dialogBinding.etRouteName.getText().toString();
        });

        dialogBinding.etZoneName.setOnItemClickListener((parent, view, position, id) -> {
            dialogBinding.ilWardNumber.setError(activity.getString(R.string.dialog_fetching_data_title));
            dialogBinding.ilWardNumber.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.danger)));

            String selectedZoneId = zoneList.get(position).getId();
            List<String> filteredWard = new ArrayList<>();

            for (Ward ward : wardList) {
                try {
                    JSONObject jsonObject = new JSONObject(ward.getZoneId().toString());
                    Log.e("initListeners: ", jsonObject.getString("_id") + "     " + selectedZoneId);
                    if (jsonObject.getString("_id").equals(selectedZoneId) && ward.getNumber() != null)
                        filteredWard.add(ward.getNumber());
                } catch (Exception e) {
                    try {
                        if (ward.getZoneId().toString().equals(selectedZoneId) && ward.getNumber() != null)
                            filteredWard.add(ward.getNumber());
                        e.printStackTrace();
                    } catch (Exception eb) {
                    }
                }

            }

            setAdapter(filteredWard, dialogBinding.ilWardNumber);

            dialogBinding.etWardNumber.setText(null, false);
        });
    }

    private void startCounterAnimation() {
        final int[] count = {5};
        Animation anim = new AlphaAnimation(0.0f, 1.0f);

        anim.setDuration(1000);
        anim.setStartOffset(0);
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(6);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onAnimationRepeat(Animation animation) {
                if (--count[0] > 0)
                    dialogBinding.tvCounter.setText(Integer.toString(count[0]));
                else {
                    dismiss();
                    if (dialogListener != null)
                        dialogListener.onCountdownComplete();
                }
            }
        });

        dialogBinding.tvCounter.startAnimation(anim);
    }

    @SuppressWarnings("unchecked")
    private void setAdapter(@NonNull final List<String> list, TextInputLayout textInputLayout) {
        if (list != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>
                    (activity, android.R.layout.simple_spinner_dropdown_item, list);

            ((MaterialAutoCompleteTextView) textInputLayout.getEditText()).setAdapter(adapter);
            textInputLayout.setError(null);
            textInputLayout.setErrorEnabled(false);
            textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimaryDark)));
        }
    }

    private void getZones(final boolean isVisible) {
        BackEndRequestCall.enqueue(RetrofitHelper.getZoneList(), ZONES_API_TAG, new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                ZonePage zonePage = ((ZoneResponse) responseBody).getZonePage();
                int totalPages = Integer.parseInt(zonePage.getTotalPages());
                List<Zone> tempZoneList = new ArrayList<>(((ZoneResponse) responseBody).getZoneList());

                if (totalPages == 1) {
                    if (isVisible && (zoneList.isEmpty() || !zoneList.equals(tempZoneList))) {
                        zoneList.clear();
                        zoneList.addAll(tempZoneList);
                        setAdapter(Zone.getZoneNameList(zoneList), dialogBinding.ilZoneName);
                    }
                    return;
                }

                for (int i = 1; i < totalPages; i++) {
                    int finalI = i;
                    BackEndRequestCall.enqueue(RetrofitHelper.getZoneList(i), ZONES_API_TAG, new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            tempZoneList.addAll(((ZoneResponse) responseBody).getZoneList());

                            if (finalI == totalPages - 1) {
                                if (isVisible && !zoneList.equals(tempZoneList)) {
                                    zoneList.clear();
                                    zoneList.addAll(tempZoneList);
                                    setAdapter(Zone.getZoneNameList(zoneList), dialogBinding.ilZoneName);
                                }
                            }
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            if (isVisible)
                                updateErrorMessage(getRelevantTextInputLayout(tag), backendError);
                            zoneList.clear();
                        }
                    });
                }
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                if (isVisible)
                    updateErrorMessage(getRelevantTextInputLayout(tag), backendError);
            }
        });
    }

    private void getWards(final boolean isVisible) {
        BackEndRequestCall.enqueue(RetrofitHelper.getWardList(), WARDS_API_TAG, new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                WardPage wardPage = ((WardResponse) responseBody).getWardPage();

                int totalPages = Integer.parseInt(wardPage.getTotalPages());
                List<Ward> tempWardList = new ArrayList<>(((WardResponse) responseBody).getWardList());

                if (totalPages == 1) {
                    if (isVisible && (wardList.isEmpty() || !wardList.equals(tempWardList))) {
                        wardList.clear();
                        wardList.addAll(tempWardList);
                        setAdapter(Ward.getWardNumberList(wardList), dialogBinding.ilWardNumber);
                    }
                    return;
                }

                for (int i = 1; i < totalPages; i++) {
                    int finalI = i;
                    BackEndRequestCall.enqueue(RetrofitHelper.getWardList(i), WARDS_API_TAG, new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            tempWardList.addAll(((WardResponse) responseBody).getWardList());

                            if (finalI == totalPages - 1) {
                                if (isVisible && !wardList.equals(tempWardList)) {
                                    wardList.clear();
                                    wardList.addAll(tempWardList);
                                    setAdapter(Ward.getWardNumberList(wardList), dialogBinding.ilWardNumber);
                                }
                            }
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            if (isVisible)
                                updateErrorMessage(getRelevantTextInputLayout(tag), backendError);
                            wardList.clear();
                        }
                    });
                }
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                if (isVisible)
                    updateErrorMessage(getRelevantTextInputLayout(tag), backendError);
                wardList.clear();
            }
        });
    }

    private void getLiveVehicles(final boolean isVisible) {
        BackEndRequestCall.enqueue(RetrofitHelper.getLiveVehicleList(), VEHICLES_API_TAG, new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {

                LiveVehicleResponse liveVehicleResponse = (LiveVehicleResponse) responseBody;

                if (!liveVehicleResponse.getLiveVehicleList().equals(liveVehicleList)) {
                    liveVehicleList = liveVehicleResponse.getLiveVehicleList();
                    vehicleNumberList = liveVehicleResponse.getVehicleNumberList();

                    if (isVisible)
                        setAdapter(vehicleNumberList, dialogBinding.ilVehicleNumber);
                }
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                if (isVisible)
                    updateErrorMessage(getRelevantTextInputLayout(tag), backendError);
            }
        });
    }

    private TextInputLayout getRelevantTextInputLayout(@NonNull final String tag) {
        TextInputLayout textInputLayout = null;
        switch (tag) {
            case VEHICLES_API_TAG:
                textInputLayout = dialogBinding.ilVehicleNumber;
                break;
            case WARDS_API_TAG:
                textInputLayout = dialogBinding.ilWardNumber;
                break;
            case ZONES_API_TAG:
                textInputLayout = dialogBinding.ilZoneName;
                break;
        }
        return textInputLayout;
    }

    private String getRelevantErrorMessage(@NonNull final BackendError backendError) {
        String errorMessage = null;
        switch (backendError) {
            case UNSUCCESSFUL:
                errorMessage = activity.getString(R.string.dialog_problem_server_down);
                break;
            case PARSING:
                errorMessage = activity.getString(R.string.dialog_problem_parsing_error);
                break;
            case NETWORK:
                errorMessage = activity.getString(R.string.dialog_problem_network_failure);
                break;
        }
        return errorMessage;
    }

    private void updateErrorMessage(@NonNull final TextInputLayout textInputLayout, @NonNull BackendError backendError) {
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError(getRelevantErrorMessage(backendError));
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.danger)));
    }

    @Override
    public void onSuccess(String tag, @NonNull Object responseBody) {
        TextInputLayout textInputLayout = getRelevantTextInputLayout(tag);
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimaryDark)));
    }

    @Override
    public void onError(String tag, @NonNull BackendError backendError) {
        TextInputLayout textInputLayout = getRelevantTextInputLayout(tag);
        updateErrorMessage(textInputLayout, backendError);
    }

    @Override
    public void onValidateErrors(List<ValidatingTextInputLayout> errorLayoutList, List<TextInputLayoutValidator.ValidationError> validationErrorList) {
    }

    @Override
    public void onError(ValidatingTextInputLayout inputLayout, TextInputLayoutValidator.ValidationError validationError, boolean isErrorOnValidate) {
        dialogBinding.btStart.setEnabled(false);
    }

    @Override
    public void onErrorResolved(ValidatingTextInputLayout inputLayout) {

    }

    @Override
    public void onSuccess() {
        dialogBinding.btStart.setEnabled(true);
    }
}
