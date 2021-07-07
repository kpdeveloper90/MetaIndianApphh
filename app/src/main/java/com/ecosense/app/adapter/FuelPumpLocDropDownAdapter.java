package com.ecosense.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.pojo.Fuel;

/**
 * Created by MITS on 06/10/2017.
 */

public class FuelPumpLocDropDownAdapter extends BaseAdapter {

    Context c;
    List<Fuel> list;

    public FuelPumpLocDropDownAdapter(Context context, List<Fuel> list) {
        super();
        this.c = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Fuel fuel_info = list.get(position);
        LayoutInflater inflater = ((Activity) c).getLayoutInflater();
        View row = inflater.inflate(R.layout.custom_dropdown_list_row, parent, false);


        TextView label = row.findViewById(R.id.tv_name);
        label.setText(fuel_info.getPump_name());


//        TextView sub = (TextView) row.findViewById(R.id.sub);
//        sub.setText(cur_obj.getSub());
//        ImageView icon = (ImageView) row.findViewById(R.id.image);
//        icon.setImageResource(cur_obj.getImage_id());

        return row;
    }
}






















//
//        ArrayAdapter<String> {
//
//    int textViewResourceId;
//    Activity context;
//    List<Fuel> list;
//    LayoutInflater inflater;
//
//    public FuelPumpLocDropDownAdapter(Activity context, int textViewResourceId, int id, List<String> list) {
//        super(context, id, list);
//
//        /********** Take passed values **********/
//        this.context = context;
//        this.list = list;
//        this.textViewResourceId = textViewResourceId;
//
//        /***********  Layout inflator to call external xml layout () **********************/
//        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    }
//
//    @NonNull
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // return super.getView(position, convertView, parent);
//
//
//        View itemView = inflater.inflate(textViewResourceId, parent, false);
//
////        ImageView imgPriority = (ImageView) itemView.findViewById(R.id.imgPriority);
////        imgPriority.setImageResource(list.get(position).getPriorityImageId());
//
//        TextView tvPriorityText = (TextView) itemView.findViewById(R.id.tv_name);
//        tvPriorityText.setText(list.get(position).getPump_address());
//
//        // Setting the color of the text
//        tvPriorityText.setTextColor(Color.rgb(114, 114, 114));
//
//
//      /*  // Setting Special atrributes for 1st element
//        if (position == 0) {
//            // Removing the image view
//            imgPriority.setVisibility(View.GONE);
//
//            // Setting the size of the text
//            tvPriorityText.setTextSize(14f);
//
//            // Setting the text Color
//            tvPriorityText.setTextColor(Color.GRAY);
//
//        }*/
//        return itemView;
//    }
//
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        return getView(position, convertView, parent);
//    }
//
//}
