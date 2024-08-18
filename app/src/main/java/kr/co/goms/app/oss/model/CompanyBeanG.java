package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class CompanyBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<CompanyBeanS> res_data;

    protected CompanyBeanG(Parcel in) {
        res_data = in.createTypedArrayList(CompanyBeanS.CREATOR);
    }

    public static final Creator<CompanyBeanG> CREATOR = new Creator<CompanyBeanG>() {
        @Override
        public CompanyBeanG createFromParcel(Parcel in) {
            return new CompanyBeanG(in);
        }

        @Override
        public CompanyBeanG[] newArray(int size) {
            return new CompanyBeanG[size];
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

    public ArrayList<CompanyBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<CompanyBeanS> res_data) {
        this.res_data = res_data;
    }
}
