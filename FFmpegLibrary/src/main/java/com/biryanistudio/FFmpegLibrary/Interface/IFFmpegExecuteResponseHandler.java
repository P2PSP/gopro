package com.biryanistudio.FFmpegLibrary.Interface;

public interface IFFmpegExecuteResponseHandler {

    void onStart();

    void onFinish();

    void onSuccess(String message);

    void onProgress(String message);

    void onFailure(String message);

    void onUploadReady();

}
