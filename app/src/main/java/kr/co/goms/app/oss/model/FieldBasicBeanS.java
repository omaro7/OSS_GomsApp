package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FieldBasicBeanS implements Parcelable {
    private String res_mh_com_idx;           //회사
    private String res_mb_idx;              //사용자
    private String res_mb_name;             //보고자

    private String res_mh_group_idx;        //그룹idx
    private String res_mh_field_idx;          //순번
    private String res_mh_field_title;        //제목
    private String res_mh_com_name;         //조사자
    private String res_mh_field_local;        //지역분구

    private String res_mh_field_regdate;      //생성일
    private String res_mh_field_tab;            //탭수

    public FieldBasicBeanS(){

    }

    protected FieldBasicBeanS(Parcel in) {
        res_mh_com_idx = in.readString();
        res_mb_idx = in.readString();
        res_mb_name = in.readString();
        res_mh_group_idx = in.readString();
        res_mh_field_idx = in.readString();
        res_mh_field_title = in.readString();
        res_mh_com_name = in.readString();
        res_mh_field_local = in.readString();
        res_mh_field_regdate = in.readString();
        res_mh_field_tab = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(res_mh_com_idx);
        dest.writeString(res_mb_idx);
        dest.writeString(res_mb_name);
        dest.writeString(res_mh_group_idx);
        dest.writeString(res_mh_field_idx);
        dest.writeString(res_mh_field_title);
        dest.writeString(res_mh_com_name);
        dest.writeString(res_mh_field_local);
        dest.writeString(res_mh_field_regdate);
        dest.writeString(res_mh_field_tab);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FieldBasicBeanS> CREATOR = new Creator<FieldBasicBeanS>() {
        @Override
        public FieldBasicBeanS createFromParcel(Parcel in) {
            return new FieldBasicBeanS(in);
        }

        @Override
        public FieldBasicBeanS[] newArray(int size) {
            return new FieldBasicBeanS[size];
        }
    };

    public String getRes_mh_com_idx() {
        return res_mh_com_idx;
    }

    public void setRes_mh_com_idx(String res_mh_com_idx) {
        this.res_mh_com_idx = res_mh_com_idx;
    }

    public String getRes_mb_idx() {
        return res_mb_idx;
    }

    public void setRes_mb_idx(String res_mb_idx) {
        this.res_mb_idx = res_mb_idx;
    }

    public String getRes_mb_name() {
        return res_mb_name;
    }

    public void setRes_mb_name(String res_mb_name) {
        this.res_mb_name = res_mb_name;
    }

    public String getRes_mh_group_idx() {
        return res_mh_group_idx;
    }

    public void setRes_mh_group_idx(String res_mh_group_idx) {
        this.res_mh_group_idx = res_mh_group_idx;
    }

    public String getRes_mh_field_idx() {
        return res_mh_field_idx;
    }

    public void setRes_mh_field_idx(String res_mh_field_idx) {
        this.res_mh_field_idx = res_mh_field_idx;
    }

    public String getRes_mh_field_title() {
        return res_mh_field_title;
    }

    public void setRes_mh_field_title(String res_mh_field_title) {
        this.res_mh_field_title = res_mh_field_title;
    }

    public String getRes_mh_com_name() {
        return res_mh_com_name;
    }

    public void setRes_mh_com_name(String res_mh_com_name) {
        this.res_mh_com_name = res_mh_com_name;
    }

    public String getRes_mh_field_local() {
        return res_mh_field_local;
    }

    public void setRes_mh_field_local(String res_mh_field_local) {
        this.res_mh_field_local = res_mh_field_local;
    }

    public String getRes_mh_field_regdate() {
        return res_mh_field_regdate;
    }

    public void setRes_mh_field_regdate(String res_mh_field_regdate) {
        this.res_mh_field_regdate = res_mh_field_regdate;
    }

    public String getRes_mh_field_tab() {
        return res_mh_field_tab;
    }

    public void setRes_mh_field_tab(String res_mh_field_tab) {
        this.res_mh_field_tab = res_mh_field_tab;
    }
}
