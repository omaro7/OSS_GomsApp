package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kr.co.goms.module.common.model.CommonBeanG;
import kr.co.goms.module.common.model.GroupBeanS;

public class GroupBeanG extends CommonBeanG implements Parcelable {

    private ArrayList<GroupBeanS> res_data;

    protected GroupBeanG(Parcel in) {
        res_data = in.createTypedArrayList(GroupBeanS.CREATOR);
    }

    public static final Creator<GroupBeanG> CREATOR = new Creator<GroupBeanG>() {
        @Override
        public GroupBeanG createFromParcel(Parcel in) {
            return new GroupBeanG(in);
        }

        @Override
        public GroupBeanG[] newArray(int size) {
            return new GroupBeanG[size];
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

    public ArrayList<GroupBeanS> getRes_data() {
        return res_data;
    }

    public void setRes_data(ArrayList<GroupBeanS> res_data) {
        this.res_data = res_data;
    }
}
