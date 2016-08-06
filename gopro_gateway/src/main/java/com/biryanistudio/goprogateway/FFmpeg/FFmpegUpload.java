package com.biryanistudio.goprogateway.FFmpeg;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.VideoFileHelper;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUpload extends Service {
    final private String TAG = getClass().getSimpleName();
    private FFmpeg mFFmpeg;
    private String YOUTUBE_KEY;
    private String DEVICE_TYPE;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        DEVICE_TYPE = intent.getStringExtra("DEVICE_TYPE");
        YOUTUBE_KEY = intent.getStringExtra("YOUTUBE_API");
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
        Log.i(TAG, "onDestroy");
        if (mFFmpeg.isFFmpegCommandRunning()) Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
    }

    /**
     * Attempts to load the FFmpeg binary from the app's Assets folder to the package specific data
     * folder.
     */
    public void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(this);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
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
                    bindCellular();
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "FFmpeg loadBinary onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes a NetworkRequest for a cellular internet capable network. Callbacks binds this WiFi
     * network once it is available to the current process.
     */
    private void bindCellular() {
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest cellularNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build();
        connectivityManager.requestNetwork(cellularNetworkReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.i(TAG, "CELLULAR AVAILABLE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (connectivityManager.bindProcessToNetwork(network)) {
                        executeCmd();
                        return;
                    }
                } else {
                    if (ConnectivityManager.setProcessDefaultNetwork(network)) {
                        executeCmd();
                        return;
                    }
                }
                Log.i(TAG, "Could not bind to CELLULAR.");
            }

            @Override
            public void onLost(Network network) {
                Log.i(TAG, "CELLULAR LOST");
            }
        });
    }

    private void executeCmd() {
        try {
            String[] cmd = {"-re", "-i", VideoFileHelper.getPath(this, DEVICE_TYPE),
                    "-ar", "44100", "-f", "flv", "rtmp://a.rtmp.youtube.com/live2/" + YOUTUBE_KEY};
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
