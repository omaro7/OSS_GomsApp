package kr.co.goms.app.oss.manager;

import android.util.ArrayMap;

import java.io.File;
import java.util.HashMap;

import kr.co.goms.app.oss.send_data.SendData;
import kr.co.goms.app.oss.send_data.SendDataFactory;
import kr.co.goms.module.common.observer.ObserverInterface;
import kr.co.goms.module.common.util.GomsLog;

/**
 * 전송 매니져
 */
public class SendManager {

    private final static String TAG = SendManager.class.getSimpleName();

    static SendManager mSendManager;    //instance

    public SendManager() {

    }

    public static SendManager I(){
        if(mSendManager == null){
            mSendManager = new SendManager();
        }
        return mSendManager;
    }

    public void destroy() {

    }

    /**
     * 서버 전송 처리
     * @param dataType DATA_TYPE
     * @param param 전달 param
     * @param observer  결과 observer
     */
    public void sendData(SendDataFactory.URL_DATA_TYPE dataType, HashMap<String, String> param, ObserverInterface observer){
        GomsLog.d(TAG, "sendData()");
        SendDataFactory sendDataFactory = new SendDataFactory();
        SendData sendData = sendDataFactory.createSendData(dataType);
        sendData.setParam(param);
        sendData.setObserver(observer);
        sendData.onSendData();
    }

    public void sendData(SendDataFactory.URL_DATA_TYPE dataType, HashMap<String, String> param, ArrayMap<Integer, File> fileParam, ObserverInterface observer){
        GomsLog.d(TAG, "sendData()");
        SendDataFactory sendDataFactory = new SendDataFactory();
        SendData sendData = sendDataFactory.createSendData(dataType);
        sendData.setParam(param);
        sendData.setFileParam(fileParam);
        sendData.setObserver(observer);
        sendData.onSendData();
    }

}
