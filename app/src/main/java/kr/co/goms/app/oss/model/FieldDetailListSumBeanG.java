package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;

public class FieldDetailListSumBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<FieldDetailListSumBeanS> res_data;

    protected FieldDetailListSumBeanG(Parcel in) {
        res_data = in.createTypedArrayList(FieldDetailListSumBeanS.CREATOR);
    }

    public static final Creator<FieldDetailListSumBeanG> CREATOR = new Creator<FieldDetailListSumBeanG>() {
        @Override
        public FieldDetailListSumBeanG createFromParcel(Parcel in) {
            return new FieldDetailListSumBeanG(in);
        }

        @Override
        public FieldDetailListSumBeanG[] newArray(int size) {
            return new FieldDetailListSumBeanG[size];
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

    public ArrayList<FieldDetailListSumBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<FieldDetailListSumBeanS> res_data) {
        this.res_data = res_data;
    }
}
