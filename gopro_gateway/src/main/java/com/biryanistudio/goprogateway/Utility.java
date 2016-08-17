package com.biryanistudio.goprogateway;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;

import java.io.File;

/**
 * Created by Sravan on 02-Aug-16.
 */
public class Utility {
    static private final String TAG = Utility.class.getSimpleName();
    static private final String FOLDER_NAME = "GoPro Gateway";

    public static void checkYouTube(Context context, String api) {
        if (api.length() == 19) {
            SharedPreferences.Editor sharedPrefsEditor = PreferenceManager
                    .getDefaultSharedPreferences(context).edit();
            sharedPrefsEditor.putString("YOUTUBE_API", api).commit();
        } else {
            Toast.makeText(context, "To livestream to YouTube, " +
                    "please enter a valid YouTube API key.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void checkFacebook(Context context) {
        if (AccessToken.getCurrentAccessToken() == null) {
            Toast.makeText(context, "To livestream to Facebook, " +
                    "please login via the Settings screen", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath() {
        checkAndMakeFolder();
        // checkAndDeleteFile();
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME);
        File file = new File(folder, "temp.avi");
        file.deleteOnExit();
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
