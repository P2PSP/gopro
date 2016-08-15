package com.biryanistudio.goprogateway.FFmpeg;

import android.app.Notification;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.VideoFileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sravan on 10-Jul-16.
 */
public class FFmpegUploadToFacebook extends AbstractFFmpegUpload {
    private String mKey;
    private String mUserId;
    private String mDevice;
    private String mFacebookURL;

    @Override
    public void handleIntent(Intent intent) {
        mDevice = intent.getStringExtra("DEVICE");
        mKey = intent.getStringExtra("KEY");
        mUserId = intent.getStringExtra("USERID");
    }

    @Override
    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Streaming from " + mDevice + "\n"
                        + "Uploading to Facebook...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(953, notification);
    }

    @Override
    public void cellularAvailable(Network cellularNetwork) {
        Log.i(TAG, "Cellular available.");
        boolean bound;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bound = mConnectivityManager.bindProcessToNetwork(cellularNetwork);
        } else {
            bound = ConnectivityManager.setProcessDefaultNetwork(cellularNetwork);
        }
        if (bound) {
            new CreateFacebookStream().execute();
        } else {
            Log.i(TAG, "Could not bind to cellular.");
        }
    }

    @Override
    public void executeCmd() {
        try {
            String[] cmd = {"-re", "-i", VideoFileHelper.getPath(this, mDevice),
                    "-ar", "44100", "-f", "flv", mFacebookURL};
            mFFmpeg.execute(cmd, new ExecuteResponseHandler() {
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

    private class CreateFacebookStream extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("graph.facebook.com")
                        .appendPath(mUserId)
                        .appendPath("live_videos")
                        .appendQueryParameter("access_token", mKey)
                        .appendQueryParameter("published", "true");
                URL url = new URL(builder.build().toString());
                Log.i(TAG, url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(is);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                return line;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                mFacebookURL = jsonObject.get("stream_url").toString();
                executeCmd();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
