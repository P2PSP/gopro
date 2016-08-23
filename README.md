
# GoPro Gateway | P2PSP

- [Here](https://github.com/sravan953/gopro/blob/master/Docs/GSOC_DOC.md) is my GSoC documentation on what has been done/what has to be done.
- [Library](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary) used is derived from [Ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java) , authored by [hiteshsondhi88](https://github.com/hiteshsondhi88).  

## CONTRIBUTING
Fork and PR!

## DEVICES
#### ANDROID:
- Works only on ARMv7/ARMv8a devices
- Tested on Nexus 6
- If your device isn't supported, read [this](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary/BUILD_GUIDE) doc about compiling ffmpeg binaries yourself!

#### ACTION CAMS:
- GoPro Hero 4 Silver (Tested)
- GoPro Hero 4 Black
- SJCAM (not working, [separate branch](https://github.com/sravan953/gopro/tree/SJCAM_support))

## GETTING THE CODE
[Clone, compile](#compiling-on-android-studio) and run the app! [Official docs](https://developer.android.com/studio/intro/migrate.html#import_projects_to_android_studio) about importing a project in Android Studio.

## RUNNING THE APP
1. Enable WiFi and high speed cellular on your Android device.
2. Pair your Android device to your GoPro.
3. Open the app. Configure your [YouTube](https://github.com/sravan953/gopro/blob/master/doc/YouTube-Streaming.md)/[Facebook](https://github.com/sravan953/gopro/blob/master/doc/Facebook-Streaming.md) streaming options in the Settings screen.
4. Upload!

## COMPILING ON ANDROID STUDIO
1. Clone repo.
2. Open Android Studio.
3. `Open an existing Android Studio project` > `gopro`
4. `Build` > `Rebuild project`
5. `Run`

![](https://github.com/sravan953/gopro/blob/master/Docs/android_studio_compile.gif)

## ISSUES
1. App crashes on dual SIM phones.
2. App does not work on VoLTE networks.
