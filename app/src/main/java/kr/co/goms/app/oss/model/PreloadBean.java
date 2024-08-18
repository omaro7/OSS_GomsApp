package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PreloadBean implements Parcelable {
    private String MH_NUM;              //맨홀번호
    private String MH_INFLOW;           //유입
    private String MH_OUTFLOW;          //유출
    private String MH_DRAINAGE;         //배제방식
    private String MH_STANDARD;         //규격
    private String MH_MATERIAL;         //재질
    private String MH_SIZE;             //사이즈
    private String MH_LOCALSP;          //시점
    private String MH_LOCALEP;          //종점
    private String MH_LOCALSPECIES;     //관종
    private String MH_LOCALCIRCUMFERENCE;//관경


    public PreloadBean(){

    }

    protected PreloadBean(Parcel in) {
        MH_NUM = in.readString();
        MH_INFLOW = in.readString();
        MH_OUTFLOW = in.readString();
        MH_DRAINAGE = in.readString();
        MH_STANDARD = in.readString();
        MH_MATERIAL = in.readString();
        MH_SIZE = in.readString();
        MH_LOCALSP = in.readString();
        MH_LOCALEP = in.readString();
        MH_LOCALSPECIES = in.readString();
        MH_LOCALCIRCUMFERENCE = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MH_NUM);
        dest.writeString(MH_INFLOW);
        dest.writeString(MH_OUTFLOW);
        dest.writeString(MH_DRAINAGE);
        dest.writeString(MH_STANDARD);
        dest.writeString(MH_MATERIAL);
        dest.writeString(MH_SIZE);
        dest.writeString(MH_LOCALSP);
        dest.writeString(MH_LOCALEP);
        dest.writeString(MH_LOCALSPECIES);
        dest.writeString(MH_LOCALCIRCUMFERENCE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PreloadBean> CREATOR = new Creator<PreloadBean>() {
        @Override
        public PreloadBean createFromParcel(Parcel in) {
            return new PreloadBean(in);
        }

        @Override
        public PreloadBean[] newArray(int size) {
            return new PreloadBean[size];
        }
    };

    public String getMH_NUM() {
        return MH_NUM;
    }

    public void setMH_NUM(String MH_NUM) {
        this.MH_NUM = MH_NUM;
    }

    public String getMH_INFLOW() {
        return MH_INFLOW;
    }

    public void setMH_INFLOW(String MH_INFLOW) {
        this.MH_INFLOW = MH_INFLOW;
    }

    public String getMH_OUTFLOW() {
        return MH_OUTFLOW;
    }

    public void setMH_OUTFLOW(String MH_OUTFLOW) {
        this.MH_OUTFLOW = MH_OUTFLOW;
    }

    public String getMH_DRAINAGE() {
        return MH_DRAINAGE;
    }

    public void setMH_DRAINAGE(String MH_DRAINAGE) {
        this.MH_DRAINAGE = MH_DRAINAGE;
    }

    public String getMH_STANDARD() {
        return MH_STANDARD;
    }

    public void setMH_STANDARD(String MH_STANDARD) {
        this.MH_STANDARD = MH_STANDARD;
    }

    public String getMH_MATERIAL() {
        return MH_MATERIAL;
    }

    public void setMH_MATERIAL(String MH_MATERIAL) {
        this.MH_MATERIAL = MH_MATERIAL;
    }

    public String getMH_SIZE() {
        return MH_SIZE;
    }

    public void setMH_SIZE(String MH_SIZE) {
        this.MH_SIZE = MH_SIZE;
    }

    public String getMH_LOCALSP() {
        return MH_LOCALSP;
    }

    public void setMH_LOCALSP(String MH_LOCALSP) {
        this.MH_LOCALSP = MH_LOCALSP;
    }

    public String getMH_LOCALEP() {
        return MH_LOCALEP;
    }

    public void setMH_LOCALEP(String MH_LOCALEP) {
        this.MH_LOCALEP = MH_LOCALEP;
    }

    public String getMH_LOCALSPECIES() {
        return MH_LOCALSPECIES;
    }

    public void setMH_LOCALSPECIES(String MH_LOCALSPECIES) {
        this.MH_LOCALSPECIES = MH_LOCALSPECIES;
    }

    public String getMH_LOCALCIRCUMFERENCE() {
        return MH_LOCALCIRCUMFERENCE;
    }

    public void setMH_LOCALCIRCUMFERENCE(String MH_LOCALCIRCUMFERENCE) {
        this.MH_LOCALCIRCUMFERENCE = MH_LOCALCIRCUMFERENCE;
    }
}
