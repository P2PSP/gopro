package com.biryanistudio.goprogateway.NetworkCallback;

import android.net.ConnectivityManager;
import android.net.Network;

import com.biryanistudio.goprogateway.FFmpegStream.AbstractFFmpegStream;
import com.biryanistudio.goprogateway.Fragment.WiFiFragment;

/**
 * Created by Sravan on 14-Aug-16.
 */
public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {
    AbstractFFmpegStream mFFmpegStream;
    WiFiFragment mWifiFragment;

    public WifiNetworkCallback(AbstractFFmpegStream context) {
         mFFmpegStream = context;
    }

    public WifiNetworkCallback(WiFiFragment context) {
        mWifiFragment = context;
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        if(mFFmpegStream != null) mFFmpegStream.wifiAvailable(network);
        else if(mWifiFragment != null) mWifiFragment.wifiAvailable(network);
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        if(mFFmpegStream != null) mFFmpegStream.wifiLost();
        else if(mWifiFragment != null) mWifiFragment.wifiLost();
    }
}
