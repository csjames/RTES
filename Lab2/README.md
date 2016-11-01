# Cross Compiling AVR Dude

## Prerequisites

Go and get libusb, libusb-compat and libftdi source.

Go and get avrdude source.

The aforementioned programs follow the configure.ac pattern. This file will check environment variables and then generate
a MakeFile for the system you describe in these env variables. You will need to set a lot to get things going although
its very clear what is going on. See later down

# DONT FORGET pie AND fPIE

Will add links to this repo later

(Alternatively there is a precompiled binary in this repo)

You will always want the android ndk as it contains build tools e.g. gcc, g++ for the android platform.

NDK is THE Native Development Kit for android. If you want to interface with precompiled .so's, or compile C|C++ to run on 
android, use the NDK (im sure there are other ways but this is canonical) <- android studio does a good job of managing
this for you surprisingly! just give it a makefile and add it to build phases and get happy

https://github.com/tladyman/avrdude-android You can alternatively use this. I have put in a pull request for platform
independant executables but the guy hasnt pushed since 2011 so dont hold your breath.

When you compile, you will have to set your environment variables like so : 

``` bash
export DEV_PREFIX=/home/james/avrdude-android # output path
export CROSS_COMPILE=arm-linux-androideabi # cross compiler path
export ANDROID_PREFIX=/home/james/avrdude-android/tc # location of all header and source from ndk
export SYSROOT=/home/james/avrdude-android/tc/sysroot # more header and source files
export CROSS_PATH=${ANDROID_PREFIX}/bin/${CROSS_COMPILE} # this is where the cross compiler is

echo $CROSS_PATH

export CPP="${CROSS_PATH}-cpp --sysroot=$SYSROOT"
export AR="${CROSS_PATH}-ar "
export AS="${CROSS_PATH}-as --sysroot=$SYSROOT"
export NM="${CROSS_PATH}-nm --sysroot=$SYSROOT"
export CC="${CROSS_PATH}-gcc --sysroot=$SYSROOT"
export CXX="${CROSS_PATH}-g++ --sysroot=$SYSROOT"
export LD="${CROSS_PATH}-ld --sysroot=$SYSROOT"
export RANLIB="${CROSS_PATH}-ranlib "

export PREFIX=${DEV_PREFIX}/android-builds
export PKG_CONFIG_PATH=${PREFIX}/lib/pkgconfig
export CFLAGS="${CFLAGS} --sysroot=${SYSROOT} -I${SYSROOT}/usr/include -I${ANDROID_PREFIX}/include -I${PREFIX}/include -fPIE -pie"
export CPPFLAGS="${CFLAGS}"
export LDFLAGS="${LDFLAGS} -L${SYSROOT}/usr/lib  -L${PREFIX}/lib -L${ANDROID_PREFIX}/lib -pie"
export LIBS="-lusb -lusb-1.0 -lftdi"
```

# Get it on a device

## ADB WIRELESS DEBUG

Go get adb, its probably in android studio sdk. i dont know where windows puts that, probably in appdata, on linux it will be in $HOME/Android/platform-tools

Enable dev mode on your phone by tapping build number 5 times (in settings > about phone). Then turn on usb debug

Plug phone in, run ./adb devices. your phone should be there. If not then accept security warning on your phone to accept key

## Deploy Time

run ./adb tcpip 5555

then ./adb connect phone_id_address:5555

pushing the binary to the phone is peasy easy. just adb push avrdude /data/local/tmp for now

then adb shell and run the binary. it will run :O if it complains about PIE stuff, it hasnt compiled and linked as a PIE



