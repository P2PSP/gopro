package com.biryanistudio.goprogateway.Fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.biryanistudio.goprogateway.R;

/**
 * Created by Sravan on 03-Jul-16.
 */
public class PermissionsFragment extends Fragment implements View.OnClickListener {
    private Button mButtonPermissionsReq;
    private Button mButtonProceed;
    boolean mLocPermission = false;
    boolean mStoragePermission = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        mButtonPermissionsReq = (Button) view.findViewById(R.id.btn_req_permissions);
        mButtonPermissionsReq.setOnClickListener(this);
        mButtonProceed = (Button) view.findViewById(R.id.btn_proceed);
        mButtonProceed.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermissions();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_req_permissions)
            checkPermissions();
        else
            getFragmentManager().beginTransaction().replace(R.id.container, new GoProFragment()).commit();
    }

    private void checkPermissions() {
        int permissionCheck1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 == PackageManager.PERMISSION_GRANTED && permissionCheck2 == PackageManager.PERMISSION_GRANTED)
            getFragmentManager().beginTransaction().replace(R.id.container, new GoProFragment()).commit();
        else {
            obtainPermissions();
        }
    }

    private void obtainPermissions() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 953);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 953 && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                mLocPermission = true;
            if(grantResults[1] == PackageManager.PERMISSION_GRANTED)
                mStoragePermission = true;
        }
        if(mLocPermission && mStoragePermission)
            mButtonProceed.setEnabled(true);
    }
}