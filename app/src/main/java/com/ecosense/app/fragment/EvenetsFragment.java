package com.ecosense.app.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.EventListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.GPSTracker;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Event;
import com.ecosense.app.pojo.UserActivity;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class EvenetsFragment extends Fragment implements ItemClickListener, View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = EvenetsFragment.class.getSimpleName();

    public static String SERVER_URL = null;


    @BindView(R.id.srl_eventList)
    SwipeRefreshLayout srl_eventList;

    @BindView(R.id.rv_eventList)
    RecyclerView rv_eventList;

    @BindView(R.id.search_event)
    SearchView search_event;


    private GridLayoutManager lLayout;
    EventListAdapter eventListAdapter;
    List<Event> eventList;

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;
    private SearchView searchView = null;
    // GPSTracker class
    GPSTracker currentLocation;


    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    public EvenetsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_evenets, container, false);
        ButterKnife.bind(this, view);
        currentLocation = new GPSTracker(getActivity());
        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");
        setHasOptionsMenu(true);
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG,  getString(R.string.you_are_offline));
            TastyToast.makeText(getActivity(),  getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        eventList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_eventList.setHasFixedSize(true);
        rv_eventList.setLayoutManager(lLayout);
        rv_eventList.setItemAnimator(new DefaultItemAnimator());
        eventListAdapter = new EventListAdapter(getActivity(), eventList);
        rv_eventList.setAdapter(eventListAdapter);
        eventListAdapter.setClickListener(this);

        rv_eventList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e(TAG, " onScrolled isLoading = " + isLoading + "  dy= " + dy);

                if (dy > 0) {
                    int visibleItemCount = lLayout.getChildCount();
                    int totalItemCount = lLayout.getItemCount();
                    int pastVisibleItemPosition = lLayout.findFirstVisibleItemPosition();

                    Log.e(TAG, "before isLoading isLoading = " + isLoading);
                    if (isLoading) {
                        if (totalItemCount > previous_Item_Total) {
                            isLoading = false;
                            previous_Item_Total = totalItemCount;
                            Log.e(TAG, "in (totalItemCount > previous_Item_Total) = " + isLoading + " \t previous_Item_Total = " + previous_Item_Total);
                        }
                    }
                    if (!isLoading && ((totalItemCount - visibleItemCount)
                            <= (pastVisibleItemPosition + view_thereshold))) {

                        getEventsDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });


        search_event.setQueryHint( getString(R.string.search));
        search_event.setOnQueryTextListener(this);

        srl_eventList.setOnRefreshListener(this);
        srl_eventList.setColorSchemeResources(R.color.colorAccent);
        srl_eventList.setNestedScrollingEnabled(true);
        previous_Item_Total = 0;
        isLoading = true;
        srl_eventList.post(() -> getEventsDetailOnServer(previous_Item_Total)
        );
    }


    @Override
    public void onRefresh() {
        srl_eventList.setRefreshing(true);
        eventList.clear();
        eventListAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;
        getEventsDetailOnServer(previous_Item_Total);
        srl_eventList.setRefreshing(false);
    }


    public void getEventsDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
//
//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage("Please Wait...");
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();
            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;
//                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//                String rDate_time = dateFormat.format(new Date());
//                // convert date to calendar
//                Calendar c = Calendar.getInstance();
//                c.setTime(new Date());
//                c.add(Calendar.DATE, 1);
//                // convert calendar to date
//                Date currentDatePlusOne = c.getTime();
//                String addOneDay = dateFormat.format(currentDatePlusOne);
//                Log.e(TAG, "CurrentDate  = " + rDate_time
//                        + "\t addOneDay = " + addOneDay);
//
//                filters = "[[\"Visitor_Management\",\"workflow_state\",\"in\",[\"Visitor Check In\",\"Visitor Check Out\"]]," +
//                        "[\"Visitor_Management\",\"visitor_arrival_date_and_time\",\">\",\"" +
//                        rDate_time +
//                        "\"],[\"Visitor_Management\",\"visitor_arrival_date_and_time\",\"<=\",\"" +
//                        addOneDay +
//                        "\"]] ";

                    filters = "[[\"Events\",\"evnstatus\",\"in\",[\"Pending\"]]]";


                    fields = "[\"*\"]";
                    Log.e(TAG, "getEventsDetailOnServer filters = " + filters + "&fields=" + fields);
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    String url = session.getMyServerIP() + "/api/resource/Events?filters="
                            + URLEncoder.encode(filters, "UTF-8")
                            + "&fields=" + URLEncoder.encode(fields, "UTF-8")
                            + "&limit_page_length=" + limit_page_length
                            + "&order_by=creation desc"
                            + "&limit_start=" + limit_start;
