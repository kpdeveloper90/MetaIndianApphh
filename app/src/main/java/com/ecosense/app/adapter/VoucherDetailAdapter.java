package com.ecosense.app.adapter;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Voucher;

public class VoucherDetailAdapter extends RecyclerView.Adapter {
    private List<Voucher> voucherList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    private int selectCount = 0;

    public VoucherDetailAdapter(Context context, List<Voucher> voucherList) {
        this.voucherList = voucherList;
        this.context = context;
        session = new UserSessionManger(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == EMPTY_VIEW) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_empty_view, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(itemView);
            return evh;
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_voucher_detail_row, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            if (voucherList != null && 0 <= position && position < voucherList.size()) {
                final Voucher voucherInfo = voucherList.get(position);


                // Save/restore the open/close state.
                // You need to provide a String id which uniquely defines the data object.
                viewBinderHelper.bind(((MyViewHolder) holder).swipeRevealLayout, voucherInfo.getName());
                viewBinderHelper.setOpenOnlyOne(true);
//                viewBinderHelper.lockSwipe(voucherInfo.getName());


                ((MyViewHolder) holder).tv_vh_No.setText(voucherInfo.getVehicle_number());
                ((MyViewHolder) holder).tv_voucher_reason.setText(voucherInfo.getVoucher_reason());
                ((MyViewHolder) holder).tv_voucherStatus.setText(voucherInfo.getVoucher_status());

                if (voucherInfo.getDriver_name() == null) {
                    ((MyViewHolder) holder).tv_voucher_driver.setText("-");
                } else {
                    ((MyViewHolder) holder).tv_voucher_driver.setText(voucherInfo.getDriver_name());
                }
                if (voucherInfo.getGenerated_by().equalsIgnoreCase(AppConfig.USubType_Driver)) {
                    viewBinderHelper.lockSwipe(voucherInfo.getName());
                }

                String vStatus = voucherInfo.getVoucher_status();

                if (vStatus.equalsIgnoreCase(AppConfig.Voucher_status_Used)) {
                    viewBinderHelper.lockSwipe(voucherInfo.getName());
                    ((MyViewHolder) holder).ll_amount_view.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_voucherStatus.setTextColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).view_voucher_status.setBackgroundColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).tv_voucher_usedDate.setText(dateFormat(voucherInfo.getVoucher_use_date()));
                    ((MyViewHolder) holder).tv_voucher_amt.setText(String.format("%.2f", Double.parseDouble(voucherInfo.getVoucher_amount())));
                } else if (vStatus.equalsIgnoreCase(AppConfig.Voucher_status_Pending)) {
                    viewBinderHelper.lockSwipe(voucherInfo.getName());
                    ((MyViewHolder) holder).ll_amount_view.setVisibility(View.GONE);
                    ((MyViewHolder) holder).view_voucher_status.setBackgroundColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_voucherStatus.setTextColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_voucher_usedDate.setText(dateFormat(voucherInfo.getCreation()));
                } else {
                    ((MyViewHolder) holder).ll_amount_view.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).view_voucher_status.setBackgroundColor(ContextCompat.getColor(context, R.color.in_progress));
                    ((MyViewHolder) holder).tv_voucherStatus.setTextColor(ContextCompat.getColor(context, R.color.in_progress));
                    ((MyViewHolder) holder).tv_voucher_usedDate.setText(dateFormat(voucherInfo.getCreation()));
                    ((MyViewHolder) holder).tv_voucher_amt.setText(String.format("%.2f", Double.parseDouble(voucherInfo.getVoucher_amount())));
                }

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private SwipeRevealLayout swipeRevealLayout;
        TextView tv_vh_No, tv_voucher_reason, tv_voucherStatus, tv_voucher_driver,
                tv_voucher_amt, tv_voucher_usedDate;
        public Context context;
        View view_voucher_status;
        ImageView img_delete, img_edit;
        CardView card_view;
        LinearLayout ll_view, ll_amount_view;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            card_view = view.findViewById(R.id.card_view);
            ll_view = view.findViewById(R.id.ll_view);
            tv_voucher_driver = view.findViewById(R.id.tv_voucher_driver);
            swipeRevealLayout = view.findViewById(R.id.swipeRevealLayout);
            view_voucher_status = view.findViewById(R.id.view_voucher_status);
            tv_voucherStatus = view.findViewById(R.id.tv_voucherStatus);
            ll_amount_view = view.findViewById(R.id.linearLayout4);
            tv_vh_No = view.findViewById(R.id.tv_vh_No);

            tv_voucher_reason = view.findViewById(R.id.tv_voucher_reason);

            tv_voucher_amt = view.findViewById(R.id.tv_voucher_amt);
            tv_voucher_usedDate = view.findViewById(R.id.tv_voucher_usedDate);

            img_edit = view.findViewById(R.id.img_edit);
            img_delete = view.findViewById(R.id.img_delete);

            img_edit.setOnClickListener(this);
            img_delete.setOnClickListener(this);
            ll_view.setOnClickListener(this);

            itemView.setTag(view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            if (view == ll_view) {
//                if (selectCount == 0) {
//                    selectCount = selectCount + 1;
//                    viewBinderHelper.openLayout(voucherList.get(this.getAdapterPosition()).getName());
//                } else {
//                    selectCount = 0;
//                    viewBinderHelper.closeLayout(voucherList.get(this.getAdapterPosition()).getName());
//                }
//            }
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
        public Context context;

        public EmptyViewHolder(View view) {
            super(view);
            context = view.getContext();

            img_empty_title = view.findViewById(R.id.img_empty_title);
            img_empty_msg = view.findViewById(R.id.img_empty_msg);
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
        String newFormat = "dd-MMM-yyyy hh:mm:ss a";

        String formatedDate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date myDate = null;
        try {
            myDate = dateFormat.parse(mStringDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(newFormat);
        formatedDate = timeFormat.format(myDate);

        return formatedDate;
    }

    public void setFilter(List<Voucher> complaintsListItems) {
        voucherList = new ArrayList<>();
        voucherList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}