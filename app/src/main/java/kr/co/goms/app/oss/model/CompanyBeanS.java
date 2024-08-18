package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CompanyBeanS implements Parcelable {
    private String res_mh_com_idx;          //순번
    private String res_mh_com_name;        //시험

    public CompanyBeanS(){

    }

    protected CompanyBeanS(Parcel in) {
        res_mh_com_idx = in.readString();
        res_mh_com_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(res_mh_com_idx);
        dest.writeString(res_mh_com_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CompanyBeanS> CREATOR = new Creator<CompanyBeanS>() {
        @Override
        public CompanyBeanS createFromParcel(Parcel in) {
            return new CompanyBeanS(in);
        }

        @Override
        public CompanyBeanS[] newArray(int size) {
            return new CompanyBeanS[size];
        }
    };

    public String getRes_mh_com_idx() {
        return res_mh_com_idx;
    }

    public void setRes_mh_com_idx(String res_mh_com_idx) {
        this.res_mh_com_idx = res_mh_com_idx;
    }

    public String getRes_mh_com_name() {
        return res_mh_com_name;
    }

    public void setRes_mh_com_name(String res_mh_com_name) {
        this.res_mh_com_name = res_mh_com_name;
    }
}
