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

public class CheckDeviceTask extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getSimpleName();
    private final String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/status";
    private final String SJCAM_STREAM_URL = "http://192.168.1.254/?custom=1&cmd=3014";
    private final WiFiFragment mWiFiFragment;

    public CheckDeviceTask(WiFiFragment wiFiFragment) {
        mWiFiFragment = wiFiFragment;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (!checkGoProDevice()) {
            if (checkSJCAMDevice()) return "SJCAM";
            return null;
        }
        return "GoPro";
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

    /**
     * Checks if the device is connected to a SJCAM camera by making a GET request for the
     * 'status' of the camera's parameters.
     *
     * @return boolean Whether the device is connected to a SJCAM
     */
    private boolean checkSJCAMDevice() {
        try {
            URL sjcamURL = new URL(SJCAM_STREAM_URL);
            URLConnection urlConnection = sjcamURL.openConnection();
            urlConnection.setConnectTimeout(500);
            InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.contains("<Function>")) return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        mWiFiFragment.connected(result);
    }
}
