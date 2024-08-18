package kr.co.goms.app.oss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.util.ArrayList;

import kr.co.goms.app.oss.activity.BaseActivity;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.process.IntroProcess;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;
import kr.co.goms.module.common.command.BaseBottomDialogCommand;
import kr.co.goms.module.common.command.Command;
import kr.co.goms.module.common.command.CommandExit;
import kr.co.goms.module.common.dialog.DialogPermissionNotice;
import kr.co.goms.module.common.manager.DialogCommandFactory;
import kr.co.goms.module.common.manager.DialogManager;
import kr.co.goms.module.common.util.GomsLog;

public class IntroActivity extends BaseActivity {

    private final String TAG = IntroActivity.class.getSimpleName();
    private IntroActivity mActivity;
    ConstraintLayout mRoot;
    IntroProcess mIntroProcess = null;

    private ArrayList<String> mPermissionCheckList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        checkUsbConnect();

        mRoot = findViewById(R.id.intro_root);

        /* Log 안찍기 */
        GomsLog.mDebugLog = AppConstant.APP_CONFIG_DEDUG;

        /* Debug mode이면 꺼져라*/
        boolean isBeingDebugged = android.os.Debug.isDebuggerConnected();
        if(isBeingDebugged){
            GomsLog.d(TAG, "Debugging : App Finish");
            appFinish();
        }

        if(checkRootingDevice()) {
            appFinish();
        }

        mActivity = this;

