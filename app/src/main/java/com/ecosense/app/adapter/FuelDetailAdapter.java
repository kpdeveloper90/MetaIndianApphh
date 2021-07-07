package com.ecosense.app.adapter;

import android.content.Context;

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
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Fuel;

public class FuelDetailAdapter extends RecyclerView.Adapter {
    private List<Fuel> fillFuelVehicleList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public FuelDetailAdapter(Context context, List<Fuel> fillFuelVehicleList) {
        this.fillFuelVehicleList = fillFuelVehicleList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_fuel_detail_row, parent, false);
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
            if (fillFuelVehicleList != null && 0 <= position && position < fillFuelVehicleList.size()) {
                final Fuel fuelInfo = fillFuelVehicleList.get(position);

                ((MyViewHolder) holder).tv_vh_No.setText(fuelInfo.getVehicle_number());
                ((MyViewHolder) holder).tv_fuel_pump_loc.setText(fuelInfo.getPump_name() + " (" + fuelInfo.getOem_name() + ")");
                ((MyViewHolder) holder).tv_meter_reading.setText(fuelInfo.getMeter_reading());

                ((MyViewHolder) holder).tv_fuel_qty.setText(String.format("%.2f", Double.parseDouble(fuelInfo.getQuantity())));
                ((MyViewHolder) holder).tv_fuel_price.setText(String.format("%.2f", Double.parseDouble(fuelInfo.getRate())));
                ((MyViewHolder) holder).tv_tot_fuel_amt.setText(String.format("%.2f", Double.parseDouble(fuelInfo.getTotal())));
                ((MyViewHolder) holder).tv_fuel_fill_date.setText(dateFormat(fuelInfo.getCreation()));

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_pull_to_refresh);

        }

    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_vh_No, tv_fuel_pump_loc, tv_meter_reading, tv_fuel_qty, tv_fuel_price,
                tv_tot_fuel_amt, tv_fuel_fill_date;
        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();

            tv_vh_No = view.findViewById(R.id.tv_vh_No);
            tv_fuel_pump_loc = view.findViewById(R.id.tv_fuel_pump_loc);
            tv_meter_reading = view.findViewById(R.id.tv_meter_reading);
            tv_fuel_qty = view.findViewById(R.id.tv_fuel_qty);
            tv_fuel_price = view.findViewById(R.id.tv_fuel_price);
            tv_tot_fuel_amt = view.findViewById(R.id.tv_tot_fuel_amt);
            tv_fuel_fill_date = view.findViewById(R.id.tv_fuel_fill_date);


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
        if (fillFuelVehicleList.size() == 0) {
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
        return fillFuelVehicleList.size() > 0 ? fillFuelVehicleList.size() : 1;
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

    public void setFilter(List<Fuel> complaintsListItems) {
        fillFuelVehicleList = new ArrayList<>();
        fillFuelVehicleList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }


}