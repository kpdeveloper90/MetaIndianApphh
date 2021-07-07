package com.ecosense.app.activity.metaData;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.driver.SimpleScannerActivity;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class UpdateMetaData extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = UpdateMetaData.class.getSimpleName();
    private Toolbar toolbar;
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.img_getCurrentLoc)
    ImageView img_getCurrentLoc;

    @BindView(R.id.tv_new_poi_date)
    TextView tv_new_poi_date;

    @BindView(R.id.edt_bin_name)
    TextInputEditText edt_bin_name;

    @BindView(R.id.edt_new_poi_location)
    TextInputEditText edt_new_poi_location;

    @BindView(R.id.sp_wardNo)
    Spinner sp_wardNo;

    @BindView(R.id.sp_poi_type)
    Spinner sp_poi_type;

    @BindView(R.id.constraintLayout2)
    ConstraintLayout constraintLayout_BINDetail;

    @BindView(R.id.edt_bin_capacity)
    TextInputEditText edt_bin_capacity;

    @BindView(R.id.edt_age_of_bin)
    TextInputEditText edt_age_of_bin;

    @BindView(R.id.sp_type_of_bin)
    Spinner sp_type_of_bin;

    @BindView(R.id.sp_type_of_Establishments)
    Spinner sp_type_of_Establishments;

    @BindView(R.id.card_view_capture_pic)
    CardView card_view_capture_pic;
    @BindView(R.id.card_view_scanQR)
    CardView card_view_scanQR;

    @BindView(R.id.ll_btn_take_a_Pic)
    LinearLayout ll_btn_take_a_Pic;

    @BindView(R.id.ll_btn_scanQR)
    LinearLayout ll_btn_scanQR;

    @BindView(R.id.img_of_bin_pic)
    ImageView img_of_bin_pic;

    @BindView(R.id.tv_qrCode)
    TextView tv_qrCode;

    @BindView(R.id.im_btn_cancel)
    ImageView im_btn_cancel;

    @BindView(R.id.im_btn_submit)
    ImageView im_btn_submit;
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
        setContentView(R.layout.activity_update_meta_data);


        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MetaData");
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

        tv_new_poi_date.setText(Connection.getCurrentDateTime());
        setSpinnerData(sp_wardNo, R.array.wardNumber);
        setSpinnerData(sp_poi_type, R.array.poi_type);
        setSpinnerData(sp_type_of_Establishments, R.array.type_of_Establishments);
        setSpinnerData(sp_type_of_bin, R.array.type_of_bin);

        constraintLayout_BINDetail.setVisibility(View.GONE);
        // For Observations 06-Aug-19 change point:5 Standard UOM to be provided (Remove UOM from App)
//        sp_poi_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                       int arg2, long arg3) {
//                String poi_type = sp_poi_type.getSelectedItem().toString();
//                if (poi_type.equalsIgnoreCase("BIN")) {
//                    if (constraintLayout_BINDetail.getVisibility() == View.GONE)
//                        constraintLayout_BINDetail.setVisibility(View.VISIBLE);
//                } else {
//                    if (constraintLayout_BINDetail.getVisibility() == View.VISIBLE)
//                        constraintLayout_BINDetail.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });
        im_btn_submit.setOnClickListener(this);
        im_btn_cancel.setOnClickListener(this);
        ll_btn_scanQR.setOnClickListener(this);
        ll_btn_take_a_Pic.setOnClickListener(this);
        FlagImg_AvailableOrNot = false;
        FlagQRCode_AvailableOrNot = false;
        img_getCurrentLoc.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    static String methodIntent = null;
    static Assets metaDataItem;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
