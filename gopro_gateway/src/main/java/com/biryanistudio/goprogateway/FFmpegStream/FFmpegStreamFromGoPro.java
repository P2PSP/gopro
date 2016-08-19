package com.biryanistudio.goprogateway.FFmpegStream;

import android.app.Notification;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

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
public class FFmpegStreamFromGoPro extends AbstractFFmpegStream {

    @Override
    protected void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Streaming from GoPro... ")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
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
            new StartGoProStreamTask().execute();
        } else {
            Log.i(TAG, "Could not bind to WiFi.");
        }
    }

    @Override
    public void wifiLost() {
        Log.i(TAG, "WiFi lost.");
    }

    @Override
    protected String[] getExecCmd() {
        return new String[]{"-i", "udp://:8554", Utility.getPath()};
    }

    private class StartGoProStreamTask extends AsyncTask<Void, Void, String> {
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
            if (result == null) {
                Log.i(TAG, "null result");
            } else {
                Log.i(TAG, result);
                loadFFMPEG();
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
