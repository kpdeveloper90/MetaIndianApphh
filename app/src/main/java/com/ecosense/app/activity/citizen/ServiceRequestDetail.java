package com.ecosense.app.activity.citizen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.supervisor.AssignComplaints;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;

public class ServiceRequestDetail extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = ServiceRequestDetail.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;

    @BindView(R.id.img_comdtl_btn_submit)
    ImageView img_comdtl_btn_submit;

    @BindView(R.id.tv_est_name)
    TextView tv_est_name;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_detail);

        ButterKnife.bind(this);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Service Request Detail");
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
            } else if (methodIntent != null && methodIntent.equals("AssignServiceRequest")) {
                selectComplaintsData = (Complaints) extras.getSerializable("ComplaintsData");
                if (selectComplaintsData.getCptstatus().equalsIgnoreCase(AppConfig.CPTSTATUS_New)) {
                    btn_assign_complain.setVisibility(View.VISIBLE);
                    btn_assign_complain.setText(R.string.btn_assign_sr);
                }
                setIntentData();
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }
    private void setIntentData() {

        img_comdtl_btn_submit.setVisibility(View.GONE);

        tv_est_name.setText(getString(R.string.establishment_name)+selectComplaintsData.getEsta_name());
        tv_com_detail_psname.setText(selectComplaintsData.getCptpname());
        tv_com_detail_email.setText(selectComplaintsData.getCptemail());
        tv_com_detail_date.setText(dateFormat(selectComplaintsData.getCptdate()));
        tv_com_detail_location.setText(selectComplaintsData.getCptloc());
        tv_com_detail_description.setText(selectComplaintsData.getCptdescription());
        tv_com_detail_type_of_loc.setText(selectComplaintsData.getCptloctype());
        tv_com_detail_type.setText(selectComplaintsData.getCpttype());
        tv_com_detail_mobileNo.setText(selectComplaintsData.getCptmobileno());
        tv_com_detail_wardNumber.setText(getString(R.string.ward_no)+selectComplaintsData.getCptward_no());
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

    }

    @Override
    public void onClick(View v) {

        if (v == btn_assign_complain) {
            finish();
            Intent mIntent = new Intent(getApplicationContext(), AssignComplaints.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("ComplaintsData", selectComplaintsData);
            mIntent.setAction("AssignCorporateServiceRequest");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
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
}
