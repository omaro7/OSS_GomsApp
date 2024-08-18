package kr.co.goms.app.oss.manager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.app.oss.R;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;
import kr.co.goms.module.common.command.BaseBottomDialogCommand;
import kr.co.goms.module.common.command.InputFormBottomDialogCommand;
import kr.co.goms.module.common.manager.DialogCommandFactory;
import kr.co.goms.module.common.manager.DialogManager;
import kr.co.goms.module.common.util.GomsLog;
import kr.co.goms.module.common.util.StringUtil;

public class SpinnerManager {

    private static final String TAG = SpinnerManager.class.getSimpleName();

    static SpinnerManager instance;
    static Activity mActivity;
    private HashMap<String, Spinner> mSpinnerMap = new HashMap<>();
    private HashMap<String, Adapter> mAdapterMap = new HashMap<>();

    public enum DAMAGE_TYPE{
        GOOD("양호"),
        LARGE("대"),
        MEDIUM("중"),
        SMALL("소"),
        ;
        String type;
        DAMAGE_TYPE(String type) {
            this.type = type;
        }
    }
    //Yes or No
    public enum YN_TYPE{
        SELECT("선택"),
        Y("O"),
        N("X"),
        ;
        String type;
        YN_TYPE(String type) {
            this.type = type;
        }
    }

    public enum INPUT_TYPE{
        LidStandard,            //규격
        Material,               //재질
        Drainage,               //배제방식
        Size,                   //사이즈
        LidDamageLMS,           //뚜껑파손
        LidDamageYN,            //뚜껑파손여부
        LidCrackLMS,            //뚜껑균열
        LidCrackYN,             //뚜껑균열여부
        LidWaterInnerLMS,        //뚜껑유입수등급
        OuterDamageLMS,         //뚜껑주변부손상 등급
        OuterDamageYN,          //뚜껑주변부손상여부
        OuterCrackLMS,          //뚜껑주변주 균열 등급
        OuterCrackYN,           //뚜껑주변부 균열 여부
        //OuterWaterInnerYN,      //뚜껑주변부
        OuterWaterInvasionLMS,  //뚜껑주변부 침입수 등급
        InnerDamageLMS,         //뚜껑내부 손상 등급
        InnerDamageYN,          //뚜껑내부 손상 여부
        InnerCrackLMS,          //뚜껑내부 균열 등급
        InnerCrackYN,           //뚜껑내부 균열 여부
        InnerWaterInvasionLMS,  //뚜껑내부 침입수 등급
        PipeDamageLMS,          //관로접합부 손상 등급
        PipeDamageYN,           //관로접합부 손상 여부
        PipeCrackLMS,           //관로접합부 균열 등급
        PipeCrackYN,            //관로접합부 균열 여부
        PipeWaterInvasionLMS,   //관로접합부 침입수 등급
        LadderYN,               //사다리 유무
        InvertYN,               //인버트 유무
        OdorGLMS,               //악취 등급
        LidSealingYN,           //뚜껑밀폐 여부
        LadderDamageLMS,        //사다리손상 등급
        EndotheliumLMS,         //내피생성 등급
        WasteOilLMS,            //폐유밀착 등급
        TempObstacleGLMS,       //임시장애물 등급
        RootIntrusionGLMS,      //뿌리침입 등급
        BlockGapLMS,            //이음부단차 등급
        BlockDamageLMS,         //이음부손상 등급
        BlockLeaveLMS,          //이음부이탈 등급
        SurfaceGapLMS,          //표면단차 등급
        BuriedYN,                 //매몰
    }

    //규격
    public enum LID_STANDARD_TYPE{
        SELECT("선택"),
        STANDARD_1("1호"),
        STANDARD_2("2호"),
        STANDARD_3("3호"),
        STANDARD_4("4호"),
        STANDARD_5("5호"),
        STANDARD_S_1("특1호"),
        STANDARD_S_2("특2호"),
        STANDARD_S_3("특3호"),
        STANDARD_S_4("특4호"),
        STANDARD_S_5("특5호"),
        STANDARD_A("현장타설"),
        STANDARD_B("부관붙힘"),
        STANDARD_C("소형"),
        STANDARD_D("직접입력"),
        ;
        String type;
        LID_STANDARD_TYPE(String type) {
            this.type = type;
        }
    }
    //배제방식 drainage
    public enum DRAINAGE_TYPE{
        SELECT("선택"),
        DRAINAGE_1("오수"),
        DRAINAGE_2("우수"),
        DRAINAGE_3("합류"),
        DRAINAGE_4("차집"),
        DRAINAGE_D("직접입력"),
        ;
        String type;
        DRAINAGE_TYPE(String type) {
            this.type = type;
        }
    }
    //재질
    public enum MATERIAL_TYPE{
        SELECT("선택"),
        MATERIAL_1("주철"),
        MATERIAL_2("닥타일"),
        MATERIAL_3("콘크리트"),
        MATERIAL_4("그레이팅"),
        MATERIAL_D("직접입력"),
        ;
        String type;
        MATERIAL_TYPE(String type) {
            this.type = type;
        }
    }
    //사이즈
    public enum SIZE_TYPE{
        SELECT("선택"),
        SIZE_648("648"),
        SIZE_700("700"),
        SIZE_900("900"),
        SIZE_D("직접입력"),
        ;
        String type;
        SIZE_TYPE(String type) {
            this.type = type;
        }
    }

