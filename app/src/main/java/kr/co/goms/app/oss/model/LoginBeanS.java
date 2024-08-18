package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginBeanS implements Parcelable {
    private String res_mb_idx;          //멤버키

    public LoginBeanS(){

    }

    protected LoginBeanS(Parcel in) {
        res_mb_idx = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(res_mb_idx);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LoginBeanS> CREATOR = new Creator<LoginBeanS>() {
        @Override
        public LoginBeanS createFromParcel(Parcel in) {
            return new LoginBeanS(in);
        }

        @Override
        public LoginBeanS[] newArray(int size) {
            return new LoginBeanS[size];
        }
    };

    public String getRes_mb_idx() {
        return res_mb_idx;
    }

    public void setRes_mb_idx(String res_mb_idx) {
        this.res_mb_idx = res_mb_idx;
    }
}
