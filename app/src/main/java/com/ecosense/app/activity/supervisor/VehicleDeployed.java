package com.ecosense.app.activity.supervisor;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
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
import android.widget.AdapterView;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
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
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.adapter.VehicleDeployedDetailAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Fuel;
import com.ecosense.app.pojo.RouteInfo;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class VehicleDeployed extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = VehicleDeployed.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.srl_VehicleDeployed)
    SwipeRefreshLayout srl_VehicleDeployed;

    @BindView(R.id.rv_VehicleDeployed)
    RecyclerView rv_VehicleDeployed;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.ll_list)
    RelativeLayout ll_list;

    @BindView(R.id.btn_Total)
    MaterialButton btn_Total;

    @BindView(R.id.btn_Deployed)
    MaterialButton btn_Deployed;

    @BindView(R.id.btn_maintenance)
    MaterialButton btn_maintenance;
    @BindView(R.id.btn_NOtDeployed)
    MaterialButton btn_NOtDeployed;


    private GridLayoutManager lLayout;
    VehicleDeployedDetailAdapter vehicleDeployedDetailAdapter;
    List<RouteInfo> routeInfoList;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;

    private boolean isLoading = true;
    static String methodIntent = null;
    static Fuel fuelDetail;
    static String API_Selected = "";
    static int total_vh_count = 0;
    static int deployed_vh_count = 0;
    static int not_deployed_vh_count = 0;
    static int maintenance_vh_count = 0;
    ArrayList<String> list_vehileType = null;
    DropDownStringAdapter adapter__vehileType = null;

    ArrayList<String> list_reson = null;
    DropDownStringAdapter adapter__reson = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vechile_deployed);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Vehicle Deployed");
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
        list_vehileType = new ArrayList<>();
        adapter__vehileType = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, list_vehileType);

        list_reson = new ArrayList<>();
        adapter__reson = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, list_reson);

        routeInfoList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_VehicleDeployed.setHasFixedSize(true);
        rv_VehicleDeployed.setLayoutManager(lLayout);
        rv_VehicleDeployed.setItemAnimator(new DefaultItemAnimator());
        vehicleDeployedDetailAdapter = new VehicleDeployedDetailAdapter(getApplicationContext(), routeInfoList);

        rv_VehicleDeployed.setAdapter(vehicleDeployedDetailAdapter);


        rv_VehicleDeployed.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

        srl_VehicleDeployed.setOnRefreshListener(this);
        srl_VehicleDeployed.setColorSchemeResources(R.color.colorAccent);
        srl_VehicleDeployed.setNestedScrollingEnabled(true);
        srl_VehicleDeployed.post(
                new Runnable() {
                    @Override
                    public void run() {
                        srl_VehicleDeployed.setRefreshing(true);
                        API_Selected = AppConfig.API_vehicle_deployed_all;
                        total_vh_count = 0;
                        deployed_vh_count = 0;
                        not_deployed_vh_count = 0;
                        maintenance_vh_count = 0;
                        btn_Total.setText(routeInfoList.size() + "");
                        btn_Deployed.setText(deployed_vh_count + "");
                        btn_NOtDeployed.setText((not_deployed_vh_count - maintenance_vh_count) + "");
                        btn_maintenance.setText(maintenance_vh_count + "");
                        getVehicleDetailOnServer(API_Selected, "NA", "NA", 0);
                        srl_VehicleDeployed.setRefreshing(false);
                    }
                }
        );
        btn_Total.setOnClickListener(this);
        btn_Deployed.setOnClickListener(this);
        btn_maintenance.setOnClickListener(this);
        btn_NOtDeployed.setOnClickListener(this);

    }

    @Override
    public void onRefresh() {
        srl_VehicleDeployed.setRefreshing(true);
//        dialog_wardNumberDetailConform();

        APIRequest("NA");

        srl_VehicleDeployed.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_Total) {
            ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_total));
            API_Selected = AppConfig.API_vehicle_deployed_all;
            APIRequest("NA");
        }
        if (v == btn_Deployed) {
            ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_deployed));
            API_Selected = AppConfig.API_vehicle_deployed;
            APIRequest("NA");
        }
        if (v == btn_NOtDeployed) {
            ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_not_eployed));
            API_Selected = AppConfig.API_vehicle_not_deployed;
            APIRequest("NA");
        }
        if (v == btn_maintenance) {
            ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_maintenance));
            API_Selected = AppConfig.API_vehicle_not_deployed;
            APIRequest("Maintenance");
        }

    }

    private void APIRequest(String fReason) {
        routeInfoList.clear();
        vehicleDeployedDetailAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;

        total_vh_count = 0;
        deployed_vh_count = 0;
        not_deployed_vh_count = 0;
        maintenance_vh_count = 0;
        btn_Total.setText(routeInfoList.size() + "");
        btn_Deployed.setText(deployed_vh_count + "");
        btn_NOtDeployed.setText((not_deployed_vh_count - maintenance_vh_count) + "");
        btn_maintenance.setText(maintenance_vh_count + "");
        getVehicleDetailOnServer(API_Selected, "NA", fReason, 0);
    }

    public void getVehicleDetailOnServer(String API_nm, String filterVType, String filterVReason, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(),  getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {


                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

//                    filters = "[[\"Fuel_Details\",\"supervisor_id\",\"like\",\""
//                            + session.getpsNo()
//                            + "\"],[\"Fuel_Details\",\"creation\",\"like\",\"" +
//                            Connection.getCurrentDate() + "%"
//                            + "\"]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + API_nm +
                            "?id="
                            + URLEncoder.encode(session.getpsNo(), "UTF-8")
                            + "&vehicle_type=" + URLEncoder.encode(filterVType, "UTF-8")
                            + "&reason=" + URLEncoder.encode(filterVReason, "UTF-8");

//                            + "&fields=" + fields
//                            + "&limit_page_length=" + limit_page_length
//                            + "&limit_start=" + limit_start;

                    Log.e(TAG, "url - >  " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("message");
                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");

                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo fDetail = objectMapper.readValue(visitor.toString(), RouteInfo.class);
                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + fDetail.getDa_vehicleno());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    if (fDetail.getStatus().equalsIgnoreCase(AppConfig.Vehicle_deployed_status)) {
                                                        deployed_vh_count = deployed_vh_count + 1;
                                                    } else if (fDetail.getStatus().equalsIgnoreCase(AppConfig.Vehicle_not_deployed_status)) {
                                                        not_deployed_vh_count = not_deployed_vh_count + 1;
                                                        if (fDetail.getReason_for_breakdown() != null) {
                                                            if (fDetail.getReason_for_breakdown().equalsIgnoreCase("Maintenance")) {
                                                                maintenance_vh_count = maintenance_vh_count + 1;
                                                            }
                                                        }
                                                    }
                                                    routeInfoList.add(fDetail);
                                                    vehicleDeployedDetailAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            Log.e(TAG, "metaDataList length = " + routeInfoList.size());


                                            btn_Total.setText(routeInfoList.size() + "");
                                            btn_Deployed.setText(deployed_vh_count + "");
                                            btn_NOtDeployed.setText((not_deployed_vh_count - maintenance_vh_count) + "");
                                            btn_maintenance.setText(maintenance_vh_count + "");
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.supervisor_home, menu);

        MenuItem homeMenuItem = menu.findItem(R.id.action_home);
        homeMenuItem.setVisible(false);

        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);
        filterMenuItem.setVisible(true);

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

            getvehicleTypeListOnServer();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    ImageView img_closeBtn;
    BottomSheetDialog bottomSheetDialog;
    BottomSheetBehavior bottomSheetBehavior;
    View bottomSheetView;
    MaterialButton btn_btn_filter_apply;
    Spinner sp_Vehicle_Type, sp_Vehicle_Breakdown_Reason;
    MaterialButtonToggleGroup toggle_btn_Vehicle_Status_group;
    String filter_Vehicle_Type = "NA";
    String filter_Vehicle_Breakdown_Reason = "NA";
    MaterialButton mtbtn_NotDeployed, mtbtn_Total, mtbtn_Deployed;
