package com.biryanistudio.FFmpegLibrary.AsyncTask;

import android.os.AsyncTask;

import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.ShellCommand;

public class FFmpegExecuteAsyncTask extends AsyncTask<String[], Void, Boolean> {
    final private String TAG = getClass().getSimpleName();
    final private ExecuteResponseHandler mExecuteResponseHandler;
    private ShellCommand mShellCommand;

    public FFmpegExecuteAsyncTask(ExecuteResponseHandler executeResponseHandler) {
        mExecuteResponseHandler = executeResponseHandler;
    }

    @Override
    protected void onPreExecute() {
        mExecuteResponseHandler.onStart();
    }

    @Override
    protected Boolean doInBackground(String[]... params) {
        mShellCommand = new ShellCommand();
        mShellCommand.run(params[0]);
        return mShellCommand.getAndPublishUpdates(mExecuteResponseHandler);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result) {
            mExecuteResponseHandler.onSuccess("Normal termination.");
            mShellCommand.destroyProcess();
        } else {
            mExecuteResponseHandler.onFailure("Interrupted execution.");
        }
        mExecuteResponseHandler.onFinish();
    }

    public boolean isProcessCompleted() {
        return mShellCommand.isProcessCompleted();
    }
}
