package com.ecosense.app.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.potyvideo.library.AndExoPlayerView;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.List;
import com.ecosense.app.R;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Message;

public class MessageAdapter extends RecyclerView.Adapter {

    private List messageList;
    private Context context;
    public static final int SENDER = 0;
    public static final int RECEIVER = 1;
    public static final int ARTICAL = 3;
    public static final int VIDEO = 4;
    UserSessionManger session = null;

    public MessageAdapter(Context context, List messages) {
        messageList = messages;
        this.context = context;
        session = new UserSessionManger(this.context);
    }

    public static class ViewHolderVideo extends RecyclerView.ViewHolder {
        public AndExoPlayerView howtos_videoplayer;

        public ViewHolderVideo(LinearLayout v) {
            super(v);
            howtos_videoplayer = v.findViewById(R.id.andExoPlayerView);

        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(LinearLayout v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text_message_body);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout) v);
            return vh;
        } else if (viewType == 3) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artical_howtos, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout) v);
            return vh;
        } else if (viewType == 4) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_how_tos_video, parent, false);
            ViewHolderVideo vh = new ViewHolderVideo((LinearLayout) v);
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout) v);
            return vh;
        }
    }

    public void remove(int pos) {
        int position = pos;
        messageList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, messageList.size());

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (messageList != null && 0 <= position && position < messageList.size()) {
            final Message messages = (Message) messageList.get(position);
//            Log.e("Ad video ", "getItemViewType = " + holder.getItemViewType());

            if (holder.getItemViewType() == 4) {
                if (messages.getMessage() != null) {

//                    Glide.with(context).load(R.drawable.img_default_how_to_video)
//                            .apply(RequestOptions.centerCropTransform())
//                            .into(((ViewHolderVideo) holder).howtos_videoplayer.thumbImageView);

//                    String url = session.getMyServerIP() + complaintsItem.getCptphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));
                    try {
                        Log.e("Ad video ", "" + session.getMyServerIP() + messages.getMessage().replaceAll(" ", "%20"));
//                        Uri uri = Uri.parse(session.getMyServerIP() + messages.getMessage().replaceAll(" ", "%20"));
                        ((ViewHolderVideo) holder).howtos_videoplayer.
                                setSource(session.getMyServerIP() + messages.getMessage().replaceAll(" ", "%20"));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    TastyToast.makeText(context, context.getString(R.string.video_not_availble), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            } else {
                ((ViewHolder) holder).mTextView.setText(messages.getMessage());
            }


//            ((ViewHolder) holder).mTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    remove(position);
//                }
//            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message messages = (Message) messageList.get(position);

        if (messages.getSenderName().equals("Admin")) {
            return SENDER;
        } else if (messages.getSenderName().equals("Artical")) {
            return ARTICAL;
        } else if (messages.getSenderName().equals("Video")) {
            return VIDEO;
        } else {
            return RECEIVER;
        }

    }

}