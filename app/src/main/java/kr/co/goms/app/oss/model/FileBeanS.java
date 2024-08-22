package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FileBeanS implements Parcelable {
    private String file_name;          //순번
    private Long file_size;        //시험

    public FileBeanS(){

    }

    protected FileBeanS(Parcel in) {
        file_name = in.readString();
        file_size = in.readLong();
    }

    public FileBeanS(String name, long length) {
        file_name = name;
        file_size = length;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file_name);
        if (file_size == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(file_size);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileBeanS> CREATOR = new Creator<FileBeanS>() {
        @Override
        public FileBeanS createFromParcel(Parcel in) {
            return new FileBeanS(in);
        }

        @Override
        public FileBeanS[] newArray(int size) {
            return new FileBeanS[size];
        }
    };

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public Long getFile_size() {
        return file_size;
    }

    public void setFile_size(Long file_size) {
        this.file_size = file_size;
    }
}
