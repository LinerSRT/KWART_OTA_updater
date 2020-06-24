
package com.ota.updates.updater.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Package {

    @SerializedName("file")
    @Expose
    private String file;
    @SerializedName("size")
    @Expose
    private Integer size;
    @SerializedName("md5")
    @Expose
    private String md5;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "Package{" +
                "file='" + file + '\'' +
                ", size=" + size +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
