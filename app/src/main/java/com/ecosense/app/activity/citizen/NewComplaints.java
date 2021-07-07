package com.ecosense.app.activity.citizen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.driver.RouteMapList;
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

public class NewComplaints extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = NewComplaints.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @BindView(R.id.img_comp_image)
    ImageView img_comp_image;

    @BindView(R.id.img_capturePic)
    ImageView img_capturePic;

    @BindView(R.id.sp_com_wardNumber)
    Spinner sp_com_wardNumber;
    @BindView(R.id.sp_com_type)
    Spinner sp_com_type;

    @BindView(R.id.sp_typeof_loc)
    Spinner sp_typeof_loc;

    @BindView(R.id.im_btn_submit)
    ImageView im_btn_submit;

    @BindView(R.id.img_icon_word_detail)
    ImageView img_icon_word_detail;

    @BindView(R.id.img_getCurrentLoc)
    ImageView img_getCurrentLoc;

    @BindView(R.id.edt_com_location)
    TextInputEditText edt_com_location;

    @BindView(R.id.edt_com_desc)
    TextInputEditText edt_com_desc;


    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session = null;
    public static final int Request_Code_CapturePIC = 102;
    static private Uri imageUri;
    static private String targetPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* Set the app into full screen mode */
        getWindow().getDecorView().setSystemUiVisibility(flags);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        setContentView(R.layout.activity_new_complaints);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("New Complaint");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();

        });

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, getString(R.string.you_are_offline));
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        CheckGpsStatus();
        Log.e(TAG, "GpsStatus  = " + GpsStatus);

        if (!GpsStatus)
            alertToturnOffGps();
        else
            getMyCurrentLocation();
        setCOMTYPE_LOCType_Default();

        img_capturePic.setOnClickListener(this);
        im_btn_submit.setOnClickListener(this);
        img_getCurrentLoc.setOnClickListener(this);
        img_icon_word_detail.setOnClickListener(this);

        onNewIntent(getIntent());
    }

    String methodIntent = null;
    static Complaints selectComplaintsData = null;
    static Vehicle vehicleDetails = null;
    static RouteItem routeDetails = null;
    static RouteInfo routeinfoDetail = null;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
