package com.biryanistudio.goprogateway;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

/**
 * Created by Sravan on 02-Aug-16.
 */
public class VideoFileHelper {

    public static int getVideoFileID(Context context) {
        int videoID = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("SAVE_FILE_ID", 1);
        return videoID;
    }

    public static void updateVideoFileID(Context context) {
        int id = getVideoFileID(context);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("SAVE_FILE_ID", id++)
                .commit();
    }

    public String[] getExecCmd() {
        int videoID = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("SAVE_FILE_ID", 1);
        File goproFolder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GoPro Gateway");
        if (!goproFolder.exists()) {
            Log.i(TAG, "Folder not found, creating.");
            goproFolder.mkdir();
        }
        String path = goproFolder.getPath() + "GoPro_" + videoID;
        String[] cmd = {"-i", "udp://:8554", path};
        return cmd;
    }

    public boolean makeFolder() {
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GoPro Gateway");
        if(!folder.exists()) return false;
        else return true;
    }
}
