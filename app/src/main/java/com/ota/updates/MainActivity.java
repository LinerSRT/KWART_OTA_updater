package com.ota.updates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ota.updates.OTAManager.OTADownloadActivity;
import com.ota.updates.OTAManager.OTAItem;
import com.ota.updates.OTAManager.OTAManager;
import com.ota.updates.OTAManager.OTAManagerInterface;
import com.ota.updates.utils.AnimationUtils;
import com.ota.updates.utils.ThemeManager;
import com.ota.updates.utils.Utils;
import com.ota.updates.views.LinerDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private TextView updateCheckingText, systemVersionText, availableOTAVersionText;
    private Button checkUpdates;
    private LinearLayout OTANoUpdateLayout, OTAUpdateLayout;

    private OTAItem ota;
    private OTAManager otaManager;
    private boolean haveInetAcces = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.initTheme(this);
        setContentView(R.layout.check_ota_layout);
        List<String> permissionsRequired = new ArrayList<>();
        permissionsRequired.add(Manifest.permission.INTERNET);
        permissionsRequired.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissionsRequired.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissionsRequired.add(Manifest.permission.CHANGE_WIFI_STATE);
        permissionsRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsRequired.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionsRequired.add(Manifest.permission.READ_PHONE_STATE);
        ActivityCompat.requestPermissions(this, permissionsRequired.toArray(new String[0]), 120);
        Utils.requestRoot();
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
                if(!available){
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            AnimationUtils.toggleViewAnimation(OTAUpdateLayout, false, true, "circle", 1);
                            AnimationUtils.toggleViewAnimation(updateCheckingText, false ,false, "circle", 1);
                            AnimationUtils.toggleViewAnimation(OTANoUpdateLayout, true, true, "circle", 300);
                            AnimationUtils.toggleViewAnimation(checkUpdates, true, true, "circle", 1000);
                            systemVersionText.setText("KWART "+otaManager.getDeviceName()+" "+otaManager.getSystemVersion());
                            checkUpdates.setText("Проверить");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            AnimationUtils.toggleViewAnimation(updateCheckingText, false ,false, "circle", 1);
                            AnimationUtils.toggleViewAnimation(OTANoUpdateLayout, false, true, "circle", 1);
                            AnimationUtils.toggleViewAnimation(checkUpdates, true, true, "circle", 1000);
                            AnimationUtils.toggleViewAnimation(OTAUpdateLayout, true, true, "circle", 300);
                            availableOTAVersionText.setText("Версия "+otaManager.getManifestVersion());
                            checkUpdates.setText("Детали");
                        }
                    });
                }
            }

            @Override
            public void onDownloadStarted() {

            }

            @Override
            public void onDownloadStopped() {

            }

            @Override
            public void onDownloadFinished() {

            }

            @Override
            public void onDownloadFailed() {

            }

            @Override
            public void onDownloading(int progress, String downloadedSize, String totalSize) {

            }

            @Override
            public void MD5Status(boolean passed) {

            }

            @Override
            public void noInternet() {
                haveInetAcces = false;
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        AnimationUtils.toggleViewAnimation(OTAUpdateLayout, false, true, "circle", 1);
                        AnimationUtils.toggleViewAnimation(updateCheckingText, false ,false, "circle", 1);
                        AnimationUtils.toggleViewAnimation(OTANoUpdateLayout, true, true, "circle", 300);
                        AnimationUtils.toggleViewAnimation(checkUpdates, true, true, "circle", 1000);
                        systemVersionText.setText("KWART "+otaManager.getDeviceName()+" "+otaManager.getSystemVersion());
                        checkUpdates.setText("Проверить");
                    }
                });
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
                            (MainActivity.this).finish();
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
        otaManager.setInterface(interfaceOTA);
        updateCheckingText = findViewById(R.id.update_check_text);
        systemVersionText = findViewById(R.id.device_modelversion_text);
        availableOTAVersionText = findViewById(R.id.versionAvailableText);
        OTANoUpdateLayout = findViewById(R.id.noUpdateLayout);
        OTAUpdateLayout = findViewById(R.id.haveUpdateLayout);
        checkUpdates = findViewById(R.id.check_for_ota);
        AnimationUtils.toggleViewAnimation(updateCheckingText, true, true, "circle", 300);
        otaManager.updateManifest();

        checkUpdates.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(haveInetAcces) {
                    if (!ota.isOTAUpdateAvailable()) {
                        AnimationUtils.toggleViewAnimation(OTANoUpdateLayout, false, true, "circle", 1);
                        AnimationUtils.toggleViewAnimation(OTAUpdateLayout, false, true, "circle", 1);
                        AnimationUtils.toggleViewAnimation(updateCheckingText, true, false, "circle", 300);
                        otaManager.updateManifest();
                    } else {
                        Intent downloadActivity = new Intent(MainActivity.this, OTADownloadActivity.class);
                        downloadActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(downloadActivity);
                    }
                } else {
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
                                (MainActivity.this).finish();
                            } else {
                                Intent settings = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                                startActivity(settings);
                                (MainActivity.this).finish();
                            }
                        }
                    });
                    linerDialog.show();
                }
            }
        });




    }
}