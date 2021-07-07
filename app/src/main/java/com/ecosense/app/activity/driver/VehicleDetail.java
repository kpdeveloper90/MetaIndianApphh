package com.ecosense.app.activity.driver;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.Traccar.TrackingService;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteInfo;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class VehicleDetail extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = VehicleDetail.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;

    @BindView(R.id.img_user)
    ImageView img_user;

    @BindView(R.id.tv_uName)
    TextView tv_uName;
    @BindView(R.id.tv_dlNo)
    TextView tv_dlNo;

    @BindView(R.id.tv_uMno)
    TextView tv_uMno;

    @BindView(R.id.tv_vehicle_no)
    TextView tv_vehicle_no;

    @BindView(R.id.tv_ch_no)
    TextView tv_ch_no;

    @BindView(R.id.tv_model)
    TextView tv_model;

    @BindView(R.id.tv_ownerName)
    TextView tv_ownerName;

    @BindView(R.id.tv_route_date)
    TextView tv_route_date;
    @BindView(R.id.tv_route_date_month)
    TextView tv_route_date_month;
    @BindView(R.id.tv_RouteName)
    TextView tv_RouteName;
    @BindView(R.id.img_playRoute)
    ImageView img_playRoute;

    @BindView(R.id.im_btn_assign_complaints)
    MaterialButton im_btn_assign_complaints;
    static Vehicle vehicleDetails;
    private SharedPreferences sharedPreferences;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Vehicle Detail");
        setSupportActionBar(toolbar);

        tv_uName.setText((session.getpsName()));
        tv_uMno.setText((session.getMobileNumber()));
        tv_dlNo.setText("DL No. : " + session.getDLNumber());
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        onNewIntent(getIntent());
        setupTraccarService();
        img_playRoute.setOnClickListener(this);
        im_btn_assign_complaints.setOnClickListener(this);

    }

    public void setupTraccarService() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);