    //맨홀뚜껑 파손 여부
    public enum LID_DAMAGE_YN{
        SELECT("선택"),
        LID_DAMAGE_Y(YN_TYPE.Y.type),
        LID_DAMAGE_N(YN_TYPE.N.type),
        ;
        String type;
        LID_DAMAGE_YN(String type) {
            this.type = type;
        }
    }
    //맨홀뚜껑 파손 타입
    public enum LID_DAMAGE_LMS{
        SELECT("선택"),
        LID_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        LID_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        LID_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        LID_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }
    //맨홀뚜껑 균열 여부
    public enum LID_CRACK_YN{
        SELECT("선택"),
        LID_CRACK_Y(YN_TYPE.Y.type),
        LID_CRACK_N(YN_TYPE.N.type),
        ;
        String type;
        LID_CRACK_YN(String type) {
            this.type = type;
        }
    }
    //맨홀뚜껑 균열 타입
    public enum LID_CRACK_LMS{
        SELECT("선택"),
        LID_CRACK_L(DAMAGE_TYPE.LARGE.type),
        LID_CRACK_M(DAMAGE_TYPE.MEDIUM.type),
        LID_CRACK_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        LID_CRACK_LMS(String type) {
            this.type = type;
        }
    }

    //맨홀뚜껑 유입수 여부
    public enum LID_WATER_INNER_LMS{
        SELECT("선택"),
        LID_WATER_INNER_L(DAMAGE_TYPE.LARGE.type),
        LID_WATER_INNER_M(DAMAGE_TYPE.MEDIUM.type),
        LID_WATER_INNER_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        LID_WATER_INNER_LMS(String type) {
            this.type = type;
        }
    }

    /*
    //맨홀뚜껑 침입수 여부
    public enum LID_WATER_INVASION_LMS{
        SELECT("선택"),
        LID_WATER_INVASION_L(DAMAGE_TYPE.LARGE.type),
        LID_WATER_INVASION_M(DAMAGE_TYPE.MEDIUM.type),
        LID_WATER_INVASION_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        LID_WATER_INVASION_LMS(String type) {
            this.type = type;
        }
    }
     */

    //맨홀주변부 파손 여부
    public enum OUTER_DAMAGE_YN{
        SELECT("선택"),
        OUTER_DAMAGE_Y(YN_TYPE.Y.type),
        OUTER_DAMAGE_N(YN_TYPE.N.type),
        ;
        String type;
        OUTER_DAMAGE_YN(String type) {
            this.type = type;
        }
    }
    //맨홀주변부 파손 타입
    public enum OUTER_DAMAGE_LMS{
        SELECT("선택"),
        OUTER_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        OUTER_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        OUTER_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        OUTER_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }
    //맨홀주변부 균열 여부
    public enum OUTER_CRACK_YN{
        SELECT("선택"),
        OUTER_CRACK_Y(YN_TYPE.Y.type),
        OUTER_CRACK_N(YN_TYPE.N.type),
        ;
        String type;
        OUTER_CRACK_YN(String type) {
            this.type = type;
        }
    }
    //맨홀주변부 균열 타입
    public enum OUTER_CRACK_LMS{
        SELECT("선택"),
        OUTER_CRACK_L(DAMAGE_TYPE.LARGE.type),
        OUTER_CRACK_M(DAMAGE_TYPE.MEDIUM.type),
        OUTER_CRACK_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        OUTER_CRACK_LMS(String type) {
            this.type = type;
        }
    }

    //맨홀주변부 유입수 여부
    public enum OUTER_WATER_INNER_YN{
        SELECT("선택"),
        OUTER_WATER_INNER_Y(YN_TYPE.Y.type),
        OUTER_WATER_INNER_N(YN_TYPE.N.type),
        ;
        String type;
        OUTER_WATER_INNER_YN(String type) {
            this.type = type;
        }
    }

    //맨홀주변부 침입수 여부
    public enum OUTER_WATER_INVASION_LMS{
        SELECT("선택"),
        OUTER_WATER_INVASION_L(DAMAGE_TYPE.LARGE.type),
        OUTER_WATER_INVASION_M(DAMAGE_TYPE.MEDIUM.type),
        OUTER_WATER_INVASION_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        OUTER_WATER_INVASION_LMS(String type) {
            this.type = type;
        }
    }

