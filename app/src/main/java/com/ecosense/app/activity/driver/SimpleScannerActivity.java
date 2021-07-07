package com.ecosense.app.activity.driver;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.Result;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.ecosense.app.R;
import com.ecosense.app.activity.supervisor.VehicleEntryForFuel;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;
import com.ecosense.app.pojo.Voucher;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class SimpleScannerActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener, View.OnClickListener, ZXingScannerView.ResultHandler {
    private static final String TAG = SimpleScannerActivity.class.getSimpleName();
    private ZXingScannerView mScannerView;
    static String methodIntent = null;
    Boolean isConnected = false;
    UserSessionManger session = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_simple_scanner);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view


        onNewIntent(getIntent());
    }

    Voucher voucherInfo = null;
    static Vehicle vehicleDetails = null;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
////        Intent intent = getIntent();
//
//        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (intent.getAction() != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

            if (methodIntent != null && methodIntent.equals("SelectDriverUsedVoucher")) {
                // When Driver Click Used Voucher
                Bundle extras = intent.getBundleExtra("SelectedNameDetail");
                voucherInfo = (Voucher) extras.getSerializable("voucherInfo");
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                Log.e(TAG, "voucherInfo. Number " + voucherInfo.getName());
                Log.e(TAG, "voucherInfo. getVehicle_number " + voucherInfo.getVehicle_number());
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    @Override
    public void onClick(View v) {
        if (v == tv_vehicle_detail_cancel) {
            dialog_VehicleDetail.dismiss();
            mScannerView.resumeCameraPreview(this);
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e(TAG, rawResult.getText()); // Prints scan results
        Log.e(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        if (methodIntent.equalsIgnoreCase("DriverScanVehicleQRCOde")) {
            getVehicleDetailOnServer(rawResult.getText());
        } else if (methodIntent.equalsIgnoreCase("VehicleQRCodeScan")) {
            getVehicleDetailOnServer(rawResult.getText());
        } else if (methodIntent.equalsIgnoreCase("BinQRCodeScan")) {
            Intent intent = new Intent();
            intent.putExtra("BinQRCode", rawResult.getText());
            setResult(UpdateBinDetail.BIN_Request_Code_SCAN_QR, intent);
            finish();//finishing activity
        } else if (methodIntent.equalsIgnoreCase("SelectDriverUsedVoucher")) {
            getVehicleDetailOnServer(rawResult.getText());
        }
        // If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this);
    }

    protected void alertDialogFor_Voucher_Not_match() {
        String msg = getString(R.string.voucher_not_match);

        // do something when the button is clicked
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.btn_yes), (arg0, arg1) -> {
                    // If you would like to resume scanning, call this method below:
                    mScannerView.resumeCameraPreview(this);
                })
                .setNegativeButton(getString(R.string.btn_no), (arg0, arg1) -> {
//
                    Intent mIntent = new Intent(this, DriverReceivedVoucher.class);
                    startActivity(mIntent);
                    finish();
                })
                .show();
    }

    static ProgressDialog mProgressDialog = null;
    Dialog dialog_VehicleDetail;
    TextView tv_vehicle_no, tv_ch_no, tv_model, tv_ownerName, tv_vehicle_detail_confirm, tv_vehicle_detail_cancel;

    public void dialog_VehicleDetailConform(Vehicle vehicleDetails) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_vehicle_confirm, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_VehicleDetail = new Dialog(SimpleScannerActivity.this);
        dialog_VehicleDetail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_VehicleDetail.setContentView(root);
        dialog_VehicleDetail.setCancelable(false);
        Objects.requireNonNull(dialog_VehicleDetail.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_VehicleDetail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        tv_vehicle_no = dialog_VehicleDetail.findViewById(R.id.tv_vehicle_no);
        tv_ch_no = dialog_VehicleDetail.findViewById(R.id.tv_ch_no);
        tv_model = dialog_VehicleDetail.findViewById(R.id.tv_model);
        tv_ownerName = dialog_VehicleDetail.findViewById(R.id.tv_ownerName);

        tv_vehicle_detail_confirm = dialog_VehicleDetail.findViewById(R.id.tv_vehicle_detail_confirm);
        tv_vehicle_detail_cancel = dialog_VehicleDetail.findViewById(R.id.tv_vehicle_detail_cancel);


        tv_vehicle_no.setText(vehicleDetails.getVehicle_registration_no());
        tv_ch_no.setText(vehicleDetails.getChassis_number());
        tv_model.setText(vehicleDetails.getVehicle_type());
        tv_ownerName.setText(vehicleDetails.getVehicle_owner_name());

        tv_vehicle_detail_confirm.setOnClickListener(v -> {
            dialog_VehicleDetail.dismiss();

            if (methodIntent.equalsIgnoreCase("VehicleQRCodeScan")) {
                Intent mIntent = new Intent();
                Bundle mBundle = new Bundle();

                mBundle.putSerializable("vehicleDetail", vehicleDetails);
                mIntent.setAction("VehicleQRCodeDetail");
                mIntent.putExtra("SelectedNameDetail", mBundle);

                setResult(VehicleEntryForFuel.Vehicle_Request_Code_SCAN_QR, mIntent);
                finish();//finishing activity
            } else if (methodIntent.equalsIgnoreCase("SelectDriverUsedVoucher")) {
                updateVoucherDetailRequest(vehicleDetails);
            } else {
                assign_and_updateDriverNameInAssets(vehicleDetails);
            }
        });
        tv_vehicle_detail_cancel.setOnClickListener(this);

        dialog_VehicleDetail.show();
    }

    public void getVehicleDetailOnServer(String qrcode) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;


                    filters = "[[\"Asset\",\"item_code\",\"in\",[\"Vehicle\"]],"
                            + "[\"Asset\",\"barcode\",\"in\",[\"" + qrcode
                            + "\"]],[\"Asset\",\"status\",\"in\",[\""
                            + AppConfig.BIN_Status_Submitted
                            + "\"]]]";


//                    filters = URLEncoder.encode("[[\"Complaints\", \"cptuserid\", \"=\",\"", "UTF-8")
//                            + session.getpsNo()
//                            + URLEncoder.encode("\"],[\"Complaints\", \"cptmobileno\", \"=\",\"", "UTF-8")
//                            + session.getMobileNumber()
//                            + URLEncoder.encode("\"]]", "UTF-8");

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
//                    Log.e(TAG, "getVehicleDetailOnServer Vehicle_Master filters = " + filters + "&fields=" + fields);

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
                                                    Log.e(TAG, "vehicleDetails.getRoute_info() = " + vehicleDetails.getVehicle_registration_no());

                                                    if (methodIntent.equalsIgnoreCase("SelectDriverUsedVoucher")) {
                                                        if (vehicleDetails.getVehicle_registration_no().equalsIgnoreCase(voucherInfo.getVehicle_number())) {
                                                            dialog_VehicleDetailConform(vehicleDetails);
                                                        } else {
                                                            alertDialogFor_Voucher_Not_match();
                                                        }
                                                    } else {
                                                        dialog_VehicleDetailConform(vehicleDetails);
                                                    }
                                                }
                                            }
                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                            mScannerView.resumeCameraPreview(this);
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
                                    mScannerView.resumeCameraPreview(this);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        mScannerView.resumeCameraPreview(this);
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

    public void check_Vehicle_PendingRoute_OnServer(Vehicle v_Details) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;

                    filters = "[[\"DriverActivity\",\"da_vehicleno\",\"=\",\""
                            + v_Details.getVehicle_registration_no()
                            + "\"],[\"DriverActivity\", \"da_routeid\", \"=\",\""
                            + v_Details.getRoute_info()
                            + "\"],[\"DriverActivity\", \"r_status\", \"=\",\""
                            + AppConfig.ROUTE_Status_In_Progress
                            + "\"],[\"DriverActivity\", \"creation\", \"like\",\""
                            + Connection.getCurrentDate() + "%"
                            + "\"],[\"DriverActivity\", \"da_mobno\", \"=\",\""
                            + session.getMobileNumber()
                            + "\"]]";


