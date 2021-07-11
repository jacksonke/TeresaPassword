//
// Created by MAC OS on 28/08/2018.
//

#include "Util.h"
#include <stdio.h>
#include <string.h>
#include <android/log.h>



int
Util::convertJString2LocalString(JNIEnv *env, jstring myJString, char *szLocal, const char *szFailedLog) {
    const char* str;
    str = env->GetStringUTFChars(myJString, NULL);

    if (0 != str) {
        strcpy(szLocal, str);
        env->ReleaseStringUTFChars(myJString, str);
        return 1;
    }
    else{
        if (szFailedLog != NULL){
            logger("%s", szFailedLog);
        }

        return 0;
    }
}

void Util::logger(const char *format, ...) {
//#ifdef KE_DEBUG
    char buf[2048] = "\0";

    va_list args;
    va_start(args,format);
    vsprintf(buf + strlen(buf), format, args);
    va_end(args);

    __android_log_write(ANDROID_LOG_INFO, szDefaultTag, buf);

//#endif
}
