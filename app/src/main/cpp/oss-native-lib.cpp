#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <math.h>
#include <syslog.h>
#include <numeric>

#define LOG_TAG "====================== native-lib :"
#include "logger.h"
#include "common.h"

using namespace std;

extern "C" {

    /**
    * AES256 IV
    */
    JNIEXPORT jstring JNICALL
    Java_kr_co_goms_app_oss_jni_GomsJNI_ivKey(JNIEnv *env, jobject obj) {
        return env->NewStringUTF(KEY_AES256_IV);
    }

    /**
    * AES256 IV
    */
    JNIEXPORT jstring JNICALL
    Java_kr_co_goms_app_oss_jni_GomsJNI_encryptKey(JNIEnv *env, jobject obj) {
        return env->NewStringUTF(KEY_ENCRYPT);
    }

    /**
    * SERVER URL
    */
    JNIEXPORT jstring JNICALL
    Java_kr_co_goms_app_oss_jni_GomsJNI_requestURL(JNIEnv *env, jobject obj, jstring _url_code) {
        const char *num = env->GetStringUTFChars(_url_code, NULL);
        char code[strlen(num) + 1];
        strcpy(code, num);
        env->ReleaseStringUTFChars(_url_code, num);  //메모리 해제처리

        if (strcmp(code, CODE_URL_REQUEST_OSS_GROUP_LIST) == 0){
            return env->NewStringUTF(URL_REQUEST_OSS_GROUP_LIST);
        }else if (strcmp(code, CODE_URL_REQUEST_OSS_COMPANY_LIST) == 0) {
            return env->NewStringUTF(URL_REQUEST_OSS_COMPANY_LIST);
        }else if (strcmp(code, CODE_URL_REQUEST_OSS_FIELD_LIST) == 0) {
            return env->NewStringUTF(URL_REQUEST_OSS_FIELD_LIST);
        }else if (strcmp(code, CODE_URL_REQUEST_OSS_FIELD_DETAIL_LIST) == 0) {
            return env->NewStringUTF(URL_REQUEST_OSS_FIELD_DETAIL_LIST);
        }else if (strcmp(code, CODE_URL_REQUEST_OSS_FIELD_DETAIL_LIST_SUM) == 0) {
            return env->NewStringUTF(URL_REQUEST_OSS_FIELD_DETAIL_LIST_SUM);
        }else {
            return env->NewStringUTF(CODE_URL_REQUEST_OSS_GROUP_LIST);
        }
    }

}
