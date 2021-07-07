package com.ecosense.app.activity.driver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdsmdg.tastytoast.TastyToast;

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
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.Traccar.TrackingService;
import com.ecosense.app.activity.citizen.NewComplaints;
import com.ecosense.app.adapter.BINReasonDropDownAdapter;
import com.ecosense.app.adapter.RouteMapListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST;
import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class RouteMapList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, OnMapReadyCallback,
        LocationListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.getUiSettings().setAllGesturesEnabled(true);
        getMyCurrentLocation();
//        showCurrentLocation();
    }

    private GoogleMap mMap;

    private static final String TAG = RouteMapList.class.getSimpleName();
    private Toolbar toolbar;
    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    static ProgressDialog mProgressDialog = null;
    List<RouteInfo> routeInfoList = null;
    List<RouteInfo> binReasonInfoList = null;

    private GridLayoutManager lLayout;
    RouteMapListAdapter routeMapListAdapter;
    BINReasonDropDownAdapter binReasonDropDownAdapter = null;

    @BindView(R.id.srl_RouteMapDetail)
    SwipeRefreshLayout srl_RouteMapDetail;

    @BindView(R.id.ll_list)
    RelativeLayout ll_list;


    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.tv_bin_pending_count)
    TextView tv_bin_pending_count;

    @BindView(R.id.tv_bin_clean_count)
    TextView tv_bin_clean_count;

    @BindView(R.id.btn_completed)
    Button btn_completed;

    @BindView(R.id.rv_RouteMapDetail)
    RecyclerView rv_RouteMapDetail;


    static RouteItem routeDetails = null;
    static Vehicle vehicleDetails = null;

    // Store a pagination variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map_list);


        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Route Map");
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
        binReasonInfoList = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        lLayout = new GridLayoutManager(this, 1);
        rv_RouteMapDetail.setHasFixedSize(true);
        rv_RouteMapDetail.setLayoutManager(lLayout);
        rv_RouteMapDetail.setItemAnimator(new DefaultItemAnimator());
        routeMapListAdapter = new RouteMapListAdapter(this, routeInfoList);
        rv_RouteMapDetail.setAdapter(routeMapListAdapter);
        routeMapListAdapter.setClickListener(this);
        binReasonDropDownAdapter = new BINReasonDropDownAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, binReasonInfoList);

        rv_RouteMapDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e(TAG, " onScrolled isLoading = " + isLoading + "  dy= " + dy);
                if (dy > 0) {
                    int visibleItemCount = lLayout.getChildCount();
                    int totalItemCount = lLayout.getItemCount();
                    int pastVisibleItemPosition = lLayout.findFirstVisibleItemPosition();
                    Log.e(TAG, "visibleItemCount = " + visibleItemCount);
                    Log.e(TAG, "totalItemCount = " + totalItemCount);
                    Log.e(TAG, "pastVisibleItemPosition = " + pastVisibleItemPosition);
//                    Log.e(TAG, "(visibleItemCount + pastVisibleItemPosition) = " + (visibleItemCount + pastVisibleItemPosition));
                    Log.e(TAG, "preLastVisible = " + previous_Item_Total);
                    Log.e(TAG, "(totalItemCount - visibleItemCount) = " + (totalItemCount - visibleItemCount));
                    Log.e(TAG, "(pastVisibleItemPosition + view_thereshold) = " + (pastVisibleItemPosition + view_thereshold));

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
                        getRouteDetailOnServer(routeDetails.getName(), totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }
                }
            }
        });


        srl_RouteMapDetail.setOnRefreshListener(this);
        srl_RouteMapDetail.setColorSchemeResources(R.color.colorAccent);
        srl_RouteMapDetail.setNestedScrollingEnabled(true);
        srl_RouteMapDetail.post(
                new Runnable() {
                    @Override
                    public void run() {

//                            getMyCurrentLocation();
//
                    }
                }
        );
        btn_completed.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    @Override
    public void onRefresh() {
        srl_RouteMapDetail.setRefreshing(true);
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//            getMyCurrentLocation();
//
//        }
        previous_Item_Total = 0;
        isLoading = true;
        mMap.clear();
        routeInfoList.clear();
        routeMapListAdapter.notifyDataSetChanged();
        binCleanCount = 0;
        binPendingCount = 0;
        btnAlertCont = 0;

        getRouteDetailOnServer(routeDetails.getName(), 0);

        srl_RouteMapDetail.setRefreshing(false);
    }

    static int btnAlertCont = 0;

    @Override
    public void onClick(View v) {
        if (v == tv_reason_cancel) {
            dialog_reason.dismiss();
        }
        if (v == btn_completed) {
            //alert 2 times
            if (routeInfoList.size() == binCleanCount) {
                updateRouteStatusServer(AppConfig.ROUTE_Status_Complete);
            } else {
                btnAlertCont = btnAlertCont + 1;
                String msg = getString(R.string.do_you_complete_route) + binPendingCount + getString(R.string.bin_scheduled);
                aleartforCompetRoute(msg);
            }
        }
    }

    protected void aleartforCompetRoute(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle(btnAlertCont + getString(R.string.title_alert_for_complete_route))
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        if (btnAlertCont >= 3) {
                            updateRouteStatusServer(AppConfig.ROUTE_Status_Complete);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                        btnAlertCont = 0;
                    }
                })
                .show();

    }

    @Override
    public void onClick(View view, int position) {
        try {
            RouteInfo routeinfoDetail = routeInfoList.get(position);
            if (view.getId() == R.id.img_btn_place_diraction) {

//                Uri uri = Uri.parse("http://maps.google.com/maps");
                double Lagi = Double.parseDouble(routeinfoDetail.getRoute_lat());
                double Long = Double.parseDouble(routeinfoDetail.getRoute_long());
                Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + MyLat + "," + MyLong + "&daddr=" + Lagi + "," + Long);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } else if (view.getId() == R.id.img_btn_reason) {
                Log.e(TAG, "onClick of img_btn_reason = " + routeinfoDetail.getRoute_assetloc());

                Intent mIntent = new Intent(getApplicationContext(), NewComplaints.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("routeDetails", routeDetails);
                b.putSerializable("routeinfoDetail", routeinfoDetail);
                b.putSerializable("vehicleDetails", vehicleDetails);
                mIntent.setAction("DriveNewComplaint");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
                finish();

//                getBINReasonOnServer(routeinfoDetail);
            } else if (view.getId() == R.id.img_btn_scan_QR) {
                Log.e(TAG, "onClick of img_btn_reason = " + routeinfoDetail.getRoute_assetloc());

                Intent mIntent = new Intent(getApplicationContext(), UpdateBinDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("routeDetails", routeDetails);
                b.putSerializable("routeinfoDetail", routeinfoDetail);
                b.putSerializable("vehicleDetails", vehicleDetails);
                mIntent.setAction("getRouteDetail");
                mIntent.putExtra("SelectedVehicleDetail", b);
                startActivity(mIntent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());

        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
//        Intent intent = getIntent();
            Bundle extras = intent.getBundleExtra("SelectedVehicleDetail");
            Log.e(TAG, "onNewIntent" + intent.getAction());
            if (extras != null) {
                methodIntent = intent.getAction();
                Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
                if (methodIntent != null && methodIntent.equals("pendingRoute")) {
                    routeDetails = (RouteItem) extras.getSerializable("pendingRouteDetails");
                    vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");

                    Log.e(TAG, "RouteItem.getName() " + routeDetails.getName());
                    Log.e(TAG, "getVehicle_registration_no : " + vehicleDetails.getVehicle_registration_no());
//                Log.e(TAG, "RouteItem.getDa_routeid() " + routeDetails.getDa_routeid());
//                Log.e(TAG, "RouteItem.getR_status() " + routeDetails.getR_status());
                }
            } else {
                Log.e(TAG, "Bundle Is empty ");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e.getMessage());
        }
    }

    static int binCleanCount = 0;
    static int binPendingCount = 0;

    public void getRouteDetailOnServer(String routeId, int request_limit_start) {
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


                    filters = "[[\"ActivityMaster\",\"da_code\",\"in\",[\"" + routeId + "\"]]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/ActivityMaster?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start;
//                    String url = session.getMyServerIP() + "/api/resource/DriverActivity/" + URLEncoder.encode(routeId, "UTF-8") + "?fields=" + fields;
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
//                                            RouteItem routeDetail = objectMapper.readValue(statusData.toString(), RouteItem.class);


                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo ri = objectMapper.readValue(visitor.toString(), RouteInfo.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {

                                                    String binStatus = ri.getClean_status();
                                                    if (binStatus.equalsIgnoreCase(AppConfig.BIN_Status_Clean)) {
                                                        binCleanCount = binCleanCount + 1;
                                                    } else if (binStatus.equalsIgnoreCase(AppConfig.BIN_Status_Scheduled)) {
                                                        binPendingCount = binPendingCount + 1;
                                                    }

                                                    routeInfoList.add(ri);
                                                    routeMapListAdapter.notifyDataSetChanged();
                                                    try {
                                                        MarkerOptions markerOptions = new MarkerOptions();
                                                        LatLng latLng = new LatLng(Double.parseDouble(ri.getRoute_lat()), Double.parseDouble(ri.getRoute_long()));
                                                        markerOptions.position(latLng);

                                                        markerOptions.title("Loc : " + ri.getRoute_assetloc());
                                                        markerOptions.snippet("Status : " + ri.getClean_status());
                                                        mMap.addMarker(markerOptions);
                                                    } catch (Exception e) {
                                                        TastyToast.makeText(getApplicationContext(), "Something wrong", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                                    }
//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
                                                }
                                            }
                                            tv_bin_clean_count.setText(binCleanCount + "");
                                            tv_bin_pending_count.setText(binPendingCount + "");
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            if (routeInfoList.size() == binCleanCount) {
                                                updateRouteStatusServer(AppConfig.ROUTE_Status_Complete);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }

//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

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
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, PROGRASS_postDelayed);
        }
    }

    public void updateRouteStatusServer(String routeStatus) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {


            mProgressDialog = new ProgressDialog(RouteMapList.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");
                    String filters = null;
                    String fields = null;


                    Log.e(TAG, "selectComplaintsData.filters = " + filters);
//                    String url = session.getMyServerIP() + "/api/resource/DriverActivity?" + URLEncoder.encode(filters, "UTF-8");
                    String url = session.getMyServerIP() + "/api/resource/DriverActivity/" + URLEncoder.encode(routeDetails.getName(), "UTF-8");
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
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

                                            RouteItem compDetail = objectMapper.readValue(statusData.toString(), RouteItem.class);

                                            Log.e(TAG, "Responce DriverActivity getName= " + compDetail.getName());
                                            if (isServiceRunning() == true) {
                                                stopTrackingService();
                                            }
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }


                                            Intent mIntent1 = new Intent(getApplicationContext(), VehicleDetail.class); // the activity that holds the fragment
                                            Bundle b1 = new Bundle();
                                            b1.putSerializable("vehicleDetails", vehicleDetails);
                                            mIntent1.setAction("getVehicleDetail");
                                            mIntent1.putExtra("SelectedVehicleDetail", b1);
                                            startActivity(mIntent1);
                                            finish();
                                        } else {
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;

                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }

                            }, error -> {
                        // error

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();

                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject = new JSONObject();

                                jsonObject.put("r_status", routeStatus);

                                params.put("data", jsonObject.toString());
                            } catch (JSONException e) {
                                Log.e("JSONObject Here", e.toString());
                            }
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
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }

    }
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.Traccar.TrackingService".equals(service.service.getClassName())) {
                Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " TrackingService not Running  return false");
        return false;
    }
    private void stopTrackingService() {
//        alarmManager.cancel(alarmIntent);
        stopService(new Intent(this, TrackingService.class));
//        setPreferencesEnabled(true);
        Log.e(TAG, "DriverDashBoard TrackingService  stop");
    }

    public void getBINReasonOnServer(RouteInfo routeinfoDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(RouteMapList.this);
            mProgressDialog.setMessage("Please Wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + "/api/resource/BINReason?fields=" + URLEncoder.encode(fields, "UTF-8");
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
                                            binReasonInfoList.clear();
                                            binReasonDropDownAdapter.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo eventDetails = objectMapper.readValue(visitor.toString(), RouteInfo.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    binReasonInfoList.add(eventDetails);
                                                    binReasonDropDownAdapter.notifyDataSetChanged();
//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
                                                }
                                            }
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            dialog_reasonDetailConform(routeinfoDetail, binReasonInfoList);

                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }
                                    } catch (IOException e) {
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;
                                        }
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
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
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    Dialog dialog_reason;
    TextView tv_reason_confirm, tv_reason_cancel;
    Spinner sp_reason;
    static RouteInfo rfInfo = new RouteInfo();

    public void dialog_reasonDetailConform(RouteInfo routeInfo, List<RouteInfo> binReasonList) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_reason, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_reason = new Dialog(RouteMapList.this);
        dialog_reason.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_reason.setContentView(root);
        dialog_reason.setCancelable(false);
        Objects.requireNonNull(dialog_reason.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_reason.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        sp_reason = dialog_reason.findViewById(R.id.sp_reason);

        sp_reason.setAdapter(binReasonDropDownAdapter);

        tv_reason_confirm = dialog_reason.findViewById(R.id.tv_reason_confirm);
        tv_reason_cancel = dialog_reason.findViewById(R.id.tv_reason_cancel);

        tv_reason_confirm.setOnClickListener(v -> {
            int d = sp_reason.getSelectedItemPosition();
            rfInfo = binReasonList.get(d);
            routeInfo.setBin_reason(rfInfo.getBin_reason());
            updateReasonRequestServer(routeInfo);
        });
        tv_reason_cancel.setOnClickListener(this);
        dialog_reason.show();
    }

    public void updateReasonRequestServer_JsonObjectRequest(RouteInfo routeinfoDetail) {
        try {
            mProgressDialog = new ProgressDialog(RouteMapList.this);
            mProgressDialog.setMessage("Please Wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("bin_reason", routeinfoDetail.getBin_reason());

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                String url = null;

                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            RouteInfo compDetail = objectMapper.readValue(statusData.toString(), RouteInfo.class);

                                            Log.e(TAG, "DriverActivity response Successfully Updated against getName= " + compDetail.getName());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), "Your Route Aborted", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                            dialog_reason.dismiss();
//                                            Intent mIntent = new Intent(getApplicationContext(), DriverDashBoard.class); // the activity that holds the fragment
//                                            startActivity(mIntent);
//                                            finish();
                                        } else {
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }
                                    } else {
                                        Log.e(TAG, "Response Error");
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
            } finally {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

            }
        }, PROGRASS_postDelayed);
    }

    public void updateReasonRequestServer(RouteInfo routeinfoDetail) {
        try {
            mProgressDialog = new ProgressDialog(RouteMapList.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {

                String url = null;

                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");

                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
                        new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            RouteInfo compDetail = objectMapper.readValue(statusData.toString(), RouteInfo.class);

                                            Log.e(TAG, "DriverActivity response Successfully Updated against getName= " + compDetail.getName());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), "Your Route Aborted", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                            dialog_reason.dismiss();
//                                            Intent mIntent = new Intent(getApplicationContext(), DriverDashBoard.class); // the activity that holds the fragment
//                                            startActivity(mIntent);
//                                            finish();
                                        } else {
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }
                                    } else {
                                        Log.e(TAG, "Response Error");
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in response upload image RequestServer ");
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        JSONObject jsonObject = new JSONObject();

                        try {
                            jsonObject = new JSONObject();

                            jsonObject.put("bin_reason", routeinfoDetail.getBin_reason());
                            params.put("data", jsonObject.toString());
                        } catch (JSONException e) {
                            Log.e("JSONObject Here", e.toString());
                        }
                        return params;
                    }

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
            } finally {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

            }
        }, PROGRASS_postDelayed);
    }

    Dialog dialog_expense;
    TextView tv_expanse_detail_cancel, tv_expanse_detail_confirm;
    TextView tv_expense_Mno, tv_expense_unm;
    Spinner sp_typeOfExpanses;
    TextInputEditText edt_expenseAmount;

    public void dialog_expenseDetailConform() {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_expance, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_expense = new Dialog(RouteMapList.this);
        dialog_expense.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_expense.setContentView(root);
        dialog_expense.setCancelable(false);
        Objects.requireNonNull(dialog_expense.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_expense.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        sp_typeOfExpanses = dialog_expense.findViewById(R.id.sp_typeOfExpanses);
        tv_expense_Mno = dialog_expense.findViewById(R.id.tv_expense_Mno);
        tv_expense_unm = dialog_expense.findViewById(R.id.tv_expense_unm);
        edt_expenseAmount = dialog_expense.findViewById(R.id.edt_expenseAmount);

        tv_expense_unm.setText(session.getpsName());
        tv_expense_Mno.setText("(M) : " + session.getMobileNumber());

        tv_expanse_detail_confirm = dialog_expense.findViewById(R.id.tv_expanse_detail_confirm);
        tv_expanse_detail_cancel = dialog_expense.findViewById(R.id.tv_expanse_detail_cancel);

        tv_expanse_detail_confirm.setOnClickListener(v -> {
            dialog_expense.dismiss();


        });
        tv_expanse_detail_cancel.setOnClickListener(v -> {
            dialog_expense.dismiss();
        });

        dialog_expense.show();
    }

    MenuItem itemMap, itemList, navigation_expense, navigation_my_voucher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_nearby_place_detail, menu);
        itemMap = menu.findItem(R.id.navigation_mapView);
        navigation_expense = menu.findItem(R.id.navigation_expense);
        navigation_my_voucher = menu.findItem(R.id.navigation_my_voucher);
        itemList = menu.findItem(R.id.navigation_listView);
        itemList.setVisible(false);
        navigation_expense.setVisible(true);
        navigation_my_voucher.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.navigation_mapView:
                itemMap.setVisible(false);
                itemList.setVisible(true);
                ll_list.setVisibility(View.GONE);
                return true;
            case R.id.navigation_listView:
                itemMap.setVisible(true);
                itemList.setVisible(false);
