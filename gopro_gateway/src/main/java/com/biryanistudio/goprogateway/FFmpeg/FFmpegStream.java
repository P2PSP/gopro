package com.biryanistudio.goprogateway.FFmpeg;

import android.annotation.TargetApi;
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

import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.FFmpegExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.LoadBinaryResponseHandler;
import com.biryanistudio.FFmpegLibrary.exceptions.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.exceptions.FFmpegNotSupportedException;
import com.biryanistudio.goprogateway.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegStream extends Service {
    final private String TAG = getClass().getSimpleName();
    private FFmpeg mFFmpeg;
    private String[] cmd = {"-i", "udp://:8554", "/storage/emulated/0/output.avi"};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
        // mUDPIntent = new Intent(this, UDPService.class);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Stream")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
        loadFFMPEG();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void bindPortOnWifi() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        connectivityManager.requestNetwork(wifiNetworkReq, new ConnectivityManager.NetworkCallback() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onAvailable (Network network) {
                Log.i(TAG, "WIFI AVAILABLE");
                connectivityManager.bindProcessToNetwork(network);
                try {
                    DatagramSocket socket = new DatagramSocket();
                    network.bindSocket(socket);
                    socket.connect(InetAddress.getByName("10.5.5.9"), 8554);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new RequestStreamTask().execute();
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "WIFI LOST");
            }
        });
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
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "FFmpeg loadBinary onFinish");
                    bindPortOnWifi();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void executeCmd() {
        try {
            mFFmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
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
        protected void onPostExecute (String result) {
            if(result == null)
                Log.i(TAG, "null");
            else {
                Log.i(TAG, result);
                // startService(mUDPIntent);
                // new KeepAliveTask().execute();
                executeCmd();
            }
        }
    }

    private class KeepAliveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String UDP_IP = "10.5.5.9";
                int UDP_PORT = 8554;
                byte[] message = "_GPHD_:0:0:2:0.000000".getBytes();
                InetAddress address = InetAddress.getByName(UDP_IP);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, UDP_PORT);
                DatagramSocket socket = new DatagramSocket();
                while (true) {
                    socket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