TextView tv_reason_tag;
    public void showBottomSheetFilter() {
        try {

            bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_vechicle_deployed_filter, null);
            bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setCancelable(false);
            bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

            btn_btn_filter_apply = bottomSheetDialog.findViewById(R.id.btn_btn_filter_apply);

            img_closeBtn = bottomSheetDialog.findViewById(R.id.img_closeBtn);
            sp_Vehicle_Type = bottomSheetDialog.findViewById(R.id.sp_Vehicle_Type);
            sp_Vehicle_Breakdown_Reason = bottomSheetDialog.findViewById(R.id.sp_Vehicle_Breakdown_Reason);
            tv_reason_tag = bottomSheetDialog.findViewById(R.id.tv_reason_tag);
            toggle_btn_Vehicle_Status_group = bottomSheetDialog.findViewById(R.id.toggle_btn_Vehicle_Status_group);
            mtbtn_Total = bottomSheetDialog.findViewById(R.id.mtbtn_Total);
            mtbtn_Deployed = bottomSheetDialog.findViewById(R.id.mtbtn_Deployed);
            mtbtn_NotDeployed = bottomSheetDialog.findViewById(R.id.mtbtn_NotDeployed);

            btn_btn_filter_apply.setEnabled(false);


            sp_Vehicle_Type.setAdapter(adapter__vehileType);
            sp_Vehicle_Breakdown_Reason.setAdapter(adapter__reson);

            img_closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bottomSheetDialog.dismiss();
                }
            });

            sp_Vehicle_Type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String vType = parent.getItemAtPosition(position).toString();
                    //
                    Log.e(TAG, "vType = " + vType);
                    if (vType.equalsIgnoreCase("Select")) {
                        filter_Vehicle_Type = "NA";
                    } else {

                        filter_Vehicle_Type = vType;

                    }
                } // to close the onItemSelected

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            sp_Vehicle_Breakdown_Reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String bReason = parent.getItemAtPosition(position).toString();
                    //
                    Log.e(TAG, "bReason = " + bReason);
                    if (bReason.equalsIgnoreCase("Select")) {
                        filter_Vehicle_Breakdown_Reason = "NA";
                    } else {

                        filter_Vehicle_Breakdown_Reason = bReason;
                        if (filter_Vehicle_Breakdown_Reason != null) {
                            if (filter_Vehicle_Breakdown_Reason.equalsIgnoreCase("Maintenance")) {
                                ll_list.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vh_maintenance));
                            }
                        }


                    }
                } // to close the onItemSelected

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
//            if (API_Selected.equalsIgnoreCase(AppConfig.API_vehicle_deployed)) {
//                mtbtn_Deployed.setChecked(true);
//            } else if (API_Selected.equalsIgnoreCase(AppConfig.API_vehicle_not_deployed)) {
//                mtbtn_NotDeployed.setChecked(true);
//            } else {
//                mtbtn_Total.setChecked(true);
//            }
            sp_Vehicle_Breakdown_Reason.setEnabled(false);
            toggle_btn_Vehicle_Status_group.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

                if (checkedId == R.id.mtbtn_Total) {
                    sp_Vehicle_Breakdown_Reason.setEnabled(false);

                    btn_btn_filter_apply.setEnabled(true);
                    API_Selected = AppConfig.API_vehicle_deployed_all;
                    ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_total));
                } else if (checkedId == R.id.mtbtn_Deployed) {
                    sp_Vehicle_Breakdown_Reason.setEnabled(false);

                    btn_btn_filter_apply.setEnabled(true);
                    API_Selected = AppConfig.API_vehicle_deployed;
                    ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_deployed));
                } else if (checkedId == R.id.mtbtn_NotDeployed) {
                    sp_Vehicle_Breakdown_Reason.setEnabled(true);

                    btn_btn_filter_apply.setEnabled(true);
                    API_Selected = AppConfig.API_vehicle_not_deployed;
                    ll_list.setBackgroundColor(ContextCompat.getColor(this, R.color.vh_not_eployed));
                }

            });
            btn_btn_filter_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (filter_POIStatus != null && !filter_POIStatus.equalsIgnoreCase("")) {
//                        Log.e(TAG, "filter_POIStatus filter = " + filter_POIStatus);
//                    }
//                    if (tiedt_date.getText().toString() != null) {

                    Log.e(TAG, "tiedt_date filter = " + filter_Vehicle_Type);
                    Log.e(TAG, "filter_POIStatus filter = " + filter_Vehicle_Breakdown_Reason);
//
//
//                    }

                    try {
                        routeInfoList.clear();
                        vehicleDeployedDetailAdapter.notifyDataSetChanged();
                        previous_Item_Total = 0;
                        isLoading = true;

                        total_vh_count = 0;
                        deployed_vh_count = 0;
                        not_deployed_vh_count = 0;
                        maintenance_vh_count = 0;
                        btn_Total.setText(routeInfoList.size() + "");
                        btn_Deployed.setText(deployed_vh_count + "");
                        btn_NOtDeployed.setText((not_deployed_vh_count - maintenance_vh_count) + "");
                        btn_maintenance.setText(maintenance_vh_count + "");
                        getVehicleDetailOnServer(API_Selected, filter_Vehicle_Type, filter_Vehicle_Breakdown_Reason, 0);
                        bottomSheetDialog.dismiss();

                        filter_Vehicle_Breakdown_Reason = "NA";
                        filter_Vehicle_Type = "NA";
                    } catch (Exception e) {
                        TastyToast.makeText(getApplicationContext(), "Something is wrong", TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    }
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

    public void getvehicleTypeListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(),  getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(VehicleDeployed.this);
            mProgressDialog.setMessage( getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_vehicle_type + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
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
                                            list_vehileType.clear();
                                            adapter__vehileType.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();
                                            list_vehileType.add("Select");
                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo fuelDetails = objectMapper.readValue(visitor.toString(), RouteInfo.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    list_vehileType.add(fuelDetails.getVehicle_type());
                                                    adapter__vehileType.notifyDataSetChanged();
                                                }
                                            }

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            getReason_for_breakdownListOnServer();
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

    public void getReason_for_breakdownListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(),  getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(VehicleDeployed.this);
            mProgressDialog.setMessage( getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_reason + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
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
                                            list_reson.clear();
                                            adapter__reson.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();
                                            list_reson.add("Select");
                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                RouteInfo fuelDetails = objectMapper.readValue(visitor.toString(), RouteInfo.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    list_reson.add(fuelDetails.getReason_for_breakdown());
                                                    adapter__reson.notifyDataSetChanged();
                                                }
                                            }

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            showBottomSheetFilter();
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
