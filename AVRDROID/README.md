# AVRDUDE METHOD

We probably could have issued commands to the FTDI chip using the library to do this without AVRDUDE and hence not require root (due to needing to access ports in /dev/usb). I kind of wanted the app to work for all atmel chips so used avrdude.

We also could have edited AVRDUDE and LIBUSB to take a unix file descriptor, which is given to us when android grants an app usb permission. This would have taken a while. The unix file descriptor would have had to be passed through a unix pipe so as to ensure it is still valid (as it is not valid for all processess, just the one which created it). 

## Cross Compiling AVR Dude

### This is where i started

https://lists.nongnu.org/archive/html/avrdude-dev/2014-10/msg00029.html

### Prerequisites

Go and get libusb, libusb-compat and libftdi source.

Go and get avrdude source.

The aforementioned programs follow the configure, make, install pattern. Running ./configure  will check environment variables and then generate a MakeFile for the system you describe in these env variables. You will need to set a lot to get things going although its very clear what is going on. See later down

## DONT FORGET pie AND fPIE

Will add links to this repo later

(Alternatively there is a precompiled binary in this repo)

You will always want the android ndk as it contains build tools e.g. gcc, g++ for the android platform.

NDK is THE Native Development Kit for android. If you want to interface with precompiled .so's, or compile C|C++ to run on 
android, use the NDK (im sure there are other ways but this is canonical) <- android studio does a good job of managing this for you surprisingly! just give it a ndk build script and add it to build phases and get happy

independant executables but the guy hasnt pushed since 2011 so dont hold your breath.

When you compile, you will have to set your environment variables like so : *** There is a complete script in avrdude_android ***

``` bash
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
export CFLAGS="${CFLAGS} --sysroot=${SYSROOT} -I${SYSROOT}/usr/include -I${PREFIX}/include -fPI"
export CPPFLAGS="${CFLAGS}"
export LDFLAGS="${LDFLAGS} -L${SYSROOT}/usr/lib  -L${PREFIX}/lib -pie"
```
Build in the order, or at least build avrdude last
libusb-1.0.20/
libusb-compat-0.1.5/
libftdi-0.20
avrdude-6.3/

# Get it on a device

## ADB WIRELESS DEBUG

Go get adb, its probably in android studio sdk. I dont know where windows puts that, probably in appdata, on linux it will be in $HOME/Android/platform-tools

Enable dev mode on your phone by tapping build number 5 times (in settings > about phone). Then turn on usb debug

Plug phone in, run ./adb devices. your phone should be there. If not then accept security warning on your phone to accept key

## Deploy Time

run ./adb tcpip 5555

then ./adb connect phone_id_address:5555

pushing the binary to the phone is peasy easy. just adb push avrdude /data/local/tmp for now

then adb shell and run the binary. it will run :O if it complains about PIE stuff, it hasnt compiled and linked as a PIE
