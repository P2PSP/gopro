package com.biryanistudio.goprogateway.Interface;

import android.net.Network;

/**
 * Created by Sravan on 14-Aug-16.
 */
public interface ICellularAvailable {
    void cellularAvailable(Network cellularNetwork);

    void cellularLost();
}
