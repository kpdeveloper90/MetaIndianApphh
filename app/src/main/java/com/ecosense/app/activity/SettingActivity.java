package com.ecosense.app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.ecosense.app.R;
import com.ecosense.app.helper.preference.EncryptedPreferenceHelper;
import com.ecosense.app.remote.RetrofitHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity {

    ImageView closenow;
    Spinner select_user;
    TextView submit;
    Context context = this;
    int currentid = 0;
    private EncryptedPreferenceHelper preferenceHelper;
    ArrayList<String> projectsids = new ArrayList<>();
    ArrayList<String> projectName = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setTitle("Setting");
        submit = findViewById(R.id.submit);
        closenow = findViewById(R.id.closenow);
        select_user = findViewById(R.id.select_user);
        preferenceHelper = new EncryptedPreferenceHelper(this);


        closenow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferenceHelper.setCurrentProject(projectsids.get(select_user.getSelectedItemPosition()));
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        getroute();
    }


    void getroute() {
        RetrofitHelper.getAllProject().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseBody1 = response.body().string();
                    Log.e("onSuccess: ", responseBody1);
                    preferenceHelper.setAllProject(responseBody1);
                    setprojectid();
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure: ", t.toString());
            }
        });

    }


    void setprojectid() {
        projectsids = new ArrayList<>();
        projectName = new ArrayList<>();
        String projectsall = preferenceHelper.getAllProject();
        try {
            JSONObject jsonObject = new JSONObject(projectsall);
            if (jsonObject.getString("code").equals("200")) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    projectsids.add(jsonArray.getJSONObject(i).getString("_id"));
                    projectName.add(jsonArray.getJSONObject(i).getString("name"));
                    if (jsonArray.getJSONObject(i).getString("_id").equals(preferenceHelper.getCurrentProject())) {
                        currentid = i;
                    }
                }
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                    (this, R.layout.spinnertext,
                            projectName); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(R.layout
                    .spinnertext);
            select_user.setAdapter(spinnerArrayAdapter);

            if (projectName.size() > 0) {
                select_user.setSelection(currentid);
            }
        } catch (Exception e) {

        }
    }


}