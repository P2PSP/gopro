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
import com.biryanistudio.goprogateway.FFmpeg.FFmpegUploadToFacebook;
import com.biryanistudio.goprogateway.FFmpeg.FFmpegUploadToYouTube;
import com.biryanistudio.goprogateway.R;
import com.facebook.AccessToken;

/**
 * Created by sravan953 on 13/06/16.
 */
public class GoProFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private static boolean mYouTubeKeyValid = false;
    private String mYouTubeKey;
    private static boolean mFacebookValid = false;
    private static TextView mTextLog;
    private Button mButtonStartStream;
    private static Button mButtonStartUpload;
    private Button mButtonStopStream;
    private Intent mIntentStartStream;
    private Intent mIntentStartUpload;
    private String mDevice;

    public static class ProgressReceiver extends BroadcastReceiver {
        public ProgressReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.biryanistudio.goprogateway.UPLOAD_READY")) {
                if (mYouTubeKeyValid && !mButtonStartUpload.isEnabled()) {
                    mButtonStartUpload.setEnabled(true);
                    mTextLog.append("\nLive stream ready to upload to YouTube...");
                } else if (mFacebookValid && !mButtonStartUpload.isEnabled()) {
                    mButtonStartUpload.setEnabled(true);
                    mTextLog.append("\nLive stream ready to upload to Facebook...");
                }
            } else if (intent.getAction().equals("com.biryanistudio.goprogateway.TEXT_LOG")) {
                mTextLog.append("\n" + intent.getStringExtra("TEXT_LOG"));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice = (PreferenceManager.getDefaultSharedPreferences(getActivity())).getString("DEVICE", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gopro, container, false);
        mTextLog = (TextView) view.findViewById(R.id.textview_log);
        mButtonStartStream = (Button) view.findViewById(R.id.button_start_stream);
        mButtonStartStream.setOnClickListener(this);
        mButtonStartUpload = (Button) view.findViewById(R.id.button_start_upload);
        mButtonStartUpload.setOnClickListener(this);
        mButtonStartUpload.setEnabled(false);
        mButtonStopStream = (Button) view.findViewById(R.id.button_stop_stream);
        mButtonStopStream.setOnClickListener(this);
        mButtonStopStream.setEnabled(false);
        mTextLog.setText("Connected to: "  + mDevice);

        mIntentStartStream = new Intent(getActivity(), FFmpegStream.class);
        mIntentStartStream.putExtra("DEVICE", mDevice);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkYouTubeAPI();
        checkFacebook();
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
            case R.id.button_start_stream:
                getActivity().startService(mIntentStartStream);
                mButtonStartStream.setEnabled(false);
                mButtonStopStream.setEnabled(true);
                break;
            case R.id.button_start_upload:
                getActivity().startService(mIntentStartUpload);
                mButtonStartUpload.setEnabled(false);
                break;
            case R.id.button_stop_stream:
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

    private void checkYouTubeAPI() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mYouTubeKey = sharedPreferences.getString("YOUTUBE_API", null);
        if (mYouTubeKey != null) {
            if (mYouTubeKey.length() == 19) {
                mYouTubeKeyValid = true;
                mIntentStartUpload = new Intent(getActivity(), FFmpegUploadToYouTube.class);
                mIntentStartUpload.putExtra("DEVICE", mDevice);
                mIntentStartUpload.putExtra("KEY", mYouTubeKey);
                return;
            }
        }
        Toast.makeText(getActivity(), "To livestream to YouTube, " +
                "please enter a valid YouTube API key.", Toast.LENGTH_SHORT).show();
    }

    private void checkFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null) {
            mFacebookValid = true;
            mIntentStartUpload = new Intent(getActivity(), FFmpegUploadToFacebook.class);
            mIntentStartUpload.putExtra("DEVICE", mDevice);
            mIntentStartUpload.putExtra("KEY", accessToken.getToken());
            mIntentStartUpload.putExtra("USERID", accessToken.getUserId());
            return;
        }
        Toast.makeText(getActivity(), "To livestream to Facebook, " +
                "please login via the Settings screen", Toast.LENGTH_SHORT).show();
    }
}
