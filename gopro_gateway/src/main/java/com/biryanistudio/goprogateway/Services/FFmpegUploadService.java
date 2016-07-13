package com.biryanistudio.goprogateway.Services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.biryanistudio.goprogateway.FFmpeg.FFmpegUploader;
import com.biryanistudio.goprogateway.R;

/**
 * Created by Sravan on 12-Jul-16.
 */
public class FFmpegUploadService extends Service {
    private FFmpegUploader mFFmpegUploader;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Notification notif = new Notification.Builder(this).setContentTitle("Upload")
                .setSmallIcon(R.mipmap.ic_launcher).build();
        startForeground(2, notif);

        mFFmpegUploader = new FFmpegUploader(this);
        // mFFmpegUploader.start();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mFFmpegUploader.kill();
        stopForeground(true);
    }
}