//                String url = session.getMyServerIP() + "/api/resource/News?fields=" + URLEncoder.encode(fields, "UTF-8");
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Event eventDetails = objectMapper.readValue(visitor.toString(), Event.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    eventList.add(eventDetails);
                                                    eventListAdapter.notifyDataSetChanged();
//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
                                                }
                                            }
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }

                                    } catch (IOException e) {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }) {
                        @Override
                        protected VolleyError parseNetworkError(VolleyError volleyError) {
                            String json;
                            if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                                try {
                                    json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                                } catch (UnsupportedEncodingException e) {
                                    return new VolleyError(e.getMessage());
                                }
                                return new VolleyError(json);
                            }

                            return volleyError;
                        }
                    };
                    String tag_string_req = "string_req";
                    ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, PROGRASS_postDelayed);
        }
    }

    Dialog comp_EventDetail;
    KenBurnsView img_e_detail_pic;
    ImageView dialog_close, img_dialog_loc_icon;
    TextView tv_e_detail_DateTime, tv_dialog_title, tv_e_detail_title, tv_e_detail_desc, tv_event_dialog_loc;


    public void showcomp_EventDetail_Dialog(Event event) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_event_detail, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        comp_EventDetail = new Dialog(getActivity());
        comp_EventDetail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        comp_EventDetail.setContentView(root);
        comp_EventDetail.setCancelable(true);
        comp_EventDetail.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        comp_EventDetail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        tv_dialog_title = (TextView) comp_EventDetail.findViewById(R.id.tv_dialog_title);

        tv_e_detail_title = (TextView) comp_EventDetail.findViewById(R.id.tv_e_detail_title);
        tv_e_detail_DateTime = (TextView) comp_EventDetail.findViewById(R.id.tv_e_detail_DateTime);
        tv_e_detail_desc = (TextView) comp_EventDetail.findViewById(R.id.tv_e_detail_desc);
        tv_event_dialog_loc = (TextView) comp_EventDetail.findViewById(R.id.tv_event_dialog_loc);
        img_dialog_loc_icon = (ImageView) comp_EventDetail.findViewById(R.id.img_dialog_loc_icon);
        img_e_detail_pic = (KenBurnsView) comp_EventDetail.findViewById(R.id.img_e_detail_pic);
        dialog_close = (ImageView) comp_EventDetail.findViewById(R.id.dialog_close);


        dialog_close.setOnClickListener(v -> {
            comp_EventDetail.dismiss();
        });

        tv_dialog_title.setText("Event Detail");
        tv_e_detail_title.setText(event.getEvntitle());
        tv_e_detail_DateTime.setText(dateFormat(event.getEvndate()));
        tv_e_detail_desc.setText(event.getEvndetail());
        tv_event_dialog_loc.setText(event.getEvnloc());

        if (event.getEvnphoto() == null) {
            Glide.with(getActivity()).load(R.drawable.default_image)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_e_detail_pic);
        } else {
            String url = session.getMyServerIP() + event.getEvnphoto();
            Glide.with(getActivity()).load(url)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_e_detail_pic);
        }


        comp_EventDetail.show();
    }

