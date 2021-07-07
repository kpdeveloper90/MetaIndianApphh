package com.ecosense.app.activity.supervisor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.driver.SimpleScannerActivity;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Fuel;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class VehicleEntryForFuel extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = VehicleEntryForFuel.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    static Fuel fuelDetail;
    static Fuel updateFuelInfo;

    @BindView(R.id.edt_vh_no)
    TextInputEditText edt_vh_no;


    @BindView(R.id.edt_meter_reading)
    TextInputEditText edt_meter_reading;

    @BindView(R.id.edt_fuel_qty)
    TextInputEditText edt_fuel_qty;


    @BindView(R.id.edt_fuel_rate)
    TextInputEditText edt_fuel_rate;

    @BindView(R.id.edt_fuel_amount)
    TextInputEditText edt_fuel_amount;


    @BindView(R.id.tv_vehicle_no)
    TextView tv_vehicle_no;

    @BindView(R.id.tv_ch_no)
    TextView tv_ch_no;

    @BindView(R.id.tv_model)
    TextView tv_model;

    @BindView(R.id.tv_ownerName)
    TextView tv_ownerName;

    @BindView(R.id.btn_search)
    Button btn_search;

    @BindView(R.id.img_vh_scan_qr)
    ImageView img_vh_scan_qr;

    @BindView(R.id.im_btn_cancel)
    ImageView im_btn_cancel;

    @BindView(R.id.im_btn_submit)
    ImageView im_btn_submit;


    @BindView(R.id.card_view_userProfile)
    CardView card_view_serch_vechile;

    @BindView(R.id.card_view_fuelDetail)
    CardView card_view_fuelDetail;

    @BindView(R.id.card_view_VehicleDetail)
    CardView card_view_VehicleDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_entry_for_fuel);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Vehicle Entry For Fuel");
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

        edt_fuel_qty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {

                } catch (Exception e) {

                }

                if (charSequence.length() >= 1) {
                    Double total = getFuelTotalAmount(Double.parseDouble(
                            Objects.requireNonNull(edt_fuel_qty.getText()).toString()),
                            Double.parseDouble(Objects.requireNonNull(edt_fuel_rate.getText()).toString()));

                    edt_fuel_amount.setText(String.format("%.2f", total));
                } else {
                    edt_fuel_amount.setText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        im_btn_submit.setOnClickListener(this);
        im_btn_cancel.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        img_vh_scan_qr.setOnClickListener(this);
        edt_fuel_qty.setOnClickListener(this);

        onNewIntent(getIntent());
    }

    public static final int Vehicle_Request_Code_SCAN_QR = 13;
