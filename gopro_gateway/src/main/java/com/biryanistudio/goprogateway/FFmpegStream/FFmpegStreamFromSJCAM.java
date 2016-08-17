package com.biryanistudio.goprogateway.FFmpegStream;

import android.app.Notification;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegStreamFromSJCAM extends AbstractFFmpegStream {

    @Override
    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Streaming from SJCAM...")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
    }

    @Override
    public void wifiAvailable(Network wifiNetwork) {
        Log.i(TAG, "WiFi available.");
        boolean bound;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bound = mConnectivityManager.bindProcessToNetwork(wifiNetwork);
        } else {
            bound = ConnectivityManager.setProcessDefaultNetwork(wifiNetwork);
        }
        if (bound) {
            loadFFMPEG();
        } else {
            Log.i(TAG, "Could not bind to WiFi.");
        }
    }

    @Override
    public void wifiLost() {
        Log.i(TAG, "WiFi lost.");
    }

    @Override
    public String[] getExecCmd() {
        return new String[]{"-i", "rtsp://192.168.1.254/sjcam.mov",
                Utility.getPath()};
    }
}
