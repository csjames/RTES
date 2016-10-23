#!/bin/sh
export ARMGCC_DIR=/home/james/Downloads/gcc-arm-none-eabi-5_4-2016q3
export PATH=$PATH:/home/james/Downloads/gcc-arm-none-eabi-5_4-2016q3/bin
cmake -DCMAKE_TOOLCHAIN_FILE="../../../../../tools/cmake_toolchain_files/armgcc.cmake" -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Debug  .
make -j4
cmake -DCMAKE_TOOLCHAIN_FILE="../../../../../tools/cmake_toolchain_files/armgcc.cmake" -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Release  .
make -j4
cd debug && arm-none-eabi-objcopy -O srec hello_world.elf hello_world.srec && sudo cp hello_world.srec /run/media/james/FRDM-KL46Z
