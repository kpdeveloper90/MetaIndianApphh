package com.ecosense.app.activity.supervisor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.POIDetailAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteItem;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class RoutePOIDetail extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = RoutePOIDetail.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.tv_vh_route_name)
    AppCompatTextView tv_vh_route_name;

    @BindView(R.id.tv_route_vh_No)
    AppCompatTextView tv_route_vh_No;

    @BindView(R.id.tv_route_vh_type)
    AppCompatTextView tv_route_vh_type;

    @BindView(R.id.tv_route_dr_name)
    AppCompatTextView tv_route_dr_name;

    @BindView(R.id.tv_route_eta)
    AppCompatTextView tv_route_eta;

    @BindView(R.id.tv_vh_start_date)
    AppCompatTextView tv_vh_start_date;

    @BindView(R.id.tv_route_Status)
    AppCompatTextView tv_route_Status;

    @BindView(R.id.tv_route_end_Date)
    AppCompatTextView tv_route_end_Date;

    @BindView(R.id.srl_RoutePOIDetail)
    SwipeRefreshLayout srl_RoutePOIDetail;

    @BindView(R.id.rv_RoutePOIDetail)
    RecyclerView rv_RoutePOIDetail;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;


    private GridLayoutManager lLayout;
    POIDetailAdapter poiDetailAdapter;
    List<RouteInfo> routeInfoList;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;

    private boolean isLoading = true;
    static String methodIntent = null;
    static RouteItem routeDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_poidetail);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("POI Detail");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        routeInfoList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_RoutePOIDetail.setHasFixedSize(true);
        rv_RoutePOIDetail.setLayoutManager(lLayout);
        rv_RoutePOIDetail.setItemAnimator(new DefaultItemAnimator());
        poiDetailAdapter = new POIDetailAdapter(getApplicationContext(), routeInfoList);

        rv_RoutePOIDetail.setAdapter(poiDetailAdapter);


        rv_RoutePOIDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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
//                    if ((visibleItemCount + pastVisibleItemPosition) >= totalItemCount && previous_Item_Total != totalItemCount) {
//                        previous_Item_Total = totalItemCount;

//                        getFuelDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_RoutePOIDetail.setOnRefreshListener(this);
        srl_RoutePOIDetail.setColorSchemeResources(R.color.colorAccent);
        srl_RoutePOIDetail.setNestedScrollingEnabled(true);


        onNewIntent(getIntent());
    }

    @Override
    public void onRefresh() {
        srl_RoutePOIDetail.setRefreshing(true);
//        dialog_wardNumberDetailConform();

        routeInfoList.clear();
        poiDetailAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;

//        getFuelDetailOnServer(0);
        getRouteInfoDetailOnServer(routeDetail.getName(), 0);
        srl_RoutePOIDetail.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }


    public void getRouteVehiclePosition(String da_code) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {


                try {
                    String filters = null;

                    filters = "[[\"ActivityMaster\",\"da_code\",\"like\",\""
                            + da_code
                            + "\"]]";


                    Log.e(TAG, "filters = " + filters);
//                    http://nikhil:tspl@kainext.com:8888/api/positions
                    String url = session.getMyServerIP() + "/api/positions"
//                         +   "?filters="
//                            + URLEncoder.encode(filters, "UTF-8")
//                            + "&limit_page_length=" + limit_page_length
//                            + "&limit_start=" + limit_start
                            ;

                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode statusData = objectMapper.readTree(response.toString());
//                                        JsonNode statusData = rootNode.path("data");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");

                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo fDetail = objectMapper.readValue(visitor.toString(), RouteInfo.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
//                                                    routeInfoList.add(fDetail);
//                                                    poiDetailAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            Log.e(TAG, "metaDataList length = " + routeInfoList.size());
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
                    Log.e(TAG, "getAssetsBinDetailOnServer call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }

    public void getRouteInfoDetailOnServer(String da_code, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {


                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

                    filters = "[[\"ActivityMaster\",\"da_code\",\"like\",\""
                            + da_code
                            + "\"]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/ActivityMaster?filters="
                            + URLEncoder.encode(filters, "UTF-8")
                            + "&fields=" + fields
                            + "&limit_page_length=" + limit_page_length
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
                                                RouteInfo fDetail = objectMapper.readValue(visitor.toString(), RouteInfo.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    routeInfoList.add(fDetail);
                                                    poiDetailAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            Log.e(TAG, "metaDataList length = " + routeInfoList.size());
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
                    Log.e(TAG, "getAssetsBinDetailOnServer call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
//        Intent intent = getIntent();

        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
            if (methodIntent != null && methodIntent.equals("selectedRoute")) {
                routeDetail = (RouteItem) extras.getSerializable("routeDetail");

                tv_vh_route_name.setText(routeDetail.getDa_routeid());
                tv_route_vh_No.setText(routeDetail.getDa_vehicleno());
                tv_route_dr_name.setText(routeDetail.getDa_drivername());

                tv_route_Status.setText(routeDetail.getR_status());
                String rStatus = routeDetail.getR_status();
                tv_route_vh_type.setText(routeDetail.getVehicle_type());
                tv_route_eta.setText("NA");

                if (rStatus.equalsIgnoreCase(AppConfig.ROUTE_Status_Complete)) {
                    tv_route_Status.setTextColor(ContextCompat.getColor(this, R.color.complete));
                    tv_route_end_Date.setVisibility(View.VISIBLE);
                    tv_vh_start_date.setText(getString(R.string.start_date) + dateFormat(routeDetail.getCreation()));
                    tv_route_end_Date.setText(getString(R.string.end_date) + dateFormat(routeDetail.getModified()));
                } else {
                    tv_vh_start_date.setText(getString(R.string.start_date)  + dateFormat(routeDetail.getCreation()));
                    tv_route_end_Date.setVisibility(View.GONE);
                    tv_route_Status.setTextColor(ContextCompat.getColor(this, R.color.in_progress));
                }

                previous_Item_Total = 0;
                isLoading = true;
                getRouteInfoDetailOnServer(routeDetail.getName(), 0);
//                getRouteVehiclePosition(routeDetail.getDa_vehicleno());
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    MenuItem callMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.supervisor_home, menu);

        MenuItem homeMenuItem = menu.findItem(R.id.action_home);
        homeMenuItem.setVisible(false);

        callMenuItem = menu.findItem(R.id.action_request_call_driver);

        callMenuItem.setVisible(true);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_request_call_driver) {
            if (routeDetail.getDa_mobno() != null) {
                String Da_mobno = routeDetail.getDa_mobno();
                Log.e(TAG, " getHelplineName = " + routeDetail.getDa_drivername() +
                        "  getHelplineNumber = " + Da_mobno);
                callMenuItem.setVisible(true);
//                TastyToast.makeText(getApplicationContext(), "Call "+routeDetail.getDa_drivername(), TastyToast.LENGTH_SHORT, TastyToast.CONFUSING);
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Da_mobno)));
            } else {
                callMenuItem.setVisible(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = getString(R.string.back_online);
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message = getString(R.string.you_are_offline);
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), RouteCoverage.class));
    }
}
