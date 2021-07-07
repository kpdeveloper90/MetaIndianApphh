package com.ecosense.app.adapter;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flaviofaria.kenburnsview.KenBurnsView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.News;

public class NewsListAdapter extends RecyclerView.Adapter {
    private List<News> newsList;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public NewsListAdapter(Context context, List<News> newsList) {
        this.newsList = newsList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_event_list_row, parent, false);
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
            if (newsList != null && 0 <= position && position < newsList.size()) {

                final News eventItem = (News) newsList.get(position);
//
//            ((MyViewHolder) holder).tv_count_socialThumbUp.setText(eventItem.getNwlike());
//            ((MyViewHolder) holder).tv_count_socialThumbDown.setText(eventItem.getNwdislike());
                ((MyViewHolder) holder).tv_DateTime.setText(dateFormat(eventItem.getNwpftime()));
                ((MyViewHolder) holder).tv_title.setText(eventItem.getNwtitle());

                if (eventItem.getNwphoto() == null) {
                    Glide.with(context)
                            .load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_event);
                } else {
                    String url = session.getMyServerIP() + eventItem.getNwphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));

                    Glide.with(context)
                            .load(url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_event);
                }

                ((MyViewHolder) holder).img_btn_SocialThumbUp.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_socialThumbDown.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_share.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).img_btn_favorite.setVisibility(View.GONE);
                ((MyViewHolder) holder).tv_count_socialThumbUp.setVisibility(View.GONE);
                ((MyViewHolder) holder).tv_count_socialThumbDown.setVisibility(View.GONE);

                ((MyViewHolder) holder).img_btn_volunteer.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_directions.setVisibility(View.GONE);
                ((MyViewHolder) holder).img_btn_addEventInCalendar.setVisibility(View.GONE);

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        KenBurnsView img_event;
        TextView tv_title, tv_DateTime, tv_count_socialThumbUp, tv_count_socialThumbDown;
        ImageView img_btn_volunteer, img_btn_SocialThumbUp, img_btn_socialThumbDown,
                img_btn_directions, img_btn_share, img_btn_favorite, img_btn_addEventInCalendar;
        public Context context;
        CardView card_view;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_count_socialThumbDown = (TextView) view.findViewById(R.id.tv_count_socialThumbDown);
            tv_count_socialThumbUp = (TextView) view.findViewById(R.id.tv_count_socialThumbUp);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_DateTime = (TextView) view.findViewById(R.id.tv_DateTime);

            img_event = (KenBurnsView) view.findViewById(R.id.img_event);
            img_btn_volunteer = (ImageView) view.findViewById(R.id.img_btn_volunteer);
            img_btn_SocialThumbUp = (ImageView) view.findViewById(R.id.img_btn_SocialThumbUp);
            img_btn_socialThumbDown = (ImageView) view.findViewById(R.id.img_btn_socialThumbDown);
            img_btn_directions = (ImageView) view.findViewById(R.id.img_btn_directions);
            img_btn_share = (ImageView) view.findViewById(R.id.img_btn_share);
            img_btn_favorite = (ImageView) view.findViewById(R.id.img_btn_favorite);
            img_btn_addEventInCalendar = (ImageView) view.findViewById(R.id.img_btn_addEventInCalendar);
            card_view = (CardView) view.findViewById(R.id.card_view);

            img_btn_volunteer.setOnClickListener(this);
            img_btn_SocialThumbUp.setOnClickListener(this);
            img_btn_socialThumbDown.setOnClickListener(this);
            img_btn_directions.setOnClickListener(this);
            img_btn_share.setOnClickListener(this);
            img_btn_favorite.setOnClickListener(this);
            img_btn_addEventInCalendar.setOnClickListener(this);
            img_event.setOnClickListener(this);


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
        if (newsList.size() == 0) {
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
        return newsList.size() > 0 ? newsList.size() : 1;
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

    public void setFilter(List<News> complaintsListItems) {
        newsList = new ArrayList<>();
        newsList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}