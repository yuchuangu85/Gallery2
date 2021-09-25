# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_C_FLAGS
#        -ffast-math
#        -O3
#        -funroll-loops
#        -Wall
#        -Wextra
#        -Werror)
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(lib_src_DIR ../jni_jpeg)

set(lib_build_DIR ${lib_src_DIR}/outputs)
file(MAKE_DIRECTORY ${lib_build_DIR})
include_directories(src)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 配置so库信息
add_library( # Sets the name of the library.
        # 生成的so库名称，此处生成的so文件名称是libfilter-lib.so
        jni_jpegstream

        # Sets the library as a shared library.
        # STATIC：静态库，是目标文件的归档文件，在链接其它目标的时候使用
        # SHARED：动态库，会被动态链接，在运行时被加载
        # MODULE：模块库，是不会被链接到其它目标中的插件，但是可能会在运行时使用dlopen-系列的函数动态链接
        SHARED

        # Provides a relative path to your source file(s).
        # 资源文件，可以多个，
        # 资源路径是相对路径，相对于本CMakeLists.txt所在目录
        src/inputstream_wrapper.cpp
        src/jerr_hook.cpp
        src/jpeg_hook.cpp
        src/jpeg_reader.cpp
        src/jpeg_writer.cpp
        src/jpegstream.cpp
        src/outputstream_wrapper.cpp
        src/stream_wrapper.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# 从系统查找依赖库（NDK <= 19）
#find_library( # Sets the name of the path variable.
#        # android系统每个类型的库会存放一个特定的位置，而log库存放在log-lib中
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        # android系统在c环境下打log到logcat的库
#        log)

add_subdirectory(${lib_src_DIR} ${lib_build_DIR})
add_library(turbojpep STATIC IMPORTED)
set_target_properties(turbojpeg PROPERTIES IMPORTED_LOCATION ${lib_build_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libturbojpeg.a)
add_library(jpep STATIC IMPORTED)
set_target_properties(jpeg PROPERTIES IMPORTED_LOCATION ${lib_build_DIR}/${CMAKE_ANDROID_ARCH_ABI}/libjpeg.a)

include_directories( ${lib_src_DIR})

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# 配置库的链接（依赖关系）(NDK > 19)
target_link_libraries( # Specifies the target library.

        # 目标库，与 add_library 的库名称一定要相同
        jni_jpegstream

        turbojpeg
        jpeg
        log

        # Links the target library to the log library
        # included in the NDK.
        # 将目标库链接到日志库包含在NDK。
        ${log-lib})
#
