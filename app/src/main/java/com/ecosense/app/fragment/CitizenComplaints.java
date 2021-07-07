package com.ecosense.app.fragment;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.ComplaintsDetail;
import com.ecosense.app.adapter.CompReaddressedAdapter;
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;
import com.ecosense.app.pojo.Complaints;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class CitizenComplaints extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = CitizenComplaints.class.getSimpleName();

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;

    UserSessionManger session = null;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    @BindView(R.id.srl_citizen_Complaints)
    SwipeRefreshLayout srl_citizen_Complaints;

    @BindView(R.id.rv_citizen_Complaints)
    RecyclerView rv_citizen_Complaints;

    private GridLayoutManager lLayout;
    private CompReaddressedAdapter compReaddressedAdapter = null;
    private List<Complaints> complaintsList = null;

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;
    ArrayList<String> wardNoList = null;
    DropDownStringAdapter adapter_wardNo = null;

    //    @BindView(R.id.sp_assets_wardNumber)
//    Spinner sp_assets_wardNumber;
    @BindView(R.id.btn_assets_submit)
    MaterialButton btn_assets_submit;

    public CitizenComplaints() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_citizen_complaints, container, false);
        ButterKnife.bind(this, view);
        session = new UserSessionManger(getActivity());
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
            TastyToast.makeText(getActivity(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        complaintsList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_citizen_Complaints.setHasFixedSize(true);
        rv_citizen_Complaints.setLayoutManager(lLayout);
        rv_citizen_Complaints.setItemAnimator(new DefaultItemAnimator());
        compReaddressedAdapter = new CompReaddressedAdapter(getActivity(), complaintsList, TAG);

        rv_citizen_Complaints.setAdapter(compReaddressedAdapter);

        rv_citizen_Complaints.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), rv_citizen_Complaints, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Complaints comItem = complaintsList.get(position);

                Log.e(TAG, " Name = " + comItem.getComId());

                Intent mIntent = new Intent(getActivity(), ComplaintsDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", comItem);
                mIntent.setAction("AssignCitizenComplaints");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);

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

                        getComplaintsDetailOnServer(filter_Ward_NO, filter_Date, filter_CptStatus, totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });
        wardNoList = new ArrayList<>();
        adapter_wardNo = new DropDownStringAdapter(getActivity(), R.layout.custom_dropdown_list_row, R.id.tv_name, wardNoList);
//        getwardNoListOnServer();
//        sp_assets_wardNumber.setAdapter(adapter_wardNo);


        srl_citizen_Complaints.setOnRefreshListener(this);
        srl_citizen_Complaints.setColorSchemeResources(R.color.colorAccent);
        srl_citizen_Complaints.setNestedScrollingEnabled(true);
        srl_citizen_Complaints.post(
                new Runnable() {
                    @Override
                    public void run() {
//                        previous_Item_Total = 0;
//                        isLoading = true;
//                        getComplaintsDetailOnServer(0);
                    }
                }
        );
//        btn_assets_submit.setOnClickListener(this::onClick);
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
            filter_CptStatus = "NA";
            filter_Date = "NA";
            filter_Ward_NO = "Select";