//        Intent intent = getIntent();
        methodIntent = intent.getAction();
        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            if (methodIntent != null && methodIntent.equals("SelectComplaintsReopen")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");

                edt_com_location.setText(selectComplaintsData.getCptloc());
                edt_com_desc.setText(selectComplaintsData.getCptdescription());
                if (selectComplaintsData.getCpttype() != null && selectComplaintsData.getCptloctype() != null) {

                    Log.e(TAG, selectComplaintsData.getCpttype());
                    Log.e(TAG, selectComplaintsData.getCptloctype());

                    setComType(selectComplaintsData.getCpttype());
                    setLocType(selectComplaintsData.getCptloctype());
                }
                setWardNumber(selectComplaintsData.getCptward_no());


            } else if (methodIntent != null && methodIntent.equals("SelectComplaintsForUpdate")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                toolbar.setTitle("Edit Complaints");
                toolbar.invalidate();

                edt_com_location.setText(selectComplaintsData.getCptloc());
                edt_com_desc.setText(selectComplaintsData.getCptdescription());

                if (selectComplaintsData.getCpttype() != null && selectComplaintsData.getCptloctype() != null) {

                    Log.e(TAG, selectComplaintsData.getCpttype());
                    Log.e(TAG, selectComplaintsData.getCptloctype());

                    setComType(selectComplaintsData.getCpttype());
                    setLocType(selectComplaintsData.getCptloctype());
                }
                setWardNumber(selectComplaintsData.getCptward_no());

                if (selectComplaintsData.getCptphoto() == null) {
                    Glide.with(getApplicationContext()).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_comp_image);
                } else {
//                    String url = session.getMyServerIP() + selectComplaintsData.getCptphoto();
                    Glide.with(this).load(Connection.decodeFromBase64ToBitmap(selectComplaintsData.getCptphoto()))
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_comp_image);
                    FlagImg_AvailableOrNot = true;
                }

            } else if (methodIntent != null && methodIntent.equals("DriveNewComplaint")) {
                routeDetails = (RouteItem) extras.getSerializable("routeDetails");
                routeinfoDetail = (RouteInfo) extras.getSerializable("routeinfoDetail");
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");

                edt_com_desc.setText(new StringBuilder().append(routeinfoDetail.getRoute_assetname()).append("\n").append(routeinfoDetail.getRoute_assetloc()).toString());

            }
        } else {
            Log.e(TAG, "Bundle Is empty ");

        }

    }

    static Boolean FlagImg_AvailableOrNot = false;

    @Override
    public void onClick(View v) {
        if (v == img_capturePic) {

            takePhotoFromBackCamera(Request_Code_CapturePIC);
        }

        if (v == img_icon_word_detail) {
            alertforRediretToWebB("https://vmc.gov.in/AdministrativeWardwiseMap.aspx");
        }
        if (v == im_btn_submit) {
            getTextData();
        }
        if (v == img_getCurrentLoc) {
            // method to turn on the GPS if its in off state.

            CheckGpsStatus();
            Log.e(TAG, "GpsStatus  = " + GpsStatus);

            if (!GpsStatus)
                alertToturnOffGps();
            else
                getMyCurrentLocation();

        }
        if (v == alert_no2) {
            alert_dialog2.dismiss();
        }

        if (v == alert_yes) {
            alert_dialog1.dismiss();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        if (v == alert_no) {
            alert_dialog1.dismiss();
        }
    }

    protected void alertforRediretToWebB(String url) {

        // do something when the button is clicked
        AlertDialog alertbox = new AlertDialog.Builder(NewComplaints.this)
                .setTitle(getString(R.string.redirect_webpage_title))
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(getString(R.string.redirect_webpage_msg))
                .setPositiveButton(getString(R.string.btn_ok), (arg0, arg1) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();

    }

    public void getTextData() {
        try {


//
//        byte[] img_capturePic = getBitmapAsByteArray(com_Pic_bitmap);

            String location = edt_com_location.getText().toString().trim();
            String com_desc = edt_com_desc.getText().toString().trim();
            String com_type = null;
            String type_of_loc = null;

            com_type = edt_com_location.getText().toString().trim();
            type_of_loc = edt_com_desc.getText().toString().trim();

//        String rDate_time = new SimpleDateFormat("dd-MMM-yyyy HH:mm a").format(new Date());
            String rDate_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            Log.e(TAG, "location = " + location
                    + "\n rDate_time = " + rDate_time
                    + "\n com_desc = " + com_desc
                    + "\n com_type = " + com_type
                    + "\n type_of_loc = " + type_of_loc
            );

            // For Observations 06-Aug-19 change In Citizen Complaints section mandatory field photograph to be removed.
//            if (!FlagImg_AvailableOrNot) {
//                TastyToast.makeText(getApplicationContext(), "Please Capture Image", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//            } else

            if (location.isEmpty()) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.Enter_Location), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else {
                Bitmap com_Pic_bitmap = ((BitmapDrawable) img_comp_image.getDrawable()).getBitmap();

                Complaints comp = new Complaints();

                if (methodIntent.equalsIgnoreCase("SelectComplaintsReopen")) {
                    comp.setCptdescription("Previous Comp. No. : " + selectComplaintsData.getComId() + "\n " + com_desc);
                } else {
                    comp.setCptdescription(com_desc);
                }

                comp.setCptdate(Connection.getCurrentDateTime());


                comp.setCpttype(sp_com_type.getSelectedItem().toString());
                comp.setCptloctype(sp_typeof_loc.getSelectedItem().toString());
                comp.setCptward_no(sp_com_wardNumber.getSelectedItem().toString());
                if (!FlagImg_AvailableOrNot) {
                    comp.setCptphoto(null);
                } else {
                    comp.setCptphoto(Connection.encodeBitmapToFromBase64(com_Pic_bitmap));
                }
                FlagImg_AvailableOrNot = false;

                comp.setCptuserid(session.getpsNo());
                comp.setCptpname(session.getpsName());
                comp.setUser_type(session.getuserSubType());
                comp.setCptemail(session.getEmailLogin());
                comp.setCptloc(location);
                comp.setCptmobileno(session.getMobileNumber());
                comp.setCptlatitude(MyLat + "");
                comp.setCptlongitude(MyLong + "");
                comp.setCptstatus(AppConfig.CPTSTATUS_New);
                comp.setCptactstatuts(AppConfig.Complaints_Status_Active);
                showComplaintsDetail_ConformationDialog(comp);


//                finish();
//                Intent mIntent = new Intent(getApplicationContext(), ComplaintsDetail.class); // the activity that holds the fragment
//                Bundle b = new Bundle();
//                b.putSerializable("ComplaintsData", comp);
//                mIntent.setAction("SendComplaintsData");
//                mIntent.putExtra("SelectedNameDetail", b);
//                startActivity(mIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(), "Error..", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }

    Dialog comp_confComplaintsDetail = null;
    ImageView img_comdtl_btn_submit, img_status;
    KenBurnsView img_com_photo;
    TextView tv_com_detail_psname, tv_com_detail_date;
    TextView tv_com_detail_wardNumber, tvTAG_com_detail_assignTag, tv_com_detail_driver_assign, tv_com_detail_resolved_date, tv_com_detail_type_of_loc, tv_com_detail_type, tv_com_detail_location, tv_com_detail_description, tv_com_detail_email, tv_com_detail_mobileNo;
    Toolbar toolbar2;

    public void showComplaintsDetail_ConformationDialog(Complaints complaintsData) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.activity_complaints_detail, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        comp_confComplaintsDetail = new Dialog(NewComplaints.this);
        comp_confComplaintsDetail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        comp_confComplaintsDetail.setContentView(root);
        comp_confComplaintsDetail.setCancelable(false);
        comp_confComplaintsDetail.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        comp_confComplaintsDetail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        tv_com_detail_psname = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_psname);
        tv_com_detail_date = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_date);
        tv_com_detail_location = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_location);
        tv_com_detail_wardNumber = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_wardNumber);
        tv_com_detail_description = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_description);
        tv_com_detail_mobileNo = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_mobileNo);
        tv_com_detail_email = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_email);
        tv_com_detail_type_of_loc = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_type_of_loc);
        tv_com_detail_type = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_type);
        tv_com_detail_resolved_date = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_resolved_date);
        tv_com_detail_driver_assign = (TextView) comp_confComplaintsDetail.findViewById(R.id.tv_com_detail_driver_assign);
        tvTAG_com_detail_assignTag = (TextView) comp_confComplaintsDetail.findViewById(R.id.tvTAG_com_detail_assignTag);

        img_status = (ImageView) comp_confComplaintsDetail.findViewById(R.id.img_status);
        img_com_photo = (KenBurnsView) comp_confComplaintsDetail.findViewById(R.id.img_com_photo);
        img_comdtl_btn_submit = (ImageView) comp_confComplaintsDetail.findViewById(R.id.img_comdtl_btn_submit);

        toolbar2 = (Toolbar) comp_confComplaintsDetail.findViewById(R.id.toolbar);
        toolbar2.setTitle("Complaints Detail");
        toolbar2.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        tvTAG_com_detail_assignTag.setVisibility(View.GONE);
        tv_com_detail_driver_assign.setVisibility(View.GONE);
        tv_com_detail_resolved_date.setVisibility(View.GONE);

        setSupportActionBar(toolbar2);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar2.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        String status = complaintsData.getCptstatus();

        if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_In_Process)) {
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.in_progress));
            tvTAG_com_detail_assignTag.setVisibility(View.VISIBLE);
            tv_com_detail_driver_assign.setVisibility(View.VISIBLE);
            tv_com_detail_driver_assign.setText(selectComplaintsData.getDriver_name());
        } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_Complete)) {
            tvTAG_com_detail_assignTag.setVisibility(View.VISIBLE);
            tv_com_detail_driver_assign.setVisibility(View.VISIBLE);
            tv_com_detail_resolved_date.setVisibility(View.VISIBLE);
            tv_com_detail_driver_assign.setText(selectComplaintsData.getDriver_name());
            tv_com_detail_resolved_date.setText(getString(R.string.resolved_date) + dateFormat(selectComplaintsData.getCptresdate()));
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.complete));
        } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_complaint));
        } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_Pending)) {
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pending));
        }

        tv_com_detail_psname.setText(complaintsData.getCptpname());
        tv_com_detail_email.setText(complaintsData.getCptemail());
        tv_com_detail_mobileNo.setText(complaintsData.getCptmobileno());
        tv_com_detail_date.setText(dateFormat(complaintsData.getCptdate()));
        tv_com_detail_location.setText(complaintsData.getCptloc());
        tv_com_detail_description.setText(complaintsData.getCptdescription());
        tv_com_detail_wardNumber.setText(getString(R.string.ward_no) + complaintsData.getCptward_no());
        tv_com_detail_type_of_loc.setText(complaintsData.getCptloctype());
        tv_com_detail_type.setText(complaintsData.getCpttype());

        if (complaintsData.getCptphoto() == null) {
            Glide.with(getApplicationContext()).load(R.drawable.default_image)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_com_photo);
        } else {
//            String url = session.getMyServerIP() + complaintsData.getCptphoto();
            Glide.with(getApplicationContext()).load(Connection.decodeFromBase64ToBitmap(complaintsData.getCptphoto()))
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_com_photo);
        }


        img_comdtl_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "methodIntent = " + methodIntent);

