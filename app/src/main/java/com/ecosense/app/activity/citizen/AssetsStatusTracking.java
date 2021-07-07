package com.ecosense.app.activity.citizen;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.AssetsTrackersListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;
import com.ecosense.app.pojo.RouteInfo;

import static com.ecosense.app.helper.AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST;
import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class AssetsStatusTracking extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, OnMapReadyCallback,
        LocationListener, GoogleMap.OnMarkerClickListener {

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
    LocationManager locationManager;

    private static final String TAG = AssetsStatusTracking.class.getSimpleName();
    private Toolbar toolbar;
    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    static ProgressDialog mProgressDialog = null;

    @BindView(R.id.srl_Bin_Status)
    SwipeRefreshLayout srl_Bin_Status;

    @BindView(R.id.rv_Bin_Status)
    RecyclerView rv_Bin_Status;


    @BindView(R.id.ll_list)
    RelativeLayout ll_list;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    private LinearLayoutManager linearLayoutManager;
    AssetsTrackersListAdapter assetsTrackersListAdapter;
    List<Assets> assetsTrackersList;
    private SearchView searchView = null;


    @BindView(R.id.sp_assets_wardNumber)
    Spinner sp_assets_wardNumber;

    @BindView(R.id.btn_assets_submit)
    Button btn_assets_submit;


    // Store a pagination variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assets_status_tracking);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("POI Status");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        ButterKnife.bind(this);


        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, getString(R.string.you_are_offline));
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_assets_wardNumber.setAdapter(adapter_wardNumber);

        assetsTrackersList = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        rv_Bin_Status.setHasFixedSize(true);
        rv_Bin_Status.setLayoutManager(linearLayoutManager);
        rv_Bin_Status.setItemAnimator(new DefaultItemAnimator());
        assetsTrackersListAdapter = new AssetsTrackersListAdapter(getApplicationContext(), assetsTrackersList);
        rv_Bin_Status.setAdapter(assetsTrackersListAdapter);

//        rv_Bin_Status.addOnScrollListener(new RecyclerView.OnScrollListener() {
//
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                Log.e(TAG, " onScrolled isLoading = " + isLoading + "  dy= " + dy);
//
//                if (dy > 0) {
//
//
//                    int visibleItemCount = linearLayoutManager.getChildCount();
//                    int totalItemCount = linearLayoutManager.getItemCount();
//                    int pastVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//                    Log.e(TAG, "visibleItemCount = " + visibleItemCount);
//                    Log.e(TAG, "totalItemCount = " + totalItemCount);
//                    Log.e(TAG, "pastVisibleItemPosition = " + pastVisibleItemPosition);
////                    Log.e(TAG, "(visibleItemCount + pastVisibleItemPosition) = " + (visibleItemCount + pastVisibleItemPosition));
//                    Log.e(TAG, "preLastVisible = " + previous_Item_Total);
//                    Log.e(TAG, "(totalItemCount - visibleItemCount) = " + (totalItemCount - visibleItemCount));
//                    Log.e(TAG, "(pastVisibleItemPosition + view_thereshold) = " + (pastVisibleItemPosition + view_thereshold));
//
//                    Log.e(TAG, "before isLoading isLoading = " + isLoading);
//                    if (isLoading) {
//                        if (totalItemCount > previous_Item_Total) {
//                            isLoading = false;
//                            previous_Item_Total = totalItemCount;
//                            Log.e(TAG, "in (totalItemCount > previous_Item_Total) = " + isLoading + " \t previous_Item_Total = " + previous_Item_Total);
//                        }
//                    }
//                    if (!isLoading && ((totalItemCount - visibleItemCount)
//                            <= (pastVisibleItemPosition + view_thereshold))) {
////                    if ((visibleItemCount + pastVisibleItemPosition) >= totalItemCount && previous_Item_Total != totalItemCount) {
////                        previous_Item_Total = totalItemCount;
//                        getAssetsBinDetailOnServerOnLoad(sp_assets_wardNumber.getSelectedItem().toString(), totalItemCount);
//                        isLoading = true;
//                        Log.e(TAG, "in main if = " + isLoading);
//                    }
//
//                }
//            }
//        });

        srl_Bin_Status.setOnRefreshListener(this);
        srl_Bin_Status.setColorSchemeResources(R.color.colorAccent);
        srl_Bin_Status.setNestedScrollingEnabled(true);
        srl_Bin_Status.post(
                new Runnable() {
                    @Override
                    public void run() {
/* obsrvation Nikhil Sir Word File send On  23/08/2019 In mail comment finding location
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showLocationSettings();
                            getMyCurrentLocation();
                        }*/
                    }
                }
        );
        btn_assets_submit.setOnClickListener(this);
