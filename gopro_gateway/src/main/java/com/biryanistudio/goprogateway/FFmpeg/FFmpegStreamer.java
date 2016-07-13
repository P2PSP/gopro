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
import android.security.NetworkSecurityPolicy;
import android.util.Log;

import com.biryanistudio.goprogateway.UDPService;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegStreamer {
    final private String TAG = getClass().getSimpleName();
    private final Context mContext;
    private FFmpeg mFFmpeg;
    private Intent mUDPIntent;
    private Network mWifiNetwork;

    // final private String[] cmd = {"-i", "udp://:8554", "-codec:v:0", "copy", "-codec:a:1", "copy",
            // "-f", "mpegts", "/storage/emulated/0/output.ts"};

    final private String[] cmd = {"-i", "udp://:8554", "-analyzeduration", "1000000",
            "-map", "v:0:0", "-map", "a:0:1", "-codec:v:0", "copy", "-codec:a:1", "copy", "-f",
            "mpegts", "/storage/emulated/0/output.ts"};

    public FFmpegStreamer(Context context) {
        mContext = context;
        mUDPIntent = new Intent(mContext, UDPService.class);
        setWifiAsDefault();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setWifiAsDefault() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable (Network network) {
                Log.i(TAG, "WIFI");
                mWifiNetwork = network;
                connectivityManager.bindProcessToNetwork(mWifiNetwork);
                start();
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "WIFI LOST");
            }
        });
    }

    public void start() {
        loadFFMPEG();
    }

    public void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(mContext);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "ffmpeg loadBinary onStart");
                }

                @Override
                public void onFailure() {
                    Log.i(TAG, "ffmpeg loadBinary onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "ffmpeg loadBinary onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "ffmpeg loadBinary onFinish");
                    new RequestStreamTask().execute();
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
                    Log.i(TAG, "ffmpeg execute onStart");
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
                    Log.i(TAG, "ffmpeg execute onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        mFFmpeg.killRunningProcesses();
        mContext.stopService(mUDPIntent);
        Log.i(TAG, "ffmpeg killRunningProcesses");
    }

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
        final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";

        @Override
        protected String doInBackground(Void... voids) {
            Log.i(TAG, "Requesting device");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(GOPRO_STREAM_URL).build();
                Response response = client.newCall(request).execute();
                return response.body().string();
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
