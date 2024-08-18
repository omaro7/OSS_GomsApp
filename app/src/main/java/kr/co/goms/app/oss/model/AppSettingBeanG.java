package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class AppSettingBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<AppBeanS> res_data;

    protected AppSettingBeanG(Parcel in) {
        res_data = in.createTypedArrayList(AppBeanS.CREATOR);
    }

    public static final Creator<AppSettingBeanG> CREATOR = new Creator<AppSettingBeanG>() {
        @Override
        public AppSettingBeanG createFromParcel(Parcel in) {
            return new AppSettingBeanG(in);
        }

        @Override
        public AppSettingBeanG[] newArray(int size) {
            return new AppSettingBeanG[size];
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

    public ArrayList<AppBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<AppBeanS> res_data) {
        this.res_data = res_data;
    }
}
