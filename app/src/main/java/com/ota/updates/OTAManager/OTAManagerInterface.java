package com.ota.updates.OTAManager;

public interface OTAManagerInterface {
    void onManifestDownloaded();
    void onManifestDownloadStart();
    void updateAvailable(boolean available);
    void onDownloadStarted();
    void onDownloadStopped();
    void onDownloadFinished();
    void onDownloadFailed();
    void onDownloading(int progress, String downloadedSize, String totalSize);
    void MD5Status(boolean passed);
    void noInternet();
}
