package com.biryanistudio.goprogateway.Interface;

import android.net.Network;

/**
 * Created by Sravan on 14-Aug-16.
 */
public interface IWifiAvailable {
    void wifiAvailable(Network wifiNetwork);

    void wifiLost();
}
