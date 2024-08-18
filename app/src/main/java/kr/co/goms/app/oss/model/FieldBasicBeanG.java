package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class FieldBasicBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<FieldBasicBeanS> res_data;

    protected FieldBasicBeanG(Parcel in) {
        res_data = in.createTypedArrayList(FieldBasicBeanS.CREATOR);
    }

    public static final Creator<FieldBasicBeanG> CREATOR = new Creator<FieldBasicBeanG>() {
        @Override
        public FieldBasicBeanG createFromParcel(Parcel in) {
            return new FieldBasicBeanG(in);
        }

        @Override
        public FieldBasicBeanG[] newArray(int size) {
            return new FieldBasicBeanG[size];
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

    public ArrayList<FieldBasicBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<FieldBasicBeanS> res_data) {
        this.res_data = res_data;
    }
}
