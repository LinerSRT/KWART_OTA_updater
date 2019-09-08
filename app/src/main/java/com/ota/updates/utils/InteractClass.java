package com.ota.updates.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.activities.SettingsActivity;
import com.ota.updates.download.DownloadRom;
import com.ota.updates.download.DownloadRomProgress;
import com.ota.updates.views.OTADialog;

import static com.ota.updates.tasks.Changelog.TAG;
import static com.ota.updates.utils.Constants.DEBUGGING;
import static com.ota.updates.utils.Constants.MANIFEST_LOADED;
import static com.ota.updates.utils.Constants.OTA_DOWNLOAD_DIR;
import static com.ota.updates.utils.Constants.WIFI_ONLY;

public class InteractClass{
    private static Context context;
    private static OTADialog installDialog;
    private OTADialog networkDialog;
    private static MD5Checker md5Checker;
    private static InteractInterface interactInterface;


    public static NumberProgressBar mProgressBar;
    public static TextView mProgressCounterText, descriptionText;
    private DownloadRom mDownloadRom;
    private Installer installer;
    private boolean downloadFinished, downloadIsRunning, md5HasRun, md5Passed;
    private RelativeLayout downloadLayout;
    private LinearLayout interactLayout;

    public void serInteractListener(InteractInterface interactlistener){
        interactInterface = interactlistener;
    }

