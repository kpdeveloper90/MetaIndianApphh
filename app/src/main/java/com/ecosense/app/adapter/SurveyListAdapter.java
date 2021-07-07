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
import com.flaviofaria.kenburnsview.KenBurnsView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Survey;

public class SurveyListAdapter extends RecyclerView.Adapter {
    private List<Survey> surveyList;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public SurveyListAdapter(Context context, List<Survey> surveyList) {
        this.surveyList = surveyList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_survey_list_row, parent, false);
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
            if (surveyList != null && 0 <= position && position < surveyList.size()) {

                final Survey surveyItem = (Survey) surveyList.get(position);
//

                ((MyViewHolder) holder).tv_sur_authName.setText(context.getString(R.string.survey_taken_by) + surveyItem.getSurauthor());
                ((MyViewHolder) holder).tv_sur_authName.setVisibility(View.GONE);
                ((MyViewHolder) holder).tv_sur_title.setText(surveyItem.getSurtitle());

                if (surveyItem.getSurpttime() != null) {
                    ((MyViewHolder) holder).tv_sur_date_time.setText(dateFormat(surveyItem.getSurpttime()));
                }else {
                    ((MyViewHolder) holder).tv_sur_date_time.setText("");
                }
                if (surveyItem.getSurphoto() == null) {
                    Glide.with(context).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_survey);
                } else {
                    String url = session.getMyServerIP() + surveyItem.getSurphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));

                    Glide.with(context).load(url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(((MyViewHolder) holder).img_survey);
                }


            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        KenBurnsView img_survey;
        TextView tv_sur_title, tv_sur_authName, tv_sur_date_time;
        ImageView img_btn_start_survey;
        public Context context;


        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_sur_title = (TextView) view.findViewById(R.id.tv_sur_title);
            tv_sur_authName = (TextView) view.findViewById(R.id.tv_sur_authName);
            tv_sur_date_time = (TextView) view.findViewById(R.id.tv_sur_date_time);

            img_survey = (KenBurnsView) view.findViewById(R.id.img_survey);
            img_btn_start_survey = (ImageView) view.findViewById(R.id.img_btn_start_survey);

            img_btn_start_survey.setOnClickListener(this);

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
        if (surveyList.size() == 0) {
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
        return surveyList.size() > 0 ? surveyList.size() : 1;
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

    public void setFilter(List<Survey> complaintsListItems) {
        surveyList = new ArrayList<>();
        surveyList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }
}