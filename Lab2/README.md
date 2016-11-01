#Cross Compiling AVR Dude

## Prerequisites

Go and get libusb, libusb-compat and libftdi source.

Go and get avrdude source.

Will add links to this repo later

(Alternatively there is a precompiled binary in this repo)

## ADB WIRELESS DEBUG

Go get adb, its probably in android studio sdk. i dont know where windows puts that, probably in appdata, on linux it will be in $HOME/Android/platform-tools

Enable dev mode on your phone by tapping build number 5 times (in settings > about phone). Then turn on usb debug

Plug phone in, run ./adb devices. your phone should be there. If not then accept security warning on your phone to accept key

## Deploy Time

run ./adb tcpip 5555

then ./adb connect phone_id_address:5555

pushing the binary to the phone is peasy easy. just adb push avrdude /data/local/tmp for now

then adb shell and run the binary. it will run :O if it complains about PIE stuff, it hasnt compiled and linked as a PIE



