package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExcelRowColumnBean implements Parcelable {
    private String row;     //순번
    private String colnum;  //순번
    private String title;   //시험

    public ExcelRowColumnBean(){

    }

    protected ExcelRowColumnBean(Parcel in) {
        row = in.readString();
        colnum = in.readString();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(row);
        dest.writeString(colnum);
        dest.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExcelRowColumnBean> CREATOR = new Creator<ExcelRowColumnBean>() {
        @Override
        public ExcelRowColumnBean createFromParcel(Parcel in) {
            return new ExcelRowColumnBean(in);
        }

        @Override
        public ExcelRowColumnBean[] newArray(int size) {
            return new ExcelRowColumnBean[size];
        }
    };

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColnum() {
        return colnum;
    }

    public void setColnum(String colnum) {
        this.colnum = colnum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
