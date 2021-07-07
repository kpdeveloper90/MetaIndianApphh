package com.ecosense.app.activity.metaData;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ecosense.app.R;
import com.ecosense.app.adapter.RouteListAdapter;
import com.ecosense.app.databinding.ActivityRouteListBinding;
import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.helper.alertDialog.AlertDialogHelper;
import com.ecosense.app.helper.alertDialog.BackendProgressDialog;
import com.ecosense.app.helper.alertDialog.RouteFilterDialog;
import com.ecosense.app.pojo.response.RouteListResponse;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Activity class for the route list of metadata module</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteListActivity extends AppCompatActivity implements RouteFilterDialog.RouteFilterDialogClickListener {

    private ActivityRouteListBinding rootBinding;
    private RouteListAdapter routeListAdapter;
    private BackendProgressDialog backendProgressDialog;
    private RouteFilterDialog routeFilterDialog;
    private List<MetaRouteInfo> originalMetaRouteInfoList;
    private List<String> selectedZones;
    private List<String> selectedWards;
    private List<String> selectedVehicles;
    private List<String> selectedDates;
    private List<String> selectedUsers;
    private List<String> selectedProjects;
    private volatile boolean isFetchingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityRouteListBinding.inflate(getLayoutInflater());
        setContentView(rootBinding.getRoot());

        initConfig();
        initToolbar();
        initListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        setSupportActionBar(rootBinding.includedToolbar.toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Routes");
    }

    private void initConfig() {
        isFetchingData = false;
        selectedZones = new ArrayList<>();
        selectedWards = new ArrayList<>();
        selectedVehicles = new ArrayList<>();
        selectedDates = new ArrayList<>();
        selectedUsers = new ArrayList<>();
        selectedProjects = new ArrayList<>();

        backendProgressDialog = new BackendProgressDialog.Builder(this)
                .setProgressTitle(getString(R.string.dialog_routes_fetching_title))
                .setErrorTitle(getString(R.string.dialog_routes_fetching_failed_title))
                .setProgressDescription(getString(R.string.dialog_routes_fetching_description, getString(R.string.dialog_description_this_may_take_a_while)))
                .setListener(new BackendProgressDialog.BackendProgressDialogListener() {
                    @Override
                    public void onRetryClicked(AlertDialogHelper dialogInstance) {
                        getRoutes();
                    }

                    @Override
                    public void onCancelClicked(AlertDialogHelper dialogInstance) {
                        dialogInstance.dismiss();
                    }
                })
                .build();
        getRoutes();
    }

    private void initListeners() {
        rootBinding.slRoutes.setOnRefreshListener(this::getRoutes);

        rootBinding.btSyncAll.setOnClickListener(v -> TastyToast.makeText(this, getString(R.string.toast_coming_soon), TastyToast.LENGTH_SHORT, TastyToast.INFO));
    }

    private void getRoutes() {
        backendProgressDialog.showProgress();
        if (!isFetchingData) {
            isFetchingData = true;

            BackEndRequestCall.enqueue(RetrofitHelper.getRouteList(), "getRoutes", new BackEndRequestCall.BackendRequestListener() {
                @Override
                public void onSuccess(String tag, @NonNull Object responseBody) {
                    originalMetaRouteInfoList = ((RouteListResponse) responseBody).getMetaRouteInfoList();
                    routeListAdapter = new RouteListAdapter(originalMetaRouteInfoList);

                    routeFilterDialog = new RouteFilterDialog(RouteListActivity.this,
                            originalMetaRouteInfoList, RouteListActivity.this);

                    routeFilterDialog.setSelectedOptions(selectedZones, selectedWards,
                            selectedVehicles, selectedDates,
                            selectedUsers, selectedProjects);

                    hideFilteredInfo();
                    setupList();
                }

                @Override
                public void onError(String tag, @NonNull BackendError backendError) {
                    Pair<String, String> problemSolutionPair = RetrofitHelper.getProblemSolutionPair(backendError);

                    backendProgressDialog.showError(problemSolutionPair.first, problemSolutionPair.second, true);

                    if (rootBinding.slRoutes.isRefreshing())
                        rootBinding.slRoutes.setRefreshing(false);

                    isFetchingData = false;
                }
            });
        } else
            TastyToast.makeText(this, getString(R.string.toast_still_fetching_data), TastyToast.LENGTH_SHORT, TastyToast.INFO);
    }

    private void setupList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        rootBinding.rvRoutes.setLayoutManager(layoutManager);
        rootBinding.rvRoutes.setItemAnimator(new DefaultItemAnimator());
        rootBinding.rvRoutes.setAdapter(routeListAdapter);

        if (routeListAdapter.getItemCount() > 0)
            rootBinding.tvNoItem.setVisibility(View.GONE);
        else rootBinding.tvNoItem.setVisibility(View.VISIBLE);

        if (rootBinding.slRoutes.isRefreshing())
            rootBinding.slRoutes.setRefreshing(false);

        isFetchingData = false;
        backendProgressDialog.dismiss();
    }

    private void showFilterDialog() {
        if (isFetchingData)
            TastyToast.makeText(this, getString(R.string.toast_still_fetching_data), TastyToast.LENGTH_SHORT, TastyToast.INFO);
        else {
            routeFilterDialog.show();
        }
    }

    private void hideFilteredInfo() {
        getSupportActionBar().setSubtitle(null);
        rootBinding.tvFilteredViewTitle.setVisibility(View.GONE);
        rootBinding.tvFilteredViewDescription.setVisibility(View.GONE);
    }

    @Override
    public void onApplyClicked(@NonNull final List<String> selectedZones,
                               @NonNull final List<String> selectedWards,
                               @NonNull final List<String> selectedVehicles,
                               @NonNull final List<String> selectedDates,
                               @NonNull final List<String> selectedUsers,
                               @NonNull final List<String> selectedProjects) {

        if (selectedZones.size() > 0 || selectedWards.size() > 0 || selectedVehicles.size() > 0 ||
                selectedDates.size() > 0 || selectedUsers.size() > 0 || selectedProjects.size() > 0) {

            List<MetaRouteInfo> filteredMetaRouteInfoList = new ArrayList<>();

            for (MetaRouteInfo routeInfo : originalMetaRouteInfoList) {
                boolean found = false;

                for (String zone : selectedZones)
                    if (routeInfo.getZoneName() != null && routeInfo.getZoneName().equals(zone)) {
                        filteredMetaRouteInfoList.add(routeInfo);
                        found = true;
                        break;
                    }

                if (!found) {
                    for (String ward : selectedWards)
                        if (routeInfo.getWardNumber() != null && routeInfo.getWardNumber().equals(ward)) {
                            filteredMetaRouteInfoList.add(routeInfo);
                            found = true;
                            break;
                        }
                }

                if (!found) {
                    for (String vehicle : selectedVehicles)
                        if (routeInfo.getVehicleNumber() != null && routeInfo.getVehicleNumber().equals(vehicle)) {
                            filteredMetaRouteInfoList.add(routeInfo);
                            found = true;
                            break;
                        }
                }
                if (!found) {
                    for (String date : selectedDates)
                        if (routeInfo.getUpdatedAt() != null && routeInfo.getUpdatedAt()
                                .replace('T', ',').substring(0, routeInfo.getUpdatedAt().length() - 5).equals(date)) {
                            filteredMetaRouteInfoList.add(routeInfo);
                            found = true;
                            break;
                        }
                }
                if (!found) {
                    for (String user : selectedUsers)
                        if (routeInfo.getUserId() != null && routeInfo.getUserId().equals(user)) {
                            filteredMetaRouteInfoList.add(routeInfo);
                            found = true;
                            break;
                        }
                }
                if (!found) {
                    for (String project : selectedProjects)
                        if (routeInfo.getUpdatedAt() != null && routeInfo.getProjectId().equals(project)) {
                            filteredMetaRouteInfoList.add(routeInfo);
                            break;
                        }
                }
            }

            routeListAdapter.setMetaRouteInfoList(filteredMetaRouteInfoList);

            getSupportActionBar().setSubtitle(getString(R.string.tv_filtered));
            rootBinding.tvFilteredViewTitle.setVisibility(View.VISIBLE);
            rootBinding.tvFilteredViewDescription.setVisibility(View.VISIBLE);
            rootBinding.tvFilteredViewDescription.setText(getString(R.string.toast_filtered_routes, filteredMetaRouteInfoList.size(),
                    originalMetaRouteInfoList.size()));

            TastyToast.makeText(this, getString(R.string.toast_filtered_routes, filteredMetaRouteInfoList.size(),
                    originalMetaRouteInfoList.size()), TastyToast.LENGTH_LONG, TastyToast.INFO);
        } else {
            routeListAdapter.setMetaRouteInfoList(originalMetaRouteInfoList);
            hideFilteredInfo();
        }
    }

    @Override
    public void onCancelClicked() {
    }
}