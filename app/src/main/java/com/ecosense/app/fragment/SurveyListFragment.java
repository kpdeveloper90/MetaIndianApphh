package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;

import com.ecosense.app.activity.citizen.TakeTheSurvey;
import com.ecosense.app.adapter.SurveyListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Survey;
import com.ecosense.app.pojo.SurveyQA;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class SurveyListFragment extends Fragment implements ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = SurveyListFragment.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;
    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session = null;
    List<Survey> surveyList = null;


    // Store a member variable for the listener
    private int previous_Item_Total = 0;
    private int view_thereshold = 10;
    private boolean isLoading = true;

    @BindView(R.id.rltv_progressBar)
    RelativeLayout progressBar;
    @BindView(R.id.srl_surveyList)
    SwipeRefreshLayout srl_surveyList;

    @BindView(R.id.rv_surveyList)
    RecyclerView rv_surveyList;

    private GridLayoutManager lLayout;
    SurveyListAdapter surveyListAdapter = null;


    public SurveyListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_survey_list, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG,  getString(R.string.you_are_offline));
            TastyToast.makeText(getActivity(),  getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        surveyList = new ArrayList<>();

        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_surveyList.setHasFixedSize(true);
        rv_surveyList.setLayoutManager(lLayout);
        rv_surveyList.setItemAnimator(new DefaultItemAnimator());
        surveyListAdapter = new SurveyListAdapter(getActivity(), surveyList);
        rv_surveyList.setAdapter(surveyListAdapter);
        surveyListAdapter.setClickListener(this);

        rv_surveyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                        getSurveyDetailOnServer(totalItemCount);
                        isLoading = true;
                        Log.e(TAG, "in main if = " + isLoading);
                    }

                }
            }
        });


        srl_surveyList.setOnRefreshListener(this);
        srl_surveyList.setColorSchemeResources(R.color.colorAccent);
        srl_surveyList.setNestedScrollingEnabled(true);
        srl_surveyList.post(
                new Runnable() {
                    @Override
                    public void run() {
                        previous_Item_Total = 0;
                        isLoading = true;

                        getSurveyDetailOnServer(previous_Item_Total);
                    }
                }
        );
    }

    @Override
    public void onRefresh() {
        srl_surveyList.setRefreshing(true);
        surveyList.clear();
        surveyListAdapter.notifyDataSetChanged();
        previous_Item_Total = 0;
        isLoading = true;

        getSurveyDetailOnServer(previous_Item_Total);
        srl_surveyList.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

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

    @Override
    public void onClick(View view, int position) {
        try {
            Survey sur = surveyList.get(position);
            Log.e(TAG, "img_menu_option onClick " + position);
            if (view.getId() == R.id.img_btn_start_survey) {
                getUserSurveyDoOrNotOnServer(sur);
            }
        } catch (Exception e) {
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getStackTrace());
        }
    }

    public void getSurveyDetailOnServer(int request_limit_start) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG,  getString(R.string.no_internet_alert));
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

//            mProgressDialog = new ProgressDialog(getActivity());
//            mProgressDialog.setMessage("Please Wait...");
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.show();
            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;
                    int limit_page_length;
                    int limit_start;

                    filters = URLEncoder.encode("[[\"RoutePoint\", \"surstatus\", \"=\",\"", "UTF-8")
                            + AppConfig.CPTSTATUS_Pending
                            + URLEncoder.encode("\"]]", "UTF-8");
//
//
                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
                    limit_page_length = view_thereshold;
                    limit_start = request_limit_start;
//                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);
//
                    String url = session.getMyServerIP() + "/api/resource/RoutePoint?filters="
                            + filters + "&fields=" + fields
                            + "&limit_page_length=" + limit_page_length
                            + "&order_by=creation desc"
                            + "&limit_start=" + limit_start;

//                    String url = session.getMyServerIP() + "/api/resource/RoutePoint?&fields=" + fields;

//                    String url = session.getMyServerIP() + "/api/resource/RoutePoint?fields=" + URLEncoder.encode(fields, "UTF-8");
                    Log.e(TAG, url);
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Survey surveyDetails = objectMapper.readValue(visitor.toString(), Survey.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    surveyList.add(surveyDetails);
                                                    surveyListAdapter.notifyDataSetChanged();
                                                    Log.e(TAG, "surveyList = " + surveyList.size());
                                                }
                                            }
//                                        toilateLocatorAdapter.notifyDataSetChanged();
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
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
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
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, PROGRASS_postDelayed);
        }
    }

    public void getUserSurveyDoOrNotOnServer(Survey survey) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage( getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;


                    filters = URLEncoder.encode("[[\"SurveyActivity\", \"aasurveyid\", \"=\",\"", "UTF-8")
                            + survey.getSurID()
                            + URLEncoder.encode("\"],[\"SurveyActivity\", \"sauserid\", \"=\",\"", "UTF-8")
                            + session.getpsNo()
                            + URLEncoder.encode("\"]]", "UTF-8");

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
//
//                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);
//  String url = session.getMyServerIP() + "/api/resource/SurveyActivity";
                    String url = session.getMyServerIP() + "/api/resource/SurveyActivity?filters=" + filters + "&fields=" + fields;

                    Log.e(TAG, url);
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();
                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    SurveyQA surveyDetails = objectMapper.readValue(visitor.toString(), SurveyQA.class);
                                                    Log.e(TAG, "Data Available surveyId = " + surveyDetails.getAasurveyid());
                                                    TastyToast.makeText(getActivity(), getString(R.string.Survey_is_all_ready_taken_by_user), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
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
                                            Log.e(TAG, "User Not Taken survey = " + survey.getSurID());
                                            Intent mIntent = new Intent(getActivity(), TakeTheSurvey.class); // the activity that holds the fragment
                                            Bundle b = new Bundle();
                                            b.putSerializable("SurveyInfo", survey);
                                            mIntent.setAction("SurveySelect");
                                            mIntent.putExtra("SelectedNameDetail", b);
                                            startActivity(mIntent);
//                                            getActivity().finish();
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
                                    TastyToast.makeText(getActivity(), getActivity().getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                }
                            }, error -> {
                        // error
                        Log.e("Error.Response", error.toString());

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }

                        TastyToast.makeText(getActivity(), getActivity().getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                        mProgressDialog = null;
                    }

                }
            }, PROGRASS_postDelayed);
        }
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
            color = Color.WHITE;
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
//            message = "Sorry! Not connected to internet";
            message =getString(R.string.you_are_offline);
            color = Color.RED;
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
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