int EDT_TAP_COUNT=0;
    @Override
    public void onClick(View v) {
        if (v == im_btn_submit) {
            if (methodIntent.equalsIgnoreCase("UpdateFuelQty")) {
                aleartforBack(getString(R.string.are_sure_update_qty),"UpdateQty");
            }else {
                getData();
            }
        }
        if (v == im_btn_cancel) {
            aleartforBack(getString(R.string.are_you_sure_go_to_back),"Back");
        }
        if (v == btn_search) {
            getVehicleDetailOnServer(Objects.requireNonNull(edt_vh_no.getText()).toString().trim());
        }
        if (v == img_vh_scan_qr) {
            Intent intent = new Intent(getApplicationContext(), SimpleScannerActivity.class);
            intent.setAction("VehicleQRCodeScan");
            startActivityForResult(intent, Vehicle_Request_Code_SCAN_QR);
        }
        if(v==edt_fuel_qty){
            EDT_TAP_COUNT=EDT_TAP_COUNT+1;
            if(EDT_TAP_COUNT>=2){
                edt_fuel_qty.setFocusable(true);
                edt_fuel_qty.setFocusableInTouchMode(true);
            }
        }
    }

    private void getData() {

        if (Objects.requireNonNull(edt_meter_reading.getText()).toString().isEmpty()) {
            edt_meter_reading.setError(getString(R.string.enter_meter_reading));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_meter_reading), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else if (Objects.requireNonNull(edt_fuel_qty.getText()).toString().isEmpty()) {
            edt_fuel_qty.setError(getString(R.string.enter_fuel_qty));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_fuel_qty), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Fuel fd = new Fuel();
            if (methodIntent.equalsIgnoreCase("UpdateFuelQty")) {
                fd.setQuantity(Objects.requireNonNull(edt_fuel_qty.getText()).toString().trim());
                fd.setTotal(Objects.requireNonNull(edt_fuel_amount.getText()).toString().trim());

                fd.setName(updateFuelInfo.getName());
                UpdateFillFuelVichleDetailRequest(fd);
            } else {
                fd.setVehicle_number(tv_vehicle_no.getText().toString());
                fd.setMeter_reading(Objects.requireNonNull(edt_meter_reading.getText()).toString().trim());
                fd.setRate(Objects.requireNonNull(edt_fuel_rate.getText()).toString().trim());
                fd.setQuantity(Objects.requireNonNull(edt_fuel_qty.getText()).toString().trim());
                fd.setTotal(Objects.requireNonNull(edt_fuel_amount.getText()).toString().trim());
                fd.setPump_name(fuelDetail.getPump_name());
                fd.setOem_name(fuelDetail.getOem_name());
                fd.setSupervisor_id(session.getpsNo());
                fd.setSupervisor_name(session.getpsName());

                sendFillFuelVichleDetailRequest(fd);
            }
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
            if (methodIntent != null && methodIntent.equals("fuelPrice")) {
                fuelDetail = (Fuel) extras.getSerializable("fuelDetail");
            } else if (methodIntent != null && methodIntent.equals("UpdateFuelQty")) {
                fuelDetail = (Fuel) extras.getSerializable("fuelDetail");
                updateFuelInfo = (Fuel) extras.getSerializable("updateFuelInfo");
                toolbar.setTitle("Update Fuel Qty.");
                toolbar.invalidate();
                card_view_serch_vechile.setVisibility(View.GONE);
                getVehicleDetailOnServer(Objects.requireNonNull(updateFuelInfo.getVehicle_number()));
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    public void getVehicleDetailOnServer(String vehicle_Number) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(VehicleEntryForFuel.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;


                    filters = "[[\"Asset\", \"registration_number\", \"=\",\""
                            + vehicle_Number
                            + "\"]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                    Log.e(TAG, "getVehicleDetailOnServer Vehicle_Master filters = " + filters + "&fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/Asset?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields;
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
                                                JsonNode vehicleItem = elements1.next();
                                                Vehicle vehicleDetails = objectMapper.readValue(vehicleItem.toString(), Vehicle.class);

                                                if (!vehicleItem.toString().trim().equalsIgnoreCase("{}")) {

                                                    Log.e(TAG, "vehicleDetails.getRoute_info() = " + vehicleDetails.getRoute_info());
                                                    setVehicleDetail(vehicleDetails);
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
                } finally {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    protected void aleartforBack(String msg, String action) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (action.equalsIgnoreCase("Back")) {
                            Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                            Bundle mBundle = new Bundle();

                            mBundle.putSerializable("fuelDetail", fuelDetail);
                            mIntent.setAction("fuelPrice");
                            mIntent.putExtra("SelectedNameDetail", mBundle);
                            startActivity(mIntent);
                            finish();
                        }else if(action.equalsIgnoreCase("UpdateQty")){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {

            return;
        }

        if (requestCode == Vehicle_Request_Code_SCAN_QR) {

            Bundle extras = data.getBundleExtra("SelectedNameDetail");
            Log.e(TAG, "onNewIntent" + data.getAction());
            if (extras != null) {
                String mIntent = data.getAction();
                Log.e(TAG, "\n outside methodIntent=> " + mIntent);
                if (mIntent != null && mIntent.equals("VehicleQRCodeDetail")) {
                    Vehicle vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetail");
                    setVehicleDetail(vehicleDetails);
                }
            } else {
                TastyToast.makeText(getApplicationContext(), getString(R.string.invalid_qr_code), TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }
        }
    }

    private void setVehicleDetail(Vehicle vehicleDetails) {

        tv_vehicle_no.setText(vehicleDetails.getVehicle_registration_no());
        tv_ch_no.setText(vehicleDetails.getChassis_number());
        tv_model.setText(vehicleDetails.getVehicle_type());
        tv_ownerName.setText(vehicleDetails.getVehicle_owner_name());

        edt_fuel_amount.setEnabled(false);

        if (methodIntent.equalsIgnoreCase("UpdateFuelQty")) {
            edt_meter_reading.setText(updateFuelInfo.getMeter_reading());
            edt_fuel_rate.setText(updateFuelInfo.getRate());
            edt_fuel_qty.setText(updateFuelInfo.getQuantity());
            edt_fuel_qty.setEnabled(true);
            edt_meter_reading.setEnabled(false);
        } else {
            edt_fuel_rate.setText(fuelDetail.getRate());
            if (fuelDetail.isFixedQty()) {
                edt_fuel_qty.setFocusable(false);
                edt_fuel_qty.setFocusableInTouchMode(false);
                edt_fuel_qty.setText(fuelDetail.getQuantity());
            } else {
                edt_fuel_qty.setEnabled(true);
            }
        }


        card_view_VehicleDetail.setVisibility(View.VISIBLE);
        card_view_fuelDetail.setVisibility(View.VISIBLE);

    }

    public Double getFuelTotalAmount(Double fuelqty, Double itemRate) {
        try {
//            Log.e(TAG, "Select Type" + unit);
            DecimalFormat twoDForm = new DecimalFormat(".00");

            Double totFuelAmt = (fuelqty * itemRate);
            Log.e(TAG, "totFuelAmount = " + totFuelAmt);

            return Double.valueOf(twoDForm.format(totFuelAmt));
        } catch (Exception e) {
            return 0.00;
        }
    }

    public void sendFillFuelVichleDetailRequest(Fuel fueItem) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(VehicleEntryForFuel.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {
                    URLEncoder.encode("", "UTF-8");
                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Fuel_Details";
                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,
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

                                                    Fuel fuelItem = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }

                                                    Log.e(TAG, "Responce = " + fuelItem.getName());
                                                    TastyToast.makeText(getApplicationContext(), "Successfully filled fuel", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                    Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                    Bundle mBundle = new Bundle();

                                                    mBundle.putSerializable("fuelDetail", fuelDetail);
                                                    mIntent.setAction("fuelPrice");
                                                    mIntent.putExtra("SelectedNameDetail", mBundle);
                                                    startActivity(mIntent);
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

                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject = new JSONObject();

                                jsonObject.put("vehicle_number", fueItem.getVehicle_number());
                                jsonObject.put("meter_reading", fueItem.getMeter_reading());
                                jsonObject.put("rate", fueItem.getRate());
                                jsonObject.put("quantity", fueItem.getQuantity());
                                jsonObject.put("total", fueItem.getTotal());

                                jsonObject.put("pump_name", fueItem.getPump_name());
                                jsonObject.put("oem_name", fueItem.getOem_name());
                                jsonObject.put("supervisor_id", fueItem.getSupervisor_id());
                                jsonObject.put("supervisor_name", fueItem.getSupervisor_name());

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

    public void sendFillFuelVichleDetailRequest_JsonObjectRequest(Fuel fueItem) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(VehicleEntryForFuel.this);
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

                        jsonObject.put("vehicle_number", fueItem.getVehicle_number());
                        jsonObject.put("meter_reading", fueItem.getMeter_reading());
                        jsonObject.put("rate", fueItem.getRate());
                        jsonObject.put("quantity", fueItem.getQuantity());
                        jsonObject.put("total", fueItem.getTotal());

                        jsonObject.put("pump_name", fueItem.getPump_name());
                        jsonObject.put("oem_name", fueItem.getOem_name());
                        jsonObject.put("supervisor_id", fueItem.getSupervisor_id());
                        jsonObject.put("supervisor_name", fueItem.getSupervisor_name());


                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Fuel_Details";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
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

                                                    Fuel fuelItem = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }

                                                    Log.e(TAG, "Responce = " + fuelItem.getName());
                                                    TastyToast.makeText(getApplicationContext(), "Successfully filled fuel", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                    Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                    Bundle mBundle = new Bundle();

                                                    mBundle.putSerializable("fuelDetail", fuelDetail);
                                                    mIntent.setAction("fuelPrice");
                                                    mIntent.putExtra("SelectedNameDetail", mBundle);
                                                    startActivity(mIntent);
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
    }

    public void UpdateFillFuelVichleDetailRequest(Fuel fueItem) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(VehicleEntryForFuel.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Fuel_Details/" + URLEncoder.encode(fueItem.getName(), "UTF-8");
                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
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

                                                Fuel fuelItem = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                Log.e(TAG, "Responce = " + fuelItem.getName());
                                                TastyToast.makeText(getApplicationContext(), "Successfully filled fuel", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                Bundle mBundle = new Bundle();

                                                mBundle.putSerializable("fuelDetail", fuelDetail);
                                                mIntent.setAction("fuelPrice");
                                                mIntent.putExtra("SelectedNameDetail", mBundle);
                                                startActivity(mIntent);
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

                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject = new JSONObject();

                                jsonObject.put("quantity", fueItem.getQuantity());
                                jsonObject.put("total", fueItem.getTotal());

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

    public void UpdateFillFuelVichleDetailRequest_JsonObjectRequest(Fuel fueItem) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(VehicleEntryForFuel.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
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

                        jsonObject.put("quantity", fueItem.getQuantity());
                        jsonObject.put("total", fueItem.getTotal());

                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Fuel_Details/" + URLEncoder.encode(fueItem.getName(), "UTF-8");
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
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

                                                Fuel fuelItem = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                Log.e(TAG, "Responce = " + fuelItem.getName());
                                                TastyToast.makeText(getApplicationContext(), "Successfully filled fuel", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                Bundle mBundle = new Bundle();

                                                mBundle.putSerializable("fuelDetail", fuelDetail);
                                                mIntent.setAction("fuelPrice");
                                                mIntent.putExtra("SelectedNameDetail", mBundle);
                                                startActivity(mIntent);
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
        aleartforBack("Are you sure you want to go back?","Back");
    }
}