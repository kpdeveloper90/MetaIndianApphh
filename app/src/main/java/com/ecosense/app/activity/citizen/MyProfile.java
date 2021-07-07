package com.ecosense.app.activity.citizen;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.LoginDetail;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class MyProfile extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = MyProfile.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    Boolean isConnected = false;
    static ProgressDialog mProgressDialog = null;
    UserSessionManger session = null;


    @BindView(R.id.sp_wardNo)
    Spinner sp_wardNo;
    @BindView(R.id.img_profile_capturePic)
    ImageView img_profile_capturePic;

    @BindView(R.id.img_profile_pic)
    CircularImageView img_profile_pic;

    @BindView(R.id.edt_pro_name)
    TextInputEditText edt_pro_name;

    @BindView(R.id.edt_pro_email)
    TextInputEditText edt_pro_email;

    @BindView(R.id.btn_save_profile)
    Button btn_save_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);


        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.include5);
        toolbar.setTitle("My Profile");
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
            edt_pro_name.setText(session.getpsName());
            edt_pro_email.setText(session.getEmailLogin());
            setWardNumber(session.getuserWardNo());
            if (session.getUserProfilePic() != null) {
//                String url = session.getMyServerIP() + session.getUserProfilePic();
                if (session.getUserProfilePic().length() > 0) {
                    Log.e(TAG, "getUserProfilePic is hear");
                    Glide.with(this).load(Connection.decodeFromBase64ToBitmap(session.getUserProfilePic()))
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_profile_pic);
                } else {
                    Log.e(TAG, "getUserProfilePic is size hear");
                    Glide.with(this).load(R.drawable.ic_user_1)
                            .apply(RequestOptions.centerCropTransform())
                            .into(img_profile_pic);
                }
            } else {
                Log.e(TAG, "getUserProfilePic is not hear => " + session.getUserProfilePic());
                Glide.with(this).load(R.drawable.ic_user_1)
                        .apply(RequestOptions.centerCropTransform())
                        .into(img_profile_pic);
            }
