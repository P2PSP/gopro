package com.biryanistudio.goprogateway;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Sravan on 02-Aug-16.
 */
public class Utility {
    static private final String TAG = Utility.class.getSimpleName();
    static private final String FOLDER_NAME = "GoPro Gateway";

    public static String getPath() {
        checkAndMakeFolder();
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        File file = new File(folder, "temp.avi");
        return file.getAbsolutePath();
    }

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

    public static void deleteFile() {
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        File file = new File(folder, "temp.avi");
        if (file.exists()) {
            Log.i(TAG, "Deleting video file.");
            file.delete();
        }
    }
}
