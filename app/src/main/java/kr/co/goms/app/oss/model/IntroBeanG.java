package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class IntroBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<CompanyBeanS> res_data_company;

    protected IntroBeanG(Parcel in) {
        res_data_company = in.createTypedArrayList(CompanyBeanS.CREATOR);
    }

    public static final Creator<IntroBeanG> CREATOR = new Creator<IntroBeanG>() {
        @Override
        public IntroBeanG createFromParcel(Parcel in) {
            return new IntroBeanG(in);
        }

        @Override
        public IntroBeanG[] newArray(int size) {
            return new IntroBeanG[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(res_data_company);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<CompanyBeanS> getRes_data() {
        return res_data_company;
    }

    public void setRes_data(ArrayList<CompanyBeanS> res_data) {
        this.res_data_company = res_data;
    }
}
