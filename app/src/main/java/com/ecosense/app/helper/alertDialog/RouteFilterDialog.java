package com.ecosense.app.helper.alertDialog;

import android.app.Activity;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ecosense.app.R;
import com.ecosense.app.adapter.RouteFilterAdapter;
import com.ecosense.app.databinding.LayoutDialogRouteFilterBinding;
import com.ecosense.app.databinding.LayoutFilterCardBinding;
import com.ecosense.app.db.models.MetaRouteInfo;
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
 * <h1>Helper class for showing route filter dialog.</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteFilterDialog extends AlertDialogHelper {

    public interface RouteFilterDialogClickListener {
        void onApplyClicked(@NonNull final List<String> selectedZones,
                            @NonNull final List<String> selectedWards,
                            @NonNull final List<String> selectedVehicles,
                            @NonNull final List<String> selectedDates,
                            @NonNull final List<String> selectedUsers,
                            @NonNull final List<String> selectedProjects);

        default void onCancelClicked() {
        }
    }

    private static final int FILTER_ZONE = 0;
    private static final int FILTER_WARD = 1;
    private static final int FILTER_VEHICLE = 2;
    private static final int FILTER_DATE = 3;
    private static final int FILTER_USER = 4;
    private static final int FILTER_PROJECT = 6;

    private LayoutDialogRouteFilterBinding dialogBinding;
    private final RouteFilterDialogClickListener listener;
    private final List<Zone> allZoneList;
    private final List<Ward> allWardList;
    private List<LiveVehicle> allVehicleList;
    private final Set<String> availableZoneSet;
    private final Set<String> availableWardSet;
    private final Set<String> availableVehicleSet;
    private final Set<String> availableDateSet;
    private final Set<String> availableUserSet;
    private final Set<String> availableProjectSet;
    private List<MetaRouteInfo> metaRouteInfoList;
    private List<String> selectedZones;
    private List<String> selectedWards;
    private List<String> selectedVehicles;
    private List<String> selectedDates;
    private List<String> selectedUsers;
    private List<String> selectedProjects;
    private final Transition showIndividualFiltersTransition;
    private final Transition hideIndividualFiltersTransition;
    private int currentFilter;
    private boolean isInitialized;

    public RouteFilterDialog(@NonNull final Activity activity, @NonNull final List<MetaRouteInfo> metaRouteInfoList,
                             @NonNull final RouteFilterDialogClickListener listener) {
        super(activity);
        this.listener = listener;

        allZoneList = new ArrayList<>();
        allWardList = new ArrayList<>();
        allVehicleList = new ArrayList<>();

        availableZoneSet = new HashSet<>();
        availableWardSet = new HashSet<>();
        availableVehicleSet = new HashSet<>();
        availableDateSet = new HashSet<>();
        availableUserSet = new HashSet<>();
        availableProjectSet = new HashSet<>();

        if (metaRouteInfoList != null && !metaRouteInfoList.isEmpty()) {
            this.metaRouteInfoList = metaRouteInfoList;

            for (MetaRouteInfo routeInfo : metaRouteInfoList) {
                if (routeInfo.getZoneName() != null)
                    availableZoneSet.add(routeInfo.getZoneName());

                if (routeInfo.getWardNumber() != null)
                    availableWardSet.add(routeInfo.getWardNumber());

                if (routeInfo.getVehicleNumber() !=null)
                    availableVehicleSet.add(routeInfo.getVehicleNumber());

                if (routeInfo.getUpdatedAt() != null)
                    availableDateSet.add(routeInfo.getUpdatedAt().replace('T', ',').substring(0, routeInfo.getUpdatedAt().length() - 5));

                if (routeInfo.getUserId() != null)
                    availableUserSet.add(routeInfo.getUserId());

                if (routeInfo.getProjectId() != null)
                    availableProjectSet.add(routeInfo.getProjectId());
            }
        }

//        List<String> tempDateList = new ArrayList<>(availableDateSet);
//        availableDateSet.clear();
//
//        Collections.sort(tempDateList, new Comparator<String>() {
//            @SuppressLint("SimpleDateFormat")
//            final DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
//
//            @Override
//            public int compare(String o1, String o2) {
//                try {
//                    return dateFormat.parse(o1).compareTo(dateFormat.parse(o2));
//                } catch (ParseException e) {
//                    throw new IllegalArgumentException(e);
//                }
//            }
//        });
//
//
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
//
//        for (String date : tempDateList)
//            availableDateSet.add(formatter.format(new Date(date)));


        showIndividualFiltersTransition = new Slide(Gravity.TOP);
        hideIndividualFiltersTransition = new Slide(Gravity.BOTTOM);
        isInitialized = false;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(activity, "back", Toast.LENGTH_SHORT).show();
        if (dialogBinding.includedIndividualFilter.getRoot().getVisibility() == View.VISIBLE)
            hideIndividualFilters();
    }

    @Override
    public void show() {
        isInitialized = true;
        dialogBinding = LayoutDialogRouteFilterBinding.inflate(activity.getLayoutInflater());
        initViews();
        initListeners();
        createCustomAlertDialog(dialogBinding.getRoot(), false);
    }

    public void setSelectedOptions(@NonNull List<String> selectedZones,
                                   @NonNull List<String> selectedWards,
                                   @NonNull List<String> selectedVehicles,
                                   @NonNull List<String> selectedDates,
                                   @NonNull List<String> selectedUsers,
                                   @NonNull List<String> selectedProjects) {
        this.selectedZones = selectedZones;
        this.selectedWards = selectedWards;
        this.selectedVehicles = selectedVehicles;
        this.selectedDates = selectedDates;
        this.selectedUsers = selectedUsers;
        this.selectedProjects = selectedProjects;
    }

    private void initViews() {
        determineFilteringAvailability(availableZoneSet, dialogBinding.includedZoneFilter.btAdd);
        determineFilteringAvailability(availableWardSet, dialogBinding.includedWardFilter.btAdd);
        determineFilteringAvailability(availableVehicleSet, dialogBinding.includedVehicleFilter.btAdd);
        determineFilteringAvailability(availableDateSet, dialogBinding.includedDateFilter.btAdd);
        determineFilteringAvailability(availableUserSet, dialogBinding.includedUserFilter.btAdd);
        determineFilteringAvailability(availableProjectSet, dialogBinding.includedProjectFilter.btAdd);

        initializeSelectedChips(selectedZones, FILTER_ZONE);
        initializeSelectedChips(selectedWards, FILTER_WARD);
        initializeSelectedChips(selectedVehicles, FILTER_VEHICLE);
        initializeSelectedChips(selectedDates, FILTER_DATE);
        initializeSelectedChips(selectedUsers, FILTER_USER);
        initializeSelectedChips(selectedProjects, FILTER_PROJECT);

        dialogBinding.includedWardFilter.tvTitle.setText(activity.getString(R.string.tv_ward_filter));
        dialogBinding.includedVehicleFilter.tvTitle.setText(activity.getString(R.string.tv_vehicle_filter));
        dialogBinding.includedDateFilter.tvTitle.setText(activity.getString(R.string.tv_date_filter));
        dialogBinding.includedUserFilter.tvTitle.setText(activity.getString(R.string.tv_user_filter));
        dialogBinding.includedProjectFilter.tvTitle.setText(activity.getString(R.string.tv_project_filter));

        dialogBinding.includedWardFilter.tvNoFilter.setText(activity.getString(R.string.tv_no_ward_filter));
        dialogBinding.includedVehicleFilter.tvNoFilter.setText(activity.getString(R.string.tv_no_vehicle_filter));
        dialogBinding.includedDateFilter.tvNoFilter.setText(activity.getString(R.string.tv_no_date_filter));
        dialogBinding.includedUserFilter.tvNoFilter.setText(activity.getString(R.string.tv_no_user_filter));
        dialogBinding.includedProjectFilter.tvNoFilter.setText(activity.getString(R.string.tv_no_project_filter));

        if (!dialogBinding.includedZoneFilter.btAdd.getText().toString().equals(activity.getString(R.string.tv_no_data_to_filter))
                && isDataNotFetched(allZoneList, dialogBinding.includedZoneFilter.btAdd))
            getZones();

        if (!dialogBinding.includedZoneFilter.btAdd.getText().toString().equals(activity.getString(R.string.tv_no_data_to_filter))
                && isDataNotFetched(allWardList, dialogBinding.includedZoneFilter.btAdd))
            getWards();

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        dialogBinding.includedIndividualFilter.rvOptions.setLayoutManager(layoutManager);
        dialogBinding.includedIndividualFilter.rvOptions.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void initListeners() {
        dialogBinding.btCancel.setOnClickListener(v -> {
            if (listener != null)
                listener.onCancelClicked();

            dismiss();
        });

        dialogBinding.includedIndividualFilter.btDone.setOnClickListener(v -> {
            hideIndividualFilters();
        });

        dialogBinding.btApply.setOnClickListener(v -> {
            if (listener != null)
                listener.onApplyClicked(selectedZones, selectedWards, selectedVehicles,
                        selectedDates, selectedUsers, selectedProjects);

            dismiss();
        });

        dialogBinding.includedZoneFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_ZONE;
            showIndividualFilters(activity.getString(R.string.tv_zone_filter), availableZoneSet, selectedZones, true);
        });

        dialogBinding.includedWardFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_WARD;
            showIndividualFilters(activity.getString(R.string.tv_ward_filter), availableWardSet, selectedWards, true);
        });

        dialogBinding.includedVehicleFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_VEHICLE;
            showIndividualFilters(activity.getString(R.string.tv_vehicle_filter), availableVehicleSet, selectedVehicles, true);
        });

        dialogBinding.includedDateFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_DATE;
            showIndividualFilters(activity.getString(R.string.tv_date_filter), availableDateSet, selectedDates, true);
        });

        dialogBinding.includedUserFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_USER;
            showIndividualFilters(activity.getString(R.string.tv_user_filter), availableUserSet, selectedUsers, true);
        });

        dialogBinding.includedProjectFilter.btAdd.setOnClickListener(v -> {
            currentFilter = FILTER_PROJECT;
            showIndividualFilters(activity.getString(R.string.tv_project_filter), availableProjectSet, selectedProjects, true);
        });
    }

    private void addFilterChip(@NonNull final LayoutFilterCardBinding binding, @NonNull final String text, final int tag) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_filter_chip, binding.cgFilters, false);
        chip.setText(text);
        chip.setTag(tag);

        chip.setOnCloseIconClickListener(v -> {
            binding.cgFilters.removeView(chip);
            removeFromSelectedList((Integer) chip.getTag(), chip.getText().toString());
            determineMessageVisibility(binding);
        });

        binding.cgFilters.addView(chip);
        determineMessageVisibility(binding);
    }

    private void removeChipAndFromList(int filterMode, @NonNull final String option) {
        LayoutFilterCardBinding binding;
        ;
        switch (filterMode) {
            case FILTER_ZONE:
                binding = dialogBinding.includedZoneFilter;
                selectedZones.remove(option);
                break;
            case FILTER_WARD:
                binding = dialogBinding.includedWardFilter;
                selectedWards.remove(option);
                break;
            case FILTER_VEHICLE:
                binding = dialogBinding.includedVehicleFilter;
                selectedVehicles.remove(option);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + filterMode);
        }

        for (int i = 0; i < binding.cgFilters.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgFilters.getChildAt(i);

            if (chip.getText().toString().equals(option)) {
                binding.cgFilters.removeViewAt(i);
                break;
            }
        }

        determineMessageVisibility(binding);
    }

    private void removeFromSelectedList(int filterMode, @NonNull final String option) {
        switch (filterMode) {
            case FILTER_ZONE:
                selectedZones.remove(option);
                break;
            case FILTER_WARD:
                selectedWards.remove(option);
                break;
            case FILTER_VEHICLE:
                selectedVehicles.remove(option);
            case FILTER_DATE:
                selectedDates.remove(option);
            case FILTER_USER:
                selectedUsers.remove(option);
            case FILTER_PROJECT:
                selectedProjects.remove(option);
                break;
        }
    }

    private void showIndividualFilters(@NonNull final String title, @NonNull final Set<String> options,
                                       @NonNull final List<String> selectedOptions, final boolean sortData) {
        dialogBinding.groupAllFilters.setVisibility(View.GONE);

        dialogBinding.includedIndividualFilter.rvOptions.setAdapter(new RouteFilterAdapter(new ArrayList<>(options), selectedOptions,
                new RouteFilterAdapter.RouteFilterAdapterListener() {
                    @Override
                    public void onChecked(@NonNull String option) {
                        switch (currentFilter) {
                            case FILTER_ZONE:
                                selectedZones.add(option);
                                addFilterChip(dialogBinding.includedZoneFilter, option, FILTER_ZONE);
                                break;
                            case FILTER_WARD:
                                selectedWards.add(option);
                                addFilterChip(dialogBinding.includedWardFilter, option, FILTER_WARD);
                                break;
                            case FILTER_VEHICLE:
                                selectedVehicles.add(option);
                                addFilterChip(dialogBinding.includedVehicleFilter, option, FILTER_VEHICLE);
                                break;
                            case FILTER_DATE:
                                selectedDates.add(option);
                                addFilterChip(dialogBinding.includedDateFilter, option, FILTER_DATE);
                                break;
                            case FILTER_USER:
                                selectedUsers.add(option);
                                addFilterChip(dialogBinding.includedUserFilter, option, FILTER_USER);
                                break;
                            case FILTER_PROJECT:
                                selectedProjects.add(option);
                                addFilterChip(dialogBinding.includedProjectFilter, option, FILTER_PROJECT);
                                break;
                        }
                    }

                    @Override
                    public void onUnChecked(@NonNull String option) {
                        removeChipAndFromList(currentFilter, option);
                    }
                }, sortData));

        TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), showIndividualFiltersTransition);
        dialogBinding.includedIndividualFilter.getRoot().setVisibility(View.VISIBLE);
        dialogBinding.includedIndividualFilter.tvTitle.setText(title);
    }

    private void hideIndividualFilters() {
        dialogBinding.includedIndividualFilter.getRoot().setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(dialogBinding.getRoot(), hideIndividualFiltersTransition);
        dialogBinding.groupAllFilters.setVisibility(View.VISIBLE);
    }

    private void initializeSelectedChips(List<String> selectedOptionList, final int filterMode) {
        LayoutFilterCardBinding binding;
        switch (filterMode) {
            case FILTER_ZONE:
                binding = dialogBinding.includedZoneFilter;
                break;
            case FILTER_WARD:
                binding = dialogBinding.includedWardFilter;
                break;
            case FILTER_VEHICLE:
                binding = dialogBinding.includedVehicleFilter;
                break;
            case FILTER_DATE:
                binding = dialogBinding.includedDateFilter;
                break;
            case FILTER_USER:
                binding = dialogBinding.includedUserFilter;
                break;
            case FILTER_PROJECT:
                binding = dialogBinding.includedProjectFilter;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + filterMode);
        }

        for (String option : selectedOptionList)
            addFilterChip(binding, option, filterMode);
    }

    private void determineFilteringAvailability(Set<String> list, @NonNull final MaterialButton button) {
        if (list == null || list.isEmpty()) {
            button.setEnabled(false);
            button.setText(activity.getString(R.string.tv_no_data_to_filter));
        }
    }

    private boolean isDataNotFetched(List<?> list, @NonNull final MaterialButton button) {
        if (list == null || list.isEmpty()) {
            showFetchingData(button);
            return true;
        }
        return false;
    }

    private void showDataAvailable(@NonNull final MaterialButton button) {
        showButtonStatus(button, activity.getString(R.string.button_add_filter), true);
    }

    private void showFetchingData(@NonNull final MaterialButton button) {
        showButtonStatus(button, activity.getString(R.string.dialog_backend_title), false);
    }

    private void showDataFetchFailed(@NonNull final MaterialButton button) {
        showButtonStatus(button, activity.getString(R.string.dialog_backend_failed_title), false);
    }

    private void showButtonStatus(@NonNull final MaterialButton button, @NonNull final String text, final boolean isEnabled) {
        button.setEnabled(isEnabled);
        button.setText(text);
    }

    private void determineMessageVisibility(@NonNull final LayoutFilterCardBinding binding) {
        if (binding.cgFilters.getChildCount() > 0)
            binding.tvNoFilter.setVisibility(View.INVISIBLE);
        else
            binding.tvNoFilter.setVisibility(View.VISIBLE);
    }

    private void getZones() {
        BackEndRequestCall.enqueue(RetrofitHelper.getZoneList(), "getZones", new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                ZonePage zonePage = ((ZoneResponse) responseBody).getZonePage();
                int totalPages = Integer.parseInt(zonePage.getTotalPages());
                List<Zone> tempZoneList = new ArrayList<>(((ZoneResponse) responseBody).getZoneList());

                if (totalPages == 1) {
                    allZoneList.addAll(tempZoneList);
                    showDataAvailable(dialogBinding.includedZoneFilter.btAdd);
                    return;
                }

                for (int i = 1; i < totalPages; i++) {
                    int finalI = i;
                    BackEndRequestCall.enqueue(RetrofitHelper.getZoneList(i), "getZones", new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            tempZoneList.addAll(((ZoneResponse) responseBody).getZoneList());

                            if (finalI == totalPages - 1) {
                                allZoneList.addAll(tempZoneList);
                                showDataAvailable(dialogBinding.includedZoneFilter.btAdd);
                            }
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            allZoneList.clear();
                            showDataFetchFailed(dialogBinding.includedZoneFilter.btAdd);
                        }
                    });
                }
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                showDataFetchFailed(dialogBinding.includedZoneFilter.btAdd);
            }
        });
    }

    private void getWards() {
        BackEndRequestCall.enqueue(RetrofitHelper.getWardList(), "getWards", new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                WardPage wardPage = ((WardResponse) responseBody).getWardPage();
                
                int totalPages = Integer.parseInt(wardPage.getTotalPages());
                List<Ward> tempWardList = new ArrayList<>(((WardResponse) responseBody).getWardList());

                if (totalPages == 1) {
                    allWardList.addAll(tempWardList);
                    showDataAvailable(dialogBinding.includedWardFilter.btAdd);
                    return;
                }

                for (int i = 1; i < totalPages; i++) {
                    int finalI = i;
                    BackEndRequestCall.enqueue(RetrofitHelper.getWardList(i), "getWards", new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            tempWardList.addAll(((WardResponse) responseBody).getWardList());

                            if (finalI == totalPages - 1) {
                                allWardList.addAll(tempWardList);
                                showDataAvailable(dialogBinding.includedWardFilter.btAdd);
                            }
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            allWardList.clear();
                            showDataFetchFailed(dialogBinding.includedWardFilter.btAdd);
                        }
                    });
                }
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                showDataFetchFailed(dialogBinding.includedWardFilter.btAdd);
            }
        });
    }

    private void getVehicles() {
        BackEndRequestCall.enqueue(RetrofitHelper.getLiveVehicleList(), "getVehicles", new BackEndRequestCall.BackendRequestListener() {
            @Override
            public void onSuccess(String tag, @NonNull Object responseBody) {
                allVehicleList = ((LiveVehicleResponse) responseBody).getLiveVehicleList();

                for (MetaRouteInfo routeInfo : metaRouteInfoList) {
                    for (LiveVehicle liveVehicle : allVehicleList) {
                        if (routeInfo.getVehicleId() != null && routeInfo.getVehicleId().equals(liveVehicle.getId())) {
                            availableVehicleSet.add(liveVehicle.getVehicleNumber());
                            break;
                        }
                    }
                }

                if (!dialogBinding.includedZoneFilter.btAdd.getText().toString().equals(activity.getString(R.string.tv_no_data_to_filter)))
                    showDataAvailable(dialogBinding.includedVehicleFilter.btAdd);
            }

            @Override
            public void onError(String tag, @NonNull BackendError backendError) {
                showDataFetchFailed(dialogBinding.includedVehicleFilter.btAdd);
            }
        });
    }
}
