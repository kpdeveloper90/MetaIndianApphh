package com.ecosense.app.activity.metaData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.Assets;

import static com.ecosense.app.helper.AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST;
import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class CurrentLocationMark extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);


            mMap.getUiSettings().setCompassEnabled(true);
//            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);

            mMap.getUiSettings().setAllGesturesEnabled(true);
            getDeviceLocation();
            mMap.setOnMarkerDragListener(this);

        }
    }

    private GoogleMap mMap;

    private static final String TAG = CurrentLocationMark.class.getSimpleName();
    private Toolbar toolbar;
    static ProgressDialog mProgressDialog = null;
    Boolean isConnected = false;
    UserSessionManger session = null;
    @BindView(R.id.tv_current_locName)
    TextView tv_current_locName;

    @BindView(R.id.btn_markLoc)
    Button btn_markLoc;

    @BindView(R.id.ic_gps)
    ImageView ic_gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location_mark);


        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Mark Location");
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


        getLocationPermission();

        ic_gps.setOnClickListener(this);
        btn_markLoc.setOnClickListener(this);
        onNewIntent(getIntent());
    }

    static String methodIntent = null;
    static Assets metaDataItem;

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

            if (methodIntent != null && methodIntent.equals("UpdateLatLong")) {
                metaDataItem = (Assets) extras.getSerializable("metaDataItem");
            } else if (methodIntent != null && methodIntent.equals("NewLatLong")) {
                metaDataItem = (Assets) extras.getSerializable("metaDataItem");
            } else if (methodIntent != null && methodIntent.equals("UpdateMetaData")) {
                metaDataItem = (Assets) extras.getSerializable("metaDataItem");
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    static Double MyLat = 0.0, MyLong = 0.0;
    static String currentAddress = null;

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        final ProgressDialog progressDialog = new ProgressDialog(CurrentLocationMark.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.Finding_Location));
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            try {
                if (mLocationPermissionsGranted) {


                    mFusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {

                                        MyLat = location.getLatitude();
                                        MyLong = location.getLongitude();

                                        Log.e(TAG, "MyLat = " + MyLat);
                                        Log.e(TAG, "MyLong = " + MyLong);


                                        LatLng latLng = (new LatLng(location.getLatitude(), location.getLongitude()));
                                        mMap.clear();

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));

                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);
                                        markerOptions.draggable(true);

                                        BitmapDescriptor markerIcon = vectorToBitmap(R.drawable.ic__fill_placeholder,
                                                ContextCompat.getColor(getApplicationContext(),
                                                        R.color.rv_hader));

                                        markerOptions.icon(markerIcon);
                                        markerOptions.title("My Location");
                                        if (session.getMobileNumber() != null) {
                                            markerOptions.snippet(session.getMobileNumber());
                                        }
                                        mMap.addMarker(markerOptions);

                                        String add = getAddress(location.getLatitude(), location.getLongitude());
                                        Log.e(getClass().getSimpleName(), "getAddress : " + add);
                                        currentAddress = add;
                                        if (add != null)
                                            tv_current_locName.setText(add);
                                        else
                                            tv_current_locName.setText("Not Find");
                                    } else {
                                        Log.d(TAG, "onComplete: current location is null");
                                        TastyToast.makeText(getApplicationContext(), getString(R.string.unable_toget_current_location), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                        Intent mIntent = new Intent(getApplicationContext(), MetaDataList.class); // the activity that holds the fragment
                                        Bundle mBundle = new Bundle();

                                        mBundle.putSerializable("metaDataItem", metaDataItem);
                                        mIntent.setAction("UpdateLatLong");
                                        mIntent.putExtra("SelectedNameDetail", mBundle);
                                        startActivity(mIntent);
                                        finish();
                                    }
                                }
                            });
                }
                progressDialog.dismiss();
            } catch (SecurityException e) {
                progressDialog.dismiss();
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "getDeviceLocation: Exception: " + e.getMessage());
            }

        }, 2000);

    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        assert vectorDrawable != null;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_markLoc) {
            String msg = null;
            if (methodIntent.equalsIgnoreCase("UpdateLatLong")) {
                msg = getString(R.string.update_Asset_s_location);
            } else {
                msg = getString(R.string.conform_POI_location);
            }
            aleartforCompetRoute(msg);
        }
        if (v == ic_gps) {
            mMap.clear();
            getDeviceLocation();
        }

    }

    protected void aleartforCompetRoute(String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.Conformation_for_location))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        if (methodIntent != null && methodIntent.equals("UpdateLatLong")) {

                            updateMetaDataRequestServer();
                        } else {

                            metaDataItem.setFalatitude(MyLat + "");
                            metaDataItem.setFalongitude(MyLong + "");
                            metaDataItem.setFalocation(currentAddress);

                            finish();
                            Intent mIntent = new Intent(getApplicationContext(), UpdateMetaData.class); // the activity that holds the fragment
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable("metaDataItem", metaDataItem);
                            if (methodIntent.equalsIgnoreCase("UpdateMetaData")) {
                                mIntent.setAction("UpdateMetaData");
                            } else {
                                mIntent.setAction("NewLatLong");
                            }
                            mIntent.putExtra("SelectedNameDetail", mBundle);
                            startActivity(mIntent);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();

                    }
                })
                .show();

    }


    public void updateMetaDataRequestServer_JsonObjectRequest() {
        try {
            mProgressDialog = new ProgressDialog(CurrentLocationMark.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
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
                    jsonObject.put("latitude", MyLat);
                    jsonObject.put("longitude", MyLong);
                    jsonObject.put("geo_location", currentAddress);

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                String url = null;

                url = session.getMyServerIP() + "/api/resource/Asset/" + URLEncoder.encode(metaDataItem.getAsset_Id(), "UTF-8");

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            Assets assets = objectMapper.readValue(statusData.toString(), Assets.class);

                                            Log.e(TAG, " response Successfully Updated against getName= " + assets.getAsset_Id());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), "Successfully Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                            Intent mIntent = new Intent(getApplicationContext(), MetaDataList.class); // the activity that holds the fragment
                                            Bundle mBundle = new Bundle();

                                            mBundle.putSerializable("metaDataItem", metaDataItem);
                                            mIntent.setAction("UpdateLatLong");
                                            mIntent.putExtra("SelectedNameDetail", mBundle);
                                            startActivity(mIntent);
                                            finish();
                                        } else {
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    public void updateMetaDataRequestServer() {
        try {
            mProgressDialog = new ProgressDialog(CurrentLocationMark.this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e) {
        }
        new android.os.Handler().postDelayed(() -> {
            try {

                String url = null;

                url = session.getMyServerIP() + "/api/resource/Asset/" + URLEncoder.encode(metaDataItem.getAsset_Id(), "UTF-8");

                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
//                                Log.e("aaaaaaa", response.toString());
                                try {

                                    if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
//                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            Assets assets = objectMapper.readValue(statusData.toString(), Assets.class);

                                            Log.e(TAG, " response Successfully Updated against getName= " + assets.getAsset_Id());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }
                                            TastyToast.makeText(getApplicationContext(), "Successfully Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

                                            Intent mIntent = new Intent(getApplicationContext(), MetaDataList.class); // the activity that holds the fragment
                                            Bundle mBundle = new Bundle();

                                            mBundle.putSerializable("metaDataItem", metaDataItem);
                                            mIntent.setAction("UpdateLatLong");
                                            mIntent.putExtra("SelectedNameDetail", mBundle);
                                            startActivity(mIntent);
                                            finish();
                                        } else {
                                            TastyToast.makeText(getApplicationContext(), getString(R.string.could_no_send_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                            jsonObject.put("latitude", MyLat);
                            jsonObject.put("longitude", MyLong);
                            jsonObject.put("geo_location", currentAddress);
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

    @Override
    public void onMarkerDrag(Marker marker) {
        // do nothing during drag
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        Log.e(getClass().getSimpleName(), "Drag End at: " + marker.getPosition());
        String add = getAddress(marker.getPosition().latitude, marker.getPosition().longitude);
        Log.e(getClass().getSimpleName(), "Drag End at: " + add);
        MyLat = marker.getPosition().latitude;
        MyLong = marker.getPosition().longitude;
        currentAddress = add;
        tv_current_locName.setText(add);

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

        Log.e(getClass().getSimpleName(), "Drag start at: " + marker.getPosition());
    }

    public String getAddress(double latitude, double longitude) {


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());


        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();

            //addMarker();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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

    protected void aleartforBack(String msg) {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setTitle(getString(R.string.alert))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_black_24dp))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (methodIntent.equalsIgnoreCase("NewLatLong")) {
                            Intent mIntent = new Intent(getApplicationContext(), MyPOICollected.class); // the activity that holds the fragment
                            Bundle mBundle = new Bundle();

                            mBundle.putSerializable("metaDataItem", metaDataItem);
                            mIntent.setAction("UpdateMetaData");
                            mIntent.putExtra("SelectedNameDetail", mBundle);
                            startActivity(mIntent);
                            finish();
                        } else {
                            Intent mIntent = new Intent(getApplicationContext(), MetaDataList.class); // the activity that holds the fragment
                            Bundle mBundle = new Bundle();

                            mBundle.putSerializable("metaDataItem", metaDataItem);
                            mIntent.setAction("UpdateLatLong");
                            mIntent.putExtra("SelectedNameDetail", mBundle);
                            startActivity(mIntent);
                            finish();
                        }


                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        String msg = getString(R.string.do_you_want_to_back_entery_discarded);
        aleartforBack(msg);
    }

}
