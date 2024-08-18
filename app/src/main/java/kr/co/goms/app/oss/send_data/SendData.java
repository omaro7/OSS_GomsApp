package kr.co.goms.app.oss.send_data;

import android.util.ArrayMap;

import java.io.File;
import java.util.HashMap;

import kr.co.goms.app.oss.manager.GsonManager;
import kr.co.goms.app.oss.model.IntroBeanG;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.manager.HttpClientManager;
import kr.co.goms.module.common.model.CommonBeanG;
import kr.co.goms.module.common.observer.ObserverInterface;
import kr.co.goms.module.common.task.RequestItem;
import kr.co.goms.module.common.task.ServerTask;
import kr.co.goms.module.common.util.GomsLog;

public abstract class SendData {
    private static final String LOG_TAG = SendData.class.getSimpleName();
    String mUrl;
    String mMethodType;
    HashMap<String, String> mParam;
    ArrayMap<Integer, File> mFileParam;
    ObserverInterface mObserver;
    static GsonManager.PARSER_TYPE mParserType;

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public HashMap<String, String> getParam() {
        return mParam;
    }

    public void setParam(HashMap<String, String> param) {
        this.mParam = param;
    }

    public void setFileParam(ArrayMap<Integer, File> fileParam){
        this.mFileParam = fileParam;
    }

    public ObserverInterface getObserver() {
        return mObserver;
    }

    public void setParserType(GsonManager.PARSER_TYPE parserType){
        this.mParserType = parserType;
    }

    public void setObserver(ObserverInterface observerInterface) {
        this.mObserver = observerInterface;
    }

    public void onSendData() {
        GomsLog.d(LOG_TAG, "[super]SendStempStoreInsertData >>>> onSendData() " + mParserType.name());

        RequestItem requestItem = new RequestItem();
        requestItem.targetUrl = mUrl;
        requestItem.targetMethodType = mMethodType;
        requestItem.bodyMap = mParam;
        requestItem.fileMap = mFileParam;

        HttpClientManager.I().sendServerData(requestItem, new ServerTask.OnAsyncResult() {
                    @Override
                    public void onResultSuccess(int resultCode, String message, String jsonString) {
                        GomsLog.d("Stemp", "Success jsonString: " + jsonString);

                        Object object = GsonManager.I().createParserData(jsonString, mParserType);
                        if("200".equals(((CommonBeanG)object).getRes_result())){
                            successData(object);
                        }else{
                            failData();
                        }
                    }

                    @Override
                    public void onResultFail(int resultCode, String errorMessage, String jsonString) {
                        GomsLog.d("Stemp", "Fail jsonString: " + jsonString);
                        failData();
                    }
                }
        );
    }

    private void successData(Object object){
        BaseBean baseBean = new BaseBean();
        baseBean.setObject(((IntroBeanG) object).getRes_data());
        baseBean.setStatus(BaseBean.STATUS.SUCCESS);
        mObserver.callback(baseBean);
    }

    private void failData(){
        BaseBean baseBean = new BaseBean();
        baseBean.setStatus(BaseBean.STATUS.FAIL);
        mObserver.callback(baseBean);
    }
}
