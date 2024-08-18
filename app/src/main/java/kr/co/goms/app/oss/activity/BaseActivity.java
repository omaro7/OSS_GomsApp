package kr.co.goms.app.oss.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.goms.app.oss.MyApplication;

public abstract class BaseActivity extends AppCompatActivity {
	
    protected MyApplication mApp;
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = this;
        mApp = (MyApplication) getApplicationContext();
    }
    
    /**
     * ApplicationClass 객체 가져오기.
     * @return Application Class
     */
    public MyApplication app() {
        return mApp;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            try {
                super.setRequestedOrientation(requestedOrientation);
            } catch (IllegalStateException e) {
            }
        }
    }
}
