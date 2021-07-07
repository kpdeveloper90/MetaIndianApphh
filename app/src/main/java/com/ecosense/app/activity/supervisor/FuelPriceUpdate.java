package com.ecosense.app.activity.supervisor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.FuelPumpLocDropDownAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Fuel;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class FuelPriceUpdate extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = FuelPriceUpdate.class.getSimpleName();
    private Toolbar toolbar;

    static ProgressDialog mProgressDialog = null;

    Boolean isConnected = false;
    UserSessionManger session = null;
    static String methodIntent = null;

    @BindView(R.id.sp_fup_Loc)
    Spinner sp_fup_Loc;

    @BindView(R.id.tv_fuel_oem_nm)
    TextView tv_fuel_oem_nm;

    @BindView(R.id.edt_fuel_price)
    TextInputEditText edt_fuel_price;

    @BindView(R.id.edt_fuel_qty)
    TextInputEditText edt_fuel_qty;

    @BindView(R.id.cb_fixed_fuel_qty)
    CheckBox cb_fixed_fuel_qty;

    @BindView(R.id.btn_cancel)
    MaterialButton btn_cancel;

    @BindView(R.id.btn_save)
    MaterialButton btn_save;
    ArrayList<Fuel> fuelPumpList = null;
    FuelPumpLocDropDownAdapter adapter_fup_Loc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_price_update);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include7);
        toolbar.setTitle("Fuel Price Update");
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
        fuelPumpList = new ArrayList<>();
//        adapter_fup_Loc = new FuelPumpLocDropDownAdapter(this, R.layout.custom_dropdown_list_row, R.id.tv_name, fuelPumpList);
        adapter_fup_Loc = new FuelPumpLocDropDownAdapter(this, fuelPumpList);

        cb_fixed_fuel_qty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (edt_fuel_qty.getVisibility() == View.GONE)
                        edt_fuel_qty.setVisibility(View.VISIBLE);
                } else {
                    if (edt_fuel_qty.getVisibility() == View.VISIBLE)
                        edt_fuel_qty.setVisibility(View.GONE);
                }
            }
        });

        getFuelPumpLocationOnServer();
        pumpLoc_Spinner_fill();


        sp_fup_Loc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fuel fInfo = fuelPumpList.get(position);
                try {
                    tv_fuel_oem_nm.setText(fInfo.getOem_name());
                } catch (Exception e) {
                    Log.e(TAG, "Exception  sp_fup_Loc.setOnItemSelectedListener = " + e.getMessage());
                }