//        loadFragment(new BinStatusListFragment());
    }


    @Override
    public void onClick(View v) {
        if (v == btn_assets_submit) {
            if (itemList.isVisible()) {
                itemList.setVisible(false);
                itemMap.setVisible(true);
                ll_list.setVisibility(View.VISIBLE);
            }
            mMap.clear();
            assetsTrackersList.clear();
            assetsTrackersListAdapter.notifyDataSetChanged();

            previous_Item_Total = 0;
            isLoading = true;

            getAssetsBinDetailOnServerOnLoad(sp_assets_wardNumber.getSelectedItem().toString(), 0);
//            getAssetsBinDetailOnServer(sp_assets_wardNumber.getSelectedItem().toString());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

//        String locAddress = marker.getTitle();
//
//        if (previousMarker != null) {
//            previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        }
//        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//        previousMarker = marker;
        return true;
    }


    @Override
    public void onRefresh() {
        srl_Bin_Status.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        mMap.clear();
        assetsTrackersList.clear();
        assetsTrackersListAdapter.notifyDataSetChanged();


        previous_Item_Total = 0;
        isLoading = true;
//        if (session.getuserWardNo() != null) {
//            getAssetsBinDetailOnServer(session.getuserWardNo());
//            setWardNumber(session.getuserWardNo());
//        } else {
        getAssetsBinDetailOnServerOnLoad(sp_assets_wardNumber.getSelectedItem().toString(), 0);
//        getAssetsBinDetailOnServer(sp_assets_wardNumber.getSelectedItem().toString());
//        }
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//
//            getMyCurrentLocation();
//        }
        srl_Bin_Status.setRefreshing(false);
    }

    public void getAssetsBinDetailOnServerOnLoad(String WardNumber, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;


//
//                    filters = "[[\"Item\",\"ward_no\",\"in\",[\"" + WardNumber
//                            + "\"]]]";


//                    filters = "[[\"Item\",\"type_of_item\",\"in\",[\"BIN\",\"Open Spot\"]],"
//                            + "[\"Item\",\"ward_no\",\"in\",[\"" + WardNumber
//                            + "\"]]]";

//                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
//                    limit_page_length = view_thereshold;
//                    limit_start = request_limit_start;
//                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

//                    String url = session.getMyServerIP() + "/api/resource/Facility_And_Assets?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start;
//                    String url = session.getMyServerIP() + "/api/resource/Item?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start;
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_bin_status + "?id=" + URLEncoder.encode(WardNumber, "UTF-8");

                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("message");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");

                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Assets assetsDetail = objectMapper.readValue(visitor.toString(), Assets.class);
                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
//                                                    getAssetsStatusOnServer(assetsDetail);
                                                    assetsTrackersList.add(assetsDetail);
                                                    assetsTrackersListAdapter.notifyDataSetChanged();
                                                    if (assetsDetail.getFalatitude() != null || assetsDetail.getFalongitude() != null) {
                                                        addBinOnMap(assetsDetail);
                                                    }else{
                                                    Log.e(TAG, "NOt Find LAt,LONG getAsset_Id() = " + assetsDetail.getAsset_Id()+"  = , getFalatitude & getFalongitude not avalable = ");
                                                }
                                                }
                                            }

                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                        } else {

                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    public void addBinOnMap(Assets assetsItem) {
        try {

            Log.e(TAG, "Marker Add to Loc = " + assetsItem.getFalocation()
                    + "\t getFalatitude()= " + assetsItem.getFalatitude()
                    + "\t getFalongitude()= " + assetsItem.getFalongitude()
            );
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(Double.parseDouble(assetsItem.getFalatitude()), Double.parseDouble(assetsItem.getFalongitude()));
            markerOptions.position(latLng);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.throw_to_paper_bin2))
            markerOptions.title(assetsItem.getAsset_name());
            markerOptions.snippet("Loc : " + assetsItem.getFalocation() + "\nStatus : " + assetsItem.getFastatus());
            mMap.addMarker(markerOptions);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAssetsStatusOnServer(Assets assetsDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

//            mProgressDialog = new ProgressDialog(AssetsStatusTracking.this);
//            mProgressDialog.setMessage("Please Wait...");
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();
//            new android.os.Handler().postDelayed(() -> {

            try {
                String filters = null;
                String fields = null;
                int limit_page_length;
                int limit_start;


//                    filters = "[[\"ActivityMaster\",\"route_location_name\",\"=\",[\"" + routeId + "\"]]]";
                filters = "[[\"ActivityMaster\",\"route_location_name\",\"=\",\"" + assetsDetail.getAsset_Id() + "\"],"
                        + "[\"ActivityMaster\",\"modified\",\"like\",\"" + Connection.getCurrentDate() + "%"
                        + "\"]]";

                fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                Log.e(TAG, "filters = " + filters + "&fields=" + fields);

                String url = session.getMyServerIP() + "/api/resource/ActivityMaster?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields;
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
                                                Log.e(TAG, "Data ri.getName = " + ri.getName());
//                                                Log.e(TAG, "Data ri.getName = " + ri.getName());
//                                                Log.e(TAG, "Data getRoute_location_name = " + ri.getRoute_location_name());
//                                                Log.e(TAG, "Data getDa_code = " + ri.getDa_code());
//                                                Log.e(TAG, "Data binStatus = " + binStatus);

//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
                                                assetsDetail.setFastatus(binStatus);
                                                assetsDetail.setModified(ri.getModified());
                                                assetsTrackersList.add(assetsDetail);
                                                assetsTrackersListAdapter.notifyDataSetChanged();
                                                addBinOnMap(assetsDetail);
                                            }
                                            break;
                                        }
                                    } else {
                                        Log.e(TAG, "Nodata Data  = " + assetsDetail.getAsset_Id());
                                        assetsDetail.setFastatus(AppConfig.BIN_Status_Pending);
                                        assetsTrackersList.add(assetsDetail);
                                        assetsTrackersListAdapter.notifyDataSetChanged();
                                        addBinOnMap(assetsDetail);
//                                           TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                    }
                                    Log.e(TAG, "assetsTrackersList.size()  = " + assetsTrackersList.size());
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
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
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
            }