//                showComp_ConformationDialog();

                if (methodIntent.equalsIgnoreCase("SelectComplaintsForUpdate")) {
                    complaintsData.setComId(selectComplaintsData.getComId());
                    updateComplaintsRequestServer(complaintsData);
                } else {
                    sendComplaintsRequestServer(complaintsData);
                }
            }
        });

//        List<BillFormat> billFormatList
        comp_confComplaintsDetail.show();


    }


    Dialog comp_confDialo;
    ImageView img_popUp_close;
    TextView tv_com_com_popUp_date, tv_com_popUp_tag, tv_com_popUp_number;

    public void showComp_ConformationDialog(Complaints copmDetail) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_com_conformation, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        comp_confDialo = new Dialog(NewComplaints.this);
        comp_confDialo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        comp_confDialo.setContentView(root);
        comp_confDialo.setCancelable(false);
        comp_confDialo.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        comp_confDialo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        img_popUp_close = (ImageView) comp_confDialo.findViewById(R.id.img_popUp_close);
        tv_com_popUp_number = (TextView) comp_confDialo.findViewById(R.id.tv_com_popUp_number);
        tv_com_com_popUp_date = (TextView) comp_confDialo.findViewById(R.id.tv_com_com_popUp_date);
        tv_com_popUp_tag = (TextView) comp_confDialo.findViewById(R.id.tv_com_popUp_tag);

        if (methodIntent.equalsIgnoreCase("SelectComplaintsForUpdate")) {
            tv_com_popUp_tag.setText(R.string.Update_Complaint_Number);
        } else {
            tv_com_popUp_tag.setText(R.string.New_Complaint_Number);
        }

        tv_com_popUp_number.setText(copmDetail.getComId());
        tv_com_com_popUp_date.setText(dateFormat(copmDetail.getCptdate()));

        img_popUp_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comp_confDialo.dismiss();
                onBackPressed();
            }
        });

