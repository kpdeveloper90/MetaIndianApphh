package com.ecosense.app.helper.bottomSheet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.adapter.PoiListAdapter;
import com.ecosense.app.databinding.BottomSheetRouteMapBinding;
import com.ecosense.app.db.models.PoiPoint;

/**
 * <h1>Bottom sheet to show poi list</h1>
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PoiBottomSheet extends BottomSheetDialogFragment implements PoiListAdapter.OnChangeListener {

    public interface OnPoiStateListener {
        void onClicked(@NonNull PoiPoint poiPoint);
        void onEditClicked(@NonNull PoiPoint poiPoint);
        void onDeleteClicked(@NonNull PoiPoint poiPoint, final boolean isDeletionSuccessful);
    }

    private BottomSheetRouteMapBinding rootBinding;
    private FrameLayout bottomSheet;
    private PoiListAdapter poiListAdapter;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private OnPoiStateListener listener;
    private boolean isRoutePlottingInstance;

    public PoiBottomSheet() {
    }

    public PoiBottomSheet(@NonNull List<PoiPoint> poiPointList, boolean isRoutePlottingInstance) {
        poiListAdapter = new PoiListAdapter(poiPointList, this);
        poiListAdapter.setRoutePlottingInstance(isRoutePlottingInstance);
        this.isRoutePlottingInstance = isRoutePlottingInstance;
    }

    public void setListener(OnPoiStateListener listener) {
        this.listener = listener;
    }

    public void setPoiPointList(List<PoiPoint> poiPointList) {
        poiListAdapter = new PoiListAdapter(poiPointList, this);
    }

    public void setRoutePlottingInstance(boolean routePlottingInstance) {
        isRoutePlottingInstance = routePlottingInstance;
        poiListAdapter.setRoutePlottingInstance(isRoutePlottingInstance);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootBinding = BottomSheetRouteMapBinding.inflate(inflater, container, false);
        updateViews();
        return rootBinding.getRoot();
    }


    private void updateViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        rootBinding.rvPoi.setLayoutManager(layoutManager);
        rootBinding.rvPoi.setItemAnimator(new DefaultItemAnimator());
        rootBinding.rvPoi.setAdapter(poiListAdapter);

        if (poiListAdapter.getItemCount() > 0)
            rootBinding.tvNoItem.setVisibility(View.GONE);
        else rootBinding.tvNoItem.setVisibility(View.VISIBLE);

        rootBinding.tvTotal.setText(getString(R.string.tv_total, poiListAdapter.getItemCount()));

        setupBottomSheet();
    }

    private void setupBottomSheet() {
        requireDialog().setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;

            bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            bottomSheet.setBackgroundColor(Color.TRANSPARENT);

            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        });
    }

    public void addPoi(@NonNull final PoiPoint poiPoint) {
        if (poiListAdapter != null)
            poiListAdapter.addPoi(poiPoint);
    }

    @Override
    public void onPoiAdded() {

    }

    @Override
    public void onPoiRemoved() {
        if (poiListAdapter.getItemCount() == 0)
            rootBinding.tvNoItem.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPoiClicked(@NonNull PoiPoint poiPoint) {
        if (listener != null)
            listener.onClicked(poiPoint);
    }

    @Override
    public void onEditClicked(@NonNull PoiPoint poiPoint) {
        if (listener != null)
            listener.onEditClicked(poiPoint);
    }

    @Override
    public void onDeleteClicked(@NonNull PoiPoint poiPoint, final boolean isDeletionSuccessful) {
        if (listener != null)
            listener.onDeleteClicked(poiPoint, isDeletionSuccessful);
    }

    public void dismiss() {
        if (bottomSheet != null && bottomSheet.isShown())
            super.dismiss();
    }
}
