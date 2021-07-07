package com.ecosense.app.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.Window;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.ComplaintsDetail;
import com.ecosense.app.activity.citizen.NewComplaints;
import com.ecosense.app.adapter.ComplaintsListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;
import com.ecosense.app.pojo.Reminder;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComplaintsListFragment extends Fragment implements ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ComplaintsListFragment.class.getSimpleName();

    @BindView(R.id.srl_empRegDetail)
    SwipeRefreshLayout srl_empRegDetail;

    @BindView(R.id.rv_empRegDetail)
    RecyclerView rv_empRegDetail;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;

    private GridLayoutManager lLayout;
    ComplaintsListAdapter complaintsListAdapter;
    List<Complaints> complaintsList;

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;

    public ComplaintsListFragment() {
        // Required empty public constructor
    }

    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_complaints_lis, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Activity");
        setHasOptionsMenu(true);

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG,  getString(R.string.you_are_offline));
            TastyToast.makeText(getActivity(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        complaintsList = new ArrayList<>();
        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_empRegDetail.setHasFixedSize(true);
        rv_empRegDetail.setLayoutManager(lLayout);
        rv_empRegDetail.setItemAnimator(new DefaultItemAnimator());
        complaintsListAdapter = new ComplaintsListAdapter(getActivity(), complaintsList);
        rv_empRegDetail.setAdapter(complaintsListAdapter);
        complaintsListAdapter.setClickListener(this);
        rv_empRegDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {


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

        srl_empRegDetail.setOnRefreshListener(this);
        srl_empRegDetail.setColorSchemeResources(R.color.colorAccent);
        srl_empRegDetail.setNestedScrollingEnabled(true);
//        srl_empRegDetail.post(
//                () -> getComplaintsDetailOnServer()
//        );
    }


    @Override
    public void onRefresh() {

        srl_empRegDetail.setRefreshing(true);
//        dialog_wardNumberDetailConform();
        try {

            complaintsList.clear();
            complaintsListAdapter.notifyDataSetChanged();
            previous_Item_Total = 0;
            isLoading = true;

            getComplaintsDetailOnServer(0);
        } catch (Exception e) {
            TastyToast.makeText(getActivity(),  getString(R.string.somthing_wrong), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        srl_empRegDetail.setRefreshing(false);
    }


    @Override
    public void onClick(View view, int position) {
        try {
            Complaints com = complaintsList.get(position);
            Log.e(TAG, "img_menu_option onClick " + position);
            if (view.getId() == R.id.rl_top) {
                //redirect todetail
                Intent mIntent = new Intent(getActivity(), ComplaintsDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", com);
                mIntent.setAction("SelectComplaints");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
            } else if (view.getId() == R.id.img_btn_reopen) {
                countComplaintsReopenDays(com);

//                getActivity().finish();
            } else if (view.getId() == R.id.img_btn_feedback) {
                dialog_Feedback(com);
            } else if (view.getId() == R.id.img_btn_edit) {
                Intent mIntent = new Intent(getActivity(), NewComplaints.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", com);
                mIntent.setAction("SelectComplaintsForUpdate");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
//                getActivity().finish();
            } else if (view.getId() == R.id.img_btn_delete) {
                alertdialogForDeleteComplaints(com.getComId(), AppConfig.Complaints_Status_Deactive);
            } else if (view.getId() == R.id.img_btn_reminder) {
                getRemidetCountDetailOnServer(com);
            }

//            if (view.getId() == R.id.img_menu_option) {
//                Log.e(TAG, "img_menu_option onClick " + com.getComplainNo());
//                showPopUpMenu(view, com);
//            }
        } catch (Exception e) {
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getStackTrace());
        }
    }

    public void countComplaintsReopenDays(Complaints com) {
//        String dateStart = "08/01/2019 09:29:58";
//        String dateStop = "08/12/2019 10:31:48";

        String dateStart = dateFormat(com.getModified());
        String dateStop = dateFormat(Connection.getCurrentDateTime());

        Log.e(TAG, "dateStart = " + dateStart);
        Log.e(TAG, "dateStop = " + dateStop);
        //HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            Log.e(TAG, diffDays + " days, ");
            Log.e(TAG, diffHours + " hours, ");
            Log.e(TAG, diffMinutes + " minutes, ");
            Log.e(TAG, diffSeconds + " seconds.");

            if (diffDays > 15) {
                Log.e(TAG, "true diffDays>15.");
                TastyToast.makeText(getActivity(), getString(R.string.alert_15days_to_reopen), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else {
                Log.e(TAG, "false diffDays<15.");
                Intent mIntent = new Intent(getActivity(), NewComplaints.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("ComplaintsData", com);
                mIntent.setAction("SelectComplaintsReopen");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String dateFormat(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "MM/dd/yyyy HH:mm:ss";

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
    public void onClick(View v) {
        if (v == tv_feedback_cancel) {
            dialog_feedback.dismiss();
        }

        if (v == tv_reminder_cancel) {
            dialog_reminder.dismiss();
        }

        if (v == alert_no2) {
            alert_dialog2.dismiss();
        }

        if (v == alert_yes) {
            alert_dialog1.dismiss();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        if (v == alert_no) {
            alert_dialog1.dismiss();
        }
    }


    public void getComplaintsDetailOnServer(int request_limit_start) {
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

//                    filters = URLEncoder.encode("[[\"Complaints\", \"cptuserid\", \"=\",\"", "UTF-8")
//                            + session.getpsNo()
//                            + URLEncoder.encode("\"],[\"Complaints\", \"cptactstatuts\", \"=\",\"", "UTF-8")
//                            + AppConfig.Complaints_Status_Active
//                            + URLEncoder.encode("\"],[\"Complaints\", \"cptmobileno\", \"=\",\"", "UTF-8")
//                            + session.getMobileNumber()
//                            + URLEncoder.encode("\"]]", "UTF-8");

                    filters = "[[\"Complaints\",\"cptmobileno\",\"=\",\""
                            + session.getMobileNumber()
                            + "\"],[\"Complaints\",\"cptuserid\",\"like\",\""
                            + session.getpsNo()
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
                                                    complaintsList.add(cDetail);
                                                    complaintsListAdapter.notifyDataSetChanged();
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
                    Log.e(TAG, "complaints Detail call finally");
                }
            }, PROGRASS_postDelayed);
        }
    }

    private SearchView searchView = null;
    MenuItem searchItem;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        searchItem = menu.findItem(R.id.action_filter_search);
        searchItem.setVisible(true);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);

//            queryTextListener = new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    Log.i("onQueryTextChange", newText);
//
//                    return true;
//                }
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    Log.i("onQueryTextSubmit", query);
//
//                    return true;
//                }
//            };
//            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Not implemented here
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
        complaintsListAdapter.setFilter(filteredModelList);
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


    Dialog dialog_feedback;
    TextView tv_feedback_comp_no, tv_feedback_submit, tv_feedback_cancel;
    SmileRating feedback_ratingView;


    public void dialog_Feedback(Complaints complaints) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_feedback, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_feedback = new Dialog(getActivity());
        dialog_feedback.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_feedback.setContentView(root);
        dialog_feedback.setCancelable(false);
        dialog_feedback.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_feedback.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        tv_feedback_cancel = (TextView) dialog_feedback.findViewById(R.id.tv_feedback_cancel);
        tv_feedback_submit = (TextView) dialog_feedback.findViewById(R.id.tv_feedback_submit);

        tv_feedback_comp_no = (TextView) dialog_feedback.findViewById(R.id.tv_feedback_comp_no);
        tv_feedback_comp_no.setText(complaints.getComId());

        feedback_ratingView = (SmileRating) dialog_feedback.findViewById(R.id.feedback_ratingView);


        tv_feedback_cancel.setOnClickListener(this);
        tv_feedback_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String feedback = null;
                @BaseRating.Smiley int smiley = feedback_ratingView.getSelectedSmile();
                switch (smiley) {
                    case SmileRating.BAD:
//                    Log.e(TAG, "Bad");
                        feedback = "Bed";
                        break;
                    case SmileRating.GOOD:
//                    Log.e(TAG, "Good");
                        feedback = "Good";
                        break;
                    case SmileRating.GREAT:
//                    Log.e(TAG, "Great");
                        feedback = "Great";
                        break;
                    case SmileRating.OKAY:
//                    Log.e(TAG, "Okay");
                        feedback = "Okay";
                        break;
                    case SmileRating.TERRIBLE:
//                    Log.e(TAG, "Terrible");
                        feedback = "Terrible";
                        break;
                    case SmileRating.NONE:
//                    Log.e(TAG, "None");
                        feedback = "None";
                        break;
                }
//            TastyToast.makeText(getActivity(), "Thank you for Feedback", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
//            dialog_feedback.dismiss();

                Complaints cmp = new Complaints();
                String rDate_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                cmp.setCptfeedbk_date(rDate_time);
                cmp.setCptfeedback(feedback);
                cmp.setComId(complaints.getComId());
                sendComp_FeedbackRequest(cmp);
            }

        });

        dialog_feedback.show();
    }

    public void sendComp_FeedbackRequest(Complaints compFeedback) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Please Wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");


                    String url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(compFeedback.getComId(), "UTF-8");
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);

                                            Log.e(TAG, "Responce = " + compDetail.getComId() + "\t feedback = " + compDetail.getCptfeedback());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getActivity(), "Thank you for Feedback", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                            dialog_feedback.dismiss();


                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getActivity(), getString(R.string.could_no_send_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                            dialog_feedback.dismiss();
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
                                    show_alert_Dialog_singlebutton(getString(R.string.response_error));
                                }

                            }, error -> {
                        // error

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        show_alert_Dialog_singlebutton(getString(R.string.network_error));

                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
//                            Gson gson = new Gson();
//                            String regpojo = gson.toJson(compFeedback);
//                            Log.e(TAG, "getParams   = " + regpojo);
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject();

                                jsonObject.put("cptfeedbk_date", Connection.getCurrentDateTime());
                                jsonObject.put("cptfeedback", compFeedback.getCptfeedback());

                                params.put("data", jsonObject.toString());
                            } catch (JSONException e) {
                                Log.e("JSONObject Here", e.toString());
                            }

                            return params;
                        }

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
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }

    }

    public void sendComp_ReminderRequest(Reminder reminder) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Please Wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");


