package com.ecosense.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;

import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Voucher;

public class DriverReceivedVoucherAdapter extends RecyclerView.Adapter {
    private List<Voucher> voucherList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public DriverReceivedVoucherAdapter(Context context, List<Voucher> voucherList) {
        this.voucherList = voucherList;
        this.context = context;
        session = new UserSessionManger(this.context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == EMPTY_VIEW) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_empty_view, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(itemView);
            return evh;
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_driver_voucher_detail_row, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            if (voucherList != null && 0 <= position && position < voucherList.size()) {
                final Voucher voucherInfo = voucherList.get(position);

                ((MyViewHolder) holder).tv_driver_name.setText(voucherInfo.getDriver_name());
                ((MyViewHolder) holder).tv_supervisor_name.setText(voucherInfo.getSupervisor_name());
                ((MyViewHolder) holder).tv_voucher_reason.setText(voucherInfo.getVoucher_reason());

                ((MyViewHolder) holder).tv_voucher_usedDate.setText(dateFormat(voucherInfo.getCreation()));

                String vStatus = voucherInfo.getVoucher_status();

                if (vStatus.equalsIgnoreCase(AppConfig.Voucher_status_Pending)) {
                    ((MyViewHolder) holder).tv_voucher_amt.setTextColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_voucher_amt.setText(vStatus);
                    ((MyViewHolder) holder).img_tot_fuel_amt.setVisibility(View.GONE);
                    ((MyViewHolder) holder).btn_Used_Voucher.setVisibility(View.GONE);

                } else {
                    ((MyViewHolder) holder).tv_voucher_amt.setTextColor(ContextCompat.getColor(context, R.color.white));
                    ((MyViewHolder) holder).tv_voucher_amt.setText(String.format("%.2f", Double.parseDouble(voucherInfo.getVoucher_amount())));
                    ((MyViewHolder) holder).img_tot_fuel_amt.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).btn_Used_Voucher.setVisibility(View.VISIBLE);
                }

                if (voucherInfo.getVoucher_reason().equalsIgnoreCase("Fuel")) {
                    Glide.with(context).load(R.drawable.reason_fuel_filled)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_reason);
                } else if (voucherInfo.getVoucher_reason().equalsIgnoreCase("Vehicle Breakdown")) {
                    Glide.with(context).load(R.drawable.reason_vechicle_breakdown)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_reason);
                }


                Log.e(",", "voucherInfo.getCreation() = > " + voucherInfo.getCreation());
            }
        } else if (holder instanceof EmptyViewHolder) {
//            ((EmptyViewHolder) holder).img_empty.setVisibility(View.INVISIBLE);
//
//            ((EmptyViewHolder) holder).ll_empty_view.setBackground(ContextCompat.getDrawable(context, R.drawable.voucher_image));

            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_pull_to_refresh);

//            ((EmptyViewHolder) holder).img_empty_title.setTextColor(ContextCompat.getColor(context, R.color.grey_900));
//            ((EmptyViewHolder) holder).img_empty_msg.setTextColor(ContextCompat.getColor(context, R.color.grey_900));


        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_supervisor_name,tv_driver_name, tv_voucher_reason, tv_voucher_amt, tv_voucher_usedDate;
        public Context context;
        Button btn_Used_Voucher;
        ImageView img_reason, img_tot_fuel_amt;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();


            img_reason = view.findViewById(R.id.img_reason);
            tv_supervisor_name = view.findViewById(R.id.tv_supervisor_name);
            tv_driver_name = view.findViewById(R.id.tv_driver_name);
            tv_voucher_reason = view.findViewById(R.id.tv_voucher_reason);
            tv_voucher_amt = view.findViewById(R.id.tv_voucher_amt);
            tv_voucher_usedDate = view.findViewById(R.id.tv_voucher_usedDate);
            img_tot_fuel_amt = view.findViewById(R.id.img_tot_fuel_amt);

            btn_Used_Voucher = view.findViewById(R.id.btn_Used_Voucher);
            btn_Used_Voucher.setOnClickListener(this);
            itemView.setTag(view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (voucherList.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        TextView img_empty_title, img_empty_msg;
        ImageView img_empty;
        LinearLayout ll_empty_view;
        public Context context;

        public EmptyViewHolder(View view) {
            super(view);
            context = view.getContext();

            img_empty_title = view.findViewById(R.id.img_empty_title);
            img_empty_msg = view.findViewById(R.id.img_empty_msg);
            ll_empty_view = view.findViewById(R.id.ll_empty_view);
            img_empty = view.findViewById(R.id.img_empty);
        }

    }

    @Override
    public int getItemCount() {
//        return this.fillFuelVehicleList.size();
        return voucherList.size() > 0 ? voucherList.size() : 1;
    }


    public String dateFormat(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "dd-MMM-yyyy hh:mm a";

        String formatedDate = "";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date myDate = null;
        try {
            myDate = dateFormat.parse(mStringDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat(newFormat);
        formatedDate = timeFormat.format(myDate);

        return formatedDate;
    }

    public void setFilter(List<Voucher> complaintsListItems) {
        voucherList = new ArrayList<>();
        voucherList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}