package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.databinding.RvImpCnoNewBinding;
import com.ecosense.app.helper.CommunicationHelper;
import com.ecosense.app.pojo.model.Contact;
import com.ecosense.app.util.CommunicationUtil;

/**
 * <h1>Adapter class for {@link RvImpCnoNewBinding}</h1> layout.
 * <p>
 * Copyright 2021 Vivekanand Mishra.
 * All rights reserved.
 *
 * @author Vivekanand Mishra
 * @version 1.0
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {
    private final List<Contact> contactList;
    private Context context;

    public ContactListAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(RvImpCnoNewBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rootBinding.tvName.setText(contactList.get(position).getName());
        holder.rootBinding.tvDesignation.setText(contactList.get(position).getDesignation());
        holder.rootBinding.tvMobile.setText(contactList.get(position).getMobile());

        holder.rootBinding.btCall.setOnClickListener(v -> {
            if (!CommunicationHelper.callOrOpenDialer(context, contactList.get(position).getMobile()))
                Toast.makeText(context, context.getString(R.string.toast_unable_to_place_call), Toast.LENGTH_SHORT).show();
        });
        holder.rootBinding.btMessage.setOnClickListener(v -> {
            if (!CommunicationUtil.sendSmsUsingIntent(context, contactList.get(position).getMobile()))
                Toast.makeText(context, context.getString(R.string.toast_unable_to_find_sms_app), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return this.contactList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final RvImpCnoNewBinding rootBinding;

        public ViewHolder(@NonNull RvImpCnoNewBinding rootBinding) {
            super(rootBinding.getRoot());
            this.rootBinding = rootBinding;
        }
    }
}