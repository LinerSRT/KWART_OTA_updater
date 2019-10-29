package com.ota.updates.OTAManager;

import android.annotation.SuppressLint;
import android.content.Context;

public class OTAItem {
    @SuppressLint("StaticFieldLeak")
    private static OTAItem otaItem;
    private OTAPreferenceManager otaPreferenceManager;

    private String TAG_OTA_DEVICE_NAME      = "device_name";
    private String TAG_OTA_VERSION          = "ota_version";
    private String TAG_OTA_SUBVERSION       = "ota_subversion";
    private String TAG_OTA_EXTRAVERSION     = "ota_extraversion";
    private String TAG_OTA_UPDATE_DATE      = "update_date";
    private String TAG_OTA_FILENAME         = "filename";
    private String TAG_OTA_FILESIZE         = "file_size";
    private String TAG_OTA_FILEMD5          = "file_md5";
    private String TAG_OTA_DOWNLOAD_URL     = "download_url";
    private String TAG_OTA_CHANGELOG        = "changelog";

    private String TAG_OTA_AVAILABLE            = "ota_updateAvailable";
    private String TAG_OTA_DOWNLOAD_ID          = "ota_downloadID";
    private String TAG_OTA_MD5PASS              = "ota_md5pass";
    private String TAG_OTA_DOWNLOADING          = "ota_downloading";
    private String TAG_OTA_DOWNLOAD_FINISHED    = "ota_downloadFinished";
    private String TAG_OTA_LAST_CHECKED_TIME    = "last_checked";

    public static OTAItem getInstance(Context context) {
        if (otaItem == null) {
            otaItem = new OTAItem(context);
        }
        return otaItem;
    }

    private OTAItem(Context context){
        this.otaPreferenceManager = OTAPreferenceManager.getInstance(context);
    }

    void setUpdateAvailable(boolean available){
        otaPreferenceManager.saveBoolean(TAG_OTA_AVAILABLE, available);
    }

    void setOTADeviceName(String deviceName){
        otaPreferenceManager.saveString(TAG_OTA_DEVICE_NAME, deviceName);
    }

    void setOTAVersion(int version){
        otaPreferenceManager.saveInt(TAG_OTA_VERSION, version);
    }

    void setOTASubVersion(int subversion){
        otaPreferenceManager.saveInt(TAG_OTA_SUBVERSION, subversion);
    }

    void setOTAExtraVersion(int extraVersion){
        otaPreferenceManager.saveInt(TAG_OTA_EXTRAVERSION, extraVersion);
    }

    void setOTAUpdateDate(String updateDate){
        otaPreferenceManager.saveString(TAG_OTA_UPDATE_DATE, updateDate);
    }

    void setOTAFilename(String filename){
        otaPreferenceManager.saveString(TAG_OTA_FILENAME, filename);
    }

    void setOTAFilesize(int otaFilesize){
        otaPreferenceManager.saveInt(TAG_OTA_FILESIZE, otaFilesize);
    }

    void setOTAMD5(String otamd5){
        otaPreferenceManager.saveString(TAG_OTA_FILEMD5, otamd5);
    }

    void setOTADownloadURL(String otaDownloadURL){
        otaPreferenceManager.saveString(TAG_OTA_DOWNLOAD_URL, otaDownloadURL);
    }

    void setOTAChangelog(String changelog){
        otaPreferenceManager.saveString(TAG_OTA_CHANGELOG, changelog);
    }

    public boolean isOTAUpdateAvailable(){
        return otaPreferenceManager.getBoolean(TAG_OTA_AVAILABLE, false);
    }

    String getOTADeviceName(){
        return otaPreferenceManager.getString(TAG_OTA_DEVICE_NAME, "null");
    }

    int getOTAVersion(){
        return otaPreferenceManager.getInt(TAG_OTA_VERSION, 0);
    }
    int getOTASubVersion(){
        return otaPreferenceManager.getInt(TAG_OTA_SUBVERSION, 0);
    }
    int getOTAExtraVersion(){
        return otaPreferenceManager.getInt(TAG_OTA_EXTRAVERSION, 0);
    }
    String getOTAUpdateDate(){
        return otaPreferenceManager.getString(TAG_OTA_UPDATE_DATE, "null");
    }
    String getOTAFilename(){
        return otaPreferenceManager.getString(TAG_OTA_FILENAME, "null");
    }
    int getOTAFileSize(){
        return otaPreferenceManager.getInt(TAG_OTA_FILESIZE, 0);
    }
    String getOTAMD5(){
        return otaPreferenceManager.getString(TAG_OTA_FILEMD5, "null");
    }
    String getOTADownloadURL(){
        return otaPreferenceManager.getString(TAG_OTA_DOWNLOAD_URL, "null");
    }
    String getOTAChangelog(){
        return otaPreferenceManager.getString(TAG_OTA_CHANGELOG, "null");
    }

    void setDownloadRunningStatus(boolean isDownloading){
        otaPreferenceManager.saveBoolean(TAG_OTA_DOWNLOADING, isDownloading);
    }

    boolean getDownloadRunningStatus(){
        return otaPreferenceManager.getBoolean(TAG_OTA_DOWNLOADING, false);
    }

    void setDownloadFinishStatus(boolean isDownloading){
        otaPreferenceManager.saveBoolean(TAG_OTA_DOWNLOAD_FINISHED, isDownloading);
    }

    boolean getDownloadFinishStatus(){
        return otaPreferenceManager.getBoolean(TAG_OTA_DOWNLOAD_FINISHED, false);
    }

    void setDownloadID(long downloadID){
        otaPreferenceManager.saveLong(TAG_OTA_DOWNLOAD_ID, downloadID);
    }

    long getDownloadID(){
        return  otaPreferenceManager.getLong(TAG_OTA_DOWNLOAD_ID, 0);
    }

    void setMD5Status(boolean passed){
        otaPreferenceManager.saveBoolean(TAG_OTA_MD5PASS, passed);
    }

    boolean getMD5Status(){
        return otaPreferenceManager.getBoolean(TAG_OTA_MD5PASS, false);
    }

    void setLastCheckedTime(String time){
        otaPreferenceManager.saveString(TAG_OTA_LAST_CHECKED_TIME, time);
    }

    String getLastCheckedTime(){
        return otaPreferenceManager.getString(TAG_OTA_LAST_CHECKED_TIME, "Нет данных");
    }
}
