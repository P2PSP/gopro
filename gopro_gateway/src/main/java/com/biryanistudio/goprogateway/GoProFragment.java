package com.biryanistudio.goprogateway;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sravan953 on 13/06/16.
 */
public class GoProFragment extends Fragment implements View.OnClickListener {
    final private String TAG = getClass().getSimpleName();
    private TextView textView;
    private FFmpeg ffmpeg;

    // final private String[] cmd = {"-codec:v:0", "h264", "-codec:a:1", "aac", "-i", "udp://:8554", "/storage/emulated/0/output.mp4"};

    // Added -f mpegts flags, as seen below
    final private String[] cmd = {"-codec:v:0", "h264", "-codec:a:1", "aac", "-f", "mpegts", "-i", "udp://:8554", "/storage/emulated/0/output.mp4"};

    // Works without codec specifiers too
    // final private String[] cmd = {"-i", "udp://:8554", "/storage/emulated/0/output.mp4"};

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadFFMPEG();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gopro, container, false);
        textView = (TextView) view.findViewById(R.id.log);
        (view.findViewById(R.id.start)).setOnClickListener(this);
        (view.findViewById(R.id.udp)).setOnClickListener(this);
        (view.findViewById(R.id.stop)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.start) start();
        else if(view.getId() == R.id.udp) new UDPTask().execute();
        else stop();
    }

    private void start() {
        try {
            new RequestStreamTask().execute();
            getActivity().startService(new Intent(getActivity(), UDPService.class));
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "ffmpeg execute onStart");
                    textView.append("\nffmpeg execute onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, message);
                    textView.append("\n" + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, message);
                    textView.append("\n" + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, message);
                    textView.append("\n" + message);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "ffmpeg execute onFinish");
                    textView.append("\nffmpeg execute onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        ffmpeg.killRunningProcesses();
        Log.i(TAG, "ffmpeg killRunningProcesses");
        textView.append("\nffmpeg killRunningProcesses");
    }

    private void loadFFMPEG() {
        ffmpeg = FFmpeg.getInstance(getActivity());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "ffmpeg loadBinary onStart");
                    textView.append("\nffmpeg loadBinary onStart");
                }

                @Override
                public void onFailure() {
                    Log.i(TAG, "ffmpeg loadBinary onFailure");
                    textView.append("\nffmpeg loadBinary onFailure");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "ffmpeg loadBinary onSuccess");
                    textView.append("\nffmpeg loadBinary onSuccess");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "ffmpeg loadBinary onFinish");
                    textView.append("\nffmpeg loadBinary onFinish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private class RequestStreamTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String url = "http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute (String result) {
            textView.append("\n"+result);
        }
    }

    private class UDPTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String UDP_IP = "10.5.5.9";
                int UDP_PORT = 8554;
                byte[] message = "_GPHD_:0:0:2:0.000000".getBytes();
                InetAddress address = InetAddress.getByName(UDP_IP);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, UDP_PORT);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
