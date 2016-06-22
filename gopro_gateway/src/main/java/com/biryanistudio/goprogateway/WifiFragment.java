package com.biryanistudio.goprogateway;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sravan953 on 13/06/16.
 */
public class WifiFragment extends Fragment implements View.OnClickListener {
    final String TAG = getClass().getSimpleName();
    TextView wifiMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        wifiMessage = (TextView) view.findViewById(R.id.wifi_msg);
        (view.findViewById(R.id.connect_btn)).setOnClickListener(this);
        (view.findViewById(R.id.next_btn)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateViews();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.connect_btn) {
            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            getActivity().startActivity(intent);
        } else {
            getFragmentManager().beginTransaction().replace(R.id.container, new GoProFragment()).commit();
        }
    }

    private void updateViews() {
        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        String SSID = wm.getConnectionInfo().getSSID();
        wifiMessage.setText("Currently connected to: " + SSID);
    }
}
