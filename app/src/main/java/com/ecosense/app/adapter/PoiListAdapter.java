package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.List;
import java.util.concurrent.Executors;

import com.ecosense.app.R;
import com.ecosense.app.databinding.ItemPoiBinding;
import com.ecosense.app.db.RouteDatabase;
import com.ecosense.app.db.models.PoiPoint;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.alertDialog.AddEditPoiDialog;
import com.ecosense.app.remote.BackEndRequestCall;
import com.ecosense.app.remote.BackendError;
import com.ecosense.app.remote.RetrofitHelper;

/**
 * <h1>Adapter class for {@link ItemPoiBinding}</h1> layout.
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class PoiListAdapter extends RecyclerView.Adapter<PoiListAdapter.ViewHolder> {

    public interface OnChangeListener {
        default void onPoiAdded() {
        }

        default void onPoiRemoved() {
        }

        void onPoiClicked(@NonNull final PoiPoint poiPoint);

        void onEditClicked(@NonNull final PoiPoint poiPoint);

        void onDeleteClicked(@NonNull final PoiPoint poiPoint, final boolean isDeletionSuccessful);
    }

    private List<PoiPoint> poiPointList;
    private Context context;
    private OnChangeListener listener;
    private AddEditPoiDialog addEditPoiDialog;
    private boolean isRoutePlottingInstance;

    public PoiListAdapter() {
    }

    public PoiListAdapter(@NonNull List<PoiPoint> poiPointList, @NonNull final OnChangeListener listener) {
        this.poiPointList = poiPointList;
        this.listener = listener;
        addEditPoiDialog = new AddEditPoiDialog(ConnactionCheckApplication.activity);
    }

    public void setPoiPointList(List<PoiPoint> poiPointList) {
        this.poiPointList = poiPointList;
    }

    public void setRoutePlottingInstance(boolean routePlottingInstance) {
        isRoutePlottingInstance = routePlottingInstance;
        addEditPoiDialog.setRoutePlottingInstance(isRoutePlottingInstance);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(ItemPoiBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rootBinding.tvPoiName.setText(poiPointList.get(position).getName());
        holder.rootBinding.cpPoiType.setText(poiPointList.get(position).getType());
        holder.rootBinding.tvLat.setText(poiPointList.get(position).getCoordinate().getLatitude());
        holder.rootBinding.tvLon.setText(poiPointList.get(position).getCoordinate().getLongitude());

        holder.rootBinding.getRoot().setOnClickListener(v -> {
            if (listener != null)
                listener.onPoiClicked(poiPointList.get(position));
        });

        holder.rootBinding.ivEdit.setOnClickListener(v -> {
            if (listener != null)
                listener.onEditClicked(poiPointList.get(position));
        });

        if (isRoutePlottingInstance) {
            holder.rootBinding.ivDelete.setOnClickListener(v -> Executors.newSingleThreadExecutor().execute(() ->
            {
                notifyDeleteStatus(poiPointList.get(position), position,
                        RouteDatabase.getInstance(context).poiPointDao().deletePoint(poiPointList.get(position)) > 0);
            }));
        } else {
            holder.rootBinding.ivDelete.setOnClickListener(v -> {
                BackEndRequestCall.enqueue(RetrofitHelper.deletePoi(poiPointList.get(position).getServerId()),
                        "poiDelete", new BackEndRequestCall.BackendRequestListener() {
                            @Override
                            public void onSuccess(String tag, @NonNull Object responseBody) {
                                notifyDeleteStatus(poiPointList.get(position), position, true);
                            }

                            @Override
                            public void onError(String tag, @NonNull BackendError backendError) {
                                notifyDeleteStatus(poiPointList.get(position), position, false);
                            }
                        });
            });
        }
    }

    private void notifyDeleteStatus(@NonNull final PoiPoint poiPoint, final int index, final boolean isDeleteSuccessful) {
        ConnactionCheckApplication.activity.runOnUiThread(() -> {
            TastyToast.makeText(context, isDeleteSuccessful ?
                            context.getString(R.string.toast_delete_poi_success) : context.getString(R.string.toast_delete_poi_error),
                    TastyToast.LENGTH_SHORT, isDeleteSuccessful ? TastyToast.SUCCESS : TastyToast.ERROR);

            if (isDeleteSuccessful)
                removePoi(poiPointList.get(index), index);

            if (listener != null)
                listener.onDeleteClicked(poiPoint, isDeleteSuccessful);
        });
    }

    @Override
    public int getItemCount() {
        return poiPointList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPoiBinding rootBinding;

        public ViewHolder(@NonNull ItemPoiBinding rootBinding) {
            super(rootBinding.getRoot());
            this.rootBinding = rootBinding;
        }
    }

    public void addPoi(@NonNull final PoiPoint poiPoint) {
        poiPointList.add(poiPoint);
        notifyItemInserted(poiPointList.size() - 1);

        if (listener != null)
            listener.onPoiAdded();
    }

    private void removePoi(@NonNull final PoiPoint poiPoint, final int index) {
        poiPointList.remove(poiPoint);
        notifyItemRemoved(index);

        if (listener != null)
            listener.onPoiRemoved();
    }
}
