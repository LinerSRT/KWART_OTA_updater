package com.ota.updates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.ota.updates.OTAManager.OTAItem;
import com.ota.updates.OTAManager.OTAManager;
import com.ota.updates.OTAManager.OTAManagerInterface;
import com.ota.updates.OTAManager.OTAPreferenceManager;
import com.ota.updates.utils.Utils;
import com.ota.updates.views.LinerDialog;
import com.ota.updates.views.OTADialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ota.updates.utils.Config.AMOLED_VERSION;
import static com.ota.updates.utils.Config.DEBUGGING;
import static com.ota.updates.utils.Config.TAG;

public class MainActivity extends Activity {
    private TextView descriptionText, interactText, lastUpdateText, downloadStatusText;
    private TextView aboutRomVersion;
    private TextView aboutRomLastUpdate;
    private TextView aboutDevice;
    private ImageView smile, interactIcon;
    private NumberProgressBar downloadProgressBar;
    private RelativeLayout downloadLayout;
    private LinearLayout interact_layout;



    private boolean systemHaveUpdates = false;
    private boolean isOTADownloading = false;
    private boolean isOTADownloadFinish = false;
    private boolean isOTAManagerCanDownload = false;
    private boolean isOTAManagerCanInstallOTA = false;

    private OTAManager otaManager;
    private OTAItem otaItem;
    private OTAPreferenceManager otaPreferenceManager;


