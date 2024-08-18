package kr.co.goms.app.oss;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;

import kr.co.goms.aar.photoedit.PhotoEditModule;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.jni.GomsJNI;
import kr.co.goms.app.oss.model.CompanyBeanS;
import kr.co.goms.module.common.WaterFramework;
import kr.co.goms.module.common.application.ApplicationBackground;
import kr.co.goms.module.common.application.ApplicationInterface;
import kr.co.goms.module.common.curvlet.CurvletExit;
import kr.co.goms.module.common.curvlet.CurvletManager;
import kr.co.goms.module.common.curvlet.CurvletToast;

public class MyApplication extends ApplicationBackground implements ApplicationInterface, Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private static final String LOG_TAG = MyApplication.class.getSimpleName();

    public static MyApplication mMyApplication;
    public static GomsJNI mGomsJNI;
    //public static ExamDBHelper mExamDBHelper;
    private ManHolePrefs mPreference;

    private Activity currentActivity;

    private ArrayList<CompanyBeanS> mCompanyList;

    public MyApplication() {
        if(null != mMyApplication) {
            Log.d(LOG_TAG, "MyApplication() - 애플리케이션 생성자가 또 실행 되었다?");
        }
        Log.d(LOG_TAG, "################################## MyApplication() start! #################################");
        mMyApplication = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "################################## MyApplication::onCreate() start! ##########################");
        this.registerActivityLifecycleCallbacks(this);

        mMyApplication = this;
        mPreference = ManHolePrefs.getInstance(getApplicationContext());

        mGomsJNI = new GomsJNI(this);
        //mExamDBHelper = new ExamDBHelper(this);

        mCompanyList = new ArrayList<>();

        curvletApiSettings();

        onSetupLifecycleObserver();
        beginFramework();
        checkDarkMode();

        PhotoEditModule.initialize(this, PhotoEditModule.EDIT_SHARP_TYPE.NONE.name());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mMyApplication = null;
    }

    public void destroy() {
        Log.d(LOG_TAG, "destroy()");
        Log.e(LOG_TAG, "애플리케이션 종료");
    }

    @Override
    public void curvletApiSettings() {
        Log.d(LOG_TAG, "curvletApiSettings()");
        CurvletManager.I().addCurvlet("water", "appExit", CurvletExit.class);
        CurvletManager.I().addCurvlet("water", "toast", CurvletToast.class);
    }

    public void init() {
        Log.d(LOG_TAG, "init()");
    }

    public static synchronized MyApplication getInstance() {
        return mMyApplication;
    }

    public GomsJNI getGomsJNI(){
        if(mGomsJNI == null){
            mGomsJNI = new GomsJNI(this);
        }
        return mGomsJNI;
    }

    public ManHolePrefs prefs() {
        return mPreference;
    }

    public String encryptKey() {
        if (prefs().getKey().equals(ManHolePrefs.EMPTY)) {
            return mGomsJNI.getEncryptKey();
        } else {
            return prefs().getKey();
        }
    }

    public void beginFramework() {
        ApplicationInterface applicationInterface = WaterFramework.I().getApplication();

        if(null == applicationInterface) {
            WaterFramework.Listener listener = new WaterFramework.Listener() {
                @Override
                public void init() {
                    mMyApplication.init();
                }

                @Override
                public void destroy() {
                    mMyApplication.destroy();
                }

                @Override
                public void shutdown(Bundle bundle) {

                }

                @Override
                public void restore(Bundle bundle) {

                }
            };

            WaterFramework.I().create(this, listener, 720.0f, 1280.0f);
        }
    }

    /**
     * 다크모드 미지원 처리
     */
    private void checkDarkMode(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * DefaultLifecycleObserver method that shows the app open ad when the app moves to foreground.
     */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
    }

    /** ActivityLifecycleCallback methods. */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}

    public ArrayList<CompanyBeanS> getCompanyList() {
        return mCompanyList;
    }

    public void setCompanyList(ArrayList<CompanyBeanS> mCompanyList) {
        this.mCompanyList = mCompanyList;
    }

}
