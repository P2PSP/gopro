package com.biryanistudio.FFmpegLibrary.Interface;

public interface IFFmpegLoadBinaryResponseHandler {
    void onStart();

    void onFinish();

    void onFailure();

    void onSuccess();

}
