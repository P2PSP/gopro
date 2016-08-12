
# GoPro Gateway | P2PSP

- [Here](https://github.com/sravan953/gopro/blob/master/GSOC_DOC.md) is my GSoC documentation on what has been done/what has to be done.
- [Library](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary) used is derived from [Ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java) , authored by [hiteshsondhi88](https://github.com/hiteshsondhi88).  


## DEVICES
- GoPro Hero 4 Silver
- GoPro Hero 4 Black
- SJCAM SJ5000x

## APP: HOW TO
1. [Clone, compile](#compiling-on-android-studio) and run the app.
2. [Get](#getting-your-youtube-secret-key) your YouTube secret key
3. Paste this secret key in the app, in the settings screen.
4. Connect to a Hero 4 Silver/SJ5000x device.
5. Grant requested permissions.
6. Enable cellular connection.
7. Begin obtaining the live stream. If you've entered a valid YouTube key, the upload button will enable once the app is ready, assuming you have a working cellular connection.

## COMPILING ON ANDROID STUDIO
1. Clone repo.
2. Open Android Studio.
3. `Open an existing Android Studio project` > `gopro`
4. `Build` > `Rebuild project`
5. `Run`

## GETTING YOUR YOUTUBE SECRET KEY:
1. Open YouTube Creator Studio (you need to have signed in).
2. Click on 'Live Streaming'.
3. Click on 'Reveal', and copy this key.

![](https://github.com/sravan953/gopro/blob/master/android_studio_compile.gif)

## ANDROID N DEV PREVIEW 4 ISSUE
> Cleartext HTTP traffic to 10.5.59 not permitted

#### WORKAROUND:
From [StackOverflow](http://stackoverflow.com/questions/37866619/cleartext-http-traffic-to-myserver-com-not-permitted-on-android-n-preview), call:
`NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();`
