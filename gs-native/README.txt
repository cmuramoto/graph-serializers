Utf8 to Utf16 expansion using SSE.

Compile with:

gcc -I/opt/tools/java/latest/include/linux -I/opt/tools/java/latest/include -O3 -g3 -Wall -msse4.1 -mavx2 -c -fmessage-length=0 -MMD -MP -MF"com_nc_gs_io_UTF8Util.d" -MT"com_nc_gs_io_UTF8Util.d" -o "com_nc_gs_io_UTF8Util.o" "../com_nc_gs_io_UTF8Util.c"

Then link it:

gcc -shared -o "libgs-native.so"  ./com_nc_gs_io_UTF8Util.o