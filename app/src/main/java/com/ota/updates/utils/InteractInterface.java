package com.ota.updates.utils;

public interface InteractInterface {

    void onDownloadFinished();
    void onDownloadStarted();
    void onDownloadProgressChanched(Integer... progress);
    void onDownloadStopped();


    void onMD5Checked(boolean status);

    void needUpdateManifest();
    void onManifestUpdated();
    void onManifestDownloaded();

    void onOTADeleted();

}
