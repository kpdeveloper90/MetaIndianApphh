package com.ecosense.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.pojo.PlaceInfo;

public class NearByPlaceTypeListAdapter extends RecyclerView.Adapter {
    private List<PlaceInfo> placeInfoList;
    private Context context;
    private ItemClickListener clickListener;

    public NearByPlaceTypeListAdapter(Context context, List<PlaceInfo> placeInfoList) {
        this.placeInfoList = placeInfoList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_place_type_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (placeInfoList != null && 0 <= position && position < placeInfoList.size()) {

            final PlaceInfo placeInfoItem = (PlaceInfo) placeInfoList.get(position);
//
            ((MyViewHolder) holder).tv_placeType.setText(placeInfoItem.getName());

        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_placeType;
        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_placeType = (TextView) view.findViewById(R.id.tv_placeType);

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
        return this.placeInfoList.size();
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



    public void setFilter(List<PlaceInfo> placeInfolist) {
        placeInfoList = new ArrayList<>();
        placeInfoList.addAll(placeInfolist);
        notifyDataSetChanged();
    }
}