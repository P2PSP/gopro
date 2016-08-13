package com.biryanistudio.FFmpegLibrary.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.FileUtils;
import com.biryanistudio.FFmpegLibrary.Interface.LoadBinaryResponseHandler;

import java.io.File;

public class FFmpegLoadBinaryAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = getClass().getSimpleName();
    private final LoadBinaryResponseHandler mLoadBinaryResponseHandler;
    private final Context mContext;

    public FFmpegLoadBinaryAsyncTask(Context context,
                                     LoadBinaryResponseHandler loadBinaryResponseHandler) {
        mContext = context;
        mLoadBinaryResponseHandler = loadBinaryResponseHandler;
    }

    @Override
    protected void onPreExecute() {
        mLoadBinaryResponseHandler.onStart();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!checkBinaryExists()) {
            boolean isFileCopied = FileUtils.copyBinaryFromAssetsToData(mContext);
            // Make file executable
            if (isFileCopied) {
                File ffmpegFile = new File(FileUtils.getFFmpegPath(mContext));
                if (ffmpegFile.canExecute()) {
                    Log.i(TAG, "FFmpeg is executable.");
                    return true;
                } else {
                    Log.i(TAG, "FFmpeg is not executable, trying to make it executable.");
                    if (ffmpegFile.setExecutable(true)) return true;
                }
                return false;
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mLoadBinaryResponseHandler != null) {
            if (isSuccess) {
                mLoadBinaryResponseHandler.onSuccess();
            } else {
                mLoadBinaryResponseHandler.onFailure();
            }
            mLoadBinaryResponseHandler.onFinish();
        }
    }

    private boolean checkBinaryExists() {
        File ffmpegFile = new File(FileUtils.getFFmpegPath(mContext));
        if (ffmpegFile.exists()) {
            Log.i(TAG, "FFmpeg already exists.");
            return true;
        } else {
            Log.i(TAG, "FFmpeg does not exist.");
            return false;
        }
    }
}
