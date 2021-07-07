package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.HowTosDetail;
import com.ecosense.app.adapter.HowTosListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.HowTos;
import com.ecosense.app.pojo.UserActivity;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class HowTosFragment extends Fragment implements ItemClickListener, View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = HowTosFragment.class.getSimpleName();

    @BindView(R.id.srl_How_tos)
    SwipeRefreshLayout srl_How_tos;

    @BindView(R.id.rv_How_tos)
    RecyclerView rv_How_tos;

    @BindView(R.id.search_howTos)
    SearchView search_howTos;


    private GridLayoutManager lLayout;
    HowTosListAdapter howTosListAdapter;
    List<HowTos> howTosList;

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;
    private SearchView searchView = null;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    public HowTosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_how_tos, container, false);
        ButterKnife.bind(this, view);
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
            Log.e(TAG, getString(R.string.you_are_offline));
            TastyToast.makeText(getActivity(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        howTosList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_How_tos.setHasFixedSize(true);
        rv_How_tos.setLayoutManager(lLayout);
        rv_How_tos.setItemAnimator(new DefaultItemAnimator());
        howTosListAdapter = new HowTosListAdapter(getActivity(), howTosList);
        rv_How_tos.setAdapter(howTosListAdapter);
        howTosListAdapter.setClickListener(this);

        rv_How_tos.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                        getHowTosDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });


        search_howTos.setOnQueryTextListener(this);
        search_howTos.setQueryHint("Search");
        search_howTos.setOnQueryTextListener(this);

        srl_How_tos.setOnRefreshListener(this);
        srl_How_tos.setColorSchemeResources(R.color.colorAccent);
        srl_How_tos.setNestedScrollingEnabled(true);
        previous_Item_Total = 0;
        isLoading = true;
        srl_How_tos.post(() -> getHowTosDetailOnServer(previous_Item_Total)
        );
    }


    @Override
    public void onRefresh() {
        srl_How_tos.setRefreshing(true);
        howTosList.clear();
        howTosListAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;
        getHowTosDetailOnServer(previous_Item_Total);
        srl_How_tos.setRefreshing(false);
    }


    public void getHowTosDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

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

//                filters = "[[\"Events\",\"evnstatus\",\"in\",[\"Pending\"]]]";


                    fields = "[\"*\"]";
                    Log.e(TAG, "getHowTosDetailOnServer filters = " + filters + "&fields=" + fields);
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