//                getrouteNoListOnServer(wno);
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_cancel.setOnClickListener(this);
        btn_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_save) {
            aleartforsubmit(getString(R.string.are_you_sure_rate_save));
        }

        if (v == btn_cancel) {

//            Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
//            Bundle mBundle = new Bundle();
//            mIntent.setAction("f");
//            mIntent.putExtra("SelectedNameDetail", mBundle);
//            startActivity(mIntent);
//            finish();
            onBackPressed();
        }
    }

    private void getData() {
        String fuel_fixed_qty = null;
        String fuel_price = null;
        String fuel_pump_location = null;
        String fuel_oem_name = null;
        Fuel fuelDetail = new Fuel();
        if (Objects.requireNonNull(edt_fuel_price.getText()).toString().isEmpty()) {
            edt_fuel_price.setError(getString(R.string.enter_fuel_price));
            TastyToast.makeText(getApplicationContext(), getString(R.string.enter_fuel_price), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {

            if (cb_fixed_fuel_qty.isChecked()) {
                if (Objects.requireNonNull(edt_fuel_qty.getText()).toString().isEmpty()) {
                    edt_fuel_qty.setError(getString(R.string.enter_fuel_qty));
                    TastyToast.makeText(getApplicationContext(), getString(R.string.enter_fuel_qty), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                } else {
                    fuel_fixed_qty = Objects.requireNonNull(edt_fuel_qty.getText()).toString();

                    fuel_price = Objects.requireNonNull(edt_fuel_price.getText()).toString();
                    fuel_oem_name = Objects.requireNonNull(tv_fuel_oem_nm.getText()).toString();
                    fuel_pump_location = Objects.requireNonNull(fuelPumpList.get(sp_fup_Loc.getSelectedItemPosition()).getPump_name());

                    fuelDetail.setPump_name(fuel_pump_location);
                    fuelDetail.setOem_name(fuel_oem_name);
                    fuelDetail.setRate(fuel_price);
                    fuelDetail.setQuantity(fuel_fixed_qty);
                    fuelDetail.setDate(Connection.getCurrentDate());
                    fuelDetail.setSupervisor_id(session.getpsNo());
                    fuelDetail.setFixedQty(true);

                    Log.e(TAG, "isFixedQty = " + fuelDetail.isFixedQty() + " fuelDetail.getQuantity() = " + fuelDetail.getQuantity());
                    sendFuelPriceRequest(fuelDetail);
                }
            } else {

                fuel_price = Objects.requireNonNull(edt_fuel_price.getText()).toString();
                fuel_pump_location = Objects.requireNonNull(fuelPumpList.get(sp_fup_Loc.getSelectedItemPosition()).getPump_name());
                fuel_oem_name = Objects.requireNonNull(tv_fuel_oem_nm.getText()).toString();
                fuelDetail.setPump_name(fuel_pump_location);
                fuelDetail.setOem_name(fuel_oem_name);
                fuelDetail.setRate(fuel_price);
                fuelDetail.setDate(Connection.getCurrentDate());
                fuelDetail.setSupervisor_id(session.getpsNo());
                fuelDetail.setFixedQty(false);

                Log.e(TAG, "isFixedQty = " + fuelDetail.isFixedQty());
                sendFuelPriceRequest(fuelDetail);
            }

        }
    }

    protected void aleartforsubmit(String msg) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this,
                R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setTitle(getString(R.string.alert))
                .setMessage(msg)
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getData();
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                });
        materialAlertDialogBuilder.show();


//        AlertDialog alertbox = new AlertDialog.Builder(this)
//                .setMessage(msg)
//                .setTitle("Alert")
//                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    // do something when the button is clicked
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        getData();
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//
//                    // do something when the button is clicked
//                    public void onClick(DialogInterface arg0, int arg1) {
////                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .show();

    }

    public void sendFuelPriceRequest(Fuel fuelDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(FuelPriceUpdate.this);
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
                    url = session.getMyServerIP() + "/api/resource/Daily_Fuel_Rate";
                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,
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

                                                    Fuel fuelinfo = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }

                                                    Log.e(TAG, "Responce = " + fuelinfo.getName());

                                                    Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                    Bundle mBundle = new Bundle();

                                                    mBundle.putSerializable("fuelDetail", fuelDetail);
                                                    mIntent.setAction("fuelPrice");
                                                    mIntent.putExtra("SelectedNameDetail", mBundle);
                                                    startActivity(mIntent);
                                                    finish();

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
                                }
                            }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                                mProgressDialog = null;
                            }
                            TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                            Log.e(TAG, " Error in response upload image RequestServer ");
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();

                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject = new JSONObject();

                                jsonObject.put("pump_name", fuelDetail.getPump_name());
                                jsonObject.put("oem_name", fuelDetail.getOem_name());
                                jsonObject.put("rate", fuelDetail.getRate());
                                jsonObject.put("date", fuelDetail.getDate());
                                jsonObject.put("supervisor_id", fuelDetail.getSupervisor_id());

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
    }

    public void sendFuelPriceRequest_JsonObjectRequest(Fuel fuelDetail) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(FuelPriceUpdate.this);
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

                        jsonObject.put("pump_name", fuelDetail.getPump_name());
                        jsonObject.put("oem_name", fuelDetail.getOem_name());
                        jsonObject.put("rate", fuelDetail.getRate());
                        jsonObject.put("date", fuelDetail.getDate());
                        jsonObject.put("supervisor_id", fuelDetail.getSupervisor_id());


                    } catch (JSONException e) {
                        Log.e("JSONObject Here", e.toString());
                    }

                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Daily_Fuel_Rate";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
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

                                                    Fuel fuelinfo = objectMapper.readValue(statusData.toString(), Fuel.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }

                                                    Log.e(TAG, "Responce = " + fuelinfo.getName());

                                                    Intent mIntent = new Intent(getApplicationContext(), FuelDetail.class); // the activity that holds the fragment
                                                    Bundle mBundle = new Bundle();

                                                    mBundle.putSerializable("fuelDetail", fuelDetail);
                                                    mIntent.setAction("fuelPrice");
                                                    mIntent.putExtra("SelectedNameDetail", mBundle);
                                                    startActivity(mIntent);
                                                    finish();

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
                                }
                            }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                                mProgressDialog = null;
                            }
                            TastyToast.makeText(getApplicationContext(), getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
    }

    public void pumpLoc_Spinner_fill() {

//        adapter_fup_Loc = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, fuelPumpList);
////        adapter_fup_Loc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


//        ArrayAdapter<CharSequence> adapter_fup_Loc = ArrayAdapter.createFromResource(FuelPriceUpdate.this,fuelPumpList, android.R.layout.simple_spinner_item);
//        adapter_fup_Loc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_fup_Loc.setAdapter(adapter_fup_Loc);
    }

    public void getFuelPumpLocationOnServer() {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            mProgressDialog = new ProgressDialog(FuelPriceUpdate.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String filters = null;
                    String fields = null;

                    fields = "[\"*\"]";
                    String url = session.getMyServerIP() + "/api/resource/Fuel_Pump_Master?fields=" + URLEncoder.encode(fields, "UTF-8") + "&limit_page_length=500";
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
                                            fuelPumpList.clear();
                                            adapter_fup_Loc.notifyDataSetChanged();
                                            Log.d(TAG, "Data Available");
                                            Iterator<JsonNode> elements1 = statusData.elements();

                                            while (elements1.hasNext()) {
                                                JsonNode visitor = elements1.next();
                                                Fuel fuelDetails = objectMapper.readValue(visitor.toString(), Fuel.class);

                                                if (!visitor.toString().trim().equalsIgnoreCase("{}")) {
//                                                    fuelPumpList.add(fuelDetails.getPump_name());
                                                    fuelPumpList.add(fuelDetails);
                                                    adapter_fup_Loc.notifyDataSetChanged();
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
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
    }
}
