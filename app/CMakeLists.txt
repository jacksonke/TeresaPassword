# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/PasswordGenerator.cpp
             src/main/cpp/Util.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )



#### crypto
add_library( crypto
             STATIC
             IMPORTED )

set_target_properties( # Specifies the target library.
                       crypto

                       # Specifies the parameter you want to define.
                       PROPERTIES IMPORTED_LOCATION

                       # 这里需要使用绝对路径，不然在 osx 上找不到。详见 https://stackoverflow.com/questions/41478323/cmakemissing-and-no-known-rule-to-make-it-when-i-import-a-prebuilt-library
                       # PROJECT_SOURCE_DIR 和 CMAKE_SOURCE_DIR 的差别 https://stackoverflow.com/questions/32028667/are-cmake-source-dir-and-project-source-dir-the-same-in-cmake/32030551
                       # CMAKE_SOURCE_DIR CMakeList.txt 所在的文件夹
                       # Provides the path to the library you want to import.
                       ${CMAKE_SOURCE_DIR}/prebuilt-libs/openssl/arch-${ANDROID_ABI}/lib/libcrypto.a )

##### ssl
add_library( ssl
             STATIC
             IMPORTED )

set_target_properties( # Specifies the target library.
                      ssl

                       # Specifies the parameter you want to define.
                       PROPERTIES IMPORTED_LOCATION

                       # Provides the path to the library you want to import.
                       ${CMAKE_SOURCE_DIR}/prebuilt-libs/openssl/arch-${ANDROID_ABI}/lib/libssl.a )


include_directories( prebuilt-libs/openssl/include/ )




# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       ssl
                       crypto

                       )

