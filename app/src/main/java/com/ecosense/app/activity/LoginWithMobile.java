package com.ecosense.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.chaos.view.PinView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sdsmdg.tastytoast.TastyToast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.CitizenDashBoard;
import com.ecosense.app.activity.driver.DriverDashBoard;
import com.ecosense.app.activity.metaData.MetaDataDashBoard;
import com.ecosense.app.activity.supervisor.SupervisorHome;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.LoginDetail;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;


public class LoginWithMobile extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = LoginWithMobile.class.getSimpleName();
    public static String SERVER_URL = null;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @BindView(R.id.tv_tab_individual)
    TextView tv_tab_individual;

    @BindView(R.id.tv_tab_corporate)
    TextView tv_tab_corporate;

    @BindView(R.id.sw_team_login)
    Switch sw_team_login;

    @BindView(R.id.btn_sendOTP)
    Button btn_sendOTP;

    @BindView(R.id.input_mno)
    TextInputEditText input_mno;

    @BindView(R.id.linearLayout2)
    LinearLayout ll_citizan_tab;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    static boolean LOGIN_TAB_SELECTION = true;

    @BindView(R.id.input_email)
    TextInputEditText input_email;
    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    Location location;

    Double MyLat = 0.0, MyLong = 0.0;
    String CityName = "";
    String StateName = "";
    String CountryName = "";
    static String UserAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
//        /* Set the app into full screen mode */
//        getWindow().getDecorView().setSystemUiVisibility(flags);
        Log.e(TAG, "onCreate : " + TAG);

        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());

        setContentView(R.layout.activity_login_with_mobile);

        ButterKnife.bind(this);
        input_email.setVisibility(View.GONE);

        checkPermission();


        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, getString(R.string.you_are_offline));
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
//
//        CookieManager manager = new CookieManager();
//        CookieHandler.setDefault(manager);


//        CheckGpsStatus();
//        Log.e(TAG, "GpsStatus  = " + GpsStatus);

