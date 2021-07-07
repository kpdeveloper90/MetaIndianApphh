package com.ecosense.app.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mvivekanandji.validatingtextinputlayout.TextInputLayoutValidator;
import com.mvivekanandji.validatingtextinputlayout.ValidatingTextInputLayout;

import java.util.List;

import com.ecosense.app.R;
import com.ecosense.app.activity.metaData.MetaDataDashBoard;
import com.ecosense.app.databinding.ActivityLoginBinding;
import com.ecosense.app.firebase.Analytics;
import com.ecosense.app.firebase.CrashAnalytics;
import com.ecosense.app.helper.FingerprintHelper;
import com.ecosense.app.helper.preference.EncryptedPreferenceHelper;
import com.ecosense.app.pojo.model.User;
import com.ecosense.app.remote.MetadataUserBackend;

public class LoginActivity extends AppCompatActivity implements
        TextInputLayoutValidator.ValidatorListener {

    public static final String SESSION_EXPIRED_KEY = "session_expired";

    private ActivityLoginBinding rootBinding;
    private FingerprintHelper fingerprintHelper;
    private MetadataUserBackend metadataUserBackend;
    private TextInputLayoutValidator inputLayoutValidator;
    private EncryptedPreferenceHelper preferenceHelper;
    private boolean isFirstTry;
    private Analytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(rootBinding.getRoot());
        preferenceHelper = new EncryptedPreferenceHelper(this);
if(preferenceHelper.getMetaUserId()!=null){
    startMetadataDashboard();
}
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && preferenceHelper.getMetaUserId() != null
                && !getIntent().getBooleanExtra(SESSION_EXPIRED_KEY, false)) {
            rootBinding.ivFingerprint.setVisibility(View.VISIBLE);
            initializeFingerprint();
        }

        if (getIntent().getBooleanExtra(SESSION_EXPIRED_KEY, false))
            resetMetadataUserPreferences();

        initConfigs();
        initListeners();

        if (getIntent().getBooleanExtra(SESSION_EXPIRED_KEY, false))
            resetMetadataUserPreferences();
    }

    private void initConfigs() {
        analytics = Analytics.getInstance();
        isFirstTry = true;
        inputLayoutValidator = new TextInputLayoutValidator(rootBinding.getRoot(), this);
        metadataUserBackend = new MetadataUserBackend(this, metadataUser -> startMetadataDashboard());
    }

    @SuppressWarnings("ConstantConditions")
    private void initListeners() {
        rootBinding.mbLogin.setOnClickListener(v -> {
            if (isFirstTry)
                inputLayoutValidator.validate();

            isFirstTry = false;

            if (inputLayoutValidator.isValid()) {
                metadataUserBackend.authenticate(new User(
                        rootBinding.etUsername.getText().toString(),
                        rootBinding.etPassword.getText().toString()), 500);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initializeFingerprint() {
        fingerprintHelper = new FingerprintHelper(this, new FingerprintHelper.OnAuthenticationListener() {
            @Override
            public void onSuccess() {
                CrashAnalytics.setUserId(preferenceHelper.getMetaUserId());
                Analytics.setUserId(preferenceHelper.getMetaUserId());
                CrashAnalytics.log("Logged via fingerprint");
                Analytics.getInstance().logSignInSuccess(Analytics.SIGN_IN_METHOD_FINGERPRINT, preferenceHelper.getMetaUserEmail());

                startMetadataDashboard();
            }

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                Toast.makeText(LoginActivity.this, getString(R.string.toast_fingerprint_authentication_failed), Toast.LENGTH_SHORT).show();
                Analytics.getInstance().logSignInFailed(Analytics.SIGN_IN_METHOD_FINGERPRINT, preferenceHelper.getMetaUserEmail(), errorMsg);
                CrashAnalytics.log("Fingerprint sign in failed:" + errorMsg);
            }
        });

        rootBinding.ivFingerprint.setOnClickListener(v -> {
            if (FingerprintHelper.canAuthenticate(LoginActivity.this, rootBinding.getRoot(), true)) {
                Analytics.getInstance().logSignInAttempt(Analytics.SIGN_IN_METHOD_FINGERPRINT, preferenceHelper.getMetaUserEmail());
                fingerprintHelper.authenticate(null, null, null);
            }
        });
    }

    private void startMetadataDashboard() {
        Intent intent = new Intent(LoginActivity.this, MetaDataDashBoard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void resetMetadataUserPreferences() {
        EncryptedPreferenceHelper preferenceHelper = new EncryptedPreferenceHelper(this);

        preferenceHelper.setMetaUserId(null);
        preferenceHelper.setMetaUserName(null);
        preferenceHelper.setMetaUserEmail(null);
        preferenceHelper.setMetaUserMobile(null);
        preferenceHelper.setMetaUserRole(null);
        preferenceHelper.setMetaUserToken(null);

        CrashAnalytics.log("MetaUser preferences RESET!.");
        Analytics.getInstance().logEvent("meta_user_preferences_reset");
    }

    @Override
    public void onValidateErrors(List<ValidatingTextInputLayout> errorLayoutList, List<TextInputLayoutValidator.ValidationError> validationErrorList) {

    }

    @Override
    public void onError(ValidatingTextInputLayout inputLayout, TextInputLayoutValidator.ValidationError validationError, boolean isErrorOnValidate) {
        rootBinding.mbLogin.setEnabled(false);
        inputLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger)));
        inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger)));
    }


    @Override
    public void onErrorResolved(ValidatingTextInputLayout inputLayout) {
        inputLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
        inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
    }

    @Override
    public void onSuccess() {
        rootBinding.mbLogin.setEnabled(true);
    }
}