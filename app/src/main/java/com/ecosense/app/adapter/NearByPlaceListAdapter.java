package com.ecosense.app.adapter;

import android.content.Context;
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
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.NearByPlace;

public class NearByPlaceListAdapter extends RecyclerView.Adapter {
    private List<NearByPlace> nearByPlaceList;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;

    public NearByPlaceListAdapter(Context context, List<NearByPlace> nearByPlaceList) {
        this.nearByPlaceList = nearByPlaceList;
        this.context = context;
        session = new UserSessionManger(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_nearby_place_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (nearByPlaceList != null && 0 <= position && position < nearByPlaceList.size()) {

            final NearByPlace nearByPlace = (NearByPlace) nearByPlaceList.get(position);
//
            if (nearByPlace.getRating()== null) {
                ((MyViewHolder) holder).ratingBar_nrb.setText(context.getString(R.string.rating_tag)+"0");
            } else {
                ((MyViewHolder) holder).ratingBar_nrb.setText(context.getString(R.string.rating_tag)+ nearByPlace.getRating());
            }

            ((MyViewHolder) holder).tv_nrb_placeName.setText(nearByPlace.getName());
            ((MyViewHolder) holder).tv_tv_nrb_placeAddress.setText(nearByPlace.getVicinity());

            if (nearByPlace.getIcon() == null) {
                Glide.with(context).load(R.drawable.default_image)
                        .apply(RequestOptions.centerCropTransform())
                        .into(((MyViewHolder) holder).img_nrb_placeIcon);
            } else {
//                String url = session.getMyServerIP() + nearByPlace.getCptphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));

                Glide.with(context).load(nearByPlace.getIcon())
                        .apply(RequestOptions.centerCropTransform())
                        .into(((MyViewHolder) holder).img_nrb_placeIcon);
            }

        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_nrb_placeName, tv_tv_nrb_placeAddress;
        ImageView img_btn_nrb_place_diraction;
        public Context context;
        CircularImageView img_nrb_placeIcon;
        RelativeLayout rl_top;
        TextView ratingBar_nrb;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();

            ratingBar_nrb = (TextView) view.findViewById(R.id.ratingBar_nrb);
            tv_nrb_placeName = (TextView) view.findViewById(R.id.tv_nrb_placeName);
            tv_tv_nrb_placeAddress = (TextView) view.findViewById(R.id.tv_tv_nrb_placeAddress);

            img_nrb_placeIcon = (CircularImageView) view.findViewById(R.id.img_nrb_placeIcon);
            img_btn_nrb_place_diraction = (ImageView) view.findViewById(R.id.img_btn_nrb_place_diraction);

            img_btn_nrb_place_diraction.setOnClickListener(this);

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
        return this.nearByPlaceList.size();
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

    public void setFilter(List<NearByPlace> complaintsListItems) {
        nearByPlaceList = new ArrayList<>();
        nearByPlaceList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}