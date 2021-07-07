package com.ecosense.app.activity.supervisor;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.AssetsTrackersListAdapter;
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;
import com.ecosense.app.pojo.Fuel;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class POIStatus extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = POIStatus.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.srl_POIStatus)
    SwipeRefreshLayout srl_POIStatus;

    @BindView(R.id.rv_POIStatus)
    RecyclerView rv_POIStatus;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;


    @BindView(R.id.sp_assets_wardNumber)
    Spinner sp_assets_wardNumber;

//    @BindView(R.id.sp_assets_routeNumber)
//    Spinner sp_assets_routeNumber;

    @BindView(R.id.btn_assets_submit)
    MaterialButton btn_assets_submit;

    @BindView(R.id.tiedt_count_clean)
    TextInputEditText tiedt_count_clean;

    @BindView(R.id.tiedt_count_pending)
    TextInputEditText tiedt_count_pending;

    @BindView(R.id.tiedt_count_scheduled)
    TextInputEditText tiedt_count_scheduled;

    static private int count_clean = 0;
    static private int count_pending = 0;
    static private int count_scheduled = 0;

    private GridLayoutManager lLayout;
    AssetsTrackersListAdapter assetsTrackersListAdapter;
    List<Assets> assetsTrackersList;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;

    private boolean isLoading = true;
    static String methodIntent = null;
    static Fuel fuelDetail;
    ArrayList<String> wardNoList = null;
    DropDownStringAdapter adapter_wardNo = null;

    ArrayList<String> routeNoList = null;
    DropDownStringAdapter adapter_routeNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poistatus);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("POI Status");
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

        wardNoList = new ArrayList<>();
        adapter_wardNo = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, wardNoList);

        routeNoList = new ArrayList<>();
        adapter_routeNo = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, routeNoList);

        getwardNoListOnServer();
        sp_assets_wardNumber.setAdapter(adapter_wardNo);


//        sp_assets_wardNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String wno = parent.getItemAtPosition(position).toString();
//                //
//                Log.e(TAG, "WNo = " + wno);
////                getrouteNoListOnServer(wno);
//            } // to close the onItemSelected
//
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        assetsTrackersList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_POIStatus.setHasFixedSize(true);
        rv_POIStatus.setLayoutManager(lLayout);
        rv_POIStatus.setItemAnimator(new DefaultItemAnimator());
        assetsTrackersListAdapter = new AssetsTrackersListAdapter(getApplicationContext(), assetsTrackersList);

        rv_POIStatus.setAdapter(assetsTrackersListAdapter);