//                    String url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(compFeedback.getComId(), "UTF-8");
                    String url = session.getMyServerIP() + "/api/resource/Reminder";
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
//                                    Log.e("Response", response);
                                    if (!response.equalsIgnoreCase("{}")) {
                                        try {
                                            //create ObjectMapper instance
                                            ObjectMapper objectMapper = new ObjectMapper();
                                            JsonNode rootNode = objectMapper.readTree(response.toString());
                                            JsonNode statusData = rootNode.path("data");
//                                            Log.e(TAG, "statusData = " + statusData.toString());

                                            if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                                Reminder rem = objectMapper.readValue(statusData.toString(), Reminder.class);

                                                Log.e(TAG, "Responce = " + rem.getRemId());

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                TastyToast.makeText(getActivity(), "Reminder send Successfully", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                dialog_reminder.dismiss();
                                            } else {
                                                TastyToast.makeText(getActivity(), getString(R.string.could_no_send_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                        }
                                    } else {
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;
                                        }
                                        show_alert_Dialog_singlebutton(getString(R.string.response_error));
                                    }
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    Log.e(TAG, "Response Error");
                                    show_alert_Dialog_singlebutton(getString(R.string.response_error));
                                }

                            }, error -> {
                        // error

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        show_alert_Dialog_singlebutton(getString(R.string.network_error));
                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
//                            Gson gson = new Gson();
//                            String regpojo = gson.toJson(reminder);
//                            Log.e(TAG, "getParams   = " + regpojo);

                            JSONObject jsonObject = null;

                            try {
                                jsonObject = new JSONObject();
                                jsonObject.put("rdatetime", Connection.getCurrentDateTime());
                                jsonObject.put("ecomplaintsno", reminder.getEcomplaintsno());
                                jsonObject.put("rusernmae", reminder.getRusernmae());
                                jsonObject.put("ruserid", reminder.getRuserid());
                                jsonObject.put("rdesc", reminder.getRdesc());
                            } catch (JSONException e) {
                                Log.e("JSONObject Here", e.toString());
                            }
                            params.put("data", jsonObject.toString());
                            return params;
                        }

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
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }

    }

    public void getRemidetCountDetailOnServer(Complaints complaints) {
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
//                JSONObject jsonObject = null;
//
//                try {
//                    jsonObject = new JSONObject();
//                    jsonObject.put("clean_status", status);
//                    jsonObject.put("route_binphoto", binImage);
//                } catch (JSONException e) {
//                    Log.e("JSONObject Here", e.toString());
//                }

                String url = null;

                url = session.getMyServerIP() + "/api/method/erpnext.accounts.doctype.account.account.getRemCount?cptno=" + URLEncoder.encode(complaints.getComId(), "UTF-8");
//            Log.e(TAG, url);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {


                                        JSONArray messageArray = response.getJSONArray("message");
//                                    Log.e(TAG, "JSONArray message  = " + messageArray.get(0).toString());
                                        if (messageArray != null) {
                                            JSONArray m1 = (JSONArray) messageArray.get(0);

                                            Log.e(TAG, "JSONArray m1 like = " + m1.get(0));
//                                    Log.e(TAG, "JSONArray m1  dis like= " + m1.get(1));
                                            complaints.setCptRemCount(m1.get(0).toString());
                                        } else {
                                            complaints.setCptRemCount(0 + "");
                                        }
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;
                                        }
                                        dialog_Reminder(complaints);
                                    } else {
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;
                                        }
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
            }
        }, PROGRASS_postDelayed);
    }

    public void deleteComplaintsRequestServer(String comID, String deleteStatus) {
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

                URLEncoder.encode("", "UTF-8");

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(comID, "UTF-8");
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
                        new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
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

                                                Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);
                                                Log.e(TAG, "Responce getComId= " + compDetail.getComId());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                TastyToast.makeText(getActivity(), compDetail.getComId() + " is Successfully Delete", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
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
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
//                        Gson gson = new Gson();
//                        String regpojo = gson.toJson(userActivity);
//                        Log.e(TAG, "getParams   = " + regpojo);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject();
                            jsonObject.put("cptactstatuts", deleteStatus);
                            params.put("data", jsonObject.toString());
                        } catch (JSONException e) {
                            Log.e("JSONObject Here", e.toString());
                        }
                        return params;
                    }

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

    public void deleteComplaintsRequestServer_JsonObjectRequest(String comID, String deleteStatus) {
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
                    jsonObject.put("cptactstatuts", deleteStatus);


                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";
//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(comID, "UTF-8");
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

                                                Complaints compDetail = objectMapper.readValue(statusData.toString(), Complaints.class);
                                                Log.e(TAG, "Responce getComId= " + compDetail.getComId());
                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }
                                                TastyToast.makeText(getActivity(), compDetail.getComId() + " is Successfully Delete", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
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

    Dialog dialog_reminder;
    TextInputEditText edt_reminder_date, edt_reminder_note;
    TextView tv_reminder_comp_no, tv_reminderCount;
    TextView tv_reminder_submit, tv_reminder_cancel;


    public void dialog_Reminder(Complaints complaints) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_reminder, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog_reminder = new Dialog(getActivity());
        dialog_reminder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_reminder.setContentView(root);
        dialog_reminder.setCancelable(false);
        dialog_reminder.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        dialog_reminder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        tv_reminder_cancel = (TextView) dialog_reminder.findViewById(R.id.tv_reminder_cancel);
        tv_reminder_submit = (TextView) dialog_reminder.findViewById(R.id.tv_reminder_submit);
        tv_reminderCount = (TextView) dialog_reminder.findViewById(R.id.tv_reminderCount);

        edt_reminder_note = (TextInputEditText) dialog_reminder.findViewById(R.id.edt_reminder_note);

        tv_reminder_comp_no = (TextView) dialog_reminder.findViewById(R.id.tv_reminder_comp_no);
        tv_reminder_comp_no.setText(complaints.getComId());
        tv_reminderCount.setText(complaints.getCptRemCount());

        tv_reminder_cancel.setOnClickListener(this);
        tv_reminder_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Reminder rem = new Reminder();
                rem.setRdatetime(Connection.getCurrentDateTime());
                rem.setEcomplaintsno(complaints.getComId());
                rem.setRusernmae(session.getpsName());
                rem.setRuserid(session.getpsNo());
                rem.setRdesc(edt_reminder_note.getText().toString().trim());
                sendComp_ReminderRequest(rem);

            }
        });

        dialog_reminder.show();
    }

    protected void alertdialogForDeleteComplaints(String comID, String deleteStatus) {

        AlertDialog alertbox = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Alert")
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage("Are you sure delete complaint ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteComplaintsRequestServer(comID, deleteStatus);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    Dialog alert_dialog1, alert_dialog2;
    Button alert_yes, alert_no, alert_no2;
    TextView alert_msg, alert_msg2;

    public void show_alert_two_button(String msg) {


        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog1 = new Dialog(getActivity());
        alert_dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog1.setContentView(root);
        alert_dialog1.setCancelable(false);
        alert_dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_yes = (Button) alert_dialog1.findViewById(R.id.alert_close);
        alert_msg = (TextView) alert_dialog1.findViewById(R.id.alert_msg);
        alert_no = (Button) alert_dialog1.findViewById(R.id.alert_no);
        alert_yes.setText("Yes");
        alert_msg.setText(msg);
        alert_no.setVisibility(View.VISIBLE);


        alert_yes.setOnClickListener(this);
        alert_no.setOnClickListener(this);


        alert_dialog1.show();
    }

    public void show_alert_Dialog_singlebutton(String msg) {

        int counter = 0;
        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog2 = new Dialog(getActivity());
        alert_dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog2.setContentView(root);
        alert_dialog2.setCancelable(false);
        alert_dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_no2 = (Button) alert_dialog2.findViewById(R.id.alert_close);
        alert_msg2 = (TextView) alert_dialog2.findViewById(R.id.alert_msg);

        alert_msg2.setText(msg);


        alert_no2.setOnClickListener(this);


        alert_dialog2.show();
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
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message =getString(R.string.you_are_offline);
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        complaintsList.clear();
        complaintsListAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;

        getComplaintsDetailOnServer(0);
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
