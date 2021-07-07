package com.ecosense.app.activity.supervisor;

import android.app.ProgressDialog;
import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.ecosense.app.adapter.FuelDetailAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Fuel;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class FuelDetail extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = FuelDetail.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;

    @BindView(R.id.srl_FuelDetail)
    SwipeRefreshLayout srl_FuelDetail;

    @BindView(R.id.rv_FuelDetail)
    RecyclerView rv_FuelDetail;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.tv_tot_vehicle_fill_fuel_count)
    TextView tv_tot_vehicle_fill_fuel_count;

    @BindView(R.id.tv_tot_fuel_qty)
    TextView tv_tot_fuel_qty;

    @BindView(R.id.tv_tot_fuel_amt)
    TextView tv_tot_fuel_amt;

    private GridLayoutManager lLayout;
    FuelDetailAdapter fuelDetailAdapter;
    List<Fuel> fillFuelVehicleList;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;

    private boolean isLoading = true;
    static String methodIntent = null;
    static Fuel fuelDetail;
    static double totFuelQty = 0.00;
    static double totFuelAmt = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_detail);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Fuel Detail");
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


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent mIntent = new Intent(getApplicationContext(), VehicleEntryForFuel.class); // the activity that holds the fragment
            Bundle mBundle = new Bundle();

            mBundle.putSerializable("fuelDetail", fuelDetail);
            mIntent.setAction("fuelPrice");
            mIntent.putExtra("SelectedNameDetail", mBundle);
            startActivity(mIntent);
            finish();
        });

                fillFuelVehicleList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_FuelDetail.setHasFixedSize(true);
        rv_FuelDetail.setLayoutManager(lLayout);
        rv_FuelDetail.setItemAnimator(new DefaultItemAnimator());
        fuelDetailAdapter = new FuelDetailAdapter(getApplicationContext(), fillFuelVehicleList);

        rv_FuelDetail.setAdapter(fuelDetailAdapter);

       /* rv_FuelDetail.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_FuelDetail, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Fuel updateFuelInfo = fillFuelVehicleList.get(position);
//                Log.e(TAG, " Name = " + fuelinfo.getName());
                Intent mIntent = new Intent(getApplicationContext(), VehicleEntryForFuel.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("updateFuelInfo", updateFuelInfo);
                b.putSerializable("fuelDetail", fuelDetail);
                mIntent.setAction("UpdateFuelQty");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
*/

        rv_FuelDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

                        getFuelDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_FuelDetail.setOnRefreshListener(this);
        srl_FuelDetail.setColorSchemeResources(R.color.colorAccent);
        srl_FuelDetail.setNestedScrollingEnabled(true);
        srl_FuelDetail.post(
                new Runnable() {
                    @Override
                    public void run() {
                        srl_FuelDetail.setRefreshing(true);
                        getFuelDetailOnServer(0);

                        srl_FuelDetail.setRefreshing(false);
                    }
                }
        );

        onNewIntent(getIntent());
    }

    @Override
    public void onRefresh() {
        srl_FuelDetail.setRefreshing(true);
//        dialog_wardNumberDetailConform();

        fillFuelVehicleList.clear();
        fuelDetailAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;
        totFuelQty = 0.00;
        totFuelAmt = 0.00;

        tv_tot_vehicle_fill_fuel_count.setText("0");
        tv_tot_fuel_amt.setText(String.format("%.2f", totFuelAmt));
        tv_tot_fuel_qty.setText(String.format("%.2f", totFuelQty));
        getFuelDetailOnServer(0);

        srl_FuelDetail.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }


    public void getFuelDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                totFuelQty = 0.00;
                totFuelAmt = 0.00;
                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

                    filters = "[[\"Fuel_Details\",\"supervisor_id\",\"like\",\""
                            + session.getpsNo()
                            + "\"],[\"Fuel_Details\",\"creation\",\"like\",\"" +
                            Connection.getCurrentDate() + "%"
                            + "\"]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/Fuel_Details?filters="
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
                                                Fuel fDetail = objectMapper.readValue(visitor.toString(), Fuel.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {

                                                    totFuelQty = totFuelQty + Double.parseDouble(fDetail.getQuantity());
                                                    totFuelAmt = totFuelAmt + Double.parseDouble(fDetail.getTotal());

                                                    fillFuelVehicleList.add(fDetail);
                                                    fuelDetailAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            tv_tot_vehicle_fill_fuel_count.setText(fillFuelVehicleList.size()+"");
                                            tv_tot_fuel_amt.setText(String.format("%.2f", totFuelAmt));
                                            tv_tot_fuel_qty.setText(String.format("%.2f", totFuelQty));
                                            Log.e(TAG, "metaDataList length = " + fillFuelVehicleList.size());
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
            if (methodIntent != null && methodIntent.equals("fuelPrice")) {
                fuelDetail = (Fuel) extras.getSerializable("fuelDetail");
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.supervisor_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            finish();
            startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
            return true;
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
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), FuelPriceUpdate.class));
    }
}