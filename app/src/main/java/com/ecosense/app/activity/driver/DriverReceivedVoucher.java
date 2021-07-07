package com.ecosense.app.activity.driver;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.DatePicker;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.supervisor.GenerateVoucher;
import com.ecosense.app.adapter.DriverReceivedVoucherAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;
import com.ecosense.app.pojo.Voucher;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class DriverReceivedVoucher extends AppCompatActivity implements ItemClickListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = DriverReceivedVoucher.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;


    @BindView(R.id.srl_voucherDetail)
    SwipeRefreshLayout srl_voucherDetail;

    @BindView(R.id.rv_voucherDetail)
    RecyclerView rv_voucherDetail;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.tiedt_date)
    TextInputEditText tiedt_date;

    @BindView(R.id.btn_voucher_submit)
    MaterialButton btn_voucher_submit;

    private GridLayoutManager lLayout;
    DriverReceivedVoucherAdapter myVoucherDetailAdapter;
    List<Voucher> voucherList;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;
    static RouteItem routeDetails = null;
    static Vehicle vehicleDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_received_voucher);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Voucher");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        filter_Date = Connection.getCurrentDate();
        tiedt_date.setText(Connection.getCurrentDate_dd_mm_yyyy());

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        voucherList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_voucherDetail.setHasFixedSize(true);
        rv_voucherDetail.setLayoutManager(lLayout);

        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        rv_voucherDetail.setLayoutAnimation(animation);

