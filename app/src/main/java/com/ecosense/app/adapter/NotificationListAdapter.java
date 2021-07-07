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
import com.devs.readmoreoption.ReadMoreOption;
import com.github.vipulasri.timelineview.TimelineView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.pojo.NotificationItem;

public class NotificationListAdapter extends RecyclerView.Adapter {
    private List<NotificationItem> notificationList;
    private Context context;
    private static final int EMPTY_VIEW = 10;

    public NotificationListAdapter(Context context, List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
        this.context = context;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == EMPTY_VIEW) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_empty_view, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(itemView);
            return evh;
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_notification_list_, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

        if (notificationList != null && 0 <= position && position < notificationList.size()) {

            final NotificationItem notification = (NotificationItem) notificationList.get(position);
//
            ((MyViewHolder) holder).tv_noti_title.setText( notification.getNftdoctypename());


            ReadMoreOption readMoreOption = new ReadMoreOption.Builder(context)
                    .textLength(100)
                    .moreLabel("Read More")
                    .lessLabel("Read Less")
                    .moreLabelColor(ContextCompat.getColor(context, R.color.grey_400))
                    .lessLabelColor(ContextCompat.getColor(context, R.color.card3))
                    .labelUnderLine(true)
                    .build();

            readMoreOption.addReadMoreTo(((MyViewHolder) holder).tv_noti_text2, notification.getNftdesc());


            ((MyViewHolder) holder).tv_noti_dateTime.setVisibility(View.GONE);
//            ((MyViewHolder) holder).tv_noti_dateTime.setText( dateFormat(notification.getNftdatetime()));
            ((MyViewHolder) holder).img_noti_icon.setImageDrawable(getCharacterImage(notification.getNftdoctypename().substring(0, 1)));

//
//            if (empRegItem.getEmp_active() == 0) {
//                ((MyViewHolder) holder).tv_regDate.setText("New Registration");
//            } else {
//                ((MyViewHolder) holder).tv_regDate.setText("Re. Date : " + empRegItem.getR_date_time());
//            }
        }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }

    }

    public Drawable getCharacterImage(String s) {
        ColorGenerator generator = ColorGenerator.DEFAULT; // or use DEFAULT
// generate random color
        int color1 = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
//                .withBorder(3) /* thickness in px */
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


        TextView tv_noti_title,tv_noti_text2,tv_noti_dateTime;
     ImageView img_noti_icon;
        public Context context;

        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_noti_dateTime = (TextView) view.findViewById(R.id.tv_noti_dateTime);
            tv_noti_text2 = (TextView) view.findViewById(R.id.tv_noti_text2);
            tv_noti_title = (TextView) view.findViewById(R.id.tv_noti_title);

            img_noti_icon = (ImageView) view.findViewById(R.id.img_noti_icon);

        }

        @Override
        public void onClick(View view) {

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (notificationList.size() == 0) {
            return EMPTY_VIEW;
        }
        return TimelineView.getTimeLineViewType(position,getItemCount());
////        return super.getItemViewType(position);
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
        return notificationList.size() > 0 ? notificationList.size() : 1;
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

    public void setFilter(List<NotificationItem> listItems) {
        notificationList = new ArrayList<>();
        notificationList.addAll(listItems);
        notifyDataSetChanged();
    }
}