//            }, PROGRASS_postDelayed);
        }
    }

    Dialog dialog_wardDetail;
    TextView tv_ward_confirm, tv_ward_cancel;
    Spinner sp_wardNumber;

    public void dialog_wardNumberDetailConform() {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_ward_deatail, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_wardDetail = new Dialog(AssetsStatusTracking.this);
        dialog_wardDetail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_wardDetail.setContentView(root);
        dialog_wardDetail.setCancelable(false);
        Objects.requireNonNull(dialog_wardDetail.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_wardDetail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        sp_wardNumber = dialog_wardDetail.findViewById(R.id.sp_wardNumber);

        tv_ward_confirm = dialog_wardDetail.findViewById(R.id.tv_ward_confirm);
        tv_ward_cancel = dialog_wardDetail.findViewById(R.id.tv_ward_cancel);

        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(AssetsStatusTracking.this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_wardNumber.setAdapter(adapter_wardNumber);

        tv_ward_confirm.setOnClickListener(v -> {
            dialog_wardDetail.dismiss();

        });
        tv_ward_cancel.setOnClickListener(v -> {
            dialog_wardDetail.dismiss();
        });

        dialog_wardDetail.show();
    }


    MenuItem itemMap, itemList, searchItem;
    EditText txtSearch;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_nearby_place_detail, menu);
        itemMap = menu.findItem(R.id.navigation_mapView);
        itemList = menu.findItem(R.id.navigation_listView);
        searchItem = menu.findItem(R.id.action_filter_search);
        searchItem.setVisible(true);

        itemList.setVisible(false);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            assert searchManager != null;
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        // Associate searchable configuration with the SearchView


        searchView.setMaxWidth(Integer.MAX_VALUE);

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_baseline_close_24);

