package com.ecosense.app.fragment;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.NotificationListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.NotificationItem;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = NotificationFragment.class.getSimpleName();

    Boolean isConnected = false;
    public static String SERVER_URL = null;


    @BindView(R.id.srl_notification)
    SwipeRefreshLayout srl_notification;

    @BindView(R.id.rv_notification)
    RecyclerView rv_notification;

    private GridLayoutManager lLayout;
    NotificationListAdapter notificationListAdapter;
    List<NotificationItem> notificationList;

    Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;
    UserSessionManger session;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.bind(this, view);

        notificationList = new ArrayList<>();
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

        lLayout = new GridLayoutManager(getActivity(), 1);
        rv_notification.setHasFixedSize(true);
        rv_notification.setLayoutManager(lLayout);
        rv_notification.setItemAnimator(new DefaultItemAnimator());
        notificationListAdapter = new NotificationListAdapter(getActivity(), notificationList);
        rv_notification.setAdapter(notificationListAdapter);
        rv_notification.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), rv_notification, new RecyclerItemClickListener.OnItemClickListener() {
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

        srl_notification.setOnRefreshListener(this);
        srl_notification.setColorSchemeResources(R.color.colorAccent);
        srl_notification.setNestedScrollingEnabled(true);
        srl_notification.post(this::getNotificationOnServer);

    }

    @Override
    public void onRefresh() {
        srl_notification.setRefreshing(true);
        notificationList.clear();
        notificationListAdapter.notifyDataSetChanged();
        srl_notification.post(this::getNotificationOnServer);
        srl_notification.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }

    public void  getNotificationOnServer() {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getActivity(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
                    Log.e(TAG, "filters = " + filters + "&fields=" + fields);
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_notification + "?id=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
//                    String url = session.getMyServerIP() + "/api/resource/Notifications?filters=" + URLEncoder.encode(filters, "UTF-8") + "&fields=" + URLEncoder.encode(fields, "UTF-8");
//                    String url = session.getMyServerIP() + "/api/resource/Notifications?fields=" + URLEncoder.encode(fields, "UTF-8");
                    StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                // response
                                if (!response.isEmpty()) {
                                    Log.e("Response", response);
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
                                                JsonNode notifi = elements1.next();

                                                if (!notifi.toString().trim().equalsIgnoreCase("{}")) {
                                                    NotificationItem notificationItem = objectMapper.readValue(notifi.toString(), NotificationItem.class);
//                                                    Log.e(TAG, "getNotificationID = " + notificationItem.getNotificationID());
//                                                    Log.e(TAG, "getNftdoctypename = " + notificationItem.getNftdoctypename());
                                                    notificationList.add(notificationItem);
                                                    notificationListAdapter.notifyDataSetChanged();
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
                                            TastyToast.makeText(getActivity(), getString(R.string.no_record_found_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                            Log.e(TAG, getString(R.string.no_record_found_error));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Response IOException " + e.getMessage());
                                    }
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    TastyToast.makeText(getActivity(), getString(R.string.response_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                    Log.e(TAG, "Response Error");
                                }
                            }, error -> {
                        // error
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getActivity(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e("Error.Response", error.toString());
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
    public boolean onQueryTextChange(String newText) {
        final List<NotificationItem> filteredModelList = filter(notificationList, newText);
        notificationListAdapter.setFilter(filteredModelList);
        return false;
    }
    private List<NotificationItem> filter(List<NotificationItem> models, String query) {
        query = query.toLowerCase();

        final List<NotificationItem> filteredModelList = new ArrayList<>();
        for (NotificationItem model : models) {
            final String text = model.getNftdoctypename().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

}