//        rv_voucherDetail.setItemAnimator(new DefaultItemAnimator());
        myVoucherDetailAdapter = new DriverReceivedVoucherAdapter(getApplicationContext(), voucherList);
        rv_voucherDetail.setAdapter(myVoucherDetailAdapter);
        myVoucherDetailAdapter.setClickListener(this);

        rv_voucherDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e(TAG, " onScrolled isLoading = " + isLoading + "  dy= " + dy);

                if (dy > 0) {


                    int visibleItemCount = lLayout.getChildCount();
                    int totalItemCount = lLayout.getItemCount();
                    int pastVisibleItemPosition = lLayout.findFirstVisibleItemPosition();

                    Log.e(TAG, "before isLoading isLoading = " + isLoading);
                    if (isLoading) {
                        if (totalItemCount > previous_Item_Total) {
                            isLoading = false;
                            previous_Item_Total = totalItemCount;
                            Log.e(TAG, "in (totalItemCount > previous_Item_Total) = " + isLoading + " \t previous_Item_Total = " + previous_Item_Total);
                        }
                    }
                    if (!isLoading && ((totalItemCount - visibleItemCount)
                            <= (pastVisibleItemPosition + view_thereshold))) {
//                    if ((visibleItemCount + pastVisibleItemPosition) >= totalItemCount && previous_Item_Total != totalItemCount) {
//                        previous_Item_Total = totalItemCount;
                        geVoucherDetailOnServer(totalItemCount, routeDetails.getVehicle_registration_no());
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });
        onNewIntent(getIntent());
        srl_voucherDetail.setOnRefreshListener(this);
        srl_voucherDetail.setColorSchemeResources(R.color.colorAccent);
        srl_voucherDetail.setNestedScrollingEnabled(true);
        srl_voucherDetail.post(
                () -> geVoucherDetailOnServer(0, routeDetails.getVehicle_registration_no())
        );
        btn_voucher_submit.setOnClickListener(this);
        tiedt_date.setOnClickListener(this);

    }

    @Override
    public void onRefresh() {
        srl_voucherDetail.setRefreshing(true);
//        dialog_wardNumberDetailConform();

        voucherList.clear();
//        voucherDetailAdapter.notifyDataSetChanged();
        runLayoutAnimation(rv_voucherDetail);
        previous_Item_Total = 0;
        isLoading = true;

        geVoucherDetailOnServer(0, routeDetails.getVehicle_registration_no());
        srl_voucherDetail.setRefreshing(false);

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
                    vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                    Log.e(TAG, "RouteItem.getName() " + routeDetails.getName());
                    Log.e(TAG, "RouteItem.getName() " + routeDetails.getDa_vehicleno());
//                Log.e(TAG, "RouteItem.getDa_routeid() " + routeDetails.getDa_routeid());
//                Log.e(TAG, "RouteItem.getR_status() " + routeDetails.getR_status());
                } else if (methodIntent != null && methodIntent.equals("SelectDriverUsedVoucher")) {
                    vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                }
            } else {
                Log.e(TAG, "Bundle Is empty ");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e.getMessage());
        }
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onClick(View v) {
        if (v == tiedt_date) {
            openDatePicker(tiedt_date);
        }
        if (v == btn_voucher_submit) {
            onRefresh();
//            geVoucherDetailOnServer(0);
        }
    }
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public String dateFormat(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "yyyy-MM-dd";

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

    @Override
    public void onClick(View view, int position) {
        try {
            Voucher voucherInfo = voucherList.get(position);
            if (view.getId() == R.id.btn_Used_Voucher) {
                Date date1 = formatter.parse(Connection.getCurrentDate());
                Date date2 = formatter.parse(dateFormat(voucherInfo.getCreation()));
                Log.e(TAG, "date1: " + date1);
                Log.e(TAG, "date2: " + date2);

                if (Objects.requireNonNull(date1).compareTo(date2) == 0) {
                    alertDialogFor_Use_Voucher(voucherInfo);
                } else {
                    alertDialogFor_DoNotApprovedRequest(voucherInfo, "DoNotUsedVoucher");
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());
        }
    }

    protected void alertDialogFor_DoNotApprovedRequest(Voucher vInfo, String actionName) {

        String msg = null;
        if (actionName.equalsIgnoreCase("DoNotUsedVoucher")) {
            msg = getString(R.string.alert_msg_DoNotUsedVoucher);
        }
        // do something when the button is clicked

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this,
                R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
//        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(msg)

                .setPositiveButton(getString(R.string.btn_ok), (arg0, arg1) -> {

                });
//
//                .setNegativeButton(getString(R.string.btn_no), (arg0, arg1) -> {
////                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
//                });
        materialAlertDialogBuilder.show();

    }

    String filter_Date = null;

    public void openDatePicker(TextInputEditText editTextDate) {
        final TextInputEditText edtDate = editTextDate;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String day = "";
                        if (dayOfMonth < 10)
                            day = "0" + dayOfMonth;
                        else
                            day = String.valueOf(dayOfMonth);


                        String month = "";
                        if ((monthOfYear + 1) < 10)
                            month = "0" + (monthOfYear + 1);
                        else
                            month = String.valueOf((monthOfYear + 1));


                        edtDate.setText(day + "-" + month + "-" + year);
                        filter_Date = year + "-" + month + "-" + day;
                        Log.e(TAG, "onDateSet: filter_Date Set :"+filter_Date );

                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }
    public void geVoucherDetailOnServer(int request_limit_start, String vehicle_number) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {


                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

                    filters = "[[\"Voucher\",\"vehicle_number\",\"like\",\""
                            + vehicle_number
                            + "\"],[\"Voucher\",\"creation\",\"like\",\"" +
                            filter_Date + "%"
                            + "\"],[\"Voucher\",\"status\",\"like\",\"" +
                            AppConfig.Active_Status
                            + "\"],[\"Voucher\",\"voucher_status\",\"in\",[\"" +
                            AppConfig.Voucher_status_Unused
                            + "\",\""
                            + AppConfig.Voucher_status_Pending
                            + "\"]]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/Voucher?filters="
                            + URLEncoder.encode(filters, "UTF-8")
                            + "&fields=" + fields
                            + "&limit_page_length=" + limit_page_length
                            + "&limit_start=" + limit_start;

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
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");

                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Voucher fDetail = objectMapper.readValue(visitor.toString(), Voucher.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {

                                                    voucherList.add(fDetail);
//                                                    voucherDetailAdapter.notifyDataSetChanged();
                                                    runLayoutAnimation(rv_voucherDetail);
                                                }
                                            }

                                            Log.e(TAG, "metaDataList length = " + voucherList.size());
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                        }

                                    } catch (IOException e) {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
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
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
                    Log.e(TAG, "getAssetsBinDetailOnServer call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }

    protected void alertDialogFor_Use_Voucher(Voucher vInfo) {
        String msg = getString(R.string.are_you_sure_use_voucher);

        // do something when the button is clicked
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.btn_yes), (arg0, arg1) -> {


                    Intent mIntent = new Intent(this, SimpleScannerActivity.class); // the activity that holds the fragment
                    Bundle b = new Bundle();
                    b.putSerializable("voucherInfo", vInfo);
                    b.putSerializable("vehicleDetails", vehicleDetails);
                    mIntent.setAction("SelectDriverUsedVoucher");
                    mIntent.putExtra("SelectedNameDetail", b);
                    startActivity(mIntent);
                    finish();

                })
                .setNegativeButton(getString(R.string.btn_no), (arg0, arg1) -> {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public void deleteVoucherRequest(String voucherID, String deleteStatus) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(DriverReceivedVoucher.this);
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

                        jsonObject.put("status", deleteStatus);

                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;

                    url = session.getMyServerIP() + "/api/resource/Voucher/" + URLEncoder.encode(voucherID, "UTF-8");
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

                                                    Voucher voDetail = objectMapper.readValue(statusData.toString(), Voucher.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }

                                                    Log.e(TAG, "Responce = " + voDetail.getName());
                                                    TastyToast.makeText(getApplicationContext(), "Successfully Voucher Deleted", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                    onRefresh();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.supervisor_home, menu);

        MenuItem homeMenuItem = menu.findItem(R.id.action_home);
        homeMenuItem.setVisible(false);


        MenuItem request_voucher = menu.findItem(R.id.action_request_voucher);
        request_voucher.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       //noinspection SimplifiableIfStatement
        if (id == R.id.action_request_voucher) {

            Intent mIntent = new Intent(getApplicationContext(), GenerateVoucher.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putSerializable("routeDetails", routeDetails);
            b.putSerializable("vehicleDetails", vehicleDetails);
            mIntent.setAction("DriverGenerateVoucher");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    // Showing the status in Snackbar
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
    public void onBackPressed() {
//        super.onBackPressed();
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




