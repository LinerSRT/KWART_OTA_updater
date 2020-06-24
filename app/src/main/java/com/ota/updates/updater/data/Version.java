
package com.ota.updates.updater.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Version {

    @SerializedName("general")
    @Expose
    private Integer general;
    @SerializedName("sub")
    @Expose
    private Integer sub;
    @SerializedName("extra")
    @Expose
    private Integer extra;

    public Integer getGeneral() {
        return general;
    }

    public void setGeneral(Integer general) {
        this.general = general;
    }

    public Integer getSub() {
        return sub;
    }

    public void setSub(Integer sub) {
        this.sub = sub;
    }

    public Integer getExtra() {
        return extra;
    }

    public void setExtra(Integer extra) {
        this.extra = extra;
    }

}
