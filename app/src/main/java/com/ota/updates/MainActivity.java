package com.ota.updates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.thunder413.netrequest.NetError;
import com.github.thunder413.netrequest.NetRequest;
import com.github.thunder413.netrequest.NetResponse;
import com.github.thunder413.netrequest.OnNetResponse;
import com.github.thunder413.netrequest.RequestMethod;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.ota.updates.utils.InteractClass;
import com.ota.updates.utils.InteractInterface;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Utils;
import com.ota.updates.views.LinerDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ota.updates.utils.Config.AMOLED_VERSION;
import static com.ota.updates.utils.Config.DEBUGGING;
import static com.ota.updates.utils.Config.INSTALL_AFTER_FLASH_DIR;
import static com.ota.updates.utils.Config.OTA_DOWNLOAD_DIR;
import static com.ota.updates.utils.Config.OTA_VERSION;
import static com.ota.updates.utils.Config.SD_CARD;
import static com.ota.updates.utils.Config.TAG;

public class MainActivity extends Activity {
    private TextView descriptionText, interactText, lastUpdateText, downloadStatusText;
    private TextView aboutRomVersion;
    private TextView aboutRomLastUpdate;
    private ImageButton toSettingsBtn;
    private ImageView smile, interactIcon;
    private NumberProgressBar downloadProgressBar;
    private RelativeLayout downloadLayout;
    private LinearLayout interact_layout;
    private static InteractClass interactClass;
    private boolean canInstall = false;
    private boolean canDownload = true;
    private boolean haveUpdates = false;
    private LinerDialog linerDialog;

    public static InteractClass getInteractClass() {
        return interactClass;
    }

    private void initTheme() {
        if (RomUpdate.getRomName(this).equals("Advance")) {
            setTheme(R.style.AdvanceTheme);
        } else {
            switch (android.provider.Settings.Global.getInt(getContentResolver(), "system_theme", 4)) {
                case 0:
                    setTheme(R.style.AppTheme);
                    break;
                case 1:
                    setTheme(R.style.BlueDeepTheme);
                    break;
                case 2:
                    setTheme(R.style.RedDeepTheme);
                    break;
                case 3:
                    setTheme(R.style.GreenDeepTheme);
                    break;
                case 4:
                    setTheme(R.style.DarkTheme);
                    break;
            }
        }
    }

    private boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
        if (AMOLED_VERSION) {
            setContentView(R.layout.activity_main_amoled);
        } else {
            setContentView(R.layout.activity_main);
        }
        if (DEBUGGING)
            Utils.deleteObjectByPath("/sdcard/" + OTA_DOWNLOAD_DIR);

