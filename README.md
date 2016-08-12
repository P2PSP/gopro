
# GoPro Gateway | P2PSP

- [Here](https://github.com/sravan953/gopro/blob/master/GSOC_DOC.md) is my GSoC documentation on what has been done/what has to be done.
- [Library](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary) used is derived from [Ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java) , authored by [hiteshsondhi88](https://github.com/hiteshsondhi88).  

## QUICKSTART GUIDE TO USING hiteshsondhi88's LIBRARY:
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

## ANDROID N DEV PREVIEW 4 ISSUE
> Cleartext HTTP traffic to 10.5.59 not permitted

#### WORKAROUND:
From [StackOverflow](http://stackoverflow.com/questions/37866619/cleartext-http-traffic-to-myserver-com-not-permitted-on-android-n-preview), call:
`NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();`
