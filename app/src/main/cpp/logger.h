//
// Created by store_camera on 2018-07-24.
//

#ifndef IMPLAYERTEST_LOGGER_H
#define IMPLAYERTEST_LOGGER_H

#include <strings.h>
#include <android/log.h>

#define LOG_GOMS(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/*
static void print_gl_string( const char* name, GLenum s ){
    const char* v = ( const char* )glGetString( s );
    LOG_INFO( "GL %s = %s\n", name, v );
}

static void check_gl_error( const char* op ){
    for( int error = glGetError(); error; error = glGetError() ){
        LOG_INFO( "after %s() glError (0x%x)\n", op, error );
    }
}*/

#endif //IMPLAYERTEST_LOGGER_H