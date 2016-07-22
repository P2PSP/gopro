package com.biryanistudio.FFmpegLibrary;

import com.biryanistudio.FFmpegLibrary.Interface.IFFmpegExecuteUpdateHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommand {
    final private String TAG = getClass().getSimpleName();
    private Process mProcess;
    private IFFmpegExecuteUpdateHandler mFFmpegExecuteUpdateHandler;

    public ShellCommand(IFFmpegExecuteUpdateHandler ffmpegExecuteUpdateHandler) {
        mFFmpegExecuteUpdateHandler = ffmpegExecuteUpdateHandler;
    }

    public void run(String[] command) {
        try {
            mProcess = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getAndPublishUpdates() {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
        while (!isProcessCompleted()) {
            try {
                while ((line = reader.readLine()) != null) {
                    mFFmpegExecuteUpdateHandler.publishUpdate(line);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean isProcessCompleted() {
        try {
            if (mProcess == null) return true;
            mProcess.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            // Do nothing
            e.printStackTrace();
        }
        return false;
    }

    public void destroyProcess() {
        if (mProcess != null) mProcess.destroy();
    }
}