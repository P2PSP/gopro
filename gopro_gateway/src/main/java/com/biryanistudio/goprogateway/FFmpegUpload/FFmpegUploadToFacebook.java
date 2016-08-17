package com.biryanistudio.goprogateway.FFmpegUpload;

import android.app.Notification;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

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
    private String mAccessToken;
    private String mUserId;
    private String mFacebookURL;

    @Override
    protected void getKey() {
        FacebookSdk.sdkInitialize(this, new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                mAccessToken = accessToken.getToken();
                mUserId = accessToken.getUserId();

            }
        });
    }

    @Override
    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Uploading to Facebook...")
                .setSmallIcon(R.drawable.ic_notification)
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
            if(FacebookSdk.isInitialized()) new CreateFacebookStream().execute();
        } else {
            Log.i(TAG, "Could not bind to cellular.");
        }
    }

    @Override
    public void cellularLost() {
        Log.i(TAG, "Lost cellular.");
    }

    @Override
    protected String[] getExecCmd() {
        String[] cmd = {"-re", "-i", Utility.getPath(),
                "-ar", "44100", "-f", "flv", mFacebookURL};
        return cmd;
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
                        .appendQueryParameter("access_token", mAccessToken)
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
                loadFFMPEG();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
