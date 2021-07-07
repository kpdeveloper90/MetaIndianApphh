package com.ecosense.app.activity.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.CompReaddressedAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class MyAssignComplaintsList extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = MyAssignComplaintsList.class.getSimpleName();

    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.srl_citizen_Complaints)
    SwipeRefreshLayout srl_citizen_Complaints;

    @BindView(R.id.rv_citizen_Complaints)
    RecyclerView rv_citizen_Complaints;

    private GridLayoutManager lLayout;
    private CompReaddressedAdapter compReaddressedAdapter = null;
    private List<Complaints> complaintsList = null;
    Vehicle vehicleDetails=null;
    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_assign_complaints);


        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include9);
        toolbar.setTitle("My Assign Complaints");
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

        complaintsList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_citizen_Complaints.setHasFixedSize(true);
        rv_citizen_Complaints.setLayoutManager(lLayout);
        rv_citizen_Complaints.setItemAnimator(new DefaultItemAnimator());
        compReaddressedAdapter = new CompReaddressedAdapter(getApplicationContext(), complaintsList, TAG);

        rv_citizen_Complaints.setAdapter(compReaddressedAdapter);

        rv_citizen_Complaints.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_citizen_Complaints, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Complaints comItem = complaintsList.get(position);

                Log.e(TAG, " Name = " + comItem.getComId());

                Intent mIntent = new Intent(getApplicationContext(), UpdateBinDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", comItem);
                b.putSerializable("vehicleDetails", vehicleDetails);
                mIntent.setAction("AssignCitizenComplaints");
                mIntent.putExtra("SelectedVehicleDetail", b);
                startActivity(mIntent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        rv_citizen_Complaints.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

                        getComplaintsDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_citizen_Complaints.setOnRefreshListener(this);
        srl_citizen_Complaints.setColorSchemeResources(R.color.colorAccent);
        srl_citizen_Complaints.setNestedScrollingEnabled(true);
        srl_citizen_Complaints.post(
                new Runnable() {
                    @Override
                    public void run() {
                        previous_Item_Total = 0;
                        isLoading = true;
                        getComplaintsDetailOnServer(0);
                    }
                }
        );
        onNewIntent(getIntent());
    }

    @Override
    public void onRefresh() {
        srl_citizen_Complaints.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {
            complaintsList.clear();
            compReaddressedAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getComplaintsDetailOnServer(0);
        } catch (Exception e) {
            TastyToast.makeText(this, getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_citizen_Complaints.setRefreshing(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//
//        Intent intent = getIntent();
        Bundle extras = intent.getBundleExtra("SelectedVehicleDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
            methodIntent = intent.getAction();
            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);

            if (methodIntent != null && methodIntent.equals("getVehicleDetail")) {
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                Log.e(TAG, "vehicleDetails.getVehicle_registration_no() " + vehicleDetails.getVehicle_registration_no());
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }
    }
    @Override
    public void onClick(View v) {

    }

    public void getComplaintsDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(this, getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;


                    filters = "[[\"Complaints\",\"vehicle_no\",\"in\",[\""
                            + vehicleDetails.getVehicle_registration_no()
                            + "\"]],[\"Complaints\",\"driver_id\",\"like\",\""
                            + session.getpsNo()
                            + "\"],[\"Complaints\",\"driver_name\",\"like\",\""
                            + session.getpsName()
                            + "\"],[\"Complaints\",\"creation\",\"like\",\"" +
                            Connection.getCurrentDate() + "%"
                            + "\"],[\"Complaints\",\"cptactstatuts\",\"in\",[\""
                            + AppConfig.Complaints_Status_Active
                            + "\"]]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/Complaints?filters="
                            + URLEncoder.encode(filters, "UTF-8")
                            + "&fields=" + fields
                            + "&limit_page_length=" + limit_page_length
                            + "&limit_start=" + limit_start;

                    Log.e(TAG, "url = " + url);
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
                                                Complaints cDetail = objectMapper.readValue(visitor.toString(), Complaints.class);
                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + cDetail.getComId());
//                                                Log.e(TAG, "visitor. = " + visitor.toString());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
//
//                                                    if (eDetail.getExpenses().size() > 0) {
//                                                        for (Expense ex : eDetail.getExpenses()) {
//                                                            ex.setApproval_status(eDetail.getApproval_status());
//                                                            ex.setEmployee_name(eDetail.getEmployee_name());
                                                    complaintsList.add(cDetail);
                                                    compReaddressedAdapter.notifyDataSetChanged();
//                                                        }
//                                                    }
                                                }
                                            }
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }

                                        } else {

                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(this, getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                    TastyToast.makeText(this, getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(this, getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
    public void onBackPressed() {
//        super.onBackPressed();
        Intent mIntent = new Intent(getApplicationContext(), VehicleDetail.class);
        Bundle b = new Bundle();
        b.putSerializable("vehicleDetails", vehicleDetails);
        mIntent.setAction("getVehicleDetail");
        mIntent.putExtra("SelectedVehicleDetail", b);
        startActivity(mIntent);
        finish();

    }
}
