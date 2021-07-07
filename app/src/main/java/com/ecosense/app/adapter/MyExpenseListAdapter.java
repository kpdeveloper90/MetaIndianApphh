package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Expense;

public class MyExpenseListAdapter extends RecyclerView.Adapter {
    private List<Expense> expenseList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;
    private static String className = null;

    public MyExpenseListAdapter(Context context, List<Expense> expenseList, String className) {
        this.expenseList = expenseList;
        this.context = context;
        session = new UserSessionManger(this.context);
        this.className = className;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == EMPTY_VIEW) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_empty_view, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(itemView);
            return evh;
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_my_expense_row, parent, false);
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
            if (expenseList != null && 0 <= position && position < expenseList.size()) {
                final Expense eInfo = expenseList.get(position);

                ((MyViewHolder) holder).tv_name.setText(eInfo.getEmployee_name());
                ((MyViewHolder) holder).tv_mno.setText(eInfo.getMobile_no());
                ((MyViewHolder) holder).tv_Date.setText(dateFormat(eInfo.getModified()));
                ((MyViewHolder) holder).chip_amount.setText(eInfo.getTotal_claimed_amount());

                if (className.equalsIgnoreCase("ExpenseDetail")) {

                    ((MyViewHolder) holder).img_reason.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_reason.setVisibility(View.VISIBLE);


                    ((MyViewHolder) holder).tv_Status.setVisibility(View.GONE);
                    ((MyViewHolder) holder).view_status.setVisibility(View.GONE);

                    ((MyViewHolder) holder).tv_reason.setText(eInfo.getExpense_type());
                    ((MyViewHolder) holder).chip_amount.setText(eInfo.getClaim_amount());

                } else {

                    ((MyViewHolder) holder).img_reason.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_reason.setVisibility(View.GONE);

                    ((MyViewHolder) holder).tv_Status.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).view_status.setVisibility(View.VISIBLE);

                    String rStatus = eInfo.getApproval_status();

                    if (rStatus.equalsIgnoreCase(AppConfig.Expense_Status_Approved)) {

                        ((MyViewHolder) holder).tv_Status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                        ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.complete));
                        ((MyViewHolder) holder).tv_Status.setText(eInfo.getApproval_status());

                    } else if (rStatus.equalsIgnoreCase(AppConfig.Expense_Status_Draft)) {
                        ((MyViewHolder) holder).tv_Status.setTextColor(ContextCompat.getColor(context, R.color.in_progress));
                        ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.in_progress));
//                        ((MyViewHolder) holder).tv_Status.setText(eInfo.getApproval_status());
                        ((MyViewHolder) holder).tv_Status.setText("Submitted");

                    } else {
                        ((MyViewHolder) holder).tv_Status.setTextColor(ContextCompat.getColor(context, R.color.pending));
                        ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.pending));
                        ((MyViewHolder) holder).tv_Status.setText(eInfo.getApproval_status());
                    }
                }



            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }

    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatTextView tv_name, tv_mno, tv_reason, tv_Status, tv_Date;
        View view_status;
        Chip chip_amount;
        public Context context;
        private SwipeRevealLayout swipeRevealLayout;
        MaterialCardView card_view;
        LinearLayout ll_view;
        AppCompatImageView img_reason;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            card_view = view.findViewById(R.id.card_view);
            view_status = view.findViewById(R.id.view_status);

            ll_view = view.findViewById(R.id.ll_view);
            tv_name = view.findViewById(R.id.tv_name);
            chip_amount = view.findViewById(R.id.chip_amount);
            tv_mno = view.findViewById(R.id.tv_mno);
            tv_reason = view.findViewById(R.id.tv_reason);
            img_reason = view.findViewById(R.id.img_reason);
            tv_Status = view.findViewById(R.id.tv_Status);
            tv_Date = view.findViewById(R.id.tv_Date);


            ll_view.setOnClickListener(this);

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
        if (expenseList.size() == 0) {
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
        return expenseList.size() > 0 ? expenseList.size() : 1;
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

    public void setFilter(List<Expense> complaintsListItems) {
        expenseList = new ArrayList<>();
        expenseList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }


}