        initViews();

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                permissionGranted = true;
            }
        });

        if(!permissionGranted){
            linerDialog = new LinerDialog(this, "Нет разрешений",
                    getResources().getString(R.string.main_not_connected_message), false, false);
            linerDialog.setupOkBtn(getResources().getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            linerDialog.show();
        }

        Utils.setHasFileDownloaded(this);

        updateValues();
        updateViews();
        interactClass = new InteractClass(MainActivity.this);

        if (!Utils.isConnected(this)) {
            linerDialog = new LinerDialog(this, getResources().getString(R.string.main_not_connected_title),
                    getResources().getString(R.string.main_not_connected_message), false, true);
            linerDialog.setupCancelBtn(getResources().getString(R.string.close_app), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linerDialog.close();
                    (MainActivity.this).finish();
                }
            });
            linerDialog.setupOkBtn(getResources().getString(R.string.settings_btn), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT < 24) {
                        Intent settings = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settings);
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivity(settings);
                        (MainActivity.this).finish();
                    }
                }
            });
            linerDialog.show();
        } else {
            interactClass.updateManifest(false);
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            assert telephonyManager != null;
            @SuppressLint({"MissingPermission", "HardwareIds"}) String deviceId = telephonyManager.getDeviceId();
            String deviceName = RomUpdate.getRomName(this);
            if(deviceName == null){
                deviceName = Utils.getProp("ro.product.device");
            }
            String osVersion = Utils.getProp(OTA_VERSION);
            NetRequest netRequest = new NetRequest(this);
            netRequest.addParameter("device_id",String.valueOf(deviceId));
            netRequest.addParameter("device_name",String.valueOf(deviceName));
            netRequest.addParameter("os_version",String.valueOf(osVersion));
            netRequest.setRequestMethod(RequestMethod.GET);
            netRequest.setOnResponseListener(new OnNetResponse() {
                @Override
                public void onNetResponseCompleted(NetResponse netResponse) {

                }

                @Override
                public void onNetResponseError(NetError netError) {

                }
            });
            netRequest.setRequestUri("http://18.222.210.219/index.php");
            netRequest.load();
        }



        interact_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!haveUpdates){
                    interactClass.updateManifest(false);
                }
                if (canInstall) {
                    interactClass.installOTA(false, false, false);

                } else if (canDownload) {
                    interactClass.downloadOTA();
                }
            }
        });

        toSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT < 24) {
                    Intent intent = new Intent(MainActivity.this, Settings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, Settings.class);
                    startActivity(intent);
                }
            }
        });


        interactClass.serInteractListener(new InteractInterface() {
            @Override
            public void onDownloadFinished() {
                updateValues();
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
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadProgressChanched(Integer... progress) {
                downloadProgressBar.setProgress(progress[0]);
                downloadStatusText.setText(Utils.formatDataFromBytes(progress[1]) +"/" +Utils.formatDataFromBytes(progress[2]));
            }

            @Override
            public void onDownloadStopped() {
                Log.d(TAG+"Downloader", "Download stopped!");
            }

            @Override
            public void onMD5Checked(boolean status) {

                updateValues();
            }

            @Override
            public void needUpdateManifest() {

            }

            @Override
            public void onManifestUpdated() {
                getInformation();
                updateValues();
                updateViews();
            }

            @Override
            public void onManifestDownloaded() {

            }

            @Override
            public void onOTADeleted() {
                updateValues();
                getInformation();
                updateViews();
            }
        });


    }


    private void updateValues(){
        haveUpdates = RomUpdate.getUpdateAvailability(this) || (!RomUpdate.getUpdateAvailability(this)) && Utils.isUpdateIgnored(this);
        canDownload = haveUpdates && !Preferences.getDownloadFinished(this) && !Preferences.getIsDownloadOnGoing(this);
        canInstall = Preferences.getDownloadFinished(this) && haveUpdates && Preferences.getMD5Passed(this);
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
                descriptionText.setTextColor(Color.GREEN);

            }
        } else {
            smile.setImageDrawable(getDrawable(R.drawable.ic_update_not_available));
            descriptionText.setTextColor(Utils.getAttrColor(this, R.attr.textColor));
            descriptionText.setText(getString(R.string.main_no_update_available));
            downloadLayout.setVisibility(View.GONE);
            interactText.setText(getResources().getString(R.string.tap_check_updates));
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

            lastUpdateText.setText(getResources().getString(R.string.last_checked) + time);
        }
    }

    private void getInformation(){
        String romUpdateTime = RomUpdate.getUpdateDate(this);
        aboutRomLastUpdate.setText(" "+romUpdateTime);
        String romVersionActual = Utils.getProp(OTA_VERSION);
        aboutRomVersion.setText(" "+romVersionActual);
    }

    private void initViews(){
        descriptionText = findViewById(R.id.description);
        interactText = findViewById(R.id.interact_text);
        lastUpdateText = findViewById(R.id.last_update_text);
        lastUpdateText.setText(getResources().getString(R.string.last_checked)+" "+Preferences.getUpdateLastChecked(this, "Нет данных"));
        downloadStatusText = findViewById(R.id.download_status);

        smile = findViewById(R.id.smile);
        interactIcon = findViewById(R.id.interact_img);

        downloadProgressBar = findViewById(R.id.download_progress_bar);
        downloadLayout = findViewById(R.id.download_layout);
        interact_layout = findViewById(R.id.interact_layout);

        TextView aboutDevice = findViewById(R.id.about_device_text);
        aboutDevice.setText(getResources().getString(R.string.kwart_watch)+" "+RomUpdate.getRomName(this));
        aboutRomLastUpdate = findViewById(R.id.about_rom_last_update);
        aboutRomVersion = findViewById(R.id.about_rom_version);
        toSettingsBtn = findViewById(R.id.to_settings_btn);
    }

}
