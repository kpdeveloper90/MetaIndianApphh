package com.ecosense.app.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.LoginDetail;

public class SplashScreen extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = SplashScreen.class.getSimpleName();
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    UserSessionManger session = null;
    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static int SPLASH_TIME_OUT = 2000;

    @BindView(R.id.tv_version_code)
    TextView tv_version_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        /* Set the app into full screen mode */
//        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_splesh_screen);
        session = new UserSessionManger(this);
        ButterKnife.bind(this);
//        Intent intent = new Intent(getBaseContext(),ReminderService.class);
//        Splesh_Screen.this.startService(intent);


        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
        tv_version_code.setText("v 1.0");
        if (!isTimeAutomatic(this)) {
            alertAuto_Time_ON_Off();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Date date1 = formatter.parse(formatter.format(new Date()));
                        Date date2 = formatter.parse("2022-08-31");
                        if (date1.compareTo(date2) == 0) {
                            Log.e(TAG, "Your License is expired. ");
                            alert_License();
                        } else if (date1.compareTo(date2) > 0) {
                            Log.e(TAG, "Your License is expired. ");
                            alert_License();
                        } else {
                            session.setApp_InstallDate(date1.toString());
                            Log.e(TAG, "setApp_InstallDate = " + date1.toString());
                            move_to_login();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }, SPLASH_TIME_OUT);
        }
    }

    public void move_to_login() {
        //                final String uid = "Administrator";
        final String uid = "admin";
//                String password = "tspl";
        String password = "tspl#123";
//                http://159.89.164.145:8000/api/method/login?usr=Administrator&pwd=tspl
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), "You're Not Connected Internet", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            finish();
        } else {
            FindLoginDetailFromServer(uid, password);
        }
    }

    @Override
    public void onClick(View v) {

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

    public static boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(c.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    protected void alertAuto_Time_ON_Off() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Alert")
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage("Please Turn on Automatic date & time !!!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                })
                .show();

    }

    protected void alert_License() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(getString(R.string.connect_admin))
                .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

//                        List<String> list = getInstalledApps();
//                        Log.e(TAG, "list " + list.toString());
//                         Initialize a new Intent to uninstall an app/package
                        finish();
//                        Intent intent = new Intent(Intent.ACTION_DELETE);
//                        String packageName = "com.ecosense.app";
//                        intent.setData(Uri.parse("package:" + packageName));
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);

                    }
                })
                .show();

    }

    private List<String> getInstalledApps() {
        List<String> res = new ArrayList<String>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((isSystemPackage(p) == false)) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                String packageName = p.packageName;
                Log.e(TAG, "packageName " + packageName);
                res.add(new String(packageName));
            }
        }
        return res;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

    private void FindLoginDetailFromServer(String lid, String lpwd) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_two_button("No internet connection. \nPlease Turn on internet.");