    //맨홀내부 파손 여부
    public enum INNER_DAMAGE_YN{
        SELECT("선택"),
        INNER_DAMAGE_Y(YN_TYPE.Y.type),
        INNER_DAMAGE_N(YN_TYPE.N.type),
        ;
        String type;
        INNER_DAMAGE_YN(String type) {
            this.type = type;
        }
    }
    //맨홀내부 파손 타입
    public enum INNER_DAMAGE_LMS{
        SELECT("선택"),
        INNER_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        INNER_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        INNER_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        INNER_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }
    //맨홀내부 균열 여부
    public enum INNER_CRACK_YN{
        SELECT("선택"),
        INNER_CRACK_Y(YN_TYPE.Y.type),
        INNER_CRACK_N(YN_TYPE.N.type),
        ;
        String type;
        INNER_CRACK_YN(String type) {
            this.type = type;
        }
    }
    //맨홀내부 균열 타입
    public enum INNER_CRACK_LMS{
        SELECT("선택"),
        INNER_CRACK_L(DAMAGE_TYPE.LARGE.type),
        INNER_CRACK_M(DAMAGE_TYPE.MEDIUM.type),
        INNER_CRACK_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        INNER_CRACK_LMS(String type) {
            this.type = type;
        }
    }

    //맨홀내부 침입수 여부
    public enum INNER_WATER_INVASION_LMS{
        SELECT("선택"),
        INNER_WATER_INVASION_L(DAMAGE_TYPE.LARGE.type),
        INNER_WATER_INVASION_M(DAMAGE_TYPE.MEDIUM.type),
        INNER_WATER_INVASION_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        INNER_WATER_INVASION_LMS(String type) {
            this.type = type;
        }
    }

    //관로접합부 파손 여부
    public enum PIPE_DAMAGE_YN{
        SELECT("선택"),
        PIPE_DAMAGE_Y(YN_TYPE.Y.type),
        PIPE_DAMAGE_N(YN_TYPE.N.type),
        ;
        String type;
        PIPE_DAMAGE_YN(String type) {
            this.type = type;
        }
    }
    //관로접합부 파손 타입
    public enum PIPE_DAMAGE_LMS{
        SELECT("선택"),
        PIPE_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        PIPE_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        PIPE_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        PIPE_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }
    //관로접합부 균열 여부
    public enum PIPE_CRACK_YN{
        SELECT("선택"),
        PIPE_CRACK_Y(YN_TYPE.Y.type),
        PIPE_CRACK_N(YN_TYPE.N.type),
        ;
        String type;
        PIPE_CRACK_YN(String type) {
            this.type = type;
        }
    }
    //관로접합부 균열 타입
    public enum PIPE_CRACK_LMS{
        SELECT("선택"),
        PIPE_CRACK_L(DAMAGE_TYPE.LARGE.type),
        PIPE_CRACK_M(DAMAGE_TYPE.MEDIUM.type),
        PIPE_CRACK_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        PIPE_CRACK_LMS(String type) {
            this.type = type;
        }
    }

    //관로접합부 침입수 여부
    public enum PIPE_WATER_INVASION_LMS{
        SELECT("선택"),
        PIPE_WATER_INVASION_L(DAMAGE_TYPE.LARGE.type),
        PIPE_WATER_INVASION_M(DAMAGE_TYPE.MEDIUM.type),
        PIPE_WATER_INVASION_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        PIPE_WATER_INVASION_LMS(String type) {
            this.type = type;
        }
    }

    //사디리 유무
    public enum LADDER_YN{
        SELECT("선택"),
        LADDER_Y(YN_TYPE.Y.type),
        LADDER_N(YN_TYPE.N.type),
        ;
        String type;
        LADDER_YN(String type) {
            this.type = type;
        }
    }

    //인버트 유무
    public enum INVERT_YN{
        SELECT("선택"),
        INVERT_Y(YN_TYPE.Y.type),
        INVERT_N(YN_TYPE.N.type),
        ;
        String type;
        INVERT_YN(String type) {
            this.type = type;
        }
    }

    //악취발생 양호,대중소
    public enum ODOR_GLMS{
        SELECT("선택"),
        ODOR_G(DAMAGE_TYPE.GOOD.type),
        ODOR_L(DAMAGE_TYPE.LARGE.type),
        ODOR_M(DAMAGE_TYPE.MEDIUM.type),
        ODOR_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        ODOR_GLMS(String type) {
            this.type = type;
        }
    }

    //뚜껑밀폐 여부
    public enum LID_SEALING_YN{
        SELECT("선택"),
        LID_SEALING_Y(YN_TYPE.Y.type),
        LID_SEALING_N(YN_TYPE.N.type),
        ;
        String type;
        LID_SEALING_YN(String type) {
            this.type = type;
        }
    }

