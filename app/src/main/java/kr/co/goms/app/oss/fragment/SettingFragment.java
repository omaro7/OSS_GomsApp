package kr.co.goms.app.oss.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.module.common.activity.CustomActivity;

public class SettingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SettingFragment.class.getSimpleName();

    public SettingFragment(){}

    private Toolbar mToolbar;
    private SwitchCompat mSwitchBlcok;

    private String isShowBlockYN = "N";


    public static SettingFragment getFragment(int page){
        SettingFragment fragment = new SettingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", page);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_setting, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar = view.findViewById(R.id.toolbar);
        ((CustomActivity) getActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((CustomActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView tvToolBarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvToolBarTitle.setText("설정");

        isShowBlockYN = MyApplication.getInstance().prefs().get(ManHolePrefs.MH_BLOCK_IMPORT_YN);

        mSwitchBlcok = view.findViewById(R.id.sc_block_yn);
        mSwitchBlcok.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isShowBlockYN = isChecked?"Y":"N";
                MyApplication.getInstance().prefs().put(ManHolePrefs.MH_BLOCK_IMPORT_YN, isShowBlockYN);
            }
        });

        mSwitchBlcok.setChecked("Y".equalsIgnoreCase(isShowBlockYN));

        RelativeLayout rltCadEdit = view.findViewById(R.id.rl_cad_edit);
        rltCadEdit.setOnClickListener(this);

        this.setHasOptionsMenu(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.rl_cad_edit){
        }

    }
}
