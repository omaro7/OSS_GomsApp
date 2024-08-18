package kr.co.goms.app.oss.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.fragment.SettingFragment;
import kr.co.goms.module.common.activity.CustomActivity;

public class SettingActivity extends CustomActivity {

    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment fragment = SettingFragment.getFragment(0);
        fragmentTransaction.replace(R.id.setting_nav_host_fragment, fragment).commit();
    }

    public void chageFragment(Fragment fragment, String name){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("");
        fragmentTransaction.replace(R.id.setting_nav_host_fragment, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        int count = mFragmentManager.getBackStackEntryCount();
        if(count == 0){
            super.onBackPressed();
        }else{
            getSupportFragmentManager().popBackStack();
        }
    }

}