package com.biryanistudio.goprogateway.FFmpeg;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.goprogateway.UDPService;
import com.biryanistudio.FFmpegLibrary.FFmpeg;
import com.biryanistudio.FFmpegLibrary.FFmpegExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.LoadBinaryResponseHandler;
import com.biryanistudio.FFmpegLibrary.exceptions.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.exceptions.FFmpegNotSupportedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegStreamer {
    final private String TAG = getClass().getSimpleName();
    final private Context mContext;
    private FFmpeg mFFmpeg;
    private Network mWifiNetwork;
    private boolean mWifiBound = false;
    private boolean mCellularBound = false;
    final private Intent mUDPIntent;

    final private String YOUTUBE_KEY = "x5v1-uqey-h9qf-1fa3";
    private String[] cmd = {"-i", "udp://10.5.5.9:8554", "-codec:v:0", "copy", "-codec:a:1", "copy",
            "-ar", "44100", "-f", "flv", "rtmp://a.rtmp.youtube.com/live2/" + YOUTUBE_KEY};

    public FFmpegStreamer(Context context) {
        mContext = context;
        mUDPIntent = new Intent(mContext, UDPService.class);
        loadFFMPEG();
    }

    public void start() {
        if(mWifiBound && mCellularBound) {
            new RequestStreamTask().execute();
        } else {
            bindPortOnWifi();
            setCellularAsDefault();
        }
    }

    public void stop() {
        Log.i(TAG, "FFmpeg killRunningProcess");
        mFFmpeg.killRunningProcesses();
        mContext.stopService(mUDPIntent);
    }

    private void bindPortOnWifi() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest wifiNetworkReq = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        connectivityManager.requestNetwork(wifiNetworkReq, new ConnectivityManager.NetworkCallback() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onAvailable (Network network) {
                try {
                    Log.i(TAG, "WIFI AVAILABLE");
                    mWifiBound = true;
                    mWifiNetwork = network;
                    DatagramSocket datagramSocket = new DatagramSocket();
                    network.bindSocket(datagramSocket);
                    datagramSocket.connect(InetAddress.getByName("10.5.5.9"), 8554);
                    Log.i(TAG, "DatagramSocket PORT: "+ datagramSocket.getLocalPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "WIFI LOST");
            }
        });
    }

    private void setCellularAsDefault() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest cellularNetworkReq = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build();
        connectivityManager.requestNetwork(cellularNetworkReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable (Network network) {
                Log.i(TAG, "CELLULAR AVAILABLE");
                mCellularBound = true;
                connectivityManager.bindProcessToNetwork(network);
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "CELLULAR LOST");
            }
        });}

    public void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(mContext);
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
                URLConnection urlConnection = mWifiNetwork.openConnection(new URL(GOPRO_STREAM_URL));
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
                mContext.startService(mUDPIntent);
                executeCmd();
            }
        }
    }
}
