package com.ecosense.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Base64;
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
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;

public class RouteMapListAdapter extends RecyclerView.Adapter {
    private List<RouteInfo> routeInfoList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;

    public RouteMapListAdapter(Context context, List<RouteInfo> routeInfoList) {
        this.routeInfoList = routeInfoList;
        this.context = context;
        session = new UserSessionManger(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_routemap, parent, false);

        return new MyViewHolder(itemView);
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (routeInfoList != null && 0 <= position && position < routeInfoList.size()) {
            final RouteInfo routeInfo = routeInfoList.get(position);

//
            ((MyViewHolder) holder).tv_placeType.setText(routeInfo.getRoute_assetname());
            ((MyViewHolder) holder).tv_assets_location.setText(routeInfo.getRoute_assetloc());

            ((MyViewHolder) holder).tv_assets_status.setText(routeInfo.getClean_status());
            String binStatus = routeInfo.getClean_status();


            if (binStatus.equalsIgnoreCase(AppConfig.BIN_Status_Clean)) {
                if (routeInfo.getRoute_binphoto() == null) {
                    Glide.with(context).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                } else {
//                    String url = session.getMyServerIP() + metaRouteInfo.getRoute_icon();
                    Bitmap bin_pic=decodeFromBase64ToBitmap(routeInfo.getRoute_binphoto());
                    Glide.with(context).load(bin_pic)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                }
                ((MyViewHolder) holder).img_placeIcon.setBorderColor(ContextCompat.getColor(context, R.color.complete));
                ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                ((MyViewHolder) holder).tv_date.setText(dateFormat(routeInfo.getModified()));

                ((MyViewHolder) holder).img_btn_scan_QR.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_reason.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_place_diraction.setVisibility(View.GONE);

            } else if (binStatus.equalsIgnoreCase(AppConfig.BIN_Status_Scheduled)) {
                if (routeInfo.getRoute_icon() == null) {
                    Glide.with(context).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                } else {
                    String url = session.getMyServerIP() + routeInfo.getRoute_icon();
                    Glide.with(context).load(url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                }
                ((MyViewHolder) holder).img_placeIcon.setBorderColor(ContextCompat.getColor(context, R.color.in_progress));
                ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.in_progress));


                ((MyViewHolder) holder).img_btn_scan_QR.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).img_btn_reason.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).img_btn_place_diraction.setVisibility(View.VISIBLE);
            }else{
                if (routeInfo.getRoute_icon() == null) {
                    Glide.with(context).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                } else {
                    String url = session.getMyServerIP() + routeInfo.getRoute_icon();
                    Glide.with(context).load(url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_placeIcon);
                }
                ((MyViewHolder) holder).img_placeIcon.setBorderColor(ContextCompat.getColor(context, R.color.pending));
                ((MyViewHolder) holder).tv_assets_status.setTextColor(ContextCompat.getColor(context, R.color.pending));


                ((MyViewHolder) holder).img_btn_scan_QR.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).img_btn_reason.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).img_btn_place_diraction.setVisibility(View.VISIBLE);
            }

        }
    }
    private Bitmap decodeFromBase64ToBitmap(String base64String) {
        String base64Image = base64String.split(",")[1];
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_placeType, tv_assets_status, tv_assets_location, tv_date;
        ImageView img_btn_scan_QR, img_btn_reason, img_btn_place_diraction;

        public Context context;
        CircularImageView img_placeIcon;
        RelativeLayout rl_top;
        TextView ratingBar_nrb;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            img_placeIcon = view.findViewById(R.id.img_placeIcon);

            tv_placeType = view.findViewById(R.id.tv_placeType);
            tv_assets_status = view.findViewById(R.id.tv_assets_status);
            tv_assets_location = view.findViewById(R.id.tv_assets_location);
            tv_date = view.findViewById(R.id.tv_date);
            img_btn_scan_QR = view.findViewById(R.id.img_btn_scan_QR);
            img_btn_reason = view.findViewById(R.id.img_btn_reason);
            img_btn_place_diraction = view.findViewById(R.id.img_btn_place_diraction);


            img_btn_scan_QR.setOnClickListener(this);
            img_btn_reason.setOnClickListener(this);
            img_btn_place_diraction.setOnClickListener(this);

            itemView.setTag(view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return this.routeInfoList.size();
//        return 10;
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