//        Intent intent = getIntent();
        try {
            Bundle extras = intent.getBundleExtra("SelectedNameDetail");
            Log.e(TAG, "onNewIntent" + intent.getAction());
            if (extras != null) {
                methodIntent = intent.getAction();
                Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

                if (methodIntent != null && methodIntent.equals("UpdateMetaData")) {
                    metaDataItem = (Assets) extras.getSerializable("metaDataItem");

                    toolbar.setTitle("Update MetaData");
                    toolbar.invalidate();

                    edt_bin_name.setText(metaDataItem.getAsset_name());
                    edt_new_poi_location.setText(metaDataItem.getFalocation());
                    tv_new_poi_date.setText(metaDataItem.getModified());
                    tv_qrCode.setText(metaDataItem.getFaqr_code_no());

                    FlagQRCode_AvailableOrNot = true;

                    find_setSpinnerData(sp_wardNo, R.array.wardNumber, metaDataItem.getFawardno());
                    find_setSpinnerData(sp_poi_type, R.array.poi_type, metaDataItem.getItem_code());

                    // FOr POI Update below 2 field is disable
                    edt_bin_name.setEnabled(false);
                    sp_poi_type.setEnabled(false);
                    if (metaDataItem.getFabinimage() != null) {
                        FlagImg_AvailableOrNot = true;
                        Glide.with(this).load(Connection.decodeFromBase64ToBitmap(metaDataItem.getFabinimage()))
                                .apply(RequestOptions.centerCropTransform())
                                .into(img_of_bin_pic);
                    } else {

                        Glide.with(this).load(R.drawable.default_image)
                                .apply(RequestOptions.centerCropTransform())
                                .into(img_of_bin_pic);
                    }

                    if (metaDataItem.getItem_code().equalsIgnoreCase("BIN")) {

                        if (constraintLayout_BINDetail.getVisibility() == View.GONE)
                            constraintLayout_BINDetail.setVisibility(View.VISIBLE);

                        edt_age_of_bin.setText(metaDataItem.getAge_of_bin());
                        edt_bin_capacity.setText(metaDataItem.getFacapcty());
                        find_setSpinnerData(sp_type_of_bin, R.array.type_of_bin, metaDataItem.getType_of_bin());
                    } else {
                        if (constraintLayout_BINDetail.getVisibility() == View.VISIBLE)
                            constraintLayout_BINDetail.setVisibility(View.GONE);
                    }
                    find_setSpinnerData(sp_type_of_Establishments, R.array.type_of_Establishments, metaDataItem.getType_of_establishments());


                } else if (methodIntent != null && methodIntent.equals("UpdateLatLong")) {
                    metaDataItem = (Assets) extras.getSerializable("metaDataItem");
                } else if (methodIntent != null && methodIntent.equals("NewLatLong")) {
                    metaDataItem = (Assets) extras.getSerializable("metaDataItem");

                    edt_new_poi_location.setText(metaDataItem.getFalocation());
                }
            } else {
                Log.e(TAG, "Bundle Is empty ");
            }
        } catch (Exception e) {
            Log.e(TAG, "onNewIntent Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == img_getCurrentLoc) {
            finish();
            Intent mIntent = new Intent(getApplicationContext(), CurrentLocationMark.class); // the activity that holds the fragment
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("metaDataItem", metaDataItem);

            if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                mIntent.setAction("UpdateMetaData");
            } else {
                mIntent.setAction("NewLatLong");
            }

            mIntent.putExtra("SelectedNameDetail", mBundle);
            startActivity(mIntent);
        }

        if (v == ll_btn_take_a_Pic) {
            takePhotoFromBackCamera(Request_Code_CapturePIC);
        }
        if (v == ll_btn_scanQR) {
            Intent intent = new Intent(getApplicationContext(), SimpleScannerActivity.class);
            intent.setAction("BinQRCodeScan");
            startActivityForResult(intent, BIN_Request_Code_SCAN_QR);
        }
        if (v == im_btn_cancel) {
            String msg = getString(R.string.poi_entry_discarded);
            aleartforBack(msg);
        }
        if (v == im_btn_submit) {
            if (methodIntent != null && methodIntent.equals("UpdateMetaData")) {
                aleartforUpdate(getString(R.string.edit_poi));
            } else {
                getTextData();
            }

        }
    }

    public void getTextData() {
        try {

            JSONObject jsonObject = null;

            String date = Connection.getCurrentDateTime();

            if (Objects.requireNonNull(edt_bin_name.getText()).toString().isEmpty()) {
                edt_bin_name.setError(getString(R.string.enter_poi_name));
                TastyToast.makeText(getApplicationContext(), getString(R.string.enter_poi_name), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else {
                String poi_loc = Objects.requireNonNull(edt_new_poi_location.getText()).toString().trim();
                String wardNO = Objects.requireNonNull(sp_wardNo.getSelectedItem()).toString();
                String poiType = Objects.requireNonNull(sp_poi_type.getSelectedItem()).toString();
                String type_of_Establishments = Objects.requireNonNull(sp_type_of_Establishments.getSelectedItem()).toString();

                jsonObject = new JSONObject();
//            jsonObject.put("fadate", date);
//            jsonObject.put("fatype", AppConfig.fatype_Asset);
                String bin_name = Objects.requireNonNull(edt_bin_name.getText()).toString().trim();
                jsonObject.put("longitude", metaDataItem.getFalongitude());
                jsonObject.put("latitude", metaDataItem.getFalatitude());
                jsonObject.put("address", poi_loc);
                jsonObject.put("geo_location", poi_loc);
                jsonObject.put("ward_no", wardNO);

                jsonObject.put("employee_id", session.getpsNo());
//            jsonObject.put("fastatus", AppConfig.BIN_Status_Scheduled);
                jsonObject.put("type_of_establishments", type_of_Establishments);
                jsonObject.put("is_existing_asset", "1");
                jsonObject.put("gross_purchase_amount", "1");

                jsonObject.put("location", "Vadodara");
                jsonObject.put("company", "Ecosense");
                if (!methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                    jsonObject.put("asset_name", bin_name);// update
                    jsonObject.put("item_code", poiType);// update
                    jsonObject.put("purchase_date", Connection.getCurrentDate()); // update
                }
//                if (poiType.equalsIgnoreCase("BIN")) {
//
//                    String binCapacity = Objects.requireNonNull(edt_bin_capacity.getText()).toString().trim();
//                    String age_of_bin = Objects.requireNonNull(edt_age_of_bin.getText()).toString().trim();
//                    String type_of_bin = Objects.requireNonNull(sp_type_of_bin.getSelectedItem()).toString();
//
//                    jsonObject.put("capacity", binCapacity);
//                    jsonObject.put("age_of_bin", age_of_bin);
//                    jsonObject.put("type_of_bin", type_of_bin);
//                }
                if (!FlagImg_AvailableOrNot) {
                    TastyToast.makeText(getApplicationContext(), getString(R.string.please_capture_image), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                } else {
                    if (!FlagQRCode_AvailableOrNot && tv_qrCode.getText().toString().equalsIgnoreCase("NA")) {
//                        TastyToast.makeText(getApplicationContext(), "Please scan QR-Code", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, "faqr_code_no not available ");
                    } else {
                        Log.e(TAG, "faqr_code_no =  " + tv_qrCode.getText().toString());
                        jsonObject.put("barcode", tv_qrCode.getText().toString());
                    }

                    Bitmap com_Pic_bitmap = ((BitmapDrawable) img_of_bin_pic.getDrawable()).getBitmap();
                    String image = Connection.encodeBitmapToFromBase64(com_Pic_bitmap);
                    jsonObject.put("image_string", image);

                    addPOIDetailRequestServer(jsonObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }

    protected void aleartforUpdate(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        getTextData();
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

    public void addPOIDetailRequestServer(JSONObject jsonObject) {
        try {
            mProgressDialog = new ProgressDialog(UpdateMetaData.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {

                String url = null;
                int method = 0;
                if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                    url = session.getMyServerIP() + "/api/resource/Asset/" + metaDataItem.getAsset_Id();
                    method = Request.Method.PUT;
                } else {
                    url = session.getMyServerIP() + "/api/resource/Asset";
                    method = Request.Method.POST;
                }
                Log.e(TAG, "addPOIDetailRequestServer url = " + url);
                Log.e(TAG, "addPOIDetailRequestServer method = " + method);
                StringRequest jsonObjectRequest = new StringRequest(method, url,
                        new Response.Listener<String>() {
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

                                            Assets compDetail = objectMapper.readValue(statusData.toString(), Assets.class);

                                            Log.e(TAG, "Assets response Successfully add  getName= " + compDetail.getAsset_Id());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.successfully_updated), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            } else {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.successfully_added), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            }

                                            Intent mIntent = new Intent(getApplicationContext(), MyPOICollected.class); // the activity that holds the fragment
                                            Bundle mBundle = new Bundle();
                                            mBundle.putSerializable("metaDataItem", metaDataItem);
                                            mIntent.setAction("UpdateMetaData");
                                            mIntent.putExtra("SelectedNameDetail", mBundle);
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
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }


                        error.printStackTrace();
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in onErrorResponse  ErrorListener");
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

//                        JSONObject jsonObject = new JSONObject();
//
//                        try {

                        params.put("data", jsonObject.toString());

                        Log.e(TAG, "addPOIDetailRequestServer data getParams = " + params.toString());
//                        } catch (JSONException e) {
//                            Log.e("JSONObject Here", e.toString());
//                        }
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

    public void addPOIDetailRequestServer_JsonObjectRequest(JSONObject jsonObject) {
        try {
            mProgressDialog = new ProgressDialog(UpdateMetaData.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {
//                JSONObject jsonObject = null;
//
//                try {
//
//
//                    jsonObject = new JSONObject();
//                    jsonObject.put("fadate", "");
//                } catch (JSONException e) {
//                    Log.e("JSONObject Here", e.toString());
//                }

                String url = null;
                int method = 0;
                if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                    url = session.getMyServerIP() + "/api/resource/Asset/" + metaDataItem.getAsset_Id();
                    method = Request.Method.PUT;
                } else {
                    url = session.getMyServerIP() + "/api/resource/Asset";
                    method = Request.Method.POST;
                }


                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, jsonObject,
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

                                            Assets compDetail = objectMapper.readValue(statusData.toString(), Assets.class);

                                            Log.e(TAG, "Assets response Successfully add  getName= " + compDetail.getAsset_Id());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                                                TastyToast.makeText(getApplicationContext(), "Successfully Updated", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            } else {
                                                TastyToast.makeText(getApplicationContext(), "Successfully Added", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                            }

                                            Intent mIntent = new Intent(getApplicationContext(), MyPOICollected.class); // the activity that holds the fragment
                                            Bundle mBundle = new Bundle();
                                            mBundle.putSerializable("metaDataItem", metaDataItem);
                                            mIntent.setAction("UpdateMetaData");
                                            mIntent.putExtra("SelectedNameDetail", mBundle);
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
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }


                        error.printStackTrace();
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in onErrorResponse  ErrorListener");
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
            String qrcode = data.getStringExtra("BinQRCode");
            Log.e(TAG, "qrcode = = " + qrcode);


            if (qrcode != null) {
                tv_qrCode.setText(qrcode);
                FlagQRCode_AvailableOrNot = true;
            } else {
                TastyToast.makeText(getApplicationContext(), getString(R.string.invalid_qr_code), TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }
        } else if (requestCode == Request_Code_CapturePIC) {


            Bitmap VISIPIC_bitmao = (Bitmap) data.getExtras().get("data");

            img_of_bin_pic.setImageBitmap(VISIPIC_bitmao);

            FlagImg_AvailableOrNot = true;

        }
    }

    public void setSpinnerData(Spinner spname, int arrayId) {
        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNumber);

    }

    public void find_setSpinnerData(Spinner spname, int arrayId, String locType) {
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNo);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            spname.setSelection(spinnerPosition);
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

    protected void aleartforBack(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent mIntent = new Intent(getApplicationContext(), MyPOICollected.class); // the activity that holds the fragment
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable("metaDataItem", metaDataItem);
                        mIntent.setAction("UpdateMetaData");
                        mIntent.putExtra("SelectedNameDetail", mBundle);
                        startActivity(mIntent);
                        finish();
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
    public void onBackPressed() {
//        super.onBackPressed();
        String msg = getString(R.string.do_you_want_to_back_entery_discarded);
        aleartforBack(msg);
    }

}
