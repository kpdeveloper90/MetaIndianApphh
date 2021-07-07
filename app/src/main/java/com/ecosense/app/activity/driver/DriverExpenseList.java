package com.ecosense.app.activity.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.ecosense.app.activity.supervisor.GenerateExpense;
import com.ecosense.app.adapter.MyExpenseDeatilAdapter;
import com.ecosense.app.adapter.MyExpenseListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Expense;
import com.ecosense.app.pojo.RouteItem;
import com.ecosense.app.pojo.Vehicle;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class DriverExpenseList extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = DriverExpenseList.class.getSimpleName();

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;

    UserSessionManger session = null;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.srl_My_Expense_Detail)
    SwipeRefreshLayout srl_My_Expense_Detail;

    @BindView(R.id.rv_My_Expense_Detail)
    RecyclerView rv_My_Expense_Detail;

    private GridLayoutManager lLayout;

    static private MyExpenseListAdapter myExpenseListAdapter=null;

    static private List<Expense> expenseList=null;
    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    static String methodIntent = null;
    static RouteItem routeDetails;
    private Toolbar toolbar;
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
        setContentView(R.layout.activity_driver_expense_list);


        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Expense");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        ButterKnife.bind(this);

        // Inflate the layout for this fragment

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {

            Intent mIntent = new Intent(getApplicationContext(), GenerateExpense.class); // the activity that holds the fragment
//            Bundle b = new Bundle();
//            b.putSerializable("ComplaintsData", comItem);
            mIntent.setAction("DriverExpanseList");
//            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        });

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        expenseList = new ArrayList<>();
        lLayout = new GridLayoutManager(getApplicationContext(), 1);
        rv_My_Expense_Detail.setHasFixedSize(true);
        rv_My_Expense_Detail.setLayoutManager(lLayout);
        rv_My_Expense_Detail.setItemAnimator(new DefaultItemAnimator());
        myExpenseListAdapter = new MyExpenseListAdapter(getApplicationContext(), expenseList,TAG);
        rv_My_Expense_Detail.setAdapter(myExpenseListAdapter);
        rv_My_Expense_Detail.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), rv_My_Expense_Detail, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Expense expenseInfo = expenseList.get(position);
                showExpenseDetail_Dialog(expenseInfo);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));


        rv_My_Expense_Detail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

                        getExpenseDetailOnServer( totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_My_Expense_Detail.setOnRefreshListener(this);
        srl_My_Expense_Detail.setColorSchemeResources(R.color.colorAccent);
        srl_My_Expense_Detail.setNestedScrollingEnabled(true);
        srl_My_Expense_Detail.post(
                new Runnable() {
                    @Override
                    public void run() {
//                        previous_Item_Total = 0;
//                        isLoading = true;
//                        getExpenseDetailOnServer(0);
                    }
                }
        );
        onNewIntent(getIntent());
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
            if (methodIntent != null && methodIntent.equals("pendingRoute")) {
                routeDetails = (RouteItem) extras.getSerializable("pendingRouteDetails");
                vehicleDetails = (Vehicle) extras.getSerializable("vehicleDetails");
                Log.e(TAG, "RouteItem.getName() " + routeDetails.getName());
//                Log.e(TAG, "RouteItem.getDa_routeid() " + routeDetails.getDa_routeid());
//                Log.e(TAG, "RouteItem.getR_status() " + routeDetails.getR_status());
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }
    }

    @Override
    public void onRefresh() {
        srl_My_Expense_Detail.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {
            expenseList.clear();
            myExpenseListAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getExpenseDetailOnServer(0);
        } catch (Exception e) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_My_Expense_Detail.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }
    static Dialog dialog_expense_detail;
    ImageView dialog_close;

    SwipeRefreshLayout srl_Expense_Detail;
    static RecyclerView rv_Expense_Detail;
    static RelativeLayout progressBar_eDetail;
    TextView tv_dialog_title;

    private GridLayoutManager lLayout_eDetail;
    MyExpenseDeatilAdapter e_detailListAdapter = null;
    List<Expense> e_detailList = null;
    MaterialButton im_btn_reject, im_btn_approved;
    TextView tv_total_amount;

    private void showExpenseDetail_Dialog(Expense expenseItem) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_expense_detail_action, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_expense_detail = new Dialog(DriverExpenseList.this);
        dialog_expense_detail.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_expense_detail.setContentView(root);
        dialog_expense_detail.setCancelable(true);
        Objects.requireNonNull(dialog_expense_detail.getWindow()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_expense_detail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        tv_dialog_title = dialog_expense_detail.findViewById(R.id.tv_dialog_title);
        dialog_close = dialog_expense_detail.findViewById(R.id.dialog_close);

        srl_Expense_Detail = dialog_expense_detail.findViewById(R.id.srl_Expense_Detail);
        rv_Expense_Detail = dialog_expense_detail.findViewById(R.id.rv_Expense_Detail);
        progressBar_eDetail = dialog_expense_detail.findViewById(R.id.rltv_progressBar);
        tv_total_amount = dialog_expense_detail.findViewById(R.id.tv_total_amount);

        im_btn_reject = dialog_expense_detail.findViewById(R.id.im_btn_reject);
        im_btn_approved = dialog_expense_detail.findViewById(R.id.im_btn_approved);

        tv_dialog_title.setText("Expense Detail");

        e_detailList = new ArrayList<>();
        lLayout_eDetail = new GridLayoutManager(getApplicationContext(), 1);
        rv_Expense_Detail.setHasFixedSize(true);
        rv_Expense_Detail.setLayoutManager(lLayout_eDetail);
        rv_Expense_Detail.setItemAnimator(new DefaultItemAnimator());
        e_detailListAdapter = new MyExpenseDeatilAdapter(getApplicationContext(), e_detailList,
                "ExpenseDetail");
        rv_Expense_Detail.setAdapter(e_detailListAdapter);

        im_btn_reject.setVisibility(View.GONE);
        im_btn_approved.setVisibility(View.GONE);


        getExpenseDetailOnServer(expenseItem.getName());


        srl_Expense_Detail.setColorSchemeResources(R.color.colorAccent);
        srl_Expense_Detail.setNestedScrollingEnabled(true);

        srl_Expense_Detail.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {

                srl_Expense_Detail.setRefreshing(true);

                e_detailList.clear();
                e_detailListAdapter.notifyDataSetChanged();

                getExpenseDetailOnServer(expenseItem.getName());

                srl_Expense_Detail.setRefreshing(false);
            }
//                }, 2000);
//            }
        });
        dialog_close.setOnClickListener(v -> {
            dialog_expense_detail.dismiss();
        });


        dialog_expense_detail.show();
    }


    public void getExpenseDetailOnServer( int request_limit_start) {
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

                    filters = "[[\"Expense Claim\",\"employee\",\"like\",\""
                            + session.getpsNo()
                            + "\"]]";

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields + "&limit_page_length=" + limit_page_length + "&limit_start=" + limit_start);

                    String url = session.getMyServerIP() + "/api/resource/Expense%20Claim?filters="
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
                                                Expense eDetail = objectMapper.readValue(visitor.toString(), Expense.class);
                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + eDetail.getName());
                                                Log.e(TAG, "visitor. = " + visitor.toString());
                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
