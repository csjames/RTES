# RTES
Woooo

# Place this in the demo_apps folder of the KSDK (examples/frdmkl46z/demo_apps/helo_world

## Install armgcc and export following path variabnles 
export PATH=$PATH:/path/to/gcc-arm-none-eabi-5_4-2016q3/bin
export CC_DIR=/path/to/gcc-arm-none-eabi-5_4-2016q3
export ARMGCC_DIR=/path/to/gcc-arm-none-eabi-5_4-2016q3

## Build with ./build-all.sh provided in top level of repo.

If your board mounts at a different location to the one described inside this script you will be required to change that path.
Then the .srec should be deployed to the board on build.
