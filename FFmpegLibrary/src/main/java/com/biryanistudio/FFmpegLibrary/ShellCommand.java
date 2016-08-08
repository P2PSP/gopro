package com.biryanistudio.FFmpegLibrary;

import com.biryanistudio.FFmpegLibrary.Interface.ExecuteResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommand {
    final private String TAG = getClass().getSimpleName();
    public Process mProcess;

    public void run(String[] command) {
        try {
            mProcess = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getAndPublishUpdates(ExecuteResponseHandler executeResponseHandler) {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
        while (!isProcessCompleted()) {
            try {
                while ((line = reader.readLine()) != null) {
                    executeResponseHandler.onProgress(line);
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
            return false;
        }
    }

    public void destroyProcess() {
        if (mProcess != null) mProcess.destroy();
    }
}