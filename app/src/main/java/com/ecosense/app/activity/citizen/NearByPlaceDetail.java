package com.ecosense.app.activity.citizen;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.DrawableRes;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.NearByPlaceListAdapter;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.NearByPlace;
import com.ecosense.app.pojo.PlaceInfo;

import static com.ecosense.app.helper.AppConfig.GEOMETRY;
import static com.ecosense.app.helper.AppConfig.GOOGLE_BROWSER_API_KEY;
import static com.ecosense.app.helper.AppConfig.ICON;
import static com.ecosense.app.helper.AppConfig.LATITUDE;
import static com.ecosense.app.helper.AppConfig.LOCATION;
import static com.ecosense.app.helper.AppConfig.LONGITUDE;
import static com.ecosense.app.helper.AppConfig.NAME;
import static com.ecosense.app.helper.AppConfig.OK;
import static com.ecosense.app.helper.AppConfig.PLACE_ID;
import static com.ecosense.app.helper.AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST;
import static com.ecosense.app.helper.AppConfig.PROXIMITY_RADIUS;
import static com.ecosense.app.helper.AppConfig.RATING;
import static com.ecosense.app.helper.AppConfig.REFERENCE;
import static com.ecosense.app.helper.AppConfig.STATUS;
import static com.ecosense.app.helper.AppConfig.SUPERMARKET_ID;
import static com.ecosense.app.helper.AppConfig.VICINITY;
import static com.ecosense.app.helper.AppConfig.ZERO_RESULTS;


public class NearByPlaceDetail extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ItemClickListener, View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener, OnMapReadyCallback,
        LocationListener {
    private static final String TAG = NearByPlaceTypeName.class.getSimpleName();

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.getUiSettings().setAllGesturesEnabled(true);
        getMyCurrentLocation();
//        showCurrentLocation();
    }

    @BindView(R.id.srl_NearbyPlaceDetail)
    SwipeRefreshLayout srl_NearbyPlaceDetail;

    @BindView(R.id.ll_list)
    LinearLayout ll_list;

    @BindView(R.id.rv_NearbyPlaceDetail)
    RecyclerView rv_NearbyPlaceDetail;

    private GoogleMap mMap;
    LocationManager locationManager;
    Toolbar toolbar;

    Boolean isConnected = false;
    ProgressDialog mProgressDialog;
    List<NearByPlace> nearByPlaceList = null;
    private GridLayoutManager lLayout;
    NearByPlaceListAdapter nearByPlaceListAdapter;

    String methodIntent = null;
    PlaceInfo intentPlaceInfo = null;
    UserSessionManger session = null;

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
        setContentView(R.layout.activity_near_by_place_detail);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Nearby Places");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        nearByPlaceList = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        lLayout = new GridLayoutManager(this, 1);
        rv_NearbyPlaceDetail.setHasFixedSize(true);
        rv_NearbyPlaceDetail.setLayoutManager(lLayout);
        rv_NearbyPlaceDetail.setItemAnimator(new DefaultItemAnimator());
        nearByPlaceListAdapter = new NearByPlaceListAdapter(this, nearByPlaceList);
        rv_NearbyPlaceDetail.setAdapter(nearByPlaceListAdapter);
        nearByPlaceListAdapter.setClickListener(this);

        srl_NearbyPlaceDetail.setOnRefreshListener(this);
        srl_NearbyPlaceDetail.setColorSchemeResources(R.color.colorAccent);
        srl_NearbyPlaceDetail.setNestedScrollingEnabled(true);
        srl_NearbyPlaceDetail.post(
                new Runnable() {
                    @Override
                    public void run() {
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showLocationSettings();
                            getMyCurrentLocation();
                        }
                    }
                }
        );

        onNewIntent(getIntent());
    }

    @Override
    public void onRefresh() {
        srl_NearbyPlaceDetail.setRefreshing(true);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            getMyCurrentLocation();
        }
        srl_NearbyPlaceDetail.setRefreshing(false);
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

            if (methodIntent != null && methodIntent.equals("PlaceSelect")) {
                intentPlaceInfo = (PlaceInfo) extras.getSerializable("PlaceInfo");
                Log.e(TAG, "intentPlaceInfo.getName() " + intentPlaceInfo.getName());
                toolbar.setTitle(intentPlaceInfo.getName());
                toolbar.invalidate();
            }
        } else {
            Log.e(TAG, "Bundle Is empty ");
        }

    }

    private void showLocationSettings() {
        Toast.makeText(this, "Location Error: GPS Disabled!", Toast.LENGTH_SHORT).show();
//        Snackbar snackbar = Snackbar
//                .make(mainCoordinatorLayout, "Location Error: GPS Disabled!",
//                        Snackbar.LENGTH_LONG)
//                .setAction("Enable", new View.OnClickListener() {
//                    @Override                    public void onClick(View v) {
//
//                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    }
//                });
//        snackbar.setActionTextColor(Color.RED);
//        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
//
//        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView
//                .findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(Color.YELLOW);
//
//        snackbar.show();
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
            NearByPlace nearByPlace = nearByPlaceList.get(position);
            if (view.getId() == R.id.img_btn_nrb_place_diraction) {
//                Log.e(TAG, "onClick Current = " + MyLat + "," + MyLong);
//                Log.e(TAG, "onClick diraction = " + nearByPlace.getLat() + "," + nearByPlace.getLng());
                Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + MyLat + "," + MyLong + "&daddr=" + nearByPlace.getLat() + "," + nearByPlace.getLng());
                Log.e(TAG, "onClick uri = " + uri);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onClick of Recycleview Exception = " + e.getMessage());

        }
    }

    MenuItem itemMap, itemList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_nearby_place_detail, menu);
        itemMap = menu.findItem(R.id.navigation_mapView);
        itemList = menu.findItem(R.id.navigation_listView);
        itemList.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.navigation_mapView:
                itemMap.setVisible(false);
                itemList.setVisible(true);
                ll_list.setVisibility(View.GONE);
                return true;
            case R.id.navigation_listView:
                itemMap.setVisible(true);
                itemList.setVisible(false);
