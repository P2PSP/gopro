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
    static final private String TAG = VideoFileHelper.class.getSimpleName();
    static private String FOLDER_NAME = "GoPro Gateway";
    static private String PREF_KEY = "SAVE_FILE_ID";


    /**
     * Returns a String value denoting the path where the file will be saved.
     *
     * @param context Context
     * @return String The save path
     */
    public static String getPath(Context context, String device) {
        checkAndMakeFolder();
        int id = getVideoFileID(context);
        updateVideoFileID(context, id);
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        File file = new File(folder, device + "_" + id + ".avi");
        return file.getAbsolutePath();
    }

    /**
     * Returns an int value that is appended to the file name in {@link #getPath(Context context,
     * String device) getPath} method.
     *
     * @param context Context
     * @return int The number to be concatenated with the file name
     */
    private static int getVideoFileID(Context context) {
        int numFiles = getNumberOfFiles();
        int fileID = PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_KEY, 1);
        if (numFiles != fileID) {
            updateVideoFileID(context, numFiles);
            fileID = PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_KEY, 1);
        }
        return fileID;
    }

    /**
     * Returns the number of files in the device-specific folder.
     *
     * @return int Number of files in device-specific
     */
    private static int getNumberOfFiles() {
        File folder = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        return folder.list().length;
    }

    /**
     * Updates SharedPreferences after incrementing the file ID that is passed as a parameter.
     *
     * @param context Context
     * @param id      The file ID that will be incremented and then saved to SharedPreferences
     */
    private static void updateVideoFileID(Context context, int id) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREF_KEY, ++id)
                .commit();
    }

    /**
     * Checks if 'GoPro Gateway' folder exists under the Pictures directory, and if not, creates it.
     */
    private static void checkAndMakeFolder() {
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        if (!folder.exists()) {
            Log.i(TAG, "Folder not found, creating.");
            folder.mkdir();
        } else {
            Log.i(TAG, "Folder found.");
        }
    }
}