    //사다리 손상 대중소
    public enum LADDER_DAMAGE_LMS{
        SELECT("선택"),
        LADDER_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        LADDER_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        LADDER_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        LADDER_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }

    //내피생성 대중소
    public enum ENDOTHELIUM_LMS{
        SELECT("선택"),
        ENDOTHELIUM_L(DAMAGE_TYPE.LARGE.type),
        ENDOTHELIUM_M(DAMAGE_TYPE.MEDIUM.type),
        ENDOTHELIUM_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        ENDOTHELIUM_LMS(String type) {
            this.type = type;
        }
    }

    //폐유부착 대중소
    public enum WASTE_OILD_LMS{
        SELECT("선택"),
        WASTE_OILD_L(DAMAGE_TYPE.LARGE.type),
        WASTE_OILD_M(DAMAGE_TYPE.MEDIUM.type),
        WASTE_OILD_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        WASTE_OILD_LMS(String type) {
            this.type = type;
        }
    }

    //임시장애물 양호대중소
    public enum TEMP_OBSTACLE_GLMS{
        SELECT("선택"),
        TEMP_OBSTACLE_G(DAMAGE_TYPE.GOOD.type),
        TEMP_OBSTACLE_L(DAMAGE_TYPE.LARGE.type),
        TEMP_OBSTACLE_M(DAMAGE_TYPE.MEDIUM.type),
        TEMP_OBSTACLE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        TEMP_OBSTACLE_GLMS(String type) {
            this.type = type;
        }
    }

    //뿌리침입 대중소
    public enum ROOT_INTRUSION_GLMS{
        SELECT("선택"),
        ROOT_INTRUSION_G(DAMAGE_TYPE.GOOD.type),
        ROOT_INTRUSION_L(DAMAGE_TYPE.LARGE.type),
        ROOT_INTRUSION_M(DAMAGE_TYPE.MEDIUM.type),
        ROOT_INTRUSION_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        ROOT_INTRUSION_GLMS(String type) {
            this.type = type;
        }
    }

    //블록이음부 단차 대중소
    public enum BLOCK_CAP_LMS{
        SELECT("선택"),
        BLOCK_CAP_L(DAMAGE_TYPE.LARGE.type),
        BLOCK_CAP_M(DAMAGE_TYPE.MEDIUM.type),
        BLOCK_CAP_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        BLOCK_CAP_LMS(String type) {
            this.type = type;
        }
    }

    //블록이음부 손상 대중소
    public enum BLOCK_DAMAGE_LMS{
        SELECT("선택"),
        BLOCK_DAMAGE_L(DAMAGE_TYPE.LARGE.type),
        BLOCK_DAMAGE_M(DAMAGE_TYPE.MEDIUM.type),
        BLOCK_DAMAGE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        BLOCK_DAMAGE_LMS(String type) {
            this.type = type;
        }
    }

    //블록이음부 이탈 대중소
    public enum BLOCK_LEAVE_LMS{
        SELECT("선택"),
        BLOCK_LEAVE_L(DAMAGE_TYPE.LARGE.type),
        BLOCK_LEAVE_M(DAMAGE_TYPE.MEDIUM.type),
        BLOCK_LEAVE_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        BLOCK_LEAVE_LMS(String type) {
            this.type = type;
        }
    }

    public enum SURFACE_GAP_LMS{
        SELECT("선택"),
        SURFACE_GAP_L(DAMAGE_TYPE.LARGE.type),
        SURFACE_GAP_M(DAMAGE_TYPE.MEDIUM.type),
        SURFACE_GAP_S(DAMAGE_TYPE.SMALL.type),
        ;
        String type;
        SURFACE_GAP_LMS(String type) {
            this.type = type;
        }
    }

    /**
     * 매몰여부
     */
    public enum BURIED_YN{
        SELECT("선택"),
        BURIED_Y(YN_TYPE.Y.type),
        BURIED_N(YN_TYPE.N.type),
        ;
        String type;
        BURIED_YN(String type) {
            this.type = type;
        }
    }

    private LID_STANDARD_TYPE[] mLidStandard;
    private MATERIAL_TYPE[] mMaterial;
    private DRAINAGE_TYPE[] mDrainage;
    private SIZE_TYPE[] mSize;

    private LID_DAMAGE_LMS[] mLidDamageLMS;
    private LID_DAMAGE_YN[] mLidDamageYN;
    private LID_CRACK_LMS[] mLidCrackLMS;
    private LID_CRACK_YN[] mLidCrackYN;
    private LID_WATER_INNER_LMS[] mLidWaterInnerLMS;

