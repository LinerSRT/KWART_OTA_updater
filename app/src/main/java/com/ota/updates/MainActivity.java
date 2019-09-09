package com.ota.updates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NativeActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Installer;
import com.ota.updates.utils.InteractClass;
import com.ota.updates.utils.InteractInterface;
import com.ota.updates.utils.PreferenceManager;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.ota.updates.utils.Constants.*;

public class MainActivity extends Activity {
    //Include Views
    private TextView descriptionText, interactText, lastUpdateText, downloadStatusText;
    private TextView aboutDevice, aboutRomVersion, aboutRomLastUpdate;

    private ImageView smile, interactIcon;
    private NumberProgressBar downloadProgressBar;
    private RelativeLayout downloadLayout;
    private LinearLayout interact_layout;

    //Include util
    PreferenceManager preferenceManager;
    private static InteractClass interactClass;
    private Installer installer;

    //Booleans
    private boolean permissionGrant = false;
    private boolean isFirstRun = true;
    private boolean canInstall = false;
    private boolean canDownload = true;
    private boolean haveUpdates = false;


    public static InteractClass getInteractClass(){
        return interactClass;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkPermissions();
        File installAfterFlashDir = new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + INSTALL_AFTER_FLASH_DIR);
        installAfterFlashDir.mkdirs();
        Utils.setHasFileDownloaded(this);
        updateViews();
        updateButtonStat();
        interactClass = new InteractClass(MainActivity.this);

