package com.biryanistudio.goprogateway.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private String mDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        setDefaultDestination();
        setContentView();
        setFacebookLogin();
    }

    private void setDefaultDestination() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDestination = sharedPreferences.getString("DESTINATION", "");
        if (mDestination.equals(""))
            sharedPreferences.edit().putString("DESTINATION", "YouTube").commit();
    }

    private void setContentView() {
        setContentView(R.layout.activity_settings);
        if (mDestination.equals("YouTube"))
            ((Switch) findViewById(R.id.switch_destination)).setChecked(false);
        else
            ((Switch) findViewById(R.id.switch_destination)).setChecked(true);
        ((Switch) findViewById(R.id.switch_destination)).setOnCheckedChangeListener(this);
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
    protected void onStop() {
        super.onStop();
        if(mDestination.equals("YouTube")) saveYouTubeApi();
        else if(mDestination.equals("Facebook")) checkFacebook();
    }

    private void saveYouTubeApi() {
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        if (editText.getText() != null) {
            String api = editText.getText().toString();
            Utility.checkYouTube(this, api);
        }
    }

    private void checkFacebook() {
        Utility.checkFacebook(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        if (!b) {
            mDestination = "YouTube";
            sharedPrefsEditor.putString("DESTINATION", "YouTube").commit();
        } else {
            mDestination = "Facebook";
            sharedPrefsEditor.putString("DESTINATION", "Facebook").commit();
        }

    }
}
