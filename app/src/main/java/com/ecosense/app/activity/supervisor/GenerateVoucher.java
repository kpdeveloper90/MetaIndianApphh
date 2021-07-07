package com.ecosense.app.activity.supervisor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.ecosense.app.activity.driver.DriverReceivedVoucher;
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.adapter.VehicleDeployedDetailAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;
import com.ecosense.app.pojo.Voucher;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class GenerateVoucher extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = GenerateVoucher.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;


    @BindView(R.id.tv_voucher_genDate)
    TextView tv_voucher_genDate;

    @BindView(R.id.edt_vh_no)
    TextInputEditText edt_vh_no;

    @BindView(R.id.sp_reason_for_voucher)
    Spinner sp_reason_for_voucher;

    @BindView(R.id.edt_voucher_amount)
    TextInputEditText edt_voucher_amount;
    @BindView(R.id.tv_voucher_amount)
    TextView tv_voucher_amount;


    @BindView(R.id.im_btn_cancel)
    ImageView im_btn_cancel;

    @BindView(R.id.im_btn_submit)
    ImageView im_btn_submit;
    ArrayList<String> voucherReasonList = null;
    DropDownStringAdapter adapter_voucherReason = null;
    Voucher voucherInfo;
    static RouteItem routeDetails = null;
    static Vehicle vehicleDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_voucher);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include7);
        toolbar.setTitle("Generate Voucher");
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


        voucherReasonList = new ArrayList<>();
        adapter_voucherReason = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, voucherReasonList);
        getVoucherReasonOnServer();

        sp_reason_for_voucher.setAdapter(adapter_voucherReason);

        tv_voucher_genDate.setText(Connection.getCurrentDateTime12Hours());
        im_btn_cancel.setOnClickListener(this);
        im_btn_submit.setOnClickListener(this);
        edt_vh_no.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
            if (methodIntent != null && methodIntent.equals("SelectVoucherForUpdate")) {
                voucherInfo = (Voucher) extras.getSerializable("voucherInfo");
                toolbar.setTitle("Update Voucher");
                toolbar.invalidate();
                edt_vh_no.setText(voucherInfo.getVehicle_number());
                edt_voucher_amount.setText(voucherInfo.getVoucher_amount());
            } else if (methodIntent != null && methodIntent.equals("VoucherApprovedRequest")) {
                voucherInfo = (Voucher) extras.getSerializable("voucherInfo");
                toolbar.setTitle("Requested Voucher");
                toolbar.invalidate();
                edt_vh_no.setText(voucherInfo.getVehicle_number());
                sp_reason_for_voucher.setEnabled(false);

            } else if (methodIntent != null && methodIntent.equals("DriverGenerateVoucher")) {
                routeDetails = (RouteItem) extras.getSerializable("routeDetails");
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                Log.e(TAG, "RouteItem.getName() " + routeDetails.getName());
                Log.e(TAG, "RouteItem.getDa_vehicleno() " + routeDetails.getDa_vehicleno());
                edt_vh_no.setText(routeDetails.getDa_vehicleno());
                tv_voucher_amount.setVisibility(View.GONE);
                edt_voucher_amount.setVisibility(View.GONE);
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    @Override
    public void onClick(View v) {
        if (v == im_btn_submit) {
            if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                aleartForSubmitVoucher(getString(R.string.alert_msg_approved_voucher));
            } else if (methodIntent.equalsIgnoreCase("DriverGenerateVoucher")) {
                aleartForSubmitVoucher(getString(R.string.alert_msg_request_voucher));
            } else if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")) {
                aleartForSubmitVoucher(getString(R.string.alert_update_voucher));
            } else {
                aleartForSubmitVoucher(getString(R.string.alert_msg_generate_voucher));
            }
        }
        if (v == edt_vh_no) {
            if (methodIntent != null && !methodIntent.equals("DriverGenerateVoucher")
                    && !methodIntent.equals("VoucherApprovedRequest")) {
                showSearchDetail_Dialog("Vehicle");
            }
        }
        if (v == im_btn_cancel) {
            onBackPressed();
        }
    }

    static String da_supervisor_id = null;
    static String da_supervisor_name = null;

    protected void aleartForSubmitVoucher(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (methodIntent.equalsIgnoreCase("DriverGenerateVoucher")) {
                            getDataDriver();
                        } else if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                            getDataVoucherApprovedRequest();
                        } else {
                            getData();
                        }

                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    private void getDataDriver() {

//        if (Objects.requireNonNull(edt_vh_no.getText()).toString().isEmpty()) {
//            edt_vh_no.setError("Enter Vehicle No.");
//            TastyToast.makeText(getApplicationContext(), "Enter  Vehicle No", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//        } else {
        Voucher vd = new Voucher();

        vd.setVehicle_number(edt_vh_no.getText().toString());
        vd.setVoucher_amount(edt_voucher_amount.getText().toString());
        vd.setVoucher_status(AppConfig.Voucher_status_Pending);
        vd.setStatus(AppConfig.Active_Status);
        vd.setVoucher_reason(sp_reason_for_voucher.getSelectedItem().toString());


        vd.setDriver_id(session.getpsNo());
        vd.setDriver_name(session.getpsName());
        vd.setGenerated_by(session.getuserSubType());

        da_supervisor_id = "";
        da_supervisor_name = "";
        getSuperVisorDetailOnServer(routeDetails.getDa_vehicleno(), vd);


//        }
    }

    private void getData() {

        if (Objects.requireNonNull(edt_vh_no.getText()).toString().isEmpty()) {
            edt_vh_no.setError(getString(R.string.enetr_vehicle_no));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enetr_vehicle_no), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else if (Objects.requireNonNull(edt_voucher_amount.getText()).toString().isEmpty()) {
            edt_voucher_amount.setError(getString(R.string.enter_voucher_amount));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_voucher_amount), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Voucher vd = new Voucher();

            vd.setVehicle_number(edt_vh_no.getText().toString());
            vd.setVoucher_amount(edt_voucher_amount.getText().toString());
            vd.setVoucher_status(AppConfig.Voucher_status_Unused);
            vd.setStatus(AppConfig.Active_Status);
            vd.setVoucher_reason(sp_reason_for_voucher.getSelectedItem().toString());
            vd.setSupervisor_id(session.getpsNo());
            vd.setSupervisor_name(session.getpsName());
            vd.setGenerated_by(session.getuserSubType());

            sendVoucherDetailRequest(vd);

        }
    }

    private void getDataVoucherApprovedRequest() {

        if (Objects.requireNonNull(edt_vh_no.getText()).toString().isEmpty()) {
            edt_vh_no.setError(getString(R.string.enetr_vehicle_no));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enetr_vehicle_no), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else if (Objects.requireNonNull(edt_voucher_amount.getText()).toString().isEmpty()) {
            edt_voucher_amount.setError(getString(R.string.enter_voucher_amount));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_voucher_amount), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Voucher vd = new Voucher();

            vd.setVoucher_amount(edt_voucher_amount.getText().toString());
            vd.setVoucher_status(AppConfig.Voucher_status_Unused);

            sendVoucherDetailRequest(vd);

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

    static String selected_VhNO = null;


    public void showSearchDetail_Dialog(String searchOn) {
        selected_VhNO = null;

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
        tv_dialog_title.setText(getString(R.string.select_tag) + searchOn);

//        if (searchOn.equalsIgnoreCase("Vehicle")) {
        routeInfoList = new ArrayList<>();
        vehicleDeployedDetailAdapter = new VehicleDeployedDetailAdapter(getApplicationContext(), routeInfoList);
        getVehicleDetailOnServer(AppConfig.API_vehicle_deployed_all, "NA", "NA");
//        } else {
//
//        }


        srl_searchName.setColorSchemeResources(R.color.colorAccent);
        srl_searchName.setNestedScrollingEnabled(true);

        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_searchName.setHasFixedSize(true);
        rv_searchName.setLayoutManager(lLayout);
        rv_searchName.setItemAnimator(new DefaultItemAnimator());

//        if (searchOn.equalsIgnoreCase("Vehicle")) {
        rv_searchName.setAdapter(vehicleDeployedDetailAdapter);
//        } else {
//
//        }

        srl_searchName.setOnRefreshListener(() -> {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

//                    if (searchOn.equalsIgnoreCase("Vehicle")) {
                    routeInfoList.clear();
                    vehicleDeployedDetailAdapter.notifyDataSetChanged();
                    getVehicleDetailOnServer(AppConfig.API_vehicle_deployed_all, "NA", "NA");
//                    } else {
//                    }
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
//                if (searchOn.equalsIgnoreCase("Vehicle")) {
                RouteInfo vehicleInfo = routeInfoList.get(position);
                selected_VhNO = vehicleInfo.getDa_vehicleno();
                edt_vh_no.setText(vehicleInfo.getDa_vehicleno());
                dialog_search_name.dismiss();
//                } else {
//
//                    dialog_search_name.dismiss();
//                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        dialog_search_name.show();
    }

    public void getVoucherReasonOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(GenerateVoucher.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + "/api/resource/Voucher_Reason?fields=" + URLEncoder.encode(fields, "UTF-8") + "&limit_page_length=500";
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
                                            voucherReasonList.clear();
                                            adapter_voucherReason.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Voucher fuelDetails = objectMapper.readValue(visitor.toString(), Voucher.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    voucherReasonList.add(fuelDetails.getVoucher_reason());
                                                    adapter_voucherReason.notifyDataSetChanged();
                                                }
                                            }

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

                                            if (methodIntent != null) {
                                                if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")
                                                        || methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                                                    Log.e(TAG, "NOt null methodIntent " + methodIntent);
                                                    setVoucherReason(voucherInfo.getVoucher_reason());
                                                }
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

    public void setVoucherReason(String vReason) {


//        ArrayAdapter<CharSequence> adapter_ComType = ArrayAdapter.createFromResource(NewComplaints.this, R.array.Complaint_type, android.R.layout.simple_spinner_item);
//        adapter_ComType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_com_type.setAdapter(adapter_ComType);
        if (!vReason.equals(null)) {
            int spinnerPosition = adapter_voucherReason.getPosition(vReason);
            Log.e(TAG, "spinnerPosition = " + spinnerPosition);
            sp_reason_for_voucher.setSelection(spinnerPosition);
        }

    }

    public void sendVoucherDetailRequest_JsonObjectRequest(Voucher voucherDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(GenerateVoucher.this);
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


                        if (methodIntent.equalsIgnoreCase("DriverGenerateVoucher")) {
                            jsonObject.put("vehicle_number", voucherDetail.getVehicle_number());
                            jsonObject.put("voucher_reason", voucherDetail.getVoucher_reason());
                            jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                            jsonObject.put("status", voucherDetail.getStatus());
                            jsonObject.put("driver_id", voucherDetail.getDriver_id());
                            jsonObject.put("driver_name", voucherDetail.getDriver_name());
                            jsonObject.put("supervisor_id", voucherDetail.getSupervisor_id());
                            jsonObject.put("supervisor_name", voucherDetail.getSupervisor_name());
                            jsonObject.put("generated_by", voucherDetail.getGenerated_by());
                        } else if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                            jsonObject.put("voucher_amount", voucherDetail.getVoucher_amount());
                            jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                        } else {
                            jsonObject.put("vehicle_number", voucherDetail.getVehicle_number());
                            jsonObject.put("voucher_reason", voucherDetail.getVoucher_reason());
                            jsonObject.put("voucher_amount", voucherDetail.getVoucher_amount());
                            jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                            jsonObject.put("status", voucherDetail.getStatus());

                            jsonObject.put("supervisor_id", voucherDetail.getSupervisor_id());
                            jsonObject.put("supervisor_name", voucherDetail.getSupervisor_name());
                            jsonObject.put("generated_by", voucherDetail.getGenerated_by());
                        }

                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    int requestMethod;
                    if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")) {
                        url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                        requestMethod = Request.Method.PUT;
                    } else if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                        url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                        requestMethod = Request.Method.PUT;
                    } else {
                        url = session.getMyServerIP() + "/api/resource/Voucher";
                        requestMethod = Request.Method.POST;
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestMethod, url, jsonObject,
                            response -> {
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

                                                Voucher voDetail = objectMapper.readValue(statusData.toString(), Voucher.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                Log.e(TAG, "Responce = " + voDetail.getName());
                                                if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully approved voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                } else if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")) {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully Updated voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                } else {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully generate voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                }


                                                onBackPressed();

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
                            }, error -> {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in response upload image RequestServer ");
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

    }

    public void sendVoucherDetailRequest(Voucher voucherDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(GenerateVoucher.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {

                    String url = null;
                    int requestMethod;
                    if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")) {
                        url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                        requestMethod = Request.Method.PUT;
                    } else if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                        url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                        requestMethod = Request.Method.PUT;
                    } else {
                        url = session.getMyServerIP() + "/api/resource/Voucher";
                        requestMethod = Request.Method.POST;
                    }
                    StringRequest jsonObjectRequest = new StringRequest(requestMethod, url,
                            response -> {
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

                                                Voucher voDetail = objectMapper.readValue(statusData.toString(), Voucher.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                Log.e(TAG, "Responce = " + voDetail.getName());
                                                if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully approved voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                } else if (methodIntent.equalsIgnoreCase("SelectVoucherForUpdate")) {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully Updated voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                } else {
                                                    TastyToast.makeText(getApplicationContext(), "Successfully generate voucher", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                }


                                                onBackPressed();

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
                            }, error -> {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in response upload image RequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject();

                                if (methodIntent.equalsIgnoreCase("DriverGenerateVoucher")) {
                                    jsonObject.put("vehicle_number", voucherDetail.getVehicle_number());
                                    jsonObject.put("voucher_reason", voucherDetail.getVoucher_reason());
                                    jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                                    jsonObject.put("status", voucherDetail.getStatus());
                                    jsonObject.put("driver_id", voucherDetail.getDriver_id());
                                    jsonObject.put("driver_name", voucherDetail.getDriver_name());
                                    jsonObject.put("supervisor_id", voucherDetail.getSupervisor_id());
                                    jsonObject.put("supervisor_name", voucherDetail.getSupervisor_name());
                                    jsonObject.put("generated_by", voucherDetail.getGenerated_by());
                                } else if (methodIntent.equalsIgnoreCase("VoucherApprovedRequest")) {
                                    jsonObject.put("voucher_amount", voucherDetail.getVoucher_amount());
                                    jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                                } else {
                                    jsonObject.put("vehicle_number", voucherDetail.getVehicle_number());
                                    jsonObject.put("voucher_reason", voucherDetail.getVoucher_reason());
                                    jsonObject.put("voucher_amount", voucherDetail.getVoucher_amount());
                                    jsonObject.put("voucher_status", voucherDetail.getVoucher_status());
                                    jsonObject.put("status", voucherDetail.getStatus());

                                    jsonObject.put("supervisor_id", voucherDetail.getSupervisor_id());
                                    jsonObject.put("supervisor_name", voucherDetail.getSupervisor_name());
                                    jsonObject.put("generated_by", voucherDetail.getGenerated_by());
                                }
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

    public void getSuperVisorDetailOnServer(String vehicleNo, Voucher vInfo) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(GenerateVoucher.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_supervisordetails + "?id=" + URLEncoder.encode(vehicleNo, "UTF-8");
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
                                                Voucher vDetails = objectMapper.readValue(visitor.toString(), Voucher.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    da_supervisor_id = vDetails.getSupervisor_id();
                                                    da_supervisor_name = vDetails.getSupervisor_name();
                                                    Log.d(TAG, "Data Available");
                                                }
                                            }

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            vInfo.setSupervisor_id(da_supervisor_id);
                                            vInfo.setSupervisor_name(da_supervisor_name);
                                            sendVoucherDetailRequest(vInfo);
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
//        super.onBackPressed();
        if (methodIntent.equalsIgnoreCase("DriverGenerateVoucher")) {
            Intent mIntent = new Intent(getApplicationContext(), DriverReceivedVoucher.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("routeDetails", routeDetails);
            b.putSerializable("vehicleDetails", vehicleDetails);
            mIntent.setAction("getRouteDetail");
            mIntent.putExtra("SelectedVehicleDetail", b);
            startActivity(mIntent);
            finish();
        } else {
            finish();
            startActivity(new Intent(getApplicationContext(), VoucherDetail.class));
        }
    }
}