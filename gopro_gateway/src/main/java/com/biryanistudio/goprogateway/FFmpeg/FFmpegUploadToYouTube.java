package com.biryanistudio.goprogateway.FFmpeg;

import android.app.Notification;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.VideoFileHelper;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUploadToYouTube extends AbstractFFmpegUpload {
    private String mKey;
    private String mDevice;

    @Override
    public void handleIntent(Intent intent) {
        mDevice = intent.getStringExtra("DEVICE");
        mKey = intent.getStringExtra("KEY");
    }

    @Override
    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Streaming from " + mDevice + "\n"
                        + "Uploading to YouTube...")
                .setSmallIcon(R.mipmap.ic_launcher)
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
            executeCmd();
        } else {
            Log.i(TAG, "Could not bind to cellular.");
        }
    }

    @Override
    public void executeCmd() {
        try {
            String url = "rtmp://a.rtmp.youtube.com/live2/" + mKey;
            String[] cmd = {"-re", "-i", VideoFileHelper.getPath(this, mDevice),
                    "-ar", "44100", "-f", "flv", url};
            mFFmpeg.execute(cmd, new ExecuteResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "FFmpeg execute onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, message);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "FFmpeg execute onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
