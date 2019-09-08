package com.ota.updates;

import android.Manifest;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ota.updates.download.DownloadRom;
import com.ota.updates.tasks.LoadUpdateManifest;
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
    private CompatibilityTask compatibilityTask;
    private InteractClass interactClass;
    private Installer installer;
    private DownloadRom mDownloadRom;

    //Booleans
    private boolean permissionGrant = false;
    private boolean isFirstRun = true;
    private boolean downloadFinished, downloadIsRunning, md5HasRun, md5Passed;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = PreferenceManager.getInstance(this);
        mDownloadRom = new DownloadRom();
        registerReceiver(manifestReceiver, new IntentFilter(MANIFEST_LOADED));
        initValues();
        initViews();
        checkPermissions();
        init();

        if(isFirstRun){
            preferenceManager.saveBoolean(FIRST_RUN, false);
            //Do some stuff when first run
        }

        File installAfterFlashDir = new File(SD_CARD
                + File.separator
                + OTA_DOWNLOAD_DIR
                + File.separator
                + INSTALL_AFTER_FLASH_DIR);
        //noinspection ResultOfMethodCallIgnored
        installAfterFlashDir.mkdirs();


        Utils.setHasFileDownloaded(this);
        getInformation();
        updateViews();




        interactClass = new InteractClass(MainActivity.this, downloadFinished, downloadIsRunning,
                md5HasRun, md5Passed, installer, mDownloadRom,downloadStatusText, descriptionText,
                downloadProgressBar, downloadLayout, interact_layout);
        interact_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {








                if(!(RomUpdate.getUpdateAvailability(MainActivity.this) || (!RomUpdate.getUpdateAvailability(MainActivity.this)) && Utils.isUpdateIgnored(MainActivity.this))){
                    //Если нет обновлений
                    new LoadUpdateManifest(MainActivity.this, true).execute();
                    updateViews();
                }

                if (Preferences.getDownloadFinished(MainActivity.this)) {
                    // Если есть обновление и оно загружно
                    interactClass.installUpdate();

                } else if (!Preferences.getIsDownloadOnGoing(MainActivity.this)) {
                    // Если есть обновление
                    interactClass.startDownload();
                }
                //interactClass.deleteUpdate();
            }
        });


        smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        interactClass.serInteractListener(new InteractInterface() {
            @Override
            public void onDownloadFinished() {
                new LoadUpdateManifest(MainActivity.this, true).execute();
                updateViews();
                interact_layout.setVisibility(View.VISIBLE);
                downloadLayout.setVisibility(View.GONE);
                interactText.setText(getString(R.string.main_download_completed_details));
            }
        });

    }


    private void updateViews(){

        downloadLayout.setVisibility(View.GONE);
        if (RomUpdate.getUpdateAvailability(this) ||
                (!RomUpdate.getUpdateAvailability(this)) && Utils.isUpdateIgnored(this)) {
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

    private void init(){
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
            compatibilityTask = new CompatibilityTask(this);
            compatibilityTask.execute();
        }
    }

    private void initValues(){
        isFirstRun = preferenceManager.getBoolean(FIRST_RUN, true);
        downloadFinished = Preferences.getDownloadFinished(this);
        downloadIsRunning = Preferences.getIsDownloadOnGoing(this);
        md5HasRun = Preferences.getHasMD5Run(this);
        md5Passed = Preferences.getMD5Passed(this);
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

    private BroadcastReceiver manifestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), MANIFEST_LOADED)) {
                getInformation();
                updateViews();
            }
        }
    };

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
    
    @Override
    protected void onStop() {
        unregisterReceiver(manifestReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        registerReceiver(manifestReceiver, new IntentFilter(MANIFEST_LOADED));
        super.onStart();
    }


    public class CompatibilityTask extends AsyncTask<Void, Boolean, Boolean> implements Constants {

        public final String TAG = this.getClass().getSimpleName();

        private Context mContext;
        private String mPropName;

        public CompatibilityTask(Context context) {
            this.mContext = context;
            mPropName = context.getResources().getString(R.string.prop_name);
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            return Utils.doesPropExist(mPropName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                new LoadUpdateManifest(mContext, true).execute();
                updateViews();
            }
            super.onPostExecute(result);
        }
    }

}
