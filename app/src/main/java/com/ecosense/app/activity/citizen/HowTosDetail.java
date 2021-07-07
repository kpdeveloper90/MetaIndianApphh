package com.ecosense.app.activity.citizen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.MessageAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.HowTos;
import com.ecosense.app.pojo.Message;

public class HowTosDetail extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = HowTosDetail.class.getSimpleName();
    @BindView(R.id.srl_How_tosDetail)
    SwipeRefreshLayout srl_How_tosDetail;

    @BindView(R.id.rv_How_tosDetail)
    RecyclerView rv_How_tosDetail;

    @BindView(R.id.tv_howto_title)
    TextView tv_howto_title;

    MessageAdapter messageAdapter;


    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List messageList = new ArrayList();

    Toolbar toolbar;
    Boolean isConnected = false;
    ProgressDialog mProgressDialog;
    UserSessionManger session;
    private SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_tos_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("How To's Detail");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        ButterKnife.bind(this);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(this, "You're Offline", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        rv_How_tosDetail.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rv_How_tosDetail.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(getBaseContext(), messageList);
        rv_How_tosDetail.setAdapter(mAdapter);
        rv_How_tosDetail.setItemAnimator(new DefaultItemAnimator());


//
//        searchView.setQueryHint("Search");
//        searchView.setOnQueryTextListener(this);
        srl_How_tosDetail.setOnRefreshListener(this);
        srl_How_tosDetail.setColorSchemeResources(R.color.colorAccent);
        srl_How_tosDetail.setNestedScrollingEnabled(true);


        onNewIntent(getIntent());
    }

    static String methodIntent = null;
    static String howTosId = null;

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

            if (methodIntent != null && methodIntent.equals("HowTosSelect")) {
                howTosId = null;
                howTosId = extras.getString("HowTos_id");
                Log.e(TAG, "howTosId => " + howTosId);
                srl_How_tosDetail.post(() -> getHowtosDetailOnServer(howTosId)
                );
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

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

    private void getHowtosDetailOnServer(String howtoId) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton(getString(R.string.no_internet_alert));
        } else {

            try {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {
                String filters = null;
                String fields = null;
                try {
                    filters = URLEncoder.encode("[[\"HOW_TOs\", \"name\", \"=\",\"", "UTF-8")
                            + howtoId
                            + URLEncoder.encode("\"]]", "UTF-8");

//                    fields = URLEncoder.encode("[\"visitor_email\"," +
//                            "\"visitor_name\"," +
//                            "\"visitor_contact_number" +
//                            "\"]", "UTF-8");

                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");


                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

//                String url = "http://203.109.125.77:7070/api/resource/Visitor_Management?filters=" + filters + "&fields=" + fields;
                String url = session.getMyServerIP() + "/api/resource/HOW_TOs?filters=" + filters + "&fields=" + fields;
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
                                    Log.e(TAG, "statusData = " + statusData.toString());
                                    if (!statusData.toString().trim().equalsIgnoreCase("[]")) {
                                        JsonNode arrayData = statusData.get(0);
                                        HowTos howTosDetails = objectMapper.readValue(arrayData.toString(), HowTos.class);

                                        if (howTosDetails != null) {
                                            Log.e(TAG, "visitorDetails = " + howTosDetails.getHttitle());
                                            fillData(howTosDetails);
                                        } else {
                                            show_alert_Dialog_singlebutton("No record found");
                                            show_alert_Dialog_singlebutton(getString(R.string.no_record_found_error));
                                        }
                                    } else {
                                        show_alert_Dialog_singlebutton(getString(R.string.no_record_found_error));
                                    }
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "Response Error");
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;

                                }
                                show_alert_Dialog_singlebutton(getString(R.string.response_error));
                            }
                        }, error -> {
                    // error
                    Log.e("Error.Response", error.toString());
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    show_alert_Dialog_singlebutton(getString(R.string.network_error));
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
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

            }
        }
    }


    @Override
    public void onRefresh() {
        srl_How_tosDetail.setRefreshing(true);
        messageList.clear();
        mAdapter.notifyDataSetChanged();
        getHowtosDetailOnServer(howTosId);
        srl_How_tosDetail.setRefreshing(false);
    }

    public void fillData(HowTos howTos) {
        tv_howto_title.setText(howTos.getHttitle());
        messageList.add(new Message(1, howTos.getHtque_1(), "Admin"));
        messageList.add(new Message(2, howTos.getHtans_1(), "Client"));
        messageList.add(new Message(3, howTos.getHtque_2(), "Admin"));
        messageList.add(new Message(4, howTos.getHtans_2(), "Client"));
        messageList.add(new Message(5, howTos.getHtque_3(), "Admin"));
        messageList.add(new Message(6, howTos.getHtans_3(), "Client"));
        messageList.add(new Message(7, howTos.getHtque_4(), "Admin"));
        messageList.add(new Message(8, howTos.getHtans_4(), "Client"));
        messageList.add(new Message(9, howTos.getHtvideo(), "Video"));
        messageList.add(new Message(10, howTos.getHtartical(), "Artical"));

        mAdapter.notifyDataSetChanged();

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
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
//        stopLockTask();
    }


}