//    public void sendUserActionOnServer(UserActivity userActivity) {
    public void sendUserActionOnServer(JSONObject jsonObject) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                show_dialog_progress("Please Wait...");
            } catch (Exception e) {
            }
            try {
                String data = null;
                data = "{}";
                URLEncoder.encode(data, "UTF-8");


//                String url = session.getMyServerIP() + "/api/resource/UserActivity/" + URLEncoder.encode(compFeedback.getComId(), "UTF-8");
                String url = session.getMyServerIP() + "/api/resource/UserActivity";
//                Log.e(TAG, "url = > " + url);
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            // response
                            if (!response.isEmpty()) {
//                                Log.e("Response", response);
                                try {
                                    //create ObjectMapper instance
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    JsonNode rootNode = objectMapper.readTree(response.toString());
                                    JsonNode statusData = rootNode.path("data");
//                                    Log.e(TAG, "statusData = " + statusData.toString());
                                    if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                        UserActivity compDetail = objectMapper.readValue(statusData.toString(), UserActivity.class);

                                        Log.e(TAG, " sendUserActionOnServer response  = " + compDetail.getUauserid());
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                        }
                                        onRefresh();

                                    } else {
                                        TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                            } else {
                                TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            }

                        }, error -> {
                    // error
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

                    Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
//                        Gson gson = new Gson();
//                        String regpojo = gson.toJson(userActivity);
//                        Log.e(TAG, "getParams   = " + regpojo);

                        params.put("data", jsonObject.toString());
                        return params;
                    }

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        String json;
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            try {
                                json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                            } catch (UnsupportedEncodingException e) {
                                return new VolleyError(e.getMessage());
                            }
                            return new VolleyError(json);
                        }
                        return volleyError;
                    }

                };
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (dialog_progress != null) {
                    dialog_progress.dismiss();
                    dialog_progress = null;
                }

            }
        }

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
    public void onClick(View v) {
        if (v == alert_no2) {
            alert_dialog2.dismiss();
        }

        if (v == alert_yes) {
            alert_dialog1.dismiss();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        if (v == alert_no) {
            alert_dialog1.dismiss();
        }
    }

    public long convertLongDate(String date) {
        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
        long dt = 0;
        try {
            Date d = f.parse(date);
            dt = d.getTime();
            Log.e(TAG, "DAte = " + dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    //Convert Date to Calendar
    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }


    @Override
    public void onClick(View view, int position) {
        try {
            Event event = eventList.get(position);
            if (view.getId() == R.id.img_event) {
                showcomp_EventDetail_Dialog(event);
            } else if (view.getId() == R.id.img_btn_addEventInCalendar) {
                addToCalendarEvent(event.getEvntitle(), event.getEvndetail(), event.getEvnloc(), event.getEvnbroadcast_date(), event.getEvndate());
            } else if (view.getId() == R.id.img_btn_share) {

                String desc = "Event : " + event.getEvntitle() + "\t on " + dateFormat(event.getEvndate()) + "\n \n" +
                        event.getEvnurl();


                shareTextUrl(getString(R.string.share_event), event.getEvntitle(), desc);
            } else if (view.getId() == R.id.img_btn_directions) {
                diractionOfEvent(event.getEvnlatitude(), event.getEvnlongitude());
            } else if (view.getId() == R.id.img_btn_volunteer) {
//
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage( getString(R.string.you_are_offline));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                new android.os.Handler().postDelayed(() -> {
                    try {
//                        UserActivity ua = new UserActivity();
//                        ua.setUaadate(Connection.getCurrentDateTime());
//                        ua.setUauname(session.getpsName());
//                        ua.setUauserid(session.getpsNo());
//                        ua.setUaarticalid(event.getEvent_id());
//                        ua.setUaacttype(AppConfig.Uaacttype_Volunteer);
//                        ua.setUaartcltype(AppConfig.Uaartcltype_Events);
//                        ua.setUaplatform(AppConfig.Uaplatform_Android);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject();

                            jsonObject.put("uaadate", Connection.getCurrentDateTime());
                            jsonObject.put("uauname", session.getpsName());
                            jsonObject.put("uauserid", session.getpsNo());
                            jsonObject.put("uaarticalid", event.getEvent_id());
                            jsonObject.put("uaacttype",AppConfig.Uaacttype_Volunteer);
                            jsonObject.put("uaartcltype",AppConfig.Uaartcltype_Events);
                            jsonObject.put("uaplatform",AppConfig.Uaplatform_Android);

                        } catch (JSONException e) {
                            Log.e("JSONObject Here", e.toString());
                        }



                        sendUserActionOnServer(jsonObject);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                    }
                }, 2000);
            }
        } catch (
                Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());

        }
    }

    public long convertDateInMilis(String Formate, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Formate);
        Date myDate = null;
        try {
            myDate = dateFormat.parse(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "myDate = " + myDate);
//        Calendar cal = Calendar.getInstance();/
        Calendar cal = new GregorianCalendar();
        cal.setTime(myDate);

        return cal.getTimeInMillis();
    }

    private void addToCalendarEvent(String title, String evnDetail, String location, String dtstart, String dtend) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");

        String halfFormat = "yyyy-MM-dd";
        String fullFormat = "yyyy-MM-dd HH:mm:ss";

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, convertDateInMilis(halfFormat, dtstart));
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, convertDateInMilis(fullFormat, dtend));
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);

        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, evnDetail);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");
        startActivity(intent);
    }


    private void diractionOfEvent(String Lat, String Long) {
        // Create a Uri from an intent string. Use the result to create an Intent.

////        47.5951518,-122.3316393
//        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Lat + "," + Long);
//
//// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//// Make the Intent explicit by setting the Google Maps package
////        mapIntent.setPackage("com.google.android.apps.maps");
//
//// Attempt to start an activity that can handle the Inten
//        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivity(mapIntent);
//        }

        getMyCurrentLocation();

//        // check if GPS enabled
//        if(currentLocation.canGetLocation()){
//
//            MyLat = currentLocation.getLatitude();
//            MyLong = currentLocation.getLongitude();

        Log.e(TAG, "onClick Current = " + MyLat + "," + MyLong);
//                Log.e(TAG, "onClick diraction = " + nearByPlace.getLat() + "," + nearByPlace.getLng());
        Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + MyLat + "," + MyLong + "&daddr=" + Lat + "," + Long);
        Log.e(TAG, "onClick uri = " + uri);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
