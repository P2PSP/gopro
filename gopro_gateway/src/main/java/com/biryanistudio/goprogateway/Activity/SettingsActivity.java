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
import android.widget.Toast;

import com.biryanistudio.goprogateway.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_settings);
        setSwitch();
        setFacebookLogin();
    }

    private void setSwitch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destination = sharedPreferences.getString("DESTINATION", "");
        if (destination.equals("Facebook"))
            ((Switch) findViewById(R.id.switch_destination)).setChecked(true);
        ((Switch) findViewById(R.id.switch_destination)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (!checked) {
            saveYouTubeApi();
        } else {
            checkFacebook();
        }
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

    private void saveYouTubeApi() {
        SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        EditText editText = (EditText) findViewById(R.id.edit_text_youtube_api);
        if (editText.getText() != null) {
            String api = editText.getText().toString();
            if (api.length() == 19) {
                sharedPrefsEditor.putString("DESTINATION", "YouTube").apply();
                sharedPrefsEditor.putString("YOUTUBE_API", api).apply();
                return;
            }
        }
        sharedPrefsEditor.putString("DESTINATION", "").apply();
        Toast.makeText(this, "To livestream to YouTube, " +
                "please enter a valid YouTube API key.", Toast.LENGTH_SHORT).show();
    }

    private void checkFacebook() {
        SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        if (AccessToken.getCurrentAccessToken() == null) {
            sharedPrefsEditor.putString("DESTINATION", "").apply();
            Toast.makeText(this, "To livestream to Facebook, " +
                    "please login via the Settings screen", Toast.LENGTH_SHORT).show();
        } else {
            sharedPrefsEditor.putString("DESTINATION", "Facebook").apply();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