    private OUTER_DAMAGE_LMS[] mOuterDamageLMS;
    private OUTER_DAMAGE_YN[] mOuterDamageYN;
    private OUTER_CRACK_LMS[] mOuterCrackLMS;
    private OUTER_CRACK_YN[] mOuterCrackYN;
    private OUTER_WATER_INNER_YN[] mOuterWaterInnerYN;
    private OUTER_WATER_INVASION_LMS[] mOuterWaterInvasionLMS;

    private INNER_DAMAGE_LMS[] mInnerDamageLMS;
    private INNER_DAMAGE_YN[] mInnerDamageYN;
    private INNER_CRACK_LMS[] mInnerCrackLMS;
    private INNER_CRACK_YN[] mInnerCrackYN;
    private INNER_WATER_INVASION_LMS[] mInnerWaterInvasionLMS;

    private PIPE_DAMAGE_LMS[] mPipeDamageLMS;
    private PIPE_DAMAGE_YN[] mPipeDamageYN;
    private PIPE_CRACK_LMS[] mPipeCrackLMS;
    private PIPE_CRACK_YN[] mPipeCrackYN;
    private PIPE_WATER_INVASION_LMS[] mPipeWaterInvasionLMS;

    private LADDER_YN[] mLadderYN;
    private INVERT_YN[] mInvertYN;
    private ODOR_GLMS[] mOdorGLMS;
    private LID_SEALING_YN[] mLidSealingYN;

    private LADDER_DAMAGE_LMS[] mLadderDamageLMS;
    private ENDOTHELIUM_LMS[] mEndotheliumLMS;
    private WASTE_OILD_LMS[] mWasteOilLMS;
    private TEMP_OBSTACLE_GLMS[] mTempObstacleGLMS;
    private ROOT_INTRUSION_GLMS[] mRootIntrusionGLMS;

    private BLOCK_CAP_LMS[] mBlockCapLMS;
    private BLOCK_DAMAGE_LMS[] mBlockDamageLMS;
    private BLOCK_LEAVE_LMS[] mBlockLeaveLMS;
    private SURFACE_GAP_LMS[] mSurfaceGapLMS;
    private BURIED_YN[] mBuriedYN;



    /*
        var material = arrayOf("선택", MaterialType.A, MaterialType.B, MaterialType.C, "직접입력")//재질
    private var frameDamage = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//맨홀뚜껑/프레임 손상 (소/중/대)
    private var surfaceDamageIn = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//표면손상(내부)
    private var surfaceDamageOut = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//표면손상(외부)
    private var damageIN = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//파손(내부)
    private var damageOUT = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//파손(외부)
    private var crackHorizon = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//균열(수평)
    private var crackVertical = arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//균열(수직)
    private var surfaceLevel=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//표면단차
    private var transform=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//변형
    private var sewerPipeProtrusion=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//하수구관로 접속부(돌출)
    private var sewerPipeAnomaly=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//하수구관로 접속부(이상)
    private var invertFault=arrayOf("선택", DamageType.B, DamageType.S)//인버트 결함
    private var odor=arrayOf("선택", "O","X")//악취발생
    private var temporaryObstacle=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//임시장애물
    private var ladderDamage=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//사다리손상
    private var rootIntrusion=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//뿌리 침입
    private var waterInvasion=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)//침입수
    private var lidSealing=arrayOf("선택", "O","X")//뚜껑밀폐
    private var blockDamage=arrayOf("선택", DamageType.B, DamageType.M, DamageType.S)
     */

    public SpinnerManager() {

    }

    public static SpinnerManager I(){
        if(instance == null) {
            instance = new SpinnerManager();
        }
        return instance;
    }

    public static void D(){
        if(null != instance){
            instance.onDestroy();
            instance = null;
        }
    }

