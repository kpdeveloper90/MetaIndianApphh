package com.ecosense.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.GsonRequest;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.DirectionObject;
import com.ecosense.app.pojo.LegsObject;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteObject;

public class POIDetailAdapter extends RecyclerView.Adapter {
    private static final String TAG = POIDetailAdapter.class.getSimpleName();
    private List<RouteInfo> routeInfoList = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    private static final int EMPTY_VIEW = 10;

    public POIDetailAdapter(Context context, List<RouteInfo> routeInfoList) {
        this.routeInfoList = routeInfoList;
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
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_poi_timeline_row, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView, viewType);
            return vh;
        }

    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            if (routeInfoList != null && 0 <= position && position < routeInfoList.size()) {
                final RouteInfo rInfo = routeInfoList.get(position);

                ((MyViewHolder) holder).tv_vh_route_name.setText(rInfo.getRoute_assetloc());
                ((MyViewHolder) holder).tv_vh_assets_name.setText(rInfo.getRoute_assetname());
                ((MyViewHolder) holder).tv_route_vh_type.setText(rInfo.getType());

                if (rInfo.getRoute_assetname().equalsIgnoreCase("BIN")) {
                    ((MyViewHolder) holder).tv_bin_capecity.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).img_bin_capecity.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).tv_bin_capecity.setText(rInfo.getRoute_assetcap());
                } else {
                    ((MyViewHolder) holder).tv_bin_capecity.setVisibility(View.GONE);
                    ((MyViewHolder) holder).img_bin_capecity.setVisibility(View.GONE);
                }

                String rStatus = rInfo.getClean_status();

                if (rStatus.equalsIgnoreCase(AppConfig.BIN_Status_Clean)) {
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.complete));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getClean_status());
                    // For Observations 06-Aug-19 change point-20 Replace date by time in Route coverrage POI details.
                    ((MyViewHolder) holder).tv_POI_Date.setText(dateFormat(rInfo.getModified()));
                    ((MyViewHolder) holder).tv_eta_time.setText("");
                    ((MyViewHolder) holder).poi_timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.marker),
                            ContextCompat.getColor(context, R.color.complete));
                } else if (rStatus.equalsIgnoreCase(AppConfig.BIN_Status_Scheduled)) {
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.in_progress));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getClean_status());
                    ((MyViewHolder) holder).tv_POI_Date.setText("");
//                    ((MyViewHolder) holder).poi_timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_marker_active));

                    ((MyViewHolder) holder).poi_timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_marker_active),
                            ContextCompat.getColor(context, R.color.in_progress));

                    ((MyViewHolder) holder).tv_eta_time.setText("");
