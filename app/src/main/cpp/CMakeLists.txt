# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("Gallery")


include(ExternalProject)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#添加子目录,将会调用子目录中的CMakeLists.txt
ADD_SUBDIRECTORY(jni)
ADD_SUBDIRECTORY(jni_egl)
ADD_SUBDIRECTORY(jni_jpegstream)

#ExternalProject_Add(
#        jni  # 使用简单、不含斜杠的名称
#        SOURCE_DIR ${CMAKE_SOURCE_DIR}/jni  # 本地代码目录
#        CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${JNI_INSTALL_DIR}
#        UPDATE_COMMAND ""
#        PATCH_COMMAND ""
#        BUILD_COMMAND ${CMAKE_COMMAND} --build <BINARY_DIR> --target install
#        INSTALL_COMMAND ""
#)
#ExternalProject_Add(
#        jni_egl  # 使用简单、不含斜杠的名称
#        SOURCE_DIR ${CMAKE_SOURCE_DIR}/jni_egl  # 本地代码目录
#        CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${JNI_EGL_INSTALL_DIR}
#        UPDATE_COMMAND ""
#        PATCH_COMMAND ""
#        BUILD_COMMAND ${CMAKE_COMMAND} --build <BINARY_DIR> --target install
#        INSTALL_COMMAND ""
#)
#ExternalProject_Add(
#        jni_jpegstream  # 使用简单、不含斜杠的名称
#        SOURCE_DIR ${CMAKE_SOURCE_DIR}/jni_jpegstream  # 本地代码目录
#        CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${JNI_JPEGSTREAM_INSTALL_DIR}
#        UPDATE_COMMAND ""
#        PATCH_COMMAND ""
#        BUILD_COMMAND ${CMAKE_COMMAND} --build <BINARY_DIR> --target install
#        INSTALL_COMMAND ""
#)
