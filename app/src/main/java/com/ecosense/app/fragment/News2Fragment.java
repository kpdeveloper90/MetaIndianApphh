package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.NewsListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.LoginDetail;
import com.ecosense.app.pojo.News;
import com.ecosense.app.pojo.UserActivity;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class News2Fragment extends Fragment implements ItemClickListener, View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = News2Fragment.class.getSimpleName();

    public static String SERVER_URL = null;

    @BindView(R.id.srl_newsList)
    SwipeRefreshLayout srl_newsList;

    @BindView(R.id.rv_newsList)
    RecyclerView rv_newsList;

    @BindView(R.id.search_news)
    SearchView search_news;
    static ProgressDialog mProgressDialog = null;

    private GridLayoutManager lLayout;
    NewsListAdapter newsListAdapter;
    List<News> eventList;

    Boolean isConnected = false;
    UserSessionManger session;
    private SearchView searchView = null;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    public News2Fragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_news2, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, getString(R.string.you_are_offline));
            TastyToast.makeText(getActivity(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        eventList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_newsList.setHasFixedSize(true);
        rv_newsList.setLayoutManager(lLayout);
        rv_newsList.setItemAnimator(new DefaultItemAnimator());
        newsListAdapter = new NewsListAdapter(getActivity(), eventList);
        rv_newsList.setAdapter(newsListAdapter);
        newsListAdapter.setClickListener(this);

        search_news.setQueryHint(getString(R.string.search));
        search_news.setOnQueryTextListener(this);

        srl_newsList.setOnRefreshListener(this);
        srl_newsList.setColorSchemeResources(R.color.colorAccent);
        srl_newsList.setNestedScrollingEnabled(true);


        rv_newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                        getNewsDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        previous_Item_Total = 0;
        isLoading = true;

        srl_newsList.post(() -> getNewsDetailOnServer(previous_Item_Total)
        );

    }

    @Override
    public void onRefresh() {
        try {
            srl_newsList.setRefreshing(true);
            eventList.clear();
            newsListAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getNewsDetailOnServer(0);
            srl_newsList.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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


    public void getNewsDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage(getString(R.string.please_wait));
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

                    filters = "[[\"News\",\"nwnewsstatus\",\"in\",[\"Active\"]]]";


                    fields = "[\"*\"]";
                    Log.e(TAG, "getNewsDetailOnServer filters = " + filters + "&fields=" + fields);
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    String url = session.getMyServerIP() + "/api/resource/News?filters="
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
                                                News eventDetails = objectMapper.readValue(visitor.toString(), News.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    eventList.add(eventDetails);
                                                    newsListAdapter.notifyDataSetChanged();
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

    public void getLikeDislikeCountDetailOnServer(News eventDetails) {
//        try {
//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage(getString(R.string.please_wait));
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();
//        } catch (Exception e) {
//        }
//        new android.os.Handler().postDelayed(() -> {
        try {
//                JSONObject jsonObject = null;
//
//                try {
//                    jsonObject = new JSONObject();
//                    jsonObject.put("clean_status", status);
//                    jsonObject.put("route_binphoto", binImage);
//                } catch (JSONException e) {
//                    Log.e("JSONObject Here", e.toString());
//                }

            String url = null;

            url = session.getMyServerIP() + "/api/method/erpnext.accounts.doctype.account.account.News_cunt?docnm=" + URLEncoder.encode(eventDetails.getNews_id(), "UTF-8");
//            Log.e(TAG, url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                            try {

                                if (!response.toString().trim().equalsIgnoreCase("{}")) {


                                    JSONArray messageArray = response.getJSONArray("message");
//                                    Log.e(TAG, "JSONArray message  = " + messageArray.get(0).toString());
                                    if (messageArray != null) {
                                        JSONArray m1 = (JSONArray) messageArray.get(0);

//                                    Log.e(TAG, "JSONArray m1 like = " + m1.get(0));
//                                    Log.e(TAG, "JSONArray m1  dis like= " + m1.get(1));
                                        eventDetails.setNwlike(m1.get(0).toString());
                                        eventDetails.setNwdislike(m1.get(1).toString());
                                    } else {
                                        eventDetails.setNwlike(0 + "");
                                        eventDetails.setNwdislike(0 + "");
                                    }
                                    eventList.add(eventDetails);
                                    newsListAdapter.notifyDataSetChanged();
                                } else {
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                    Log.e(TAG, " Error in response upload image RequestServer ");
                }
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    String json;
                    if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                        try {
                            json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                            Log.e(TAG, "Error  = " + json);
                        } catch (UnsupportedEncodingException e) {
                            return new VolleyError(e.getMessage());
                        }
                        return new VolleyError(json);
                    }
                    return volleyError;
                }
            };

            String tag_string_req = "object_req";
            ConnactionCheckApplication.getInstance().addToRequestQueue(jsonObjectRequest, tag_string_req);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }, PROGRASS_postDelayed);
    }

    //    public void sendUserActionOnServer(UserActivity userActivity) {
    public void sendUserActionOnServer(JSONObject jsonObject) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");