//                    filters = URLEncoder.encode("[[\"Complaints\", \"cptuserid\", \"=\",\"", "UTF-8")
//                            + session.getpsNo()
//                            + URLEncoder.encode("\"],[\"Complaints\", \"cptmobileno\", \"=\",\"", "UTF-8")
//                            + session.getMobileNumber()
//                            + URLEncoder.encode("\"]]", "UTF-8");

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                    Log.e(TAG, "check_Vehicle_PendingRoute_OnServer DriverActivity filters = " + filters + "&fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/DriverActivity?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields;
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                    Log.e(TAG + "Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.e(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode vehicleItem = elements1.next();
                                                RouteItem routeDetails = objectMapper.readValue(vehicleItem.toString(), RouteItem.class);

                                                if (!vehicleItem.toString().trim().equalsIgnoreCase("{}")) {

                                                    Log.e(TAG, "DriverActivity routeDetails.getName() = " + routeDetails.getName());

                                                    if (routeDetails.getR_status().equalsIgnoreCase(AppConfig.ROUTE_Status_In_Progress)) {
                                                        Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
                                                        Bundle b = new Bundle();
                                                        routeDetails.setVehicle_registration_no(v_Details.getVehicle_registration_no());
                                                        b.putSerializable("pendingRouteDetails", routeDetails);
                                                        b.putSerializable("vehicleDetails", v_Details);
                                                        mIntent.setAction("pendingRoute");
                                                        mIntent.putExtra("SelectedVehicleDetail", b);
                                                        startActivity(mIntent);
                                                        finish();
                                                    } else {
                                                        Intent mIntent = new Intent(getApplicationContext(), VehicleDetail.class); // the activity that holds the fragment
                                                        Bundle b = new Bundle();
                                                        b.putSerializable("vehicleDetails", v_Details);
                                                        mIntent.setAction("getVehicleDetail");
                                                        mIntent.putExtra("SelectedVehicleDetail", b);
                                                        startActivity(mIntent);
                                                        finish();
                                                    }
//
                                                }
                                            }
                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                            Intent mIntent = new Intent(getApplicationContext(), VehicleDetail.class); // the activity that holds the fragment
                                            Bundle b = new Bundle();
                                            b.putSerializable("vehicleDetails", v_Details);
                                            mIntent.setAction("getVehicleDetail");
                                            mIntent.putExtra("SelectedVehicleDetail", b);
                                            startActivity(mIntent);
                                            finish();
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

    public void updateVoucherDetailRequest(Vehicle vehicleDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
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

                    url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                    requestMethod = Request.Method.PUT;

                    StringRequest jsonObjectRequest = new StringRequest(requestMethod, url,
                            (String response) -> {
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

                                                TastyToast.makeText(getApplicationContext(), "Your voucher is used", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);


                                                Intent mIntent = new Intent(this, DriverReceivedVoucher.class); // the activity that holds the fragment
                                                Bundle b = new Bundle();
                                                b.putSerializable("vehicleDetails", vehicleDetails);
                                                mIntent.setAction("SelectDriverUsedVoucher");
                                                mIntent.putExtra("SelectedVehicleDetail", b);
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

                                jsonObject.put("chassis_number", vehicleDetail.getChassis_number());
                                jsonObject.put("voucher_use_date", Connection.getCurrentDateTime());
                                jsonObject.put("voucher_status", AppConfig.Voucher_status_Used);


                                jsonObject.put("driver_id", session.getpsNo());
                                jsonObject.put("driver_name", session.getpsName());
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
    }
   public void updateVoucherDetailRequest_JsonObjectRequest(Vehicle vehicleDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
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


                        jsonObject.put("chassis_number", vehicleDetail.getChassis_number());
                        jsonObject.put("voucher_use_date", Connection.getCurrentDateTime());
                        jsonObject.put("voucher_status", AppConfig.Voucher_status_Used);


                        jsonObject.put("driver_id", session.getpsNo());
                        jsonObject.put("driver_name", session.getpsName());


                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    int requestMethod;

                    url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherInfo.getName(), "UTF-8");
                    requestMethod = Request.Method.PUT;

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

                                                TastyToast.makeText(getApplicationContext(), "Your voucher is used", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);


                                                Intent mIntent = new Intent(this, DriverReceivedVoucher.class); // the activity that holds the fragment
                                                Bundle b = new Bundle();
                                                b.putSerializable("vehicleDetails", vehicleDetails);
                                                mIntent.setAction("SelectDriverUsedVoucher");
                                                mIntent.putExtra("SelectedVehicleDetail", b);
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

    public void assign_and_updateDriverNameInAssets_JsonObjectRequest(Vehicle send_vehicleDetails) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
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


                        jsonObject.put("driver_id", session.getpsNo());
                        jsonObject.put("driver_name", session.getpsName());


                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    int requestMethod;

                    url = session.getMyServerIP() + "/api/resource/Asset/" + URLEncoder.encode(send_vehicleDetails.getName(), "UTF-8");
                    requestMethod = Request.Method.PUT;

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
                                                check_Vehicle_PendingRoute_OnServer(send_vehicleDetails);
                                                Log.e(TAG, "Responce = " + voDetail.getName());


                                            } else {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                                mScannerView.resumeCameraPreview(this);
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
                                        mScannerView.resumeCameraPreview(this);
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
                        mScannerView.resumeCameraPreview(this);
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

    public void assign_and_updateDriverNameInAssets(Vehicle send_vehicleDetails) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(SimpleScannerActivity.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {
                    JSONObject jsonObject = null;

                    String url = null;
                    int requestMethod;

                    url = session.getMyServerIP() + "/api/resource/Asset/" + URLEncoder.encode(send_vehicleDetails.getName(), "UTF-8");
                    requestMethod = Request.Method.PUT;

                    StringRequest jsonObjectRequest = new StringRequest(requestMethod, url,
                            (String response) -> {

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
                                                check_Vehicle_PendingRoute_OnServer(send_vehicleDetails);
                                                Log.e(TAG, "Responce = " + voDetail.getName());


                                            } else {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                                mScannerView.resumeCameraPreview(this);
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
                                        mScannerView.resumeCameraPreview(this);
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
                        mScannerView.resumeCameraPreview(this);
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("driver_id", session.getpsNo());
                                jsonObject.put("driver_name", session.getpsName());

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

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (methodIntent.equalsIgnoreCase("DriverScanVehicleQRCOde")) {
            Intent mIntent = new Intent(this, DriverDashBoard.class);
            startActivity(mIntent);
            finish();
        } else if (methodIntent.equalsIgnoreCase("VehicleQRCodeScan")) {
            Intent mIntent = new Intent();
            Bundle mBundle = new Bundle();
//            mBundle.putSerializable("vehicleDetail", vehicleDetails);
            mIntent.setAction("VehicleQRCodeDetail");
            mIntent.putExtra("SelectedNameDetail", mBundle);

            setResult(VehicleEntryForFuel.Vehicle_Request_Code_SCAN_QR, mIntent);
            finish();//finishing activity
        } else if (methodIntent.equalsIgnoreCase("BinQRCodeScan")) {
            Intent mIntent = new Intent();
            setResult(UpdateBinDetail.BIN_Request_Code_SCAN_QR, mIntent);
            finish();//finishing activity
        } else if (methodIntent.equalsIgnoreCase("SelectDriverUsedVoucher")) {

            Intent mIntent = new Intent(this, DriverReceivedVoucher.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("vehicleDetails", vehicleDetails);
            mIntent.setAction("SelectDriverUsedVoucher");
            mIntent.putExtra("SelectedVehicleDetail", b);
            startActivity(mIntent);
            finish();
        }
    }

}
