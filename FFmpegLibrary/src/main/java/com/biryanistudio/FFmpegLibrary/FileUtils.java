package com.biryanistudio.FFmpegLibrary;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private final static String TAG = FileUtils.class.getSimpleName();
    public final static String mFFmpegFileName = "arm";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

    public static String getFFmpegPath(Context context) {
        return getFilesDirectory(context).getAbsolutePath() + File.separator + mFFmpegFileName;
    }

    static File getFilesDirectory(Context context) {
        // data/data/package name
        return context.getFilesDir();
    }

    public static boolean copyBinaryFromAssetsToData(Context context) {
        // Create files directory under /data/data/package name
        File filesDirectory = getFilesDirectory(context);
        InputStream is;
        try {
            is = context.getAssets().open(mFFmpegFileName);
            // Copy file from assets to files dir
            final FileOutputStream os = new FileOutputStream(new File(filesDirectory,
                    mFFmpegFileName));
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            int n;
            while (EOF != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            }
            closeOutputStream(os);
            closeInputStream(is);
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Issue in coping binary from assets to data. " + e);
        }
        return false;
    }

    static void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}