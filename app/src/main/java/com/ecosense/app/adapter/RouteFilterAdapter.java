package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import com.ecosense.app.databinding.ItemIndividualFilterBinding;

/**
 * <h1>Adapter class for {@link ItemIndividualFilterBinding}</h1> layout.
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class RouteFilterAdapter extends RecyclerView.Adapter<RouteFilterAdapter.ViewHolder> {

    public interface RouteFilterAdapterListener {
        void onChecked(@NonNull final String option);

        void onUnChecked(@NonNull final String option);
    }

    private final List<String> options;
    private final List<String> selectedOptions;
    private final RouteFilterAdapterListener listener;
    private Context context;

    public RouteFilterAdapter(@NonNull final List<String> options, @NonNull final List<String> selectedOptions,
                              @NonNull final RouteFilterAdapterListener listener, final boolean sortData) {
        this.options = options;
        this.selectedOptions = selectedOptions;
        this.listener = listener;

        if (sortData)
            Collections.sort(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(ItemIndividualFilterBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rootBinding.cbOption.setText(options.get(position));
        holder.rootBinding.cbOption.setChecked(selectedOptions.contains(options.get(position)));

        holder.rootBinding.cbOption.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                if (isChecked)
                    listener.onChecked(buttonView.getText().toString());
                else listener.onUnChecked(buttonView.getText().toString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemIndividualFilterBinding rootBinding;

        public ViewHolder(@NonNull ItemIndividualFilterBinding rootBinding) {
            super(rootBinding.getRoot());
            this.rootBinding = rootBinding;
        }
    }
}
