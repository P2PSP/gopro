package com.biryanistudio.goprogateway.FFmpeg;

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

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.Interface.ICellularAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.CellularNetworkCallback;

/**
 * Created by Sravan on 10-Jul-16.
 */
abstract class AbstractFFmpegUpload extends Service implements ICellularAvailable {
    public final String TAG = getClass().getSimpleName();
    public FFmpeg mFFmpeg;
    public ConnectivityManager mConnectivityManager;
    public ConnectivityManager.NetworkCallback mNetworkCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        handleIntent(intent);
        showNotification();
        loadFFMPEG();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        if (mFFmpeg.isFFmpegCommandRunning()) Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
    }

    public abstract void handleIntent(Intent intent);

    public abstract void showNotification();

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
    public void cellularLost() {
        Log.i(TAG, "Cellular lost.");
    }

    public abstract void executeCmd();
}
