package com.biryanistudio.goprogateway.NetworkCallback;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import com.biryanistudio.goprogateway.FFmpeg.FFmpegUploadToFacebook;
import com.biryanistudio.goprogateway.FFmpeg.FFmpegUploadToYouTube;

/**
 * Created by Sravan on 14-Aug-16.
 */
public class CellularNetworkCallback extends ConnectivityManager.NetworkCallback {
    Context mContext;

    public CellularNetworkCallback(Context context) {
        mContext = context;
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
        if (mContext instanceof FFmpegUploadToFacebook) {
            ((FFmpegUploadToFacebook) mContext).cellularAvailable(network);
        } else {
            ((FFmpegUploadToYouTube) mContext).cellularAvailable(network);
        }
    }

    private void cellularLost() {
        if (mContext instanceof FFmpegUploadToFacebook) {
            ((FFmpegUploadToFacebook) mContext).cellularLost();
        } else {
            ((FFmpegUploadToYouTube) mContext).cellularLost();
        }
    }
}
