package com.biryanistudio.goprogateway.FFmpeg;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

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
public class FFmpegHelper {
    final private String TAG = getClass().getSimpleName();
    private final Context mContext;
    private final TextView mTextLog;
    private FFmpeg mFFmpeg;
    private Intent mUDPIntent;

    final private String[] cmd = {"-i", "udp://:8554", "-codec:v:0", "copy", "-codec:a:1", "copy", "-preset", "veryfast", "/storage/emulated/0/output.mp4"};

    public FFmpegHelper(Context context, TextView textLog) {
        mContext = context;
        mTextLog = textLog;
        mUDPIntent = new Intent(mContext, UDPService.class);
    }

    public void start() {
        new RequestStreamTask().execute();
    }

    public void loadFFMPEG() {
        mFFmpeg = FFmpeg.getInstance(mContext);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "ffmpeg loadBinary onStart");
                    mTextLog.append("\nffmpeg loadBinary onStart");
                }

                @Override
                public void onFailure() {
                    Log.i(TAG, "ffmpeg loadBinary onFailure");
                    mTextLog.append("\nffmpeg loadBinary onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "ffmpeg loadBinary onSuccess");
                    mTextLog.append("\nffmpeg loadBinary onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "ffmpeg loadBinary onFinish");
                    mTextLog.append("\nffmpeg loadBinary onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void execute() {
        try {
            mFFmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "ffmpeg execute onStart");
                    mTextLog.append("\nffmpeg execute onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, message);
                    mTextLog.append("\n" + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, message);
                    mTextLog.append("\n" + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, message);
                    mTextLog.append("\n" + message);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "ffmpeg execute onFinish");
                    mTextLog.append("\nffmpeg execute onFinish");
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
        mTextLog.append("\nffmpeg killRunningProcesses");
    }

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
        final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";

        @Override
        protected String doInBackground(Void... voids) {
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
            mTextLog.append("\n"+result);
            if(result != null) {
                mTextLog.append("\n Please try again");
            } else {
                mContext.startService(mUDPIntent);
                FFmpegHelper.this.execute();
            }
        }
    }
}
