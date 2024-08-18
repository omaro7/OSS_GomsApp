package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 집계표를 만들기 위해 필요한 bean
 */
public class FieldDetailListSumBean implements Parcelable {

    private FieldBasicBeanS fieldBasicBeanS;
    private FieldDetailListSumBeanS fieldDetailListSumBeanS;

    public FieldDetailListSumBean(){

    }

    protected FieldDetailListSumBean(Parcel in) {
        fieldBasicBeanS = in.readParcelable(FieldBasicBeanS.class.getClassLoader());
        fieldDetailListSumBeanS = in.readParcelable(FieldDetailListSumBeanS.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(fieldBasicBeanS, flags);
        dest.writeParcelable(fieldDetailListSumBeanS, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FieldDetailListSumBean> CREATOR = new Creator<FieldDetailListSumBean>() {
        @Override
        public FieldDetailListSumBean createFromParcel(Parcel in) {
            return new FieldDetailListSumBean(in);
        }

        @Override
        public FieldDetailListSumBean[] newArray(int size) {
            return new FieldDetailListSumBean[size];
        }
    };

    public FieldBasicBeanS getFieldBasicBeanS() {
        return fieldBasicBeanS;
    }

    public void setFieldBasicBeanS(FieldBasicBeanS fieldBasicBeanS) {
        this.fieldBasicBeanS = fieldBasicBeanS;
    }

    public FieldDetailListSumBeanS getFieldDetailListSumBeanS() {
        return fieldDetailListSumBeanS;
    }

    public void setFieldDetailListSumBeanS(FieldDetailListSumBeanS fieldDetailListSumBeanS) {
        this.fieldDetailListSumBeanS = fieldDetailListSumBeanS;
    }
}
