package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.biryanistudio.goprogateway.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sravan953 on 13/06/16.
 */
public class WifiFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private TextView mTextViewMessage;
    private Button mButtonProceed;
    private BroadcastReceiver wifiChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Connectivity change, updating views");
            updateViews();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        mTextViewMessage = (TextView) view.findViewById(R.id.textview_wifi_msg);
        (view.findViewById(R.id.button_connect)).setOnClickListener(this);
        mButtonProceed = (Button) view.findViewById(R.id.button_proceed);
        mButtonProceed.setEnabled(false);
        mButtonProceed.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(wifiChangedReceiver, intentFilter);
        updateViews();
        new CheckStatusTask().execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(wifiChangedReceiver);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_connect)
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getFragmentManager().beginTransaction().replace(R.id.layout_container,
                        new PermissionsFragment()).commit();
            else
                getFragmentManager().beginTransaction().replace(R.id.layout_container,
                        new GoProFragment()).commit();
        }
    }

    private void updateViews() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        String SSID = wifiManager.getConnectionInfo().getSSID();
        mTextViewMessage.setText("Currently connected to: " + SSID);
    }

    private class CheckStatusTask extends AsyncTask<Void, Void, String> {
        final private String GOPRO_STREAM_URL = "http://10.5.5.9/gp/gpControl/status";
        final private String SJCAM_STREAM_URL = "http://192.168.1.254/?custom=1&cmd=3014";

        @Override
        protected String doInBackground(Void... voids) {
            if (!checkGoProDevice()) {
                if (checkSJCAMDevice()) return "SJCAM";
                return null;
            }
            return "GOPRO";
        }

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

        private boolean checkSJCAMDevice() {
            try {
                URL sjcamURL = new URL(SJCAM_STREAM_URL);
                URLConnection urlConnection = sjcamURL.openConnection();
                urlConnection.setConnectTimeout(100);
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
            if (result != null) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString("DEVICE_TYPE", result).commit();
                if (result.equals("GOPRO")) Log.i(TAG, "Connected to GoPro device.");
                else if (result.equals("SJCAM")) Log.i(TAG, "Connected to SJCAM device.");
                mButtonProceed.setEnabled(true);
                return;
            }
            Log.i(TAG, "Not connected to any device.");
            mButtonProceed.setEnabled(false);
        }
    }
}
