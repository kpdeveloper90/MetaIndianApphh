package com.ecosense.app.activity.supervisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.textfield.TextInputEditText;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.DropDownStringAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Expense;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class GenerateExpense extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = GenerateExpense.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;
    @BindView(R.id.tv_emp_name)
    AppCompatTextView tv_emp_name;

    @BindView(R.id.sp_type_of_expense)
    Spinner sp_type_of_expense;

    @BindView(R.id.edt_description)
    TextInputEditText edt_description;

    @BindView(R.id.edt_expense_amount)
    TextInputEditText edt_expense_amount;
    @BindView(R.id.edt_expense_date)
    TextInputEditText edt_expense_date;

    @BindView(R.id.im_btn_cancel)
    ImageView im_btn_cancel;

    @BindView(R.id.im_btn_submit)
    ImageView im_btn_submit;

    ArrayList<String> list_expanse_type = null;
    DropDownStringAdapter adapter_expanse_type = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_expense);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include8);
        toolbar.setTitle("Generate Expense");
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
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Log.e(TAG, "Connected to Internet");
        }
        tv_emp_name.setText(session.getpsName());
        edt_expense_date.setText(Connection.getCurrentDate());
        list_expanse_type = new ArrayList<>();
        adapter_expanse_type = new DropDownStringAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, list_expanse_type);
        getExpenseTypeOnServer();
        sp_type_of_expense.setAdapter(adapter_expanse_type);

        im_btn_cancel.setOnClickListener(this);
        im_btn_submit.setOnClickListener(this);
        edt_expense_date.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    @Override
    public void onClick(View v) {
        if (v == im_btn_submit) {
            getData();
        }
        if (v == im_btn_cancel) {
            if (methodIntent.equalsIgnoreCase("SupervisorExpanseList")) {
                finish();
                startActivity(new Intent(getApplicationContext(), ExpenseDetail.class));
            } else {
                finish();
            }
        }
        if (v == edt_expense_date) {
            openDatePicker(edt_expense_date);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            //        Intent intent = getIntent();

            methodIntent = intent.getAction();
            Log.e(TAG, "onNewIntent" + intent.getAction());
//        Bundle extras = intent.getBundleExtra("SelectedNameDetail");
//        Log.e(TAG, "onNewIntent" + intent.getAction());
//        if (extras != null) {
//            methodIntent = intent.getAction();
//            Log.e(TAG, "\n outside methodIntent=> " + methodIntent);
//
//        } else {
//            Log.e(TAG, "Bundle Is empty ");
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getData() {

        if (Objects.requireNonNull(edt_expense_amount.getText()).toString().isEmpty()) {
            edt_expense_amount.setError(getString(R.string.enter_expense_amount));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_expense_amount), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            Expense e_item = new Expense();

            e_item.setApproval_status(AppConfig.Expense_Status_Draft);
            e_item.setEmployee(session.getpsNo());
            e_item.setEmployee_name(session.getpsName());
            e_item.setTitle(session.getpsName());
            e_item.setMobile_no(session.getMobileNumber());
            e_item.setCost_center(session.getuserCost_Center());
            Log.e(TAG, "session.getuserCost_Center() = " + session.getuserCost_Center());
            //in expance line Item Add in list
            List<Expense> es = new ArrayList<>();
            Expense e_type_info = new Expense();
            e_type_info.setExpense_type(sp_type_of_expense.getSelectedItem().toString());
            e_type_info.setClaim_amount(edt_expense_amount.getText().toString());
            e_type_info.setSanctioned_amount(edt_expense_amount.getText().toString());
            e_type_info.setDescription(edt_description.getText().toString());
            e_type_info.setExpense_date(edt_expense_date.getText().toString());
            es.add(e_type_info);

            e_item.setExpenses(es);

            sendExpenseDetailRequest(e_item);

        }
    }

    public void getExpenseTypeOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(GenerateExpense.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + "/api/resource/Expense%20Claim%20Type";
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
                                            list_expanse_type.clear();
                                            adapter_expanse_type.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Expense fuelDetails = objectMapper.readValue(visitor.toString(), Expense.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
                                                    list_expanse_type.add(fuelDetails.getName());
                                                    adapter_expanse_type.notifyDataSetChanged();
                                                    Log.d(TAG, "Data Available");
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
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                    });
                    String tag_string_req = "string_req";
                    ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);

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

    public void sendExpenseDetailRequest(Expense expenseDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(GenerateExpense.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {
                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    int requestMethod;

                    url = session.getMyServerIP() + "/api/resource/Expense%20Claim";
                    requestMethod = Request.Method.POST;

                    StringRequest jsonObjectRequest = new StringRequest(requestMethod, url,
                            (String response) -> {
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

                                                Expense voDetail = objectMapper.readValue(statusData.toString(), Expense.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                TastyToast.makeText(getApplicationContext(), "Successfully generate Expenses", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                if (methodIntent.equalsIgnoreCase("SupervisorExpanseList")) {
                                                    finish();
                                                    startActivity(new Intent(getApplicationContext(), ExpenseDetail.class));
                                                } else {
                                                    finish();
                                                }
                                            } else {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                }
                            }, error -> {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in response upload image RequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();

                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject.put("employee", expenseDetail.getEmployee());
                                jsonObject.put("employee_name", expenseDetail.getEmployee_name());
                                jsonObject.put("approval_status", expenseDetail.getApproval_status());
                                jsonObject.put("mobile_no", expenseDetail.getMobile_no());
                                jsonObject.put("title", expenseDetail.getEmployee_name());
                                jsonObject.put("cost_center", expenseDetail.getCost_center());

                                Log.e(TAG, "cost_center = " + expenseDetail.getCost_center());

                                JSONArray array = new JSONArray();

                                JSONObject obj = new JSONObject();
                                obj.put("expense_date", expenseDetail.getExpenses().get(0).getExpense_date());
                                obj.put("expense_type", expenseDetail.getExpenses().get(0).getExpense_type());
                                obj.put("description", expenseDetail.getExpenses().get(0).getDescription());
                                obj.put("claim_amount", expenseDetail.getExpenses().get(0).getClaim_amount());
                                obj.put("sanctioned_amount", expenseDetail.getExpenses().get(0).getSanctioned_amount());

                                array.put(obj);

                                jsonObject.put("expenses", array);

                                params.put("data", jsonObject.toString());

                                Log.e(TAG, "params = " + params);
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
    }

    public void sendExpenseDetailRequest_JsonObjectRequest(Expense expenseDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(GenerateExpense.this);
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

                        jsonObject.put("employee", expenseDetail.getEmployee());
                        jsonObject.put("employee_name", expenseDetail.getEmployee_name());
                        jsonObject.put("approval_status", expenseDetail.getApproval_status());
                        jsonObject.put("mobile_no", expenseDetail.getMobile_no());
                        jsonObject.put("title", expenseDetail.getEmployee_name());
                        jsonObject.put("cost_center", expenseDetail.getCost_center());


                        JSONArray array = new JSONArray();

                        JSONObject obj = new JSONObject();
                        obj.put("expense_date", expenseDetail.getExpenses().get(0).getExpense_date());
                        obj.put("expense_type", expenseDetail.getExpenses().get(0).getExpense_type());
                        obj.put("description", expenseDetail.getExpenses().get(0).getDescription());
                        obj.put("claim_amount", expenseDetail.getExpenses().get(0).getClaim_amount());
                        obj.put("sanctioned_amount", expenseDetail.getExpenses().get(0).getSanctioned_amount());

                        array.put(obj);

                        jsonObject.put("expenses", array);

                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    int requestMethod;

                    url = session.getMyServerIP() + "/api/resource/Expense%20Claim";
                    requestMethod = Request.Method.POST;

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestMethod, url, jsonObject,
                            response -> {
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

                                                Expense voDetail = objectMapper.readValue(statusData.toString(), Expense.class);

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
                                                }

                                                TastyToast.makeText(getApplicationContext(), "Successfully generate Expenses", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                                if (methodIntent.equalsIgnoreCase("SupervisorExpanseList")) {
                                                    finish();
                                                    startActivity(new Intent(getApplicationContext(), ExpenseDetail.class));
                                                } else {
                                                    finish();
                                                }
                                            } else {
                                                TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                }
                            }, error -> {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        Log.e(TAG, " Error in response upload image RequestServer ");
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
    }

    private int mYear;
    private int mMonth;
    private int mDay;

    public void openDatePicker(TextInputEditText editTextDate) {
        final TextInputEditText edtDate = editTextDate;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
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


                        edtDate.setText(year + "-" + month + "-" + day);
//                        filter_Date = year + "-" + month + "-" + day;

                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
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
            message = getString(R.string.back_online);
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
            message = getString(R.string.you_are_offline);
            TastyToast.makeText(getApplicationContext(), message, TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
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
    public void onBackPressed() {
//        super.onBackPressed();
        if (methodIntent.equalsIgnoreCase("SupervisorExpanseList")) {
            finish();
            startActivity(new Intent(getApplicationContext(), ExpenseDetail.class));
        } else {
            finish();
        }
    }
}