//
//                                                    if (eDetail.getExpenses().size() > 0) {
//                                                        for (Expense ex : eDetail.getExpenses()) {
//                                                            ex.setApproval_status(eDetail.getApproval_status());
//                                                            ex.setEmployee_name(eDetail.getEmployee_name());
                                                    expenseList.add(eDetail);
                                                    myExpenseListAdapter.notifyDataSetChanged();
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


    private void getExpenseDetailOnServer(String e_Id) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar_eDetail.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                try {

                    String fields = null;

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

                    Log.e(TAG, "fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/Expense%20Claim/" + e_Id + "?fields="
                            + fields;

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
//                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Expense eDetail = objectMapper.readValue(statusData.toString(), Expense.class);
//                                            Log.e(TAG, "assetsDetail.getAsset_Id() = " + eDetail.getName());
//                                            Log.e(TAG, "visitor. = " + statusData.toString());
                                            if (eDetail.getExpenses().size() > 0) {
                                                for (Expense ex : eDetail.getExpenses()) {

                                                    ex.setCost_center(eDetail.getCost_center());
                                                    ex.setTitle(eDetail.getTitle());
                                                    ex.setEmployee(eDetail.getEmployee());
                                                    ex.setEmployee_name(eDetail.getEmployee_name());
                                                    ex.setMobile_no(eDetail.getMobile_no());
                                                    ex.setApproval_status(eDetail.getApproval_status());
                                                    ex.setModified(eDetail.getModified());
                                                    ex.setTotal_claimed_amount(eDetail.getTotal_claimed_amount());

                                                    e_detailList.add(ex);
                                                    e_detailListAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            tv_total_amount.setText(getString(R.string.total_amount) + eDetail.getTotal_claimed_amount());
//                                            Iterator<JsonNode> elements1 = statusData.elements();

//                                            while (elements1.hasNext()) {
//                                                JsonNode visitor = elements1.next();
//                                                Expense eDetail = objectMapper.readValue(visitor.toString(), Expense.class);
//                                                Log.e(TAG, "assetsDetail.getAsset_Id() = " + eDetail.getName());
//                                                Log.e(TAG, "visitor. = " + visitor.toString());
//                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
////
//                                                    if (eDetail.getExpenses().size() > 0) {
//                                                        for (Expense ex : eDetail.getExpenses()) {
//                                                            eDetail.setExpense_type(ex.getExpense_type());
//                                                            eDetail.setAmount(ex.getAmount());
//
//                                                            e_detailList.add(eDetail);
//                                                            e_detailListAdapter.notifyDataSetChanged();
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                            Log.e(TAG, "getExpense_type. = " + e_detailList.get(0).getExpenses().get(0).getExpense_type());
                                            if (progressBar_eDetail != null) {
                                                progressBar_eDetail.setVisibility(View.GONE);
                                            }


                                        } else {
                                            if (progressBar_eDetail != null) {
                                                progressBar_eDetail.setVisibility(View.GONE);
                                            }
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        }

                                    } catch (IOException e) {
                                        if (progressBar_eDetail != null) {
                                            progressBar_eDetail.setVisibility(View.GONE);
                                        }
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (progressBar_eDetail != null) {
                                        progressBar_eDetail.setVisibility(View.GONE);
                                    }
                                    Log.e(TAG, "Response Error");
                                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar_eDetail != null) {
                            progressBar_eDetail.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    });
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
            message = "Back-Online";
            color = Color.WHITE;
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
//
//        Snackbar snackbar = Snackbar
//                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);
//
//        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(color);
//        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
        onRefresh();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
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
        Intent mIntent1 = new Intent(getApplicationContext(), RouteMapList.class); // the activity that holds the fragment
        Bundle b1 = new Bundle();
        b1.putSerializable("pendingRouteDetails", routeDetails);
        b1.putSerializable("vehicleDetails", vehicleDetails);
        mIntent1.setAction("pendingRoute");
        mIntent1.putExtra("SelectedVehicleDetail", b1);
        startActivity(mIntent1);
        finish();

    }

}
