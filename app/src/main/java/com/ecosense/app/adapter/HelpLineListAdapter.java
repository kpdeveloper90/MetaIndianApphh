package com.ecosense.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.HelplineData;

public class HelpLineListAdapter extends RecyclerView.Adapter {
    private List<HelplineData> helplineList;
    private Context context;
    private static final int EMPTY_VIEW = 10;
    UserSessionManger session = null;
    public HelpLineListAdapter(Context context, List<HelplineData> helplineList) {
        this.helplineList = helplineList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_helpline_custom_row, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {

        if (helplineList != null && 0 <= position && position < helplineList.size()) {

            final HelplineData helplineItem = (HelplineData) helplineList.get(position);
//
            ((MyViewHolder) holder).tv_helplineName.setText(helplineItem.getHelplineName());

            if (helplineItem.getHelplineIcond() == null) {
                Glide.with(context).load(R.drawable.default_image)
                        .apply(RequestOptions.centerCropTransform())
                        .into(((MyViewHolder) holder).img_icon_helpline);
            } else {
                String url = session.getMyServerIP() + helplineItem.getHelplineIcond();
                Glide.with(context).load(url)
                        .apply(RequestOptions.centerCropTransform())
                        .into(((MyViewHolder) holder).img_icon_helpline);
            }
        }
    } else if (holder instanceof EmptyViewHolder) {
        ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
        ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_pull_to_refresh);
    }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_helplineName;
        ImageView img_icon_helpline;

        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_helplineName = (TextView) view.findViewById(R.id.tv_helplineName);
            img_icon_helpline = (ImageView) view.findViewById(R.id.img_icon_helpline);

        }

        @Override
        public void onClick(View view) {

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (helplineList.size() == 0) {
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
        return helplineList.size() > 0 ? helplineList.size() : 1;
    }

    public void setFilter(List<HelplineData> helplinelist) {
        helplineList = new ArrayList<>();
        helplineList.addAll(helplinelist);
        notifyDataSetChanged();
    }
}