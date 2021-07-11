//
// Created by MAC OS on 28/08/2018.
//

#ifndef TERESAKEY_UTIL_H
#define TERESAKEY_UTIL_H

#include <jni.h>

static const char* szDefaultTag = "ksyJniLogger";

class Util {
public: static int convertJString2LocalString(JNIEnv *env, jstring myJString, char* szLocal, const char* szFailedLog);

public:static void logger(const char *format, ...);


};


#endif //TERESAKEY_UTIL_H
