package com.biryanistudio.goprogateway;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.biryanistudio.goprogateway.Fragment.WifiFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // See README for documentation about NetworkSecurityPolicy
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.container, new WifiFragment()).commit();

        // NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
        Log.i("TAG", Arrays.asList(Build.SUPPORTED_ABIS).toString());
    }
}