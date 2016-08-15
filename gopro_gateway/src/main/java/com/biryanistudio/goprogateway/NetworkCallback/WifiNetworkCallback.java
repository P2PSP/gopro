package com.biryanistudio.goprogateway.NetworkCallback;

import android.net.ConnectivityManager;
import android.net.Network;

import com.biryanistudio.goprogateway.FFmpeg.FFmpegStream;

/**
 * Created by Sravan on 14-Aug-16.
 */
public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {
    FFmpegStream mFFmpegStream;

    public WifiNetworkCallback(FFmpegStream context) {
        mFFmpegStream = context;
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        mFFmpegStream.wifiAvailable(network);
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        mFFmpegStream.wifiLost();
    }
}
