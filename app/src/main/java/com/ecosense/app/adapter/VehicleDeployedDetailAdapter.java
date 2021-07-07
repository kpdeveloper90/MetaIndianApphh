package com.ecosense.app.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;

public class VehicleDeployedDetailAdapter extends RecyclerView.Adapter {
    private List<RouteInfo> routeInfoList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public VehicleDeployedDetailAdapter(Context context, List<RouteInfo> routeInfoList) {
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_vehicle_deployed_row, parent, false);
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
                final RouteInfo rInfo = routeInfoList.get(position);

                ((MyViewHolder) holder).tv_vh_No.setText(rInfo.getDa_vehicleno());


                String vStatus = rInfo.getStatus();

                if (vStatus.equalsIgnoreCase(AppConfig.Vehicle_deployed_status)) {

                    ((MyViewHolder) holder).tv_vh_Status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).tv_vh_type.setText(rInfo.getVehicle_type() + " (" + rInfo.getPoi() + ")");



                    ((MyViewHolder) holder).img_vh_dr_name.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_vh_route.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_vh_route_name.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_vh_dr_name.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_tagWard_No.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_Ward_No.setVisibility(View.VISIBLE);

                    ((MyViewHolder) holder).img_vh_reason.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_vh_reason.setVisibility(View.GONE);

                    ((MyViewHolder) holder).tv_Ward_No.setText(rInfo.getFawardno());
                    ((MyViewHolder) holder).tv_vh_Date.setText(dateFormat(Connection.getCurrentDateTime()));
                    ((MyViewHolder) holder).tv_vh_dr_name.setText(rInfo.getDa_drivername());
                    ((MyViewHolder) holder).tv_vh_route_name.setText(rInfo.getR_name());
                    ((MyViewHolder) holder).tv_vh_Status.setText(rInfo.getStatus());
                } else {
                    ((MyViewHolder) holder).tv_vh_Date.setText(dateFormat(Connection.getCurrentDateTime()));
                    ((MyViewHolder) holder).img_vh_dr_name.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_vh_route.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_vh_route_name.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_vh_dr_name.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_Ward_No.setVisibility(View.GONE);
                    ((MyViewHolder) holder).tv_tagWard_No.setVisibility(View.GONE);

                    ((MyViewHolder) holder).img_vh_reason.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_vh_reason.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_vh_reason.setText(rInfo.getReason_for_breakdown());
                    ((MyViewHolder) holder).tv_vh_type.setText(rInfo.getVehicle_type());
                    ((MyViewHolder) holder).view_status.setBackgroundColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_vh_Status.setTextColor(ContextCompat.getColor(context, R.color.pending));

                    ((MyViewHolder) holder).tv_vh_Status.setText(rInfo.getStatus());
                }

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }

    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_vh_No, tv_vh_type, tv_vh_dr_name,tv_tagWard_No,tv_Ward_No, tv_vh_route_name, tv_vh_reason, tv_vh_Status, tv_vh_Date;
        View view_status;
        public Context context;
        ImageView img_vh_dr_name, img_vh_route, img_vh_reason;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();

            view_status = view.findViewById(R.id.view_status);
            tv_vh_No = view.findViewById(R.id.tv_vh_No);
            tv_vh_type = view.findViewById(R.id.tv_vh_type);
            tv_vh_dr_name = view.findViewById(R.id.tv_vh_dr_name);
            tv_vh_route_name = view.findViewById(R.id.tv_vh_route_name);
            tv_vh_reason = view.findViewById(R.id.tv_vh_reason);
            tv_vh_Status = view.findViewById(R.id.tv_vh_Status);
            tv_vh_Date = view.findViewById(R.id.tv_vh_Date);
            tv_Ward_No = view.findViewById(R.id.tv_Ward_No);
            tv_tagWard_No = view.findViewById(R.id.tv_tagWard_No);
            img_vh_dr_name = view.findViewById(R.id.img_vh_dr_name);
            img_vh_route = view.findViewById(R.id.img_vh_route);
            img_vh_reason = view.findViewById(R.id.img_vh_reason);


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

    public void setFilter(List<RouteInfo> complaintsListItems) {
        routeInfoList = new ArrayList<>();
        routeInfoList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }


}