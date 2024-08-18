package kr.co.goms.app.oss.jni;


import kr.co.goms.app.oss.MyApplication;

public class GomsJNI {

    public native String ivKey();
    public native String encryptKey();
    public native String requestURL(String urlCode);

    private static MyApplication mApp;

    static {
        System.loadLibrary("oss-native-lib");
    }

    public GomsJNI(MyApplication app)
    {
        mApp = app;
    }

    // 암호키
    public String getEncryptKey()
    {
        return encryptKey();
    }


}
