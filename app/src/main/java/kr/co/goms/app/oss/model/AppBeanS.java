package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;
public class AppBeanS implements Parcelable {
    private String exam_seq;          //순번
    private String exam_title;        //시험

    public AppBeanS(){

    }

    protected AppBeanS(Parcel in) {
        exam_seq = in.readString();
        exam_title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(exam_seq);
        dest.writeString(exam_title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppBeanS> CREATOR = new Creator<AppBeanS>() {
        @Override
        public AppBeanS createFromParcel(Parcel in) {
            return new AppBeanS(in);
        }

        @Override
        public AppBeanS[] newArray(int size) {
            return new AppBeanS[size];
        }
    };

    public String getExam_seq() {
        return exam_seq;
    }

    public void setExam_seq(String exam_seq) {
        this.exam_seq = exam_seq;
    }

    public String getExam_title() {
        return exam_title;
    }

    public void setExam_title(String exam_title) {
        this.exam_title = exam_title;
    }
}
