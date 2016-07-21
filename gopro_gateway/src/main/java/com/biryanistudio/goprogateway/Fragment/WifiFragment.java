package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biryanistudio.goprogateway.R;

/**
 * Created by sravan953 on 13/06/16.
 */
public class WifiFragment extends Fragment implements View.OnClickListener {
    final String TAG = getClass().getSimpleName();
    private TextView wifiMessage;
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
        wifiMessage = (TextView) view.findViewById(R.id.tv_wifi_msg);
        (view.findViewById(R.id.btn_connect)).setOnClickListener(this);
        (view.findViewById(R.id.btn_proceed)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateViews();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(wifiChangedReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(wifiChangedReceiver);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_connect)
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getFragmentManager().beginTransaction().replace(R.id.container, new PermissionsFragment()).commit();
            else
                getFragmentManager().beginTransaction().replace(R.id.container, new GoProFragment()).commit();
        }
    }

    private void updateViews() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        String SSID = wifiManager.getConnectionInfo().getSSID();
        wifiMessage.setText("Currently connected to: " + SSID);
    }
}
