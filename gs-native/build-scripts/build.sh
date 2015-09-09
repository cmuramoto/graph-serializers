#!/bin/bash

function clean(){
    rm -f ./Release/*
}

function compile(){
    echo "Building SIMD lib for $1"
    echo "Building file: ../com_nc_gs_io_UTF8Util.c"
    echo "Invoking: GCC C Compiler"
    target="-D__GS__$1__"
    cmd='gcc '$target' -O3 -Wall -Wunused-label -c -msse4.2 -mavx2 -fmessage-length=0 -MMD -MP -MF"Release/com_nc_gs_io_UTF8Util.d" -MT"Release/com_nc_gs_io_UTF8Util.d" -o "Release/com_nc_gs_io_UTF8Util.o" "com_nc_gs_io_UTF8Util.c"'
    echo $cmd
    eval $cmd
    echo "Finished building: ../com_nc_gs_io_UTF8Util.c"
}

function link(){
    echo "Building target: libgs-native-$1.so"
    echo "Invoking: GCC C Linker"
    cmd='gcc -shared -o "Release/libgs-native-'"$1"'.so"  ./Release/com_nc_gs_io_UTF8Util.o'
    echo $cmd
    eval $cmd 
    echo "Finished building target: libgs-native-$1.so"

}

function copy(){
    if [ "copy" = "$2" ];
    then
        cmd='cp "Release/libgs-native-'"$1"'.so"  ../../graph-serializers/src/main/resources/precompiled'
        echo $cmd
        eval $cmd
    else
        echo 'Lib ready to be copied to ../../graph-serializers/src/main/resources/precompiled'
    fi
}

function build(){
    pushd ../ > /dev/null
    clean
    compile $1
    link $1
    copy $1 $2	
}

function postAction(){
   rm -f Release/*.o	
   rm -f Release/*.d
}

trap postAction EXIT;

case "$1" in
        SSE3 | AVX2 | AVX512)
                build $1 $2
        ;;
        *)
                echo "Usage: "`basename $0`" (SSE3|AVX2|AVX512)"
                exit 1
        ;;
esac
