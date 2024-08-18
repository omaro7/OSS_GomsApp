package kr.co.goms.app.oss.process;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;

import kr.co.goms.app.oss.process.command.IntroCommand;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;

public class IntroProcess {

    private static String LOG_TAG = "IntroProcess";

    private Activity activity;

    /**
     * 인트로 처리 완료 시 응답 용
     */
    private WaterCallBack introProcessCommonCallBack;

    /**
     * 인트로 화면에 붙어있는 처리 중 Animation
     */
    private ImageView animationImage = null;

    /**
     * 병렬처리 통합
     */
    ProcessGather processGather = null;
    // 통합할 프로세스 PID 발급
    int pidTaskIntro = 0;

    boolean onlyOnec = true;

    /**
     * 생성
     * @param callback
     */
    public void create(Activity activity, WaterCallBack callback) {
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess create() >>> IntroProcess 시작");
        this.activity = activity;
        introProcessCommonCallBack = callback;
        // 병렬처리 통합
        onCreateProcessGather();
    }

    /**
     * 병렬 처리 후 프로세스 통합
     */
    private void onCreateProcessGather() {
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess onCreateProcessGather() >>> Intro Pid 등록");
        processGather = new ProcessGather();
        processGather.create();
        processGather.setOnCompleteListener(new ProcessGather.Complete() {
            @Override
            public void complete(boolean flag, int pid, Object object, int remainProcess) {

                if(flag) {
                    BaseBean callBackData = new BaseBean();
                    callBackData.setStatus(BaseBean.STATUS.SUCCESS, "");
                    introProcessCommonCallBack.callback(callBackData);
                }
            }
        });

        pidTaskIntro = processGather.createProcess("Intro");
//        pidTaskSub = processGather.createProcess("Task Sub");
    }

    private void taskIntro() {
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess taskIntro() >>> 시작");
        WaterCallBack callback = new WaterCallBack() {
            @Override
            public void callback(BaseBean callBackData) {
                if(null == callBackData) {
                    return;
                }
                processGather.finishProcess(pidTaskIntro);
            }
        };

        IntroCommand introCommand = new IntroCommand();
        introCommand.setCallBack(callback);
        introCommand.command(activity, "");

    }

    public void windowFocusChanged(boolean hasFocus) {
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess windowFocusChanged() hasFocus : " + hasFocus);
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess windowFocusChanged() onlyOnec : " + onlyOnec);
        if(hasFocus) {
            if (onlyOnec) {
                onlyOnec = false;
                // Target API 23 이상 사용 시 permissionCheck() 사용
                permissionCheck();
            }
        }

    }

    /**
     * 권한 체크
     */
    private void permissionCheck() {
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess permissionCheck() >>> 시작");
        Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> IntroProcess permissionCheck() >>> 완료");
        taskIntro();
    }


    public void onBackPressed() {

    }


}
