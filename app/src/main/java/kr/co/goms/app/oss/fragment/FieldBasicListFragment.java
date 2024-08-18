package kr.co.goms.app.oss.fragment;

import android.annotation.SuppressLint;
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

import kr.co.goms.app.oss.MainActivity;
import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.adapter.FieldBasicAdapter;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.manager.SendManager;
import kr.co.goms.app.oss.model.FieldBasicBeanS;
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

public class FieldBasicListFragment extends Fragment  implements View.OnClickListener {

    private final String TAG = FieldBasicListFragment.class.getSimpleName();
    private Toolbar mToolbar;
    private TextView mTvToolbarTitle;
    private LottieAnimationView mLavNoData;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Filter mFilter;

    private ProgressBar mPbLoading;

    private ArrayList<FieldBasicBeanS> mFieldBasicList = new ArrayList<FieldBasicBeanS>();

    private static final int SPAN_COUNT = 1;

    private EditText mEtSearchPlace;
    private ImageButton mInputClear;

    private Button mBtnGroupCreate;

    private ObserverInterface mDataObserver;
    private int mTotal = 0;

    private String mGroupIdx = "";
    private String mGroupName = "";

    public static FieldBasicListFragment getFragment(String groupIdx, String groupName){
        FieldBasicListFragment fragment = new FieldBasicListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("groupIdx", groupIdx);
        bundle.putString("groupName", groupName);
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
        return inflater.inflate(R.layout.fragment_field_basic_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mGroupIdx = getArguments().getString("groupIdx");
        mGroupName = getArguments().getString("groupName");

        if(StringUtil.isEmpty(mGroupIdx)){
            CurvletManager.process(getActivity(), null, "water://toast?text=그룹을 선택 후, 진입 가능합니다.");
            return;
        }

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mTvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        mTvToolbarTitle.setText(mGroupName);

        mPbLoading = view.findViewById(R.id.pb_loader);
        mLavNoData = view.findViewById(R.id.lav_field_basic_list);
        mRecyclerView = view.findViewById(R.id.rv_field_basic_list);

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

        getFieldBasicData();

        //mTvTitle.setText(getString(R.string.exam_place_title));

        mBtnGroupCreate = view.findViewById(R.id.btn_ok);
        mBtnGroupCreate.setOnClickListener(this);

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

    private void getFieldBasicData(){

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

                    mFieldBasicList = (ArrayList<FieldBasicBeanS>) baseBean.getObject();
                    GomsLog.d(TAG, "mFieldBasicList 갯수 : " + mFieldBasicList.size());

                    //왜 화면로딩이 되지 않을까?? -> Thread 처리
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        setFieldBasicList(mFieldBasicList);
                    } else {
                        // WorkThread이면, MainThread에서 실행 되도록 변경.
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFieldBasicList(mFieldBasicList);
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
        params.put("groupIdx", mGroupIdx);
        SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_BASIC_LIST, params, mDataObserver);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void setFieldBasicList(ArrayList<FieldBasicBeanS> fieldBasicList){
        mAdapter = new FieldBasicAdapter(getActivity(), fieldBasicList, new FieldBasicAdapter.FieldBasicClickListener() {
            @Override
            public void onFieldBasicClick(int position, FieldBasicBeanS fieldBasicBeanS) {
                Log.d(TAG, "FieldBasic 클릭");
                ((MainActivity)getActivity()).changeFragment(FieldDetailListFragment.getFragment(fieldBasicBeanS), "fieldDetailList");
            }

            @Override
            public void onFieldBasicLongClick(int position, FieldBasicBeanS fieldBasicBeanS) {
                Log.d(TAG, "FieldBasic 롱클릭 >>>> " + fieldBasicBeanS.getRes_mh_field_title());
                goFieldBasicDeleteDialog(fieldBasicBeanS);
            }
        });

        ((FieldBasicAdapter)mAdapter).setSearchType(FieldBasicAdapter.SEARCH_TYPE.TITLE);    //장소명 검색조건
        mFilter = ((FieldBasicAdapter)mAdapter).getFilter();

        mAdapter.notifyDataSetChanged();

        mRecyclerView.setAdapter(mAdapter);

        try{
            mTotal = fieldBasicList.size();
        }catch(NullPointerException e){

        }
        Log.d(TAG, "FieldBasic mTotal : " + mTotal);

        if(mTotal > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mLavNoData.setVisibility(View.GONE);
        }else{
            mRecyclerView.setVisibility(View.GONE);
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

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if(id == R.id.btn_input_clear) {
            applyClear();
        }else if(id == R.id.btn_ok) {
                //((MainActivity)getActivity()).changeFragment(FieldBasicFormFragment.getFragment(mGroupIdx, mGroupName), "fieldBasic");
        }
    }

    /**
     * 기본데이타 서버전송
     * @param fieldBasicIdx
     */
    private void sendFieldBasicDelete(String mbIdx, String groupIdx, String fieldBasicIdx){
        HashMap<String, String> params = new HashMap<>();
        params.put("mbIdx", mbIdx);
        params.put("groupIdx", groupIdx);
        params.put("fieldBasicIdx", fieldBasicIdx);
        //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.FIELD_BASIC_DELETE, params, mDataObserver);
    }

    /**
     * 기본데이타 삭제하기 하단팝업
     * @param fieldBasicBeanS
     */
    private void goFieldBasicDeleteDialog(FieldBasicBeanS fieldBasicBeanS){
        DialogManager.I().setTag("fieldBasicDelete")
                .setTitle("'"+ fieldBasicBeanS.getRes_mh_field_title()+"' 삭제")
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
                            Log.d(TAG, " 클릭 >>>> 삭제하기 mbIdx:" + MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX) + ",fieldIdx:" + fieldBasicBeanS.getRes_mh_field_idx());
                            String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
                            sendFieldBasicDelete(mbIdx, mGroupIdx, fieldBasicBeanS.getRes_mh_field_idx());
                        }
                    }
                }))
                .showDialog(getActivity());
    }


}
