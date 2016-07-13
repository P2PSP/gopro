package com.biryanistudio.goprogateway.FFmpeg;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUploader {
    final private String TAG = getClass().getSimpleName();
    private final Context mContext;
    private FFmpeg mFFmpeg;
    private Network mCellularNetwork;

    // ffmpeg -i /Users/sravan953/GOPR5959.MP4 -preset veryfast -acodec libmp3lame -ar 44100 -f flv rtmp://a.rtmp.youtube.com/live2/x5v1-uqey-h9qf-1fa3
    final private String YOUTUBE_KEY = "x5v1-uqey-h9qf-1fa3";
    final private String[] cmd = {"-i", "/storage/emulated/0/output.flv", "-acodec", "libmp3lame",
            "-ar", "44100", "-f", "flv", "rtmp://a.rtmp.youtube.com/live2/" + YOUTUBE_KEY};

    public FFmpegUploader(Context context) {
        mContext = context;
        setCellularAsDefault();
        // start();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setCellularAsDefault() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build();
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable (Network network) {
                Log.i(TAG, "CELLULAR");
                mCellularNetwork = network;
                connectivityManager.bindProcessToNetwork(mCellularNetwork);
                start();
            }

            @Override
            public void onLost (Network network) {
                Log.i(TAG, "CELLULAR LOST");
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
                    executeCmd();
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
        Log.i(TAG, "ffmpeg killRunningProcesses");
    }
}
