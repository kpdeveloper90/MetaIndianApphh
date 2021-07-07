package com.ecosense.app.activity.supervisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.button.MaterialButton;
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
import com.ecosense.app.activity.citizen.ServiceRequestDetail;
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.adapter.ServiceRequestReaddressedAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;
import com.ecosense.app.pojo.Complaints;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class ServiceRequestReaddressed extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ServiceRequestReaddressed.class.getSimpleName();
    private Toolbar toolbar;
    @BindView(R.id.srl_service_req_Detail)
    SwipeRefreshLayout srl_service_req_Detail;

    @BindView(R.id.sp_assets_wardNumber)
    Spinner sp_assets_wardNumber;
    @BindView(R.id.btn_assets_submit)
    MaterialButton btn_assets_submit;
    @BindView(R.id.rv_service_req_Detail)
    RecyclerView rv_service_req_Detail;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    private GridLayoutManager lLayout;
    ServiceRequestReaddressedAdapter serviceRequestReaddressedAdapter;
    List<Complaints> serviceRequestList;

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;
    ArrayList<String> wardNoList = null;
    DropDownStringAdapter adapter_wardNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_readdressed);

        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Service Request");
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


        serviceRequestList = new ArrayList<>();
        lLayout = new GridLayoutManager(this, 1);
        rv_service_req_Detail.setHasFixedSize(true);
        rv_service_req_Detail.setLayoutManager(lLayout);
        rv_service_req_Detail.setItemAnimator(new DefaultItemAnimator());
        serviceRequestReaddressedAdapter = new ServiceRequestReaddressedAdapter(this, serviceRequestList);
        rv_service_req_Detail.setAdapter(serviceRequestReaddressedAdapter);

        rv_service_req_Detail.addOnItemTouchListener(new RecyclerItemClickListener(this, rv_service_req_Detail, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Complaints comItem = serviceRequestList.get(position);

                Log.e(TAG, " Name = " + comItem.getComId());

                Intent mIntent = new Intent(getApplicationContext(), ServiceRequestDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", comItem);
                mIntent.setAction("AssignServiceRequest");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));


        rv_service_req_Detail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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


                        String wardNo = sp_assets_wardNumber.getSelectedItem().toString();

                        getComplaintsDetailOnServer(wardNo, totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });
        wardNoList = new ArrayList<>();
        adapter_wardNo = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, wardNoList);
        getwardNoListOnServer();
        sp_assets_wardNumber.setAdapter(adapter_wardNo);


        srl_service_req_Detail.setOnRefreshListener(this);
        srl_service_req_Detail.setColorSchemeResources(R.color.colorAccent);
        srl_service_req_Detail.setNestedScrollingEnabled(true);

        btn_assets_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {


                    String wardNo = sp_assets_wardNumber.getSelectedItem().toString();

                    serviceRequestList.clear();
                    serviceRequestReaddressedAdapter.notifyDataSetChanged();
                    previous_Item_Total = 0;
                    isLoading = true;

                    getComplaintsDetailOnServer(wardNo, 0);
                } catch (Exception e) {
                    TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                }
            }
        });
//        srl_empRegDetail.post(
//                () -> getComplaintsDetailOnServer()
//        );
    }

    @Override
    public void onRefresh() {

        srl_service_req_Detail.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {
            serviceRequestList.clear();
            serviceRequestReaddressedAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;
            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
            getComplaintsDetailOnServer(wardNo, 0);
        } catch (Exception e) {
            TastyToast.makeText(this, getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_service_req_Detail.setRefreshing(false);
    }

    private void getComplaintsDetailOnServer(String ward_No, int request_limit_start) {
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


                    filters = "[[\"Complaints\",\"user_type\",\"in\",[\""
                            + AppConfig.USubType_Corporate
                            + "\"]],[\"Complaints\",\"cptward_no\",\"like\",\""
                            + ward_No
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
                                                    serviceRequestList.add(cDetail);
                                                    serviceRequestReaddressedAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);
                                            }
//                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                    Log.e(TAG, "complaints Detail call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }

    public void getwardNoListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(ServiceRequestReaddressed.this);
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

    EditText txtSearch;
    private SearchView searchView = null;
    MenuItem searchItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_search, menu);

        searchItem = menu.findItem(R.id.action_filter_search);
        searchItem.setVisible(true);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);

        }

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_baseline_close_24);

//        txtSearch.setHighlightColor(ContextCompat.getColor(this, R.color.read_linearLayoutBg));
//        txtSearch.setAllCaps(true);
        txtSearch = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHint(getString(R.string.search));
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.tab_text));
        txtSearch.setTextColor(ContextCompat.getColor(this, R.color.white));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Complaints> filteredModelList = filter(serviceRequestList, query);
        serviceRequestReaddressedAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Complaints> filter(List<Complaints> models, String query) {
        query = query.toLowerCase();

        final List<Complaints> filteredModelList = new ArrayList<>();
        for (Complaints model : models) {
            final String text = model.getComId().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
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
        try {

            serviceRequestList.clear();
            serviceRequestReaddressedAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;
            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();

            getComplaintsDetailOnServer(wardNo, 0);
        } catch (Exception e) {
            Log.e(TAG, "Something is wrong Exception onResume = " + e.getMessage());
        }
        // register connection status listener
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
    }

}
