package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class FieldDetailBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<FieldDetailBeanS> res_data;

    protected FieldDetailBeanG(Parcel in) {
        res_data = in.createTypedArrayList(FieldDetailBeanS.CREATOR);
    }

    public static final Creator<FieldDetailBeanG> CREATOR = new Creator<FieldDetailBeanG>() {
        @Override
        public FieldDetailBeanG createFromParcel(Parcel in) {
            return new FieldDetailBeanG(in);
        }

        @Override
        public FieldDetailBeanG[] newArray(int size) {
            return new FieldDetailBeanG[size];
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

    public ArrayList<FieldDetailBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<FieldDetailBeanS> res_data) {
        this.res_data = res_data;
    }
}
