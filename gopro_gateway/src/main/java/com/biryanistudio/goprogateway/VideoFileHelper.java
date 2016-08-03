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
    final static private String TAG = VideoFileHelper.class.getSimpleName();

    public static String getPath(Context context) {
        int id = getVideoFileID(context);
        updateVideoFileID(context, id);
        checkAndMakeFolder();
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GoPro Gateway");
        File file = new File(folder, "GoPro_" + id + ".avi");
        return file.getAbsolutePath();
    }

    private static int getVideoFileID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("SAVE_FILE_ID", 1);
    }

    private static void updateVideoFileID(Context context, int id) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("SAVE_FILE_ID", ++id)
                .commit();
    }

    public static void checkAndMakeFolder() {
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GoPro Gateway");
        if (!folder.exists()) {
            Log.i(TAG, "Folder not found, creating.");
            folder.mkdir();
        } else {
            Log.i(TAG, "Folder found.");
        }
    }
}