//                String url = session.getMyServerIP() + "/api/resource/UserActivity/" + URLEncoder.encode(compFeedback.getComId(), "UTF-8");
                    String url = session.getMyServerIP() + "/api/resource/UserActivity";
//                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            UserActivity compDetail = objectMapper.readValue(statusData.toString(), UserActivity.class);

                                            Log.e(TAG, "sendUserActionOnServer response = " + compDetail.getUauserid());

                                            if (progressDialog != null) {
                                                progressDialog.dismiss();

                                            }
                                            onRefresh();

                                        } else {

                                            if (progressDialog != null) {
                                                progressDialog.dismiss();
                                            }
                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }

                            }, error -> {
                        // error
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
//                            Gson gson = new Gson();
//                            String regpojo = gson.toJson(userActivity);
//                            Log.e(TAG, "getParams   = " + regpojo);

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
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }, PROGRASS_postDelayed);
        }

    }

    Dialog comp_EventDetail;
    KenBurnsView img_e_detail_pic;
    ImageView dialog_close, img_dialog_loc_icon;
    TextView tv_e_detail_DateTime, tv_event_dialog_loc, tv_dialog_title, tv_e_detail_title, tv_e_detail_desc;


    public void showcomp_EventDetail_Dialog(News event) {

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


        img_dialog_loc_icon.setVisibility(View.GONE);
        tv_event_dialog_loc.setVisibility(View.GONE);
        dialog_close.setOnClickListener(v -> {
            comp_EventDetail.dismiss();
        });

        tv_dialog_title.setText("News Detail");
        tv_e_detail_title.setText(event.getNwtitle());
        tv_e_detail_DateTime.setText(dateFormat(event.getNwpftime()));
        tv_e_detail_desc.setText(event.getNwdescription());

        if (event.getNwphoto() == null) {
            Glide.with(getActivity()).load(R.drawable.default_image)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_e_detail_pic);
        } else {

            String url = session.getMyServerIP() + event.getNwphoto();

            Glide.with(getActivity()).load(url)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_e_detail_pic);
        }
        comp_EventDetail.show();
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

    @Override
    public void onClick(View view, int position) {
        try {
            News event = eventList.get(position);
            if (view.getId() == R.id.img_btn_share) {

                String desc = "News : " + event.getNwtitle() +
                        "\t on " + dateFormat(event.getNwpftime()) + "\n \n" +
                        event.getNwurl();

                shareTextUrl("Share News", event.getNwtitle(), desc);
            } else if (view.getId() == R.id.img_event) {
                showcomp_EventDetail_Dialog(event);
            } else if (view.getId() == R.id.img_btn_SocialThumbUp) {
//                UserActivity ua = new UserActivity();
//
//                ua.setUaadate(Connection.getCurrentDateTime());
//                ua.setUauname(session.getpsName());
//                ua.setUauserid(session.getpsNo());
//                ua.setUaarticalid(event.getNews_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Like);
//                ua.setUaartcltype(AppConfig.Uaartcltype_News);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", event.getNews_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Like);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_News);
                    jsonObject.put("uaplatform", AppConfig.Uaplatform_Android);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }
                sendUserActionOnServer(jsonObject);
            } else if (view.getId() == R.id.img_btn_socialThumbDown) {
//                UserActivity ua = new UserActivity();
//
//                ua.setUaadate(Connection.getCurrentDateTime());
//                ua.setUauname(session.getpsName());
//                ua.setUauserid(session.getpsNo());
//                ua.setUaarticalid(event.getNews_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Dislike);
//                ua.setUaartcltype(AppConfig.Uaartcltype_News);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", event.getNews_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Dislike);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_News);
                    jsonObject.put("uaplatform", AppConfig.Uaplatform_Android);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }
                sendUserActionOnServer(jsonObject);
            } else if (view.getId() == R.id.img_btn_favorite) {
//                UserActivity ua = new UserActivity();
//
//                ua.setUaadate(Connection.getCurrentDateTime());
//                ua.setUauname(session.getpsName());
//                ua.setUauserid(session.getpsNo());
//                ua.setUaarticalid(event.getNews_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Favourites);
//                ua.setUaartcltype(AppConfig.Uaartcltype_News);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);
//                sendUserActionOnServer(ua);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", event.getNews_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Favourites);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_News);
                    jsonObject.put("uaplatform", AppConfig.Uaplatform_Android);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }
                sendUserActionOnServer(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());

        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void FindLoginDetailFromServer(String lid, String lpwd) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {

            try {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {
//                String url = "http://159.89.164.145:8000/api/method/login?usr=Administrator&pwd=tspl";
                String url = session.getMyServerIP() + "/api/method/login";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {

                            Log.e("Response", response);

                            //create ObjectMapper instance
                            ObjectMapper objectMapper = new ObjectMapper();

                            LoginDetail loginDetail = null;

                            try {
                                loginDetail = objectMapper.readValue(response.toString(), LoginDetail.class);


                                Log.e(TAG, " response.getMessage() " + loginDetail.getMessage());
                                Log.e(TAG, " response.getFull_name() " + loginDetail.getFull_name());
//                            if (sweetItem.getSuccess() == 1) {
                                if (loginDetail.getMessage().equalsIgnoreCase("Logged In")) {
//
//                                String psNo = sweetItem.getPsNo();
//                                String psName = sweetItem.getPsName();
//                                String userType = sweetItem.getUserType();
//
//                                Log.e(TAG, "psName = " + psName + "\n  psNo= " + psNo + "\n  userType= " + userType);
//                                session.createUserLoginSession(psNo, psName, userType);
//
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }

                                    Log.e(TAG, "Server Connected Successfully");


                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }

                                    show_alert_Dialog_singlebutton(loginDetail.getMessage());

                                    Log.e(TAG, "FindLoginDetailFromServer onResponse Error :" + response);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, " Error");
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }
                            }

                        }, error -> {
                    // error
                    Log.e(TAG + " Error.Response", error.toString());
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("usr", lid);
                        params.put("pwd", lpwd);

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return getAuthHeader(getActivity().getApplicationContext());
                    }

                    @Override
                    protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                        // since we don't know which of the two underlying network vehicles
                        // will Volley use, we have to handle and store session cookies manually
                        Log.e("response", response.headers.toString());
                        Map<String, String> responseHeaders = response.headers;
                        String rawCookies = responseHeaders.get("Set-Cookie");
                        Log.e("cookies", rawCookies);
                        return super.parseNetworkResponse(response);

                    }
                };
                // Tag used to cancel the request
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
//        ApplicationController.getInstance(getApplicationContext()).addToRequestQueue(postRequest, tag_string_req);
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(postRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String auth, API_KEY, CONTENT_TYPE;

    public static Map<String, String> getAuthHeader(Context context) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("token", auth);
        headerMap.put("Api-key", API_KEY);
        headerMap.put("Content-Type", CONTENT_TYPE);
        return headerMap;
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String query) {
        final List<News> filteredModelList = filter(eventList, query);
        newsListAdapter.setFilter(filteredModelList);
//
        return true;
    }


    private List<News> filter(List<News> models, String query) {
        query = query.toLowerCase();

        final List<News> filteredModelList = new ArrayList<>();
        for (News model : models) {
            final String text = model.getNwtitle().toLowerCase();

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
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message =getString(R.string.you_are_offline);
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
//        CookieManager manager = new CookieManager();
//        CookieHandler.setDefault(manager);
//        FindLoginDetailFromServer( "Administrator", "tspl");
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
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
