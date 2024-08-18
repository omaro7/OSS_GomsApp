package kr.co.goms.app.oss.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kr.co.goms.aar.photoedit.EditActivity;
import kr.co.goms.aar.photoedit.PhotoEditModule;
import kr.co.goms.aar.photoedit.model.CadBean;
import kr.co.goms.app.oss.AppConstant;
import kr.co.goms.app.oss.MainActivity;
import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.manager.ExcelManager;
import kr.co.goms.app.oss.manager.GsonManager;
import kr.co.goms.app.oss.manager.SpinnerManager;
import kr.co.goms.app.oss.model.FieldDetailBeanS;
import kr.co.goms.app.oss.model.PreloadBean;
import kr.co.goms.module.common.activity.CustomActivity;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;
import kr.co.goms.module.common.command.BaseBottomDialogCommand;
import kr.co.goms.module.common.command.InputFormBottomDialogCommand;
import kr.co.goms.module.common.command.SearchListBottomDialogCommand;
import kr.co.goms.module.common.curvlet.CurvletManager;
import kr.co.goms.module.common.manager.DialogCommandFactory;
import kr.co.goms.module.common.manager.DialogManager;
import kr.co.goms.module.common.model.SearchListBean;
import kr.co.goms.module.common.observer.ObserverInterface;
import kr.co.goms.module.common.util.DateUtil;
import kr.co.goms.module.common.util.FileUtil;
import kr.co.goms.module.common.util.GPSTracker;
import kr.co.goms.module.common.util.GomsLog;
import kr.co.goms.module.common.util.GomsUtils;
import kr.co.goms.module.common.util.StringUtil;

