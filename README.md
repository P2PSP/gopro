
# GoPro Gateway | P2PSP

- [Here](https://github.com/sravan953/gopro/blob/master/GSOC_DOC.md) is my GSoC documentation on what has been done/what has to be done.
- [Library](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary) used is derived from [Ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java) , authored by [hiteshsondhi88](https://github.com/hiteshsondhi88).  


## DEVICES
- GoPro Hero 4 Silver
- GoPro Hero 4 Black
- SJCAM SJ5000x

## APP: HOW TO
- [Clone, compile](#compiling-on-android-studio) and run the app.
- Obtain your YouTube secret key:
![](https://github.com/sravan953/gopro/blob/master/youtube_secret.gif)

- Paste this secret key in the app, in the settings screen.
- Connect to a Hero 4 Silver/SJ5000x device.
- Grant requested permissions.
- Enable cellular connection.
- Begin obtaining the live stream. If you've entered a valid YouTube key, the upload button will enable once the app is ready, assuming you have a working cellular connection.

## COMPILING ON ANDROID STUDIO
- Clone repo.
- Open Android Studio.
- `Open an existing Android Studio project` > `gopro`
- `Build` > `Rebuild project`
- `Run`

![](https://github.com/sravan953/gopro/blob/master/android_studio_compile.gif)

## ANDROID N DEV PREVIEW 4 ISSUE
> Cleartext HTTP traffic to 10.5.59 not permitted

#### WORKAROUND:
From [StackOverflow](http://stackoverflow.com/questions/37866619/cleartext-http-traffic-to-myserver-com-not-permitted-on-android-n-preview), call:
`NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();`
