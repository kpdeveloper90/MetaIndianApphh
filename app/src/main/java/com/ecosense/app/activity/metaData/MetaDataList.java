package com.ecosense.app.activity.metaData;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;


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
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.adapter.MetaDataListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.Text_to_Speech;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class MetaDataList extends AppCompatActivity implements ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = MetaDataList.class.getSimpleName();
    private Toolbar toolbar;
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;


    @BindView(R.id.srl_metaData)
    SwipeRefreshLayout srl_metaData;

    @BindView(R.id.rv_metaData)
    RecyclerView rv_metaData;


    @BindView(R.id.ll_list)
    RelativeLayout ll_list;

    private GridLayoutManager lLayout;
    MetaDataListAdapter metaDataListAdapter;
    List<Assets> metaDataList;

    @BindView(R.id.sp_assets_wardNumber)
    Spinner sp_assets_wardNumber;

    @BindView(R.id.sp_assets_routeNumber)
    Spinner sp_assets_routeNumber;

    @BindView(R.id.btn_metaData_submit)
    Button btn_metaData_submit;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;
    Text_to_Speech tts = null;
    ArrayList<String> wardNoList = null;
    DropDownStringAdapter adapter_wardNo = null;

    ArrayList<String> routeNoList = null;
    DropDownStringAdapter adapter_routeNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_data_list);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MetaData List");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
//        setSpinnerData(sp_assets_wardNumber, R.array.wardNumber);
//        setSpinnerData(sp_assets_routeNumber, R.array.RouteNumber);

        wardNoList = new ArrayList<>();
        adapter_wardNo = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, wardNoList);

        routeNoList = new ArrayList<>();
        adapter_routeNo = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, routeNoList);


        getwardNoListOnServer();
        sp_assets_wardNumber.setAdapter(adapter_wardNo);

        metaDataList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_metaData.setHasFixedSize(true);
        rv_metaData.setLayoutManager(lLayout);
        rv_metaData.setItemAnimator(new DefaultItemAnimator());
        metaDataListAdapter = new MetaDataListAdapter(getApplicationContext(), metaDataList, TAG);
        rv_metaData.setAdapter(metaDataListAdapter);
        metaDataListAdapter.setClickListener(this);

        sp_assets_wardNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String wno = parent.getItemAtPosition(position).toString();
                //

                metaDataList.clear();
                metaDataListAdapter.notifyDataSetChanged();

                Log.e(TAG, "WNo = " + wno);
                getrouteNoListOnServer(wno);
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_assets_routeNumber.setAdapter(adapter_routeNo);
        sp_assets_routeNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                metaDataList.clear();
                metaDataListAdapter.notifyDataSetChanged();

            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tts = new Text_to_Speech(this);
//        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(this, R.array.wardNumber, android.R.layout.simple_spinner_item);
//        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_assets_wardNumber.setAdapter(adapter_wardNumber);