//            img_text_profile_pic.setImageDrawable(getCharacterImage(session.getpsName().substring(0, 1)));
        }

        btn_save_profile.setOnClickListener(this);
        img_profile_capturePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_save_profile) {

            if (!validate()) {
                return;
            }
            Bitmap profile_Pic_bitmap = ((BitmapDrawable) img_profile_pic.getDrawable()).getBitmap();

            LoginDetail lo = new LoginDetail();
            lo.setUserId(session.getpsNo());
            lo.setRegfname(edt_pro_name.getText().toString());
            lo.setRegemail(edt_pro_email.getText().toString());
            lo.setReg_wardno(sp_wardNo.getSelectedItem().toString());
            lo.setReg_photo(Connection.encodeBitmapToFromBase64(profile_Pic_bitmap));

            updateUserDetail(lo);
        }
        if (v == img_profile_capturePic) {
            showPictureDialog();
        }
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

    public void setWardNumber(String locType) {
        Log.e(TAG,"setWardNumber + sp_wardNo ="+locType);
        ArrayAdapter<CharSequence> adapter_wardNo = ArrayAdapter.createFromResource(MyProfile.this, R.array.wardNumber, android.R.layout.simple_spinner_item);
        adapter_wardNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_wardNo.setAdapter(adapter_wardNo);
        if (locType != null) {
            int spinnerPosition = adapter_wardNo.getPosition(locType);
            sp_wardNo.setSelection(spinnerPosition);
        } else {
            sp_wardNo.setSelection(0);
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = edt_pro_email.getText().toString();
        String unm = edt_pro_name.getText().toString();

//        reg_wardno
//        if (mno.isEmpty() || mno.length() < 10 || mno.length() > 10) {
        if (unm.isEmpty()) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.Please_Enter_Name), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            edt_pro_name.setText(session.getpsName());

            valid = false;
        } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            TastyToast.makeText(getApplicationContext(), getString(R.string.error_email_id), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            edt_pro_email.setText(session.getEmailLogin());
            valid = false;
        } else {

        }
        return valid;
    }

    public void updateUserDetail1(LoginDetail loginDetail) {

        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            show_alert_Dialog_singlebutton("No internet connection. \nPlease Turn on internet.");
        } else {


            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please Wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            new android.os.Handler().postDelayed(() -> {
                try {
                    String data = null;
                    data = "{}";
                    URLEncoder.encode(data, "UTF-8");


                    String url = session.getMyServerIP() + "/api/resource/Registration/" + URLEncoder.encode(loginDetail.getUserId(), "UTF-8");
                    Log.e(TAG, "url = > " + url);
                    StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
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
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            LoginDetail loginDetail1 = objectMapper.readValue(statusData.toString(), LoginDetail.class);

                                            Log.e(TAG, "visitorDetails GatePass Number = " + loginDetail1.getRegfname() +
                                                    "status = " + loginDetail1.getUserId()
                                            );

                                            session.setpsNo(loginDetail1.getUserId());
                                            session.setpsName(loginDetail1.getRegfname());
                                            session.setUserProfilePic(loginDetail1.getReg_photo());
                                            session.setDlNumber(loginDetail1.getReg_license_number());
                                            session.setMobileNumber(loginDetail1.getRegmobile());
                                            session.setuserSubType(loginDetail1.getRegsubusertype());
                                            session.setuserType(loginDetail1.getRegusertype());

                                            if (loginDetail1.getRegemail() != null) {
                                                session.setEmailLogin(loginDetail1.getRegemail());
                                            }

                                            startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                            finish();

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

                                        } else {
                                            show_alert_Dialog_singlebutton(getString(R.string.could_no_send_error));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;

                                        }
                                    }
                                } else {
                                    show_alert_Dialog_singlebutton(getString(R.string.response_error));
                                }

                            }, error -> {
                        // error

                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        show_alert_Dialog_singlebutton(getString(R.string.network_error));

                        Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            Gson gson = new Gson();
                            String regpojo = gson.toJson(loginDetail);
                            Log.e(TAG, "getParams   = " + regpojo);

                            params.put("data", regpojo);
                            return params;
                        }

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


    public void updateUserDetail(LoginDetail loginDetail) {
        try {
            mProgressDialog = new ProgressDialog(MyProfile.this);
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
//                url = session.getMyServerIP() + "/api/resource/Complaints";

//                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(tempcomplaintsDetails.getComId(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Registration/" + URLEncoder.encode(loginDetail.getUserId(), "UTF-8");
                Log.e(TAG, url);
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT, url,
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

                                                LoginDetail loginDetail1 = objectMapper.readValue(statusData.toString(), LoginDetail.class);

                                                Log.e(TAG, "visitorDetails GatePass Number = " + loginDetail1.getRegfname() +
                                                        "status = " + loginDetail1.getUserId()
                                                );

                                                session.setpsNo(loginDetail1.getUserId());
                                                session.setpsName(loginDetail1.getRegfname());
                                                session.setUserProfilePic(loginDetail1.getReg_photo());
                                                session.setMobileNumber(loginDetail1.getRegmobile());
                                                session.setuserSubType(loginDetail1.getRegsubusertype());
                                                session.setuserType(loginDetail1.getRegusertype());
                                                session.setuserWardNo(loginDetail1.getReg_wardno());
                                                if (loginDetail1.getRegemail() != null) {
                                                    session.setEmailLogin(loginDetail1.getRegemail());
                                                }
                                                TastyToast.makeText(getApplicationContext(), "Profile Successfully Updated", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                                startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                                finish();

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
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
                            jsonObject.put("reg_photo", loginDetail.getReg_photo());
//                            jsonObject.put("reg_photo", "");
                            jsonObject.put("regemail", loginDetail.getRegemail());
                            jsonObject.put("regfname", loginDetail.getRegfname());
                            jsonObject.put("reg_wardno", loginDetail.getReg_wardno());
//                    reglocation
//                    reglatitude
//                    reglongitude
                            Log.e(TAG, "data=" + jsonObject.toString());
                            params.put("data", jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
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

    public void updateUserDetail_JsonObjectRequest(LoginDetail loginDetail) {
        try {
            mProgressDialog = new ProgressDialog(MyProfile.this);
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
                    jsonObject.put("reg_photo", loginDetail.getReg_photo());
                    jsonObject.put("regemail", loginDetail.getRegemail());
                    jsonObject.put("regfname", loginDetail.getRegfname());
                    jsonObject.put("reg_wardno", loginDetail.getReg_wardno());
//                    reglocation
//                    reglatitude
//                    reglongitude

                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }

                URLEncoder.encode("", "UTF-8");

                String url = null;
//                url = session.getMyServerIP() + "/api/resource/Complaints";

//                url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(tempcomplaintsDetails.getComId(), "UTF-8");
                url = session.getMyServerIP() + "/api/resource/Registration/" + URLEncoder.encode(loginDetail.getUserId(), "UTF-8");
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
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

                                                LoginDetail loginDetail1 = objectMapper.readValue(statusData.toString(), LoginDetail.class);

                                                Log.e(TAG, "visitorDetails GatePass Number = " + loginDetail1.getRegfname() +
                                                        "status = " + loginDetail1.getUserId()
                                                );

                                                session.setpsNo(loginDetail1.getUserId());
                                                session.setpsName(loginDetail1.getRegfname());
                                                session.setUserProfilePic(loginDetail1.getReg_photo());
                                                session.setMobileNumber(loginDetail1.getRegmobile());
                                                session.setuserSubType(loginDetail1.getRegsubusertype());
                                                session.setuserType(loginDetail1.getRegusertype());

                                                if (loginDetail1.getRegemail() != null) {
                                                    session.setEmailLogin(loginDetail1.getRegemail());
                                                }
                                                TastyToast.makeText(getApplicationContext(), "Profile Successfully Updated", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                                startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
                                                finish();

                                                if (mProgressDialog != null) {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = null;
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

    private int GALLERY = 1, CAMERA = 2;

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                    String path = saveImage(bitmap);
//                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();

                    img_profile_pic.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
//                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");


            img_profile_pic.setImageBitmap(thumbnail);

//            Log.e(TAG, "Image Data = " + data.getExtras().get("data"));
//            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();

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

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    public Drawable getCharacterImage(String s) {
        ColorGenerator generator = ColorGenerator.DEFAULT; // or use DEFAULT
// generate random color
        int color1 = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
//                .withBorder(3) /* thickness in px */
//                .textColor(Color.BLACK)
//                .useFont(Typeface.DEFAULT)
//                .fontSize(30) /* size in px */
//                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(s, getRandomMaterialColor("400"));


        return drawable;
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
            TastyToast.makeText(getApplicationContext(), getString(R.string.back_online), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
            TastyToast.makeText(getApplicationContext(), getString(R.string.you_are_offline), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
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
        super.onBackPressed();

        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
        finish();
    }

}
