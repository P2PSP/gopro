package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.biryanistudio.goprogateway.R;
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
 * Created by sravan953 on 13/06/16.
 */
public class GoProFragment extends Fragment implements View.OnClickListener {
    final private String TAG = getClass().getSimpleName();
    final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";
    private TextView mTextLog;
    private Button mButtonStart;
    private Button mButtonStop;
    private FFmpeg mFfmpeg;
    private Intent mUDPIntent;

    final private String[] cmd = {"-i", "udp://:8554", "-codec:v:0", "copy", "-codec:a:1", "copy", "-preset", "veryfast", "/storage/emulated/0/output.mp4"};

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUDPIntent = new Intent(getActivity(), UDPService.class);
        loadFFMPEG();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gopro, container, false);
        mTextLog = (TextView) view.findViewById(R.id.tv_log);
        mButtonStart = (Button) view.findViewById(R.id.btn_start);
        mButtonStart.setOnClickListener(this);
        mButtonStop = (Button) view.findViewById(R.id.btn_stop);
        mButtonStop.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_start) {
            start();
            mButtonStart.setEnabled(false);
            mButtonStop.setEnabled(true);
        }
        else {
            killFfmpeg();
            mButtonStart.setEnabled(true);
            mButtonStop.setEnabled(false);
        }
    }

    private void start() {
        new RequestStreamTask().execute();
    }

    private void startFfmpeg() {
        try {
            mFfmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
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

    private void killFfmpeg() {
        mFfmpeg.killRunningProcesses();
        getActivity().stopService(mUDPIntent);
        Log.i(TAG, "ffmpeg killRunningProcesses");
        mTextLog.append("\nffmpeg killRunningProcesses");
    }

    private void loadFFMPEG() {
        mFfmpeg = FFmpeg.getInstance(getActivity());
        try {
            mFfmpeg.loadBinary(new LoadBinaryResponseHandler() {
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

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
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
            if(result.equalsIgnoreCase("null")) {
                mTextLog.append("\n Please try again");
            } else {
                getActivity().startService(mUDPIntent);
                startFfmpeg();
            }
        }
    }
}
