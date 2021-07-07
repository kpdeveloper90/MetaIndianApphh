package com.ecosense.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteItem;

public class RouteCoverageAdapter extends RecyclerView.Adapter {
    private List<RouteItem> routeInfoList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public RouteCoverageAdapter(Context context, List<RouteItem> routeInfoList) {
        this.routeInfoList = routeInfoList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_route_coverage_row, parent, false);
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
            if (routeInfoList != null && 0 <= position && position < routeInfoList.size()) {
                final RouteItem rInfo = routeInfoList.get(position);

                ((MyViewHolder) holder).tv_vh_route_name.setText(rInfo.getDa_routeid());
                ((MyViewHolder) holder).tv_route_vh_No.setText(rInfo.getDa_vehicleno());
                ((MyViewHolder) holder).tv_route_dr_name.setText(rInfo.getDa_drivername());
                ((MyViewHolder) holder).tv_route_vh_type.setText( rInfo.getVehicle_type());

                String rStatus = rInfo.getR_status();

                if (rStatus.equalsIgnoreCase(AppConfig.ROUTE_Status_Complete)) {
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getR_status());
                    ((MyViewHolder) holder).tv_route_end_Date.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_vh_start_date.setText(context.getString(R.string.start_date) +dateFormat(rInfo.getCreation()));
                    ((MyViewHolder) holder).tv_route_end_Date.setText(context.getString(R.string.end_date) +dateFormat(rInfo.getModified()));

                } else if (rStatus.equalsIgnoreCase(AppConfig.ROUTE_Status_In_Progress)) {
                    ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.in_progress));
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.in_progress));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getR_status());

                    ((MyViewHolder) holder).tv_vh_start_date.setText(context.getString(R.string.start_date)+ dateFormat(rInfo.getCreation()));
                    ((MyViewHolder) holder).tv_route_end_Date.setVisibility(View.GONE);
                } else {
                    ((MyViewHolder) holder).tv_vh_start_date.setText(context.getString(R.string.start_date)+ dateFormat(rInfo.getCreation()));
                    ((MyViewHolder) holder).tv_route_end_Date.setVisibility(View.GONE);

                    ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getR_status());
                }

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }

    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_vh_route_name, tv_route_vh_No, tv_route_dr_name, tv_route_vh_type, tv_vh_start_date, tv_route_end_Date, tv_route_Status;
        View view_status;
        public Context context;

        MaterialCardView card_view;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            card_view = view.findViewById(R.id.card_view);
            view_status = view.findViewById(R.id.view_status);

            tv_vh_route_name = view.findViewById(R.id.tv_vh_route_name);
            tv_route_vh_No = view.findViewById(R.id.tv_route_vh_No);
            tv_route_dr_name = view.findViewById(R.id.tv_route_dr_name);


            tv_route_vh_type = view.findViewById(R.id.tv_route_vh_type);
            tv_vh_start_date = view.findViewById(R.id.tv_vh_start_date);
            tv_route_end_Date = view.findViewById(R.id.tv_route_end_Date);
            tv_route_Status = view.findViewById(R.id.tv_route_Status);


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
        if (routeInfoList.size() == 0) {
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
        return routeInfoList.size() > 0 ? routeInfoList.size() : 1;
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

    public void setFilter(List<RouteItem> complaintsListItems) {
        routeInfoList = new ArrayList<>();
        routeInfoList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }


}