//            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
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
//                String url = "http://159.89.164.145:8000/api/method/login?usr=Administrator&pwd=tspl";
                String url = session.getMyServerIP() + "/api/method/login";
                Log.e(TAG, url);
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {

                            Log.e("Response", response);

                            //create ObjectMapper instance
                            ObjectMapper objectMapper = new ObjectMapper();

                            LoginDetail loginDetail = null;

                            try {
                                loginDetail = objectMapper.readValue(response.toString(), LoginDetail.class);


                                Log.e(TAG, " response.getMessage() " + loginDetail.getMessage());
                                Log.e(TAG, " response.getFull_name() " + loginDetail.getFull_name());
//                            if (sweetItem.getSuccess() == 1) {
                                if (loginDetail.getMessage().equalsIgnoreCase("Logged In")) {
//
//                                String psNo = sweetItem.getPsNo();
//                                String psName = sweetItem.getPsName();
//                                String userType = sweetItem.getUserType();
//
//                                Log.e(TAG, "psName = " + psName + "\n  psNo= " + psNo + "\n  userType= " + userType);
//                                session.createUserLoginSession(psNo, psName, userType);
//
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }

                                    Log.e(TAG, "Server Connected Successfully session.getAppInstall_1stTime()" + session.getAppInstall_1stTime());

                                    if (session.getAppInstall_1stTime().equalsIgnoreCase("Yes")) {
                                        session.setAppInstall_1stTime("No");
                                        Intent i = new Intent(getApplicationContext(), LoginWithMobile.class);
                                        startActivity(i);
                                        finish();

                                   /** as per some time move to direct login screen */
                                    /*    finish();
                                        Intent intent = new Intent(getApplicationContext(), SelectAppLanguage.class); // the activity that holds the fragment
                                        Bundle b = new Bundle();
//                    b.putSerializable("GateEntryItem", gateEntryItem);
                                        intent.setAction("NewAppInstall");
                                        intent.putExtra("SelectedNameDetail", b);
                                        startActivity(intent);*/
                                    } else {
                                        Log.e(TAG, "session.checkLogin() = " + session.isUserLoggedIn());
                                        if (!session.isUserLoggedIn()) {
                                            Log.e(TAG, "in session.isUserLoggedIn()");
                                            Intent i = new Intent(getApplicationContext(), LoginWithMobile.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            //check User Type
                                            if (session.getuserSubType() != null) {
                                                String subType = session.getuserSubType();
                                                if (subType.equalsIgnoreCase(AppConfig.USubType_Individual) || subType.equalsIgnoreCase(AppConfig.USubType_Corporate)) {
                                                    startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                                    finish();
                                                } else if (subType.equalsIgnoreCase(AppConfig.USubType_Driver)) {
                                                    startActivity(new Intent(getApplicationContext(), DriverDashBoard.class));
                                                    finish();
                                                } else if (subType.equalsIgnoreCase(AppConfig.USubType_Data_Collector)) {
                                                    startActivity(new Intent(getApplicationContext(), MetaDataDashBoard.class));
                                                    finish();
                                                } else if (subType.equalsIgnoreCase(AppConfig.USubType_Supervisor)) {
                                                    startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
                                                    finish();
                                                }
                                                else {
                                                    TastyToast.makeText(getApplicationContext(), "Something is wrong", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                                    Log.e(TAG, "in else session.checkLogin()  ");
                                                    startActivity(new Intent(getApplicationContext(), LoginWithMobile.class));
                                                    finish();
                                                }
                                            } else {

                                                Intent i = new Intent(getApplicationContext(), LoginWithMobile.class);
                                                startActivity(i);
                                                finish();
                                                /** as per some time move to direct login screen */
                                          /**      finish();
                                                Intent intent = new Intent(getApplicationContext(), SelectAppLanguage.class); // the activity that holds the fragment
                                                Bundle b = new Bundle();
//                    b.putSerializable("GateEntryItem", gateEntryItem);
                                                intent.setAction("NewAppInstall");
                                                intent.putExtra("SelectedNameDetail", b);
                                                startActivity(intent);*/

                                            }
                                        }
                                    }
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                    finish();
                                    Log.e(TAG, "FindLoginDetailFromServer onResponse Error :" + response);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, " Error");
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;
                                }
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
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("usr", lid);
                        params.put("pwd", lpwd);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = super.getHeaders();

                        if (headers == null
                                || headers.equals(Collections.emptyMap())) {
                            headers = new HashMap<String, String>();
                        }

                        ConnactionCheckApplication.getInstance().addSessionCookie(headers);

//                        return getAuthHeader(getApplicationContext());
                        return headers;
                    }

                    @Override
                    protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
                        // since we don't know which of the two underlying network vehicles
                        // will Volley use, we have to handle and store session cookies manually
                        Log.e("response", response.headers.toString());
                        Map<String, String> responseHeaders = response.headers;
                        String rawCookies = responseHeaders.get("Set-Cookie");
                        Log.e("cookies", rawCookies);
                        // since we don't know which of the two underlying network vehicles
                        // will Volley use, we have to handle and store session cookies manually
                        ConnactionCheckApplication.getInstance().checkSessionCookie(response.headers);

                        return super.parseNetworkResponse(response);
                    }
                };
                // Tag used to cancel the request
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);
//        ApplicationController.getInstance(getApplicationContext()).addToRequestQueue(postRequest, tag_string_req);
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(postRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String auth, API_KEY, CONTENT_TYPE;

    public static Map<String, String> getAuthHeader(Context context) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("token", auth);
        headerMap.put("Api-key", API_KEY);
        headerMap.put("Content-Type", CONTENT_TYPE);
        return headerMap;
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
        int color;
        if (isConnected) {
            message = "Back-Online";
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
        }
////        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        Snackbar snackbar = Snackbar
//                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);

//        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(color);
//        snackbar.show();
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
