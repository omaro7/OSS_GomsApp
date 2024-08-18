package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class LoginBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<LoginBeanS> res_data;

    protected LoginBeanG(Parcel in) {
        res_data = in.createTypedArrayList(LoginBeanS.CREATOR);
    }

    public static final Creator<LoginBeanG> CREATOR = new Creator<LoginBeanG>() {
        @Override
        public LoginBeanG createFromParcel(Parcel in) {
            return new LoginBeanG(in);
        }

        @Override
        public LoginBeanG[] newArray(int size) {
            return new LoginBeanG[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(res_data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<LoginBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<LoginBeanS> res_data) {
        this.res_data = res_data;
    }
}