/*
        rv_POIStatus.addOnScrollListener(new RecyclerView.OnScrollListener() {


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
                        String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
                        String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

                        getAssetsBinDetailOnServer(wardNo, routeNo,"NA", "NA", totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });
*/
        srl_POIStatus.setOnRefreshListener(this);
        srl_POIStatus.setColorSchemeResources(R.color.colorAccent);
        srl_POIStatus.setNestedScrollingEnabled(true);
        srl_POIStatus.post(
                new Runnable() {
                    @Override
                    public void run() {

                    }
                }
        );

        btn_assets_submit.setOnClickListener(this);
    }

    @Override
    public void onRefresh() {
        srl_POIStatus.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {
            assetsTrackersList.clear();
            assetsTrackersListAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;
            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
//            String routeNo = sp_assets_routeNumber.getSelectedItem().toString();
            getAssetsBinDetailOnServer(wardNo, "NA", "NA", "NA", 0);
        } catch (Exception e) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_POIStatus.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_assets_submit) {
            try {
                assetsTrackersList.clear();
                assetsTrackersListAdapter.notifyDataSetChanged();

                previous_Item_Total = 0;
                isLoading = true;


                String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
//                String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

                getAssetsBinDetailOnServer(wardNo, "NA", "NA", "NA", 0);
            } catch (Exception e) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }

        }
    }

    public void getAssetsBinDetailOnServer(String wardNumber, String routeNumber, String filterStatus, String filterDate, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {
            count_clean = 0;
            count_pending = 0;
            count_scheduled = 0;
            tiedt_count_clean.setText(count_clean + "");
            tiedt_count_pending.setText(count_pending + "");
            tiedt_count_scheduled.setText(count_scheduled + "");
            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;
                    String url = null;
                    if (routeNumber.equalsIgnoreCase("NA")) {
                        url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_bin_status + "?id=" + URLEncoder.encode(wardNumber, "UTF-8");
                    } else {
                        url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_bin_cleared_filter
                                + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8")
                                + "&ward_no=" + URLEncoder.encode(wardNumber, "UTF-8")
                                + "&route_no=" + URLEncoder.encode(routeNumber, "UTF-8")
                                + "&status=" + URLEncoder.encode(filterStatus, "UTF-8")
                                + "&for_date=" + URLEncoder.encode(filterDate, "UTF-8");
                    }

                    Log.e(TAG, "url = " + url);
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
                                                    if (assetsDetail.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Clean)) {
                                                        count_clean = count_clean + 1;
                                                    } else if (assetsDetail.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Scheduled)) {
                                                        count_scheduled = count_scheduled + 1;
                                                    } else if (assetsDetail.getFastatus().equalsIgnoreCase(AppConfig.BIN_Status_Pending)) {
                                                        count_pending = count_pending + 1;
                                                    }
                                                    assetsTrackersList.add(assetsDetail);
                                                    assetsTrackersListAdapter.notifyDataSetChanged();

                                                }
                                            }
                                            tiedt_count_clean.setText(count_clean + "");
                                            tiedt_count_pending.setText(count_pending + "");
                                            tiedt_count_scheduled.setText(count_scheduled + "");
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

    public void getwardNoListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(POIStatus.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_ward_no + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
                    Log.e(TAG, "url = " + url);
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
                                            wardNoList.clear();
                                            adapter_wardNo.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Assets fuelDetails = objectMapper.readValue(visitor.toString(), Assets.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    wardNoList.add(fuelDetails.getFawardno());
                                                    adapter_wardNo.notifyDataSetChanged();
                                                }
                                            }

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

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

    public void getrouteNoListOnServer(String wardNO) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.please_wait_find_route));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;
                    routeNoList.clear();
                    adapter_routeNo.notifyDataSetChanged();
                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_route_info + "?id=" + URLEncoder.encode(wardNO, "UTF-8");
                    Log.e(TAG, "url = " + url);
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
                                                Assets fuelDetails = objectMapper.readValue(visitor.toString(), Assets.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    routeNoList.add(fuelDetails.getR_name());
                                                    adapter_routeNo.notifyDataSetChanged();
                                                }
                                            }
                                            showBottomSheet();
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

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


    public void setSpinnerData(Spinner spname, int arrayId) {
        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNumber);

    }

    public void setWardNumber_RouteNo(Spinner spname, int arrayId, String locType) {
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNo);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            spname.setSelection(spinnerPosition);
        }
    }
    EditText txtSearch;
    private SearchView searchView = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.supervisor_home, menu);

        MenuItem homeMenuItem = menu.findItem(R.id.action_home);
        homeMenuItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_filter_search);
        searchItem.setVisible(true);

        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);
        filterMenuItem.setVisible(true);


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
        txtSearch.setHint(getString(R.string.search_by_location));
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.tab_text));
        txtSearch.setTextColor(ContextCompat.getColor(this, R.color.white));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            getrouteNoListOnServer(sp_assets_wardNumber.getSelectedItem().toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            }else {
                text="";
            }
            final String text2 = model.getAsset_name().toLowerCase();


            if (text.contains(query) || text2.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    ImageView img_closeBtn;
    BottomSheetDialog bottomSheetDialog;
    BottomSheetBehavior bottomSheetBehavior;
    View bottomSheetView;
    MaterialButtonToggleGroup toggle_btn_POI_Status_group;
    MaterialButton btn_btn_filter_apply;
    TextInputEditText tiedt_date;
    Spinner sp_routeNumber;
    private int mYear;
    private int mMonth;
    private int mDay;
    String filter_POIStatus = "NA";
    String filter_Date = "NA";

    public void showBottomSheet() {
        try {
            bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_poi_filter, null);
            bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setCancelable(false);
            bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

            btn_btn_filter_apply = bottomSheetDialog.findViewById(R.id.btn_btn_filter_apply);

            img_closeBtn = bottomSheetDialog.findViewById(R.id.img_closeBtn);
            toggle_btn_POI_Status_group = bottomSheetDialog.findViewById(R.id.toggle_btn_POI_Status_group);
            tiedt_date = bottomSheetDialog.findViewById(R.id.tiedt_date);
            sp_routeNumber = bottomSheetDialog.findViewById(R.id.sp_assets_routeNumber);

            btn_btn_filter_apply.setEnabled(true);

            sp_routeNumber.setAdapter(adapter_routeNo);


            tiedt_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openDatePicker(tiedt_date);
                    btn_btn_filter_apply.setEnabled(true);
                }
            });
            img_closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bottomSheetDialog.dismiss();
                }
            });
            btn_btn_filter_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (filter_POIStatus != null && !filter_POIStatus.equalsIgnoreCase("")) {
//                        Log.e(TAG, "filter_POIStatus filter = " + filter_POIStatus);
//                    }
//                    if (tiedt_date.getText().toString() != null) {

                    Log.e(TAG, "tiedt_date filter = " + filter_Date);
                    Log.e(TAG, "filter_POIStatus filter = " + filter_POIStatus);
//
//
//                    }

                    try {
                        assetsTrackersList.clear();
                        assetsTrackersListAdapter.notifyDataSetChanged();

                        previous_Item_Total = 0;
                        isLoading = true;

                        String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
                        String routeNo = sp_routeNumber.getSelectedItem().toString();
                        getAssetsBinDetailOnServer(wardNo, routeNo, filter_POIStatus, filter_Date, 0);

                        bottomSheetDialog.dismiss();

                        filter_POIStatus = "NA";
                        filter_Date = "NA";
                    } catch (Exception e) {
                        TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    }
                }
            });
            toggle_btn_POI_Status_group.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

                if (toggle_btn_POI_Status_group.getCheckedButtonId() == R.id.mtbtn_Clean) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_POIStatus = AppConfig.BIN_Status_Clean;
                } else if (toggle_btn_POI_Status_group.getCheckedButtonId() == R.id.mtbtn_Pending) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_POIStatus = AppConfig.BIN_Status_Pending;
                } else if (toggle_btn_POI_Status_group.getCheckedButtonId() == R.id.mtbtn_Scheduled) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_POIStatus = AppConfig.BIN_Status_Scheduled;
                }

            });
            bottomSheetDialog.show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception e) {
            Log.e(TAG, "In showBottomSheet error = > " + e.getMessage());

            Log.e(TAG, "In showBottomSheet Exception flagBottomSheetDialog =0");
            e.printStackTrace();
        }
    }

    public void openDatePicker(TextInputEditText editTextDate) {
        final TextInputEditText edtDate = editTextDate;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String day = "";
                        if (dayOfMonth < 10)
                            day = "0" + dayOfMonth;
                        else
                            day = String.valueOf(dayOfMonth);


                        String month = "";
                        if ((monthOfYear + 1) < 10)
                            month = "0" + (monthOfYear + 1);
                        else
                            month = String.valueOf((monthOfYear + 1));


                        edtDate.setText(day + "-" + month + "-" + year);
                        filter_Date = year + "-" + month + "-" + day;

                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            Log.e(TAG, "BottomSheetBehavior COLLAPSED");
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            Log.e(TAG, "BottomSheetBehavior DRAGGING");
//                            bottomSheetDialog.dismiss();
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            Log.e(TAG, "BottomSheetBehavior EXPANDED");
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            Log.e(TAG, "BottomSheetBehavior HIDDEN");

                            Log.e(TAG, "In BottomSheetBehavior HIDDEN flagBottomSheetDialog =0");
                            bottomSheetDialog.dismiss();
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            Log.e(TAG, "BottomSheetBehavior SETTLING");
                            break;
                        default:
                            Log.e(TAG, "BottomSheetBehavior unknown");
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            };

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
        startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
    }
}
