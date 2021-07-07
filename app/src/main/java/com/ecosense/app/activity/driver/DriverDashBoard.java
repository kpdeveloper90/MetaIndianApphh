package com.ecosense.app.activity.driver;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.ContactListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.LoginDetail;
import com.ecosense.app.pojo.model.Contact;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class DriverDashBoard extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = DriverDashBoard.class.getSimpleName();
    private Toolbar toolbar;
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.btn_start)
    Button btn_start;

    @BindView(R.id.img_user)
    ImageView img_user;

    @BindView(R.id.tv_uName)
    TextView tv_uName;

    @BindView(R.id.tv_uMno)
    TextView tv_uMno;

    @BindView(R.id.tv_dlNo)
    TextView tv_dlNo;

    @BindView(R.id.rv_imp_cno)
    RecyclerView rv_imp_cno;
    static LoginDetail loginDetail;
    ArrayList<Contact> imp_cno = null;
    ContactListAdapter contactListAdapter = null;

//
//    //Traccar Config
//    private static final int ALARM_MANAGER_INTERVAL = 15000;
//
//
//    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
//
//    private SharedPreferences sharedPreferences;
//
//    private AlarmManager alarmManager;
//    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dash_board);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Driver Dashboard");
        setSupportActionBar(toolbar);
        isConnected = checkConnection();
//        showSnack(isConnected);
        if (isConnected) {
            Log.e(TAG, "Connected to Internet");
        } else if (!isConnected) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
        imp_cno = new ArrayList<>();
        GridLayoutManager lLayout = new GridLayoutManager(this, 1);
        rv_imp_cno.setHasFixedSize(true);
        rv_imp_cno.setLayoutManager(lLayout);
        rv_imp_cno.setItemAnimator(new DefaultItemAnimator());
        contactListAdapter = new ContactListAdapter(imp_cno);
        rv_imp_cno.setAdapter(contactListAdapter);
        btn_start.setOnClickListener(this);

        getRegUserDetailOnServer(session.getpsNo());

//
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);
//
////        if (!sharedPreferences.contains(KEY_DEVICE)) {
////            String id = String.valueOf(new Random().nextInt(900000) + 100000);
////
////        }
//        sharedPreferences.edit().putString(AppConfig.KEY_DEVICE, session.getpsNo()).apply();
//        sharedPreferences.edit().putBoolean(AppConfig.KEY_STATUS, Connection.KEY_STATUS_value).apply();
//        sharedPreferences.edit().putString(AppConfig.KEY_INTERVAL, Connection.KEY_INTERVAL_value).apply();
//        sharedPreferences.edit().putString(AppConfig.KEY_ANGLE, Connection.KEY_ANGLE_value).apply();
//        sharedPreferences.edit().putString(AppConfig.KEY_DISTANCE, Connection.KEY_DISTANCE_value).apply();
//        sharedPreferences.edit().putString(AppConfig.KEY_URL, Connection.Traccar_url_value).apply();
//        sharedPreferences.edit().putString(AppConfig.KEY_ACCURACY, Connection.KEY_ACCURACY_value).apply();


    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.Traccar.TrackingService".equals(service.service.getClassName())) {
                Log.e(TAG, " TrackingService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " SyncService not Running  return false");
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == btn_start) {
//            if (isServiceRunning() == false) {
//                // if start tracking Uncomment condition
//                if (sharedPreferences.getBoolean(AppConfig.KEY_STATUS, false)) {
//                    startTrackingService(true, false);
//                }
//            } else {
//                Log.e(TAG, "DriverDashBoard TrackingService Already Running");
//            }
//            startActivity(new Intent(getApplicationContext(), TraccarMainActivity.class));
            Intent mIntent = new Intent(getApplicationContext(), SimpleScannerActivity.class); // the activity that holds the fragment
//            Bundle b = new Bundle();
//            b.putSerializable("PlaceInfo", placeItem);
            mIntent.setAction("DriverScanVehicleQRCOde");
//            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
            finish();

        }
    }

    public void getRegUserDetailOnServer(String regNo) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(DriverDashBoard.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;


                    filters = "[[\"Employee\", \"name\", \"=\",\""
                            + regNo
                            + "\"]]";


//                    filters = URLEncoder.encode("[[\"Complaints\", \"cptuserid\", \"=\",\"", "UTF-8")
//                            + session.getpsNo()
//                            + URLEncoder.encode("\"],[\"Complaints\", \"cptmobileno\", \"=\",\"", "UTF-8")
//                            + session.getMobileNumber()
//                            + URLEncoder.encode("\"]]", "UTF-8");

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

//                fields = "[\"*\"]";
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

//                    String url = session.getMyServerIP() + "/api/resource/Registration?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + fields;
                    String url = session.getMyServerIP() + "/api/resource/Employee/" + URLEncoder.encode(regNo, "UTF-8") + "?fields=" + fields;
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
                                            LoginDetail loginDet = objectMapper.readValue(statusData.toString(), LoginDetail.class);

                                            Log.e(TAG, "getVisitor_name = " + loginDet.getUserId());

                                            session.setpsNo(loginDet.getUserId());
                                            session.setpsName(loginDet.getEmployee_name());
                                            session.setuserCost_Center(loginDet.getCost_center());
                                            //for team user used reg_teamphoto and for citizen reg_photo
                                            session.setUserProfilePic(loginDet.getImage());
                                            session.setDlNumber(loginDet.getReg_license_number());
                                            session.setMobileNumber(loginDet.getRegmobile());
                                            session.setuserSubType(loginDet.getDesignation());
                                            session.setuserType(AppConfig.UType_Team);

                                            if (loginDet.getPersonal_email() != null) {
                                                session.setEmailLogin(loginDet.getPersonal_email());
                                            }
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

                                            if (loginDet.getReg_impcno() != null) {
                                                imp_cno.clear();
                                                Log.e(TAG, "Size loginDet.getReg_impcno() " + loginDet.getReg_impcno().size());

//                                                imp_cno.addAll(loginDet.getReg_impcno());
                                                contactListAdapter.notifyDataSetChanged();
                                            }
                                            tv_uName.setText((session.getpsName()));
                                            tv_dlNo.setText("DL No. : " + session.getDLNumber());
                                            tv_uMno.setText((session.getMobileNumber()));


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

    protected void exitByBackKey(String action,String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        if (isServiceRunning() == true) {
//                            stopTrackingService();
//                        }
                        // Clear the session data
                        // This will clear all session data and
                        //  rdirect user to LoginActivity


                        if (action.equalsIgnoreCase("Logout")) {
                            FragmentManager mFragmentManager = getSupportFragmentManager();
                            if (mFragmentManager.getBackStackEntryCount() > 0)
                                mFragmentManager.popBackStackImmediate();
                            session.logoutUser();

                        }
                        finish();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_driver_dashboard, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.navigation_logout:
                exitByBackKey("Logout",getString(R.string.logout_msg));
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

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
//        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

/**
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
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL, ALARM_MANAGER_INTERVAL, alarmIntent);
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
        alarmManager.cancel(alarmIntent);
        stopService(new Intent(this, TrackingService.class));
//        setPreferencesEnabled(true);
        Log.e(TAG, "DriverDashBoard TrackingService  stop");
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
 */
    @Override
    public void onBackPressed() {
//            super.onBackPressed();
        exitByBackKey("Back", getString(R.string.exit_msg));
    }


}
