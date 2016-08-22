package com.biryanistudio.goprogateway.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.biryanistudio.goprogateway.FFmpegStream.FFmpegStreamFromGoPro;
import com.biryanistudio.goprogateway.R;
import com.biryanistudio.goprogateway.Utility;

/**
 * Created by Sravan on 17-Aug-16.
 */
public class DeviceFragment extends Fragment implements View.OnClickListener {
    private Intent mFFmpegIntent;
    private static ImageButton mButtonStart;
    private static TextView mTextLog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        mButtonStart = (ImageButton) view.findViewById(R.id.button_start);
        mButtonStart.setOnClickListener(this);
        mTextLog = (TextView) view.findViewById(R.id.textview_log);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setButtonImage();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFFmpegIntent != null) getActivity().stopService(mFFmpegIntent);
    }

    @Override
    public void onClick(View view) {
        if (checkCellular()) {
            mFFmpegIntent = new Intent(getActivity(), FFmpegStreamFromGoPro.class);
            getActivity().startService(mFFmpegIntent);
            Drawable drawable = mButtonStart.getDrawable();
            ((Animatable) drawable).start();
        } else {
            Utility.showSnackbar(getActivity().findViewById(android.R.id.content),
                    "Enable your cellular connection to proceed.");
        }
    }

    private boolean checkCellular() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        int type = (connectivityManager.getActiveNetworkInfo()).getType();
        return type == ConnectivityManager.TYPE_MOBILE;
    }

    private void setButtonImage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String destination = sharedPreferences.getString("DESTINATION", "");
        switch (destination) {
            case "":
                mButtonStart.setImageResource(R.drawable.upload_svg);
                Utility.showSnackbar(getActivity().findViewById(android.R.id.content),
                        "Set up your streaming options in the Settings screen.");
                break;
            case "YouTube":
                mButtonStart.setImageResource(R.drawable.animyoutube_svg);
                mButtonStart.setEnabled(true);
                break;
            case "Facebook":
                mButtonStart.setImageResource(R.drawable.animfacebook_svg);
                mButtonStart.setEnabled(true);
                break;
        }
    }

    public static class ProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(context.getString(R.string.broadcast_key));
            mTextLog.setText(msg);
            if(msg.equals(context.getString(R.string.broadcast_beginning_upload))) {
                Drawable drawable = mButtonStart.getDrawable();
                ((Animatable) drawable).start();
            }
        }
    }
}
