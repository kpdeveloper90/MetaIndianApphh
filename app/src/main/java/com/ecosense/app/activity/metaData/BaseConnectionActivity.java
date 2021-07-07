package com.ecosense.app.activity.metaData;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecosense.app.broadcastReceiver.NetworkReceiver;
import com.ecosense.app.helper.UserSessionManger;

public class BaseConnectionActivity extends AppCompatActivity {
    protected String TAG;
    protected UserSessionManger userSessionManger;
    private boolean isFirstTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userSessionManger = new UserSessionManger(this);
        userSessionManger.updateAppLanguage(this, userSessionManger.getAppLanguage());

        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        isFirstTime = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime)
            new NetworkReceiver().checkConnectivityAndShowAppropriateAlert();

        isFirstTime = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    protected void initToolbar(@NonNull final Toolbar toolbar, @NonNull final String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }
}
