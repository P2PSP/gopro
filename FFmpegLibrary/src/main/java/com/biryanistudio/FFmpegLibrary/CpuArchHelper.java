package com.biryanistudio.FFmpegLibrary;

import android.os.Build;
import android.util.Log;

class CpuArchHelper {
    final private String TAG = getClass().getSimpleName();

    public CPU_ARCH getCpuArch() {
        String build = Build.SUPPORTED_ABIS[0];
        Log.i(TAG, "Build.CPU_ABI : " + build);
        if (build.equals("")) return CPU_ARCH.ARMv7;
        else return CPU_ARCH.NONE;
    }

    public enum CPU_ARCH {
        ARMv7, NONE;
    }
}