    public void init(Activity activity){
        mActivity = activity;
        mLidStandard = LID_STANDARD_TYPE.values();
        mMaterial = MATERIAL_TYPE.values();

        mDrainage = DRAINAGE_TYPE.values();
        mSize = SIZE_TYPE.values();

        mLidDamageLMS = LID_DAMAGE_LMS.values();
        mLidDamageYN = LID_DAMAGE_YN.values();
        mLidCrackLMS = LID_CRACK_LMS.values();
        mLidCrackYN = LID_CRACK_YN.values();
        mLidWaterInnerLMS = LID_WATER_INNER_LMS.values();

        mOuterDamageLMS = OUTER_DAMAGE_LMS.values();
        mOuterDamageYN = OUTER_DAMAGE_YN.values();
        mOuterCrackLMS = OUTER_CRACK_LMS.values();
        mOuterCrackYN = OUTER_CRACK_YN.values();
        mOuterWaterInnerYN = OUTER_WATER_INNER_YN.values();
        mOuterWaterInvasionLMS = OUTER_WATER_INVASION_LMS.values();

        mInnerDamageLMS = INNER_DAMAGE_LMS.values();
        mInnerDamageYN = INNER_DAMAGE_YN.values();
        mInnerCrackLMS = INNER_CRACK_LMS.values();
        mInnerCrackYN = INNER_CRACK_YN.values();
        mInnerWaterInvasionLMS = INNER_WATER_INVASION_LMS.values();

        mPipeDamageLMS = PIPE_DAMAGE_LMS.values();
        mPipeDamageYN = PIPE_DAMAGE_YN.values();
        mPipeCrackLMS = PIPE_CRACK_LMS.values();
        mPipeCrackYN = PIPE_CRACK_YN.values();
        mPipeWaterInvasionLMS = PIPE_WATER_INVASION_LMS.values();

        mLadderYN = LADDER_YN.values();
        mInvertYN = INVERT_YN.values();
        mOdorGLMS = ODOR_GLMS.values();
        mLidSealingYN = LID_SEALING_YN.values();

        mLadderDamageLMS = LADDER_DAMAGE_LMS.values();
        mEndotheliumLMS = ENDOTHELIUM_LMS.values();
        mWasteOilLMS = WASTE_OILD_LMS.values();
        mTempObstacleGLMS = TEMP_OBSTACLE_GLMS.values();
        mRootIntrusionGLMS = ROOT_INTRUSION_GLMS.values();

        mBlockCapLMS = BLOCK_CAP_LMS.values();
        mBlockDamageLMS = BLOCK_DAMAGE_LMS.values();
        mBlockLeaveLMS = BLOCK_LEAVE_LMS.values();
        mSurfaceGapLMS = SURFACE_GAP_LMS.values();
        mBuriedYN = BURIED_YN.values();
    }

