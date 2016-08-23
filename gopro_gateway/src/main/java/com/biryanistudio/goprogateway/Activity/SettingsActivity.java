package com.biryanistudio.goprogateway.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private final String TAG = getClass().getSimpleName();
    private CallbackManager callbackManager;
    private String mUploadDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        setContentView();
        setSwitch();
        setYouTubeLogin();
        setFacebookLogin();
    }

    private void setContentView() {
        setContentView(R.layout.activity_settings);
        showYouTubeLogin(true);
        showFacebookLogin(false);
        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadDestination.equals("YouTube")) saveYouTubeApiKey();
                else checkFacebookLogin();
            }
        });
    }

    private void setSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUploadDestination = sharedPreferences.getString("DESTINATION", "");
        if (mUploadDestination.equals("YouTube")) {
            fillEditText();
        }
        ((Switch) findViewById(R.id.switch_destination)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (!checked) {
            showYouTubeLogin(true);
            showFacebookLogin(false);
            mUploadDestination = "YouTube";
        } else {
            showYouTubeLogin(false);
            showFacebookLogin(true);
            mUploadDestination = "Facebook";
        }
    }

    private void setYouTubeLogin() {
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        editText.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.youtube_banner),
                null, null, null);
    }

    private void setFacebookLogin() {
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPublishPermissions("publish_actions");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String token = AccessToken.getCurrentAccessToken().getUserId();
                Log.i(TAG, "Facebook login successful: " + token);
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Facebook login cancelled.");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i(TAG, exception.toString());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showYouTubeLogin(boolean show) {
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        if (show) {
            editText.animate().alpha(1.0f).start();
            editText.setEnabled(true);
        } else {
            editText.animate().alpha(0.0f).start();
            editText.setEnabled(false);
        }
    }

    private void showFacebookLogin(boolean show) {
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        if (show) {
            loginButton.animate().alpha(1.0f).start();
            loginButton.setEnabled(true);
        } else {
            loginButton.animate().alpha(0.0f).start();
            loginButton.setEnabled(false);
        }
    }

    private void fillEditText() {
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String api = sharedPrefs.getString("YOUTUBE_API", "");
        editText.setText(api);
    }

    private void saveYouTubeApiKey() {
        SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        if (editText.getText() != null) {
            String api = editText.getText().toString();
            if (api.length() == 19) {
                sharedPrefsEditor.putString("DESTINATION", "YouTube").apply();
                sharedPrefsEditor.putString("YOUTUBE_API", api).apply();
                finish();
            }
        }
        sharedPrefsEditor.putString("DESTINATION", "").apply();
        Utility.showSnackbar(findViewById(android.R.id.content),
                "To livestream to YouTube, please enter a valid YouTube API key.");
    }

    private void checkFacebookLogin() {
        SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        if (AccessToken.getCurrentAccessToken() == null) {
            sharedPrefsEditor.putString("DESTINATION", "").apply();
            Utility.showSnackbar(findViewById(android.R.id.content),
                    "To livestream to Facebook, please login via the Settings screen");
        } else {
            sharedPrefsEditor.putString("DESTINATION", "Facebook").apply();
            finish();
        }
    }
}
