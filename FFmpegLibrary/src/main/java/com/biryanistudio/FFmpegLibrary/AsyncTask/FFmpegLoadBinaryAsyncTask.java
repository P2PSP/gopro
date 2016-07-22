package com.biryanistudio.FFmpegLibrary.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.FileUtils;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegLoadBinaryResponseHandler;

import java.io.File;

public class FFmpegLoadBinaryAsyncTask extends AsyncTask<Void, Void, Boolean> {
    final private String TAG = getClass().getSimpleName();
    final private IFFmpegLoadBinaryResponseHandler mFFmpegLoadBinaryResponseHandler;
    final private Context mContext;

    public FFmpegLoadBinaryAsyncTask(Context context,
                                     IFFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler) {
        mContext = context;
        mFFmpegLoadBinaryResponseHandler = ffmpegLoadBinaryResponseHandler;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File ffmpegFile = new File(FileUtils.getFFmpegPath(mContext));
        if (ffmpegFile.exists()) {
            return false;
        } else {
            boolean isFileCopied = FileUtils.copyBinaryFromAssetsToData(mContext,
                    File.separator + FileUtils.mFFmpegFileName);

            // Make file executable
            if (isFileCopied) {
                if (!ffmpegFile.canExecute()) {
                    Log.i(TAG, "FFmpeg is not executable, trying to make it executable.");
                    if (ffmpegFile.setExecutable(true)) {
                        return true;
                    }
                } else {
                    Log.i(TAG, "FFmpeg is executable.");
                    return true;
                }
            }
        }
        return ffmpegFile.exists() && ffmpegFile.canExecute();
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mFFmpegLoadBinaryResponseHandler != null) {
            if (isSuccess) {
                mFFmpegLoadBinaryResponseHandler.onSuccess();
            } else {
                mFFmpegLoadBinaryResponseHandler.onFailure();
            }
            mFFmpegLoadBinaryResponseHandler.onFinish();
        }
    }
}
