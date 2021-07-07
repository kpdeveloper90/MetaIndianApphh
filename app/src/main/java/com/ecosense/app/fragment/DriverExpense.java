package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.ecosense.app.adapter.MyExpenseDeatilAdapter;
import com.ecosense.app.adapter.MyExpenseListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Expense;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverExpense extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = DriverExpense.class.getSimpleName();

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;

    UserSessionManger session = null;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.srl_driver_Expense_Detail)
    SwipeRefreshLayout srl_driver_Expense_Detail;

    @BindView(R.id.rv_driver_Expense_Detail)
    RecyclerView rv_driver_Expense_Detail;

    private GridLayoutManager lLayout;
    static private MyExpenseListAdapter myExpenseListAdapter = null;
    static private List<Expense> expenseList = null;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;


    public DriverExpense() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_driver_expense, container, false);
        ButterKnife.bind(this, view);

        // Inflate the layout for this fragment


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");
        setHasOptionsMenu(true);
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getActivity(),  getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        expenseList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_driver_Expense_Detail.setHasFixedSize(true);
        rv_driver_Expense_Detail.setLayoutManager(lLayout);
        rv_driver_Expense_Detail.setItemAnimator(new DefaultItemAnimator());
        myExpenseListAdapter = new MyExpenseListAdapter(getActivity(), expenseList, TAG);
        rv_driver_Expense_Detail.setAdapter(myExpenseListAdapter);
        rv_driver_Expense_Detail.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), rv_driver_Expense_Detail, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Expense expenseInfo = expenseList.get(position);
                showExpenseDetail_Dialog(expenseInfo);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        rv_driver_Expense_Detail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

                        getExpenseOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });

        srl_driver_Expense_Detail.setOnRefreshListener(this);
        srl_driver_Expense_Detail.setColorSchemeResources(R.color.colorAccent);
        srl_driver_Expense_Detail.setNestedScrollingEnabled(true);

        srl_driver_Expense_Detail.post(
                new Runnable() {
                    @Override
                    public void run() {
                        previous_Item_Total = 0;
                        isLoading = true;
                        getExpenseOnServer(0);
                        srl_driver_Expense_Detail.setRefreshing(false);

                        srl_driver_Expense_Detail.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public void onRefresh() {
        srl_driver_Expense_Detail.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {
            expenseList.clear();
            myExpenseListAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getExpenseOnServer(0);
        } catch (Exception e) {
            TastyToast.makeText(getActivity(),  getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_driver_Expense_Detail.setRefreshing(false);
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
        dialog_expense_detail = new Dialog(getActivity());
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
        lLayout_eDetail = new GridLayoutManager(getActivity(), 1);
        rv_Expense_Detail.setHasFixedSize(true);
        rv_Expense_Detail.setLayoutManager(lLayout_eDetail);
        rv_Expense_Detail.setItemAnimator(new DefaultItemAnimator());
        e_detailListAdapter = new MyExpenseDeatilAdapter(getActivity(), e_detailList,
                "ExpenseDetail");
        rv_Expense_Detail.setAdapter(e_detailListAdapter);
        if (expenseItem.getApproval_status().equalsIgnoreCase(AppConfig.Expense_Status_Draft)) {
            im_btn_reject.setVisibility(View.VISIBLE);
            im_btn_approved.setVisibility(View.VISIBLE);
        } else {
            im_btn_reject.setVisibility(View.GONE);
            im_btn_approved.setVisibility(View.GONE);
        }
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

        im_btn_approved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogFor_Expense_Action(AppConfig.Expense_Status_Approved, expenseItem.getName());
            }
        });

        im_btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogFor_Expense_Action(AppConfig.Expense_Status_Reject, expenseItem.getName());
            }
        });


        dialog_expense_detail.show();
    }


    private void alertDialogFor_Expense_Action(String action_status, String e_id) {

        String msg = null;

        if (action_status.equalsIgnoreCase("Approved")) {
            msg = "Are you sure Approved Expense ?";
        } else if (action_status.equalsIgnoreCase("Reject")) {
            msg = "Are you sure Reject Expense ?";
        }
        // do something when the button is clicked
        AlertDialog alertbox = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Alert")
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage(msg)
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    if (action_status.equalsIgnoreCase("Approved")) {
                        updateExpenseRequestServer(AppConfig.Expense_Status_Approved, e_id);
                    } else if (action_status.equalsIgnoreCase("Reject")) {
                        updateExpenseRequestServer(AppConfig.Expense_Status_Reject, e_id);
                    } else {
                        dialog_expense_detail.dismiss();
                    }
                })
                .setNegativeButton("No", (arg0, arg1) -> {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onClick(View v) {

    }


    private void getExpenseDetailOnServer(String e_Id) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_LONG, TastyToast.ERROR);

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

                                            tv_total_amount.setText("Total Amount : " + eDetail.getTotal_claimed_amount());
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
                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar_eDetail != null) {
                            progressBar_eDetail.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    public void getExpenseOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_LONG, TastyToast.ERROR);

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
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    public void updateExpenseRequestServer(String e_status, String e_id) {
        try {
            mProgressDialog = new ProgressDialog(getActivity());
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

                    jsonObject.put("approval_status", e_status);


                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;

                url = session.getMyServerIP() + "/api/resource/Expense%20Claim/" + URLEncoder.encode(e_id, "UTF-8");
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

                                                Expense compDetail = objectMapper.readValue(statusData.toString(), Expense.class);
                                                Log.e(TAG, "Responce getComId= " + compDetail.getName());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                dialog_expense_detail.dismiss();
                                                onRefresh();
                                            } else {
                                                TastyToast.makeText(getActivity(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                        TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

}
