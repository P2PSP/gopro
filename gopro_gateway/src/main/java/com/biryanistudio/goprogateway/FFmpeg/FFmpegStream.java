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
import com.biryanistudio.goprogateway.R;

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
public class FFmpegStream extends Service {
    final private String TAG = getClass().getSimpleName();
    private FFmpeg mFFmpeg;
    private Timer mTimer;
    final private String[] CMD = {"-i", "udp://:8554", "/storage/emulated/0/output.avi"};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
        }
        loadFFMPEG();
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Stream")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if(mTimer != null) {
            Log.i(TAG, "Clearing timer.");
            mTimer.cancel();
            mTimer.purge();
        }
        if(mFFmpeg.isFFmpegCommandRunning())Log.i(TAG, "Killing process: " +
                mFFmpeg.killRunningProcesses());
    }

    private void loadFFMPEG() {
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

    private void bindPortOnWifi() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        connectivityManager.requestNetwork(wifiNetworkReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.i(TAG, "WIFI AVAILABLE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (connectivityManager.bindProcessToNetwork(network)) {
                        new RequestStreamTask().execute();
                        return;
                    }
                } else {
                    if(ConnectivityManager.setProcessDefaultNetwork(network)) {
                        new RequestStreamTask().execute();
                        return;
                    }
                }
                Log.i(TAG, "Could not bind to WIFI.");
            }

            @Override
            public void onLost(Network network) {
                Log.i(TAG, "WIFI LOST");
            }
        });
    }

    private void executeCmd() {
        try {
            mFFmpeg.execute(CMD, new ExecuteResponseHandler() {
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

                @Override
                public void onUploadReady() {
                    Log.i(TAG, "FFmpeg execute onUploadReady");
                    Intent intent = new Intent();
                    intent.setAction("com.biryanistudio.goprogateway.UPLOAD_READY");
                    sendBroadcast(intent);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
        final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";

        @Override
        protected String doInBackground(Void... voids) {
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
            if (result == null)
                Log.i(TAG, "null result");
            else {
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
            final DatagramPacket packet = new DatagramPacket(message, message.length, address, UDP_PORT);
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
