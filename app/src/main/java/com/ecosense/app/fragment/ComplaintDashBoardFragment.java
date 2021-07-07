package com.ecosense.app.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.NewComplaints;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Complaints;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComplaintDashBoardFragment extends Fragment implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;
    private static final String TAG = ComplaintDashBoardFragment.class.getSimpleName();

    @BindView(R.id.tv_pending_count)
    TextView tv_pending_count;

    @BindView(R.id.tv_wInp_count)
    TextView tv_wInp_count;

    @BindView(R.id.tv_closed_count)
    TextView tv_closed_count;

    @BindView(R.id.tv_total_count)
    TextView tv_total_count;

    public ComplaintDashBoardFragment() {
        // Required empty public constructor
    }


    PieChart picahrt_Surveys_Taken;


    BarChart chart_sug_barchart;
    private String[] xValues1 = {"Pending", "In-Progress", "Closed", "Total"};

    // colors for different sections in Barchart
    public static final int[] MY_COLORS = {
            Color.parseColor("#f25b43"), Color.parseColor("#F8A310"),
            Color.parseColor("#54AB1A"), Color.parseColor("#2699FB")
    };

    public static final int[] MY_PiChart_COLORS = {
            Color.parseColor("#54ab1a"), Color.parseColor("#DD2C2C")
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_complaint_dash_board, container, false);
        ButterKnife.bind(this, view);


        picahrt_Surveys_Taken = (PieChart) view.findViewById(R.id.picahrt_Surveys_Taken);
        chart_sug_barchart = (BarChart) view.findViewById(R.id.chart_sug_barchart);


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Create New Complaint", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                getActivity().finish();
//                Intent i = new Intent(getActivity(), NewComplaints.class);
//                startActivity(i);

                Intent mIntent = new Intent(getActivity(), NewComplaints.class); // the activity that holds the fragment
                Bundle b = new Bundle();
//                b.putSerializable("ComplaintsData", com);
                mIntent.setAction("NewComplaintsGen");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
