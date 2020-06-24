
package com.ota.updates.updater.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OTAPackage {

    @SerializedName("device")
    @Expose
    private String device;
    @SerializedName("version")
    @Expose
    private Version version;
    @SerializedName("package")
    @Expose
    private Package _package;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("changelog")
    @Expose
    private String changelog;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Package getPackage() {
        return _package;
    }

    public void setPackage(Package _package) {
        this._package = _package;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    @Override
    public String toString() {
        return "OTAPackage{" +
                "device='" + device + '\'' +
                ", version=" + version +
                ", _package=" + _package +
                ", date='" + date + '\'' +
                ", changelog='" + changelog + '\'' +
                '}';
    }
}
