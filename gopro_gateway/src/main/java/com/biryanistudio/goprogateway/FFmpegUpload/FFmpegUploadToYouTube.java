package com.biryanistudio.goprogateway.FFmpegUpload;

import android.app.Notification;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUploadToYouTube extends AbstractFFmpegUpload {
    private String mKey;

    @Override
    protected void getKey() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mKey = sharedPreferences.getString("YOUTUBE_API", "");
    }

    @Override
    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Uploading to YouTube...")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
    }

    @Override
    public void cellularAvailable(Network cellularNetwork) {
        Log.i(TAG, "Cellular available.");
        boolean bound;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bound = mConnectivityManager.bindProcessToNetwork(cellularNetwork);
        } else {
            bound = ConnectivityManager.setProcessDefaultNetwork(cellularNetwork);
        }
        if (bound) {
            loadFFMPEG();
        } else {
            Log.i(TAG, "Could not bind to cellular.");
        }
    }

    @Override
    public void cellularLost() {
        Log.i(TAG, "Lost cellular.");
    }

    @Override
    protected String[] getExecCmd() {
        String url = "rtmp://a.rtmp.youtube.com/live2/" + mKey;
        String[] cmd = {"-re", "-i", Utility.getPath(),
                "-ar", "44100", "-f", "flv", url};
        return cmd;
    }
}