        if (!Utils.isConnected(this)) {
            AlertDialog.Builder notConnectedDialog = new AlertDialog.Builder(this);
            notConnectedDialog.setTitle(R.string.main_not_connected_title)
                    .setMessage(R.string.main_not_connected_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            (MainActivity.this).finish();
                        }
                    })
                    .show();
        } else {
            interactClass.updateManifest(false);
        }


        interactClass.serInteractListener(new InteractInterface() {
            @Override
            public void onDownloadFinished() {
                updateViews();
                downloadProgressBar.setProgress(0);
                downloadStatusText.setText(" ");
                interact_layout.setVisibility(View.VISIBLE);
                downloadLayout.setVisibility(View.GONE);
                interactText.setText(getString(R.string.main_download_completed_details));
            }

            @Override
            public void onDownloadStarted() {
                downloadProgressBar.setProgress(0);
                downloadStatusText.setText(" ");
                interact_layout.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.VISIBLE);
                Log.d(TAG+"Downloader", "Download started!");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadProgressChanched(Integer... progress) {
                downloadProgressBar.setProgress(progress[0]);
                downloadStatusText.setText(Utils.formatDataFromBytes(progress[1]) +"/" +Utils.formatDataFromBytes(progress[2]));

                Log.d(TAG+"Downloader", "Download changed!");
            }

            @Override
            public void onDownloadStopped() {
                Log.d(TAG+"Downloader", "Download stopped!");
            }

            @Override
            public void onMD5Checked(boolean status) {
                Log.d(TAG+"MD5Checker", "Check status - "+status);
            }

            @Override
            public void needUpdateManifest() {

            }

            @Override
            public void onManifestUpdated() {
                Log.v(TAG+"MANIFEST", "Downloaded");
                getInformation();
                updateViews();
                updateButtonStat();
                updateViews();
            }

            @Override
            public void onManifestDownloaded() {

            }

            @Override
            public void onOTADeleted() {
                updateButtonStat();
                getInformation();
                updateViews();
                Log.d(TAG+"Manager", "OTADeleted");
            }
        });
        interact_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!haveUpdates){
                    interactClass.updateManifest(false);
                }
                if (canInstall) {
                    interactClass.installOTA();

                } else if (canDownload) {
                    interactClass.downloadOTA();
                }
                Log.d(TAG+"Button", "Status = HU "+haveUpdates+" | CI "+canInstall+" | CD "+canDownload);
            }
        });

        //DEBUG
        smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interactClass.updateManifest(false);
                getInformation();
                updateViews();
            }
        });
        smile.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                interactClass.deleteUpdate();
                return false;
            }
        });
        //DEBUG



    }


    private void updateButtonStat(){
        haveUpdates = RomUpdate.getUpdateAvailability(this) || (!RomUpdate.getUpdateAvailability(this)) && Utils.isUpdateIgnored(this);
        canDownload = haveUpdates && !Preferences.getDownloadFinished(this) && !Preferences.getIsDownloadOnGoing(this);
        canInstall = Preferences.getDownloadFinished(this) && haveUpdates && Preferences.getMD5Passed(this);

        Log.d(TAG+"Button", "Status = HU "+haveUpdates+" | CI "+canInstall+" | CD "+canDownload);
    }

    private void updateViews(){

        downloadLayout.setVisibility(View.GONE);
        if (haveUpdates) {
            interactIcon.setImageDrawable(getDrawable(R.drawable.ic_update_download_done));
            smile.setImageDrawable(getDrawable(R.drawable.ic_update_available));

            if (Preferences.getDownloadFinished(this)) { //  Update already finished?
                descriptionText.setText(getResources().getString(R.string.main_update_finished));
                interactText.setText(getResources().getString(R.string.main_download_completed_details));
            } else if (Preferences.getIsDownloadOnGoing(this)) {
                downloadLayout.setVisibility(View.VISIBLE);
                interact_layout.setVisibility(View.GONE);
            } else {
                smile.setImageDrawable(getDrawable(R.drawable.ic_update_available));
                descriptionText.setText(getResources().getString(R.string.main_update_available));
                interactIcon.setImageDrawable(getDrawable(R.drawable.ic_download));
                interactText.setText(getResources().getString(R.string.main_tap_to_download));
                int color = getResources().getColor(R.color.text_color);
                descriptionText.setTextColor(color);

            }
        } else {
            smile.setImageDrawable(getDrawable(R.drawable.ic_update_not_available));
            descriptionText.setText(getString(R.string.main_no_update_available));
            downloadLayout.setVisibility(View.GONE);
            interactText.setText("Нажмите что бы проверить");
            interactIcon.setImageDrawable(getDrawable(R.drawable.ic_check));

            boolean is24 = DateFormat.is24HourFormat(this);
            Date now = new Date();
            Locale locale = Locale.getDefault();
            String time = "";
            if (is24) {
                time = new SimpleDateFormat("d MMMM HH:mm", locale).format(now);
            } else {
                time = new SimpleDateFormat("d MMMM hh:mm a", locale).format(now);
            }
            Preferences.setUpdateLastChecked(this, time);
            lastUpdateText.setText("Последняя проверка: " + time);
        }
    }

    private void getInformation(){
        String romUpdateTime = RomUpdate.getUpdateDate(this);
        aboutRomLastUpdate.setText(romUpdateTime);
        String romVersionActual = Utils.getProp(OTA_VERSION);
        aboutRomVersion.setText(romVersionActual);
    }

    private void initViews(){
        descriptionText = (TextView) findViewById(R.id.description);
        interactText = (TextView) findViewById(R.id.interact_text);
        lastUpdateText = (TextView) findViewById(R.id.last_update_text);
        downloadStatusText = (TextView) findViewById(R.id.download_status);

        smile = (ImageView) findViewById(R.id.smile);
        interactIcon = (ImageView) findViewById(R.id.interact_img);

        downloadProgressBar = (NumberProgressBar) findViewById(R.id.download_progress_bar);
        downloadLayout = (RelativeLayout) findViewById(R.id.download_layout);
        interact_layout = (LinearLayout) findViewById(R.id.interact_layout);

        aboutDevice = (TextView) findViewById(R.id.about_device_text);
        aboutRomLastUpdate = (TextView) findViewById(R.id.about_rom_last_update);
        aboutRomVersion = (TextView) findViewById(R.id.about_rom_version);
    }

    private void checkPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,	String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGrant = true;
                } else {
                    permissionGrant = false;
                }
                return;
            }
        }
    }
}