//                nearByPlaceListAdapter.notifyDataSetChanged();
                ll_list.setVisibility(View.VISIBLE);

                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadNearByPlaces(double latitude, double longitude) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        } else {

            try {
                mProgressDialog = new ProgressDialog(NearByPlaceDetail.this);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            try {
                String type = intentPlaceInfo.getName();
                StringBuilder googlePlacesUrl =
                        new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
                googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
                googlePlacesUrl.append("&types=").append(URLEncoder.encode(type.toLowerCase(), "UTF-8"));

//           googlePlacesUrl.append("&keyword=").append("cruise");

                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);


                Log.e(TAG, googlePlacesUrl.toString());

                StringRequest postRequest = new StringRequest(Request.Method.GET, googlePlacesUrl.toString(),
                        response -> {
                            // response
                            if (!response.isEmpty()) {
                                Log.e("Response", response);
                                try {

                                    JSONObject person = new JSONObject(response);
                                    parseLocationResult(person);
//                            ObjectMapper objectMapper = new ObjectMapper();
//                            JsonNode rootNode = objectMapper.readTree(response.toString());
                                    Log.e(TAG, "" + person.toString());

                                } catch (Exception e) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                    e.printStackTrace();
                                }
                            } else {
                                TastyToast.makeText(getApplicationContext(), getString(R.string.no_record_found_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                    mProgressDialog = null;

                                }
                                Log.e(TAG, "Response Error");
                            }
                        }, error -> {
                    // error
                    Log.e("Error.Response", error.toString());
                    TastyToast.makeText(getApplicationContext(), getString(R.string.response_error), TastyToast.LENGTH_LONG, TastyToast.ERROR);
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
                    mProgressDialog = null;
                }

            }
        }
    }

    private void parseLocationResult(JSONObject result) {
        nearByPlaceList.clear();
        mMap.clear();
        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;
        String rating = null;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (jsonArray.length() != 0) {

                if (result.getString(STATUS).equalsIgnoreCase(OK)) {

                    mMap.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject place = jsonArray.getJSONObject(i);


                        id = place.getString(SUPERMARKET_ID);
                        place_id = place.getString(PLACE_ID);

                        if (!place.isNull(RATING)) {
                            rating = place.getString(RATING);
                        }

                        if (!place.isNull(NAME)) {
                            placeName = place.getString(NAME);
                        }
                        if (!place.isNull(VICINITY)) {
                            vicinity = place.getString(VICINITY);
                        }
                        latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                .getDouble(LATITUDE);
                        longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                .getDouble(LONGITUDE);


                        reference = place.getString(REFERENCE);
                        icon = place.getString(ICON);

                        NearByPlace nb = new NearByPlace();
                        nb.setId(id);
                        nb.setPlace_id(place_id);
                        nb.setName(placeName);

                        nb.setVicinity(vicinity);
                        nb.setRating(rating);
                        nb.setLat(latitude);
                        nb.setLng(longitude);
                        nb.setReference(reference);
                        nb.setIcon(icon);
                        nearByPlaceList.add(nb);
                        nearByPlaceListAdapter.notifyDataSetChanged();

                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(latitude, longitude);
                        markerOptions.position(latLng);
//                    markerOptions.icon(BitmapDescriptorFactory.fromFile(icon));
//                    markerOptions.title(placeName + " : " + vicinity);
                        markerOptions.title(placeName);
                        markerOptions.snippet(vicinity);
//                    "https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png
                        mMap.addMarker(markerOptions);

                    }

                    Log.e(TAG, "Size = " + nearByPlaceList.size());
                    Toast.makeText(getBaseContext(), jsonArray.length() + " " + intentPlaceInfo.getName() + " found!",
                            Toast.LENGTH_LONG).show();
                } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                    TastyToast.makeText(getApplicationContext(), "No Place found !!!", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            } else {
                TastyToast.makeText(getApplicationContext(), "No Place found !!!", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            }
        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_bin_locators);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @SuppressLint("MissingPermission")
    void getMyCurrentLocation() {
        final ProgressDialog progressDialog = new ProgressDialog(NearByPlaceDetail.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Finding Location...");
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.LocationListener locListener = new MyLocationListener();

            try {
                gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }
            try {
                network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            //don't start listeners if no provider is enabled
            //if(!gps_enabled && !network_enabled)
            //return false;

            if (gps_enabled) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
            }
            if (gps_enabled) {
                location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (network_enabled && location == null) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
            }
            if (network_enabled && location == null) {
                location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                MyLat = location.getLatitude();
                MyLong = location.getLongitude();
            } else {
                Location loc = getLastKnownLocation(this);
                if (loc != null) {
                    MyLat = loc.getLatitude();
                    MyLong = loc.getLongitude();
                }
            }
            locManager.removeUpdates(locListener);
            // removes the periodic updates from location listener to //avoid battery drainage. If you want to get location at the periodic intervals call this method using //pending intent.
            try {
// Getting address from found locations.

                // you can get more details other than this . like country code, state code, etc.
                Log.e(TAG, "" + MyLat);
                Log.e(TAG, "" + MyLong);

                LatLng latLng = new LatLng(MyLat, MyLong);
                mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                loadNearByPlaces(MyLat, MyLong);
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, 2000);

    }

    Double MyLat, MyLong;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    Location location;

    public static Location getLastKnownLocation(Context context) {
        Location location = null;
        LocationManager locationmanager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List list = locationmanager.getAllProviders();
        boolean i = false;
        Iterator iterator = list.iterator();
        do {
            //System.out.println("---------------------------------------------------------------------");
            if (!iterator.hasNext())
                break;
            String s = (String) iterator.next();
            //if(i != 0 && !locationmanager.isProviderEnabled(s))
            if (i != false && !locationmanager.isProviderEnabled(s))
                continue;
            // System.out.println("provider ===> "+s);
            @SuppressLint("MissingPermission") Location location1 = locationmanager.getLastKnownLocation(s);
            if (location1 == null)
                continue;
            if (location != null) {
                //System.out.println("location ===> "+location);
                //System.out.println("location1 ===> "+location);
                float f = location.getAccuracy();
                float f1 = location1.getAccuracy();
                if (f >= f1) {
                    long l = location1.getTime();
                    long l1 = location.getTime();
                    if (l - l1 <= 600000L)
                        continue;
                }
            }
            location = location1;
            // System.out.println("location  out ===> "+location);
            //System.out.println("location1 out===> "+location);
            i = locationmanager.isProviderEnabled(s);
            // System.out.println("---------------------------------------------------------------------");
        } while (true);
        return location;
    }

    // Location listener class. to get location.
    public class MyLocationListener implements android.location.LocationListener {
        public void onLocationChanged(Location location) {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
            }
        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

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

        Intent mIntent = new Intent(getApplicationContext(), NearByPlaceTypeName.class); // the activity that holds the fragment
//        Bundle b = new Bundle();
//        b.putSerializable("PlaceInfo", placeItem);
//        mIntent.setAction("PlaceSelect");
//        mIntent.putExtra("SelectedNameDetail", b);
        startActivity(mIntent);
        finish();
    }
}
