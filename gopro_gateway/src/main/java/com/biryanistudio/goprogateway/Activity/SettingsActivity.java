package com.biryanistudio.goprogateway.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.biryanistudio.goprogateway.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_settings);
        setFacebookLogin();
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
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        if(editText.getText() != null) {
            String api = editText.getText().toString();
            SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
            sharedPrefsEditor.putString("YOUTUBE_API", api).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
