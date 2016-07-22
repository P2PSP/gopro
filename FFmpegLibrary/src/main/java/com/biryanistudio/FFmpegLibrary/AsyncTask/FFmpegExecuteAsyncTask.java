package com.biryanistudio.FFmpegLibrary.AsyncTask;

import android.os.AsyncTask;

import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegExecuteUpdateHandler;
import com.biryanistudio.FFmpegLibrary.ShellCommand;

public class FFmpegExecuteAsyncTask extends AsyncTask<String[], String, Void>
        implements IFFmpegExecuteUpdateHandler {
    final private String TAG = getClass().getSimpleName();
    final private IFFmpegExecuteResponseHandler mFFmpegExecuteResponseHandler;
    private ShellCommand mShellCommand;

    public FFmpegExecuteAsyncTask(IFFmpegExecuteResponseHandler ffmpegExecuteResponseHandler) {
        mFFmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
    }

    @Override
    protected void onPreExecute() {
        mFFmpegExecuteResponseHandler.onStart();
    }

    @Override
    protected Void doInBackground(String[]... params) {
        mShellCommand = new ShellCommand(this);
        mShellCommand.run(params[0]);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values != null && values[0] != null && mFFmpegExecuteResponseHandler != null) {
            mFFmpegExecuteResponseHandler.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if(mShellCommand.getAndPublishUpdates()) {
            mFFmpegExecuteResponseHandler.onSuccess("Normal termination.");
            mShellCommand.destroyProcess();
        } else {
            mFFmpegExecuteResponseHandler.onFailure("Error occurred.");
        }
        mFFmpegExecuteResponseHandler.onFinish();
    }

    @Override
    public void publishUpdate(String update) {
        publishProgress(update);
    }

    public boolean isProcessCompleted() {
        return mShellCommand.isProcessCompleted();
    }
}