//        if (!GpsStatus)
//            alertToturnOffGps();
//        else {
//            getMyCurrentLocation();
//        }
        sw_team_login.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ll_citizan_tab.setVisibility(View.GONE);
                if (input_email.getVisibility() == View.VISIBLE) {
                    input_email.setVisibility(View.GONE);
                }
            } else {
                ll_citizan_tab.setVisibility(View.VISIBLE);
                tv_tab_individual.setBackground(getDrawable(R.drawable.login_tab_selecter));
                tv_tab_corporate.setBackground(getDrawable(R.drawable.login_tab_bg));
                input_email.setVisibility(View.GONE);
                input_email.setText("");
                input_mno.setText("");
                LOGIN_TAB_SELECTION = true;
            }
        });

        tv_tab_individual.setOnClickListener(this);
        tv_tab_corporate.setOnClickListener(this);
        input_email.setOnClickListener(this);
        btn_sendOTP.setOnClickListener(this);
        input_mno.setOnClickListener(this);

        Log.e(TAG, "getMyServerIP = " + session.getMyServerIP());
    }

    public void onClick(View v) {
        if (v == tv_tab_individual) {
            tv_tab_individual.setBackground(getDrawable(R.drawable.login_tab_selecter));
            tv_tab_corporate.setBackground(getDrawable(R.drawable.login_tab_bg));
            input_email.setVisibility(View.GONE);
            input_email.setText("");
            input_mno.setText("");
            LOGIN_TAB_SELECTION = true;
        }
        if (v == tv_tab_corporate) {
            tv_tab_individual.setBackground(getDrawable(R.drawable.login_tab_bg));
            tv_tab_corporate.setBackground(getDrawable(R.drawable.login_tab_selecter));
            input_email.setVisibility(View.VISIBLE);
            input_email.setText("");
            input_mno.setText("");
            LOGIN_TAB_SELECTION = false;
        }
        if (v == btn_sendOTP) {

            login();
        }
        if (v == input_email) {
            input_email.setFocusable(true);
            input_email.setFocusableInTouchMode(true);

            showSoftKeyboard(v);
        }
        if (v == input_mno) {
            input_mno.setFocusable(true);
            input_mno.setFocusableInTouchMode(true);

            showSoftKeyboard(v);
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

        if (v == tv_btn_edit_MobileNO) {
            dialog_OTP_Verification.dismiss();
        }
        if (v == tv_btn_resend) {
            dialog_OTP_Verification.dismiss();
        }
    }


    public void login() {
        Log.d(TAG, "Login");
        try {


            if (!validate()) {
                return;
            }
            String mno_login = null;
            String email_login = null;

            LoginDetail loginDetail = new LoginDetail();
            String rDate_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            mno_login = Objects.requireNonNull(input_mno.getText()).toString().trim();
            if (sw_team_login.isChecked()) {

                loginDetail.setRegusertype(AppConfig.UType_Team);
                loginDetail.setRegteam(AppConfig.Team_Login_Yes);
                loginDetail.setRegmobile(mno_login);
                Log.e(TAG, "team_login = " + AppConfig.Team_Login_Yes);
            } else {
                Log.e(TAG, "team_login = " + AppConfig.Team_Login_No);
                if (LOGIN_TAB_SELECTION) {
                    loginDetail.setRegmobile(mno_login);
                    loginDetail.setRegusertype(AppConfig.UType_Citizen);
                    loginDetail.setRegteam(AppConfig.Team_Login_No);
                    loginDetail.setRegsubusertype(AppConfig.USubType_Individual);
                } else {
                    email_login = Objects.requireNonNull(input_email.getText()).toString().trim();
                    loginDetail.setRegmobile(mno_login);
                    loginDetail.setRegemail(email_login);
                    loginDetail.setRegteam(AppConfig.Team_Login_No);
                    loginDetail.setRegusertype(AppConfig.UType_Citizen);
                    loginDetail.setRegsubusertype(AppConfig.USubType_Corporate);
                }
            }

            loginDetail.setRegfname("Guest");
            loginDetail.setReglocation(UserAddress);
            loginDetail.setReglatitude(MyLat.toString());
            loginDetail.setReglongitude(MyLong.toString());
            loginDetail.setRegdate(rDate_time);

            Log.e(TAG, "email_login = " + email_login + " \t mno_login" + mno_login);

            final ProgressDialog progressDialog = new ProgressDialog(LoginWithMobile.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Sending OTP...");
            progressDialog.show();


            // TODO: Implement your own authentication logic here.

            new android.os.Handler().postDelayed(() -> {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                show_dialog_OTP_Verification(loginDetail);

            }, 2000);

//        FindLoginDetailFromServer(email, password);
            // request for sms
//        progressBar.setVisibility(View.VISIBLE);
//        requestForSMS(email_login,mno_login);
//        session.createUserLoginSession("0011", email, "Admin");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = input_email.getText().toString();
        String mno = input_mno.getText().toString();


//        if (mno.isEmpty() || mno.length() < 10 || mno.length() > 10) {
        if (mno.isEmpty() || !isValidPhoneNumber(mno)) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.error_mno_10digit), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            input_mno.setText("");
            valid = false;
        } else if (input_email.getVisibility() == View.VISIBLE) {
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                TastyToast.makeText(getApplicationContext(), getString(R.string.error_email_id), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                input_email.setText("");
                valid = false;
            } else {

            }
        } else {

        }
        return valid;
    }

    private void sendOTP(String country_Code, String mno_login) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(this, getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {

                String url = Connection.API_OTP_SEND + AppConfig.API_SEND_OTP +
                        "?authkey=" + getString(R.string.authKey) +
                        "&template_id=" + getString(R.string.template_id) +
                        "&mobile=" + country_Code + mno_login +
                        "&invisible=" + getString(R.string.invisible) +
                        "&otp_length=" + getString(R.string.otp_length) +
                        "&otp_expiry=" + getString(R.string.otp_expiry);
//                Log.e(TAG, url);
                StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            Log.e("Response", response);
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                LoginDetail loginDetail = objectMapper.readValue(rootNode.toString(), LoginDetail.class);

                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }
                                if (loginDetail.getOtp_type().equalsIgnoreCase("success")) {
                                    Toast.makeText(this, getString(R.string.otp_success), Toast.LENGTH_SHORT).show();
//                                    CALL THIS METHOD FOR OTP VERIFICATION REFAT KOOGROO PROJECT
//                                    show_dialog_OTP_Verification(country_Code, mno_login);

                                } else {
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, error -> {

                    // error
                    Log.e(TAG + " Error.Response", error.toString());
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    finish();
                });
                // Tag used to cancel the request
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void reSendOTP(String country_Code, String mno_login) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(this, getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {

//                String url = Connection.API_OTP_SEND + AppConfig.API_RESEND_OTP +
//                        "?authkey=" + getString(R.string.authKey) +
//                        "&mobile=" + country_Code + mno_login +
//                        "&retrytype=" + getString(R.string.retrytype);
                String url = Connection.API_OTP_SEND + AppConfig.API_SEND_OTP +
                        "?authkey=" + getString(R.string.authKey) +
                        "&template_id=" + getString(R.string.template_id) +
                        "&mobile=" + country_Code + mno_login +
                        "&invisible=" + getString(R.string.invisible) +
                        "&otp_length=" + getString(R.string.otp_length) +
                        "&otp_expiry=" + getString(R.string.otp_expiry);
                Log.e(TAG, url);
                StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            Log.e("Response", response);
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                LoginDetail loginDetail = objectMapper.readValue(rootNode.toString(), LoginDetail.class);

                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }
                                if (loginDetail.getOtp_type().equalsIgnoreCase("success")) {
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.otp_resend_success), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                    tv_btn_resend.setVisibility(View.GONE);
//                                    startTimer(callculateTotaltime("00:05:00"));
                                } else {
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, error -> {

                    // error
                    Log.e(TAG + " Error.Response", error.toString());
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    finish();
                });
                // Tag used to cancel the request
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void verificationOTP(String country_Code, String mno_login, String otp) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(this, getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {
                String url = Connection.API_OTP_SEND + AppConfig.API_VERIFY_OTP +
                        "?authkey=" + getString(R.string.authKey) +
                        "&mobile=" + country_Code + mno_login +
                        "&otp=" + otp;
//                Log.e(TAG, url);
                StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            Log.e("Response", response);
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                LoginDetail loginDetail = objectMapper.readValue(rootNode.toString(), LoginDetail.class);

                                if (loginDetail.getOtp_type().equalsIgnoreCase("success")) {
//                                    GENERATE SESSION FOR USING ONLY MOBILE NO
//                                    session.createUserLoginSessionUsingMobile(mno_login);
                                    TastyToast.makeText(getApplicationContext(), loginDetail.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    //UNCOMMENT BELOW LINE
//                               if (loginDetail.getRegteam().equalsIgnoreCase(AppConfig.Team_Login_Yes)) {
//                            FindEmployeeLoginMobileNoOnServer(loginDetail);
//                        } else {
//                            FindLoginMobileNoOnServer(loginDetail);
//                        }
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    TastyToast.makeText(getApplicationContext(), loginDetail.getMessage(), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, error -> {

                    // error
                    Log.e(TAG + " Error.Response", error.toString());
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    finish();
                });
                // Tag used to cancel the request
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void FindLoginMobileNoOnServer(LoginDetail loginDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(LoginWithMobile.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;


                    filters = "[[\"Registration\",\"regmobile\",\"=\",\""
                            + loginDetail.getRegmobile()
                            + "\"],[\"Registration\", \"regusertype\", \"=\",\""
                            + loginDetail.getRegusertype()
                            + "\"],[\"Registration\", \"regsubusertype\", \"=\",\""
                            + loginDetail.getRegsubusertype()
                            + "\"],[\"Registration\", \"regteam\", \"=\",\""
                            + loginDetail.getRegteam()
                            + "\"]]";


                    fields = "[\"*\"]";
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/Registration?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + URLEncoder.encode(fields, "UTF-8");
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.e(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                LoginDetail loginDet = objectMapper.readValue(visitor.toString(), LoginDetail.class);

                                                if (!loginDet.toString().trim().equalsIgnoreCase("{}")) {
                                                    Log.e(TAG, "LoginDetail getRegmobile = " + loginDet.getRegmobile());

                                                    if (loginDetail.getRegmobile().equalsIgnoreCase(loginDet.getRegmobile())) {
                                                        Log.e(TAG, "LoginDetail getUserId = " + loginDet.getUserId());
                                                        session.createUserLoginSession(
                                                                loginDet.getUserId(),
                                                                loginDet.getRegfname(),
                                                                loginDet.getRegmobile(),
                                                                loginDet.getRegusertype()
                                                        );
                                                        String subType = loginDet.getRegsubusertype();
                                                        session.setuserSubType(subType);

                                                        Log.e(TAG, "LoginDetail subType = " + subType);
                                                        if (loginDet.getRegemail() != null) {
                                                            session.setEmailLogin(loginDet.getRegemail());
                                                        }
                                                        session.setuserWardNo(loginDet.getReg_wardno());
                                                        dialog_OTP_Verification.dismiss();

//                                                        if (subType.equalsIgnoreCase(AppConfig.USubType_Individual) || subType.equalsIgnoreCase(AppConfig.USubType_Corporate)) {
                                                        //for team user used reg_teamphoto and for citizen reg_photo
                                                        if (loginDet.getReg_photo() != null) {
                                                            session.setUserProfilePic(loginDet.getReg_photo());
                                                        } else {
                                                            session.setUserProfilePic(null);
                                                        }

                                                        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                                        finish();
//                                                        }

                                                       /* else if (subType.equalsIgnoreCase(AppConfig.USubType_Driver)) {
                                                            //for team user used reg_teamphoto and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getReg_teamphoto());

                                                            startActivity(new Intent(getApplicationContext(), DriverDashBoard.class));
                                                            finish();
                                                        } else if (subType.equalsIgnoreCase(AppConfig.USubType_Data_Collector)) {
                                                            //for team user used reg_teamphoto and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getReg_teamphoto());
                                                            startActivity(new Intent(getApplicationContext(), MetaDataDashBoard.class));
                                                            finish();
                                                        } else if (subType.equalsIgnoreCase(AppConfig.USubType_Supervisor)) {
                                                            //for team user used reg_teamphoto and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getReg_teamphoto());
                                                            startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
                                                            finish();
                                                        }*/
                                                    } else {
                                                        //for comment due to demo
//                                                        if (loginDetail.getRegusertype().equalsIgnoreCase(AppConfig.UType_Citizen)) {
//                                                            registrationNewUserOnServer(loginDetail);
//                                                        } else {
                                                        dialog_OTP_Verification.dismiss();
                                                        TastyToast.makeText(getApplicationContext(), "User is not Registered please contact Admin", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//                                                        }
                                                    }
                                                }
                                                break;
                                            }

                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            Log.e(TAG, "No record found");
                                            //for comment due to demo
//                                            registrationNewUserOnServer(loginDetail);
                                            dialog_OTP_Verification.dismiss();
                                            TastyToast.makeText(getApplicationContext(), "User is not Registered please contact Admin", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                                    dialog_OTP_Verification.dismiss();
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    public void FindEmployeeLoginMobileNoOnServer(LoginDetail loginDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(LoginWithMobile.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;


                    filters = "[[\"Employee\",\"regmobile\",\"=\",\""
                            + loginDetail.getRegmobile()
                            + "\"],[\"Employee\", \"status\", \"=\",\""
                            + AppConfig.Active_Status
                            + "\"]]";

                    fields = "[\"*\"]";
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/Employee?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + URLEncoder.encode(fields, "UTF-8");
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.e(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                LoginDetail loginDet = objectMapper.readValue(visitor.toString(), LoginDetail.class);

                                                if (!loginDet.toString().trim().equalsIgnoreCase("{}")) {
                                                    Log.e(TAG, "LoginDetail getRegmobile = " + loginDet.getRegmobile());

                                                    if (loginDetail.getRegmobile().equalsIgnoreCase(loginDet.getRegmobile())) {
                                                        Log.e(TAG, "LoginDetail getUserId = " + loginDet.getUserId());
                                                        session.createUserLoginSession(
                                                                loginDet.getUserId(),
                                                                loginDet.getEmployee_name(),
                                                                loginDet.getRegmobile(),
                                                                AppConfig.UType_Team
                                                        );
                                                        String subType = loginDet.getDesignation();
                                                        session.setuserSubType(subType);
                                                        session.setuserCost_Center(loginDet.getCost_center());
                                                        Log.e(TAG, "LoginDetail subType = " + subType);
                                                        if (loginDet.getPersonal_email() != null) {
                                                            session.setEmailLogin(loginDet.getPersonal_email());
                                                        }
                                                        session.setuserWardNo(loginDet.getReg_wardno());
                                                        dialog_OTP_Verification.dismiss();
                                                    if(subType!=null) {
                                                        if (subType.equalsIgnoreCase(AppConfig.USubType_Driver)) {
                                                            //for team user used image and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getImage());

                                                            startActivity(new Intent(getApplicationContext(), DriverDashBoard.class));
                                                            finish();
                                                        } else if (subType.equalsIgnoreCase(AppConfig.USubType_Data_Collector)) {
                                                            //for team user used image and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getImage());
                                                            startActivity(new Intent(getApplicationContext(), MetaDataDashBoard.class));
                                                            finish();
                                                        } else if (subType.equalsIgnoreCase(AppConfig.USubType_Supervisor)) {
                                                            //for team user used image and for citizen reg_photo
                                                            session.setUserProfilePic(loginDet.getImage());
                                                            startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
                                                            finish();
                                                        }
                                                    }

                                                    } else {
                                                        //for comment due to demo
//                                                        if (loginDetail.getRegusertype().equalsIgnoreCase(AppConfig.UType_Citizen)) {
//                                                            registrationNewUserOnServer(loginDetail);
//                                                        } else {
                                                        dialog_OTP_Verification.dismiss();
                                                        TastyToast.makeText(getApplicationContext(), getString(R.string.user_not_registered_please_contact_admin), TastyToast.LENGTH_SHORT, TastyToast.ERROR);

//                                                        }
                                                    }
                                                }
                                                break;
                                            }

                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            Log.e(TAG, "No record found");
                                            //for comment due to demo
                                            dialog_OTP_Verification.dismiss();
//                                            registrationNewUserOnServer(loginDetail);
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.user_not_registered_please_contact_admin), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                                    dialog_OTP_Verification.dismiss();
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        dialog_OTP_Verification.dismiss();
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    public void registrationNewUserOnServer(LoginDetail loginDetail) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {

            try {
                show_dialog_progress(getString(R.string.please_wait));
            } catch (Exception e) {
            }
            try {
                String data = null;
                data = "{}";
                URLEncoder.encode(data, "UTF-8");

//                String url = session.getMyServerIP() + "/api/resource/UserActivity/" + URLEncoder.encode(compFeedback.getComId(), "UTF-8");
                String url = session.getMyServerIP() + "/api/resource/Registration";
                Log.e(TAG, "url = > " + url);
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
//                                    Log.e(TAG, "statusData = " + statusData.toString());
                                    if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                        LoginDetail loginDet = objectMapper.readValue(statusData.toString(), LoginDetail.class);

                                        Log.e(TAG, "getUserId  = " + loginDet.getUserId());

                                        if (dialog_progress != null) {
                                            dialog_progress.dismiss();
                                            dialog_progress = null;
                                        }
                                        session.createUserLoginSession(
                                                loginDet.getUserId(),
                                                loginDet.getRegfname(),
                                                loginDet.getRegmobile(),
                                                loginDet.getRegusertype()
                                        );
                                        if (loginDet.getRegemail() != null) {
                                            session.setEmailLogin(loginDet.getRegemail());
                                        }
                                        session.setuserSubType(loginDet.getRegsubusertype());
                                        //for team user used reg_teamphoto and for citizen reg_photo
                                        session.setUserProfilePic(loginDet.getReg_photo());


                                        dialog_OTP_Verification.dismiss();
                                        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                        finish();
                                    } else {
                                        show_alert_Dialog_singlebutton(getString(R.string.failed_to_registration_error));
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
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;

                                }
                                show_alert_Dialog_singlebutton(getString(R.string.response_error));
                            }

                        }, error -> {
                    // error

                    if (dialog_progress != null) {
                        dialog_progress.dismiss();
                        dialog_progress = null;
                    }

                    show_alert_Dialog_singlebutton(getString(R.string.network_error));
                    Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        Gson gson = new Gson();
                        String regpojo = gson.toJson(loginDetail);
                        Log.e(TAG, "getParams   = " + regpojo);

                        params.put("data", regpojo);
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
                if (dialog_progress != null) {
                    dialog_progress.dismiss();
                    dialog_progress = null;
                }

            }
        }

    }


    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile
     * @return
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }


    public void showSoftKeyboard(View view) {

//        View view = getActivity().getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    Dialog dialog_OTP_Verification;
    TextView tv_tag_mno;
    TextView tv_btn_resend, tv_btn_edit_MobileNO;
    PinView edt_OTP_Pin;
    Button btn_verifyOTP;

    public void show_dialog_OTP_Verification(LoginDetail loginDetail) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_otp_verification, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_OTP_Verification = new Dialog(LoginWithMobile.this);
        dialog_OTP_Verification.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_OTP_Verification.setContentView(root);
        dialog_OTP_Verification.setCancelable(false);
        dialog_OTP_Verification.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_OTP_Verification.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        try {
            btn_verifyOTP = dialog_OTP_Verification.findViewById(R.id.btn_verifyOTP);

            edt_OTP_Pin = dialog_OTP_Verification.findViewById(R.id.edt_OTP_Pin);

            tv_tag_mno = dialog_OTP_Verification.findViewById(R.id.tv_tag_mno);
            tv_btn_resend = dialog_OTP_Verification.findViewById(R.id.tv_btn_resend);
            tv_btn_edit_MobileNO = dialog_OTP_Verification.findViewById(R.id.tv_btn_edit_MobileNO);

            tv_tag_mno.setText(maskString(loginDetail.getRegmobile(), 2, 6, '*'));

            tv_btn_edit_MobileNO.setOnClickListener(this);
            tv_btn_resend.setOnClickListener(this);
            edt_OTP_Pin.setOnClickListener(this);
            btn_verifyOTP.setOnClickListener(v -> {
                String edt_pin = edt_OTP_Pin.getText().toString();
                if (edt_pin.length() == 4) {
                    if (edt_pin.equals("1234")) {
                        final String uid = "Administrator";
                        String password = "tspl";
//                http://159.89.164.145:8000/api/method/login?usr=Administrator&pwd=tspl
                        if (loginDetail.getRegteam().equalsIgnoreCase(AppConfig.Team_Login_Yes)) {
                            FindEmployeeLoginMobileNoOnServer(loginDetail);
                        } else {
                            FindLoginMobileNoOnServer(loginDetail);
                        }


//                    dialog_OTP_Verification.dismiss();
//                    finish();
//                    startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                    } else {
                        TastyToast.makeText(getApplicationContext(), getString(R.string.error_Invalid_OTP), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                    }
                } else {
                    TastyToast.makeText(getApplicationContext(), getString(R.string.error_Invalid_OTP), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            });

            dialog_OTP_Verification.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Dialog dialog_progress;
    TextView tv_prog_msg;

    public void show_dialog_progress(String Msg) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            final View root = inflater.inflate(R.layout.dialog_progress, null);
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialog_progress = new Dialog(LoginWithMobile.this);
            dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_progress.setContentView(root);
            dialog_progress.setCancelable(false);
            dialog_progress.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
            dialog_progress.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


            tv_prog_msg = (TextView) dialog_progress.findViewById(R.id.tv_prog_msg);
            tv_prog_msg.setText(Msg);
            dialog_progress.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String maskString(String strText, int start, int end, char maskChar)
            throws Exception {

        if (strText == null || strText.equals(""))
            return "";

        if (start < 0)
            start = 0;

        if (end > strText.length())
            end = strText.length();

        if (start > end)
            throw new Exception("End index cannot be greater than start index");

        int maskLength = end - start;

        if (maskLength == 0)
            return strText;

        StringBuilder sbMaskString = new StringBuilder(maskLength);

        for (int i = 0; i < maskLength; i++) {
            sbMaskString.append(maskChar);
        }

        return strText.substring(0, start)
                + sbMaskString.toString()
                + strText.substring(start + maskLength);
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;


    private void checkPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");

        int accessfinelocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int accesscarselocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writestoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int accesswifistate = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int CAMERA = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        int RECEIVE_SMS = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int READ_SMS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        int SEND_SMS = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int CALL_PHONE = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int READ_CALENDAR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        int WRITE_CALENDAR = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (CAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writestoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (accessfinelocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (accesscarselocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

//        if (accesswifistate != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
//        }

        if (READ_SMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (RECEIVE_SMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
//        if (SEND_SMS != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
//        }
        if (CALL_PHONE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (READ_CALENDAR != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALENDAR);
        }
        if (WRITE_CALENDAR != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_CALENDAR);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    LOCATION_PERMISSION_REQUEST_CODE);

        } else {
            CheckGpsStatus();
            Log.e(TAG, "GpsStatus  = " + GpsStatus);
            if (!GpsStatus) {
                alertToturnOffGps();
            } else {
                getMyCurrentLocation();
            }
        }

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


    public void clearEditText() {
        input_mno.setText(null);
        input_email.setText(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d(TAG, "onRequestPermissionsResult: permission name = " + permissions[i] + "  Result = " + grantResults[i]);
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize our map
//                    initMap();
                    CheckGpsStatus();
                    Log.e(TAG, "GpsStatus  = " + GpsStatus);
                    if (!GpsStatus)
                        alertToturnOffGps();
                    else {
                        getMyCurrentLocation();
                    }
                }
            }
        }
    }

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    LocationManager locationManager;
    boolean GpsStatus;

    public void CheckGpsStatus() {

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
                dialogInterface.dismiss();
                finish();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Check the type of GPS Provider available at that instance and
     * collect the location informations
     *
     * @Output Latitude and Longitude
     */
    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(LoginWithMobile.this);
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
//                Log.e(TAG, " addressFragments.get(0) " + addressFragments.toString());

                UserAddress = addresses.get(0).getAddressLine(0);
//            Log.e(TAG," getFeatureName.get(0) " + addresses.get(0).getFeatureName());
//            Log.e(TAG," getThoroughfare.get(0) " + addresses.get(0).getThoroughfare());
//            Log.e(TAG," getPostalCode.get(0) " + addresses.get(0).getPostalCode());

                // you can get more details other than this . like country code, state code, etc.
                Log.e(TAG, " getFeatureName.get(0) " + addresses.get(0).getAddressLine(0));
                Log.e(TAG, "MyLat: " + MyLat);
                Log.e(TAG, "MyLong : " + MyLong);
                Log.e(TAG, " StateName : " + StateName
                        + " CityName : " + CityName
                        + " CountryName : " + CountryName);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
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

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = getString(R.string.back_online);
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = getString(R.string.you_are_offline);
            color = Color.RED;
        }
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
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
        Log.e(TAG, " onDestroy => " + TAG);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
//        stopLockTask();
    }
}