//        if (!sharedPreferences.contains(KEY_DEVICE)) {
//            String id = String.valueOf(new Random().nextInt(900000) + 100000);
//
//        }
        sharedPreferences.edit().putString(AppConfig.KEY_DEVICE, session.getpsNo()).apply();
        sharedPreferences.edit().putBoolean(AppConfig.KEY_STATUS, Connection.KEY_STATUS_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_INTERVAL, Connection.KEY_INTERVAL_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ANGLE, Connection.KEY_ANGLE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_DISTANCE, Connection.KEY_DISTANCE_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_URL, Connection.Traccar_url_value).apply();
        sharedPreferences.edit().putString(AppConfig.KEY_ACCURACY, Connection.KEY_ACCURACY_value).apply();

    }

    static int btnAlertCont = 0;

    @Override
    public void onClick(View v) {
        if (v == img_playRoute) {

            btnAlertCont = btnAlertCont + 1;
            aleartforCompetRoute(getString(R.string.start_route));


//            Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
//            Bundle b = new Bundle();
//            b.putSerializable("vehicleDetails", vehicleDetails);
//            mIntent.setAction("getVehicleDetail");
//            mIntent.putExtra("SelectedVehicleDetail", b);
//            startActivity(mIntent);
//            finish();
        }
        if (v == im_btn_assign_complaints) {
            Intent mIntent = new Intent(getApplicationContext(), MyAssignComplaintsList.class);
            Bundle b = new Bundle();
            b.putSerializable("vehicleDetails", vehicleDetails);
            mIntent.setAction("getVehicleDetail");
            mIntent.putExtra("SelectedVehicleDetail", b);
            startActivity(mIntent);
            finish();
        }
    }

    protected void aleartforCompetRoute(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(btnAlertCont + getString(R.string.alert_for_start_routr))
                .setCancelable(false)
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        if (btnAlertCont >= 3) {
                        if (vehicleDetails.getRoute_info().isEmpty() && vehicleDetails.getRoute_info() == null) {
                            TastyToast.makeText(getApplicationContext(), getString(R.string.route_not_assign), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        } else {
                            getRouteDetailOnServer(vehicleDetails.getRoute_info());
                            btnAlertCont = 0;
                        }
//                        }
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                        btnAlertCont = 0;
                    }
                })
                .show();

    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.Traccar.TrackingService".equals(service.service.getClassName())) {
                Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " TrackingService not Running  return false");
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (isServiceRunning() == false) {
                // if start tracking Uncomment condition
                if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
                    startTrackingService(true, false);
                }
            } else {
                Log.e(TAG, "DriverDashBoard TrackingService Already Running");
            }
        }
    }

    private void startTrackingService(boolean checkPermission, boolean permission) {
        if (checkPermission) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                permission = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
        }

        if (permission) {
            ContextCompat.startForegroundService(this, new Intent(this, TrackingService.class));
//            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    ALARM_MANAGER_INTERVAL, ALARM_MANAGER_INTERVAL, alarmIntent);
            Log.e(TAG, "DriverDashBoard TrackingService  Start");
        } else {
//            sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply();
//            TwoStatePreference preference = (TwoStatePreference) findPreference(KEY_STATUS);
//            preference.setChecked(false);

            if (isServiceRunning() == true) {
                stopTrackingService();
            }
        }
    }

    private void stopTrackingService() {
//        alarmManager.cancel(alarmIntent);
        stopService(new Intent(this, TrackingService.class));
//        setPreferencesEnabled(true);
        Log.e(TAG, "DriverDashBoard TrackingService  stop");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        Intent intent = getIntent();
        Bundle extras = intent.getBundleExtra("SelectedVehicleDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

            if (methodIntent != null && methodIntent.equals("getVehicleDetail")) {
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                Log.e(TAG, "vehicleDetails.getVehicle_registration_no() " + vehicleDetails.getVehicle_registration_no());

                tv_vehicle_no.setText(vehicleDetails.getVehicle_registration_no());
                tv_ch_no.setText(vehicleDetails.getChassis_number());
                tv_model.setText(vehicleDetails.getVehicle_type());
                tv_ownerName.setText(vehicleDetails.getVehicle_owner_name());
                if (session.getUserProfilePic() != null) {
                    String pic_url = session.getMyServerIP() + session.getUserProfilePic();
                    Glide.with(this).load(pic_url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_user);
                } else {
                    Glide.with(this).load(R.drawable.ic_user)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_user);
                }
                if (vehicleDetails.getRoute_info().isEmpty() && vehicleDetails.getRoute_info() == null) {
                    TastyToast.makeText(getApplicationContext(), "Route is not Assign ", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                } else {
                    tv_RouteName.setText(vehicleDetails.getVm_routename());
                    tv_route_date.setText(geatDayFromDate(vehicleDetails.getRoute_lastmodified()));
                    tv_route_date_month.setText(geatMonthNameFromDate(vehicleDetails.getRoute_lastmodified()));
                }
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }
    }

    public String geatDayFromDate(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "dd";


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


    public void getRouteDetailOnServer(String routeId) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(VehicleDetail.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;


                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

//                    String url = session.getMyServerIP() + "/api/resource/Registration?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields;
                    String url = session.getMyServerIP() + "/api/resource/Route_Master/" + URLEncoder.encode(routeId, "UTF-8") + "?fields=" + fields;
//                    String url = session.getMyServerIP() + "/api/resource/Route_Master/" + routeId + "?fields=" + fields;
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
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            RouteItem routeDetail = objectMapper.readValue(statusData.toString(), RouteItem.class);
                                            String rDate_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                                            Log.e(TAG, "getRouteDetailOnServer Route_Master  = " + routeDetail.getName());

                                            RouteItem routeTrans = new RouteItem();

                                            routeTrans.setDa_date(rDate_time);
                                            routeTrans.setDa_vehicleno(vehicleDetails.getVehicle_registration_no());
                                            routeTrans.setVehicle_type(vehicleDetails.getVehicle_type());


                                            routeTrans.setDa_drivername(session.getpsName());
                                            routeTrans.setDa_mobno(session.getMobileNumber());
                                            routeTrans.setDa_dlno(session.getDLNumber());

                                            routeTrans.setDa_routeid(routeDetail.getName());
                                            routeTrans.setR_status(AppConfig.ROUTE_Status_In_Progress);
                                            routeTrans.setR_name(routeDetail.getR_name());
//                                            routeTrans.setDa_routeinfo_act(routeDetail.getR_info());
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            createRouteTransaction(routeTrans, routeDetail.getR_info());

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


    public void createRouteTransaction(RouteItem routeTrans, ArrayList<RouteInfo> routeItemArrayList) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(VehicleDetail.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");


//                String url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(complaintsDetails.getComId(), "UTF-8");
                    String url = session.getMyServerIP() + "/api/resource/DriverActivity";
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    if (!response.toString().equalsIgnoreCase("{}")) {
//                                        Log.e("Response", response);
                                        try {
                                            //create ObjectMapper instance
                                            ObjectMapper objectMapper = new ObjectMapper();
                                            JsonNode rootNode = objectMapper.readTree(response.toString());
                                            JsonNode statusData = rootNode.path("data");
//                                            Log.e(TAG, "statusData = " + statusData.toString());
                                            if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                                RouteItem rDetail = objectMapper.readValue(statusData.toString(), RouteItem.class);
                                                Log.e(TAG, "createRouteTransaction response DriverActivity getComId = " + rDetail.getDa_routeid());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                ArrayList<RouteInfo> routeNodeList = new ArrayList<>();
                                                for (int i = 0; i < routeItemArrayList.size(); i++) {
                                                    RouteInfo ri = routeItemArrayList.get(i);

                                                    RouteInfo routeInfoNode = new RouteInfo();

                                                    routeInfoNode.setRoute_qrcode(ri.getRoute_qrcode());
                                                    routeInfoNode.setRoute_photo(ri.getRoute_photo());
//                                                    routeInfoNode.setClean_status(ri.getClean_status());
                                                    routeInfoNode.setClean_status(AppConfig.BIN_Status_Scheduled);

                                                    routeInfoNode.setFawardno(ri.getFawardno());
                                                    routeInfoNode.setBarcode(ri.getBarcode());
                                                    routeInfoNode.setRoute_assetname(ri.getRoute_assetname());
                                                    routeInfoNode.setRoute_icon(ri.getRoute_icon());
                                                    routeInfoNode.setRoute_assetloc(ri.getRoute_assetloc());
                                                    routeInfoNode.setRoute_assetcap(ri.getRoute_assetcap());
                                                    routeInfoNode.setRoute_lat(ri.getRoute_lat());
                                                    routeInfoNode.setRoute_long(ri.getRoute_long());
                                                    routeInfoNode.setType(ri.getType());
                                                    routeInfoNode.setRoute_location_name(ri.getRoute_location_name());

                                                    routeInfoNode.setDa_code(rDetail.getName());
                                                    routeInfoNode.setR_name(rDetail.getR_name());
                                                    routeInfoNode.setDa_drivername(rDetail.getDa_drivername());
                                                    routeInfoNode.setDa_mobno(rDetail.getDa_mobno());
                                                    routeInfoNode.setDa_dlno(rDetail.getDa_dlno());

                                                    routeInfoNode.setDa_routeid(rDetail.getDa_routeid());
                                                    routeInfoNode.setDa_date(rDetail.getDa_date());
                                                    routeInfoNode.setDa_vehicleno(rDetail.getDa_vehicleno());

                                                    routeNodeList.add(routeInfoNode);

                                                    createAllNodeTransaction(routeInfoNode);
                                                }
                                                if (routeItemArrayList.size() == routeNodeList.size()) {
                                                    if (isServiceRunning() == false) {
                                                        // if start tracking Uncomment condition
                                                        if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
                                                            startTrackingService(true, false);
                                                        }
                                                    } else {
                                                        Log.e(TAG, "DriverDashBoard TrackingService Already Running");
                                                    }
                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }
                                                    try {
                                                        rDetail.setVehicle_registration_no(vehicleDetails.getVehicle_registration_no());
                                                        if (routeItemArrayList.get(1).getFawardno() != null) {
                                                            rDetail.setFawardno(routeItemArrayList.get(1).getFawardno());
                                                        }
                                                    } catch (Exception e) {

                                                    }
                                                    Intent mIntent = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
                                                    Bundle b = new Bundle();
                                                    b.putSerializable("pendingRouteDetails", rDetail);
                                                    b.putSerializable("vehicleDetails", vehicleDetails);
                                                    mIntent.setAction("pendingRoute");
                                                    mIntent.putExtra("SelectedVehicleDetail", b);
                                                    startActivity(mIntent);
                                                    finish();
                                                } else
                                                    TastyToast.makeText(getApplicationContext(), "Error! Could note create transaction", TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                    }
                                } else {
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }

                            }, error -> {
                        // error

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            Gson gson = new Gson();
                            String regpojo = gson.toJson(routeTrans);
//                            Log.e(TAG, "getParams   = " + regpojo);

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
                }
            }, PROGRASS_postDelayed);
        }
    }

    public void createAllNodeTransaction(RouteInfo routeNodeTrans) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