    public InteractClass(final Context context, boolean downloadFinished, boolean downloadIsRunning,
                         boolean md5HasRun, boolean md5Passed, Installer installer,
                         DownloadRom downloadRom, TextView mProgressCounterText, TextView descriptionText,
                         NumberProgressBar mProgressBar, RelativeLayout downloadLayout, LinearLayout interactLayout){
        this.context = context;
        this.downloadFinished = downloadFinished;
        this.downloadIsRunning = downloadIsRunning;
        this.md5HasRun = md5HasRun;
        this.md5Passed = md5Passed;
        this.installer = installer;
        this.mDownloadRom = downloadRom;
        this.mProgressCounterText = mProgressCounterText;
        this.descriptionText = descriptionText;
        this.mProgressBar = mProgressBar;
        this.downloadLayout = downloadLayout;
        this.interactLayout = interactLayout;
        md5Checker = new MD5Checker(context);


        installDialog = new OTADialog(context, "Установить?", RomUpdate.getChangelog(context),101, context.getString(R.string.cancel), " ", context.getString(R.string.install));
        networkDialog = new OTADialog(context, context.getString(R.string.available_wrong_network_title), context.getString(R.string.available_wrong_network_message), 001,
                " ", " ", context.getString(R.string.settings));
        networkDialog.setOkBtn(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networkDialog.close();
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
            }
        });
    }


    public static void setDescriptionText(String descriptionText) {
        installDialog.setDialogText(descriptionText);
    }

    public void installUpdate(){
        installDialog.setOkBtn(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = "/sdcard"+OTA_DOWNLOAD_DIR+"/"+RomUpdate.getFilename(context)+".zip";
                Toast.makeText(context, "INST "+filename, Toast.LENGTH_SHORT).show();
                //Installer.flashFiles(AvailableActivity.this, filename, false, false, false);
            }
        });
        installDialog.setNegativeBtn(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installDialog.close();
            }
        });




        installDialog.show();


    }

    public void startDownload(){
        interactLayout.setVisibility(View.GONE);
        downloadLayout.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
        if (Preferences.getIsDownloadOnGoing(context)) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            new DownloadRomProgress(context, downloadManager).execute();
        }
        if (!downloadIsRunning){
            String httpUrl = RomUpdate.getHttpUrl(context);
            String directUrl = RomUpdate.getDirectUrl(context);
            boolean isMobile = Utils.isMobileNetwork(context);
            boolean isSettingWiFiOnly = Preferences.getNetworkType(context).equals(WIFI_ONLY);
            if (isMobile && isSettingWiFiOnly) {
                networkDialog.show();
            } else {
                // We're good, open links or start downloads
                boolean directUrlEmpty = directUrl.equals("null") || directUrl.isEmpty();
                boolean httpUrlEmpty = httpUrl.equals("null") || httpUrl.isEmpty();
                if (directUrlEmpty) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(httpUrl));
                    context.startActivity(intent);
                } else if (httpUrlEmpty || !directUrlEmpty) {
                    mDownloadRom.startDownload(context);
                    /////////////////////////////////////////////////////////////setupUpdateNameInfo();
                }
            }









        }




    }


    @SuppressLint("SetTextI18n")
    public static void updateDownloadStatus(int progress, int downloaded, int total){
        mProgressBar.setProgress(progress);
        mProgressCounterText.setText(
                Utils.formatDataFromBytes(downloaded) +
                        "/" +
                        Utils.formatDataFromBytes(total));
    }

    public static void onDownloadFinish(){
        Resources res = context.getResources();
        boolean downloadFinished = Preferences.getDownloadFinished(context);
        if (downloadFinished) {
            String ready = context.getResources().getString(R.string.available_ready_to_install);
            int color = context.getResources().getColor(R.color.green_accent_color_dark);

            if(descriptionText != null) {
                descriptionText.setTextColor(color);
                descriptionText.setText(ready);
            }
            if(mProgressBar != null) {
                mProgressBar.setProgress(100);
            }






            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // NEED TO UPDATE BUTTON STATE AFTER DOWNLOAD AND MD5 CHECK
                    if(interactInterface != null)
                        interactInterface.onDownloadFinished();
                    md5Checker.cancel(true);
                }
            }, 1000);




        } else {
            int fileSize = RomUpdate.getFileSize(context);
            String fileSizeStr = Utils.formatDataFromBytes(fileSize);
            if(mProgressCounterText != null) {
                mProgressCounterText.setText(fileSizeStr);
            }
            if(mProgressBar != null) {
                mProgressBar.setProgress(0);
            }
        }
    }

    public void deleteUpdate(){
        Utils.deleteFile(RomUpdate.getFullFile(context));
        Preferences.setHasMD5Run(context, false);
        Preferences.setDownloadFinished(context, false);
        context.sendBroadcast(new Intent(MANIFEST_LOADED));
    }

    public void networkCheck(){

        boolean isMobile = Utils.isMobileNetwork(context);
        boolean isSettingWiFiOnly = Preferences.getNetworkType(context).equals(WIFI_ONLY);
        if (isMobile && isSettingWiFiOnly) {
            networkDialog.show();
        }


    }







    public class MD5Checker extends AsyncTask<Object, Boolean, Boolean> {

        public final String TAG = this.getClass().getSimpleName();

        Context mContext;

        public MD5Checker(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String file = RomUpdate.getFullFile(mContext).getAbsolutePath(); // Full file, with path
            String md5Remote = RomUpdate.getMd5(mContext); // Remote MD5 form the manifest. This is what we expect it to be
            String md5Local = Tools.shell("md5sum " + file + " | cut -d ' ' -f 1", false); // Run the check on our local file
            md5Local = md5Local.trim(); // Trim both to remove any whitespace
            md5Remote = md5Remote.trim();
            return md5Local.equalsIgnoreCase(md5Remote); // Return the comparison
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(mContext, mContext.getString(R.string.available_md5_ok), Toast.LENGTH_LONG).show();
                // If nd5 check ok dialogOkBtn.setText(context.getResources().getString(R.string.install));
                // If nd5 check ok dialogOkBtn.setOnClickListener(new View.OnClickListener() {
                // If nd5 check ok     @Override
                // If nd5 check ok     public void onClick(View view) {
                // If nd5 check ok         String filename = "/sdcard"+OTA_DOWNLOAD_DIR+"/"+RomUpdate.getFilename(context)+".zip";
                // If nd5 check ok         Toast.makeText(context, "Installing "+filename, Toast.LENGTH_SHORT).show();
                // If nd5 check ok         //Installer.flashFiles(context, filename, false, false, false);
                // If nd5 check ok     }
                // If nd5 check ok });

            } else {
                Toast.makeText(mContext, mContext.getString(R.string.available_md5_failed), Toast.LENGTH_LONG).show();
            }

            Preferences.setMD5Passed(mContext, result); // Set value for other persistent settings
            super.onPostExecute(result);
        }
    }



}
