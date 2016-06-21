package com.biryanistudio.goprogateway;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.security.NetworkSecurityPolicy;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.container, new WifiFragment()).commit();

        // See README for documentation on this issue
        NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }
}