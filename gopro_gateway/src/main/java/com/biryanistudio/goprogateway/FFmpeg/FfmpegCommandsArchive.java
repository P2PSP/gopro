package com.biryanistudio.goprogateway.FFmpeg;

/**
 * Created by Sravan on 08-Jul-16.
 */
public class FfmpegCommandsArchive {
    /*
    Archive of ffmpeg commands:
    1. final private String[] cmd = {"-codec:v:0", "h264", "-codec:a:1", "aac", "-i", "udp://:8554", "/storage/emulated/0/output.mp4"};
    2. Added -f mpegts flags, as seen below:
    final private String[] cmd = {"-codec:v:0", "h264", "-codec:a:1", "aac", "-f", "mpegts", "-i", "udp://:8554", "/storage/emulated/0/output.mp4"};
    3. final private String[] cmd = {"-i", "udp://:8554", "/storage/emulated/0/output.avi"};
    4. final private String[] cmd = {"-i", "udp://:8554", "-preset", "veryfast", "/storage/emulated/0/output.mp4"};
    5. Works without codec specifiers too
    final private String[] cmd = {"-i", "udp://:8554", "/storage/emulated/0/output.mp4"};
     */
}
