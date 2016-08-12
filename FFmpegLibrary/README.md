# FFMPEG -- OBTAINING LIVE PREVIEW
## OSX/LINUX:
1. Start streaming - `HTTP GET: http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart`
2. To get stream:
`ffmpeg -i udp://10.5.5.9:8554 output.mp4`

## ANDROID:
- Library used is derived from [Ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java), authored by [hiteshsondhi88](https://github.com/hiteshsondhi88)
- [Guide](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary/BUILD_GUIDE.md) on compiling ffmpeg yourself; so that you can always use the most up-to-date ffempg binaries in your app.


### QUICKSTART ON ADDING FFMPEG-ANDROID-JAVA IN YOUR APP:

- Add dependency: `compile 'com.writingminds:FFmpegAndroid:0.3.2'`
- Load FFmpeg binary:
```java
FFmpeg ffmpeg = FFmpeg.getInstance(context);
ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
…
}
```
- Execute command:
```java
String[] cmd = {"-codec:v:0", "h264", "-codec:a:1", "aac", "-i", "udp://:8554", "/storage/emulated/0/output.mp4"};
ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
…
}
```
- To stop:
`ffmpeg.killRunningProcesses();`

# FFPROBE -- OBTAINING STREAM INFO

1. Start streaming - `HTTP GET: http://10.5.5.9/gp/gpControl/execute?p1=gpStream&a1=proto_v2&c1=restart`
2. To get info about broadcasted streams:
`ffprobe -show_streams udp://10.5.5.9:8554`

## OUTPUT:
> Input #0, mpegts, from 'udp://10.5.5.9:8554':
  Duration: N/A, start: 0.000000, bitrate: N/A
  Program 1 
    Stream #0:0[0x1011]: Video: h264 (Main) ([27][0][0][0] / 0x001B), yuvj420p(pc, bt709), 432x240 [SAR 1:1 DAR 9:5], 29.97 fps, 29.97 tbr, 90k tbn, 59.94 tbc
    Stream #0:1[0x1100]: Audio: aac (LC) ([15][0][0][0] / 0x000F), 48000 Hz, stereo, fltp, 130 kb/s
    Stream #0:2[0x200]: Unknown: none ([128][0][0][0] / 0x0080)
