package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.FFmpeg.FFmpegStreamService;
import com.biryanistudio.goprogateway.UDPService;

/**
 * Created by sravan953 on 13/06/16.
 */
public class GoProFragment extends Fragment implements View.OnClickListener {
    final private String TAG = getClass().getSimpleName();
    private TextView mTextLog;
    private Button mButtonStartStream;
    private Button mButtonStopStream;
    private Intent mStreamIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gopro, container, false);
        mTextLog = (TextView) view.findViewById(R.id.tv_log);
        mButtonStartStream = (Button) view.findViewById(R.id.btn_start_stream);
        mButtonStartStream.setOnClickListener(this);
        mButtonStopStream = (Button) view.findViewById(R.id.btn_stop_stream);
        mButtonStopStream.setOnClickListener(this);

        mStreamIntent = new Intent(getActivity(), FFmpegStreamService.class);
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.btn_start_stream:
                getActivity().startService(mStreamIntent);
                break;
            case R.id.btn_stop_stream:
                getActivity().stopService(mStreamIntent);
                break;
            default:
                break;

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().stopService(mStreamIntent);
    }
}
