package com.ecosense.app.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

public class AssetsTrackersListAdapter extends RecyclerView.Adapter {
    private List<Assets> trackersList;
    private Context context;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public AssetsTrackersListAdapter(Context context, List<Assets> trackersList) {
        this.trackersList = trackersList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_bin_status_, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof MyViewHolder) {
                if (trackersList != null && 0 <= position && position < trackersList.size()) {

                    final Assets complaintsItem = (Assets) trackersList.get(position);

                    ((MyViewHolder) holder).tv_assets_name.setText(complaintsItem.getAsset_name());
                    ((MyViewHolder) holder).tv_assets_loc.setText(complaintsItem.getFalocation());

                    ((MyViewHolder) holder).tv_assets_status.setText(complaintsItem.getFastatus());
                    ((MyViewHolder) holder).tv_wardNumber.setText(complaintsItem.getFawardno());
                    ((MyViewHolder) holder).tv_type.setText(complaintsItem.getItem_code());
                    if (complaintsItem.getModified() != null) {
                        ((MyViewHolder) holder).tv_assets_dateTime.setText(dateFormat(complaintsItem.getModified()));
                    } else {
//                        ((MyViewHolder) holder).tv_assets_dateTime.setText(dateFormat(Connection.getCurrentDateTime()));
                        ((MyViewHolder) holder).tv_assets_dateTime.setText("");
                    }

                    if (complaintsItem.getDa_routeid() != null) {
                        ((MyViewHolder) holder).tv_route_No.setText(complaintsItem.getDa_routeid());
                    } else {
                        ((MyViewHolder) holder).tv_route_No.setText(" -");

                    }
                    if (complaintsItem.getFacapcty() != null) {
                        ((MyViewHolder) holder).tv_assets_lavel.setText(complaintsItem.getFacapcty());
                    } else {
                        ((MyViewHolder) holder).tv_assets_lavel.setText("-");
                    }
                    if (complaintsItem.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Clean)) {
                        ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.complete));
                        ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                    } else if (complaintsItem.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Scheduled)) {
                        ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.in_progress));
                        ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.in_progress));
                    } else if (complaintsItem.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Pending)) {
                        ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.pending));
                        ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.pending));
                    }

                    if (complaintsItem.getFabinimage() == null) {
                        Glide.with(context).load(R.drawable.default_image)
                                .apply(RequestOptions.centerCropTransform())
                                .into(((MyViewHolder) holder).img_assets);
                    } else {
                        String url = session.getMyServerIP() + complaintsItem.getFabin_icon();
                        Glide.with(context).load(url)
                                .apply(RequestOptions.centerCropTransform())
                                .into(((MyViewHolder) holder).img_assets);
                    }
//            if (empRegItem.getEmp_active() == 0) {
//                ((MyViewHolder) holder).tv_regDate.setText("New Registration");
//            } else {
//                ((MyViewHolder) holder).tv_regDate.setText("Re. Date : " + empRegItem.getR_date_time());
//            }
                }
            } else if (holder instanceof EmptyViewHolder) {
                ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
                ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_pull_to_refresh);
            }
        } catch (Exception e) {
            Log.e("Exception", "Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_type, tv_route_No, tv_wardNumber, tv_assets_name, tv_assets_dateTime, tv_assets_status, tv_assets_loc, tv_assets_lavel, tv_snake_count, tv_snake_tag;
        CircularImageView img_assets;

        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_assets_name = (TextView) view.findViewById(R.id.tv_assets_name);
            tv_assets_loc = (TextView) view.findViewById(R.id.tv_assets_loc);
            tv_assets_status = (TextView) view.findViewById(R.id.tv_assets_status);
            tv_assets_dateTime = (TextView) view.findViewById(R.id.tv_assets_dateTime);

            tv_route_No = view.findViewById(R.id.tv_route_No);
            tv_assets_lavel = (TextView) view.findViewById(R.id.tv_assets_lavel);
            tv_wardNumber = (TextView) view.findViewById(R.id.tv_wardNumber);


            tv_type = (TextView) view.findViewById(R.id.tv_type);


            img_assets = (CircularImageView) view.findViewById(R.id.img_assets);

        }

        @Override
        public void onClick(View view) {

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (trackersList.size() == 0) {
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
//        return this.trackersList.size();
        return trackersList.size() > 0 ? trackersList.size() : 1;
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

    public void setFilter(List<Assets> listItems) {
        trackersList = new ArrayList<>();
        trackersList.addAll(listItems);
        notifyDataSetChanged();
    }
}