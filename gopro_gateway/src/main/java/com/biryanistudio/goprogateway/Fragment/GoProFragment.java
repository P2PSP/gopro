package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.biryanistudio.goprogateway.FFmpeg.FFmpegStream;
import com.biryanistudio.goprogateway.FFmpeg.FFmpegUpload;
import com.biryanistudio.goprogateway.R;

/**
 * Created by sravan953 on 13/06/16.
 */
public class GoProFragment extends Fragment implements View.OnClickListener {
    final private String TAG = getClass().getSimpleName();
    private static boolean mAPIValid;
    private String mAPIKey;
    private TextView mTextLog;
    private Button mButtonStartStream;
    private static Button mButtonStartUpload;
    private Button mButtonStopStream;
    private Intent mIntentStartStream;
    private Intent mIntentStartUpload;

    public static class ProgressReceiver extends BroadcastReceiver {
        public ProgressReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "com.biryanistudio.goprogateway.UPLOAD_READY") {
                if (mAPIValid && !mButtonStartUpload.isEnabled())
                    mButtonStartUpload.setEnabled(true);
            } else if(intent.getAction() == "com.biryanistudio.goprogateway.TEXT_LOG") {

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gopro, container, false);
        mTextLog = (TextView) view.findViewById(R.id.tv_log);
        mButtonStartStream = (Button) view.findViewById(R.id.btn_start_stream);
        mButtonStartStream.setOnClickListener(this);
        mButtonStartUpload = (Button) view.findViewById(R.id.btn_start_upload);
        mButtonStartUpload.setOnClickListener(this);
        mButtonStartUpload.setEnabled(false);
        mButtonStopStream = (Button) view.findViewById(R.id.btn_stop_stream);
        mButtonStopStream.setOnClickListener(this);
        mButtonStopStream.setEnabled(false);

        mIntentStartStream = new Intent(getActivity(), FFmpegStream.class);
        mIntentStartUpload = new Intent(getActivity(), FFmpegUpload.class);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkAPIKey();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().stopService(mIntentStartStream);
        getActivity().stopService(mIntentStartUpload);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_start_stream:
                getActivity().startService(mIntentStartStream);
                mButtonStartStream.setEnabled(false);
                mButtonStopStream.setEnabled(true);
                break;
            case R.id.btn_start_upload:
                getActivity().startService(mIntentStartUpload);
                mButtonStartUpload.setEnabled(false);
                break;
            case R.id.btn_stop_stream:
                getActivity().stopService(mIntentStartStream);
                getActivity().stopService(mIntentStartUpload);
                mButtonStartStream.setEnabled(true);
                mButtonStartUpload.setEnabled(false);
                mButtonStopStream.setEnabled(false);
                break;
            default:
                break;
        }
    }

    private void checkAPIKey() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mAPIKey = sharedPreferences.getString("YOUTUBE_API", null);
        if (mAPIKey != null) {
            if (mAPIKey.length() == 19) {
                mAPIValid = true;
                mIntentStartUpload.putExtra("YOUTUBE_API", mAPIKey);
                return;
            }
        }
        mAPIValid = false;
        Toast.makeText(getActivity(), "To livestream to YouTube, " +
                "please enter a valid YouTube API key.", Toast.LENGTH_LONG).show();
    }
}
