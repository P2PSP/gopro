package com.biryanistudio.goprogateway.FFmpeg;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegLoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.R;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUpload extends Service {
    final private String TAG = getClass().getSimpleName();
    private FFmpeg mFFmpeg;

    final private String YOUTUBE_KEY = "x5v1-uqey-h9qf-1fa3";
    final private String[] cmd = {"-re", "-i", "/storage/emulated/0/output.avi",
            "-ar", "44100", "-f", "flv", "rtmp://a.rtmp.youtube.com/live2/" + YOUTUBE_KEY};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        loadFFMPEG();
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Upload")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(954, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if(mFFmpeg.isFFmpegCommandRunning())
            mFFmpeg.killRunningProcesses();
    }

    public void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(this);
        try {
            mFFmpeg.loadBinary(new IFFmpegLoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "FFmpeg loadBinary onStart");
                }

                @Override
                public void onFailure() {
                    Log.i(TAG, "FFmpeg loadBinary onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "FFmpeg loadBinary onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "FFmpeg loadBinary onFinish");
                    setCellularAsDefault();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void setCellularAsDefault() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest cellularNetworkReq = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build();
        connectivityManager.requestNetwork(cellularNetworkReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable (Network network) {
                Log.i(TAG, "CELLULAR AVAILABLE");
                connectivityManager.bindProcessToNetwork(network);
                executeCmd();
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "CELLULAR LOST");
            }
        });}

    private void executeCmd() {
        try {
            mFFmpeg.execute(cmd, new IFFmpegExecuteResponseHandler() {
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
