package com.biryanistudio.FFmpegLibrary;

import android.content.Context;
import android.util.Log;

import com.biryanistudio.FFmpegLibrary.AsyncTask.FFmpegExecuteAsyncTask;
import com.biryanistudio.FFmpegLibrary.AsyncTask.FFmpegLoadBinaryAsyncTask;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegCommandAlreadyRunningException;
import com.biryanistudio.FFmpegLibrary.Exception.FFmpegNotSupportedException;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegExecuteResponseHandler;
import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegLoadBinaryResponseHandler;

public class FFmpeg {
    final private String TAG = getClass().getSimpleName();
    final private Context mContext;
    private FFmpegExecuteAsyncTask mFFmpegExecuteAsyncTask;
    private FFmpegLoadBinaryAsyncTask mFFmpegLoadBinaryAsyncTask;
    private static FFmpeg mInstance = null;

    private FFmpeg(Context context) {
        mContext = context;
    }

    public static FFmpeg getInstance(Context context) {
        if (mInstance == null) mInstance = new FFmpeg(context);
        return mInstance;
    }

    public void loadBinary(IFFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler)
            throws FFmpegNotSupportedException {
        switch (new CpuArchHelper().getCpuArch()) {
            case ARMv7:
                Log.i(TAG, "Loading FFmpeg for ARMv7 CPU");
                mFFmpegLoadBinaryAsyncTask = new FFmpegLoadBinaryAsyncTask(mContext,
                        ffmpegLoadBinaryResponseHandler);
                mFFmpegLoadBinaryAsyncTask.execute();
                break;
            case NONE:
                throw new FFmpegNotSupportedException("Device not supported");
        }
    }

    public void execute(String[] command, IFFmpegExecuteResponseHandler ffmpegExecuteResponseHandler)
            throws FFmpegCommandAlreadyRunningException {
        if (mFFmpegExecuteAsyncTask != null) {
            throw new FFmpegCommandAlreadyRunningException("FFmpeg command is already running, " +
                    "you are only allowed to run single command at a time");
        }
        if (command.length != 0) {
            String[] ffmpegBinaryPath = new String[]{FileUtils.getFFmpegPath(mContext)};
            command = concatenate(ffmpegBinaryPath, command);
            mFFmpegExecuteAsyncTask = new FFmpegExecuteAsyncTask(ffmpegExecuteResponseHandler);
            mFFmpegExecuteAsyncTask.execute(command);
        } else {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
    }

    public String[] concatenate(String[] a, String[] b) {
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public boolean isFFmpegCommandRunning() {
        return mFFmpegExecuteAsyncTask != null && !mFFmpegExecuteAsyncTask.isProcessCompleted();
    }

    public boolean killRunningProcesses() {
        boolean killLoadTask = mFFmpegLoadBinaryAsyncTask != null
                && !mFFmpegLoadBinaryAsyncTask.isCancelled()
                && mFFmpegLoadBinaryAsyncTask.cancel(true);
        boolean killExecuteTask = mFFmpegExecuteAsyncTask != null
                && !mFFmpegExecuteAsyncTask.isCancelled()
                && mFFmpegExecuteAsyncTask.cancel(true);
        return killLoadTask && killExecuteTask;
    }
}
