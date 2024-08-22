package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class FileBeanS implements Parcelable {
    private String file_name;
    private Long file_size;
    private File file;

    public FileBeanS(){

    }

    public FileBeanS(String name, long length, File file) {
        this.file_name = name;
        this.file_size = length;
        this.file = file;
    }

    protected FileBeanS(Parcel in) {
        file_name = in.readString();
        if (in.readByte() == 0) {
            file_size = null;
        } else {
            file_size = in.readLong();
        }
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
