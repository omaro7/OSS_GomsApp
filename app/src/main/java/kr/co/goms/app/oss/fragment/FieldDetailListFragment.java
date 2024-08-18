package kr.co.goms.app.oss.fragment;

import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import kr.co.goms.app.oss.AppConstant;
import kr.co.goms.app.oss.MainActivity;
import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.adapter.FieldDetailListAdapter;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.manager.ExcelManager;
import kr.co.goms.app.oss.manager.SendManager;
import kr.co.goms.app.oss.model.FieldBasicBeanS;
import kr.co.goms.app.oss.model.FieldDetailBeanS;
import kr.co.goms.app.oss.model.FieldDetailListSumBeanS;
import kr.co.goms.app.oss.send_data.SendDataFactory;
import kr.co.goms.module.common.activity.CustomActivity;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;
import kr.co.goms.module.common.command.BaseBottomDialogCommand;
import kr.co.goms.module.common.curvlet.CurvletManager;
import kr.co.goms.module.common.manager.DialogCommandFactory;
import kr.co.goms.module.common.manager.DialogManager;
import kr.co.goms.module.common.observer.ObserverInterface;
import kr.co.goms.module.common.util.GomsLog;
import kr.co.goms.module.common.util.StringUtil;

public class FieldDetailListFragment extends Fragment  implements View.OnClickListener {

    private final String TAG = FieldDetailListFragment.class.getSimpleName();
    private Toolbar mToolbar;
    private TextView mTvToolbarTitle;
    private LottieAnimationView mLavNoData, mLavLoadingExcel;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Filter mFilter;

    private ProgressBar mPbLoading;

    private ArrayList<FieldDetailBeanS> mFieldDetailList = new ArrayList<FieldDetailBeanS>();

    private static final int SPAN_COUNT = 1;

    private EditText mEtSearchPlace;
    private ImageButton mInputClear;

    private Button mBtnCreate;
    private Button mBtnExcel;

    private ObserverInterface mDataObserver;

    private String mFieldIdx = "";
    private String mFieldTitle = "";

    private int mTotal = 0;

    private FieldBasicBeanS mFieldBasicBeanS;
    private static final String EXT_FIELD_BASIC = "ext_field_basic";

    public static FieldDetailListFragment getFragment(FieldBasicBeanS fieldBasicBeanS){
        FieldDetailListFragment fragment = new FieldDetailListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXT_FIELD_BASIC, fieldBasicBeanS);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_field_detail_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mFieldBasicBeanS = getArguments().getParcelable(EXT_FIELD_BASIC);

        mFieldIdx = mFieldBasicBeanS.getRes_mh_field_idx();
        mFieldTitle = mFieldBasicBeanS.getRes_mh_field_title();

        if(StringUtil.isEmpty(mFieldIdx)){
            CurvletManager.process(getActivity(), null, "water://toast?text=기본데이타를 선택 후, 진입 가능합니다.");
            return;
        }

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mTvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        mTvToolbarTitle.setText(mFieldTitle);

        mPbLoading = view.findViewById(R.id.pb_loader);
        mLavNoData = view.findViewById(R.id.lav_field_detail_list);
        mLavLoadingExcel = view.findViewById(R.id.lav_loading_excel);
        mRecyclerView = view.findViewById(R.id.rv_field_detail_list);

