package com.ecosense.app.activity.citizen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.Qua_Ans_ListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Survey;
import com.ecosense.app.pojo.SurveyQA;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class TakeTheSurvey extends AppCompatActivity implements ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = TakeTheSurvey.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;

    @BindView(R.id.img_survey)
    CircularImageView img_survey;

    @BindView(R.id.tv_sur_title)
    TextView tv_sur_title;
    @BindView(R.id.rv_QADetail)
    RecyclerView rv_QADetail;


    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session = null;
    String methodIntent = null;
    Survey intentSurveyInfo = null;
    List<SurveyQA> surveyList = null;
    List<SurveyQA> AnssurveyList = null;
    Qua_Ans_ListAdapter qua_ans_listAdapter = null;
    LinearLayoutManager horizontalLayoutManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_the_survey);


        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.include4);
        toolbar.setTitle("RoutePoint");
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
            TastyToast.makeText(getApplicationContext(), "You're Offline", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        surveyList = new ArrayList<>();
        AnssurveyList = new ArrayList<>();
//        btn_next_Qu.setOnClickListener(this);
//        btn_Complete_Qu.setOnClickListener(this);

        rv_QADetail.setHasFixedSize(true);
//         horizontalLayoutManager = new LinearLayoutManager(TakeTheSurvey.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManager = new LinearLayoutManager(TakeTheSurvey.this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rv_QADetail.setLayoutManager(horizontalLayoutManager);
        rv_QADetail.setItemAnimator(new DefaultItemAnimator());
        qua_ans_listAdapter = new Qua_Ans_ListAdapter(this, surveyList, horizontalLayoutManager, rv_QADetail);
        rv_QADetail.setAdapter(qua_ans_listAdapter);
        qua_ans_listAdapter.setClickListener(this);
        onNewIntent(getIntent());
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

            if (methodIntent != null && methodIntent.equals("SurveySelect")) {
                intentSurveyInfo = (Survey) extras.getSerializable("SurveyInfo");
                Log.e(TAG, "intentPlaceInfo.getName() " + intentSurveyInfo.getSurID());

                if (intentSurveyInfo.getSurphoto() == null) {
                    Glide.with(getApplicationContext()).load(R.drawable.default_image)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_survey);
                } else {
                    String url = session.getMyServerIP() + intentSurveyInfo.getSurphoto();
//            Log.e("News Adapter ", "Url = " + session.getMyServerURL() + URLEncoder.encode(eventItem.getNwphoto()));

                    Glide.with(getApplicationContext()).load(url)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_survey);
                }
                tv_sur_title.setText(intentSurveyInfo.getSurtitle());

                getSurveyQADetailOnServer(intentSurveyInfo.getSurID());
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    @Override
    public void onClick(View v) {
//        if (v == btn_next_Qu) {
//
//        }
//        if (v == btn_Complete_Qu) {
//
//        }
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
            SurveyQA qua_Item = surveyList.get(position);
            String qaAnswer = null;

//            if (view.getId() == R.id.btn_next_Qu) {
//                Log.e(TAG, "onClick of getQAID= " + qua_Item.getQAID());
//
////                rv_QADetail.getLayoutManager().scrollToPosition(horizontalLayoutManager.findLastVisibleItemPosition() + 1);
////                AnssurveyList.add(qua_Item);
//            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());
        }
    }

    static String surveyId = "";

    public void getSurveyQADetailOnServer(String surID) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(TakeTheSurvey.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;


                    filters = URLEncoder.encode("[[\"RoutePoint\", \"name\", \"=\",\"", "UTF-8")
                            + surID
                            + URLEncoder.encode("\"]]", "UTF-8");

//
                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");
//
//                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

//                    String url = session.getMyServerIP() + "/api/resource/RoutePoint?filters=" + filters + "&fields=" + fields;
                    String url = session.getMyServerIP() + "/api/resource/RoutePoint/" + surID + "?&fields=" + fields;

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
                                        JsonNode statusData1 = rootNode.path("data");
                                        Survey surveyDetail = objectMapper.readValue(statusData1.toString(), Survey.class);
                                        surveyId = surveyDetail.getSurID();
                                        Log.e(TAG, "surveyId For QA = " + surveyId);
                                        JsonNode statusData = statusData1.path("surtable");

                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                SurveyQA surveyQADetails = objectMapper.readValue(visitor.toString(), SurveyQA.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    surveyQADetails.setAasurveyid(surveyId);
                                                    surveyList.add(surveyQADetails);
//                                                    Log.e(TAG, "surveyList = " + surveyDetails.getIdx());
                                                }
                                            }
                                            qua_ans_listAdapter.notifyDataSetChanged();
                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);

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
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
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
        alert_dialog1 = new Dialog(this);
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
        alert_dialog2 = new Dialog(this);
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
            message = "Back-Online";
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
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

    public void onBackPressed() {
//        super.onBackPressed();
        alertdialogForCancelSurvey();
    }

    protected void alertdialogForCancelSurvey() {

        AlertDialog alertbox = new AlertDialog.Builder(TakeTheSurvey.this)
                .setCancelable(false)
                .setTitle("Alert")
                .setIcon(getResources().getDrawable(R.drawable.ic_error_black_24dp))
                .setMessage("Are you sure cancel survey?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
//                        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
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

}