//            // \n is for new line
//            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
//                    + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
//        }else{
//            // can't get location
//            // GPS or Network is not enabled
//            // Ask user to enable GPS/network in settings
//            currentLocation.showSettingsAlert();
//        }


    }

    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.Finding_Location));
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {

            LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            android.location.LocationListener locListener = new MyLocationListener();

            try {
                gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }
            try {
                network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            //don't start listeners if no provider is enabled
            //if(!gps_enabled && !network_enabled)
            //return false;

            if (gps_enabled) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            }
            if (gps_enabled) {
                location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (network_enabled && location == null) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
            }
            if (network_enabled && location == null) {
                location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                MyLat = location.getLatitude();
                MyLong = location.getLongitude();
            } else {
                Location loc = getLastKnownLocation(getActivity());
                if (loc != null) {
                    MyLat = loc.getLatitude();
                    MyLong = loc.getLongitude();
                }
            }
            locManager.removeUpdates(locListener);
            // removes the periodic updates from location listener to //avoid battery drainage. If you want to get location at the periodic intervals call this method using //pending intent.
            try {
// Getting address from found locations.

                // you can get more details other than this . like country code, state code, etc.
                Log.e(TAG, "" + MyLat);
                Log.e(TAG, "" + MyLong);

                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, 2000);

    }

    public static Location getLastKnownLocation(Context context) {
        Location location = null;
        LocationManager locationmanager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List list = locationmanager.getAllProviders();
        boolean i = false;
        Iterator iterator = list.iterator();
        do {
            //System.out.println("---------------------------------------------------------------------");
            if (!iterator.hasNext())
                break;
            String s = (String) iterator.next();
            //if(i != 0 && !locationmanager.isProviderEnabled(s))
            if (i != false && !locationmanager.isProviderEnabled(s))
                continue;
            // System.out.println("provider ===> "+s);
            @SuppressLint("MissingPermission") Location location1 = locationmanager.getLastKnownLocation(s);
            if (location1 == null)
                continue;
            if (location != null) {
                //System.out.println("location ===> "+location);
                //System.out.println("location1 ===> "+location);
                float f = location.getAccuracy();
                float f1 = location1.getAccuracy();
                if (f >= f1) {
                    long l = location1.getTime();
                    long l1 = location.getTime();
                    if (l - l1 <= 600000L)
                        continue;
                }
            }
            location = location1;
            // System.out.println("location  out ===> "+location);
            //System.out.println("location1 out===> "+location);
            i = locationmanager.isProviderEnabled(s);
            // System.out.println("---------------------------------------------------------------------");
        } while (true);
        return location;
    }

    // Location listener class. to get location.
    public class MyLocationListener implements android.location.LocationListener {
        public void onLocationChanged(Location location) {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
            }
        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    }

    Double MyLat, MyLong;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    Location location;

    private void shareTextUrl(String titleOfDialog, String titleOfPost, String sendingUrl) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, titleOfPost);
        share.putExtra(Intent.EXTRA_TEXT, sendingUrl);
        startActivity(Intent.createChooser(share, titleOfDialog));
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        if (menu != null) {
            menu.findItem(R.id.action_filter_search).setVisible(false);
        }
