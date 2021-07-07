package com.ecosense.app.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.helper.LocaleHelper;
import com.ecosense.app.helper.UserSessionManger;

public class SelectAppLanguage extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SelectAppLanguage.class.getSimpleName();
    public static String SERVER_URL = null;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @BindView(R.id.ll_appselector)
    LinearLayout ll_appselector;

    @BindView(R.id.tv_selected_language)
    TextView tv_selected_language;
    UserSessionManger session = null;

    String methodIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* Set the app into full screen mode */
        getWindow().getDecorView().setSystemUiVisibility(flags);
        setContentView(R.layout.activity_select_app_language);

        ButterKnife.bind(this);
        checkPermission();
        session = new UserSessionManger(this);

        // Initializing a String Array
        dialogForSelectLanguage();

        ll_appselector.setOnClickListener(this);

        Intent intent = getIntent();
        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

        } else {
            Log.e(TAG, "Bundle Is empty ");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == ll_appselector) {
            dialogForSelectLanguage();
        }
    }


    private void dialogForSelectLanguage() {

        final String[] nm = {""};
        String[] plants = new String[]{
                "English",
                "ગુજરાતી",
                "हिंदी",
                "मराठी",
        };

        final List<String> lang_List = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.app_language)));
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, lang_List);

//        ItemGroupNameForAlertDialog arrayAdapter = new ItemGroupNameForAlertDialog(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, sweetItemsGroupList);


        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Select Language");
        mBuilder.setCancelable(false);
        mBuilder.setSingleChoiceItems(plants, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

//                    SweetItem sweetItem = sweetItemsGroupList.get(i);
//
//                    Toast.makeText(ItemMaster.this, "Group Name = " + sweetItem.getItemGroup(), Toast.LENGTH_SHORT).show();
//
//                    dialogInterface.dismiss();

//                dialogInterface.dismiss();
//                String sweetItem = lang_List.get(i);
                String sweetItem = plants[i];
                nm[0] = sweetItem;
                Log.e(TAG, " nm[0]  = " + nm[0]);
            }
        });
        // Set the alert dialog positive button
        mBuilder.setPositiveButton("Set", (dialogInterface, i) -> {
            if (nm[0].equalsIgnoreCase("")) {
                TastyToast.makeText(getApplicationContext(), "Not Select Any Language", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                dialogForSelectLanguage();
            } else {
//                tv_selected_language.setText(nm[0]);
                slectedLanguage(nm[0]);
                TastyToast.makeText(getApplicationContext(), "Set " + nm[0] + " Language for App.", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                finish();
                startActivity(new Intent(getApplicationContext(), LoginWithMobile.class));
                dialogInterface.dismiss();

            }

        });
        mBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            finish();
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void slectedLanguage(String sele_language) {

        Log.e(TAG, "select_language = " + sele_language);
        //
        if (sele_language.equalsIgnoreCase("English")) {
            session.setAppLanguage("en");
            updateView(session.getAppLanguage());
        } else if (sele_language.equalsIgnoreCase("हिंदी")) {
            session.setAppLanguage("hi");
            updateView(session.getAppLanguage());
        } else if (sele_language.equalsIgnoreCase("ગુજરાતી")) {
            session.setAppLanguage("gu");
            updateView(session.getAppLanguage());
        } else if (sele_language.equalsIgnoreCase("मराठी")) {
            session.setAppLanguage("mr");
            updateView(session.getAppLanguage());
        }

        if (methodIntent.equalsIgnoreCase("NewAppInstall")) {
            session.setAppInstall_1stTime("No");
        }
    }

    private void updateView(String lang) {
        Context context = LocaleHelper.setLocale(this, lang);
        Resources resources = context.getResources();
//        Toast.makeText(context, "App. language Updated", Toast.LENGTH_SHORT).show();
//        this.setContentView(R.layout.activity_login_main);
//        recreate();

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
        if (accesswifistate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (READ_SMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (RECEIVE_SMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if (SEND_SMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
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

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");


        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize our map
//                    initMap();
                }
            }
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
//        stopLockTask();
    }


}