//                String url = session.getMyServerIP() + "/api/resource/Events?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + URLEncoder.encode(fields, "UTF-8");
                    String url = session.getMyServerIP() + "/api/resource/HOW_TOs?fields="
                            + URLEncoder.encode(fields, "UTF-8")
                            + "&limit_page_length=" + limit_page_length
                            + "&order_by=creation desc"
                            + "&limit_start=" + limit_start;
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
                                                HowTos eventDetails = objectMapper.readValue(visitor.toString(), HowTos.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    howTosList.add(eventDetails);
                                                    howTosListAdapter.notifyDataSetChanged();
//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
//                                                    getLikeDislikeCountDetailOnServer(eventDetails);
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
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG + " error in response", "Error: " + error.getMessage());
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

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

    public void getLikeDislikeCountDetailOnServer(HowTos eventDetails) {
//        try {
//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage("Please Wait...");
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

            url = session.getMyServerIP() + "/api/method/erpnext.accounts.doctype.account.account.News_cunt?docnm=" + URLEncoder.encode(eventDetails.getHowTos_id(), "UTF-8");
            Log.e(TAG, url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                            try {

                                if (!response.toString().trim().equalsIgnoreCase("{}")) {


                                    JSONArray messageArray = response.getJSONArray("message");
                                    Log.e(TAG, "JSONArray message  = " + messageArray.get(0).toString());
                                    if (messageArray != null) {
                                        JSONArray m1 = (JSONArray) messageArray.get(0);

//                                    Log.e(TAG, "JSONArray m1 like = " + m1.get(0));
//                                    Log.e(TAG, "JSONArray m1  dis like= " + m1.get(1));
                                        eventDetails.setHtlike(m1.get(0).toString());
                                        eventDetails.setHtdislike(m1.get(1).toString());
                                    } else {
                                        eventDetails.setHtlike(0 + "");
                                        eventDetails.setHtdislike(0 + "");
                                    }
                                    howTosList.add(eventDetails);
                                    howTosListAdapter.notifyDataSetChanged();
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

    public void updateLikeDislikeCountDetailOnServer() {
        try {
            String url = null;
            url = session.getMyServerIP() + "/api/method/erpnext.accounts.doctype.account.account.HOW_TOslike_cunt";
            Log.e(TAG, url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                    response -> {
                        Log.e(TAG, "updateLikeDislikeCountDetailOnServer = " + response.toString());
                    }, error -> {
                TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                Log.e(TAG, " Error in response updateLikeDislikeCountDetailOnServer ");
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    public void sendUserActionOnServer(UserActivity userActivity) {
    public void sendUserActionOnServer(JSONObject jsonObject) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
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
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            UserActivity compDetail = objectMapper.readValue(statusData.toString(), UserActivity.class);

                                            Log.e(TAG, "sendUserActionOnServer response = " + compDetail.getUauserid());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                            }
                                            updateLikeDislikeCountDetailOnServer();
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
                                    Log.e(TAG, "Response Error");
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }

                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Log.e(TAG+" error in response", "Error: " + error.getMessage());
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }

                            String message = null;
                            if (error instanceof NetworkError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (error instanceof ServerError) {
                                message = "The server could not be found. Please try again after some time!!";
                            } else if (error instanceof AuthFailureError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (error instanceof ParseError) {
                                message = "Parsing error! Please try again after some time!!";
                            } else if (error instanceof NoConnectionError) {
                                message = "Cannot connect to Internet...Please check your connection!";
                            } else if (error instanceof TimeoutError) {
                                message = "Connection TimeOut! Please check your internet connection.";
                            }
                            TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            Log.e(TAG, " Error in response message = >> " + message);
                            onErrorResponseError(error);
                        }
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
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            int mStatusCode = response.statusCode;
                            Log.e(TAG, "parseNetworkResponse    = " + mStatusCode);
                            return super.parseNetworkResponse(response);
                        }

                        @Override
                        protected VolleyError parseNetworkError(VolleyError volleyError) {
                            String json;
                            if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                                try {
                                    json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
//                                    Log.e(TAG, " Error in response json = "+json.toString());
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
                    Log.e(TAG, "UnsupportedEncodingException    = " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "Exception    = " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                }
            }, 2000);
        }

    }

    //* import com.android.volley.toolbox.HttpHeaderParser; */
    public void onErrorResponseError(VolleyError error) {

        // As of f605da3 the following should work
        NetworkResponse response = error.networkResponse;
        if (error instanceof ServerError && response != null) {
            try {
                String res = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                // Now you can use any deserializer to make sense of data
                JSONObject obj = new JSONObject(res);
            } catch (UnsupportedEncodingException e1) {
                // Couldn't properly decode data to string
                e1.printStackTrace();
            } catch (JSONException e2) {
                // returned data is not JSONObject?
                e2.printStackTrace();
            }
        } else {
            Log.e(TAG, "Error: onErrorResponseError null");
        }
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
            HowTos howtodata = howTosList.get(position);
            if (view.getId() == R.id.img_event) {
//                showcomp_EventDetail_Dialog(event);

                Intent mIntent = new Intent(getActivity(), HowTosDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putString("HowTos_id", howtodata.getHowTos_id());
                mIntent.setAction("HowTosSelect");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
            } else if (view.getId() == R.id.img_btn_SocialThumbUp) {
//                UserActivity ua = new UserActivity();
//
//                ua.setUaadate(Connection.getCurrentDateTime());
//                ua.setUauname(session.getpsName());
//                ua.setUauserid(session.getpsNo());
//                ua.setUaarticalid(howtodata.getHowTos_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Like);
//                ua.setUaartcltype(AppConfig.Uaartcltype_How_Tos);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", howtodata.getHowTos_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Like);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_How_Tos);
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
//                ua.setUaarticalid(howtodata.getHowTos_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Dislike);
//                ua.setUaartcltype(AppConfig.Uaartcltype_How_Tos);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", howtodata.getHowTos_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Dislike);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_How_Tos);
                    jsonObject.put("uaplatform", AppConfig.Uaplatform_Android);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }
                sendUserActionOnServer(jsonObject);
            }
            if (view.getId() == R.id.img_btn_share) {

                String desc = "How-Tos : " + howtodata.getHttitle() + "\n \n" +
                        howtodata.getHturl();

                shareTextUrl(getString(R.string.share_how_to_s), howtodata.getHttitle(), desc);
            } else if (view.getId() == R.id.img_btn_favorite) {
//                UserActivity ua = new UserActivity();
//
//                ua.setUaadate(Connection.getCurrentDateTime());
//                ua.setUauname(session.getpsName());
//                ua.setUauserid(session.getpsNo());
//                ua.setUaarticalid(howtodata.getHowTos_id());
//                ua.setUaacttype(AppConfig.Uaacttype_Favourites);
//                ua.setUaartcltype(AppConfig.Uaartcltype_How_Tos);
//                ua.setUaplatform(AppConfig.Uaplatform_Android);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("uaadate", Connection.getCurrentDateTime());
                    jsonObject.put("uauname", session.getpsName());
                    jsonObject.put("uauserid", session.getpsNo());
                    jsonObject.put("uaarticalid", howtodata.getHowTos_id());
                    jsonObject.put("uaacttype", AppConfig.Uaacttype_Favourites);
                    jsonObject.put("uaartcltype", AppConfig.Uaartcltype_How_Tos);
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
        final List<HowTos> filteredModelList = filter(howTosList, query);
        howTosListAdapter.setFilter(filteredModelList);
//
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        if (menu != null) {
            menu.findItem(R.id.action_filter_search).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private List<HowTos> filter(List<HowTos> models, String query) {
        query = query.toLowerCase();

        final List<HowTos> filteredModelList = new ArrayList<>();
        for (HowTos model : models) {
            final String text = model.getHttitle().toLowerCase();

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
        howTosListAdapter.notifyDataSetChanged();
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

}
