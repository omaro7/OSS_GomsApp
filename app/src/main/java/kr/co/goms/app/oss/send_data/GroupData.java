package kr.co.goms.app.oss.send_data;

import kr.co.goms.app.oss.manager.GsonManager;
import kr.co.goms.app.oss.model.GroupBeanG;
import kr.co.goms.module.common.base.BaseBean;
import kr.co.goms.module.common.manager.HttpClientManager;
import kr.co.goms.module.common.model.CommonBeanG;
import kr.co.goms.module.common.task.RequestItem;
import kr.co.goms.module.common.task.ServerTask;
import kr.co.goms.module.common.util.GomsLog;

/**
 * 그룹 리스트 가져오기
 */
public class GroupData extends SendData implements ISendData{
    private final String TAG = GroupData.class.getSimpleName();

    static GroupData instance;
    public static GroupData I(GsonManager.PARSER_TYPE parserType){
        if(instance == null){
            instance = new GroupData();
        }
        mParserType = parserType;
        return instance;
    }

    @Override
    public void onSendData() {
        GomsLog.d(TAG, GroupData.class.getSimpleName() + " >>>> onSendStempData()");

        RequestItem requestItem = new RequestItem();
        requestItem.targetUrl = mUrl;
        requestItem.targetMethodType = mMethodType;
        requestItem.bodyMap = mParam;

        HttpClientManager.I().sendServerData(requestItem, new ServerTask.OnAsyncResult() {
                    @Override
                    public void onResultSuccess(int resultCode, String message, String jsonString) {
                        GomsLog.d(TAG, "jsonString: " + jsonString);

                        Object object = GsonManager.I().createParserData(jsonString, mParserType);
                        if("200".equals(((CommonBeanG)object).getRes_result())){
                            successData(object);
                        }else{
                            failData(jsonString);
                        }
                    }

                    @Override
                    public void onResultFail(int resultCode, String errorMessage, String jsonString) {
                        GomsLog.d(TAG, "Fail jsonString: " + jsonString);

                        Object object = GsonManager.I().createParserData(jsonString, mParserType);
                        if("200".equals(((CommonBeanG)object).getRes_result())){
                            successData(object);
                        }else{
                            failData(jsonString);
                        }

                        //failData();
                    }
                }
        );
    }

    private void successData(Object object){
        GomsLog.d(TAG, "successData() 성공");
        BaseBean baseBean = new BaseBean();
        baseBean.setObject(((GroupBeanG) object).getRes_data());
        baseBean.setStatus(BaseBean.STATUS.SUCCESS);
        mObserver.callback(baseBean);
    }

    private void failData(String jsonString){
        GomsLog.d(TAG, "failData() 실패");
        BaseBean baseBean = new BaseBean();
        baseBean.setStatus(BaseBean.STATUS.FAIL);
        baseBean.setObject(jsonString);
        mObserver.callback(baseBean);
    }
}
