package com.biryanistudio.goprogateway.FFmpegStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.FFmpegUpload.FFmpegUploadToFacebook;
import com.biryanistudio.goprogateway.FFmpegUpload.FFmpegUploadToYouTube;
import com.biryanistudio.goprogateway.Interface.IWifiAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.WifiNetworkCallback;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

import java.util.Timer;

/**
 * Created by Sravan on 10-Jul-16.
 */
public abstract class AbstractFFmpegStream extends Service implements IWifiAvailable {
    protected final String TAG = getClass().getSimpleName();
    protected FFmpeg mFFmpeg;
    protected Timer mTimer;
    protected ConnectivityManager mConnectivityManager;

    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private Intent mStartUploadIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
        }*/
        showNotification();
        bindProcessToWiFi();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        if (mFFmpeg.isFFmpegCommandRunning()) Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
        if (mTimer != null) {
            Log.i(TAG, "Clearing, purging timer.");
            mTimer.cancel();
            mTimer.purge();
        }
        if (mStartUploadIntent != null) stopService(mStartUploadIntent);
        Utility.deleteFile();
    }

    protected abstract void showNotification();

    /**
     * Makes a NetworkRequest for a WiFi capable network. Callbacks binds this WiFi network once it
     * is available to the current process.
     */
    private void bindProcessToWiFi() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        mNetworkCallback = new WifiNetworkCallback(this);
        mConnectivityManager.requestNetwork(wifiNetworkReq, mNetworkCallback);
    }

    @Override
    public abstract void wifiAvailable(Network wifiNetwork);

    @Override
    public abstract void wifiLost();

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

    private void executeCmd() {
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
                        Log.i(TAG, "FFmpeg execute onUploadReady");
                        beginUpload();
                        Utility.sendBroadcast(AbstractFFmpegStream.this,
                                getString(R.string.broadcast_setting_up));
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

    private void beginUpload() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destination = sharedPreferences.getString("DESTINATION", "YouTube");
        if (destination.equals("YouTube")) mStartUploadIntent = new Intent(this,
                FFmpegUploadToYouTube.class);
        else mStartUploadIntent = new Intent(this,
                FFmpegUploadToFacebook.class);
        startService(mStartUploadIntent);
    }
}
