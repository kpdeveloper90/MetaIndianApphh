package com.ecosense.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.ecosense.app.R;

/**
 * Created by MITS on 06/10/2017.
 */

public class DropDownStringAdapter extends ArrayAdapter<String> {

    int textViewResourceId;
    Activity context;
    List<String> list;
    LayoutInflater inflater;

    public DropDownStringAdapter(Activity context, int textViewResourceId, int id, List<String> list) {
        super(context, id, list);

        /********** Take passed values **********/
        this.context = context;
        this.list = list;
        this.textViewResourceId = textViewResourceId;

        /***********  Layout inflator to call external xml layout () **********************/
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return super.getView(position, convertView, parent);


        View itemView = inflater.inflate(textViewResourceId, parent, false);

//        ImageView imgPriority = (ImageView) itemView.findViewById(R.id.imgPriority);
//        imgPriority.setImageResource(list.get(position).getPriorityImageId());

        TextView tvPriorityText = (TextView) itemView.findViewById(R.id.tv_name);
        tvPriorityText.setText(list.get(position));

        // Setting the color of the text
        tvPriorityText.setTextColor(Color.rgb(114, 114, 114));


      /*  // Setting Special atrributes for 1st element
        if (position == 0) {
            // Removing the image view
            imgPriority.setVisibility(View.GONE);

            // Setting the size of the text
            tvPriorityText.setTextSize(14f);

            // Setting the text Color
            tvPriorityText.setTextColor(Color.GRAY);

        }*/
        return itemView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
