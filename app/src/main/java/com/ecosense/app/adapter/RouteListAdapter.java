package com.ecosense.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.sdsmdg.tastytoast.TastyToast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.activity.metaData.RouteMapActivity;
import com.ecosense.app.databinding.ItemRouteBinding;
import com.ecosense.app.db.models.MetaRouteInfo;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.alertDialog.AlertDialogHelper;
import com.ecosense.app.helper.alertDialog.BackendProgressDialog;
import com.ecosense.app.helper.view.TextSetter;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Adapter class for {@link ItemRouteBinding}</h1> layout.
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {

    private List<MetaRouteInfo> metaRouteInfoList;
    private Context context;
    private BackendProgressDialog backendProgressDialog;
    private int currentSelectedIndex;
    private volatile boolean isFetchingData;
    private Format formatter;

    @SuppressLint("SimpleDateFormat")
    public RouteListAdapter(@NonNull List<MetaRouteInfo> metaRouteInfoList) {
        this.metaRouteInfoList = metaRouteInfoList;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public List<MetaRouteInfo> getMetaRouteInfoList() {
        return metaRouteInfoList;
    }

    public void setMetaRouteInfoList(List<MetaRouteInfo> metaRouteInfoList) {
        this.metaRouteInfoList = new ArrayList<>(metaRouteInfoList);
        notifyDataSetChanged();
    }

    private void initConfig() {
        isFetchingData = false;

        backendProgressDialog = new BackendProgressDialog.Builder(ConnactionCheckApplication.activity)
                .setProgressTitle(context.getString(R.string.dialog_route_fetching_title))
                .setErrorTitle(context.getString(R.string.dialog_route_fetching_failed_title))
                .setProgressDescription(context.getString(R.string.dialog_route_fetching_description, context.getString(R.string.dialog_description_this_may_take_a_while)))
                .setListener(new BackendProgressDialog.BackendProgressDialogListener() {
                    @Override
                    public void onRetryClicked(AlertDialogHelper dialogInstance) {
                        getRoute();
                    }

                    @Override
                    public void onCancelClicked(AlertDialogHelper dialogInstance) {
                        dialogInstance.dismiss();
                    }
                })
                .build();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        initConfig();
        return new ViewHolder(ItemRouteBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextSetter.setText(holder.rootBinding.tvRouteName, metaRouteInfoList.get(position).getRouteName());
        TextSetter.setText(holder.rootBinding.tvVehicleNumber, metaRouteInfoList.get(position).getVehicleNumber());
        TextSetter.setText(holder.rootBinding.tvZone, metaRouteInfoList.get(position).getZoneName());
        TextSetter.setText(holder.rootBinding.tvWard, metaRouteInfoList.get(position).getWardNumber());
        TextSetter.setText(holder.rootBinding.tvDate, metaRouteInfoList.get(position).getUpdatedAt());

        holder.rootBinding.getRoot().setOnClickListener(v -> {
            currentSelectedIndex = position;
            getRoute();
        });
    }

    private void getRoute() {
        backendProgressDialog.showProgress();
        if (!isFetchingData) {
            isFetchingData = true;
            BackEndRequestCall.enqueue(RetrofitHelper.getRoute(metaRouteInfoList.get(currentSelectedIndex).getRouteId()),
                    null, new BackEndRequestCall.BackendRequestListener() {
                        @Override
                        public void onSuccess(String tag, @NonNull Object responseBody) {
                            isFetchingData = false;
                            Intent intent = new Intent(context, RouteMapActivity.class);
                            intent.putExtra(RouteMapActivity.KEY_INVOCATION_TYPE_PLOTTING, false);
                            intent.putExtra(RouteMapActivity.KEY_ROUTE_ID, metaRouteInfoList.get(currentSelectedIndex).getRouteId());

                            backendProgressDialog.dismiss();
                            context.startActivity(intent);
                        }

                        @Override
                        public void onError(String tag, @NonNull BackendError backendError) {
                            Pair<String, String> problemSolutionPair = RetrofitHelper.getProblemSolutionPair(backendError);

                            backendProgressDialog.showError(problemSolutionPair.first, problemSolutionPair.second,
                                    BackendError.PARSING == backendError || BackendError.UNSUCCESSFUL == backendError);

                            isFetchingData = false;
                        }
                    });
        } else
            TastyToast.makeText(context, context.getString(R.string.toast_still_fetching_data), TastyToast.LENGTH_SHORT, TastyToast.INFO);
    }

    @Override
    public int getItemCount() {
        return metaRouteInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRouteBinding rootBinding;

        public ViewHolder(@NonNull ItemRouteBinding rootBinding) {
            super(rootBinding.getRoot());
            this.rootBinding = rootBinding;
        }
    }
}
