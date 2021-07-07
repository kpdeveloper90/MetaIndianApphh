package com.ecosense.app.activity.citizen;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import com.android.volley.Response;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.textfield.TextInputLayout;
import com.ramotion.directselect.DSListView;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.ecosense.app.pojo.Suggestions;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class NewSuggestions extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = NewSuggestions.class.getSimpleName();
    public static String SERVER_URL = null;
    private Toolbar toolbar;

    @BindView(R.id.img_sug_image)
    ImageView img_sug_image;

    @BindView(R.id.img_sug_captur_pic)
    ImageView img_sug_captur_pic;

    @BindView(R.id.ds_picker)
    DSListView ds_categorie_picker;

    @BindView(R.id.tag_sug_desc)
    TextInputLayout tag_sug_desc;

    @BindView(R.id.edt_sug_desc)
    TextInputEditText edt_sug_desc;

    @BindView(R.id.im_sug_btn_submit)
    ImageView im_sug_btn_submit;
    Boolean isConnected = false;
    static ProgressDialog mProgressDialog;
    UserSessionManger session = null;
    public static final int Request_Code_CapturePIC = 102;
    static private Uri imageUri;
    static private String targetPath;
    static Boolean FlagImg_AvailableOrNot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        /* Set the app into full screen mode */