//        rv_metaData.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_metaData, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Assets trackers = metaDataList.get(position);
////
////                Log.e(TAG, " Name = " + empRegItem.getPsno() + "  getPsname = " + empRegItem.getPsname());
//
//
//                finish();
//                Intent mIntent = new Intent(getApplicationContext(), CurrentLocationMark.class); // the activity that holds the fragment
//                Bundle mBundle = new Bundle();
//
//                mBundle.putSerializable("metaDataItem", trackers);
//                mIntent.setAction("UpdateLatLong");
//                mIntent.putExtra("SelectedNameDetail", mBundle);
//                startActivity(mIntent);
//
//            }
//
//            @Override
//            public void onItemLongClick(View view, int position) {
//            }
//        }));

        rv_metaData.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e(TAG, " onScrolled isLoading = " + isLoading + "  dy= " + dy);

                if (dy > 0) {


                    int visibleItemCount = lLayout.getChildCount();
                    int totalItemCount = lLayout.getItemCount();
                    int pastVisibleItemPosition = lLayout.findFirstVisibleItemPosition();
                    Log.e(TAG, "visibleItemCount = " + visibleItemCount);
                    Log.e(TAG, "totalItemCount = " + totalItemCount);
                    Log.e(TAG, "pastVisibleItemPosition = " + pastVisibleItemPosition);
//                    Log.e(TAG, "(visibleItemCount + pastVisibleItemPosition) = " + (visibleItemCount + pastVisibleItemPosition));
                    Log.e(TAG, "preLastVisible = " + previous_Item_Total);
                    Log.e(TAG, "(totalItemCount - visibleItemCount) = " + (totalItemCount - visibleItemCount));
                    Log.e(TAG, "(pastVisibleItemPosition + view_thereshold) = " + (pastVisibleItemPosition + view_thereshold));

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

                        String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
                        String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

                        getAssetsBinDetailOnServer(wardNo, routeNo, totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_metaData.setOnRefreshListener(this);
        srl_metaData.setColorSchemeResources(R.color.colorAccent);
        srl_metaData.setNestedScrollingEnabled(true);
        srl_metaData.post(
                new Runnable() {
                    @Override
                    public void run() {
//                        getAssetsBinDetailOnServer(sp_assets_wardNumber.getSelectedItem().toString());
//                        getAssetsBinDetailOnServer();
                    }
                }
        );
        btn_metaData_submit.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    static String methodIntent = null;
    static Assets metaDataItem;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//        Intent intent = getIntent();
        methodIntent = intent.getAction();
        Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
        Log.e(TAG, "onNewIntent" + intent.getAction());
        if (extras != null) {
//            methodIntent = intent.getAction();
            if (methodIntent != null && methodIntent.equals("UpdateLatLong")) {
                metaDataItem = (Assets) extras.getSerializable("metaDataItem");
                try {
                    Log.e(TAG, "metaDataItem wno = " + metaDataItem.getFawardno());
                    Log.e(TAG, "metaDataItem getFarouteno = " + metaDataItem.getFarouteno());
//                    setWardNumber_RouteNo(sp_assets_wardNumber, R.array.wardNumber, metaDataItem.getFawardno());
//                    setWardNumber_RouteNo(sp_assets_routeNumber, R.array.RouteNumber, metaDataItem.getFarouteno());
//                    isLoading = true;
//                    String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
//                    String routeNo = sp_assets_routeNumber.getSelectedItem().toString();
//                    getAssetsBinDetailOnServer(metaDataItem.getFawardno(), metaDataItem.getFarouteno(), 0);
                } catch (Exception e) {
                    TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }


    @Override
    public void onRefresh() {
        srl_metaData.setRefreshing(true);
//        dialog_wardNumberDetailConform();

        metaDataList.clear();
        metaDataListAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;
        String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
        String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

        getAssetsBinDetailOnServer(wardNo, routeNo, 0);

        srl_metaData.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_metaData_submit) {

            metaDataList.clear();
            metaDataListAdapter.notifyDataSetChanged();

            previous_Item_Total = 0;
            isLoading = true;
            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
            String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

            getAssetsBinDetailOnServer(wardNo, routeNo, 0);
        }
    }

    @Override
    public void onClick(View view, int position) {
        try {
            Assets trackers = metaDataList.get(position);
            if (view.getId() == R.id.cl_contain) {
                finish();
                Intent mIntent = new Intent(getApplicationContext(), CurrentLocationMark.class); // the activity that holds the fragment
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("metaDataItem", trackers);
                mIntent.setAction("UpdateLatLong");
                mIntent.putExtra("SelectedNameDetail", mBundle);
                startActivity(mIntent);
            } else if (view.getId() == R.id.img_metadata_play_text) {

                if (trackers.getFalocation() != null) {
                    tts.speakOut(trackers.getFalocation());
                } else {
                    tts.speakOut(trackers.getAsset_name());
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());

        }
    }

    public void getwardNoListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(MetaDataList.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_ward_no + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
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
                                        JsonNode statusData = rootNode.path("message");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {
                                            wardNoList.clear();
                                            adapter_wardNo.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Assets fuelDetails = objectMapper.readValue(visitor.toString(), Assets.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    wardNoList.add(fuelDetails.getFawardno());
                                                    adapter_wardNo.notifyDataSetChanged();
                                                    Log.d(TAG, "Data Available");
                                                }
                                            }
                                            if (methodIntent.equalsIgnoreCase("UpdateLatLong")) {
                                                String wName = metaDataItem.getFawardno();
                                                if (!wName.equals(null)) {
                                                    int spinnerPosition = adapter_wardNo.getPosition(wName);
                                                    sp_assets_wardNumber.setSelection(spinnerPosition);
                                                }

                                            }
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

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
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
//                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    public void getrouteNoListOnServer(String wardNO) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(MetaDataList.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;
                    routeNoList.clear();
                    adapter_routeNo.notifyDataSetChanged();
                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_route_info + "?id=" + URLEncoder.encode(wardNO, "UTF-8");
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
                                        JsonNode statusData = rootNode.path("message");
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Assets fuelDetails = objectMapper.readValue(visitor.toString(), Assets.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    routeNoList.add(fuelDetails.getR_name());
                                                    adapter_routeNo.notifyDataSetChanged();
                                                }
                                            }
                                            if (methodIntent.equalsIgnoreCase("UpdateLatLong")) {
                                                String rName = metaDataItem.getFarouteno();
                                                if (!rName.equals(null)) {
                                                    int spinnerPosition = adapter_routeNo.getPosition(rName);
                                                    sp_assets_routeNumber.setSelection(spinnerPosition);

                                                    previous_Item_Total = 0;
                                                    isLoading = true;
                                                    String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
                                                    String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

                                                    getAssetsBinDetailOnServer(wardNo, routeNo, 0);
                                                }

                                            }
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

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
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
//                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
    }

    public void getAssetsBinDetailOnServer(String wardNumber, String routeNO, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {


                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

//                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

//                String rDate_time = dateFormat.format(new Date());
//                // convert date to calendar
//                Calendar c = Calendar.getInstance();
//                c.setTime(new Date());
//                c.add(Calendar.DATE, 1);
//                // convert calendar to date
//                Date currentDatePlusOne = c.getTime();
//                String addOneDay = dateFormat.format(currentDatePlusOne);
//                Log.e(TAG, "CurrentDate  = " + rDate_time
//                        + "\t addOneDay = " + addOneDay);
//


                    filters = "[[\"Asset\",\"item_code\",\"in\",[\"BIN\",\"Open Spot\",\"Door To Door\"]],"
                            + "[\"Asset\",\"ward_no\",\"in\",[\""
                            + wardNumber
                            + "\"]],[\"Asset\",\"route_info\",\"in\",[\""
                            + routeNO
                            + "\"]],[\"Asset\",\"status\",\"in\",[\""
                            + AppConfig.BIN_Status_Submitted
                            + "\"]]]";

//                    filters = "[[\"Item\",\"ward_no\",\"in\",[\""
//                            + wardNumber
//                            + "\"]],[\"Item\",\"route_no\",\"in\",[\"" +
//                            routeNO
//                            + "\"]]]";


                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;

                    limit_start = request_limit_start;
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" +
                            limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/Asset?filters=" +
                            URLEncoder.encode(filters, "UTF-8")
                            + "&fields=" + fields
                            + "&limit_page_length=" + limit_page_length
                            + "&limit_start=" + limit_start;
//                    String url = session.getMyServerIP() + "/api/resource/Facility_And_Assets?fields=" + fields;

//                String url = session.getMyServerIP() + "/api/resource/News?fields=" + URLEncoder.encode(fields, "UTF-8");
                    Log.e(TAG, "url" + url);
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
                                                Assets assetsDetail = objectMapper.readValue(visitor.toString(), Assets.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + assetsDetail.getAsset_Id());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    metaDataList.add(assetsDetail);
                                                    metaDataListAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            Log.e(TAG, "metaDataList length = " + metaDataList.size());
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    public void setSpinnerData(Spinner spname, int arrayId) {
        ArrayAdapter<CharSequence> adapter_wardNumber = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNumber);

    }

    public void setWardNumber_RouteNo(Spinner spname, int arrayId, String locType) {
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spname.setAdapter(adapter_wardNo);
        if (!locType.equals(null)) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            spname.setSelection(spinnerPosition);
        }
    }

    EditText txtSearch;
    private SearchView searchView = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_search, menu);


//        // Get the search menu.
        MenuItem searchItem = menu.findItem(R.id.action_filter_search);
        MenuItem action_filter_add_metaData = menu.findItem(R.id.action_filter_add_metaData);

        searchItem.setVisible(true);
//        action_filter_add_metaData.setVisible(true);
//        // Get SearchView object.
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
//        return true;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            assert searchManager != null;
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        // Associate searchable configuration with the SearchView


        searchView.setMaxWidth(Integer.MAX_VALUE);

//
//        searchItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
//
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                // Do something when collapsed
//                metaDataListAdapter.setFilter(metaDataList);
//                return true; // Return true to collapse action view
//
//            }
//        });

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_baseline_close_24);

//        txtSearch.setHighlightColor(ContextCompat.getColor(this, R.color.read_linearLayoutBg));
//        txtSearch.setAllCaps(true);
        txtSearch = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHint(getString(R.string.search_by_location));
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.tab_text));
        txtSearch.setTextColor(ContextCompat.getColor(this, R.color.white));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_filter_add_metaData:
                Assets trackers = new Assets();
                String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
                String routeNo = sp_assets_routeNumber.getSelectedItem().toString();

                trackers.setFarouteno(routeNo);
                trackers.setFawardno(wardNo);

                finish();
                Intent mIntent = new Intent(getApplicationContext(), CurrentLocationMark.class); // the activity that holds the fragment
                Bundle mBundle = new Bundle();

                mBundle.putSerializable("metaDataItem", trackers);
                mIntent.setAction("NewLatLong");
                mIntent.putExtra("SelectedNameDetail", mBundle);
                startActivity(mIntent);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // filter recycler view when query submitted
//        metaDataListAdapter.getFilter().filter(query);

        final List<Assets> filteredModelList = filter(metaDataList, query);
        metaDataListAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Assets> filter(List<Assets> models, String query) {
        query = query.toLowerCase();
        final List<Assets> filteredModelList = new ArrayList<>();
        for (Assets model : models) {
            final String text;
            if (model.getFalocation() != null) {
                text = model.getFalocation().toLowerCase();
            } else {
                text = "";
            }
            final String text2 = model.getAsset_name().toLowerCase();


            if (text.contains(query) || text2.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
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
        tts.destroy_tts_Speech();
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
        startActivity(new Intent(getApplicationContext(), MetaDataDashBoard.class));
        finish();
    }
}
