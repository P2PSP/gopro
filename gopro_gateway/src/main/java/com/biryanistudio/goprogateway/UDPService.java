package com.biryanistudio.goprogateway;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Sravan on 29-Jun-16.
 */
public class UDPService extends IntentService {
    final private String TAG = getClass().getSimpleName();

    public UDPService() {
        super("UDPService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        try {
            String UDP_IP = "10.5.5.9";
            int UDP_PORT = 8554;
            byte[] message = "_GPHD_:0:0:2:0.000000".getBytes();
            InetAddress address = InetAddress.getByName(UDP_IP);
            DatagramPacket packet = new DatagramPacket(message, message.length, address, UDP_PORT);
            DatagramSocket socket = new DatagramSocket();
            while(true) {
                socket.send(packet);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