        startCheckPermission();
    }


    private void startCheckPermission(){
        if (!checkPermissions()) {
            if (mPermissionCheckList.size() == 1) {
                callPermissionPop();
            } else {
                //화면에 퍼미션 관련 Dialog를 띄운다
                DialogPermissionNotice dialogPermissionNotice = new DialogPermissionNotice(mActivity, mActivity, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callPermissionPop();
                    }
                });
                dialogPermissionNotice.show();
            }
        }else{
            startIntroData();
        }
    }

    private void startIntroData() {

        mIntroProcess = new IntroProcess();
        mIntroProcess.create(this, new WaterCallBack() {
            @Override
            public void callback(BaseBean baseBean) {
                switch (baseBean.getStatus()) {
                    case SUCCESS:
                        introProcessLoadComplete();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //퍼미션리스트 항목에 대한 수락 여부 체크
    private boolean checkPermissions() {
        boolean result = true;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Android 11 / R(30) 일 때는 requestPermission 무시함.
        }else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mPermissionCheckList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        /*
        if(Build.VERSION.SDK_INT >= 30) {
            if (checkSelfPermission(android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
                mPermissionCheckList.add(android.Manifest.permission.READ_PHONE_NUMBERS);
            }
        }

        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            mPermissionCheckList.add(Manifest.permission.READ_PHONE_STATE);
        }
         */


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPermissionCheckList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPermissionCheckList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mPermissionCheckList.add(Manifest.permission.CAMERA);
        }

        int iPermissionCheckTotal = 0;
        try{
            iPermissionCheckTotal = mPermissionCheckList.size();
        }catch(NullPointerException e){

        }
        if (iPermissionCheckTotal > 0) {
            result = false;
        }
        return result;
    }

    //퍼미션리스트 항목에 대한 시스템 Pop을 띄움
    @TargetApi(23)
    private void callPermissionPop() {
        if (mPermissionCheckList != null) {
            if (mPermissionCheckList.size() > 0) {
                requestPermissions(mPermissionCheckList.toArray(new String[mPermissionCheckList.size()]), 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                boolean result = true;
                if (grantResults != null && grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            result = false;
                            break;
                        }
                    }
                }

                //권한체크 true 완료 시
                if (result) {
                    startIntroData();
                } else {

                    DialogManager.I().setTitle("앱사용 권한 안내")
                            .setMessage("해당 앱을 사용하기 위해서는 [전화,저장] 권한을 허용해 주셔야 정상적인 서비스 이용이 가능합니다.\n\n앱 권한 설정화면으로 이동하시겠습니까?")
                            .setShowTitle(false)
                            .setShowMessage(true)
                            .setNegativeBtnName(getString(kr.co.goms.module.common.R.string.no))
                            .setPositiveBtnName(getString(kr.co.goms.module.common.R.string.yes))
                            .setCancelable(true)
                            .setCancelTouchOutSide(true)
                            .setCommand(DialogCommandFactory.I().createDialogCommand(mActivity, DialogCommandFactory.DIALOG_TYPE.permission.name(), new WaterCallBack() {
                                @Override
                                public void callback(BaseBean baseData) {
                                    String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                                    if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){
                                        //앱 종료 처리
                                        Command exit = new CommandExit();
                                        exit.command(mActivity, "");
                                    }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){
                                        //설정이동
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
                                        mActivity.startActivity(intent);
                                        mActivity.finish();
                                    }
                                }
                            }))
                            .showDialog(mActivity);

                }
                break;
        }
    }

    /**
     * Intro Load 완료
     */
    protected void introProcessLoadComplete() {
        GomsLog.d(TAG, "introProcessLoadComplete() : 인트로 프로세스 완료");
        MyApplication.getInstance().prefs().put(ManHolePrefs.MB_IDX, "1");
        String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
        MyApplication.getInstance().prefs().put(ManHolePrefs.MB_IDX, "1");
        GomsLog.d(TAG, "introProcessLoadComplete() : MyApplication.getInstance().getMbIdx() : " + mbIdx);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();

        MyApplication.getInstance().prefs().put(ManHolePrefs.IS_FIRST_TIME_EXECUTE_THE_APP, true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        GomsLog.d(TAG, "onWindowFocusChanged() hasFocus : " + hasFocus);
        if(hasFocus){
            hideSystemUI();
        }
        if (null != mIntroProcess) {
            mIntroProcess.windowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 배포버전 시, 사용자가 USB 꽂고 디버깅 버드일 때 앱 실행이 되지 않도록 조치함
     */
    public void checkUsbConnect(){
        /*
        //usb 연결 여부 확인, 꽂았다가 뺐을 때 바로 앱실행시, 꽂음으로 인식함. receiver이기에 조금 시차기 있는 듯함.
        if(!BuildConfig.DEBUG) { //디버그모드 아닐 때 체크
            //배포판에서 USB연결 후 실행 시, 앱 중지시켜버림
            Intent intent = registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
            if (intent != null && intent.getExtras() != null && intent.getBooleanExtra("connected", false)) {

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    Toast.makeText(this, "USB연결(디버그) 시, 앱 실행을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(IntroActivity.this, "USB연결(디버그) 시, 앱 실행을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                appFinish();
            }
        }
         */
    }

    /**
     * 앱 무결성 체크. 사인값 비교
     */
    @SuppressLint("PackageManagerGetSignatures")
    private void checkSignature(){
        PackageManager pm = mActivity.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(AppConstant.APP_URI, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        Signature[] signatures = packageInfo.signatures;


    }

    /**
     * 루팅체크
     * 체크방법 : 녹스플레이어에서 root 선택한 후에 해당 apk 설치 처리
     *리@return
     */
    private boolean checkRootingDevice() {
        boolean isRooting = false;
        String ROOT_PATH = Environment.getExternalStorageDirectory() + "";

        String[] RootFilesPath = new String[]{
            ROOT_PATH + "/sbin/su",
            ROOT_PATH + "/system/su" ,
            ROOT_PATH + "/system/bin/su" ,
            ROOT_PATH + "/system/sbin/su" ,
            ROOT_PATH + "/system/xbin/su" ,
            ROOT_PATH + "/system/xbin/mu" ,
            ROOT_PATH + "/system/bin/.ext/.su" ,
            ROOT_PATH + "/system/usr/su-backup" ,
            ROOT_PATH + "/system/bin/.ext" ,
            ROOT_PATH + "/system/xbin/.ext" ,
            ROOT_PATH + "/system/app/SuperUser.apk" ,
            ROOT_PATH + "/system/app/Superuser.apk" ,
            ROOT_PATH + "/system/app/superUser.apk" ,
            ROOT_PATH + "/system/app/superuser.apk" ,
            ROOT_PATH + "/system/app/su.apk" ,
            ROOT_PATH + "/system/sd/xbin/su",
            ROOT_PATH + "/system/bin/failsafe/su",
            ROOT_PATH + "/data/local/xbin/su",
            ROOT_PATH + "/data/local/bin/su",
            ROOT_PATH + "/data/local/su",
            ROOT_PATH + "/su/bin/su",
            ROOT_PATH + "/data/data/com.noshufou.android.su"
        };
        try {
            Runtime.getRuntime().exec("su");
            isRooting = true;
        } catch ( Exception e) {
            // Exception 나면 루팅 false;
            isRooting = checkRootingFiles(createFiles(RootFilesPath));
        }
        return isRooting;
    }

    /**
     * 루팅파일 의심 Path를 가진 파일들을 생성 한다.
     */
    private File[] createFiles(String[] sfiles) {
        File[] rootingFiles = new File[sfiles.length];
        for (int i = 0; i < sfiles.length; i++)
            rootingFiles[i] = new File(sfiles[i]);
        return rootingFiles;
    }

    /**
     * 루팅파일 여부를 확인 한다.
     */
    private boolean checkRootingFiles(File... file) {
        for (File f : file) {
            if (f != null && f.exists() && f.isFile())
                return true;
        }
        return false;
    }

    /** app finish */
    public void appFinish(){
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void hideSystemUI(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