//        txtSearch.setHighlightColor(ContextCompat.getColor(this, R.color.read_linearLayoutBg));
//        txtSearch.setAllCaps(true);
        txtSearch = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search by location");
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.tab_text));
        txtSearch.setTextColor(ContextCompat.getColor(this, R.color.white));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.navigation_mapView:
                searchItem.setVisible(false);
                itemMap.setVisible(false);
                itemList.setVisible(true);
                ll_list.setVisibility(View.GONE);
                return true;
            case R.id.navigation_listView:

                itemMap.setVisible(true);
                searchItem.setVisible(true);
                itemList.setVisible(false);
//                nearByPlaceListAdapter.notifyDataSetChanged();
                ll_list.setVisibility(View.VISIBLE);


                searchView.clearFocus();
                searchView.setIconified(true);
                searchItem.collapseActionView();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
//        final ProgressDialog progressDialog = new ProgressDialog(AssetsStatusTracking.this);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage(getString(R.string.Finding_Location));
//        progressDialog.show();
//        new android.os.Handler().postDelayed(() -> {

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
            //            //if(!gps_enabled && !network_enabled)
            //            //return false;

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
                Log.e(TAG, "" + MyLat);
                Log.e(TAG, "" + MyLong);

                LatLng latLng = new LatLng(MyLat, MyLong);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.throw_to_paper_bin2))
                markerOptions.title("My Location");
                if (session.getMobileNumber() != null) {
                    markerOptions.snippet(session.getMobileNumber());
                }
                mMap.addMarker(markerOptions);


//                progressDialog.dismiss();

//                if (session.getuserWardNo() != null) {
//                    setWardNumber(session.getuserWardNo());
//                    getAssetsBinDetailOnServer(session.getuserWardNo());
//                } else {
//                    getAssetsBinDetailOnServer(sp_wardNumber.getSelectedItem().toString());
//                }


            } catch (Exception e) {
                e.printStackTrace();
//                progressDialog.dismiss();
            }
//        }, 2000);

    }

    public void setWardNumber(String locType) {
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_assets_wardNumber.setAdapter(adapter_wardNo);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            sp_assets_wardNumber.setSelection(spinnerPosition);
        }
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
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

//    @Override
//    public void onPause() {
//        Log.e(TAG, "onPause => " + TAG);
//        super.onPause();
//        if (mProgressDialog != null)
//            mProgressDialog.dismiss();
//    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // filter recycler view when query submitted
//        metaDataListAdapter.getFilter().filter(query);

        final List<Assets> filteredModelList = filter(assetsTrackersList, query);
        assetsTrackersListAdapter.setFilter(filteredModelList);
        return true;
    }


    private List<Assets> filter(List<Assets> models, String query) {
        query = query.toLowerCase();
        final List<Assets> filteredModelList = new ArrayList<>();
        for (Assets model : models) {
            final String text;
            if (model.getFalocation() != null) {
                text = model.getFalocation().toLowerCase();
            } else {
                text = "";
            }
            final String text2 = model.getAsset_name().toLowerCase();


            if (text.contains(query) || text2.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frame_container, fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
