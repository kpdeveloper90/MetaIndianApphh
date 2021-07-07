package com.ecosense.app.activity.citizen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.supervisor.AssignComplaints;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;

public class ComplaintsDetail extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = ComplaintsDetail.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


    @BindView(R.id.img_comdtl_btn_submit)
    ImageView img_comdtl_btn_submit;

    @BindView(R.id.img_com_photo)
    KenBurnsView img_com_photo;

    @BindView(R.id.tv_com_detail_psname)
    TextView tv_com_detail_psname;

    @BindView(R.id.tv_com_detail_date)
    TextView tv_com_detail_date;

    @BindView(R.id.tv_com_detail_mobileNo)
    TextView tv_com_detail_mobileNo;

    @BindView(R.id.tv_com_detail_email)
    TextView tv_com_detail_email;

    @BindView(R.id.tv_com_detail_location)
    TextView tv_com_detail_location;

    @BindView(R.id.tv_com_detail_description)
    TextView tv_com_detail_description;

    @BindView(R.id.tv_com_detail_type_of_loc)
    TextView tv_com_detail_type_of_loc;

    @BindView(R.id.tv_com_detail_wardNumber)
    TextView tv_com_detail_wardNumber;

    @BindView(R.id.tv_com_detail_type)
    TextView tv_com_detail_type;

    @BindView(R.id.tvTAG_com_detail_assignTag)
    TextView tvTAG_com_detail_assignTag;
    @BindView(R.id.tv_com_detail_driver_assign)
    TextView tv_com_detail_driver_assign;

    @BindView(R.id.tv_com_detail_resolved_date)
    TextView tv_com_detail_resolved_date;

    @BindView(R.id.img_status)
    ImageView img_status;

    @BindView(R.id.btn_assign_complain)
    MaterialButton btn_assign_complain;


    Boolean isConnected = false;
    ProgressDialog mProgressDialog;
    UserSessionManger session = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* Set the app into full screen mode */
        getWindow().getDecorView().setSystemUiVisibility(flags);
        setContentView(R.layout.activity_complaints_detail);


        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Complaints Detail");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        session = new UserSessionManger(this);
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(),  getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        img_comdtl_btn_submit.setOnClickListener(this);
        btn_assign_complain.setOnClickListener(this);


        onNewIntent(getIntent());
    }

    String methodIntent = null;
    Complaints selectComplaintsData = null;

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

            if (methodIntent != null && methodIntent.equals("SelectComplaints")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                setIntentData();
            } else if (methodIntent != null && methodIntent.equals("AssignCitizenComplaints")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                if (selectComplaintsData.getCptstatus().equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
                    btn_assign_complain.setVisibility(View.VISIBLE);
                }
                setIntentData();
            } else if (methodIntent != null && methodIntent.equals("AssignDriverComplaints")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");

//                if (selectComplaintsData.getCptstatus().equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
//                    btn_assign_complain.setVisibility(View.VISIBLE);
//                }

                setIntentData();
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    private void setIntentData() {

        img_comdtl_btn_submit.setVisibility(View.GONE);

        tv_com_detail_psname.setText(selectComplaintsData.getCptpname());
        tv_com_detail_email.setText(selectComplaintsData.getCptemail());
        tv_com_detail_date.setText(dateFormat(selectComplaintsData.getCptdate()));
        tv_com_detail_location.setText(selectComplaintsData.getCptloc());
        tv_com_detail_description.setText(selectComplaintsData.getCptdescription());
        tv_com_detail_type_of_loc.setText(selectComplaintsData.getCptloctype());
        tv_com_detail_type.setText(selectComplaintsData.getCpttype());
        tv_com_detail_mobileNo.setText(selectComplaintsData.getCptmobileno());
        tv_com_detail_wardNumber.setText(getString(R.string.ward_no) +selectComplaintsData.getCptward_no());
        tvTAG_com_detail_assignTag.setVisibility(View.GONE);
        tv_com_detail_driver_assign.setVisibility(View.GONE);
        tv_com_detail_resolved_date.setVisibility(View.GONE);
        String status = selectComplaintsData.getCptstatus();

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
            tv_com_detail_resolved_date.setText(getString(R.string.resolved_date)+dateFormat(selectComplaintsData.getCptresdate()));
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.complete));
        } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_complaint));
        } else if (status.equalsIgnoreCase(AppConfig.CPTSTATUS_Pending)) {
            img_status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pending));
        }
        if (selectComplaintsData.getCptphoto() == null) {
            Glide.with(getApplicationContext()).load(R.drawable.default_image)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_com_photo);
        } else {
//            String url = session.getMyServerIP() + complaintsData.getCptphoto();
            Glide.with(getApplicationContext()).load(Connection.decodeFromBase64ToBitmap(selectComplaintsData.getCptphoto()))
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_com_photo);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == img_comdtl_btn_submit) {
            showComp_ConformationDialog();
        }
        if (v == btn_assign_complain) {
            finish();
            Intent mIntent = new Intent(getApplicationContext(), AssignComplaints.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("ComplaintsData", selectComplaintsData);
            mIntent.setAction("AssignCitizenComplaints");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
    }

    Dialog comp_confDialo;
    ImageView img_popUp_close;

    public void showComp_ConformationDialog() {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_com_conformation, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        comp_confDialo = new Dialog(ComplaintsDetail.this);
        comp_confDialo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        comp_confDialo.setContentView(root);
        comp_confDialo.setCancelable(false);
        comp_confDialo.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        comp_confDialo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        img_popUp_close = (ImageView) comp_confDialo.findViewById(R.id.img_popUp_close);


        img_popUp_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comp_confDialo.dismiss();
            }
        });

//        List<BillFormat> billFormatList
        comp_confDialo.show();


    }


    public void generateComplaintsRequestServer(Complaints complaintsDetails) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {

            try {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Please Wait...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {
                String data = null;
                data = "{}";
                URLEncoder.encode(data, "UTF-8");

//                String url = "http://203.109.125.77:7070/api/resource/Visitor_Management?filters=" + URLEncoder.encode(filters, "UTF-8")
                String url = session.getMyServerIP() + "/api/resource/Complaints";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            // response
                            if (!response.isEmpty()) {
                                Log.e("Response", response);
                                try {
                                    //create ObjectMapper instance
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    JsonNode rootNode = objectMapper.readTree(response.toString());
                                    JsonNode statusData = rootNode.path("data");
                                    Log.e(TAG, "statusData = " + statusData.toString());
                                    if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                        Complaints visitorDetails = objectMapper.readValue(statusData.toString(), Complaints.class);
//
//                                        Log.e(TAG, "Registration Successfully = " +
//                                                "AppNo =" + visitorDetails.getName() +
//                                                "exterNalID =" + visitorDetails.getExternal_id()
//                                        );


                                    } else {
                                        show_alert_Dialog_singlebutton("Failed to registered");
                                    }
                                } catch (IOException e) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;

                                    }
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "Response Error");
                            }

                        }, error -> {
                    // error

                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    show_alert_Dialog_singlebutton("Error!");
                    Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
//                        Gson gson = new Gson();
//                        String regpojo = gson.toJson(complaintsDetails);
//                        Log.e(TAG, "getParams   = " + regpojo);

//                        params.put("data", regpojo);
                        return params;
                    }

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
        }

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

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        if (isConnected) {
            message =getString(R.string.back_online);
        } else {
            message =getString(R.string.you_are_offline);
        }
        TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
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
}