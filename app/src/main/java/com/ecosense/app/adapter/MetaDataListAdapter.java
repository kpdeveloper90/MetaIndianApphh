package com.ecosense.app.adapter;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
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

import android.widget.Filter;
import android.widget.Filterable;

import com.ecosense.app.R;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

public class MetaDataListAdapter extends RecyclerView.Adapter implements Filterable {
    private static final String TAG = MetaDataListAdapter.class.getSimpleName();
    private List<Assets> trackersList;
    private List<Assets> trackersListFilter;
    private Context context;
    private String className;
    UserSessionManger session = null;
    private ItemClickListener clickListener;
    private static final int EMPTY_VIEW = 10;

    public MetaDataListAdapter(Context context, List<Assets> trackersList, String className) {
        this.trackersList = trackersList;
        this.trackersListFilter = trackersList;
        this.context = context;
        this.className = className;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_metadata_list_row_, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {


            if (holder instanceof MyViewHolder) {
                if (trackersList != null && 0 <= position && position < trackersList.size()) {

                    final Assets complaintsItem = (Assets) trackersList.get(position);
//
                    ((MyViewHolder) holder).tv_assets_name.setText(complaintsItem.getAsset_name());

                    ((MyViewHolder) holder).tv_assets_dateTime.setText(dateFormat(complaintsItem.getModified()));
                    ((MyViewHolder) holder).tv_wardNumber.setText(complaintsItem.getFawardno());
                    ((MyViewHolder) holder).tv_type.setText(complaintsItem.getItem_code());

                    if (complaintsItem.getFalocation() != null) {
                        ((MyViewHolder) holder).img_assets_loc.setVisibility(View.VISIBLE);
                        ((MyViewHolder) holder).tv_assets_loc.setVisibility(View.VISIBLE);
                        ((MyViewHolder) holder).tv_assets_loc.setText(complaintsItem.getFalocation());
                    } else {
                        ((MyViewHolder) holder).img_assets_loc.setVisibility(View.GONE);
                        ((MyViewHolder) holder).tv_assets_loc.setVisibility(View.GONE);
                    }


                    if (complaintsItem.getFarouteno() != null) {
                        ((MyViewHolder) holder).tv_assets_route_NO.setText(context.getString(R.string.route_no)+ complaintsItem.getFarouteno());
                        ((MyViewHolder) holder).tv_assets_route_NO.setTextColor(ContextCompat.getColor(context, R.color.grey_800));
                        if (className.equalsIgnoreCase("MyPOICollected")) {
                            ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.complete));
                        }
                    } else {
                        ((MyViewHolder) holder).tv_assets_route_NO.setText(context.getString(R.string.route_no)+"Pending");
                        ((MyViewHolder) holder).tv_assets_route_NO.setTextColor(ContextCompat.getColor(context, R.color.pending));
                        if (className.equalsIgnoreCase("MyPOICollected")) {
                            ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.pending));
                        }
                    }

                    if ( complaintsItem.getFalatitude()==null
                            || complaintsItem.getFalongitude()==null
                            ||complaintsItem.getFalatitude().equalsIgnoreCase("0.0")
                            || (complaintsItem.getFalongitude().equalsIgnoreCase("0.0"))

                    ) {
                        ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.pending));
                        ((MyViewHolder) holder).tv_assets_lang.setText(context.getString(R.string.lati) +context.getString(R.string.na_tag));
                        ((MyViewHolder) holder).tv_assets_long.setText(context.getString(R.string.logi) + context.getString(R.string.na_tag));
                    } else{
                        ((MyViewHolder) holder).img_assets.setBorderColor(ContextCompat.getColor(context, R.color.complete));
                        ((MyViewHolder) holder).tv_assets_lang.setText(context.getString(R.string.lati) + complaintsItem.getFalatitude());
                        ((MyViewHolder) holder).tv_assets_long.setText(context.getString(R.string.logi) + complaintsItem.getFalongitude());
                    }
                    if (complaintsItem.getFabin_icon() == null) {
                        Log.e(TAG, "getFabin_icon null in if ");
                        if (complaintsItem.getFabinimage() == null) {
                            Log.e(TAG, "getFabinimage null in if ");
                            Glide.with(context).load(R.drawable.default_image)
                                    .apply(RequestOptions.centerCropTransform())
                                    .into(((MyViewHolder) holder).img_assets);
                        } else {
                            Log.e(TAG, "getFabinimage not null in else ");
                            Glide.with(context).load(Connection.decodeFromBase64ToBitmap(complaintsItem.getFabinimage()))
                                    .apply(RequestOptions.centerCropTransform())
                                    .into(((MyViewHolder) holder).img_assets);
                        }
                    } else {

                        Log.e(TAG, "getFabin_icon not null in else ");
                        String url = session.getMyServerIP() + complaintsItem.getFabin_icon();
                        Glide.with(context).load(url)
                                .apply(RequestOptions.centerCropTransform())
                                .into(((MyViewHolder) holder).img_assets);
                    }

                }
            } else if (holder instanceof EmptyViewHolder) {
                ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
                ((EmptyViewHolder) holder).img_empty_msg.setText(R.string.no_record_found_pull_to_refresh);
            }
        } catch (
                Exception e) {
            Log.e("Exception", "Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_assets_name, tv_type, tv_wardNumber, tv_assets_route_NO, tv_assets_dateTime, tv_assets_lang, tv_assets_long, tv_assets_loc, tv_assets_lavel, tv_snake_count, tv_snake_tag;
        CircularImageView img_assets;
        ConstraintLayout cl_contain;
        ImageView img_metadata_play_text, img_assets_loc;
        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_assets_name = view.findViewById(R.id.tv_assets_name);
            tv_assets_loc = view.findViewById(R.id.tv_assets_loc);
            img_assets_loc = view.findViewById(R.id.imageView2);

            tv_assets_dateTime = view.findViewById(R.id.tv_assets_dateTime);

            tv_assets_lavel = view.findViewById(R.id.tv_assets_lavel);
            tv_wardNumber = view.findViewById(R.id.tv_wardNumber);
            tv_assets_lang = view.findViewById(R.id.tv_assets_lang);
            tv_assets_long = view.findViewById(R.id.tv_assets_long);
            tv_assets_route_NO = view.findViewById(R.id.tv_assets_route_NO);


            tv_type = view.findViewById(R.id.tv_type);


            img_assets = view.findViewById(R.id.img_assets);

            img_metadata_play_text = view.findViewById(R.id.img_metadata_play_text);
            cl_contain = view.findViewById(R.id.cl_contain);

            img_metadata_play_text.setOnClickListener(this);
            cl_contain.setOnClickListener(this);

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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    trackersList = trackersListFilter;
                } else {
                    List<Assets> filteredList = new ArrayList<>();
                    for (Assets row : trackersListFilter) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getFalocation().toLowerCase().contains(charString.toLowerCase()) || row.getItem_code().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    trackersList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = trackersList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                trackersList = (ArrayList<Assets>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setFilter(List<Assets> listItems) {
        trackersList = new ArrayList<>();
        trackersList.addAll(listItems);
        trackersList = listItems;
        notifyDataSetChanged();
    }
}