//                nearByPlaceListAdapter.notifyDataSetChanged();
                ll_list.setVisibility(View.VISIBLE);
                return true;
            case R.id.navigation_expense:
                Intent mIntent1 = new Intent(getApplicationContext(), DriverExpenseList.class); // the activity that holds the fragment
                Bundle b1 = new Bundle();
                b1.putSerializable("pendingRouteDetails", routeDetails);
                b1.putSerializable("vehicleDetails", vehicleDetails);
                mIntent1.setAction("pendingRoute");
                mIntent1.putExtra("SelectedVehicleDetail", b1);
                startActivity(mIntent1);
                finish();
//                dialog_expenseDetailConform();
                return true;

            case R.id.navigation_my_voucher:
                Log.e(TAG, "da_vehicleno = " + routeDetails.getDa_vehicleno());

                Intent mIntent = new Intent(getApplicationContext(), DriverReceivedVoucher.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("routeDetails", routeDetails);
                b.putSerializable("vehicleDetails", vehicleDetails);
                mIntent.setAction("getRouteDetail");
                mIntent.putExtra("SelectedVehicleDetail", b);
                startActivity(mIntent);
                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(RouteMapList.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.Finding_Location));
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                Location loc = getLastKnownLocation(this);
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
                Log.e(TAG, "My Location MyLat" + MyLat);
                Log.e(TAG, "My Location MyLong" + MyLong);

                LatLng latLng = new LatLng(MyLat, MyLong);
//                mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                progressDialog.dismiss();


                getRouteDetailOnServer(routeDetails.getName(), 0);

            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, 2000);

    }

    Double MyLat, MyLong;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    Location location;

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

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

    }

    protected void aleartforBack(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//
//                    // do something when the button is clicked
//                    public void onClick(DialogInterface arg0, int arg1) {
////                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
//                    }
//                })
                .show();

    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message =getString(R.string.you_are_offline);
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
//        super.onBackPressed();

        if (routeInfoList.size() == binCleanCount) {
            String msg = getString(R.string.msg_please_complete_route);
            aleartforBack(msg);
        } else {
            String msg = getString(R.string.pleasecomplete_route_you_have_still)
                    + binPendingCount
                    + getString(R.string.bin_cleaning_pending);
            aleartforBack(msg);
        }


    }

}