public class FieldDetailFormFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = FieldDetailFormFragment.class.getSimpleName();

    private Toolbar mToolbar;
    private Context mContext;

    private String mFieldIdx = "";
    private String mFieldTitle = "";

    private FieldDetailBeanS mFieldDetailBeanS;    //생성 후 수정할 때
    private EditText mEtMhNum, mEtMhDepth, mEtMhInflow, mEtMhOutflow, mEtMhRemark, mEtMhLocalSp, mEtMhLocalEp, mEtMhLocalSpecies, mEtMhLocalCircumference;
    private EditText mEtMhLocalExtension, mEtMhLocalBigo;
    private TextView mTvMhDate, mTvMhCoordinate, mTvMhCoordinateAddress;
    private LinearLayout mLltPhotoAround, mLltPhotoOuter, mLltPhotoInner, mLltPhotoEtc, mLltCad;
    private ImageView mIvPhotoAround, mIvPhotoOuter, mIvPhotoInner, mIvPhotoEtc, mIvCad;
    private LottieAnimationView mLavPhotoAround, mLavPhotoOuter, mLavPhotoInner, mLavPhotoEtc, mLavCad;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private PHOTO_SELECT_TYPE mPhotoSelectType = PHOTO_SELECT_TYPE.CAMERA;
    private enum PHOTO_SELECT_TYPE{
        CAMERA,
        ALBUM,
    }

    private DIALOG_TYPE mDialogType = DIALOG_TYPE.GROUP;
    private enum DIALOG_TYPE{
        GROUP,
        AROUND,
        OUTER,
        INNER,
        ETC,
        CAD,
    }

    private Spinner mSpLidStandard,mSpMaterial,mSpDrainage,mSpSize;

    private Uri mPhotoAroundUri;
    private Uri mPhotoOuterUri;
    private Uri mPhotoInnerUri;
    private Uri mPhotoEtcUri;
    private Uri mCadUri;

    private HashMap<String, String> mPreloadDataHashMap = new HashMap<>();

    private FORM_TYPE mFormType = FORM_TYPE.CREATE;
    private enum FORM_TYPE{
        CREATE, //생성
        MODIFY,   //수정
    }

    private GPSHandler mGpsHandler;
    private GPSTracker mGpsTracker;

    public FieldDetailFormFragment(){}

    public static FieldDetailFormFragment getFragment(String fieldIdx, String fieldTitle){
        FieldDetailFormFragment fragment = new FieldDetailFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fieldIdx", fieldIdx);
        bundle.putString("fieldTitle", fieldTitle);
        bundle.putString("formType", FORM_TYPE.CREATE.name());
        fragment.setArguments(bundle);
        return fragment;
    }

    public static FieldDetailFormFragment getFragment(String fieldIdx, String fieldTitle, FieldDetailBeanS fieldDetailBeanS){
        FieldDetailFormFragment fragment = new FieldDetailFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fieldIdx", fieldIdx);
        bundle.putString("fieldTitle", fieldTitle);
        bundle.putParcelable("fieldDetailBeanS", fieldDetailBeanS);
        bundle.putString("formType", FORM_TYPE.MODIFY.name());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        //mGpsHandler = new GPSHandler();
        //mGpsTracker = new GPSTracker(getActivity(), mGpsHandler);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_field_detail_form, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFieldIdx = getArguments().getString("fieldIdx");
        mFieldTitle = getArguments().getString("fieldTitle");
        mFieldDetailBeanS = getArguments().getParcelable("fieldDetailBeanS");

        if(StringUtil.isEmpty(mFieldIdx)){
            CurvletManager.process(getActivity(), null, "water://toast?text=기본데이타 선택 후, 진입 가능합니다.");
            return;
        }

        String formType = getArguments().getString("formType");
        if(FORM_TYPE.CREATE.name().equalsIgnoreCase(formType)){
            mFormType = FORM_TYPE.CREATE;
        }else{
            //MODY 라고 해도, mFieldDetailIdx가 없으면 CREATE 라고 판단함.
            if(StringUtil.isNotNull(mFieldDetailBeanS.getRes_mh_field_d_idx()) && StringUtil.stringToInt(mFieldDetailBeanS.getRes_mh_field_d_idx()) > 0) {
                mFormType = FORM_TYPE.MODIFY;
            }
        }

        GomsLog.d(LOG_TAG, "mFormType : " + mFormType.name());

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView tvToolBarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolBarTitle.setText(mFieldTitle);

        setInitUI(view);
        mOnNextProcess = new OnNextProcess() {
            @Override
            public void onNext() {
                SpinnerManager.I().createSelectListener();
            }
        };

        if(FORM_TYPE.CREATE.name().equalsIgnoreCase(mFormType.name())) {
            checkPreloadData(); //사전데이타 넣기
            setBasicData();     //기본데이타 넣기
        }else{
            checkModifyData();
        }

        this.setHasOptionsMenu(true);

    }

    private void setInitUI(View view){

        Button btnSave = view.findViewById(R.id.btn_save);

        if(FORM_TYPE.CREATE.name().equalsIgnoreCase(mFormType.name())) {
            btnSave.setText("저장");
        }else{
            btnSave.setText("수정");
        }

        mSpLidStandard = view.findViewById(R.id.sp_lid_standard);
        mSpMaterial = view.findViewById(R.id.sp_mh_material);
        mSpDrainage = view.findViewById(R.id.sp_mh_drainage);
        mSpSize = view.findViewById(R.id.sp_mh_size);
        Spinner spLidDamageLMS = view.findViewById(R.id.sp_mh_lid_damage_lms);
        Spinner spLidDamageYN = view.findViewById(R.id.sp_mh_lid_damage_yn);
        Spinner spLidCrackLMS = view.findViewById(R.id.sp_mh_lid_crack_lms);
        Spinner spLidCrackYN = view.findViewById(R.id.sp_mh_lid_crack_yn);

        Spinner spLidWaterInnerLMS = view.findViewById(R.id.sp_mh_lid_water_inner_lms);               // 뚜껑유입수 LMS

        Spinner spOuterDamageLMS = view.findViewById(R.id.sp_mh_outer_damage_lms);
        Spinner spOuterDamageYN = view.findViewById(R.id.sp_mh_outer_damage_yn);
        Spinner spOuterCrackLMS = view.findViewById(R.id.sp_mh_outer_crack_lms);
        Spinner spOuterCrackYN = view.findViewById(R.id.sp_mh_outer_crack_yn);
        //Spinner spOuterWaterInnerYN = view.findViewById(R.id.sp_mh_outer_water_inner_yn);           //뚜껑주변유입수 Y/N
        Spinner spOuterWaterInvasionLMS = view.findViewById(R.id.sp_mh_outer_water_invasion_lms);   //뚜껑침입수 LMS
        Spinner spInnerDamageLMS = view.findViewById(R.id.sp_mh_inner_damage_lms);
        Spinner spInnerDamageYN = view.findViewById(R.id.sp_mh_inner_damage_yn);
        Spinner spInnerCrackLMS = view.findViewById(R.id.sp_mh_inner_crack_lms);
        Spinner spInnerCrackYN = view.findViewById(R.id.sp_mh_inner_crack_yn);
        Spinner spInnerWaterInvasionLMS = view.findViewById(R.id.sp_mh_inner_water_invasion_lms);
        Spinner spPipeDamageLMS = view.findViewById(R.id.sp_mh_pipe_damage_lms);
        Spinner spPipeDamageYN = view.findViewById(R.id.sp_mh_pipe_damage_yn);
        Spinner spPipeCrackLMS = view.findViewById(R.id.sp_mh_pipe_crack_lms);
        Spinner spPipeCrackYN = view.findViewById(R.id.sp_mh_pipe_crack_yn);
        Spinner spPipeWaterInvasionLMS = view.findViewById(R.id.sp_mh_pipe_water_invasion_lms);

        Spinner spLadderYN = view.findViewById(R.id.sp_mh_ladder_yn);
        Spinner spInvertYN = view.findViewById(R.id.sp_mh_invert_yn);
        Spinner spOdorGLMS = view.findViewById(R.id.sp_mh_odor_glms);
        Spinner spLidSealingYN = view.findViewById(R.id.sp_mh_lid_sealing_yn);
        Spinner spLadderDamageLMS = view.findViewById(R.id.sp_mh_ladder_damage_lms);
        Spinner spEndotheliumLMS = view.findViewById(R.id.sp_mh_endothelium_lms);
        Spinner spWasteoilLMS = view.findViewById(R.id.sp_mh_wasteoil_lms);
        Spinner spTempObstacleGLMS = view.findViewById(R.id.sp_mh_temp_obstacle_glms);
        Spinner spRootIntrusionGLMS = view.findViewById(R.id.sp_mh_root_intrusion_glms);

        Spinner spBlockGapLMS = view.findViewById(R.id.sp_mh_block_gap_lms);
        Spinner spBlockDamageLMS = view.findViewById(R.id.sp_mh_block_damage_lms);
        Spinner spBlockLeaveLMS = view.findViewById(R.id.sp_mh_block_leave_lms);
        Spinner spSurfaceGapLMS = view.findViewById(R.id.sp_mh_surface_gap_lms);
        Spinner spBurideYN = view.findViewById(R.id.sp_mh_buried_yn);

        SpinnerManager.I().init(requireActivity());
        SpinnerManager.I().createPopup(getActivity(), mSpLidStandard, SpinnerManager.INPUT_TYPE.LidStandard);
        SpinnerManager.I().createPopup(getActivity(), mSpMaterial, SpinnerManager.INPUT_TYPE.Material);
        SpinnerManager.I().createPopup(getActivity(), mSpDrainage, SpinnerManager.INPUT_TYPE.Drainage);
        SpinnerManager.I().createPopup(getActivity(), mSpSize, SpinnerManager.INPUT_TYPE.Size);
        SpinnerManager.I().createPopup(getActivity(), spLidDamageLMS, SpinnerManager.INPUT_TYPE.LidDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spLidDamageYN, SpinnerManager.INPUT_TYPE.LidDamageYN);
        SpinnerManager.I().createPopup(getActivity(), spLidCrackLMS, SpinnerManager.INPUT_TYPE.LidCrackLMS);
        SpinnerManager.I().createPopup(getActivity(), spLidCrackYN, SpinnerManager.INPUT_TYPE.LidCrackYN);
        SpinnerManager.I().createPopup(getActivity(), spLidWaterInnerLMS, SpinnerManager.INPUT_TYPE.LidWaterInnerLMS);
        SpinnerManager.I().createPopup(getActivity(), spOuterDamageLMS, SpinnerManager.INPUT_TYPE.OuterDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spOuterDamageYN, SpinnerManager.INPUT_TYPE.OuterDamageYN);
        SpinnerManager.I().createPopup(getActivity(), spOuterCrackLMS, SpinnerManager.INPUT_TYPE.OuterCrackLMS);
        SpinnerManager.I().createPopup(getActivity(), spOuterCrackYN, SpinnerManager.INPUT_TYPE.OuterCrackYN);
        SpinnerManager.I().createPopup(getActivity(), spOuterWaterInvasionLMS, SpinnerManager.INPUT_TYPE.OuterWaterInvasionLMS);
        SpinnerManager.I().createPopup(getActivity(), spInnerDamageLMS, SpinnerManager.INPUT_TYPE.InnerDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spInnerDamageYN, SpinnerManager.INPUT_TYPE.InnerDamageYN);
        SpinnerManager.I().createPopup(getActivity(), spInnerCrackLMS, SpinnerManager.INPUT_TYPE.InnerCrackLMS);
        SpinnerManager.I().createPopup(getActivity(), spInnerCrackYN, SpinnerManager.INPUT_TYPE.InnerCrackYN);
        SpinnerManager.I().createPopup(getActivity(), spInnerWaterInvasionLMS, SpinnerManager.INPUT_TYPE.InnerWaterInvasionLMS);
        SpinnerManager.I().createPopup(getActivity(), spPipeDamageLMS, SpinnerManager.INPUT_TYPE.PipeDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spPipeDamageYN, SpinnerManager.INPUT_TYPE.PipeDamageYN);
        SpinnerManager.I().createPopup(getActivity(), spPipeCrackLMS, SpinnerManager.INPUT_TYPE.PipeCrackLMS);
        SpinnerManager.I().createPopup(getActivity(), spPipeCrackYN, SpinnerManager.INPUT_TYPE.PipeCrackYN);
        SpinnerManager.I().createPopup(getActivity(), spPipeWaterInvasionLMS, SpinnerManager.INPUT_TYPE.PipeWaterInvasionLMS);
        SpinnerManager.I().createPopup(getActivity(), spLadderYN, SpinnerManager.INPUT_TYPE.LadderYN);
        SpinnerManager.I().createPopup(getActivity(), spInvertYN, SpinnerManager.INPUT_TYPE.InvertYN);
        SpinnerManager.I().createPopup(getActivity(), spOdorGLMS, SpinnerManager.INPUT_TYPE.OdorGLMS);
        SpinnerManager.I().createPopup(getActivity(), spLidSealingYN, SpinnerManager.INPUT_TYPE.LidSealingYN);
        SpinnerManager.I().createPopup(getActivity(), spLadderDamageLMS, SpinnerManager.INPUT_TYPE.LadderDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spEndotheliumLMS, SpinnerManager.INPUT_TYPE.EndotheliumLMS);
        SpinnerManager.I().createPopup(getActivity(), spWasteoilLMS, SpinnerManager.INPUT_TYPE.WasteOilLMS);
        SpinnerManager.I().createPopup(getActivity(), spTempObstacleGLMS, SpinnerManager.INPUT_TYPE.TempObstacleGLMS);
        SpinnerManager.I().createPopup(getActivity(), spRootIntrusionGLMS, SpinnerManager.INPUT_TYPE.RootIntrusionGLMS);
        SpinnerManager.I().createPopup(getActivity(), spBlockGapLMS, SpinnerManager.INPUT_TYPE.BlockGapLMS);
        SpinnerManager.I().createPopup(getActivity(), spBlockDamageLMS, SpinnerManager.INPUT_TYPE.BlockDamageLMS);
        SpinnerManager.I().createPopup(getActivity(), spBlockLeaveLMS, SpinnerManager.INPUT_TYPE.BlockLeaveLMS);
        SpinnerManager.I().createPopup(getActivity(), spSurfaceGapLMS, SpinnerManager.INPUT_TYPE.SurfaceGapLMS);
        SpinnerManager.I().createPopup(getActivity(), spBurideYN, SpinnerManager.INPUT_TYPE.BuriedYN);

        mEtMhNum = view.findViewById(R.id.et_mh_num);               //맨홀번호
        mTvMhDate = view.findViewById(R.id.tv_mh_date);             //조사일시
        mTvMhCoordinate = view.findViewById(R.id.tv_mh_coordinate); //좌표
        mTvMhCoordinateAddress = view.findViewById(R.id.tv_mh_coordinate_address);

        mEtMhDepth = view.findViewById(R.id.et_mh_depth);           //깊이
        mEtMhInflow = view.findViewById(R.id.et_mh_inflow);         //유입관경
        mEtMhOutflow = view.findViewById(R.id.et_mh_outflow);       //유출관경
        mEtMhRemark = view.findViewById(R.id.et_mh_remark);         //특이사항

        mEtMhLocalSp = view.findViewById(R.id.et_mh_local_sp);
        mEtMhLocalEp = view.findViewById(R.id.et_mh_local_ep);
        mEtMhLocalSpecies = view.findViewById(R.id.et_mh_local_species);
        mEtMhLocalCircumference = view.findViewById(R.id.et_mh_local_circumference);
        mEtMhLocalExtension = view.findViewById(R.id.et_mh_local_extension);
        mEtMhLocalBigo = view.findViewById(R.id.et_mh_local_bigo);

        mLltPhotoAround = view.findViewById(R.id.llt_photo_around);
        mLltPhotoOuter = view.findViewById(R.id.llt_photo_outer);
        mLltPhotoInner = view.findViewById(R.id.llt_photo_inner);
        mLltPhotoEtc = view.findViewById(R.id.llt_photo_etc);
        mLltCad = view.findViewById(R.id.llt_cad);

        mIvPhotoAround = view.findViewById(R.id.iv_mh_photo_around);
        mIvPhotoOuter = view.findViewById(R.id.iv_mh_photo_outer);
        mIvPhotoInner = view.findViewById(R.id.iv_mh_photo_inner);
        mIvPhotoEtc = view.findViewById(R.id.iv_mh_photo_etc);
        mIvCad = view.findViewById(R.id.iv_mh_cad);

        mLavPhotoAround = view.findViewById(R.id.lav_photo_around);
        mLavPhotoOuter = view.findViewById(R.id.lav_photo_outer);
        mLavPhotoInner = view.findViewById(R.id.lav_photo_inner);
        mLavPhotoEtc = view.findViewById(R.id.lav_photo_etc);
        mLavCad = view.findViewById(R.id.lav_cad);
        LinearLayout mCadEdit = view.findViewById(R.id.llt_cad_edit);

        mTvMhDate.setOnClickListener(this);
        mTvMhCoordinate.setOnClickListener(this);
        mLltPhotoAround.setOnClickListener(this);
        mLltPhotoOuter.setOnClickListener(this);
        mLltPhotoInner.setOnClickListener(this);
        mLltPhotoEtc.setOnClickListener(this);
        mLltCad.setOnClickListener(this);
        mCadEdit.setOnClickListener(this);

        btnSave.setOnClickListener(this);
    }

    /**
     * 생성 > 이전데이타
     */
    private void checkPreloadData(){
        String preloadData = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_PRELOAD_DATA, "");
        PreloadBean preloadBean = GsonManager.I().getPreloadJsonData(preloadData, PreloadBean.class);

        String mhNum = "";
        try{
            mhNum = preloadBean.getMH_NUM();
        }catch(NullPointerException e){

        }
        String mhInflow= "";
        try{
            mhInflow = preloadBean.getMH_INFLOW();
        }catch(NullPointerException e){

        }
        String mhOutflow= "";
        try{
            mhOutflow = preloadBean.getMH_OUTFLOW();
        }catch(NullPointerException e){

        }

        mEtMhNum.setText(mhNum);
        mEtMhInflow.setText(mhInflow);
        mEtMhOutflow.setText(mhOutflow);


        String mhStandard= "";
        try{
            mhStandard = preloadBean.getMH_STANDARD();
        }catch(NullPointerException e){

        }
        String mhMaterial= "";
        try{
            mhMaterial = preloadBean.getMH_MATERIAL();
        }catch(NullPointerException e){

        }
        String mhSize= "";
        try{
            mhSize = preloadBean.getMH_SIZE();
        }catch(NullPointerException e){

        }
        String mhDrainage= "";
        try{
            mhDrainage = preloadBean.getMH_DRAINAGE();
        }catch(NullPointerException e){

        }
        String mhLocalSp= "";
        try{
            mhLocalSp = preloadBean.getMH_LOCALSP();
        }catch(NullPointerException e){

        }
        String mhLocalEp= "";
        try{
            mhLocalEp = preloadBean.getMH_LOCALEP();
        }catch(NullPointerException e){

        }
        String mhLocalspecies= "";
        try{
            mhLocalspecies = preloadBean.getMH_LOCALSPECIES();
        }catch(NullPointerException e){

        }
        String mhLocalcircumference= "";
        try{
            mhLocalcircumference = preloadBean.getMH_LOCALCIRCUMFERENCE();
        }catch(NullPointerException e){

        }

        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidStandard, mhStandard);
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Material, mhMaterial);
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Size, mhSize);
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Drainage, mhDrainage);

        mEtMhLocalSp.setText(mhLocalSp);
        mEtMhLocalEp.setText(mhLocalEp);
        mEtMhLocalSpecies.setText(mhLocalspecies);
        mEtMhLocalCircumference.setText(mhLocalcircumference);

        mOnNextProcess.onNext();

    }

    /**
     * 기본 데이타 넣기
     * 임시장애물.뿌리침입.악취항목
     */
    private void setBasicData(){
        String tempObstacle = "양호";
        String rootIntrusion = "양호";
        String odor = "양호";

        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.TempObstacleGLMS, tempObstacle);
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.RootIntrusionGLMS, rootIntrusion);
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OdorGLMS, odor);

    }

    /**
     * 수정모드일 때, 수정 데이타 넣기
     */
    private void checkModifyData(){
        mEtMhNum.setText(mFieldDetailBeanS.getRes_mh_num());
        mTvMhDate.setText(mFieldDetailBeanS.getRes_mh_date());
        mTvMhCoordinate.setText(mFieldDetailBeanS.getRes_mh_coordinate());

        mEtMhDepth.setText(mFieldDetailBeanS.getRes_mh_depth());
        mEtMhInflow.setText(mFieldDetailBeanS.getRes_mh_inflow());
        mEtMhOutflow.setText(mFieldDetailBeanS.getRes_mh_outflow());
        mEtMhRemark.setText(mFieldDetailBeanS.getRes_mh_remark());

        mEtMhLocalSp.setText(mFieldDetailBeanS.getRes_mh_local_sp());
        mEtMhLocalEp.setText(mFieldDetailBeanS.getRes_mh_local_ep());
        mEtMhLocalSpecies.setText(mFieldDetailBeanS.getRes_mh_local_species());
        mEtMhLocalCircumference.setText(mFieldDetailBeanS.getRes_mh_local_circumference());
        mEtMhLocalExtension.setText(mFieldDetailBeanS.getRes_mh_local_extension());
        mEtMhLocalBigo.setText(mFieldDetailBeanS.getRes_mh_local_bigo());

        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidStandard,mFieldDetailBeanS.getRes_mh_standard());
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Material,mFieldDetailBeanS.getRes_mh_material());
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Size,mFieldDetailBeanS.getRes_mh_size());
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.Drainage,mFieldDetailBeanS.getRes_mh_drainage());

        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidDamageLMS,SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_lid_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidDamageYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_lid_damage_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidCrackLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_lid_crack_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidCrackYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_lid_crack_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidWaterInnerLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_water_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OuterDamageLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_outer_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OuterDamageYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_outer_damage_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OuterCrackLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_outer_crack_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OuterCrackYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_outer_crack_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OuterWaterInvasionLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_outer_water_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InnerDamageLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InnerDamageYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_damage_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InnerCrackLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_crack_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InnerCrackYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_crack_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InnerWaterInvasionLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_inner_water_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.PipeDamageLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_pipe_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.PipeDamageYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_pipe_damage_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.PipeCrackLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_pipe_crack_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.PipeCrackYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_pipe_crack_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.PipeWaterInvasionLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_pipe_water_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LadderYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_ladder_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.InvertYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_invert_yn()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.OdorGLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_odor_glms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LidSealingYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_ladder_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.LadderDamageLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_ladder_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.EndotheliumLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_endothelium_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.WasteOilLMS,  SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_wasteoil_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.TempObstacleGLMS,  SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_temp_obstacle_glms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.RootIntrusionGLMS,  SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_root_intrusion_glms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.BlockGapLMS,  SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_block_gap_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.BlockDamageLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_block_damage_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.BlockLeaveLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_block_leave_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.SurfaceGapLMS, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_surface_gap_lms()));
        SpinnerManager.I().setSpinnerData(SpinnerManager.INPUT_TYPE.BuriedYN, SpinnerManager.I().convertData(mFieldDetailBeanS.getRes_mh_buried_yn()));

        if(!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_photo_around())){
            Glide.with(getActivity()).asBitmap().load(mFieldDetailBeanS.getRes_mh_photo_around()).into(mIvPhotoAround);
            mIvPhotoAround.setVisibility(View.VISIBLE);
            mLavPhotoAround.setVisibility(View.GONE);
        }
        if(!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_photo_outer())){
            Glide.with(getActivity()).asBitmap().load(mFieldDetailBeanS.getRes_mh_photo_outer()).into(mIvPhotoOuter);
            mIvPhotoOuter.setVisibility(View.VISIBLE);
            mLavPhotoOuter.setVisibility(View.GONE);
        }
        if(!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_photo_inner())){
            Glide.with(getActivity()).asBitmap().load(mFieldDetailBeanS.getRes_mh_photo_inner()).into(mIvPhotoInner);
            mIvPhotoInner.setVisibility(View.VISIBLE);
            mLavPhotoInner.setVisibility(View.GONE);
        }
        if(!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_photo_etc())){
            Glide.with(getActivity()).asBitmap().load(mFieldDetailBeanS.getRes_mh_photo_etc()).into(mIvPhotoEtc);
            mIvPhotoEtc.setVisibility(View.VISIBLE);
            mLavPhotoEtc.setVisibility(View.GONE);
        }
        if(!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_cad())){
            Glide.with(getActivity()).asBitmap().load(mFieldDetailBeanS.getRes_mh_cad()).into(mIvCad);
            mIvCad.setVisibility(View.VISIBLE);
            mLavCad.setVisibility(View.GONE);
        }

        String coordinate = mFieldDetailBeanS.getRes_mh_coordinate();
        String lat = "";
        String lng = "";
        if (!StringUtil.isEmpty(coordinate)) {
            String[] latlng = coordinate.split(",");
            lat = latlng[0];
            lng = latlng[1];

            String address = GomsUtils.getAddressFromGeo(mContext, StringUtil.stringToDouble(lat), StringUtil.stringToDouble(lng));
            mTvMhCoordinateAddress.setText(address);
        }

        mOnNextProcess.onNext();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            getActivity().onBackPressed();
            //mGpsTracker.stopUsingGPS();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mGpsTracker.stopUsingGPS();
        stopGPS();
    }

    @Override
    public void onStop(){
        super.onStop();
        stopGPS();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mGpsTracker.stopUsingGPS();
        stopGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mGpsTracker.getLocation();
    }

    public void stopGPS(){
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationClient;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            Location location = locationResult.getLastLocation();
            // Handle the real-time location here
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            // Do something with latitude and longitude

            String coordinate = "" + latitude + "," + longitude;
            Log.d(LOG_TAG, "좌표가져오기 : " + coordinate);
            mTvMhCoordinate.setText(coordinate);

            String address = GomsUtils.getAddressFromGeo(mContext, latitude, longitude);
            mTvMhCoordinateAddress.setText(address);
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.tv_mh_date){
            DialogManager.I().showDialogDatePicker(getActivity(), new WaterCallBack() {
                @Override
                public void callback(BaseBean baseBean) {
                    String mhDate = (String)((Bundle)baseBean.getObject()).get(DialogManager.EXT_DATE);
                    mhDate = DateUtil.displayDateYMD8(mhDate);
                    mTvMhDate.setText(mhDate);
                }
            });
        }else if(id == R.id.tv_mh_coordinate){

            //mGpsTracker.Update();

            //getLastKnownLocation();

            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000) // Update interval in milliseconds
                    .setFastestInterval(5000) // Fastest update interval
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);


        }else if(id == R.id.llt_photo_around){
            goDialog(DIALOG_TYPE.AROUND);
        }else if(id == R.id.llt_photo_outer){
            goDialog(DIALOG_TYPE.OUTER);
        }else if(id == R.id.llt_photo_inner){
            goDialog(DIALOG_TYPE.INNER);
        }else if(id == R.id.llt_photo_etc){
            goDialog(DIALOG_TYPE.ETC);
        }else if(id == R.id.llt_cad) {
            goAlbum(DIALOG_TYPE.CAD);
        }else if(id == R.id.llt_cad_edit){
            //goCadMakerDialog(mEtMhNum.getText().toString(), mCadUri);
        }else if(id == R.id.btn_save){
            goSave();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String coordinate = "" + latitude + "," + longitude;
                            Log.d(LOG_TAG, "좌표가져오기 : " + coordinate);
                            mTvMhCoordinate.setText(coordinate);
                        }}
                });

    }

    private boolean checkValue(){
        String fieldMhNum = mEtMhNum.getText().toString();
        String fieldMhDate = mTvMhDate.getText().toString();
        String fieldMhCoordinate = mTvMhCoordinate.getText().toString();
        if(StringUtil.isEmpty(fieldMhNum)){
            CurvletManager.process(getActivity(), null, "water://toast?text=맨홀번호를 넣어주세요");
            return false;
        }
        if(StringUtil.isEmpty(fieldMhDate)){
            CurvletManager.process(getActivity(), null, "water://toast?text=조사일시를 넣어주세요");
            return false;
        }
        if(StringUtil.isEmpty(fieldMhCoordinate)){
            CurvletManager.process(getActivity(), null, "water://toast?text=위치를 넣어주세요");
            return false;
        }
        return true;
    }

    private void goDialog(DIALOG_TYPE dialogType){

        int total = 0;
        String fileKey = "";
        if(FORM_TYPE.CREATE.name().equalsIgnoreCase(mFormType.name())) {
            HashMap<String, File> fileMap = ExcelManager.I(getActivity()).getPhotoFileMap();

            try{
                total = fileMap.size();
            }catch(NullPointerException e){

            }

            if(total > 0) {
                for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                    String key = entry.getKey();
                    if (dialogType.name().equalsIgnoreCase(key)) {
                        fileKey = key;
                        break;
                    }
                }
            }
        }
        if(!StringUtil.isEmpty(fileKey)){
            //선택한 사진이 있습니다. 그럼 삭제 창을 띄웁시다.
            deletePhotoDailog(dialogType, "사진 삭제", "선택한 사진을 삭제하시겠습니까?", fileKey);
        }else{
            registerPhoto(dialogType);
        }
    }

    private void goCamera(DIALOG_TYPE dialogType){

        mDialogType = dialogType;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = FileUtil.createFile(getActivity(), mFieldIdx, Environment.DIRECTORY_PICTURES, dialogType.name().toLowerCase(), ".jpg");

            if (photoFile != null) {
                if(DIALOG_TYPE.AROUND == dialogType) {
                    mPhotoAroundUri = FileProvider.getUriForFile(getActivity(), AppConstant.APP_URI + ".provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoAroundUri);
                    takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                    takePictureIntent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, dialogType.name());
                }else if(DIALOG_TYPE.OUTER == dialogType) {
                    mPhotoOuterUri = FileProvider.getUriForFile(getActivity(), AppConstant.APP_URI + ".provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoOuterUri);
                    takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                }else if(DIALOG_TYPE.INNER == dialogType) {
                    mPhotoInnerUri = FileProvider.getUriForFile(getActivity(), AppConstant.APP_URI + ".provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoInnerUri);
                    takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                }else if(DIALOG_TYPE.ETC == dialogType) {
                    mPhotoEtcUri = FileProvider.getUriForFile(getActivity(), AppConstant.APP_URI + ".provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoEtcUri);
                    takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                }else if(DIALOG_TYPE.CAD == dialogType) {
                    mCadUri = FileProvider.getUriForFile(getActivity(), AppConstant.APP_URI + ".provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCadUri);
                    takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.8);
                }
            }
            ((MainActivity)getActivity()).getCameraLauncher().launch(takePictureIntent);
        }
    }

    private void goAlbum(DIALOG_TYPE dialogType){
        mDialogType = dialogType;
        ((MainActivity)getActivity()).getAlbumLauncher().launch("image/*");
    }


    /**
     *
     * @param photoSelectType 카메라, 앨범
     * @param dialogType    맨홀주변, 맨홀외부, 맨홀내부, 기타, CAD
     * @param imageUri
     */
    private void resizeAndSaveImage(PHOTO_SELECT_TYPE photoSelectType, DIALOG_TYPE dialogType, Uri imageUri) {

        if(DIALOG_TYPE.CAD.name().equalsIgnoreCase(dialogType.name())) {
           //try {

                /*
                Bitmap originalBitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageUri));
                int newWidth = AppConstant.CAD_WIDTH;   // 480 640 1280;
                int newHeight = AppConstant.CAD_HEIGHT; // 320 480 960;
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
                File resizedFile = FileUtil.createFile(getActivity(), mFieldIdx, Environment.DIRECTORY_PICTURES, dialogType.name().toLowerCase(), ".jpg");
                FileOutputStream outputStream = new FileOutputStream(resizedFile);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream);
                outputStream.close();

                ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.CAD, resizedFile);
                ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.CAD, mCadUri);
                mCadUri = Uri.fromFile(resizedFile);
                */

                Glide.with(getActivity())
                        .asBitmap()
                        .load(mCadUri)
                        //.load(mMhCadString)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                File resizedFile = FileUtil.createFile(getActivity(), mFieldIdx, Environment.DIRECTORY_PICTURES, dialogType.name().toLowerCase(), ".jpg");
                                try{
                                    FileOutputStream outputStream = new FileOutputStream(resizedFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                                    outputStream.close();

                                    ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.CAD, resizedFile);
                                    ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.CAD, mCadUri);
                                    mCadUri = Uri.fromFile(resizedFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                GomsLog.d(LOG_TAG, "PhotoEdit >>> onLoadCleared() 파일이 안 열려요");
                            }
                        });

/*            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }else {

            try {
                Bitmap originalBitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageUri));
                int newWidth = AppConstant.PHOTO_WIDTH; // 480 640 1280;
                int newHeight = AppConstant.PHOTO_HEIGHT; // 320 480 960;
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);

                File resizedFile = FileUtil.createFile(getActivity(), mFieldIdx, Environment.DIRECTORY_PICTURES, dialogType.name().toLowerCase(), ".jpg");
                FileOutputStream outputStream = new FileOutputStream(resizedFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();

                if (DIALOG_TYPE.AROUND.name().equalsIgnoreCase(dialogType.name())) {
                    ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.AROUND, resizedFile);
                    mPhotoAroundUri = Uri.fromFile(resizedFile);
                } else if (DIALOG_TYPE.OUTER.name().equalsIgnoreCase(dialogType.name())) {
                    ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.OUTER, resizedFile);
                    mPhotoOuterUri = Uri.fromFile(resizedFile);
                } else if (DIALOG_TYPE.INNER.name().equalsIgnoreCase(dialogType.name())) {
                    ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.INNER, resizedFile);
                    mPhotoInnerUri = Uri.fromFile(resizedFile);
                } else if (DIALOG_TYPE.ETC.name().equalsIgnoreCase(dialogType.name())) {
                    ExcelManager.I(getActivity()).setPhotoFileMap(ExcelManager.PHOTO_TYPE.ETC, resizedFile);
                    mPhotoEtcUri = Uri.fromFile(resizedFile);
                }

                if (PHOTO_SELECT_TYPE.CAMERA == photoSelectType) {
                    FileUtil.deletePhoto(mContext, imageUri);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * MainActivity에서 launcher에서 registerForActivityResult로 수신하여 다시 setPhoto로 보냄
     * setPhoto시, 꼭 현재 DIALOG_TYPE가 선행되어야 함.
     */
    public void setCameraPhoto() {

        GomsLog.d(LOG_TAG, "mDialogType : " + mDialogType);

        if(DIALOG_TYPE.AROUND == mDialogType) {
            //mIvPhotoAround.setImageURI(mPhotoAroundUri);
            displayResizedImage(requireActivity(), mPhotoAroundUri, mIvPhotoAround);
            mIvPhotoAround.setVisibility(View.VISIBLE);
            mLavPhotoAround.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.AROUND, mPhotoAroundUri);

            resizeAndSaveImage(PHOTO_SELECT_TYPE.CAMERA, DIALOG_TYPE.AROUND, mPhotoAroundUri);

        }else if(DIALOG_TYPE.OUTER == mDialogType) {
            //mIvPhotoOuter.setImageURI(mPhotoOuterUri);
            displayResizedImage(requireActivity(), mPhotoOuterUri, mIvPhotoOuter);
            mIvPhotoOuter.setVisibility(View.VISIBLE);
            mLavPhotoOuter.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.OUTER, mPhotoOuterUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.CAMERA, DIALOG_TYPE.OUTER, mPhotoOuterUri);
        }else if(DIALOG_TYPE.INNER == mDialogType) {
            //mIvPhotoInner.setImageURI(mPhotoInnerUri);
            displayResizedImage(requireActivity(), mPhotoInnerUri, mIvPhotoInner);
            mIvPhotoInner.setVisibility(View.VISIBLE);
            mLavPhotoInner.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.INNER, mPhotoInnerUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.CAMERA, DIALOG_TYPE.INNER, mPhotoInnerUri);
        }else if(DIALOG_TYPE.ETC == mDialogType) {
            //mIvPhotoEtc.setImageURI(mPhotoEtcUri);
            displayResizedImage(requireActivity(), mPhotoEtcUri, mIvPhotoEtc);
            mIvPhotoEtc.setVisibility(View.VISIBLE);
            mLavPhotoEtc.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.ETC, mPhotoEtcUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.CAMERA, DIALOG_TYPE.ETC, mPhotoEtcUri);
        }else if(DIALOG_TYPE.CAD == mDialogType) {
            //mIvCad.setImageURI(mCadUri);
            displayResizedImage(requireActivity(), mCadUri, mIvCad);
            mIvCad.setVisibility(View.VISIBLE);
            mLavCad.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.CAD, mCadUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.CAMERA, DIALOG_TYPE.CAD, mCadUri);
        }
    }

    /**
     * 사진대지 > 앨범 선택 후 setAlbumPhoto 처리
     * @param photoUri
     */
    public void setAlbumPhoto(Uri photoUri){
        if(DIALOG_TYPE.AROUND == mDialogType) {
            //mIvPhotoAround.setImageURI(photoUri);
            mPhotoAroundUri = photoUri;
            displayResizedImage(requireActivity(), photoUri, mIvPhotoAround);
            mIvPhotoAround.setVisibility(View.VISIBLE);
            mLavPhotoAround.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.AROUND, mPhotoAroundUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.AROUND, mPhotoAroundUri);
        }else if(DIALOG_TYPE.OUTER == mDialogType) {
            //mIvPhotoOuter.setImageURI(photoUri);
            mPhotoOuterUri = photoUri;
            displayResizedImage(requireActivity(), photoUri, mIvPhotoOuter);
            mIvPhotoOuter.setVisibility(View.VISIBLE);
            mLavPhotoOuter.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.OUTER, mPhotoOuterUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.OUTER, mPhotoOuterUri);
        }else if(DIALOG_TYPE.INNER == mDialogType) {
            //mIvPhotoInner.setImageURI(photoUri);
            mPhotoInnerUri = photoUri;
            displayResizedImage(requireActivity(), photoUri, mIvPhotoInner);
            mIvPhotoInner.setVisibility(View.VISIBLE);
            mLavPhotoInner.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.INNER, mPhotoInnerUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.INNER, mPhotoInnerUri);
        }else if(DIALOG_TYPE.ETC == mDialogType) {
            //mIvPhotoEtc.setImageURI(photoUri);
            mPhotoEtcUri = photoUri;
            displayResizedImage(requireActivity(), photoUri, mIvPhotoEtc);
            mIvPhotoEtc.setVisibility(View.VISIBLE);
            mLavPhotoEtc.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.ETC, mPhotoEtcUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.ETC, mPhotoEtcUri);
        }else if(DIALOG_TYPE.CAD == mDialogType) {
            //mIvCad.setImageURI(photoUri);

            //캐드는 여기서 부터 시작입니다.
            mCadUri = photoUri;

            //displayResizedImage(requireActivity(), photoUri, mIvCad);

            displayCadResizedImage(requireActivity(), photoUri, mIvCad);

            mIvCad.setVisibility(View.VISIBLE);
            mLavCad.setVisibility(View.GONE);
            ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.CAD, mCadUri);
            resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.CAD, mCadUri);

            //사진대지 > 앨범 사진 선택 후 > 로컬입력 처리 팝업 띄우기
            String mhNum = mEtMhNum.getText().toString();
            if(StringUtil.isNotNull(mhNum)) {
                goCadMakerDialog(mhNum, mCadUri);
            }else{
                CurvletManager.process(getActivity(), null, "water://toast?text=맨홀번호를 선택 후, 편집 가능합니다.");
            }
        }
    }

    /**
     * 편집된 cad이미지를 Set
     */
    private void setPhotoEditCad(Uri cadUri){
        mCadUri = cadUri;
        displayResizedImage(requireActivity(), cadUri, mIvCad);
        mIvCad.setVisibility(View.VISIBLE);
        mLavCad.setVisibility(View.GONE);
        ExcelManager.I(getActivity()).setPhotoUri(ExcelManager.PHOTO_TYPE.CAD, mCadUri);
        resizeAndSaveImage(PHOTO_SELECT_TYPE.ALBUM, DIALOG_TYPE.CAD, mCadUri);

/*        Glide.with(mContext).load(cadUri)
                .placeholder(R.drawable.ic_launcher)
                .override(100,100)
                .encodeQuality(90)
                .format(DecodeFormat.PREFER_RGB_565)
                .dontAnimate()
                .into(mIvCad);*/

    }

    private void displayCadResizedImage(Activity activity, Uri imageUri, ImageView imageView) {
        try {
            // Load the captured image from the file
            Bitmap originalBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(imageUri));
            // Resize the image while maintaining aspect ratio
            //Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800); // Set the desired width here
            // Display the resized image in the ImageView
            imageView.setImageBitmap(originalBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayResizedImage(Activity activity, Uri imageUri, ImageView imageView) {
        try {
            // Load the captured image from the file
            Bitmap originalBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(imageUri));
            // Resize the image while maintaining aspect ratio
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800); // Set the desired width here
            // Display the resized image in the ImageView
            imageView.setImageBitmap(resizedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate the new height to maintain the aspect ratio
        float aspectRatio = (float) width / height;
        int newWidth = maxWidth;
        int newHeight = Math.round(maxWidth / aspectRatio);

        // Create a matrix for the resizing and apply it to the bitmap
        Matrix matrix = new Matrix();
        matrix.postScale((float) newWidth / width, (float) newHeight / height);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    /**
     * 저장하기
     */
    private void goSave() {

        Log.d(LOG_TAG, "-------------------------------------------------");
        Log.d(LOG_TAG, "맨홀번호 : " + mEtMhNum.getText());
        Log.d(LOG_TAG, "조사일시 : " + mTvMhDate.getText());
        Log.d(LOG_TAG, "좌표 : " + mTvMhCoordinate.getText());

        Log.d(LOG_TAG, "깊이 : " + mEtMhDepth.getText());
        Log.d(LOG_TAG, "유입관경 : " + mEtMhInflow.getText());
        Log.d(LOG_TAG, "유출관경 : " + mEtMhOutflow.getText());

        Log.d(LOG_TAG, "시점 : " + mEtMhLocalSp.getText());               //시점
        Log.d(LOG_TAG, "종점 : " + mEtMhLocalEp.getText());               //종점
        Log.d(LOG_TAG, "관종 : " + mEtMhLocalSpecies.getText());          //관종
        Log.d(LOG_TAG, "관경 : " + mEtMhLocalCircumference.getText());    //관경
        Log.d(LOG_TAG, "연장 : " + mEtMhLocalExtension.getText());        //연장
        Log.d(LOG_TAG, "비고 : " + mEtMhLocalBigo.getText());             //비고

        Log.d(LOG_TAG, "맨홀전경 : " + mPhotoAroundUri);                    //맨홀전경
        Log.d(LOG_TAG, "맨홀외부 : " + mPhotoOuterUri);                     //맨홀외부
        Log.d(LOG_TAG, "맨홀내부 : " + mPhotoInnerUri);                     //맨홀내부
        Log.d(LOG_TAG, "특이사항 : " + mPhotoEtcUri);                       //특이사항
        Log.d(LOG_TAG, "도면 : " + mCadUri);                              //도면

        HashMap<String, String> params = new HashMap<>();

        HashMap<String, Spinner> spinnerMapData = SpinnerManager.I().getSpinnerMap();
        for (Map.Entry<String, Spinner> entry : spinnerMapData.entrySet()) {
            String key = entry.getKey();
            Spinner spinner = entry.getValue();
            String value = "";
            if ("선택".equalsIgnoreCase(spinner.getSelectedItem().toString())) {
                value = " ";
            }else if("직접입력".equalsIgnoreCase(spinner.getSelectedItem().toString())) {
                value = ((TextView)spinner.getSelectedView()).getText().toString();
            } else {
                value = spinner.getSelectedItem().toString();
            }
            params.put(key, value);
            Log.d(LOG_TAG, "선택한 key : " + key + ", value : " + value);
        }

        Log.d(LOG_TAG, "fieldIdx : " + mFieldIdx);
        Log.d(LOG_TAG, "멤버 mbIdx : " + MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX, "1"));
        Log.d(LOG_TAG, "맨홀번호 mhNum : " + mEtMhNum.getText());
        Log.d(LOG_TAG, "조사일시 mhDate : " + mTvMhDate.getText());
        Log.d(LOG_TAG, "좌표 mhCoordinate : " + mTvMhCoordinate.getText());

        Log.d(LOG_TAG, "맨홀깊이 mhDepth : " + mEtMhDepth.getText());
        Log.d(LOG_TAG, "유입관경 mhInflow : " + mEtMhInflow.getText());
        Log.d(LOG_TAG, "유출관경 mhOutflow : " + mEtMhOutflow.getText());

        Log.d(LOG_TAG, "특이사항 mhRemark : " + mEtMhRemark.getText());

        Log.d(LOG_TAG, "시점 mhLocalSp : " + mEtMhLocalSp.getText());               //시점
        Log.d(LOG_TAG, "종점 mhLocalEp : " + mEtMhLocalEp.getText());               //종점
        Log.d(LOG_TAG, "관종 mhLocalSpecies : " + mEtMhLocalSpecies.getText());          //관종
        Log.d(LOG_TAG, "관경 mhLocalCircumference : " + mEtMhLocalCircumference.getText());    //관경
        Log.d(LOG_TAG, "연장 mhLocalExtension : " + mEtMhLocalExtension.getText());        //연장
        Log.d(LOG_TAG, "비고 mhLocalBigo: " + mEtMhLocalBigo.getText());             //비고

        Log.d(LOG_TAG, "맨홀전경 fileAround : " + mPhotoAroundUri);                    //맨홀전경
        Log.d(LOG_TAG, "맨홀외부 fileOuter : " + mPhotoOuterUri);                     //맨홀외부
        Log.d(LOG_TAG, "맨홀내부 fileInner : " + mPhotoInnerUri);                     //맨홀내부
        Log.d(LOG_TAG, "특이사항 fileEtc : " + mPhotoEtcUri);                       //특이사항
        Log.d(LOG_TAG, "도면 fileCad : " + mCadUri);                              //도면

        params.put("fieldIdx", mFieldIdx);                                                  //필드idx

        String fieldDetailIdx = "";
        try{
            fieldDetailIdx = mFieldDetailBeanS.getRes_mh_field_d_idx();
        }catch(NullPointerException e){

        }
        if(!StringUtil.isEmpty(fieldDetailIdx)) {
            params.put("fieldDetailIdx", fieldDetailIdx);            //필드상세idx
        }

        params.put("mbIdx", MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX));                                                        //회원번호
        params.put("mhNum", mEtMhNum.getText().toString());                                 //맨홀번호
        params.put("mhDate", mTvMhDate.getText().toString());                               //조사일시
        params.put("mhCoordinate", mTvMhCoordinate.getText().toString());                   //좌표
        params.put("mhDepth", mEtMhDepth.getText().toString());                             //깊이
        params.put("mhInflow", mEtMhInflow.getText().toString());                               //유입
        params.put("mhOutflow", mEtMhOutflow.getText().toString());                             //유출
        params.put("mhRemark", mEtMhRemark.getText().toString());                           //특이사항(비고)
        params.put("mhLocalSp", mEtMhLocalSp.getText().toString());                         //시점
        params.put("mhLocalEp", mEtMhLocalEp.getText().toString());                         //종점
        params.put("mhLocalSpecies", mEtMhLocalSpecies.getText().toString());               //관종
        params.put("mhLocalCircumference", mEtMhLocalCircumference.getText().toString());   //관경
        params.put("mhLocalExtension", mEtMhLocalExtension.getText().toString());           //연장
        params.put("mhLocalBigo", mEtMhLocalBigo.getText().toString());                     //비고


        ArrayMap<Integer, File> fileParams = new ArrayMap<Integer, File>();
        File file1 = ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.AROUND.name());
        File file2 = ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.OUTER.name());
        File file3 = ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.INNER.name());
        File file4 = ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.ETC.name());
        File file5 = ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.CAD.name());
        if(file1 != null){
            fileParams.put(1, ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.AROUND.name()));
        }
        if(file2 != null){
            fileParams.put(2, ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.OUTER.name()));
        }
        if(file3 != null){
            fileParams.put(3, ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.INNER.name()));
        }
        if(file4 != null){
            fileParams.put(4, ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.ETC.name()));
        }
        if(file5 != null){
            fileParams.put(5, ExcelManager.I(getActivity()).getPhotoFileMap().get(ExcelManager.PHOTO_TYPE.CAD.name()));
        }

        if(checkValue()) {

            ObserverInterface dataObserver = new ObserverInterface() {
                @Override
                public void callback(BaseBean baseBean) {
                    GomsLog.d(LOG_TAG, "mDataObserver  CallBack() getStatus() : " + BaseBean.STATUS.SUCCESS);

                    if (baseBean.getStatus() == BaseBean.STATUS.SUCCESS) {

                        //사전데이타 초기화
                        clearPreloadData();
                        //사전데이타 세팅하기
                        setPreloadData("MH_NUM", mEtMhNum.getText().toString());
                        setPreloadData("MH_INFLOW", mEtMhInflow.getText().toString());
                        setPreloadData("MH_OUTFLOW", mEtMhOutflow.getText().toString());
                        setPreloadData("MH_DRAINAGE", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Drainage));
                        setPreloadData("MH_STANDARD", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.LidStandard));
                        setPreloadData("MH_MATERIAL", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Material));
                        setPreloadData("MH_SIZE", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Size));
                        setPreloadData("MH_LOCALSP", mEtMhLocalSp.getText().toString());
                        setPreloadData("MH_LOCALEP", mEtMhLocalEp.getText().toString());
                        setPreloadData("MH_LOCALSPECIES", mEtMhLocalSpecies.getText().toString());
                        setPreloadData("MH_LOCALCIRCUMFERENCE", mEtMhLocalCircumference.getText().toString());

                        ExcelManager.I(getActivity()).clearPhotoFileMap();
                        showDialogComplete();

                    } else {
                        GomsLog.d(LOG_TAG, "CallBack() : Data 실패!!!!");
                    }
                }
            };
            if(FORM_TYPE.CREATE.name().equalsIgnoreCase(mFormType.name())) {
                //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_DETAIL_INSERT, params, fileParams, dataObserver);
            }else{
                //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_DETAIL_MODIFY, params, fileParams, dataObserver);
            }
        }
        Log.d(LOG_TAG, "-------------------------------------------------");
    }

    /**
     * 좌표가지고 오기
     */
    class GPSHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if(GPSTracker.SEND_PRINT == msg.what) {
                String coordinate = (String)msg.obj;
                mTvMhCoordinate.setText(coordinate);
            }

        }
    }

    /**
     * 사전데이타 저장하기
     * @param key
     * @param value
     */
    public void setPreloadData(String key, String value){
        GomsLog.d(LOG_TAG, "setPreloadData() : 사전데이타 저장하기");
        if(mPreloadDataHashMap == null){
            mPreloadDataHashMap = new HashMap<>();
        }
        mPreloadDataHashMap.put(key,value);

        MyApplication.getInstance().prefs().put(ManHolePrefs.MB_PRELOAD_DATA, getPreloadData());
        String preLoadData = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_PRELOAD_DATA);
        GomsLog.d(LOG_TAG, "사전데이타 저장하기 >>>>" + preLoadData);
    }

    /**
     * 사전데이타 가져오기
     */
    public String getPreloadData(){
        int total = 0;
        try{
            total = mPreloadDataHashMap.size();
        }catch(NullPointerException e){

        }
        if(total > 0) {
            String data = GsonManager.I().getHashMapData(mPreloadDataHashMap);
            return data;
        }else{
            return "";
        }
    }

    public void clearPreloadData(){
        MyApplication.getInstance().prefs().put(ManHolePrefs.MB_PRELOAD_DATA, "");
    }

    public void test(){
        //사전데이타 초기화
        clearPreloadData();
        //사전데이타 세팅하기
        setPreloadData("MH_NUM", mEtMhNum.getText().toString());
        setPreloadData("MH_INFLOW", mEtMhInflow.getText().toString());
        setPreloadData("MH_OUTFLOW", mEtMhOutflow.getText().toString());
        setPreloadData("MH_DRAINAGE", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Drainage));
        setPreloadData("MH_STANDARD", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.LidStandard));
        setPreloadData("MH_MATERIAL", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Material));
        setPreloadData("MH_SIZE", SpinnerManager.I().getSelectedSpinnerData(SpinnerManager.INPUT_TYPE.Size));
        setPreloadData("MH_LOCALSP", mEtMhLocalSp.getText().toString());
        setPreloadData("MH_LOCALEP", mEtMhLocalEp.getText().toString());
        setPreloadData("MH_LOCALSPECIES", mEtMhLocalSpecies.getText().toString());
        setPreloadData("MH_LOCALCIRCUMFERENCE", mEtMhLocalCircumference.getText().toString());

        GomsLog.d(LOG_TAG, "preloadData() : 사전데이타 확인하기 : " + getPreloadData());//{"MH_INFLOW":"","MH_DRAINAGE":"선택","MH_LOCALSPECIES":"","MH_LOCALCIRCUMFERENCE":"","MH_OUTFLOW":"","MH_STANDARD":"선택","MH_LOCALSP":"","MH_NUM":"","MH_MATERIAL":"선택","MH_SIZE":"선택","MH_LOCALEP":""}
    }

    public void showDialogComplete(){
        DialogManager.I().setTitle("맨홀조사야장")
                .setMessage("맨홀조사야장 생성 완료했습니다")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName("확인")
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.basic.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){

                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){

                            requireActivity().onBackPressed();
                            /*
                            // Create a callback to handle back button press
                            OnBackPressedCallback callback = new OnBackPressedCallback(true) {
                                @Override
                                public void handleOnBackPressed() {
                                    // Custom behavior when back button is pressed within the fragment
                                    requireActivity().onBackPressed();
                                }
                            };

                            // Add the callback to the fragment's onBackPressedDispatcher
                            requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
                            */
                        }
                    }
                }))
                .showDialog(getActivity());
    }

    private void registerPhoto(DIALOG_TYPE dialogType){
        //photo around
        String dialogTitle = "사진대지";
        String dialogMsg = "";
        if(DIALOG_TYPE.AROUND == dialogType) {
            dialogMsg = "맨홀 전경 사진을 선택해주세요";
        }else if(DIALOG_TYPE.OUTER == dialogType) {
            dialogMsg = "맨홀 외부 사진을 선택해주세요";
        }else if(DIALOG_TYPE.INNER == dialogType) {
            dialogMsg = "맨홀 내부 사진을 선택해주세요";
        }else if(DIALOG_TYPE.ETC == dialogType) {
            dialogMsg = "맨홀 기타 사진을 선택해주세요";
        }

        ArrayList<SearchListBean> searchList = new ArrayList<>();

        SearchListBean searchListBean = new SearchListBean();
        searchListBean.setList_idx("1");
        searchListBean.setList_title("카메라");
        searchListBean.setSelect(false);
        searchList.add(searchListBean);

        searchListBean = new SearchListBean();
        searchListBean.setList_idx("2");
        searchListBean.setList_title("앨범");
        searchListBean.setSelect(false);
        searchList.add(searchListBean);

        DialogManager.I().setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName("")
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setDataList(searchList)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.search_list.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseBean) {
                        String btnType = ((Bundle) baseBean.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if (BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)) {
                            Log.d(LOG_TAG, " 클릭 >>>> 왼쪽");
                        } else if (BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)) {
                            Log.d(LOG_TAG, " 클릭 >>>> 오른쪽");
                            /*
                            String inputData = ((Bundle)baseData.getObject()).getString(InputFormBottomDialogCommand.EXT_INPUT);
                            if(StringUtil.isNotNull(inputData)){
                                sendNewGroupNeme(inputData);
                            }
                             */
                        } else if (BaseBottomDialogCommand.BTN_TYPE.SELECT.name().equalsIgnoreCase(btnType)) {
                            SearchListBean searchListBean = ((Bundle) baseBean.getObject()).getParcelable(SearchListBottomDialogCommand.EXT_SEARCH_BEAN);
                            String photoToolType = searchListBean.getList_idx();

                            Log.d(LOG_TAG, " 클릭 >>>> 선택 : " + dialogType + ", " + photoToolType);

                            if(StringUtil.stringToInt(photoToolType) == 1){
                                goCamera(dialogType);
                                mPhotoSelectType = PHOTO_SELECT_TYPE.CAMERA;
                            }else{
                                goAlbum(dialogType);
                                mPhotoSelectType = PHOTO_SELECT_TYPE.ALBUM;
                            }

                        }
                    }
                }))
                .showDialog(getActivity());
    }
    private void deletePhotoDailog(DIALOG_TYPE dialogType, String dialogTitle, String dialogMsg, String key){
        DialogManager.I().setTitle(dialogTitle)
                .setMessage(dialogMsg)
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("삭제하기")
                .setPositiveBtnName("수정하기")
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.bottom_basic.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseBean) {
                        String btnType = ((Bundle) baseBean.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if (BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)) {
                            Log.d(LOG_TAG, " 클릭 >>>> 왼쪽");
                            clearPhotoItem(dialogType, key);
                        } else if (BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)) {
                            Log.d(LOG_TAG, " 클릭 >>>> 오른쪽 >>> 수정하기");
                            clearPhotoItem(dialogType, key);
                            registerPhoto(dialogType);
                        }
                    }
                }))
                .showDialog(getActivity());
    }

    private void goCadMakerDialog(String mhNum, Uri mhCadUri){

        //checkSignature();
        DialogManager.I().setTag("cadMakerInsert")
                .setTitle("도면편집")
                .setMessage("맨홀번호 : " + mhNum + "\n해당 주소명을 입력해주세요")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName(getActivity().getString(kr.co.goms.module.common.R.string.confirm))
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.input_form.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){
                            Log.d(LOG_TAG, " 클릭 >>>> 왼쪽");
                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){
                            String mhLocal = ((Bundle)baseData.getObject()).getString(InputFormBottomDialogCommand.EXT_INPUT);
                            if(StringUtil.isNotNull(mhLocal)){

                                /*
                                boolean existCad = true;
                                Uri cadPhotoUri = null;
                                if(FORM_TYPE.CREATE.name().equalsIgnoreCase(mFormType.name())) {
                                    if(mhCadUri != null){
                                        cadPhotoUri = mhCadUri;
                                    }else{
                                        //Cad이미지 없음.
                                        existCad = false;
                                    }
                                }else{
                                    if (!StringUtil.isEmpty(mFieldDetailBeanS.getRes_mh_cad())) {
                                        cadPhotoUri = Uri.parse(mFieldDetailBeanS.getRes_mh_cad());
                                    }else{
                                        existCad = false;
                                    }
                                }
                                */

                                //if(existCad) {
                                    CadBean cadBean = new CadBean();
                                    cadBean.setMH_NUM(mhNum);
                                    cadBean.setMH_LOCAL(mhLocal);
                                    cadBean.setMH_CAD_URI(mhCadUri);    //file:///storage/emulated/0/Documents/Manhole/Photo/manhole_59_cad_20240413_093424_2817441952573148117.jpg
                                    PhotoEditModule.setSharpType("NONE");
                                    PhotoEditModule.setPhotoEditCallback(new PhotoEditModule.PhotoEditCallback() {
                                        @Override
                                        public void callback(Uri photoUri) {
                                            setPhotoEditCad(photoUri);
                                        }
                                    });

                                    Intent intent = new Intent(getActivity(), EditActivity.class);
                                    intent.putExtra(EditActivity.EXT_CAD_BEAN, cadBean);
                                    intent.putExtra(EditActivity.EXT_SHARP_TYPE, PhotoEditModule.EDIT_SHARP_TYPE.NONE.name());
                                    startActivity(intent);
                                //}
                            }
                        }
                    }
                }))
                .showDialog(getActivity());
    }


    private void clearPhotoItem(DIALOG_TYPE dialogType,String key){
        ExcelManager.I(getActivity()).deletePhotoFileMapItem(key);

        if(DIALOG_TYPE.AROUND == dialogType) {
            mIvPhotoAround.setBackground(null);
            mIvPhotoAround.setVisibility(View.GONE);
            mLavPhotoAround.setVisibility(View.VISIBLE);
        }else if(DIALOG_TYPE.OUTER == dialogType) {
            mIvPhotoOuter.setBackground(null);
            mIvPhotoOuter.setVisibility(View.GONE);
            mLavPhotoOuter.setVisibility(View.VISIBLE);
        }else if(DIALOG_TYPE.INNER == dialogType) {
            mIvPhotoInner.setBackground(null);
            mIvPhotoInner.setVisibility(View.GONE);
            mLavPhotoInner.setVisibility(View.VISIBLE);
        }else if(DIALOG_TYPE.ETC == dialogType) {
            mIvPhotoEtc.setBackground(null);
            mIvPhotoEtc.setVisibility(View.GONE);
            mLavPhotoEtc.setVisibility(View.VISIBLE);
        }else if(DIALOG_TYPE.CAD == dialogType) {
            mIvCad.setBackground(null);
            mIvCad.setVisibility(View.GONE);
            mLavCad.setVisibility(View.VISIBLE);
        }
    }

    OnNextProcess mOnNextProcess;
    interface OnNextProcess{
        void onNext();
    }
}
