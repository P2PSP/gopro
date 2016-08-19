package com.biryanistudio.goprogateway.NetworkCallback;

import android.net.ConnectivityManager;
import android.net.Network;

import com.biryanistudio.goprogateway.FFmpegUpload.AbstractFFmpegUpload;

/**
 * Created by Sravan on 14-Aug-16.
 */
public class CellularNetworkCallback extends ConnectivityManager.NetworkCallback {
    AbstractFFmpegUpload mFFmpegUpload;

    public CellularNetworkCallback(AbstractFFmpegUpload context) {
        mFFmpegUpload = context;
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        cellularAvailable(network);
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        cellularLost();
    }

    private void cellularAvailable(Network network) {
        mFFmpegUpload.cellularAvailable(network);
    }

    private void cellularLost() {
        mFFmpegUpload.cellularLost();
    }
}
