package com.ecosense.app.activity.driver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class UpdateBinDetail extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = UpdateBinDetail.class.getSimpleName();
    private Toolbar toolbar;
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    @BindView(R.id.ll_btn_scanQR)
    LinearLayout ll_btn_scanQR;

    @BindView(R.id.ll_btn_take_a_Pic)
    LinearLayout ll_btn_take_a_Pic;

    @BindView(R.id.img_of_bin_pic)
    ImageView img_of_bin_pic;

    @BindView(R.id.tv_qrCodeLoc)
    TextView tv_qrCodeLoc;

    @BindView(R.id.tv_qrcapecity)
    TextView tv_qrcapecity;

    @BindView(R.id.im_btn_cancel)
    Button im_btn_cancel;

    @BindView(R.id.im_btn_submit)
    Button im_btn_submit;

    @BindView(R.id.card_view_scanQR)
    CardView card_view_scanQR;

    @BindView(R.id.card_view_capture_pic)
    CardView card_view_capture_pic;


    static RouteInfo routeinfoDetail = null;
    Complaints selectComplaintsData = null;
    static Vehicle vehicleDetails = null;
    static RouteItem routeDetails = null;
    static Boolean FlagImg_AvailableOrNot = false;
    static Boolean FlagQRCode_AvailableOrNot = false;
    public static final int Request_Code_CapturePIC = 102;
    public static final int BIN_Request_Code_SCAN_QR = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bin_detail);


        ButterKnife.bind(this);

        isConnected = checkConnection();
        onNewIntent(getIntent());

        toolbar = (Toolbar) findViewById(R.id.include6);
        toolbar.setTitle("Update Bin Status");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        im_btn_submit.setOnClickListener(this);
        im_btn_cancel.setOnClickListener(this);
        ll_btn_scanQR.setOnClickListener(this);
        ll_btn_take_a_Pic.setOnClickListener(this);
        FlagImg_AvailableOrNot = false;
        FlagQRCode_AvailableOrNot = false;
    }

    @Override
    public void onClick(View v) {
        if (v == ll_btn_take_a_Pic) {
            takePhotoFromBackCamera(Request_Code_CapturePIC);
        }
        if (v == ll_btn_scanQR) {
            Intent intent = new Intent(getApplicationContext(), SimpleScannerActivity.class);
            intent.setAction("BinQRCodeScan");
            startActivityForResult(intent, BIN_Request_Code_SCAN_QR);
        }
        if (v == im_btn_submit) {
            if (methodIntent != null && methodIntent.equals("AssignCitizenComplaints")) {
                getTextDataForComplaintResolve();
                Log.e(TAG, "in if AssignCitizenComplaints = ");
            } else {
                getTextData();
                Log.e(TAG, "in else getTextData ");
            }
        }
        if (v == im_btn_cancel) {
            String msg = getString(R.string.do_you_want_to_move_back_you_have_still_not_update_bin);
            aleartforBack(msg);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Bundle extras = intent.getBundleExtra("SelectedVehicleDetail");
            Log.e(TAG, "onNewIntent" + intent.getAction());
            if (extras != null) {
                methodIntent = intent.getAction();
                Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

                if (methodIntent != null && methodIntent.equals("getRouteDetail")) {
                    routeDetails = (RouteItem) extras.getSerializable("routeDetails");
                    routeinfoDetail = (RouteInfo) extras.getSerializable("routeinfoDetail");
                    vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");

                    Log.e(TAG, "routeDetails.getName() " + routeDetails.getName());
                    Log.e(TAG, "routeinfoDetail.getName() " + routeinfoDetail.getName());
                    Log.e(TAG, "routeinfoDetail.getRoute_qrcode() " + routeinfoDetail.getRoute_qrcode());

                    if (routeinfoDetail.getRoute_qrcode().equalsIgnoreCase("Enable")) {
                        card_view_scanQR.setVisibility(View.VISIBLE);
                    } else {
                        card_view_scanQR.setVisibility(View.GONE);
                    }

                    if (routeinfoDetail.getRoute_photo().equalsIgnoreCase("Enable")) {
                        card_view_capture_pic.setVisibility(View.VISIBLE);
                    } else {
                        card_view_capture_pic.setVisibility(View.GONE);
                    }
                } else if (methodIntent != null && methodIntent.equals("AssignCitizenComplaints")) {
                    selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                    vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                    Log.e(TAG, "selectComplaintsData.getComId() = " + selectComplaintsData.getComId());
                    card_view_scanQR.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "Bundle Is empty ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTextData() {
        try {

//            String qrCodeLoc = tv_qrCodeLoc.getText().toString().trim();
//            String qrcapecity = tv_qrcapecity.getText().toString().trim();
//
//
//            Log.e(TAG, "qrCodeLoc = " + qrCodeLoc
//                    + "\n rDate_time = " + rDate_time
//                    + "\n qrcapecity = " + qrcapecity
//                    + "\n session.getDLNumber() = " + session.getDLNumber()
//                    + "\n session.getMobileNumber() = " + session.getMobileNumber()
//                    + "\n session.getpsName() = " + session.getpsName()
//                    + "\n session.getpsNo() = " + session.getpsNo()
//            );
//
            if (routeinfoDetail.getRoute_photo().equalsIgnoreCase("Enable")) {
                if (!FlagImg_AvailableOrNot) {
                    TastyToast.makeText(getApplicationContext(), getString(R.string.please_capture_image), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                } else {
                    if (routeinfoDetail.getRoute_qrcode().equalsIgnoreCase("Enable")) {
                        if (!FlagQRCode_AvailableOrNot) {
                            TastyToast.makeText(getApplicationContext(), getString(R.string.please_scan_qr_code), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        } else {
                            Bitmap com_Pic_bitmap = ((BitmapDrawable) img_of_bin_pic.getDrawable()).getBitmap();
                            String image = Connection.encodeBitmapToFromBase64(com_Pic_bitmap);
                            updateBinDetailRequestServer(image, AppConfig.BIN_Status_Clean);
                        }
                    } else {
                        Bitmap com_Pic_bitmap = ((BitmapDrawable) img_of_bin_pic.getDrawable()).getBitmap();
                        String image = Connection.encodeBitmapToFromBase64(com_Pic_bitmap);
                        updateBinDetailRequestServer(image, AppConfig.BIN_Status_Clean);
                    }
                }
            } else {
                if (routeinfoDetail.getRoute_qrcode().equalsIgnoreCase("Enable")) {
                    if (!FlagQRCode_AvailableOrNot) {
                        TastyToast.makeText(getApplicationContext(), getString(R.string.please_scan_qr_code), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                    } else {
                        updateBinDetailRequestServer(null, AppConfig.BIN_Status_Clean);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }

    public void getTextDataForComplaintResolve() {
        try {

            if (!FlagImg_AvailableOrNot) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.please_capture_image), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else {
                Bitmap com_Pic_bitmap = ((BitmapDrawable) img_of_bin_pic.getDrawable()).getBitmap();
                String image = Connection.encodeBitmapToFromBase64(com_Pic_bitmap);
                updateComplaintsRequestServer(selectComplaintsData.getComId(), image, AppConfig.CPTSTATUS_Complete, Connection.getCurrentDateTime());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(),  getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }

    public void updateComplaintsRequestServer(String comID, String binImage, String status, String comresDate) {
        try {
            mProgressDialog = new ProgressDialog(UpdateBinDetail.this);
            mProgressDialog.setMessage( getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(comID, "UTF-8");

                Log.e(TAG, "updateComplaintsRequestServer url = " + url);

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

                                                Intent mIntent = new Intent(getApplicationContext(), MyAssignComplaintsList.class); // the activity that holds the fragment
                                                Bundle b = new Bundle();
                                                b.putSerializable("vehicleDetails", vehicleDetails);
                                                mIntent.setAction("getVehicleDetail");
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
                            jsonObject.put("cptresdate", comresDate);
                            jsonObject.put("cptstatus", status);
                            jsonObject.put("cptresphoto", binImage);

                            params.put("data", jsonObject.toString());
                            Log.e(TAG, "updateComplaintsRequestServer JSONObject Here params = " + params.toString());
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

    public void updateComplaintsRequestServer__JsonObjectRequest(String comID, String binImage, String status, String comresDate) {
        try {
            mProgressDialog = new ProgressDialog(UpdateBinDetail.this);
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
                    jsonObject.put("cptresphoto", binImage);
                    jsonObject.put("cptresdate", comresDate);
                    jsonObject.put("cptstatus", status);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(comID, "UTF-8");
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

                                                Intent mIntent = new Intent(getApplicationContext(), MyAssignComplaintsList.class); // the activity that holds the fragment
                                                Bundle b = new Bundle();
                                                b.putSerializable("vehicleDetails", vehicleDetails);
                                                mIntent.setAction("getVehicleDetail");
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

    public void updateBinDetailRequestServer_JsonObjectRequest(String binImage, String status) {
        try {
            mProgressDialog = new ProgressDialog(UpdateBinDetail.this);
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
                    jsonObject.put("clean_status", status);
                    jsonObject.put("route_binphoto", binImage);
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
                                            TastyToast.makeText(getApplicationContext(), "Successfully Updated", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
                                            Bundle b = new Bundle();
                                            b.putSerializable("pendingRouteDetails", routeDetails);
                                            b.putSerializable("vehicleDetails", vehicleDetails);
                                            mIntent.setAction("pendingRoute");
                                            mIntent.putExtra("SelectedVehicleDetail", b);
                                            startActivity(mIntent);
                                            finish();
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

    public void updateBinDetailRequestServer(String binImage, String status) {
        try {
            mProgressDialog = new ProgressDialog(UpdateBinDetail.this);
            mProgressDialog.setMessage( getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {

                String url = null;

                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                Log.e(TAG, "updateBinDetailRequestServer url = " + url);
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
                                            TastyToast.makeText(getApplicationContext(), "Successfully Updated", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
                                            Bundle b = new Bundle();
                                            b.putSerializable("pendingRouteDetails", routeDetails);
                                            b.putSerializable("vehicleDetails", vehicleDetails);
                                            mIntent.setAction("pendingRoute");
                                            mIntent.putExtra("SelectedVehicleDetail", b);
                                            startActivity(mIntent);
                                            finish();
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

                            jsonObject.put("clean_status", status);
                            jsonObject.put("route_binphoto", binImage);

                            params.put("data", jsonObject.toString());

                            Log.e(TAG, "updateBinDetailRequestServer JSONObject Here params = " + params.toString());
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

    private void takePhotoFromBackCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 2);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(intent, requestCode);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {

            return;
        }

        if (requestCode == BIN_Request_Code_SCAN_QR) {

            if (data != null) {
                String qrcode = data.getStringExtra("BinQRCode");
                Log.e(TAG, "qrcode = = " + qrcode);
                try {
                    Log.e(TAG, "routeinfoDetail.getRoute_location_name() = " + routeinfoDetail.getRoute_location_name());
                    if (routeinfoDetail.getBarcode() != null) {
                        if (routeinfoDetail.getBarcode().equalsIgnoreCase(qrcode)) {
                            tv_qrCodeLoc.setText(routeinfoDetail.getRoute_assetloc());
                            tv_qrcapecity.setText(routeinfoDetail.getRoute_assetcap());
                            FlagQRCode_AvailableOrNot = true;
                        } else {
                            tv_qrCodeLoc.setText("");
                            tv_qrcapecity.setText("");
                            TastyToast.makeText(getApplicationContext(), getString(R.string.invalid_qr_code), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        }
                    }else {
                        TastyToast.makeText(getApplicationContext(), getString(R.string.not_found_qr_code), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    }
                } catch (Exception e) {
                    TastyToast.makeText(getApplicationContext(),  getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            } else {
                Log.e(TAG, "data is null = " );
                TastyToast.makeText(getApplicationContext(),  getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }


        } else if (requestCode == Request_Code_CapturePIC) {


            Bitmap VISIPIC_bitmao = (Bitmap) data.getExtras().get("data");

//                int orientation = Exif.getOrientation(getBitmapAsByteArray(VISIPIC_bitmao));
//                img_capturePic.setImageBitmap(rotateImage(VISIPIC_bitmao, orientation));
            img_of_bin_pic.setImageBitmap(VISIPIC_bitmao);
//                imageUri = getImageUri(getApplicationContext(), VISIPIC_bitmao);
//                targetPath = getRealPathFromURI(imageUri);

//                uploadPhotoOnServer();
            FlagImg_AvailableOrNot = true;

        }
    }

//
//    public static String imageConvertBase64(Bitmap bitmap) {
//        String encodedImage = "";
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
////        int flags = Base64.DEFAULT | Base64.NO_PADDING | Base64.NO_WRAP;
////        encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
//        try {
////            encodedImage = URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT), "UTF-8");
//            encodedImage = "data:image/jpeg;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
////        } catch (UnsupportedEncodingException e) {
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return encodedImage;
//    }

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

    protected void aleartforBack(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle("Alert")
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {


                        if (methodIntent != null && methodIntent.equals("AssignCitizenComplaints")) {
                            Intent mIntent = new Intent(getApplicationContext(), MyAssignComplaintsList.class); // the activity that holds the fragment
                            Bundle b = new Bundle();
                            b.putSerializable("vehicleDetails", vehicleDetails);
                            mIntent.setAction("getVehicleDetail");
                            mIntent.putExtra("SelectedVehicleDetail", b);
                            startActivity(mIntent);
                            finish();
                        } else {
                            Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
                            Bundle b = new Bundle();
                            b.putSerializable("pendingRouteDetails", routeDetails);
                            b.putSerializable("vehicleDetails", vehicleDetails);
                            mIntent.setAction("pendingRoute");
                            mIntent.putExtra("SelectedVehicleDetail", b);
                            startActivity(mIntent);
                            finish();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        String msg = "Do you want to move back you have still not update bin ";
        aleartforBack(msg);

    }


}
