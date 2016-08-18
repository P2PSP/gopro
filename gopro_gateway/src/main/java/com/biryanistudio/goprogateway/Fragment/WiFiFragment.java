package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.biryanistudio.goprogateway.CheckDeviceTask;
import com.biryanistudio.goprogateway.Interface.IWifiAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.WifiNetworkCallback;
import com.biryanistudio.goprogateway.R;

/**
 * Created by Sravan on 17-Aug-16.
 */
public class WiFiFragment extends Fragment implements IWifiAvailable, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Button mButtonProceed;
    private boolean mWifiBound = false;
    private CheckDeviceTask mCheckDeviceTask;

    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        ImageButton buttonConnect = (ImageButton) view.findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(this);
        Drawable drawable = buttonConnect.getDrawable();
        ((Animatable) drawable).start();
        mButtonProceed = (Button) view.findViewById(R.id.button_proceed);
        mButtonProceed.setOnClickListener(this);
        mButtonProceed.setEnabled(false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        bindWifiToProcess();
    }

    @Override
    public void onStop() {
        super.onStop();
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        mCheckDeviceTask.cancel(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_connect) {
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        } else if (view.getId() == R.id.button_proceed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mConnectivityManager.bindProcessToNetwork(null);
            } else {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
            getFragmentManager().beginTransaction().replace(R.id.container, new DeviceFragment(),
                    null).addToBackStack("WiFiFragment").commit();
        }
    }

    /**
     * Makes a NetworkRequest for a WiFi capable network. Callback binds this WiFi network (once it
     * is available) to the current process.
     */
    private void bindWifiToProcess() {
        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        mNetworkCallback = new WifiNetworkCallback(this);
        mConnectivityManager.requestNetwork(wifiNetworkReq, mNetworkCallback);
    }

    @Override
    public void wifiAvailable(Network wifiNetwork) {
        Log.i(TAG, "WiFi available.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mWifiBound = mConnectivityManager.bindProcessToNetwork(wifiNetwork);
        } else {
            mWifiBound = ConnectivityManager.setProcessDefaultNetwork(wifiNetwork);
        }
        if (mWifiBound) {
            Log.i(TAG, "Successfully bound process to WiFi.");
            mCheckDeviceTask = new CheckDeviceTask(this);
            mCheckDeviceTask.execute();
        } else {
            Log.i(TAG, "Could not bind to WiFi.");
        }
    }

    @Override
    public void wifiLost() {
        Log.i(TAG, "WiFi lost.");
        mButtonProceed.setEnabled(false);
    }

    public void connected(String device) {
        if (device != null) {
            if (device.equals("GoPro")) {
                Log.i(TAG, "Connected to GoPro device.");
                Toast.makeText(getActivity(), "Connected to GoPro device.",
                        Toast.LENGTH_SHORT).show();
            } else if (device.equals("SJCAM")) {
                Log.i(TAG, "Connected to SJCAM device.");
                Toast.makeText(getActivity(), "Connected to SJCAM device.",
                        Toast.LENGTH_SHORT).show();
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.edit().putString("DEVICE", device).commit();
            mButtonProceed.setEnabled(true);
        } else {
            Toast.makeText(getActivity(), "Connect to a GoPro/SJCAM device.",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