//        List<BillFormat> billFormatList
        comp_confDialo.show();


    }


    public void updateComplaintsRequestServer(Complaints tempcomplaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(NewComplaints.this);
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

                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(tempcomplaintsDetails.getComId(), "UTF-8");

                Log.e(TAG, "updateComplaintsRequestServer url = " + url);
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
                        new Response.Listener<String>() {
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
                                                comp_confComplaintsDetail.dismiss();
                                                showComp_ConformationDialog(compDetail);
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
                            jsonObject.put("cptphoto", tempcomplaintsDetails.getCptphoto());
                            jsonObject.put("cptdate", tempcomplaintsDetails.getCptdate());
                            jsonObject.put("cptloc", tempcomplaintsDetails.getCptloc());
                            jsonObject.put("cptdescription", tempcomplaintsDetails.getCptdescription());
                            jsonObject.put("cptward_no", tempcomplaintsDetails.getCptward_no());
                            jsonObject.put("cpttype", tempcomplaintsDetails.getCpttype());
                            jsonObject.put("cptloctype", tempcomplaintsDetails.getCptloctype());

                            jsonObject.put("cptlatitude", tempcomplaintsDetails.getCptlatitude());
                            jsonObject.put("cptactstatuts", tempcomplaintsDetails.getCptactstatuts());
                            jsonObject.put("cptstatus", tempcomplaintsDetails.getCptstatus());
                            jsonObject.put("cptuserid", tempcomplaintsDetails.getCptuserid());
                            jsonObject.put("user_type", tempcomplaintsDetails.getUser_type());
                            jsonObject.put("cptmobileno", tempcomplaintsDetails.getCptmobileno());
                            jsonObject.put("cptpname", tempcomplaintsDetails.getCptpname());
                            jsonObject.put("cptemail", tempcomplaintsDetails.getCptemail());

                            params.put("data", jsonObject.toString());
                            Log.e(TAG, "Update ComplaintsRequestServer jsonObject data params = " + params.toString());
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


    public void updateComplaintsRequestServer_JsonObjectRequest(Complaints tempcomplaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(NewComplaints.this);
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
                    jsonObject.put("cptphoto", tempcomplaintsDetails.getCptphoto());
                    jsonObject.put("cptdate", tempcomplaintsDetails.getCptdate());
                    jsonObject.put("cptloc", tempcomplaintsDetails.getCptloc());
                    jsonObject.put("cptdescription", tempcomplaintsDetails.getCptdescription());
                    jsonObject.put("cptward_no", tempcomplaintsDetails.getCptward_no());
                    jsonObject.put("cpttype", tempcomplaintsDetails.getCpttype());
                    jsonObject.put("cptloctype", tempcomplaintsDetails.getCptloctype());

                    jsonObject.put("cptlatitude", tempcomplaintsDetails.getCptlatitude());
                    jsonObject.put("cptactstatuts", tempcomplaintsDetails.getCptactstatuts());
                    jsonObject.put("cptstatus", tempcomplaintsDetails.getCptstatus());
                    jsonObject.put("cptuserid", tempcomplaintsDetails.getCptuserid());
                    jsonObject.put("user_type", tempcomplaintsDetails.getUser_type());
                    jsonObject.put("cptmobileno", tempcomplaintsDetails.getCptmobileno());
                    jsonObject.put("cptpname", tempcomplaintsDetails.getCptpname());
                    jsonObject.put("cptemail", tempcomplaintsDetails.getCptemail());


                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
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
                                                comp_confComplaintsDetail.dismiss();
                                                showComp_ConformationDialog(compDetail);
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


    public void sendComplaintsRequestServer_JsonObjectRequest(Complaints complaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(NewComplaints.this);
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
                    jsonObject.put("cptphoto", complaintsDetails.getCptphoto());
                    jsonObject.put("cptdate", complaintsDetails.getCptdate());
                    jsonObject.put("cptloc", complaintsDetails.getCptloc());
                    jsonObject.put("cptdescription", complaintsDetails.getCptdescription());
                    jsonObject.put("cptward_no", complaintsDetails.getCptward_no());
                    jsonObject.put("cpttype", complaintsDetails.getCpttype());
                    jsonObject.put("cptloctype", complaintsDetails.getCptloctype());

                    jsonObject.put("cptlatitude", complaintsDetails.getCptlatitude());
                    jsonObject.put("cptlongitude", complaintsDetails.getCptlongitude());
                    jsonObject.put("cptstatus", complaintsDetails.getCptstatus());
                    jsonObject.put("cptactstatuts", complaintsDetails.getCptactstatuts());
                    jsonObject.put("cptuserid", complaintsDetails.getCptuserid());
                    jsonObject.put("cptmobileno", complaintsDetails.getCptmobileno());
                    jsonObject.put("user_type", complaintsDetails.getUser_type());
                    jsonObject.put("cptpname", complaintsDetails.getCptpname());
                    jsonObject.put("cptemail", complaintsDetails.getCptemail());


                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;
                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");

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

                                                Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);

                                                Log.e(TAG, "Responce getComId = " + compDetail.getComId());

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                comp_confComplaintsDetail.dismiss();
                                                showComp_ConformationDialog(compDetail);
//                                        TastyToast.makeText(getApplicationContext(), "Thank you for Feedback", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

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

    public void sendComplaintsRequestServer(Complaints complaintsDetails) {
        try {
            mProgressDialog = new ProgressDialog(NewComplaints.this);
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
                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                Log.e(TAG, "sendComplaintsRequestServer url = " + url);
//                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
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

                                                Log.e(TAG, "Responce getComId = " + compDetail.getComId());

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                comp_confComplaintsDetail.dismiss();
                                                showComp_ConformationDialog(compDetail);
//                                        TastyToast.makeText(getApplicationContext(), "Thank you for Feedback", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

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
                        Log.e(TAG, " Error in response  " + getString(R.string.network_error));
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject = new JSONObject();
                            jsonObject.put("cptphoto", complaintsDetails.getCptphoto());
                            jsonObject.put("cptdate", complaintsDetails.getCptdate());
                            jsonObject.put("cptloc", complaintsDetails.getCptloc());
                            jsonObject.put("cptdescription", complaintsDetails.getCptdescription());
                            jsonObject.put("cptward_no", complaintsDetails.getCptward_no());
                            jsonObject.put("cpttype", complaintsDetails.getCpttype());
                            jsonObject.put("cptloctype", complaintsDetails.getCptloctype());

                            jsonObject.put("cptlatitude", complaintsDetails.getCptlatitude());
                            jsonObject.put("cptlongitude", complaintsDetails.getCptlongitude());
                            if (methodIntent.equalsIgnoreCase("DriveNewComplaint")) {
                                jsonObject.put("cptstatus", AppConfig.CPTSTATUS_Noted);
                            } else {
                                jsonObject.put("cptstatus", complaintsDetails.getCptstatus());
                            }

                            jsonObject.put("cptactstatuts", complaintsDetails.getCptactstatuts());
                            jsonObject.put("cptuserid", complaintsDetails.getCptuserid());
                            jsonObject.put("cptmobileno", complaintsDetails.getCptmobileno());
                            jsonObject.put("user_type", complaintsDetails.getUser_type());
                            jsonObject.put("cptpname", complaintsDetails.getCptpname());
                            jsonObject.put("cptemail", complaintsDetails.getCptemail());


                            params.put("data", jsonObject.toString());

                            Log.e(TAG, "sendComplaintsRequestServer jsonObject data params = " + params.toString());
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


    public void setCOMTYPE_LOCType_Default() {


        ArrayAdapter<CharSequence> adapter_ComType = ArrayAdapter.createFromResource(NewComplaints.this, R.array.Complaint_type, android.R.layout.simple_spinner_item);
        adapter_ComType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_com_type.setAdapter(adapter_ComType);
//            if (!unit.equals(null)) {
//                int spinnerPosition = adapter_ComType.getPosition("gm");
//                sp_Unit.setSelection(spinnerPosition);
//            }


        ArrayAdapter<CharSequence> adapter_LocType = ArrayAdapter.createFromResource(NewComplaints.this, R.array.location_type, android.R.layout.simple_spinner_item);
        adapter_LocType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_typeof_loc.setAdapter(adapter_LocType);
//            if (!unit.equals(null)) {
//                int spinnerPosition = adapter_rank.getPosition(unit);
//                sp_Unit.setSelection(spinnerPosition);
//            }

        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(NewComplaints.this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_com_wardNumber.setAdapter(adapter_wardNumber);

    }

    public void setComType(String comType) {


        ArrayAdapter<CharSequence> adapter_ComType = ArrayAdapter.createFromResource(NewComplaints.this, R.array.Complaint_type, android.R.layout.simple_spinner_item);
        adapter_ComType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_com_type.setAdapter(adapter_ComType);
        if (!comType.equals(null)) {
            int spinnerPosition = adapter_ComType.getPosition(comType);
            sp_com_type.setSelection(spinnerPosition);
        }

    }

    public void setLocType(String locType) {


        ArrayAdapter<CharSequence> adapter_LocType = ArrayAdapter.createFromResource(NewComplaints.this, R.array.location_type, android.R.layout.simple_spinner_item);
        adapter_LocType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_typeof_loc.setAdapter(adapter_LocType);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_LocType.getPosition(locType);
            sp_typeof_loc.setSelection(spinnerPosition);
        }


    }

    public void setWardNumber(String locType) {
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(NewComplaints.this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_com_wardNumber.setAdapter(adapter_wardNo);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            sp_com_wardNumber.setSelection(spinnerPosition);
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {

            return;
        }

        if (requestCode == Request_Code_CapturePIC) {
            imageUri = null;
            targetPath = null;

            Bitmap VISIPIC_bitmao = (Bitmap) data.getExtras().get("data");

//                int orientation = Exif.getOrientation(getBitmapAsByteArray(VISIPIC_bitmao));
//                img_capturePic.setImageBitmap(rotateImage(VISIPIC_bitmao, orientation));
            img_comp_image.setImageBitmap(VISIPIC_bitmao);
//                imageUri = getImageUri(getApplicationContext(), VISIPIC_bitmao);
//                targetPath = getRealPathFromURI(imageUri);
//            Log.e(TAG, "imageUri = " + imageUri);
//            Log.e(TAG, "targetPath = " + targetPath);
//                uploadPhotoOnServer();
            FlagImg_AvailableOrNot = true;

        }
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


    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    /**
     * Method to turn on GPS
     **/
    public void alertToturnOffGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.location_services_not_active));
        builder.setMessage(getString(R.string.please_enable_location));
        builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public String dateFormat(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "dd-MMM-yyyy hh:mm:ss a";

        String formatedDate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date myDate = null;
        try {
            myDate = dateFormat.parse(mStringDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(newFormat);
        formatedDate = timeFormat.format(myDate);

        return formatedDate;
    }

    Dialog alert_dialog1, alert_dialog2;
    Button alert_yes, alert_no, alert_no2;
    TextView alert_msg, alert_msg2;


    public void show_alert_two_button(String msg) {


        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog1 = new Dialog(this);
        alert_dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog1.setContentView(root);
        alert_dialog1.setCancelable(false);
        alert_dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_yes = (Button) alert_dialog1.findViewById(R.id.alert_close);
        alert_msg = (TextView) alert_dialog1.findViewById(R.id.alert_msg);
        alert_no = (Button) alert_dialog1.findViewById(R.id.alert_no);
        alert_yes.setText("Yes");
        alert_msg.setText(msg);
        alert_no.setVisibility(View.VISIBLE);


        alert_yes.setOnClickListener(this);
        alert_no.setOnClickListener(this);


        alert_dialog1.show();
    }

    public void show_alert_Dialog_singlebutton(String msg) {

        int counter = 0;
        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog2 = new Dialog(this);
        alert_dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog2.setContentView(root);
        alert_dialog2.setCancelable(false);
        alert_dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_no2 = (Button) alert_dialog2.findViewById(R.id.alert_close);
        alert_msg2 = (TextView) alert_dialog2.findViewById(R.id.alert_msg);

        alert_msg2.setText(msg);


        alert_no2.setOnClickListener(this);


        alert_dialog2.show();
    }

    LocationManager locationManager;
    boolean GpsStatus;

    public void CheckGpsStatus() {

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * Check the type of GPS Provider available at that instance and
     * collect the location informations
     *
     * @Output Latitude and Longitude
     */
    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(NewComplaints.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.Finding_Location));
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locListener = new MyLocationListener();

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
            locManager.removeUpdates(locListener); // removes the periodic updates from location listener to //avoid battery drainage. If you want to get location at the periodic intervals call this method using //pending intent.

            try {
// Getting address from found locations.
                Geocoder geocoder;

                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocation(MyLat, MyLong, 1);

                /////

                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }


                StateName = addresses.get(0).getAdminArea();

                CityName = addresses.get(0).getLocality();
                CountryName = addresses.get(0).getCountryName();
                //
                Log.e(TAG, " addressFragments.get(0) " + addressFragments.toString());
                Log.e(TAG, " getFeatureName.get(0) " + addresses.get(0).getAddressLine(0));
                edt_com_location.setText(addresses.get(0).getAddressLine(0));
//            Log.e(TAG," getFeatureName.get(0) " + addresses.get(0).getFeatureName());
//            Log.e(TAG," getThoroughfare.get(0) " + addresses.get(0).getThoroughfare());
//            Log.e(TAG," getPostalCode.get(0) " + addresses.get(0).getPostalCode());

                // you can get more details other than this . like country code, state code, etc.
                Log.e(TAG, "" + MyLat);
                Log.e(TAG, "" + MyLong);
                Log.e(TAG, " StateName " + StateName + " CityName " + CityName + " CountryName " + CountryName);
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, 2000);

    }

    // Location listener class. to get location.
    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (location != null) {
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

    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    Location location;

    Double MyLat, MyLong;
    String CityName = "";
    String StateName = "";
    String CountryName = "";

// below method to get the last remembered location. because we don't get locations all the times .At some instances we are unable to get the location from GPS. so at that moment it will show us the last stored location.

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
        if (comp_confComplaintsDetail != null)
            comp_confComplaintsDetail.dismiss();

        Log.e(TAG, "onDestroy => " + TAG);
    }

    public void onBackPressed() {
        super.onBackPressed();

        if (methodIntent != null && methodIntent.equals("DriveNewComplaint")) {
            Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("pendingRouteDetails", routeDetails);
            b.putSerializable("vehicleDetails", vehicleDetails);
            mIntent.setAction("pendingRoute");
            mIntent.putExtra("SelectedVehicleDetail", b);
            startActivity(mIntent);
            finish();
        } else {
            finish();
        }

//        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
    }

}