//        getWindow().getDecorView().setSystemUiVisibility(flags);
        setContentView(R.layout.activity_new_suggestions);


        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("New Suggestion");
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
        edt_sug_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > tag_sug_desc.getCounterMaxLength())
                    tag_sug_desc.setError("Max character length is " + tag_sug_desc.getCounterMaxLength());
                else
                    tag_sug_desc.setError(null);

            }
        });

        im_sug_btn_submit.setOnClickListener(this);
        img_sug_captur_pic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == img_sug_captur_pic) {

            takePhotoFromBackCamera(Request_Code_CapturePIC);
        }
        if (v == im_sug_btn_submit) {

            getSugTextData();
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


    public void getSugTextData() {
        try {

//        byte[] img_capturePic = getBitmapAsByteArray(com_Pic_bitmap);

            String sug_dec = edt_sug_desc.getText().toString().trim();
            String categorie = ds_categorie_picker.getSelectedItem().toString();


//        String rDate_time = new SimpleDateFormat("dd-MMM-yyyy HH:mm a").format(new Date());
            String rDate_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            Log.e(TAG, "categorie = " + categorie
                    + "\n rDate_time = " + rDate_time
                    + "\n sug_dec = " + sug_dec
            );


//            if (!FlagImg_AvailableOrNot) {
//                TastyToast.makeText(getApplicationContext(), "Please Capture Image", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
//            } else
            if (categorie.isEmpty()) {
                TastyToast.makeText(getApplicationContext(), "Enter categorie", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else if (sug_dec.isEmpty()) {
                TastyToast.makeText(getApplicationContext(), "Enter suggestion", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            } else {

                Suggestions sug = new Suggestions();

                if (!FlagImg_AvailableOrNot) {
                    sug.setSugphoto(null);
                } else {
                    Bitmap com_Pic_bitmap = ((BitmapDrawable) img_sug_image.getDrawable()).getBitmap();
                    sug.setSugphoto(Connection.encodeBitmapToFromBase64(com_Pic_bitmap));
                }

                sug.setSugcategory(categorie);
                sug.setSuggestion(sug_dec);
                sug.setSugdate(rDate_time);
                sug.setSugmobile(session.getMobileNumber());
                sug.setSuguname(session.getpsName());
                sug.setSugemail(session.getEmailLogin());

                FlagImg_AvailableOrNot = false;

                sendNew_SuggestionsRequest(sug);

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception getMessage = " + e.getMessage());
            TastyToast.makeText(getApplicationContext(), "Error..", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
    }


    public void sendNew_SuggestionsRequest_jsonObjectRequest(Suggestions suggestions) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), "No internet connection. \nPlease Turn on internet.", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(NewSuggestions.this);
                mProgressDialog.setMessage(getString(R.string.no_internet_alert));
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } catch (Exception e) {
            }
            new android.os.Handler().postDelayed(() -> {
                try {


                    URLEncoder.encode("", "UTF-8");

                    String url = null;
                    url = session.getMyServerIP() + "/api/resource/Suggestion";


//                url = session.getMyServerIP() + "/api/resource/ActivityMaster/" + URLEncoder.encode(routeinfoDetail.getName(), "UTF-8");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("sugphoto", suggestions.getSugphoto());
                        jsonObject.put("sugcategory", suggestions.getSugcategory());
                        jsonObject.put("suggestion", suggestions.getSuggestion());
                        jsonObject.put("sugdate", suggestions.getSugdate());
                        jsonObject.put("sugmobile", suggestions.getSugmobile());
                        jsonObject.put("suguname", suggestions.getSuguname());
                        jsonObject.put("sugemail", suggestions.getSugemail());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

                                                    Suggestions compDetail = objectMapper.readValue(statusData.toString(), Suggestions.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }
                                                    Log.e(TAG, "Responce = " + compDetail.getSugId());
                                                    TastyToast.makeText(getApplicationContext(), "Thank you for Suggestion.", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
//                                            startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
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

    public void sendNew_SuggestionsRequest(Suggestions suggestions) {
        isConnected = checkConnection();
        if (!isConnected) {
            Log.e(TAG, " Not connected ");
            TastyToast.makeText(getApplicationContext(), getString(R.string.no_internet_alert), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        } else {
            try {
                mProgressDialog = new ProgressDialog(NewSuggestions.this);
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
                    url = session.getMyServerIP() + "/api/resource/Suggestion";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        if (!response.toString().trim().equalsIgnoreCase("{}")) {
                                            try {
                                                ObjectMapper objectMapper = new ObjectMapper();
                                                JsonNode rootNode = objectMapper.readTree(response.toString());
                                                JsonNode statusData = rootNode.path("data");
                                                Log.e(TAG, "statusData = " + statusData.toString());
                                                if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                                    Suggestions compDetail = objectMapper.readValue(statusData.toString(), Suggestions.class);

                                                    if (mProgressDialog != null) {
                                                        mProgressDialog.dismiss();
                                                        mProgressDialog = null;
                                                    }
                                                    Log.e(TAG, "Responce = " + compDetail.getSugId());
                                                    showComp_ConformationDialog(compDetail);
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

                            },
                            new Response.ErrorListener() {
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

                            JSONObject jsonObjectd = new JSONObject();
                            try {
                                jsonObjectd.put("sugphoto", suggestions.getSugphoto());
                                jsonObjectd.put("sugcategory", suggestions.getSugcategory());
                                jsonObjectd.put("suggestion", suggestions.getSuggestion());
                                jsonObjectd.put("sugdate", suggestions.getSugdate());
                                jsonObjectd.put("sugmobile", suggestions.getSugmobile());
                                jsonObjectd.put("suguname", suggestions.getSuguname());
                                jsonObjectd.put("sugemail", suggestions.getSugemail());

                                params.put("data", jsonObjectd.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return params;
                        }

                    };
                    String tag_string_req = "object_req";
                    ConnactionCheckApplication.getInstance().addToRequestQueue(stringRequest, tag_string_req);
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
    Dialog comp_confDialo;
    ImageView img_popUp_close;
    TextView tv_com_com_popUp_date, tv_com_popUp_tag, tv_com_popUp_number;

    public void showComp_ConformationDialog(Suggestions sugDetail) {

        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.dialog_com_conformation, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        comp_confDialo = new Dialog(NewSuggestions.this);
        comp_confDialo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        comp_confDialo.setContentView(root);
        comp_confDialo.setCancelable(false);
        comp_confDialo.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        comp_confDialo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        img_popUp_close = (ImageView) comp_confDialo.findViewById(R.id.img_popUp_close);
        tv_com_popUp_number = (TextView) comp_confDialo.findViewById(R.id.tv_com_popUp_number);
        tv_com_com_popUp_date = (TextView) comp_confDialo.findViewById(R.id.tv_com_com_popUp_date);
        tv_com_popUp_tag = (TextView) comp_confDialo.findViewById(R.id.tv_com_popUp_tag);

            tv_com_popUp_tag.setVisibility(View.GONE);

        tv_com_popUp_number.setText("Thanks For Suggestion");
        tv_com_com_popUp_date.setText(dateFormat(sugDetail.getSugdate()));

        img_popUp_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comp_confDialo.dismiss();
                onBackPressed();
            }
        });

//        List<BillFormat> billFormatList
        comp_confDialo.show();


    }


    Dialog dialog_progress;
    TextView tv_prog_msg;

    public void show_dialog_progress(String Msg) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            final View root = inflater.inflate(R.layout.dialog_progress, null);
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialog_progress = new Dialog(NewSuggestions.this);
            dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_progress.setContentView(root);
            dialog_progress.setCancelable(false);
            dialog_progress.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
            dialog_progress.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


            tv_prog_msg = (TextView) dialog_progress.findViewById(R.id.tv_prog_msg);
            tv_prog_msg.setText(Msg);
            dialog_progress.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {

            return;
        }

        if (requestCode == Request_Code_CapturePIC) {
            imageUri = null;
            targetPath = null;

            Bitmap VISIPIC_bitmao = (Bitmap) data.getExtras().get("data");

//                int orientation = Exif.getOrientation(getBitmapAsByteArray(VISIPIC_bitmao));
//                img_capturePic.setImageBitmap(rotateImage(VISIPIC_bitmao, orientation));
            img_sug_image.setImageBitmap(VISIPIC_bitmao);
//                imageUri = getImageUri(getApplicationContext(), VISIPIC_bitmao);
//                targetPath = getRealPathFromURI(imageUri);
            Log.e(TAG, "imageUri = " + imageUri);
            Log.e(TAG, "targetPath = " + targetPath);
//                uploadPhotoOnServer();
            FlagImg_AvailableOrNot = true;

        }
    }
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private void takePhotoFromBackCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 2);
        Log.d(TAG, "putExtra:android.intent.extras.CAMERA_FACING");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "startActivityForResult");
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(intent, requestCode);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                Log.d(TAG, "startActivityForResult");
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d(TAG, "onRequestPermissionsResult: permission name = " + permissions[i] + "  Result = " + grantResults[i]);
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize our map
//                    initMap();

                }
            }
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

    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        startActivity(new Intent(getApplicationContext(), CitizenDashBoard.class));
    }
}
