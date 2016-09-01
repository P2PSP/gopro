# You should set this path depending on where your Android NDK is stored
ANDROID_API=android-24
SYSROOT=$ANDROID_NDK/platforms/$ANDROID_API/arch-arm/

# You should adjust this path depending on your platform, e.g. darwin-x86_64 for Mac OS
TOOLCHAIN=$ANDROID_NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64

# Specifics
TARGET_OS="linux"
ARCH="arm"
CPU="armv8-a"
PREFIX="$(pwd)/android/$ARCH"
CROSS_PREFIX="$TOOLCHAIN/bin/arm-linux-androideabi-"
PKG_CONFIG="$(pwd)/ffmpeg-pkg-config"

CFLAGS='-U_FORTIFY_SOURCE -D_FORTIFY_SOURCE=2 -fno-strict-overflow -fstack-protector-all'
LDFLAGS='-Wl,-z,relro -Wl,-z,now -pie'

echo $TOOLCHAIN
echo $PREFIX
echo $CPU
echo $PKG_CONFIG

./configure \
--sysroot="$SYSROOT" \
--prefix="$PREFIX" \
--cross-prefix="$TOOLCHAIN/bin/arm-linux-androideabi-" \
--target-os="$TARGET_OS" \
--arch="$ARCH" \
--enable-runtime-cpudetect \
--enable-pic \
--enable-pthreads \
--enable-yasm \
--disable-shared \
--enable-static \
--disable-ffplay \
--disable-ffprobe \
--disable-ffserver \
--pkg-config="$PKG_CONFIG" \
--extra-cflags="-I${TOOLCHAIN_PREFIX}/include $CFLAGS" \
--extra-ldflags="-L${TOOLCHAIN_PREFIX}/lib $LDFLAGS" \
--extra-cxxflags="$CXX_FLAGS"
