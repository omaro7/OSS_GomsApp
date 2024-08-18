package kr.co.goms.app.oss.send_data;

import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.manager.GsonManager;

public class SendDataFactory {

    public enum URL_DATA_TYPE{
        APP_SETTING("100"),
        GROUP_LIST("102"),
        COMPANY_LIST("104"),
        FIELD_BASIC_LIST("106"),
        FIELD_DETAIL_LIST("108"),
        FIELD_DETAIL_LIST_SUM("109"),
        ;

        String mRequestUrlCode;
        URL_DATA_TYPE(String request_url_code) {
            this.mRequestUrlCode = request_url_code;
        }

        public String getRequestUrlCode() {
            return mRequestUrlCode;
        }
    }

    public SendData createSendData(URL_DATA_TYPE urlDataType){
        SendData sendData = null;
        if(urlDataType == URL_DATA_TYPE.APP_SETTING){
            sendData = new AppSettingData();
        }else if(urlDataType == URL_DATA_TYPE.GROUP_LIST) {
            sendData = GroupData.I(GsonManager.PARSER_TYPE.groupList);
        }else if(urlDataType == URL_DATA_TYPE.COMPANY_LIST) {
            sendData = new CompanyData();
        }else if(urlDataType == URL_DATA_TYPE.FIELD_BASIC_LIST) {
            sendData = FieldBasicData.I(GsonManager.PARSER_TYPE.fieldBasicList);
        }else if(urlDataType == URL_DATA_TYPE.FIELD_DETAIL_LIST) {
            sendData = FieldDetailData.I(GsonManager.PARSER_TYPE.fieldDetailList);
        }else if(urlDataType == URL_DATA_TYPE.FIELD_DETAIL_LIST_SUM) {
            sendData = new FieldDetailListSumData();
        }
        sendData.setUrl(MyApplication.getInstance().getGomsJNI().requestURL(urlDataType.getRequestUrlCode()));
        return sendData;
    }

}
