# GUIDE FOR COMPILING FFMPEG BINARIES FOR ANDROID

- Tested on Mac OS X El Capitan (10.11.6)
- Android NDK R12b
- Android Studio 2.1.2

## STEP 0 -- PREREQUISITES
- Download and extract [Android NDK](https://developer.android.com/ndk/downloads/index.html)
- Download and extract [ffmpeg binaries](https://ffmpeg.org/download.html)

## STEP 1 -- SETTING ENVIRONMENT VARIABLES
We need to add the Android NDK path as an environment variable. Open the Mac Terminal, and:
```
$ open ~/.bash_profile
```
In the TextEdit window that opens up:
```
export ANDROID_NDK=<path-to-your-extracted-ndk>/android-ndk-r12b
```

## STEP 2 -- BUILDING FFMPEG
- Extract the ffmpeg binaries, rename the extracted folder as `ffmpeg` (if it isn't that already).
- Place them inside the `sources` folder inside `android-ndk-r12b`.
- Create a build script and set the configurable options. [Here]() is the script I used.
- Open a Terminal and cd (change directory) to the ffmpeg source directory ([easy way](http://osxdaily.com/2011/12/07/open-a-selected-finder-folder-in-a-new-terminal-window/)) and execute the script (in this case, my script is called `ffmpeg_build.sh`):
```
./ffmpeg_build.sh
```

#### UNDERSTANDING THE CONFIGURATION OPTIONS:
- Open Terminal and cd to the ffmpeg source directory that you extracted earlier.
- Run:
```
./configure --help
```
## STEP 3 -- COMPILING FFMPEG
Now that we have built from ffmpeg sources, we need to compile it for Android. Here's how:
- Open Terminal
- cd to the ffmpeg source folder
- Run:
```
make clean
make -j4
make install
```

The `-j4` command specifies the number of cores that will be utilized. This depends on your system.

#### ABOUT THE MAKE COMMAND
CMake is a platform-agnostic tool used for managing the build process. The easiest way to get it on your Mac is by getting [Homebrew](http://brew.sh/).

## STEP 4
You're done! Depending on how you'd written the build script for ffmpeg, you'll find the compiled ffmpeg binaries in the corresponding location. In my case: `<path-to-your-extracted-ndk>/android-ndk-r12b/sources/ffmpeg/android/arm/bin`.

Just copy the compiled binary file and place it in the [`assets`](https://github.com/sravan953/gopro/tree/master/FFmpegLibrary/assets) folder in the ffmpeg Android library (which you either added as a dependency or cloned from this repo).