//            MenuItem searchItem = menu.findItem(R.id.action_filter_search);
//            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//
//            if (searchItem != null) {
//                searchView = (SearchView) searchItem.getActionView();
//            }
//            if (searchView != null) {
//                searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//                searchView.setOnQueryTextListener(this);
//            }

        //        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        //        txtSearch.setHint("SEARCH");
        //        txtSearch.setHighlightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        //        txtSearch.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.grey_600));
        //        txtSearch.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        ////        txtSearch.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

        super.onCreateOptionsMenu(menu, inflater);
    }


    Dialog dialog_progress;
    TextView tv_prog_msg;

    public void show_dialog_progress(String Msg) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            final View root = inflater.inflate(R.layout.dialog_progress, null);
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialog_progress = new Dialog(getActivity());
            dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_progress.setContentView(root);
            dialog_progress.setCancelable(false);
            dialog_progress.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
            dialog_progress.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


            tv_prog_msg = (TextView) dialog_progress.findViewById(R.id.tv_prog_msg);
            tv_prog_msg.setText(Msg);
            dialog_progress.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String query) {
        final List<Event> filteredModelList = filter(eventList, query);
        eventListAdapter.setFilter(filteredModelList);
//
        return true;
    }


    private List<Event> filter(List<Event> models, String query) {
        query = query.toLowerCase();

        final List<Event> filteredModelList = new ArrayList<>();
        for (Event model : models) {
            final String text = model.getEvntitle().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    Dialog alert_dialog1, alert_dialog2;
    Button alert_yes, alert_no, alert_no2;
    TextView alert_msg, alert_msg2;


    public void show_alert_two_button(String msg) {


        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog1 = new Dialog(getActivity());
        alert_dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog1.setContentView(root);
        alert_dialog1.setCancelable(false);
        alert_dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_yes = (Button) alert_dialog1.findViewById(R.id.alert_close);
        alert_msg = (TextView) alert_dialog1.findViewById(R.id.alert_msg);
        alert_no = (Button) alert_dialog1.findViewById(R.id.alert_no);
        alert_yes.setText("Yes");
        alert_msg.setText(msg);
        alert_no.setVisibility(View.VISIBLE);


        alert_yes.setOnClickListener(this);
        alert_no.setOnClickListener(this);


        alert_dialog1.show();
    }

    public void show_alert_Dialog_singlebutton(String msg) {

        int counter = 0;
        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog2 = new Dialog(getActivity());
        alert_dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog2.setContentView(root);
        alert_dialog2.setCancelable(false);
        alert_dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_no2 = (Button) alert_dialog2.findViewById(R.id.alert_close);
        alert_msg2 = (TextView) alert_dialog2.findViewById(R.id.alert_msg);

        alert_msg2.setText(msg);


        alert_no2.setOnClickListener(this);


        alert_dialog2.show();
    }

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message =getString(R.string.back_online);
            color = Color.WHITE;
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
//            message = "Sorry! Not connected to internet";
            message =getString(R.string.you_are_offline);
            color = Color.RED;
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }

//        Snackbar snackbar = Snackbar
//                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);

//        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(color);
//        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
        eventListAdapter.notifyDataSetChanged();
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

//    @Override
//    public void onBackPressed() {
//        if (!ecPagerView.collapse())
//            super.onBackPressed();
//    }

}