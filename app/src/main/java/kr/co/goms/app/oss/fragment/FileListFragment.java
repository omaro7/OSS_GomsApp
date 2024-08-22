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
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import kr.co.goms.app.oss.MainActivity;
import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.activity.SettingActivity;
import kr.co.goms.app.oss.adapter.FileListAdapter;
import kr.co.goms.app.oss.adapter.GroupAdapter;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.manager.SendManager;
import kr.co.goms.app.oss.model.FieldBasicBeanS;
import kr.co.goms.app.oss.model.FileBeanS;
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

public class FileListFragment extends Fragment  implements View.OnClickListener {

    private final String TAG = FileListFragment.class.getSimpleName();

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Filter mFilter;

    private ProgressBar mPbLoading;

    private static final int SPAN_COUNT = 1 ;

    private EditText mEtSearchPlace;
    private ImageButton mInputClear;

    private ObserverInterface mDataObserver;

    public static FileListFragment getFragment(){
        FileListFragment fragment = new FileListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        ImageView ivSetting = view.findViewById(R.id.iv_setting);
        ivSetting.setOnClickListener(this);
        mPbLoading = view.findViewById(R.id.pb_loader);

        mRecyclerView = view.findViewById(R.id.rv_file_list);

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

        getExcelFileData();

        this.setHasOptionsMenu(true);
    }

    private void getExcelFileData(){

        String[] excelExtensions = {".xls", ".xlsx"};
        String directoryPath = "/storage/emulated/0/Document";
        ArrayList<FileBeanS> excelFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName().toLowerCase();
                        // Check if the file name contains "oss" and is an Excel file
                        if (fileName.contains("oss") && hasExcelExtension(fileName, excelExtensions)) {
                            excelFiles.add(new FileBeanS(file.getName(), file.length()));
                        }
                    }
                }
            }
        }

        //왜 화면로딩이 되지 않을까?? -> Thread 처리
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setFileList(excelFiles);
        } else {
            // WorkThread이면, MainThread에서 실행 되도록 변경.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setFileList(excelFiles);
                }
            });
        }
    }

    private boolean hasExcelExtension(String fileName, String[] excelExtensions) {
        for (String extension : excelExtensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @MainThread
    private void setFileList(ArrayList<FileBeanS> placeList){
        Log.d(TAG, "setGroupList()");
        mAdapter = new FileListAdapter(getActivity(), placeList, new FileListAdapter.FileClickListener() {
            @Override
            public void onFileClick(int position, FileBeanS groupBeanS) {
                Log.d(TAG, "File 클릭 >>>> " + groupBeanS.getFile_name());

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
        }else if(id == R.id.iv_setting) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
        }
    }


}
