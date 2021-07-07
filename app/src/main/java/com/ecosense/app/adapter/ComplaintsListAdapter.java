package com.ecosense.app.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;

import static com.ecosense.app.helper.AppConfig.CPTSTATUS_Complete;
import static com.ecosense.app.helper.AppConfig.CPTSTATUS_In_Process;
import static com.ecosense.app.helper.AppConfig.CPTSTATUS_New;
import static com.ecosense.app.helper.AppConfig.CPTSTATUS_Pending;

public class ComplaintsListAdapter extends RecyclerView.Adapter {
    private List<Complaints> complaintsList;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public ComplaintsListAdapter(Context context, List<Complaints> complaintsList) {
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_complaint_list_row, parent, false);
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

                final Complaints complaintsItem = (Complaints) complaintsList.get(position);
//
                ((MyViewHolder) holder).tv_complainNo.setText(complaintsItem.getComId());
                ((MyViewHolder) holder).tv_com_date_time.setText(dateFormat(complaintsItem.getCptdate()));
                ((MyViewHolder) holder).tv_complain_title.setText(complaintsItem.getCpttype());
                ((MyViewHolder) holder).tv_psName.setText(complaintsItem.getCptpname());
                ((MyViewHolder) holder).tv_com_Mno.setText(complaintsItem.getCptmobileno());
//            ((MyViewHolder) holder).tv_com_emailId.setText(complaintsItem.getEmailId());


                if (complaintsItem.getDriver_name() == null) {
                    ((MyViewHolder) holder).tv_com_assign.setText("NA");
                } else {
                    ((MyViewHolder) holder).tv_com_assign.setText(complaintsItem.getDriver_name());
                }

                if (complaintsItem.getCptphoto() != null) {
                    Glide.with(context).load(Connection.decodeFromBase64ToBitmap(complaintsItem.getCptphoto()))
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_complain);
                } else {
//                String url = session.getMyServerIP() + complaintsItem.getCptphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));
                    Glide.with(context).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_complain);
                }


                String status = complaintsItem.getCptstatus();

                if (status.equalsIgnoreCase(CPTSTATUS_In_Process)) {
                    ((MyViewHolder) holder).img_btn_reminder.setVisibility(View.VISIBLE);

                    ((MyViewHolder) holder).img_btn_delete.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_edit.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_reopen.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_feedback.setVisibility(View.GONE);

                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.in_progress));
//                Glide.with(context).load(R.drawable.complete)
//                        .apply(RequestOptions.centerCropTransform())
//                        .into(((MyViewHolder) holder).img_status);

                } else if (status.equalsIgnoreCase(CPTSTATUS_Complete)) {
                    ((MyViewHolder) holder).img_btn_reopen.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_btn_feedback.setVisibility(View.VISIBLE);

                    ((MyViewHolder) holder).img_btn_reminder.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_delete.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_edit.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.complete));
//                Glide.with(context).load(R.drawable.in_progress)
//                        .apply(RequestOptions.centerCropTransform())
//                        .into(((MyViewHolder) holder).img_status);

                } else if (status.equalsIgnoreCase(CPTSTATUS_New)) {
                    ((MyViewHolder) holder).img_btn_delete.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_btn_edit.setVisibility(View.VISIBLE);

                    ((MyViewHolder) holder).img_btn_reopen.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_feedback.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_reminder.setVisibility(View.GONE);

                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.new_complaint));
                } else if (status.equalsIgnoreCase(CPTSTATUS_Pending)) {
                    ((MyViewHolder) holder).img_btn_delete.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_btn_edit.setVisibility(View.VISIBLE);

                    ((MyViewHolder) holder).img_btn_reopen.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_feedback.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_btn_reminder.setVisibility(View.GONE);

                    ((MyViewHolder) holder).img_status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pending));
//                Glide.with(context).load(R.drawable.)
//                        .apply(RequestOptions.centerCropTransform())
//                        .into(((MyViewHolder) holder).);
                }

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_error);
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_complainNo, tv_com_date_time, tv_complain_title, tv_psName, tv_com_assign, tv_com_Mno, tv_com_emailId;
        ImageView img_status, img_btn_reopen, img_btn_feedback, img_btn_edit, img_btn_delete, img_btn_reminder;
        public Context context;
        CircularImageView img_complain;
        RelativeLayout rl_top;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);
            tv_complainNo = (TextView) view.findViewById(R.id.tv_complainNo);
            tv_com_date_time = (TextView) view.findViewById(R.id.tv_com_date_time);
            tv_complain_title = (TextView) view.findViewById(R.id.tv_complain_title);
            tv_psName = (TextView) view.findViewById(R.id.tv_psName);
            tv_com_assign = (TextView) view.findViewById(R.id.tv_com_assign);
            tv_com_Mno = (TextView) view.findViewById(R.id.tv_com_Mno);
//            tv_com_emailId = (TextView) view.findViewById(R.id.tv_com_emailId);

            img_status = (ImageView) view.findViewById(R.id.img_status);
            img_complain = (CircularImageView) view.findViewById(R.id.img_complain);

            img_btn_reopen = (ImageView) view.findViewById(R.id.img_btn_reopen);
            img_btn_feedback = (ImageView) view.findViewById(R.id.img_btn_feedback);
            img_btn_edit = (ImageView) view.findViewById(R.id.img_btn_edit);
            img_btn_delete = (ImageView) view.findViewById(R.id.img_btn_delete);
            img_btn_reminder = (ImageView) view.findViewById(R.id.img_btn_reminder);

            img_btn_reopen.setOnClickListener(this);
            img_btn_feedback.setOnClickListener(this);
            img_btn_edit.setOnClickListener(this);
            img_btn_delete.setOnClickListener(this);
            img_btn_reminder.setOnClickListener(this);
            rl_top.setOnClickListener(this);

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