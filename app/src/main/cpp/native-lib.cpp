#include <jni.h>
#include <string>
#include <android/log.h>
#include "Util.h"
#include "PasswordGenerator.h"

extern "C" JNIEXPORT jstring
JNICALL
Java_com_jacksonke_teresapassword_Generator_generate(
        JNIEnv *env,
        jobject /* this */,
        jstring name,
        jstring key,
        jint ver,
        jint type) {

    Util::logger("re-generate from jni");

    char szKey[512]="\0";
    Util::convertJString2LocalString(env, key, szKey, "failed to convert key");

    char szName[512]="\0";
    Util::convertJString2LocalString(env, name, szName, "failed to convert name");

    PasswordGenerator generator = PasswordGenerator(szKey, szName);
    generator.setType(type);
    generator.setVersion(ver);
    PasswordGenerator& ref = generator;

    return env->NewStringUTF(ref.generate().c_str());
}