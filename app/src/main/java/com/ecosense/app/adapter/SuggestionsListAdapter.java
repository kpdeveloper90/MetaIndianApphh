package com.ecosense.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.devs.readmoreoption.ReadMoreOption;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Suggestions;

public class SuggestionsListAdapter extends RecyclerView.Adapter {
    private List<Suggestions> suggestionsList;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;

    private static final int EMPTY_VIEW = 10;

    public SuggestionsListAdapter(Context context, List<Suggestions> suggestionsList) {
        this.suggestionsList = suggestionsList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_suggestions_list_row, parent, false);
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
            if (suggestionsList != null && 0 <= position && position < suggestionsList.size()) {

                final Suggestions suggestionsItem = (Suggestions) suggestionsList.get(position);

                ((MyViewHolder) holder).tv_sug_date_time.setText(dateFormat(suggestionsItem.getSugdate()));
                ((MyViewHolder) holder).tv_sug_uName.setText(suggestionsItem.getSuguname());


                ReadMoreOption readMoreOption = new ReadMoreOption.Builder(context)
                        .textLength(150)
                        .moreLabel("Read More")
                        .lessLabel("Read Less")
                        .moreLabelColor(ContextCompat.getColor(context, R.color.grey_400))
                        .lessLabelColor(ContextCompat.getColor(context, R.color.card3))
                        .labelUnderLine(true)
                        .build();

                readMoreOption.addReadMoreTo(((MyViewHolder) holder).tv_sug_dec, suggestionsItem.getSuggestion());

                if (suggestionsItem.getSugphoto() == null || suggestionsItem.getSugphoto() == "") {

//                Log.e("Ad", "f" + session.getpsName().substring(0, 1));

                    ((MyViewHolder) holder).img_text_sug.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_sug.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_text_sug.setImageDrawable(getCharacterImage(suggestionsItem.getSuguname().substring(0, 1)));

//                Glide.with(context).load(drawable)
//                        .apply(RequestOptions.centerCropTransform())
//                        .into(((MyViewHolder) holder).img_sug);
                } else {

//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));
                    ((MyViewHolder) holder).img_sug.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_text_sug.setVisibility(View.GONE);
                    try {
                        if (suggestionsItem.getSugphoto() == null || suggestionsItem.getSugphoto().equals("")) {
                            Glide.with(context).load(R.drawable.ic_user_1)
                                    .apply(RequestOptions.centerCropTransform())
                                    .into(((MyViewHolder) holder).img_sug);
                        } else {
                            Glide.with(context).load(Connection.decodeFromBase64ToBitmap(suggestionsItem.getSugphoto()))
                                    .apply(RequestOptions.centerCropTransform())
                                    .into(((MyViewHolder) holder).img_sug);
                        }
                    } catch (Exception ignored) {

                    }

                }
            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.please_wait));
        }
    }

    public Drawable getCharacterImage(String s) {
        ColorGenerator generator = ColorGenerator.DEFAULT; // or use DEFAULT
// generate random color
        int color1 = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(3) /* thickness in px */
//                .textColor(Color.BLACK)
//                .useFont(Typeface.DEFAULT)
//                .fontSize(30) /* size in px */
//                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(s, color1);


        return drawable;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tv_sug_dec, tv_sug_date_time, tv_sug_uName;
        ImageView img_btn_sug_share, img_text_sug;
        CircularImageView img_sug;
        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();

            tv_sug_dec = (TextView) view.findViewById(R.id.tv_sug_dec);
            tv_sug_uName = (TextView) view.findViewById(R.id.tv_sug_uName);
            tv_sug_date_time = (TextView) view.findViewById(R.id.tv_sug_date_time);

            img_sug = (CircularImageView) view.findViewById(R.id.img_sug);
            img_text_sug = (ImageView) view.findViewById(R.id.img_text_sug);

            img_btn_sug_share = (ImageView) view.findViewById(R.id.img_btn_sug_share);

            img_btn_sug_share.setOnClickListener(this);

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
        if (suggestionsList.size() == 0) {
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
        return suggestionsList.size() > 0 ? suggestionsList.size() : 1;
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

    public void setFilter(List<Suggestions> suggestionslist) {
        suggestionsList = new ArrayList<>();
        suggestionsList.addAll(suggestionslist);
        notifyDataSetChanged();
    }
}