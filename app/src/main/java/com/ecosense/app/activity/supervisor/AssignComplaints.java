package com.ecosense.app.activity.supervisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
import com.ecosense.app.activity.citizen.ComplaintsDetail;
import com.ecosense.app.activity.citizen.ServiceRequestDetail;
import com.ecosense.app.adapter.ContactListAdapter;
import com.ecosense.app.adapter.VehicleDeployedDetailAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;
import com.ecosense.app.pojo.LoginDetail;
import com.ecosense.app.pojo.Profile;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.model.Contact;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class AssignComplaints extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = AssignComplaints.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    static Complaints selectComplaintsData;

    @BindView(R.id.tv_com_number)
    AppCompatTextView tv_com_number;

    @BindView(R.id.tv_comp_location)
    AppCompatTextView tv_comp_location;
    @BindView(R.id.tiedt_veh_no)
    TextInputEditText tiedt_veh_no;

    @BindView(R.id.tiedt_driver_name)
    TextInputEditText tiedt_driver_name;

    @BindView(R.id.im_btn_cancel)
    MaterialButton im_btn_cancel;

    @BindView(R.id.im_btn_assign)
    MaterialButton im_btn_assign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_complaints);


        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include9);
        toolbar.setTitle("Assign Complaints");
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

        tiedt_veh_no.setOnClickListener(this::onClick);
        tiedt_driver_name.setOnClickListener(this::onClick);
        im_btn_cancel.setOnClickListener(this::onClick);
        im_btn_assign.setOnClickListener(this::onClick);
        onNewIntent(getIntent());
    }

    @Override
    public void onClick(View v) {
        if (v == tiedt_veh_no) {
            showSearchDetail_Dialog("Vehicle");
        }
        if (v == tiedt_driver_name) {
            showSearchDetail_Dialog("Driver Name");
        }
        if (v == im_btn_assign) {
            getTextData();
        }
        if (v == im_btn_cancel) {
            onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
//        Intent intent = getIntent();

        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent = " + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
            if (methodIntent != null && methodIntent.equals("AssignCitizenComplaints")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                Log.e(TAG, "getComId =  " + Objects.requireNonNull(selectComplaintsData).getComId());

                tv_com_number.setText(selectComplaintsData.getComId());
                tv_comp_location.setText(selectComplaintsData.getCptloc());
            } else if (methodIntent != null && methodIntent.equals("AssignCorporateServiceRequest")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                Log.e(TAG, "getComId =  " + Objects.requireNonNull(selectComplaintsData).getComId());

                tv_com_number.setText(selectComplaintsData.getComId());
                tv_comp_location.setText(selectComplaintsData.getCptloc());
                toolbar.setTitle("Assign Service Request");
                toolbar.invalidate();
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    static Dialog dialog_search_name;
    static ImageView dialog_close;
    static SwipeRefreshLayout srl_searchName;
    static RecyclerView rv_searchName;
    static TextView tv_dialog_title;
    static GridLayoutManager lLayout;
    static RelativeLayout progressBar;
    static VehicleDeployedDetailAdapter vehicleDeployedDetailAdapter = null;
    static List<RouteInfo> routeInfoList = null;

    static ArrayList<Contact> imp_cnoList = null;
    static ContactListAdapter contactListAdapter = null;

    static String selected_VhNO = null;
    static String selected_driverName = null;
    static String selected_driverId = null;

    public void showSearchDetail_Dialog(String searchOn) {
        selected_VhNO = null;
        selected_driverName = null;
        selected_driverId = null;
        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_search_vehicle_no_driver_name, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_search_name = new Dialog(this);
        dialog_search_name.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_search_name.setContentView(root);
        dialog_search_name.setCancelable(true);
        dialog_search_name.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_search_name.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        tv_dialog_title = dialog_search_name.findViewById(R.id.tv_dialog_title);
        dialog_close = dialog_search_name.findViewById(R.id.dialog_close);

        srl_searchName = dialog_search_name.findViewById(R.id.srl_searchName);
        rv_searchName = dialog_search_name.findViewById(R.id.rv_searchName);
        progressBar = dialog_search_name.findViewById(R.id.rltv_progressBar);
        tv_dialog_title.setText("Select " + searchOn);

        if (searchOn.equalsIgnoreCase("Vehicle")) {
            routeInfoList = new ArrayList<>();
            vehicleDeployedDetailAdapter = new VehicleDeployedDetailAdapter(getApplicationContext(), routeInfoList);
            getVehicleDetailOnServer(AppConfig.API_vehicle_not_deployed, "NA", "NA");
        } else {
            imp_cnoList = new ArrayList<>();
            contactListAdapter = new ContactListAdapter(imp_cnoList);
            getDriver_not_assignedDetailOnServer();
        }


        srl_searchName.setColorSchemeResources(R.color.colorAccent);
        srl_searchName.setNestedScrollingEnabled(true);

        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_searchName.setHasFixedSize(true);
        rv_searchName.setLayoutManager(lLayout);
        rv_searchName.setItemAnimator(new DefaultItemAnimator());

        if (searchOn.equalsIgnoreCase("Vehicle")) {
            rv_searchName.setAdapter(vehicleDeployedDetailAdapter);
        } else {
            rv_searchName.setAdapter(contactListAdapter);
        }

        srl_searchName.setOnRefreshListener(() -> {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (searchOn.equalsIgnoreCase("Vehicle")) {
                        routeInfoList.clear();
                        vehicleDeployedDetailAdapter.notifyDataSetChanged();
                        getVehicleDetailOnServer(AppConfig.API_vehicle_not_deployed, "NA", "NA");
                    } else {
                        imp_cnoList.clear();
                        contactListAdapter.notifyDataSetChanged();
                        getDriver_not_assignedDetailOnServer();
                    }
                    srl_searchName.setRefreshing(false);
                }
            }, 2000);

        });
        dialog_close.setOnClickListener(v -> {
            dialog_search_name.dismiss();
        });


        rv_searchName.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_searchName, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (searchOn.equalsIgnoreCase("Vehicle")) {
                    RouteInfo vehicleInfo = routeInfoList.get(position);
                    selected_VhNO = vehicleInfo.getDa_vehicleno();
                    tiedt_veh_no.setText(vehicleInfo.getDa_vehicleno());
                    dialog_search_name.dismiss();
                } else {
//                    Profile profileInfo = imp_cnoList.get(position);
//                    tiedt_driver_name.setText(profileInfo.getImpc_name());
//                    selected_driverId = profileInfo.getName();
//                    selected_driverName = profileInfo.getImpc_name();
                    dialog_search_name.dismiss();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        dialog_search_name.show();
    }

    public void getTextData() {
        try {


//
//        byte[] img_capturePic = getBitmapAsByteArray(com_Pic_bitmap);

            String veh_no = tiedt_veh_no.getText().toString().trim();
            String driver_name = tiedt_driver_name.getText().toString().trim();


            if (veh_no.isEmpty() && veh_no.equalsIgnoreCase("")) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.Please_select_Vehicle_No), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                tiedt_veh_no.setError(getString(R.string.Please_select_Vehicle_No));
            } else if (driver_name.isEmpty() && driver_name.equalsIgnoreCase("")) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.Please_select_Driver_Name), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                tiedt_driver_name.setError(getString(R.string.Please_select_Driver_Name));
            } else {

                Complaints comp = new Complaints();
                comp.setComId(selectComplaintsData.getComId());

                comp.setVehicle_no(veh_no);
                comp.setDriver_name(driver_name);
                comp.setDriver_id(selected_driverId);

                comp.setAssign_date(Connection.getCurrentDateTime());
                comp.setCptstatus(AppConfig.CPTSTATUS_In_Process);
                comp.setAssignfrom(session.getpsNo());

                Log.e(TAG, "veh_no = " + veh_no
                        + "\n driver_name = " + driver_name
                        + "\n selected_driverId = " + selected_driverId
                        + "\n getCptid = " + selectComplaintsData.getComId()
                        + "\n CPTSTATUS_In_Process = " + AppConfig.CPTSTATUS_In_Process
                        + "\n session.getpsNo() = " + session.getpsNo()

                );

                updateComplaintsRequestServer(comp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }

    public void updateComplaintsRequestServer(Complaints tempcomplaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(AssignComplaints.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {
                JSONObject jsonObject = null;


                URLEncoder.encode("", "UTF-8");

                String url = null;

                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(tempcomplaintsDetails.getComId(), "UTF-8");


                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
                        new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        try {
                                            //create ObjectMapper instance
                                            ObjectMapper objectMapper = new ObjectMapper();
                                            JsonNode rootNode = objectMapper.readTree(response.toString());
                                            JsonNode statusData = rootNode.path("data");
                                            Log.e(TAG, "statusData = " + statusData.toString());
                                            if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                                Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);
                                                Log.e(TAG, "Responce getComId= " + compDetail.getComId());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                Intent intent = null;
                                                if (methodIntent.equalsIgnoreCase("AssignCorporateServiceRequest")) {
                                                    intent = new Intent(getApplicationContext(), ServiceRequestDetail.class);
                                                    intent.setAction("AssignServiceRequest");
                                                } else {
                                                    intent = new Intent(getApplicationContext(), ComplaintsDetail.class);
                                                    intent.setAction("AssignCitizenComplaints");
                                                }
                                                // the activity that holds the fragment
                                                Bundle b = new Bundle();
                                                b.putSerializable("ComplaintsData", compDetail);
                                                intent.putExtra("SelectedNameDetail", b);
                                                startActivity(intent);
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
                        JSONObject jsonObject = null;

                        try {
                            jsonObject = new JSONObject();

                            jsonObject.put("assign_date", tempcomplaintsDetails.getAssign_date());
                            jsonObject.put("cptstatus", tempcomplaintsDetails.getCptstatus());
                            jsonObject.put("assignfrom", tempcomplaintsDetails.getAssignfrom());
                            jsonObject.put("driver_id", tempcomplaintsDetails.getDriver_id());
                            jsonObject.put("driver_name", tempcomplaintsDetails.getDriver_name());
                            jsonObject.put("vehicle_no", tempcomplaintsDetails.getVehicle_no());


                            params.put("data", jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
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

                String tag_string_req = "string_req";
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

    public void updateComplaintsRequestServer_JsonObjectRequest(Complaints tempcomplaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(AssignComplaints.this);
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


                    jsonObject.put("assign_date", tempcomplaintsDetails.getAssign_date());
                    jsonObject.put("cptstatus", tempcomplaintsDetails.getCptstatus());
                    jsonObject.put("assignfrom", tempcomplaintsDetails.getAssignfrom());
                    jsonObject.put("driver_id", tempcomplaintsDetails.getDriver_id());
                    jsonObject.put("driver_name", tempcomplaintsDetails.getDriver_name());
                    jsonObject.put("vehicle_no", tempcomplaintsDetails.getVehicle_no());


                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;

                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(tempcomplaintsDetails.getComId(), "UTF-8");


                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        try {
                                            //create ObjectMapper instance
                                            ObjectMapper objectMapper = new ObjectMapper();
                                            JsonNode rootNode = objectMapper.readTree(response.toString());
                                            JsonNode statusData = rootNode.path("data");
                                            Log.e(TAG, "statusData = " + statusData.toString());
                                            if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                                Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);
                                                Log.e(TAG, "Responce getComId= " + compDetail.getComId());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
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

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    public void getVehicleDetailOnServer(String API_nm, String filterVType, String filterVReason) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                try {
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + API_nm +
                            "?id="
                            + URLEncoder.encode(session.getpsNo(), "UTF-8")
                            + "&vehicle_type=" + URLEncoder.encode(filterVType, "UTF-8")
                            + "&reason=" + URLEncoder.encode(filterVReason, "UTF-8");

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
                                                    routeInfoList.add(fDetail);
                                                    vehicleDeployedDetailAdapter.notifyDataSetChanged();
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

    public void getDriver_not_assignedDetailOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                try {
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_driver_not_assigned;

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
                                                LoginDetail uDetail = objectMapper.readValue(visitor.toString(), LoginDetail.class);
                                                Log.e(TAG, "assetsDetail.getUserId() = " + uDetail.getUserId());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    Profile p = new Profile();
                                                    p.setName(uDetail.getUserId());
                                                    p.setImpc_name(uDetail.getEmployee_name());
                                                    p.setImpc_no(uDetail.getRegmobile());
                                                    p.setImpc_desig(uDetail.getDesignation());
//                                                    imp_cnoList.add(p);
                                                    contactListAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            Log.e(TAG, "metaDataList length = " + imp_cnoList.size());

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
                    });
                    String tag_string_req = "string_req";
                    ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, PROGRASS_postDelayed);
        }
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Back-Online";
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
        }
        TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.ERROR);

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
        Intent mIntent = null;
        if (methodIntent.equalsIgnoreCase("AssignCorporateServiceRequest")) {
            mIntent = new Intent(getApplicationContext(), ServiceRequestDetail.class);
            mIntent.setAction("AssignServiceRequest");
        } else {
            mIntent = new Intent(getApplicationContext(), ComplaintsDetail.class);
            mIntent.setAction("AssignCitizenComplaints");
        }
        // the activity that holds the fragment
        Bundle b = new Bundle();
        b.putSerializable("ComplaintsData", selectComplaintsData);
        mIntent.putExtra("SelectedNameDetail", b);
        startActivity(mIntent);
        finish();
    }
}
