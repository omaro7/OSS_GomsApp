package kr.co.goms.app.oss.send_data;

import kr.co.goms.app.oss.manager.GsonManager;
import kr.co.goms.app.oss.model.FieldDetailBeanG;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.manager.HttpClientManager;
import kr.co.goms.module.common.model.CommonBeanG;
import kr.co.goms.module.common.task.RequestItem;
import kr.co.goms.module.common.task.ServerTask;
import kr.co.goms.module.common.util.GomsLog;

/**
 * 야장조사 상세 저장하기
 */
public class FieldDetailData extends SendData implements ISendData{
    private final String LOG_TAG = FieldDetailData.class.getSimpleName();

    static FieldDetailData instance;

    public static FieldDetailData I(GsonManager.PARSER_TYPE parserType){
        if(instance == null){
            instance = new FieldDetailData();
        }
        mParserType = parserType;
        return instance;
    }


    @Override
    public void onSendData() {
        GomsLog.d(LOG_TAG, FieldDetailData.class.getSimpleName() + " >>>> onSendStempData()");

        RequestItem requestItem = new RequestItem();
        requestItem.targetUrl = mUrl;
        requestItem.targetMethodType = mMethodType;
        requestItem.bodyMap = mParam;
        requestItem.fileMap = mFileParam;

        HttpClientManager.I().sendServerData(requestItem, new ServerTask.OnAsyncResult() {
                    @Override
                    public void onResultSuccess(int resultCode, String message, String jsonString) {
                        GomsLog.d(LOG_TAG, "jsonString: " + jsonString);

                        Object object = GsonManager.I().createParserData(jsonString, mParserType);
                        if("200".equals(((CommonBeanG)object).getRes_result())){
                            successData(object);
                        }else{
                            failData(jsonString);
                        }
                    }

                    @Override
                    public void onResultFail(int resultCode, String errorMessage, String jsonString) {
                        GomsLog.d(LOG_TAG, "Fail jsonString: " + jsonString);
                        failData(jsonString);
                        //failData();
                    }
                }
        );
    }

    private void successData(Object object){
        GomsLog.d(LOG_TAG, "successData() 성공");
        BaseBean baseBean = new BaseBean();
        baseBean.setObject(((FieldDetailBeanG) object).getRes_data());
        baseBean.setStatus(BaseBean.STATUS.SUCCESS);
        mObserver.callback(baseBean);
    }

    private void failData(String jsonString){
        GomsLog.d(LOG_TAG, "failData() 실패");
        BaseBean baseBean = new BaseBean();
        baseBean.setStatus(BaseBean.STATUS.FAIL);
        baseBean.setObject(jsonString);
        mObserver.callback(baseBean);
    }
}
