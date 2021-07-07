package com.ecosense.app.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import com.ecosense.app.R;
import com.ecosense.app.pojo.Assets;

public class ToilateLocatorAdapter  extends ArrayAdapter<Assets> {

    private Context context;
    private int resourceId;
    private ArrayList<Assets> items, tempItems, suggestions;
    public ToilateLocatorAdapter(@NonNull Context context, int resourceId, ArrayList<Assets> items) {
        super(context, resourceId, items);
        Log.e(" items. ad  "," items.size() "+ items.size());
        this.items = items;
        this.context = context;
        this.resourceId = resourceId;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(resourceId, parent, false);
            }
            Assets fruit = getItem(position);
            TextView name = (TextView) view.findViewById(R.id.tv_placeType);

            name.setText(fruit.getFalocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
    @Nullable
    @Override
    public Assets getItem(int position) {
        return items.get(position);
    }
    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return fruitFilter;
    }
    private Filter fruitFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Assets fruit = (Assets) resultValue;
            return fruit.getFalocation();
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {

                Log.e("FilterResults","charSequence = "+charSequence);
                Log.e("FilterResults","items   = "+items.size());
                Log.e("FilterResults","tempItems  = "+tempItems.size());
                Log.e("publishResults","suggestions  = "+suggestions.size());
                suggestions.clear();
                for (Assets fruit: tempItems) {
                    if (fruit.getFalocation().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        suggestions.add(fruit);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                Log.e(" after FilterResults","items   = "+items.size());
                Log.e("after FilterResults","tempItems  = "+tempItems.size());
                Log.e("after publishResults","suggestions  = "+suggestions.size());
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<Assets> tempValues = (ArrayList<Assets>) filterResults.values;
            if (filterResults != null && filterResults.count > 0) {
                Log.e("publishResults","items   = "+items.size());
                Log.e("publishResults","tempItems  = "+tempItems.size());
                Log.e("publishResults","suggestions  = "+suggestions.size());
                clear();
                for (Assets fruitObj : tempValues) {
                    add(fruitObj);
                    notifyDataSetChanged();
                }
            } else {
                clear();
                notifyDataSetChanged();
            }
        }
    };
}