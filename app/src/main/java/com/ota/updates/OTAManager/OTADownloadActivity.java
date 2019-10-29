package com.ota.updates.OTAManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ota.updates.R;
import com.ota.updates.utils.AnimationUtils;
import com.ota.updates.utils.ThemeManager;
import com.ota.updates.views.LinerDialog;

public class OTADownloadActivity extends Activity {
    private TextView downloadTextProgress;
    private TextView downloadFinishText;
    private NumberProgressBar downloadProgressBar;
    private Button downloadActionBtn;
    private LinearLayout downloadLayout;

    private OTAItem ota;
    private OTAManager otaManager;


    private boolean isOTADownloading = false;
    private boolean isOTADownloadFinish = false;
    private boolean isOTAManagerCanDownload = false;
    private boolean isOTAManagerCanInstallOTA = false;
    private boolean isOTAHasAlreadyDownloadAndCanInstall = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.initTheme(this);
        setContentView(R.layout.ota_detail_layout);
        otaManager = new OTAManager(this);
        ota = OTAItem.getInstance(this);
        final OTAManagerInterface interfaceOTA = new OTAManagerInterface() {
            @Override
            public void onManifestDownloaded() {

            }

            @Override
            public void onManifestDownloadStart() {

            }

            @Override
            public void updateAvailable(boolean available) {

            }

            @Override
            public void onDownloadStarted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AnimationUtils.toggleViewAnimation(downloadFinishText, false ,true, "circle", 500);
                        AnimationUtils.toggleViewAnimation(downloadLayout, true, true, "circle", 500);
                        downloadProgressBar.setProgress(0);
                        downloadTextProgress.setText(" ");
                        updateViews();
                    }
                });
            }

            @Override
            public void onDownloadStopped() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadProgressBar.setProgress(0);
                        AnimationUtils.toggleViewAnimation(downloadLayout, false, true, "circle", 500);
                        AnimationUtils.toggleViewAnimation(downloadFinishText, false ,true, "circle", 500);
                        updateViews();
                    }
                });
            }

            @Override
            public void onDownloadFinished() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AnimationUtils.toggleViewAnimation(downloadLayout, false, true, "circle", 10);
                        AnimationUtils.toggleViewAnimation(downloadFinishText, true ,true, "circle", 500);
                        updateViews();
                    }
                });
            }

            @Override
            public void onDownloadFailed() {

            }

            @Override
            public void onDownloading(final int progress, final String downloadedSize, final String totalSize) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        downloadProgressBar.setProgress(progress);
                        downloadTextProgress.setText(downloadedSize +"/"+ totalSize);
                    }
                });
            }

            @Override
            public void MD5Status(boolean passed) {

            }

            @Override
            public void noInternet() {

            }
        };
        otaManager.setInterface(interfaceOTA);
        TextView updateVersionText = findViewById(R.id.otaUpdateVersion);
        TextView changelogText = findViewById(R.id.changelogText);
        downloadProgressBar = findViewById(R.id.downloadProgress);
        downloadTextProgress = findViewById(R.id.downloadTextProgress);
        downloadActionBtn = findViewById(R.id.downloadActionBtn);
        downloadLayout = findViewById(R.id.downloadLayout);
        downloadFinishText = findViewById(R.id.downloadFinishedText);

        otaManager.updateManifest();
        updateVersionText.setText(otaManager.getManifestVersion());
        changelogText.setText(ota.getOTAChangelog());
        updateViews();


        downloadActionBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                otaManager.reset();
                AnimationUtils.toggleViewAnimation(downloadLayout, false, true, "circle", 500);
                AnimationUtils.toggleViewAnimation(downloadFinishText, false ,true, "circle", 500);
                downloadProgressBar.setProgress(0);
                downloadTextProgress.setText(" ");
                updateViews();
                return true;
            }
        });

    }

    private void refreshStatusValues(){
        boolean systemHaveUpdates = ota.isOTAUpdateAvailable();
        isOTADownloading = ota.getDownloadRunningStatus();
        isOTADownloadFinish = ota.getDownloadFinishStatus();
        isOTAManagerCanDownload = systemHaveUpdates && !isOTADownloading && !otaManager.getOTAFile(ota.getOTAFilename()).exists();
        isOTAManagerCanInstallOTA = systemHaveUpdates && isOTADownloadFinish && !isOTADownloading && ota.getMD5Status();
        isOTAHasAlreadyDownloadAndCanInstall = isOTAManagerCanInstallOTA && otaManager.getOTAFile(ota.getOTAFilename()).exists();
    }

    private void updateViews(){
        refreshStatusValues();
        if(isOTAManagerCanDownload){
            downloadActionBtn.setText("Загрузить");
            downloadActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    otaManager.downloadOTA();
                }
            });
        } else if (isOTAManagerCanInstallOTA || isOTAHasAlreadyDownloadAndCanInstall){
            AnimationUtils.toggleViewAnimation(downloadFinishText, true ,true, "circle", 500);
            downloadActionBtn.setText("Установить");
            downloadActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final LinerDialog linerDialog = new LinerDialog(OTADownloadActivity.this, "Установить?",
                            "Внимание, после подтверждения, Ваши часы немеделлно перезагрузятся!\n(Вы можете сбросить часы после установки, нажмите на кнопку 'Установить и сбросить')", true, true);
                    linerDialog.setupCancelBtn("Нет", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            linerDialog.close();
                        }
                    });

                    linerDialog.setupMiddleBtn("Установить и сбросить", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            linerDialog.close();
                            otaManager.installOTA(false, true, true);
                        }
                    });

                    linerDialog.setupOkBtn("Да", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            linerDialog.close();
                            otaManager.installOTA(false, false, false);

                        }
                    });
                    linerDialog.show();
                }
            });
        } else if (isOTADownloading && !isOTADownloadFinish){
            downloadActionBtn.setText("Отмена");
            downloadActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    otaManager.cancelDownloadOTA();
                    otaManager.deleteOTA(ota.getOTAFilename());
                }
            });
        }
    }
}
