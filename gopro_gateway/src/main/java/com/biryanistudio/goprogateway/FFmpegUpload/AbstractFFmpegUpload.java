package com.biryanistudio.goprogateway.FFmpegUpload;

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
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.Interface.ICellularAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.CellularNetworkCallback;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

/**
 * Created by Sravan on 10-Jul-16.
 */
public abstract class AbstractFFmpegUpload extends Service implements ICellularAvailable {
    protected final String TAG = getClass().getSimpleName();
    protected FFmpeg mFFmpeg;
    protected ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        getKey();
        showNotification();
        bindProcessToCellular();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        if (mFFmpeg.isFFmpegCommandRunning()) Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
    }

    protected abstract void getKey();

    protected abstract void showNotification();

    /**
     * Makes a NetworkRequest for a cellular internet capable network. Callbacks binds this WiFi
     * network once it is available to the current process.
     */
    private void bindProcessToCellular() {
        mConnectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest cellularNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build();
        mNetworkCallback = new CellularNetworkCallback(this);
        mConnectivityManager.requestNetwork(cellularNetworkReq, mNetworkCallback);
    }

    @Override
    public abstract void cellularAvailable(Network cellularNetwork);

    @Override
    public abstract void cellularLost();

    /**
     * Attempts to load the FFmpeg binary from the app's Assets folder to the package specific data
     * folder.
     */
    protected void loadFFMPEG() {
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
                    executeCmd();
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "FFmpeg loadBinary onFinish.");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    protected void executeCmd() {
        try {
            mFFmpeg.execute(getExecCmd(), new ExecuteResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "FFmpeg execute onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, message);
                    if (message.contains("Press [q]")) {
                        Log.i(TAG, "FFmpeg execute uploading...");
                        Utility.sendBroadcast(AbstractFFmpegUpload.this,
                                getString(R.string.broadcast_beginning_upload));
                    }
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
                    stopSelf();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    protected abstract String[] getExecCmd();
}
