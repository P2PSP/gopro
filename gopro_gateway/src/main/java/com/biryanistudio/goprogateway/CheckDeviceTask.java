package com.biryanistudio.goprogateway;

import android.os.AsyncTask;

import com.biryanistudio.goprogateway.Fragment.WiFiFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sravan on 17-Aug-16.
 */

public class CheckDeviceTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = getClass().getSimpleName();
    private final String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/status";
    private final WiFiFragment mWiFiFragment;

    public CheckDeviceTask(WiFiFragment wiFiFragment) {
        mWiFiFragment = wiFiFragment;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (checkGoProDevice()) return true;
        else return false;
    }

    /**
     * Checks if the device is connected to a GoPro camera by making a GET request for the
     * 'status' of the camera's parameters.
     *
     * @return boolean Whether the device is connected to a GoPro
     */
    private boolean checkGoProDevice() {
        try {
            URL goproURL = new URL(GOPRO_STREAM_URL);
            URLConnection urlConnection = goproURL.openConnection();
            urlConnection.setConnectTimeout(500);
            InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(is);
            return br.readLine().contains("status");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mWiFiFragment.connected(result);
    }
}