/**
 * get ETA Time by Calling API
 * Referance from
 * https://inducesmile.com/android/android-find-distance-and-duration-between-two-points-on-android-map/

                    LatLng origin = new LatLng(12.2, 31.145);
//                   LatLng destination = new LatLng(,);
                    //use Google Direction API to get the route between these Locations
                    String directionApiPath = Connection.getUrl("22.310672", "73.1681034",
                            rInfo.getRoute_lat(), rInfo.getRoute_long());

//                  String directionApiPath = Helper.getUrl(String.valueOf(origin.latitude), String.valueOf(origin.longitude),
//                    String.valueOf(destination.latitude), String.valueOf(destination.longitude));
                    Log.e(TAG, "Path " + directionApiPath);
//                    ((MyViewHolder) holder).tv_eta_time.setText("0");
                    getDirectionFromDirectionApiServer(directionApiPath, ((MyViewHolder) holder).tv_eta_time);*/

                } else {
                    ((MyViewHolder) holder).tv_route_Status.setTextColor(ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_route_Status.setText(rInfo.getClean_status());
                    ((MyViewHolder) holder).tv_POI_Date.setText("");
                    ((MyViewHolder) holder).poi_timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_marker_active),
                            ContextCompat.getColor(context, R.color.pending));
                    ((MyViewHolder) holder).tv_eta_time.setText("");
                }

            }
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).img_empty_title.setText("Oops!");
            ((EmptyViewHolder) holder).img_empty_msg.setText(context.getString(R.string.no_record_found_pull_to_refresh));
        }

    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_eta_time, tv_vh_assets_name, tv_vh_route_name, tv_route_vh_type, tv_bin_capecity, tv_route_Status, tv_POI_Date;
        public Context context;
        public TimelineView poi_timeline;
        ImageView img_bin_capecity;

        public MyViewHolder(View view, int viewType) {
            super(view);

            context = view.getContext();


            poi_timeline = view.findViewById(R.id.poi_timeline);
            poi_timeline.initLine(viewType);


            tv_vh_assets_name = view.findViewById(R.id.tv_vh_assets_name);
            tv_eta_time = view.findViewById(R.id.tv_eta_time);
            tv_vh_route_name = view.findViewById(R.id.tv_vh_route_name);
            tv_route_vh_type = view.findViewById(R.id.tv_route_vh_type);
            img_bin_capecity = view.findViewById(R.id.img_bin_capecity);
            tv_bin_capecity = view.findViewById(R.id.tv_bin_capecity);
            tv_route_Status = view.findViewById(R.id.tv_route_Status);
            tv_POI_Date = view.findViewById(R.id.tv_POI_Date);
//            itemView.setTag(view);
//            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (routeInfoList.size() == 0) {
            return EMPTY_VIEW;
        }
        return TimelineView.getTimeLineViewType(position, getItemCount());
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
        return routeInfoList.size() > 0 ? routeInfoList.size() : 1;
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

    public void setFilter(List<RouteInfo> complaintsListItems) {
        routeInfoList = new ArrayList<>();
        routeInfoList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }


    private void getDirectionFromDirectionApiServer(String url, TextView eta_Time) {
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(eta_Time),
                createRequestErrorListener(eta_Time));
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Connection.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ConnactionCheckApplication.getInstance().addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener(TextView eta_Time) {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.e(TAG, "Response.Listener<DirectionObject>" + response.toString());
                    if (response.getStatus().equals("OK")) {
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes(), eta_Time);
//                        drawRouteOnMap(mMap, mDirections);

                    } else {
                        eta_Time.setText("0");
                        Log.e(TAG, "in else Response.Listener<DirectionObject> = Not find ETA" );
//                        TastyToast.makeText(context, "Not find ETA", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//                        Toast.makeText(MapsActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Response.ErrorListener createRequestErrorListener(TextView eta_Time) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Response.ErrorListener" + error.getMessage());
                eta_Time.setText("0");
                error.printStackTrace();
            }
        };
    }

    private void setRouteDistanceAndDuration(String distance, String duration) {
        Log.e(TAG, "distance = " + distance);
        Log.e(TAG, "duration = :" + duration);
//        distanceValue.setText(distance);
//        durationValue.setText(duration);
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes, TextView eta_Time) {
        List<LatLng> directionList = new ArrayList<LatLng>();
        for (RouteObject route : routes) {
            List<LegsObject> legs = route.getLegs();
            for (LegsObject leg : legs) {
                String routeDistance = leg.getDistance().getText();
                String routeDuration = leg.getDuration().getText();

                Log.e(TAG, "distance = " + routeDistance);
                Log.e(TAG, "duration = :" + routeDuration);
                eta_Time.setText(routeDuration);
//                setRouteDistanceAndDuration(routeDistance, routeDuration);

                /**       //if draw Polyline then incomment below line
                 //           reference from =>  https://inducesmile.com/android/android-find-distance-and-duration-between-two-points-on-android-map/
                 List<StepsObject> steps = leg.getSteps();
                 for(StepsObject step : steps){
                 PolylineObject polyline = step.getPolyline();
                 String points = polyline.getPoints();
                 List<LatLng> singlePolyline = decodePoly(points);
                 for (LatLng direction : singlePolyline){
                 directionList.add(direction);
                 }
                 }*/
            }
        }
        return directionList;
    }
}