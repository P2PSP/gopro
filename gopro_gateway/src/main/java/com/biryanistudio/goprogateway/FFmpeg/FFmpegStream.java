package com.biryanistudio.goprogateway.FFmpeg;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.security.NetworkSecurityPolicy;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;
import com.biryanistudio.goprogateway.Interface.IWifiAvailable;
import com.biryanistudio.goprogateway.NetworkCallback.WifiNetworkCallback;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.VideoFileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegStream extends Service implements IWifiAvailable {
    private final String TAG = getClass().getSimpleName();
    private FFmpeg mFFmpeg;
    private Timer mTimer;
    private String mDevice;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        mDevice = intent.getStringExtra("DEVICE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
        }
        showNotification();
        loadFFMPEG();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        if (mTimer != null) {
            Log.i(TAG, "Clearing timer.");
            mTimer.cancel();
            mTimer.purge();
        }
        if (mFFmpeg.isFFmpegCommandRunning()) Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
    }

    private void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Streaming from " + mDevice)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
    }

    /**
     * Attempts to load the FFmpeg binary from the app's Assets folder to the package specific data
     * folder.
     */
    private void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(this);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "FFmpeg loadBinary onStart");
                    sendProgressBroadcast("Attempting to load FFmpeg binary...");
                }

                @Override
                public void onFailure() {
                    Log.i(TAG, "FFmpeg loadBinary onFailure");
                    sendProgressBroadcast("Failure while loading FFmpeg binary.");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "FFmpeg loadBinary onSuccess");
                    sendProgressBroadcast("Successfully loaded FFmpeg binary...");
                    bindPortOnWifi();
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
     * Makes a NetworkRequest for a WiFi capable network. Callbacks binds this WiFi network once it
     * is available to the current process.
     */
    private void bindPortOnWifi() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        mNetworkCallback = new WifiNetworkCallback(this);
        mConnectivityManager.requestNetwork(wifiNetworkReq, mNetworkCallback);
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
            if (mDevice.equals("GoPro")) new RequestStreamTask().execute();
            else executeCmd();
        } else {
            Log.i(TAG, "Could not bind to WiFi.");
            sendProgressBroadcast("Oops, something went wrong!");
        }
    }

    @Override
    public void wifiLost() {
        Log.i(TAG, "WiFi lost.");
    }

    private void executeCmd() {
        try {
            mFFmpeg.execute(getExecCmd(), new ExecuteResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "FFmpeg execute onStart");
                    sendProgressBroadcast("FFmpeg command executing...");
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, message);
                    if (message.contains("Press [q]")) {
                        Log.i(TAG, "FFmpeg execute onUploadReady");
                        sendUploadReadyBroadcast();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, message);
                    sendProgressBroadcast("FFmpeg command execution terminated.");
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, message);
                    sendProgressBroadcast("FFmpeg command successfully executed.");
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

    public String[] getExecCmd() {
        String cmd[];
        if (mDevice.equals("GoPro")) cmd = new String[]{"-i", "udp://:8554",
                VideoFileHelper.getPath(this, mDevice)};
        else cmd = new String[]{"-i", "rtsp://192.168.1.254/sjcam.mov",
                VideoFileHelper.getPath(this, mDevice)};
        return cmd;
    }

    private void sendUploadReadyBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.biryanistudio.goprogateway.UPLOAD_READY");
        sendBroadcast(intent);
    }

    private void sendProgressBroadcast(String message) {
        Intent intent = new Intent();
        intent.setAction("com.biryanistudio.goprogateway.TEXT_LOG");
        intent.putExtra("TEXT_LOG", message);
        sendBroadcast(intent);
    }

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
        final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";

        @Override
        protected String doInBackground(Void... voids) {
            sendProgressBroadcast("Requesting GoPro device to start live stream...");
            try {
                URL url = new URL(GOPRO_STREAM_URL);
                URLConnection urlConnection = url.openConnection();
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(is);
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.i(TAG, "null result");
                sendProgressBroadcast("Oops, something went wrong!");
            } else {
                Log.i(TAG, result);
                executeCmd();
                keepAlive();
            }
        }

    }

    private void keepAlive() {
        try {
            String UDP_IP = "10.5.5.9";
            int UDP_PORT = 8554;
            byte[] message = "_GPHD_:0:0:2:0.000000".getBytes();
            InetAddress address = InetAddress.getByName(UDP_IP);
            final DatagramPacket packet = new DatagramPacket(message, message.length, address,
                    UDP_PORT);
            final DatagramSocket socket = new DatagramSocket();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "Best effort UDP");
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(timerTask, 500, 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
