package com.biryanistudio.goprogateway.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.biryanistudio.goprogateway.CheckDeviceTask;
import com.biryanistudio.goprogateway.FFmpegStream.FFmpegStreamFromGoPro;
import com.biryanistudio.goprogateway.FFmpegStream.FFmpegStreamFromSJCAM;
import com.biryanistudio.goprogateway.Interface.IWifiAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.WifiNetworkCallback;
import com.biryanistudio.goprogateway.R;
import com.facebook.FacebookSdk;

import java.util.concurrent.ExecutionException;

/**
 * Created by sravan953 on 13/06/16.
 */
public class MainActivity extends AppCompatActivity implements IWifiAvailable, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mLocPermission = true;
    private boolean mStoragePermission = true;

    private ImageButton mButtonUpload;
    private ImageButton mButtonConnect;
    private static TextView mTextviewLog;
    private Intent mIntentStartStream;

    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    public static class ProgressReceiver extends BroadcastReceiver {
        public ProgressReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.biryanistudio.goprogateway.UPLOAD_READY")) {
                mTextviewLog.setText("Beginning upload...");
            } else if (intent.getAction().equals("com.biryanistudio.goprogateway.UPLOADING")) {
                mTextviewLog.setText("Uploading live stream...");
            } else if (intent.getAction().equals("com.biryanistudio.goprogateway.TEXT_LOG")) {
                Log.i(TAG, intent.getStringExtra("TEXT_LOG"));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        bindWifiToProcess();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            mLocPermission = (locationPermission == PackageManager.PERMISSION_GRANTED);
            mStoragePermission = (storagePermission == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView();
    }

    @Override
    public void onStop() {
        super.onStop();
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        if (mIntentStartStream != null) stopService(mIntentStartStream);
    }

    private void setContentView() {
        setContentView(R.layout.activity_main);
        mButtonConnect = (ImageButton) findViewById(R.id.button_connect);
        mButtonConnect.setOnClickListener(this);
        mButtonUpload = (ImageButton) findViewById(R.id.button_start);
        mButtonUpload.setOnClickListener(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destination = sharedPreferences.getString("DESTINATION", "YouTube");
        mButtonUpload.setImageResource((destination.equals("YouTube")) ? R.drawable.youtube_svg
                : R.drawable.facebook_svg);
        mTextviewLog = (TextView) findViewById(R.id.textview_log);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_connect) {
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        } else if (view.getId() == R.id.button_start) {
            if (mLocPermission && mStoragePermission)
                start();
            else
                obtainPermissions();
        }
    }

    private void obtainPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 953);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 953 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mLocPermission = true;
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                mStoragePermission = true;
            if (mLocPermission && mStoragePermission)
                start();
        }
    }

    /**
     * Makes a NetworkRequest for a WiFi capable network. Callback binds this WiFi network (once it
     * is available) to the current process.
     */
    private void bindWifiToProcess() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        mNetworkCallback = new WifiNetworkCallback(this);
        mConnectivityManager.requestNetwork(wifiNetworkReq, mNetworkCallback);
    }

    @Override
    public void wifiAvailable(Network wifiNetwork) {
        Log.i(TAG, "WiFi available.");
        boolean wifiBound;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wifiBound = mConnectivityManager.bindProcessToNetwork(wifiNetwork);
        } else {
            wifiBound = ConnectivityManager.setProcessDefaultNetwork(wifiNetwork);
        }
        if (wifiBound) Log.i(TAG, "Successfully bound process to WiFi.");
        else Log.i(TAG, "Could not bind to WiFi.");
    }

    @Override
    public void wifiLost() {
        Log.i(TAG, "WiFi lost.");
    }

    private void start() {
        try {
            String device = new CheckDeviceTask().execute().get();
            if (device.equals("GoPro")) mIntentStartStream = new Intent(this,
                    FFmpegStreamFromGoPro.class);
            else if (device.equals("SJCAM")) mIntentStartStream = new Intent(this,
                    FFmpegStreamFromSJCAM.class);
            if (mIntentStartStream != null) {
                mButtonUpload.setEnabled(true);
                startService(mIntentStartStream);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