        //mLayoutManager = new GridLayoutManager(view.getContext(), SPAN_COUNT);
        mLayoutManager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);
        ((StaggeredGridLayoutManager)mLayoutManager).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mEtSearchPlace = view.findViewById(R.id.et_search_place);

        mEtSearchPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (!TextUtils.isEmpty(charSequence.toString()) || !"".equals(charSequence.toString())) {
                        String searchTxt = mEtSearchPlace.getText().toString();
                        mFilter.filter(searchTxt);
                        showInputClear(true);
                    } else if (TextUtils.isEmpty(charSequence.toString())) {
                        mFilter.filter("");
                        showInputClear(false);
                    }
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }catch(NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mInputClear = view.findViewById(R.id.btn_input_clear);
        mInputClear.setOnClickListener(this);

        mBtnCreate = view.findViewById(R.id.btn_ok);
        mBtnCreate.setOnClickListener(this);

        mBtnExcel = view.findViewById(R.id.btn_excel);
        mBtnExcel.setOnClickListener(this);

        getFieldDetailListData();

        this.setHasOptionsMenu(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the home button (back arrow) click
            // For example, navigate back to a previous fragment or activity
            // You can customize the behavior here
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFieldDetailListData(){

        mDataObserver = new ObserverInterface() {
            @Override
            public void callback(BaseBean baseBean) {
                GomsLog.d(TAG, "mDataObserver  CallBack()");

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    mPbLoading.setVisibility(View.GONE);
                } else {
                    // WorkThread이면, MainThread에서 실행 되도록 변경.
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPbLoading.setVisibility(View.GONE);
                            }
                        });
                    }catch(NullPointerException e){

                    }
                }

                if (baseBean.getStatus() == BaseBean.STATUS.SUCCESS) {

                    mFieldDetailList = (ArrayList<FieldDetailBeanS>) baseBean.getObject();
                    GomsLog.d(TAG, "mFieldDetailList 갯수 : " + mFieldDetailList.size());

                    //왜 화면로딩이 되지 않을까?? -> Thread 처리
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        setFieldDetailList(mFieldDetailList);
                    } else {
                        // WorkThread이면, MainThread에서 실행 되도록 변경.
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFieldDetailList(mFieldDetailList);
                            }
                        });
                    }

                } else {
                    GomsLog.d(TAG, "CallBack() : 인트로 Category Data 실패!!!!");
                }
            }
        };

        GomsLog.d(TAG, "sendFontData()");
        HashMap<String, String> params = new HashMap<>();
        params.put("fieldBasicIdx", mFieldIdx);
        SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_DETAIL_LIST, params, mDataObserver);

    }

    private void setFieldDetailList(ArrayList<FieldDetailBeanS> fieldDetailList){
        mAdapter = new FieldDetailListAdapter(getActivity(), fieldDetailList, new FieldDetailListAdapter.FieldDetailClickListener() {
            @Override
            public void onFieldDetailClick(int position, FieldDetailBeanS fieldDetailBeanS) {
                Log.d(TAG, "FieldDetailClick 클릭");
                //((MainActivity)getActivity()).changeFragment(FieldDetailFormFragment.getFragment(mFieldIdx, mFieldTitle, fieldDetailBeanS), "fieldDetail");
            }

            @Override
            public void onFieldDetailLongClick(int position, FieldDetailBeanS fieldDetailBeanS) {
                Log.d(TAG, "FieldDetail 롱클릭 >>>> " + fieldDetailBeanS.getRes_mh_num());
                goFieldDetailDeleteDialog(fieldDetailBeanS);
            }
        });

        ((FieldDetailListAdapter)mAdapter).setSearchType(FieldDetailListAdapter.SEARCH_TYPE.TITLE);    //장소명 검색조건
        mFilter = ((FieldDetailListAdapter)mAdapter).getFilter();

        mRecyclerView.setAdapter(mAdapter);


        try{
            mTotal = fieldDetailList.size();
        }catch(NullPointerException e){

        }
        if(mTotal > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mBtnExcel.setVisibility(View.VISIBLE);
            mLavNoData.setVisibility(View.GONE);
        }else{
            mRecyclerView.setVisibility(View.GONE);
            mBtnExcel.setVisibility(View.GONE);
            mLavNoData.setVisibility(View.VISIBLE);
        }

        //LayoutManager를 재세팅을 해야지만, 상단으로 다시 정렬됨
        mLayoutManager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);
        ((StaggeredGridLayoutManager)mLayoutManager).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * 글자 클리어 처리
     */
    private void applyClear(){
        mEtSearchPlace.setText("");
        mFilter.filter("");
        showInputClear(false);
    }

    private void showInputClear(boolean isShow){
        if(isShow) {
            mInputClear.setVisibility(View.VISIBLE);
        }else{
            mInputClear.setVisibility(View.GONE);
        }
    }

    /**
     * 엑셀 만들기
     */
    private void createExcel(){
        GomsLog.d(TAG, "createExcel() >>>> 엑셀만들기 시작합니다.");

        mLavLoadingExcel.setVisibility(View.VISIBLE);

        //집계데이타 확인하기
        getFieldDetailListSumData(mFieldIdx);

        ExcelManager.I(getActivity()).setExcelInterface(new ExcelManager.ExcelInterface() {
            @Override
            public void onComplete() {
                showDialogComplete();
            }
        });

        //상세데이타 확인하기
    }

    private void getFieldDetailListSumData(String fieldIdx){
        GomsLog.d(TAG, "createExcel() > getFieldDetailListSumData() >>>> 집계표 데이타 송수신합니다.");
        ObserverInterface sumDataObserver = new ObserverInterface() {
            @Override
            public void callback(BaseBean baseBean) {
                GomsLog.d(TAG, "sumDataObserver  CallBack()");
                if (baseBean.getStatus() == BaseBean.STATUS.SUCCESS) {
                    ArrayList<FieldDetailListSumBeanS> fieldDetailListSumData = (ArrayList< FieldDetailListSumBeanS>) baseBean.getObject();
                    GomsLog.d(TAG, "fieldDetailListSumData 갯수 : " + fieldDetailListSumData.size());
                    //총괄집계표 및 탭 만들기
                    //ExcelManager.I(getActivity()).initializeWorkbook();

                    //엑셀 첫 시작 시점입니다.
                    ExcelManager.I(getActivity()).createWorkbook();
                    ExcelManager.I(getActivity()).createManholeSumExcel(mFieldBasicBeanS, fieldDetailListSumData.get(0), mFieldDetailList);
                } else {
                    GomsLog.d(TAG, "CallBack() : getFieldDetailListSumData 실패!!!!");
                }
            }
        };
        GomsLog.d(TAG, "getFieldDetailListSumData()");
        HashMap<String, String> params = new HashMap<>();
        params.put("fieldBasicIdx", fieldIdx);
        SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_DETAIL_LIST_SUM, params, sumDataObserver);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if(id == R.id.btn_input_clear) {
            applyClear();
        }else if(id == R.id.btn_ok) {
            ((MainActivity) getActivity()).changeFragment(FieldDetailFormFragment.getFragment(mFieldIdx, mFieldTitle), "fieldDetail");
        }else if(id == R.id.btn_excel) {
            createExcel();
        }
    }


    /**
     * 상세데이타 서버전송
     * @param fieldDetailIdx
     */
    private void sendFieldDetailDelete(String mbIdx, String fieldBasicIdx, String fieldDetailIdx){
        HashMap<String, String> params = new HashMap<>();
        params.put("mbIdx", mbIdx);
        params.put("fieldBasicIdx", fieldBasicIdx);
        params.put("fieldDetailIdx", fieldDetailIdx);
        //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_DETAIL_DELETE, params, mDataObserver);
    }

    /**
     * 상세데이타 삭제하기 하단팝업
     * @param fieldDetailBeanS
     */
    private void goFieldDetailDeleteDialog(FieldDetailBeanS fieldDetailBeanS){
        DialogManager.I().setTag("fieldDetailDelete")
                .setTitle("'"+ fieldDetailBeanS.getRes_mh_num()+"' 삭제")
                .setMessage("해당 데이타를 삭제하시겠습니까?")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("취소")
                .setPositiveBtnName(getActivity().getString(kr.co.goms.module.common.R.string.confirm))
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.bottom_basic.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){
                            Log.d(TAG, " 클릭 >>>> 왼쪽");
                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){
                            Log.d(TAG, " 클릭 >>>> 삭제하기 mbIdx:" + MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX) + ",FieldDetailIdx:" + fieldDetailBeanS.getRes_mh_field_d_idx() + ",mhNum:" + fieldDetailBeanS.getRes_mh_num());
                            String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
                            sendFieldDetailDelete(mbIdx, mFieldIdx, fieldDetailBeanS.getRes_mh_field_d_idx());
                        }
                    }
                }))
                .showDialog(getActivity());
    }

    public void showDialogComplete(){

        if (Looper.myLooper() == Looper.getMainLooper()) {
            mLavLoadingExcel.setVisibility(View.GONE);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLavLoadingExcel.setVisibility(View.GONE);
                }
            });
        }

        DialogManager.I().setTitle("엑셀 생성")
                .setMessage("엑셀 생성을 완료했습니다.")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName("확인")
                .setCancelable(false)
                .setCancelTouchOutSide(false)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.basic.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){

                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){
                            //ExcelManager.D();
                            requireActivity().onBackPressed();
                        }
                    }
                }))
                .showDialog(getActivity());
    }

    /**
     * 40개 이상이면 생성불가
     */
    public void showDialogOverCnt(int cnt){

        DialogManager.I().setTitle("맨홀조사야장 생성")
                .setMessage("총 " + cnt +"개 이상은 생성을 할 수 없습니다.")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName("확인")
                .setCancelable(false)
                .setCancelTouchOutSide(false)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.basic.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){

                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){

                        }
                    }
                }))
                .showDialog(getActivity());
    }
}