    private void initTheme() {
            switch (android.provider.Settings.Global.getInt(getContentResolver(), "system_theme", 1)) {
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
        initViews();
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                permissionGranted = true;
            }
        });

        if(!permissionGranted){
            LinerDialog linerDialog = new LinerDialog(this, "Нет разрешений",
                    "Предоставьте разрешения для запуска", false, false);
            linerDialog.setupOkBtn(getResources().getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            linerDialog.show();
        }
        otaManager = new OTAManager(this);
        otaPreferenceManager = OTAPreferenceManager.getInstance(this);
        otaItem = OTAItem.getInstance(this);
        updateViews();
        OTAManagerInterface otaManagerInterface = new OTAManagerInterface() {
            @Override
            public void onManifestDownloaded() {
                updateViews();
            }

            @Override
            public void onManifestDownloadStart() {

            }

            @Override
            public void updateAvailable(boolean available) {
                if(available){
                    updateViews();
                }
            }

            @Override
            public void onDownloadStarted() {
                downloadProgressBar.setProgress(0);
                downloadStatusText.setText(" ");
                interact_layout.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDownloadStopped() {

            }

            @Override
            public void onDownloadFinished() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadProgressBar.setProgress(0);
                        downloadStatusText.setText(" ");
                        interact_layout.setVisibility(View.VISIBLE);
                        downloadLayout.setVisibility(View.GONE);
                        interactText.setText(getString(R.string.main_download_completed_details));
                        updateViews();
                    }
                });

            }

            @Override
            public void onDownloadFailed() {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloading(int progress, String downloadedSize, String totalSize) {
                downloadProgressBar.setProgress(progress);
                downloadStatusText.setText(downloadedSize +"/"+ totalSize);
            }

            @Override
            public void MD5Status(boolean passed) {

            }

            @Override
            public void noInternet() {
                final LinerDialog linerDialog = new LinerDialog(MainActivity.this, getResources().getString(R.string.main_not_connected_title),
                        getResources().getString(R.string.main_not_connected_message), false, true);
                linerDialog.setupCancelBtn(getResources().getString(R.string.close_app), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        linerDialog.close();
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
            }
        };
        otaManager.setInterface(otaManagerInterface);
        registerDevice(otaManager);
        otaManager.updateManifest();


        if (!Utils.isConnected(this)) {
            //linerDialog = new LinerDialog(this, getResources().getString(R.string.main_not_connected_title),
            //        getResources().getString(R.string.main_not_connected_message), false, true);
            //linerDialog.setupCancelBtn(getResources().getString(R.string.close_app), new View.OnClickListener() {
            //    @Override
            //    public void onClick(View view) {
            //        linerDialog.close();
            //        (MainActivity.this).finish();
            //    }
            //});
            //linerDialog.setupOkBtn(getResources().getString(R.string.settings_btn), new View.OnClickListener() {
            //    @Override
            //    public void onClick(View view) {
            //        if (Build.VERSION.SDK_INT < 24) {
            //            Intent settings = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            //            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //            startActivity(settings);
            //        } else {
            //            Intent settings = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            //            startActivity(settings);
            //            (MainActivity.this).finish();
            //        }
            //    }
            //});
            //linerDialog.show();
        }


        interact_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateViews();
              if(!systemHaveUpdates){
                  otaManager.updateManifest();
              }
              if (isOTAManagerCanInstallOTA) {
                  otaManager.installOTA(false, false, false);

              } else if (isOTAManagerCanDownload) {
                  otaManager.downloadOTA();
              }
            }
        });

        interact_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                otaManager.reset();
                return false;
            }
        });

    }

    private void registerDevice(OTAManager otaManager){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        assert telephonyManager != null;
        @SuppressLint({"MissingPermission", "HardwareIds"}) String deviceId = telephonyManager.getDeviceId();
        String deviceName = otaItem.getOTADeviceName();
        if(deviceName.equals("null")){
            deviceName = otaManager.getProp("ro.product.device");
        }
        String osVersion = otaManager.getSystemVersion();
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
        netRequest.setRequestUri(otaManager.getDecodedServerURL()+"/index.php");
        netRequest.load();
    }

    @SuppressLint("SetTextI18n")
    private void updateViews(){
        systemHaveUpdates = otaItem.isOTAUpdateAvailable();
        isOTADownloading = otaItem.getDownloadRunningStatus();
        isOTADownloadFinish = otaItem.getDownloadFinishStatus();
        isOTAManagerCanDownload = systemHaveUpdates && !isOTADownloading;
        isOTAManagerCanInstallOTA = systemHaveUpdates && isOTADownloadFinish && !isOTADownloading && otaItem.getMD5Status();

        Log.d("OTAManager", "Have update: "+systemHaveUpdates+" Can install:"+isOTAManagerCanInstallOTA
                +" Can download: "+isOTAManagerCanDownload+" Download finished: "+isOTADownloadFinish
                +" MD5 Passed: "+otaItem.getMD5Status());
        aboutRomLastUpdate.setText(" "+otaItem.getOTAUpdateDate());
        aboutRomVersion.setText(" "+otaManager.getSystemVersion());
        aboutDevice.setText(getResources().getString(R.string.kwart_watch)+" "+otaManager.getDeviceName());
        downloadLayout.setVisibility(View.GONE);
        if (systemHaveUpdates) {
            interactIcon.setImageDrawable(getDrawable(R.drawable.ic_update_download_done));
            smile.setImageDrawable(getDrawable(R.drawable.ic_update_available));

            if (isOTADownloadFinish) { //  Update already finished?
                descriptionText.setText(getResources().getString(R.string.main_update_finished));
                interactText.setText(getResources().getString(R.string.main_download_completed_details));
            } else if (isOTADownloading) {
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
            String time;
            if (is24) {
                time = new SimpleDateFormat("d MMMM HH:mm", locale).format(now);
            } else {
                time = new SimpleDateFormat("d MMMM hh:mm a", locale).format(now);
            }
            otaItem.setLastCheckedTime(time);
            lastUpdateText.setText(getResources().getString(R.string.last_checked) + time);
        }
    }

    private void initViews(){
        descriptionText = findViewById(R.id.description);
        interactText = findViewById(R.id.interact_text);
        lastUpdateText = findViewById(R.id.last_update_text);
        downloadStatusText = findViewById(R.id.download_status);
        smile = findViewById(R.id.smile);
        interactIcon = findViewById(R.id.interact_img);
        downloadProgressBar = findViewById(R.id.download_progress_bar);
        downloadLayout = findViewById(R.id.download_layout);
        interact_layout = findViewById(R.id.interact_layout);
        aboutDevice = findViewById(R.id.about_device_text);
        aboutRomLastUpdate = findViewById(R.id.about_rom_last_update);
        aboutRomVersion = findViewById(R.id.about_rom_version);
    }

}
