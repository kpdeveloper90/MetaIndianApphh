package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.AssetsTrackersListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class BinStatusListFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = BinStatusListFragment.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @BindView(R.id.srl_Bin_Status)
    SwipeRefreshLayout srl_Bin_Status;

    @BindView(R.id.rv_Bin_Status)
    RecyclerView rv_Bin_Status;

    private GridLayoutManager lLayout;
    AssetsTrackersListAdapter assetsTrackersListAdapter;
    List<Assets> assetsTrackersList;
    private SearchView searchView = null;

    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;

    public BinStatusListFragment() {
        // Required empty public constructor
    }

    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bin_status_list, container, false);
        ButterKnife.bind(this, view);
        session = new UserSessionManger(getActivity());
//        fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(view1 -> {
//            if (isServicesOK()) {
//                Intent intent = new Intent(getActivity(), MapsActivity.class);
//                startActivity(intent);
//            }
//        });

        assetsTrackersList = new ArrayList<>();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, "Not connected ");
            TastyToast.makeText(getActivity(), "You're Offline", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }

        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_Bin_Status.setHasFixedSize(true);
        rv_Bin_Status.setLayoutManager(lLayout);
        rv_Bin_Status.setItemAnimator(new DefaultItemAnimator());
        assetsTrackersListAdapter = new AssetsTrackersListAdapter(getActivity(), assetsTrackersList);
        rv_Bin_Status.setAdapter(assetsTrackersListAdapter);
        rv_Bin_Status.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), rv_Bin_Status, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                Assets trackers = assetsTrackersList.get(position);
//
//                Log.e(TAG, " Name = " + empRegItem.getPsno() + "  getPsname = " + empRegItem.getPsname());


//                finish();
//                Intent mIntent = new Intent(getApplicationContext(), ComplaintsDetail.class); // the activity that holds the fragment
//                    Bundle mBundle = new Bundle();
//
//                    mBundle.putSerializable("empRegItem", empRegItem);
//                    mIntent.setAction("newRegistration");
//                    mIntent.putExtra("regDetail", mBundle);
//                startActivity(mIntent);

            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        srl_Bin_Status.setOnRefreshListener(this);
        srl_Bin_Status.setColorSchemeResources(R.color.colorAccent);
        srl_Bin_Status.setNestedScrollingEnabled(true);
        srl_Bin_Status.post(
                new Runnable() {
                    @Override
                    public void run() {
                        getAssetsBinDetailOnServer("BIN");
                    }
                }
        );

    }

    public void getAssetsBinDetailOnServer(String assatsName) {
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
                    String filters = null;
                    String fields = null;

//                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
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
//                filters = "[[\"Visitor_Management\",\"workflow_state\",\"in\",[\"Visitor Check In\",\"Visitor Check Out\"]]," +
//                        "[\"Visitor_Management\",\"visitor_arrival_date_and_time\",\">\",\"" +
//                        rDate_time +
//                        "\"],[\"Visitor_Management\",\"visitor_arrival_date_and_time\",\"<=\",\"" +
//                        addOneDay +
//                        "\"]] ";


                    filters = URLEncoder.encode("[[\"Facility_And_Assets\", \"faname\", \"=\",\"", "UTF-8")
                            + assatsName
                            + URLEncoder.encode("\"]]", "UTF-8");


                    fields = URLEncoder.encode("[\"*\"]", "UTF-8");

                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);

                    String url = session.getMyServerIP() + "/api/resource/Facility_And_Assets?filters=" + filters + "&fields=" + fields;

//                String url = session.getMyServerIP() + "/api/resource/News?fields=" + URLEncoder.encode(fields, "UTF-8");
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
//                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Assets eventDetails = objectMapper.readValue(visitor.toString(), Assets.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    assetsTrackersList.add(eventDetails);
                                                    assetsTrackersListAdapter.notifyDataSetChanged();
//                                                Log.e(TAG, "getVisitor_name = " + visitorDetails.getVisitor_name());
                                                }
                                            }
                                        } else {
                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            show_alert_Dialog_singlebutton(getString(R.string.no_record_found_error));
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

    @Override
    public void onRefresh() {
        srl_Bin_Status.setRefreshing(true);
        assetsTrackersList.clear();
        assetsTrackersListAdapter.notifyDataSetChanged();
        getAssetsBinDetailOnServer("BIN");
        srl_Bin_Status.setRefreshing(false);
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

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            TastyToast.makeText(getActivity(), "You can't make map requests", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_filter_search);
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
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Assets> filteredModelList = filter(assetsTrackersList, query);
        assetsTrackersListAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Assets> filter(List<Assets> models, String query) {
        query = query.toLowerCase();

        final List<Assets> filteredModelList = new ArrayList<>();
        for (Assets model : models) {
            final String text = model.getFalocation().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
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
            message = "Back-Online";
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
        }

//        Snackbar snackbar = Snackbar
//                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);

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
    public void onPause(){
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if(mProgressDialog != null)
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