//            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
            getComplaintsDetailOnServer("Select", "NA", "NA", 0);
        } catch (Exception e) {
            TastyToast.makeText(getActivity(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_citizen_Complaints.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
//        if (v == btn_assets_submit) {
//
//            complaintsList.clear();
//            compReaddressedAdapter.notifyDataSetChanged();
//            previous_Item_Total = 0;
//            isLoading = true;
//
//            String wardNo = sp_assets_wardNumber.getSelectedItem().toString();
//
//            getComplaintsDetailOnServer(wardNo, 0);
//        }
    }

    public void getComplaintsDetailOnServer(String ward_No, String cptDate, String CptStatus, int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {

                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

//                    filters = "[[\"Complaints\",\"cptuserid\",\"like\",\""
//                            + "REG00000010"
////                            + session.getpsNo()
//                            + "\"],[\"Complaints\",\"cptactstatuts\",\"in\",[\""
//                            + AppConfig.Complaints_Status_Active
//                            + "\"]]]";
                    filters = "[[\"Complaints\",\"user_type\",\"in\",[\""
                            + AppConfig.USubType_Individual
                            + "\"]],[\"Complaints\",\"cptactstatuts\",\"in\",[\""
                            + AppConfig.Complaints_Status_Active
                            + "\"]],";
                    if (CptStatus.equalsIgnoreCase("NA")) {
                        filters = filters + "[\"Complaints\",\"cptstatus\",\"in\",[\"New\",\"Pending\",\"In-Process\"]]";

                    } else {
                        filters = filters + "[\"Complaints\",\"cptstatus\",\"in\",[\""
                                + CptStatus
                                + "\"]]";
                    }

                    if (!ward_No.equalsIgnoreCase("Select")) {
                        filters = filters + ",[\"Complaints\",\"cptward_no\",\"like\",\""
                                + ward_No
                                + "\"]";
                    }
                    if (!cptDate.equalsIgnoreCase("NA")) {
                        filters = filters + ",[\"Complaints\",\"cptdate\",\"like\",\""
                                + cptDate
                                + "%\"]";
                    }

                    filters = filters + "]";

//                    filters = "[[\"Complaints\",\"user_type\",\"in\",[\""
//                            + AppConfig.USubType_Individual
//                            + "\"]],[\"Complaints\",\"cptward_no\",\"like\",\""
//                            + ward_No
//                            + "\"],[\"Complaints\",\"cptactstatuts\",\"in\",[\""
//                            + AppConfig.Complaints_Status_Active
//                            + "\"]]]";


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
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            // error
                            Log.e("Error.Response", error.toString());
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);

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
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
                    Log.e(TAG, "getAssetsBinDetailOnServer call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }


    public void getwardNoListOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(getActivity());
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
                                            wardNoList.add("Select");
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
                                            showBottomSheet();
                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    ImageView img_closeBtn;
    BottomSheetDialog bottomSheetDialog;
    BottomSheetBehavior bottomSheetBehavior;
    View bottomSheetView;
    MaterialButtonToggleGroup toggle_btn_Complaint_Status_group;
    MaterialButton btn_btn_filter_apply;
    TextInputEditText tiedt_date;
    Spinner sp_wardNumber;
    private int mYear;
    private int mMonth;
    private int mDay;
    static String filter_CptStatus = "NA";
    static String filter_Date = "NA";
    static String filter_Ward_NO = "Select";

    public void showBottomSheet() {
        try {
            bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_complaint_filter, null);
            bottomSheetDialog = new BottomSheetDialog(getActivity());
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setCancelable(false);
            bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

            btn_btn_filter_apply = bottomSheetDialog.findViewById(R.id.btn_btn_filter_apply);

            img_closeBtn = bottomSheetDialog.findViewById(R.id.img_closeBtn);
            toggle_btn_Complaint_Status_group = bottomSheetDialog.findViewById(R.id.toggle_btn_Complaint_Status_group);
            tiedt_date = bottomSheetDialog.findViewById(R.id.tiedt_date);
            sp_wardNumber = bottomSheetDialog.findViewById(R.id.sp_wardNumber);

            btn_btn_filter_apply.setEnabled(true);

            sp_wardNumber.setAdapter(adapter_wardNo);
            filter_Date = "NA";
            filter_CptStatus = "NA";
            filter_Ward_NO = "Select";

            tiedt_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openDatePicker(tiedt_date);
                    btn_btn_filter_apply.setEnabled(true);
                }
            });
            img_closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bottomSheetDialog.dismiss();
                }
            });
            btn_btn_filter_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (filter_POIStatus != null && !filter_POIStatus.equalsIgnoreCase("")) {
//                        Log.e(TAG, "filter_POIStatus filter = " + filter_POIStatus);
//                    }
//                    if (tiedt_date.getText().toString() != null) {

                    Log.e(TAG, "tiedt_date filter = " + filter_Date);
                    Log.e(TAG, "filter_POIStatus filter = " + filter_CptStatus);
//
//
//                    }

                    try {
                        complaintsList.clear();
                        compReaddressedAdapter.notifyDataSetChanged();

                        previous_Item_Total = 0;
                        isLoading = true;

                        filter_Ward_NO = sp_wardNumber.getSelectedItem().toString();


                        previous_Item_Total = 0;
                        isLoading = true;

                        getComplaintsDetailOnServer(filter_Ward_NO, filter_Date, filter_CptStatus, 0);
                        bottomSheetDialog.dismiss();

//                        filter_POIStatus = "NA";
//                        filter_Date = "NA";
                    } catch (Exception e) {
                        TastyToast.makeText(getActivity(), getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                    }
                }
            });
            toggle_btn_Complaint_Status_group.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

                if (toggle_btn_Complaint_Status_group.getCheckedButtonId() == R.id.mtbtn_new) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_CptStatus = AppConfig.CPTSTATUS_New;
                } else if (toggle_btn_Complaint_Status_group.getCheckedButtonId() == R.id.mtbtn_in_progress) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_CptStatus = AppConfig.CPTSTATUS_In_Process;
                } else if (toggle_btn_Complaint_Status_group.getCheckedButtonId() == R.id.mtbtn_Complete) {
                    btn_btn_filter_apply.setEnabled(true);
                    filter_CptStatus = AppConfig.CPTSTATUS_Complete;
                }

            });
            bottomSheetDialog.show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception e) {
            Log.e(TAG, "In showBottomSheet error = > " + e.getMessage());

            Log.e(TAG, "In showBottomSheet Exception flagBottomSheetDialog =0");
            e.printStackTrace();
        }
    }

    public void openDatePicker(TextInputEditText editTextDate) {
        final TextInputEditText edtDate = editTextDate;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
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

                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            Log.e(TAG, "BottomSheetBehavior COLLAPSED");
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            Log.e(TAG, "BottomSheetBehavior DRAGGING");
//                            bottomSheetDialog.dismiss();
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            Log.e(TAG, "BottomSheetBehavior EXPANDED");
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            Log.e(TAG, "BottomSheetBehavior HIDDEN");

                            Log.e(TAG, "In BottomSheetBehavior HIDDEN flagBottomSheetDialog =0");
                            bottomSheetDialog.dismiss();
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            Log.e(TAG, "BottomSheetBehavior SETTLING");
                            break;
                        default:
                            Log.e(TAG, "BottomSheetBehavior unknown");
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            };

    private SearchView searchView = null;
    EditText txtSearch;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.supervisor_home, menu);

        MenuItem homeMenuItem = menu.findItem(R.id.action_home);
        homeMenuItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_filter_search);
        searchItem.setVisible(true);

        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);
        filterMenuItem.setVisible(true);


        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);

            searchView.setMaxWidth(Integer.MAX_VALUE);

            ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
            closeButton.setImageResource(R.drawable.ic_baseline_close_24);

//        txtSearch.setHighlightColor(ContextCompat.getColor(this, R.color.read_linearLayoutBg));
//        txtSearch.setAllCaps(true);
            txtSearch = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
            txtSearch.setHint(getString(R.string.search_by_complaints_no));
            txtSearch.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.tab_text));
            txtSearch.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            txtSearch.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                getwardNoListOnServer();
                return false;
            default:
                break;
        }
//        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Complaints> filteredModelList = filter(complaintsList, query);
        compReaddressedAdapter.setFilter(filteredModelList);
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

        try {
            complaintsList.clear();
            compReaddressedAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getComplaintsDetailOnServer(filter_Ward_NO, filter_Date, filter_CptStatus, 0);
        } catch (Exception e) {
            Log.e(TAG, "Something is wrong Exception onResume = " + e.getMessage());
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach => " + TAG);
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