    /**
     *
     * @param activity
     * @param spinner
     * @param inputType
     */
    public void createPopup(Activity activity, Spinner spinner, INPUT_TYPE inputType){

        mSpinnerMap.put(inputType.name(), spinner);

        String[] tempArray = getSpinnerData(inputType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.item_spinner, tempArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mAdapterMap.put(inputType.name(), adapter);

    }

    public void createSelectListener(){
        for (Map.Entry<String, Spinner> entry : mSpinnerMap.entrySet()) {
            String key = entry.getKey();
            Spinner spinner = entry.getValue();

            String[] spinnerData = getSpinnerData(INPUT_TYPE.valueOf(key));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Handle the selected item here

                    String selectedData = parent.getItemAtPosition(position).toString();

                    if(StringUtil.isEmpty(selectedData) || " ".equalsIgnoreCase(selectedData)){
                        selectedData = "선택";
                    }

                    GomsLog.d(TAG, "selectedData : " + selectedData);

                    if ("직접입력".equalsIgnoreCase(selectedData)) {
                        //직접입력이면 팝업띄우기
                        goDirectInputDialog(mActivity, "직접입력", "직접입력 내용을 넣어주세요", view);
                    }

                    //맨홀뚜껑 파손 여부에 따른 등급 보이기 여부
                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.LidDamageYN.name())).getSelectedItem())) {
                        Spinner spinnerLidDamage = mSpinnerMap.get(INPUT_TYPE.LidDamageLMS.name());
                        if (spinnerLidDamage != null) {
                            spinnerLidDamage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerLidDamage = mSpinnerMap.get(INPUT_TYPE.LidDamageLMS.name());
                        if (spinnerLidDamage != null) {
                            spinnerLidDamage.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.LidDamageLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.LidCrackYN.name())).getSelectedItem())) {
                        Spinner spinnerLidCrack = mSpinnerMap.get(INPUT_TYPE.LidCrackLMS.name());
                        if (spinnerLidCrack != null) {
                            spinnerLidCrack.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerLidCrack = mSpinnerMap.get(INPUT_TYPE.LidCrackLMS.name());
                        if (spinnerLidCrack != null) {
                            spinnerLidCrack.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.LidCrackLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.OuterDamageYN.name())).getSelectedItem())) {
                        Spinner spinnerOuterDamage = mSpinnerMap.get(INPUT_TYPE.OuterDamageLMS.name());
                        if (spinnerOuterDamage != null) {
                            spinnerOuterDamage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerOuterDamage = mSpinnerMap.get(INPUT_TYPE.OuterDamageLMS.name());
                        if (spinnerOuterDamage != null) {
                            spinnerOuterDamage.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.OuterDamageLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.OuterCrackYN.name())).getSelectedItem())) {
                        Spinner spinnerOuterCrack = mSpinnerMap.get(INPUT_TYPE.OuterCrackLMS.name());
                        if (spinnerOuterCrack != null) {
                            spinnerOuterCrack.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerOuterCrack = mSpinnerMap.get(INPUT_TYPE.OuterCrackLMS.name());
                        if (spinnerOuterCrack != null) {
                            spinnerOuterCrack.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.OuterCrackLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.InnerDamageYN.name())).getSelectedItem())) {
                        Spinner spinnerInnerDamage = mSpinnerMap.get(INPUT_TYPE.InnerDamageLMS.name());
                        if (spinnerInnerDamage != null) {
                            spinnerInnerDamage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerInnerDamage = mSpinnerMap.get(INPUT_TYPE.InnerDamageLMS.name());
                        if (spinnerInnerDamage != null) {
                            spinnerInnerDamage.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.InnerDamageLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.InnerCrackYN.name())).getSelectedItem())) {
                        Spinner spinnerInnerCrack = mSpinnerMap.get(INPUT_TYPE.InnerCrackLMS.name());
                        if (spinnerInnerCrack != null) {
                            spinnerInnerCrack.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerInnerCrack = mSpinnerMap.get(INPUT_TYPE.InnerCrackLMS.name());
                        if (spinnerInnerCrack != null) {
                            spinnerInnerCrack.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.InnerCrackLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.PipeDamageYN.name())).getSelectedItem())) {
                        Spinner spinnerInnerDamage = mSpinnerMap.get(INPUT_TYPE.PipeDamageLMS.name());
                        if (spinnerInnerDamage != null) {
                            spinnerInnerDamage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerInnerDamage = mSpinnerMap.get(INPUT_TYPE.PipeDamageLMS.name());
                        if (spinnerInnerDamage != null) {
                            spinnerInnerDamage.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.PipeDamageLMS);
                        }
                    }

                    if ("O".equals(Objects.requireNonNull(mSpinnerMap.get(INPUT_TYPE.PipeCrackYN.name())).getSelectedItem())) {
                        Spinner spinnerPipeCrack = mSpinnerMap.get(INPUT_TYPE.PipeCrackLMS.name());
                        if (spinnerPipeCrack != null) {
                            spinnerPipeCrack.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Spinner spinnerPipeCrack = mSpinnerMap.get(INPUT_TYPE.PipeCrackLMS.name());
                        if (spinnerPipeCrack != null) {
                            spinnerPipeCrack.setVisibility(View.GONE);
                            setSpinnerInitData(INPUT_TYPE.PipeCrackLMS);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Handle case where nothing is selected
                    GomsLog.d(TAG, ">>>>>>>>>>>>>>>>>>> onNothingSelected");
                }
            });
        }
    }

    public String[] getSpinnerData(INPUT_TYPE inputType){

        if(inputType.ordinal() == INPUT_TYPE.LidStandard.ordinal()){
            int size = mLidStandard.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidStandard[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.Material.ordinal()){
            int size = mMaterial.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mMaterial[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.Drainage.ordinal()){
            int size = mDrainage.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mDrainage[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.Size.ordinal()){
            int size = mSize.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mSize[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidDamageLMS.ordinal()){
            int size = mLidDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidDamageYN.ordinal()){
            int size = mLidDamageYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidDamageYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidCrackLMS.ordinal()){
            int size = mLidCrackLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidCrackLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidCrackYN.ordinal()){
            int size = mLidCrackYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidCrackYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidWaterInnerLMS.ordinal()){
            int size = mLidWaterInnerLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidWaterInnerLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OuterDamageLMS.ordinal()){
            int size = mOuterDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOuterDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OuterDamageYN.ordinal()){
            int size = mOuterDamageYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOuterDamageYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OuterCrackLMS.ordinal()){
            int size = mOuterCrackLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOuterCrackLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OuterCrackYN.ordinal()){
            int size = mOuterCrackYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOuterCrackYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OuterWaterInvasionLMS.ordinal()){
            int size = mOuterWaterInvasionLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOuterWaterInvasionLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InnerDamageLMS.ordinal()){
            int size = mInnerDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInnerDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InnerDamageYN.ordinal()){
            int size = mInnerDamageYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInnerDamageYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InnerCrackLMS.ordinal()){
            int size = mInnerCrackLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInnerCrackLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InnerCrackYN.ordinal()){
            int size = mInnerCrackYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInnerCrackYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InnerWaterInvasionLMS.ordinal()){
            int size = mInnerWaterInvasionLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInnerWaterInvasionLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.PipeDamageLMS.ordinal()){
            int size = mPipeDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mPipeDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.PipeDamageYN.ordinal()){
            int size = mPipeDamageYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mPipeDamageYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.PipeCrackLMS.ordinal()){
            int size = mPipeCrackLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mPipeCrackLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.PipeCrackYN.ordinal()){
            int size = mPipeCrackYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mPipeCrackYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.PipeWaterInvasionLMS.ordinal()){
            int size = mPipeWaterInvasionLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mPipeWaterInvasionLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LadderYN.ordinal()){
            int size = mLadderYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLadderYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.InvertYN.ordinal()){
            int size = mInvertYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mInvertYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.OdorGLMS.ordinal()){
            int size = mOdorGLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mOdorGLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LidSealingYN.ordinal()){
            int size = mLidSealingYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLidCrackYN[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.LadderDamageLMS.ordinal()){
            int size = mLadderDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mLadderDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.EndotheliumLMS.ordinal()){
            int size = mEndotheliumLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mEndotheliumLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.WasteOilLMS.ordinal()){
            int size = mWasteOilLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mWasteOilLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.TempObstacleGLMS.ordinal()){
            int size = mTempObstacleGLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mTempObstacleGLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.RootIntrusionGLMS.ordinal()){
            int size = mRootIntrusionGLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mRootIntrusionGLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.BlockGapLMS.ordinal()){
            int size = mBlockCapLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mBlockCapLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.BlockDamageLMS.ordinal()){
            int size = mBlockDamageLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mBlockDamageLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.BlockLeaveLMS.ordinal()){
            int size = mBlockLeaveLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mBlockLeaveLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.SurfaceGapLMS.ordinal()){
            int size = mSurfaceGapLMS.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mSurfaceGapLMS[i].type;
            }
            return array;
        }else if(inputType.ordinal() == INPUT_TYPE.BuriedYN.ordinal()){
            int size = mBuriedYN.length;
            String[] array = new String[size];
            for (int i = 0; i < size; i++) {
                array[i] = mBuriedYN[i].type;
            }
            return array;
        }else{
            return null;
        }
    }

    /**
     * 스피너에서 직접입력 항목 선택을 했을 시, 띄우는 하단팝업입니다.
     * @param activity
     * @param title
     * @param msg
     */
    private void goDirectInputDialog(Activity activity, String title, String msg, View view){

        DialogManager.I().setTitle(title)
                .setMessage(msg)
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName("확인")
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setCommand(DialogCommandFactory.I().createDialogCommand(activity, DialogCommandFactory.DIALOG_TYPE.input_form.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseBean) {
                        String btnType = ((Bundle) baseBean.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if (BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)) {
                            Log.d(TAG, " 클릭 >>>> 왼쪽");
                        } else if (BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)) {

                            String inputData = ((Bundle)baseBean.getObject()).getString(InputFormBottomDialogCommand.EXT_INPUT);
                            Log.d(TAG, " 클릭 >>>> 오른쪽, " + inputData);

                            if(StringUtil.isNotNull(inputData)){
                                ((TextView)view).setText(inputData);
                            }
                        }
                    }
                }))
                .showDialog(activity);
    }

    public HashMap<String, Spinner> getSpinnerMap() {
        return mSpinnerMap;
    }

    public String getSelectedSpinnerData(INPUT_TYPE inputType){
        String selectedItem = Objects.requireNonNull(mSpinnerMap.get(inputType.name()).getSelectedItem()).toString();
        return selectedItem;
    }

    /**
     * 기본 스피너 데이타 세팅하기. 직접 입력 시, 입력값 항목 추가
     * @param inputType
     * @param selectedData
     */
    public void setSpinnerData(INPUT_TYPE inputType, String selectedData){
        Spinner spinner = mSpinnerMap.get(inputType.name());
        String[] spinnerData = getSpinnerData(inputType);

        if(StringUtil.isEmpty(selectedData) || " ".equalsIgnoreCase(selectedData)){
            selectedData = "선택";
        }
        boolean isContainsString = Arrays.asList(spinnerData).contains(selectedData);

        if(!isContainsString){

            String[] newArray = new String[spinnerData.length + 1];
            for (int i = 0; i < spinnerData.length; i++) {
                newArray[i] = spinnerData[i];
            }
            int lastPosition = newArray.length - 1;
            newArray[lastPosition] = selectedData;

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.item_spinner, newArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            mAdapterMap.put(inputType.name(), adapter);

            spinner.setSelection(lastPosition);

        }else {
            int i = 0;
            for (String data : spinnerData) {
                if (selectedData.equalsIgnoreCase(data)) {
                    spinner.setSelection(i);
                }
                i++;
            }
        }
    }

    /**
     * 초기 선택 처리
     * @param inputType
     */
    public void setSpinnerInitData(INPUT_TYPE inputType){
        Spinner spinner = mSpinnerMap.get(inputType.name());
        String[] spinnerData = getSpinnerData(inputType);
        spinner.setSelection(0);
    }

    public String convertData(String originalData){
        if("Y".equalsIgnoreCase(originalData)){
            return "O";
        }else if("N".equalsIgnoreCase(originalData)){
            return "X";
        }else if("G".equalsIgnoreCase(originalData)){
            return "양호";
        }else if("L".equalsIgnoreCase(originalData)){
            return "대";
        }else if("M".equalsIgnoreCase(originalData)){
            return "중";
        }else if("S".equalsIgnoreCase(originalData)){
            return "소";
        }
        return originalData;
    }

    public void onDestroy(){

    }

}
