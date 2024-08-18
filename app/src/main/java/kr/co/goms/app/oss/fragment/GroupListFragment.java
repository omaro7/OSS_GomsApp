package kr.co.goms.app.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import kr.co.goms.app.oss.MainActivity;
import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.activity.SettingActivity;
import kr.co.goms.app.oss.adapter.GroupAdapter;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.manager.SendManager;
import kr.co.goms.app.oss.send_data.SendDataFactory;
import kr.co.goms.module.common.activity.CustomActivity;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.base.WaterCallBack;
import kr.co.goms.module.common.command.BaseBottomDialogCommand;
import kr.co.goms.module.common.command.InputFormBottomDialogCommand;
import kr.co.goms.module.common.manager.DialogCommandFactory;
import kr.co.goms.module.common.manager.DialogManager;
import kr.co.goms.module.common.model.GroupBeanS;
import kr.co.goms.module.common.observer.ObserverInterface;
import kr.co.goms.module.common.util.GomsLog;
import kr.co.goms.module.common.util.StringUtil;

public class GroupListFragment extends Fragment  implements View.OnClickListener {

    private final String TAG = GroupListFragment.class.getSimpleName();

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Filter mFilter;

    private ProgressBar mPbLoading;

    private ArrayList<GroupBeanS> mGroupList = new ArrayList<GroupBeanS>();

    private static final int SPAN_COUNT = 3 ;

    private EditText mEtSearchPlace;
    private ImageButton mInputClear;

    private Button mBtnGroupCreate;

    private ObserverInterface mDataObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        ImageView ivSetting = view.findViewById(R.id.iv_setting);
        ivSetting.setOnClickListener(this);
        mPbLoading = view.findViewById(R.id.pb_loader);

        mRecyclerView = view.findViewById(R.id.rv_group_list);

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

        String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
        if(StringUtil.stringToInt(mbIdx) > 0) {
            getGroupData(mbIdx);
        }

        //mTvTitle.setText(getString(R.string.exam_place_title));

        mBtnGroupCreate = view.findViewById(R.id.btn_ok);
        mBtnGroupCreate.setOnClickListener(this);

        this.setHasOptionsMenu(true);
    }

    private void getGroupData(String mbIdx){

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

                    mGroupList = (ArrayList<GroupBeanS>) baseBean.getObject();
                    GomsLog.d(TAG, "mGroupList 갯수 : " + mGroupList.size());

                    //왜 화면로딩이 되지 않을까?? -> Thread 처리
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        setGroupList(mGroupList);
                    } else {
                        // WorkThread이면, MainThread에서 실행 되도록 변경.
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setGroupList(mGroupList);
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
        params.put("mbIdx", mbIdx);
        SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.GROUP_LIST, params, mDataObserver);

    }

    @MainThread
    private void setGroupList(ArrayList<GroupBeanS> placeList){
        Log.d(TAG, "setGroupList()");
        mAdapter = new GroupAdapter(getActivity(), placeList, new GroupAdapter.GroupClickListener() {
            @Override
            public void onGroupClick(int position, GroupBeanS groupBeanS) {
                Log.d(TAG, "Group 클릭 >>>> " + groupBeanS.getRes_mh_group_name());
                ((MainActivity)getActivity()).changeFragment(FieldBasicListFragment.getFragment(groupBeanS.getRes_mh_group_idx(), groupBeanS.getRes_mh_group_name()), "fieldBasicList");
            }

            @Override
            public void onGroupLongClick(int position, GroupBeanS groupBeanS) {
                Log.d(TAG, "Group 롱클릭 >>>> " + groupBeanS.getRes_mh_group_name());
                goGroupDeleteDialog(groupBeanS);
            }
        });

        ((GroupAdapter)mAdapter).setSearchType(GroupAdapter.SEARCH_TYPE.TITLE);    //장소명 검색조건
        mFilter = ((GroupAdapter)mAdapter).getFilter();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);

        //LayoutManager를 재세팅을 해야지만, 상단으로 다시 정렬됨
        mLayoutManager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);
        ((StaggeredGridLayoutManager)mLayoutManager).setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
    }


    @Override
    public void onPause() {
        super.onPause();
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
                goDialog();
        }else if(id == R.id.iv_setting) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
        }
    }

    private void goDialog(){

        //checkSignature();
        DialogManager.I().setTag("groupInsert")
                .setTitle("그룹생성")
                .setMessage("그룹명을 입력해주세요")
                .setShowTitle(true)
                .setShowMessage(true)
                .setNegativeBtnName("")
                .setPositiveBtnName(getActivity().getString(kr.co.goms.module.common.R.string.confirm))
                .setCancelable(true)
                .setCancelTouchOutSide(true)
                .setDataList(mGroupList)
                .setCommand(DialogCommandFactory.I().createDialogCommand(getActivity(), DialogCommandFactory.DIALOG_TYPE.input_form.name(), new WaterCallBack() {
                    @Override
                    public void callback(BaseBean baseData) {
                        String btnType = ((Bundle)baseData.getObject()).getString(BaseBottomDialogCommand.EXT_BTN_TYPE);
                        if(BaseBottomDialogCommand.BTN_TYPE.LEFT.name().equalsIgnoreCase(btnType)){
                            Log.d(TAG, " 클릭 >>>> 왼쪽");
                        }else if(BaseBottomDialogCommand.BTN_TYPE.RIGHT.name().equalsIgnoreCase(btnType)){
                            String inputData = ((Bundle)baseData.getObject()).getString(InputFormBottomDialogCommand.EXT_INPUT);
                            if(StringUtil.isNotNull(inputData)){
                                String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
                                sendNewGroupNeme(mbIdx, inputData);
                            }
                        }
                    }
                }))
                .showDialog(getActivity());
    }

    /**
     * 그룹생성하기 서버전송
     * @param inputData
     */
    private void sendNewGroupNeme(String mbIdx, String inputData){
        HashMap<String, String> params = new HashMap<>();
        params.put("mbIdx", mbIdx);
        params.put("groupName", inputData);
        //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.GROUP_INSERT, params, mDataObserver);
    }

    /**
     * 그룹삭제하기 서버전송
     * @param mbIdx
     * @param groupIdx
     */
    private void sendGroupDelete(String mbIdx, String groupIdx){
        HashMap<String, String> params = new HashMap<>();
        params.put("mbIdx", mbIdx);
        params.put("groupIdx", groupIdx);
        //SendManager.I().sendData(SendDataFactory.URL_DATA_TYPE.GROUP_DELETE, params, mDataObserver);
    }

    /**
     * 그룹삭제하기 하단팝업
     * @param groupBeanS
     */
    private void goGroupDeleteDialog(GroupBeanS groupBeanS){
        DialogManager.I().setTag("groupDelete")
                .setTitle("'"+ groupBeanS.getRes_mh_group_name()+"' 삭제")
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
                            Log.d(TAG, " 클릭 >>>> 삭제하기 mbIdx:" + MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX) + ",groupIdx:" + groupBeanS.getRes_mh_group_name());
                            String mbIdx = MyApplication.getInstance().prefs().get(ManHolePrefs.MB_IDX);
                            sendGroupDelete(mbIdx, groupBeanS.getRes_mh_group_idx());
                        }
                    }
                }))
                .showDialog(getActivity());
    }


}