//            mProgressDialog = new ProgressDialog(VehicleDetail.this);
//            mProgressDialog.setMessage("Please Wait...");
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();
//            new android.os.Handler().postDelayed(() -> {
            try {
                String data = null;
                data = "{}";
                URLEncoder.encode(data, "UTF-8");


//                String url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(complaintsDetails.getComId(), "UTF-8");
                String url = session.getMyServerIP() + "/api/resource/ActivityMaster";
                Log.e(TAG, "url = > " + url);
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            // response
                            if (!response.isEmpty()) {
                                if (!response.toString().equalsIgnoreCase("{}")) {
//                                        Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                            Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            RouteInfo rDetail = objectMapper.readValue(statusData.toString(), RouteInfo.class);

                                            Log.e(TAG, "createAllNodeTransaction ActivityMaster response MetaRouteInfo Node Name = " + rDetail.getName());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

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
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                }
                            } else {
                                Log.e(TAG, "Response Error");
                                TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            }

                        }, error -> {
                    // error

                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

                    Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        Gson gson = new Gson();
                        String regpojo = gson.toJson(routeNodeTrans);
//                            Log.e(TAG, "getParams   = " + regpojo);

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
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }

            }
//            }, PROGRASS_postDelayed);
        }

    }


    public String geatMonthNameFromDate(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "MMM";


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

    protected void exitByBackKey() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.do_you_want_to_logout))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Clear the session data
                        // This will clear all session data and
                        //  rdirect user to LoginActivity
                        FragmentManager mFragmentManager = getSupportFragmentManager();
                        if (mFragmentManager.getBackStackEntryCount() > 0)
                            mFragmentManager.popBackStackImmediate();

                        finish();
                        session.logoutUser();
//                        System.exit(0);

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

    MenuItem item_home, item_logout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_driver_dashboard, menu);
        item_logout = menu.findItem(R.id.navigation_logout);
        item_home = menu.findItem(R.id.navigation_home);
        item_logout.setVisible(false);
        item_home.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.navigation_logout:
                exitByBackKey();
                return true;

            case R.id.navigation_home:
                finish();
                startActivity(new Intent(getApplicationContext(), DriverDashBoard.class));
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
//            super.onBackPressed();

    }

}