//                getActivity().finish();
            }
        });
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
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Complaints Dashboard");
//        setDataPichart(picahrt_Surveys_Taken, 20, 10, 5, 35);
//        setdata(chart_sug_barchart, "Surveys Taken", 20, 10, 5, 35);
        getDashBoardDataOnServer();
    }

    @Override
    public void onClick(View v) {


    }
    public void getDashBoardDataOnServer() {
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
                    String url = session.getMyServerIP() + Connection.MY_SERVER_METHOD + AppConfig.API_complaint_dashboard_indv + "?reg_no=" + URLEncoder.encode(session.getpsNo(), "UTF-8");
                    Log.e(TAG, "url = " + url);
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
                                    Log.e(TAG, "statusData = " + statusData);
                                        if (!statusData.toString().trim().equalsIgnoreCase("[]")) {

                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Complaints fuelDetails = objectMapper.readValue(visitor.toString(), Complaints.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {

                                                    long com_new=Long.parseLong( fuelDetails.getCom_dash_new());
                                                    long com_wip=Long.parseLong( fuelDetails.getCom_dash_wip());
                                                    long com_closed=Long.parseLong( fuelDetails.getCom_dash_closed());
                                                    long com_total=com_new+com_wip+com_closed;

                                                    tv_pending_count.setText(com_new+"");
                                                    tv_wInp_count.setText(com_wip+"");
                                                    tv_closed_count.setText(com_closed+"");
                                                    tv_total_count.setText(com_total+"");

                                                    setComplaintsPichart(picahrt_Surveys_Taken,com_new, com_wip,com_closed,com_total);
                                                }
                                                break;
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
    // Method to manually check connection status
    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }


    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = getString(R.string.back_online);
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message = getString(R.string.you_are_offline);
            TastyToast.makeText(getActivity(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
    }

    @Override
    public void onPause(){
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
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
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
    private SearchView searchView = null;

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_search, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_filter_search);
//        menu.findItem(R.id.action_filter_search).setVisible(false);
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//            searchView.setOnQueryTextListener(this);

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
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }



    public void setComplaintsPichart(PieChart mPichart, long pendding, long In_Progress, long Closed, long Total) {
        PieChart mChart = mPichart;
        mChart.getDescription().setEnabled(false);
//        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.animateXY(1400, 1400);
        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterTextTypeface(Typeface.MONOSPACE);
//        mChart.setCenterText("RoutePoint on 12.08.2018");
        mChart.setCenterTextSize(12);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.parseColor("#E3F2FD"));

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(45f);
        mChart.setTransparentCircleRadius(50f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);


//        Legend l = mChart.getLegend();
//        l.setEnabled(false);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(Typeface.SANS_SERIF);
        mChart.setEntryLabelTextSize(8f);


        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(Typeface.SANS_SERIF);
        mChart.setEntryLabelTextSize(6f);


        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(pendding, "Pending"));
        entries.add(new PieEntry(In_Progress, "In-Progress"));
        entries.add(new PieEntry(Closed, "Closed"));
        entries.add(new PieEntry(Total, "Total"));

        PieDataSet dataSet = new PieDataSet(entries, "Complaints");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

//        dataSet.setColors(MY_PiChart_COLORS);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new DefaultValueFormatter(0));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.parseColor("#FFFFFF"));
        data.setValueTypeface(Typeface.MONOSPACE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    public void setdata(BarChart mchart, String punchType, long pendding, long In_Progress, long Closed, long Total) {
        BarChart chart = mchart;
        IAxisValueFormatter xAxisFormatter = new MyBarChartXAxisValueFormatter(xValues1);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(xAxisFormatter);


        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setEnabled(false);

        yAxis = chart.getAxisRight();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(false);
        yAxis.setEnabled(false);


//        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        leftAxis.setSpaceTop(15f);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setDrawGridLines(false);
//        leftAxis.setGranularity(1f);
//        leftAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return String.valueOf((int) value);
//            }
//        });


//        chart.setExtraBottomOffset(5);
//        chart.setExtraOffsets(float left, float top, float right, float bottom)


        XYMarkerView mv = new XYMarkerView(getContext(), xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        Legend l = chart.getLegend();
        l.setEnabled(false);

        ArrayList<IBarDataSet> dataSets = null;

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, pendding));
        entries.add(new BarEntry(1f, In_Progress));
        entries.add(new BarEntry(2f, Closed));
        entries.add(new BarEntry(3f, Total));

        BarDataSet dataset = new BarDataSet(entries, punchType);
        dataset.setColors(MY_COLORS);

        dataSets = new ArrayList<>();
        dataSets.add(dataset);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setDragEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setScaleXEnabled(false);

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.8f);
        data.setValueTextSize(12f);
        data.setValueFormatter(new DefaultValueFormatter(0));
        chart.setData(data);
        data.setValueTypeface(Typeface.MONOSPACE);
        chart.getDescription().setEnabled(false);
        chart.animateXY(2000, 2000);
        chart.invalidate();
    }
    public void setDataPichart(PieChart mPichart, long pendding, long In_Progress, long Closed, long Total) {
        PieChart mChart = mPichart;
        mChart.getDescription().setEnabled(false);
//        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.animateXY(1400, 1400);
        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterTextTypeface(Typeface.MONOSPACE);
        mChart.setCenterText("RoutePoint on 12.08.2018");
        mChart.setCenterTextSize(12);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.parseColor("#E3F2FD"));

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(45f);
        mChart.setTransparentCircleRadius(50f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);


//        Legend l = mChart.getLegend();
//        l.setEnabled(false);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(Typeface.SANS_SERIF);
        mChart.setEntryLabelTextSize(8f);


        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(Typeface.SANS_SERIF);
        mChart.setEntryLabelTextSize(6f);


        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(pendding, "Pending"));
        entries.add(new PieEntry(In_Progress, "In-Progress"));
        entries.add(new PieEntry(Closed, "Closed"));
        entries.add(new PieEntry(Total, "Total"));

        PieDataSet dataSet = new PieDataSet(entries, "Surveys Taken");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

//        dataSet.setColors(MY_PiChart_COLORS);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new DefaultValueFormatter(0));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.parseColor("#FFFFFF"));
        data.setValueTypeface(Typeface.MONOSPACE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }
}
