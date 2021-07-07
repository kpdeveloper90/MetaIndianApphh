package com.ecosense.app.helper.textWatchers;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import com.ecosense.app.R;

public class RequiredTextWatcher implements TextWatcher {

    private TextInputLayout textInputLayout;
    private Context context;
    private String errorText;

    public RequiredTextWatcher(@NonNull final TextInputLayout textInputLayout, @NonNull final Context context, final String errorText) {
        this.textInputLayout = textInputLayout;
        this.context = context;

        if (TextUtils.isEmpty(errorText))
            this.errorText = context.getString(R.string.et_error_field_required);
        else
            this.errorText = errorText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s))
            textInputLayout.setError(errorText);
        else
            textInputLayout.setError(null);
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
