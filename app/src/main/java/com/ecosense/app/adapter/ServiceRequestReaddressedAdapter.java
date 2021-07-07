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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;

public class ServiceRequestReaddressedAdapter extends RecyclerView.Adapter {
    private List<Complaints> complaintsList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public ServiceRequestReaddressedAdapter(Context context, List<Complaints> complaintsList) {
        this.complaintsList = complaintsList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_sr_assign_row, parent, false);
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
            if (complaintsList != null && 0 <= position && position < complaintsList.size()) {
                final Complaints complaintsItem = complaintsList.get(position);

                ((MyViewHolder) holder).tv_complainNo.setText(complaintsItem.getComId());
                ((MyViewHolder) holder).tv_esta_name.setText(complaintsItem.getEsta_name());
                ((MyViewHolder) holder).tv_complain_Type.setText(complaintsItem.getCpttype());
                ((MyViewHolder) holder).tv_complain_name.setText(complaintsItem.getCptpname());
                ((MyViewHolder) holder).tv_complain_ward_no.setText(context.getString(R.string.ward_no)+ complaintsItem.getCptward_no());
                ((MyViewHolder) holder).tv_complain_mno.setText(context.getString(R.string.mobile_tag) + complaintsItem.getCptmobileno());
                ((MyViewHolder) holder).tv_complain_date.setText(context.getString(R.string.req_date) + dateFormat(Objects.requireNonNull(complaintsItem.getCptdate())));

                if (complaintsItem.getDriver_name() == null) {
                    ((MyViewHolder) holder).tv_com_assign_to.setText(context.getString(R.string.not_assign));
                } else {
                    ((MyViewHolder) holder).tv_com_assign_to.setText(context.getString(R.string.assign_to)+ complaintsItem.getDriver_name());
                }

                ((MyViewHolder) holder).tv_resolved_date.setVisibility(View.GONE);
                String status = complaintsItem.getCptstatus();

                if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_In_Process)) {
                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.in_progress));
                } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_Complete)) {
                    ((MyViewHolder) holder).tv_resolved_date.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_resolved_date.setText(context.getString(R.string.resolved_date) + dateFormat(complaintsItem.getCptresdate()));
                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.complete));
                } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.new_complaint));
                } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_Pending)) {
                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pending));
                }
            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        AppCompatTextView tv_esta_name, tv_complainNo, tv_complain_Type, tv_complain_name, tv_complain_mno, tv_complain_ward_no,
                tv_com_assign_to, tv_complain_date, tv_resolved_date;


        public Context context;

        LinearLayout ll_view;
        AppCompatImageView  img_status;


        public MyViewHolder(View view) {
            super(view);


            ll_view = view.findViewById(R.id.ll_view);
            context = view.getContext();


            tv_esta_name = view.findViewById(R.id.tv_esta_name);
            tv_complainNo = view.findViewById(R.id.tv_complainNo);
            tv_complain_Type = view.findViewById(R.id.tv_complain_Type);
            tv_complain_name = view.findViewById(R.id.tv_complain_name);
            tv_complain_mno = view.findViewById(R.id.tv_complain_mno);
            tv_complain_ward_no = view.findViewById(R.id.tv_complain_ward_no);
            tv_complain_date = view.findViewById(R.id.tv_complain_date);
            tv_resolved_date = view.findViewById(R.id.tv_resolved_date);
            tv_com_assign_to = view.findViewById(R.id.tv_com_assign_to);
            img_status = view.findViewById(R.id.img_status);


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
        if (complaintsList.size() == 0) {
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
        return complaintsList.size() > 0 ? complaintsList.size() : 1;
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

    public void setFilter(List<Complaints> complaintsListItems) {
        complaintsList = new ArrayList<>();
        complaintsList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}