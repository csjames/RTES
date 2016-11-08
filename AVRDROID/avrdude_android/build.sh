export LOC=$PWD
export CROSS_COMPILE=arm-linux-androideabi
export NDK=$LOC/android-ndk-r13/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/
export NDKTOOLS=$NDK$CROSS_COMPILE
export SYSROOT=${PWD}/android-ndk-r13/platforms/android-21/arch-arm

export CPP="${NDKTOOLS}-cpp --sysroot=${SYSROOT}"
export AR="${NDKTOOLS}-ar"
export AS="${NDKTOOLS}-as --sysroot=${SYSROOT}"
export NM="${NDKTOOLS}-nm"
export CC="${NDKTOOLS}-gcc --sysroot=${SYSROOT}"
export CXX="${NDKTOOLS}-g++ --sysroot=${SYSROOT}"
export LD="${NDKTOOLS}-ld --sysroot=${SYSROOT}"
export RANLIB="${NDKTOOLS}-ranlib"

export PREFIX=${LOC}/android-builds

export PKG_CONFIG_PATH=${PREFIX}/lib/pkgconfig
export CFLAGS="${CFLAGS} --sysroot=${SYSROOT} -I${SYSROOT}/usr/include -I${ANDROID_PREFIX}/include -I${PREFIX}/include -pthread -fPIE"
export CPPFLAGS="${CFLAGS}"
export LDFLAGS="${LDFLAGS} -L${SYSROOT}/usr/lib  -L${PREFIX}/lib -L${ANDROID_PREFIX}/lib -lpthread -pie"

cd libusb-1.0.20/
make clean
./configure --host=${CROSS_COMPILE} --prefix=${PREFIX} --disable-shared "$@" --enable-udev=no
make install

cd ..

cd libusb-compat-0.1.5/
make clean
./configure --host=${CROSS_COMPILE} --prefix=${PREFIX} --disable-shared  "$@"
make install
cd ..

cd libftdi-0.20
make clean
./configure --host=${CROSS_COMPILE} --prefix=${PREFIX} --disable-shared "$@"
make
make install

cd ..

export LIBS="-lusb -lusb-1.0 -lftdi"

cd avrdude-6.3/
make clean
./bootstrap
./configure --host=${CROSS_COMPILE} --prefix=${PREFIX} --disable-shared "$@"
make install
