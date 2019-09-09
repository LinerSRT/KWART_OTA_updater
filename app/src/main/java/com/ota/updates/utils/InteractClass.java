package com.ota.updates.utils;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.tasks.RomXmlParser;
import com.ota.updates.views.OTADialog;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static com.ota.updates.utils.Constants.OTA_DOWNLOAD_DIR;
import static com.ota.updates.utils.Constants.WIFI_ONLY;

public class InteractClass{
    private OTADialog installDialog;
    private Context context;


    public InteractClass(Context context){
        this.context = context;
    }


    public void updateManifest(boolean isForegroundUpdate){
        GetManifest getManifest = new GetManifest(context, isForegroundUpdate);
        getManifest.execute();
    }


    public void installOTA(final boolean backup, final boolean wipeCache, final boolean wipeData){
        installDialog = new OTADialog(context, RomUpdate.getVersionNumber(context),"Установить?",
                RomUpdate.getChangelog(context),101,
                context.getString(R.string.cancel), " ",
                context.getString(R.string.install));
        installDialog.setOkBtn(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("SdCardPath") String filename = "/sdcard/"+OTA_DOWNLOAD_DIR+"/"+RomUpdate.getFilename(context)+".zip";
                flashFiles(context, filename, backup, wipeCache, wipeData);
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


    public void downloadOTA(){
            startDownload(context);
    }

    public void cancelDownloadOTA(Context context) {
        long mDownloadID = Preferences.getDownloadID(context);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        assert downloadManager != null;
        downloadManager.remove(mDownloadID);
        Preferences.setIsDownloadRunning(context, false);
        interactInterface.onDownloadStopped();
    }

    public void deleteUpdate(){
        Utils.deleteFile(RomUpdate.getFullFile(context));
        Preferences.setMD5Passed(context, false);
        Preferences.setDownloadFinished(context, false);
        interactInterface.onOTADeleted();
    }

///////////////////////////////INSTALL PART////////////////////////////////////////
    private static void flashFiles(Context context, String file, boolean backup, boolean wipeCache, boolean wipeData) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("mkdir -p /cache/recovery/\n");
            os.writeBytes("rm -f /cache/recovery/command\n");
            os.writeBytes("rm -f /cache/recovery/extendedcommand\n");
            os.writeBytes("echo 'boot-recovery' >> /cache/recovery/command\n");
            if (backup) {
                os.writeBytes("echo '--nandroid' >> /cache/recovery/command\n");
            }
            if (wipeData) {
                os.writeBytes("echo '--wipe_data' >> /cache/recovery/command\n");
            }
            if (wipeCache) {
                os.writeBytes("echo '--wipe_cache' >> /cache/recovery/command\n");
            }
            os.writeBytes("echo '--update_package=" + file + "' >> /cache/recovery/command\n");

            String rebootCmd = "reboot recovery";
            os.writeBytes(rebootCmd + "\n");
            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            ((PowerManager) Objects.requireNonNull(context.getSystemService(Context.POWER_SERVICE))).reboot("recovery");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

///////////////////////////////DOWNLOAD PART///////////////////////////////////////
    private void startDownload(Context context) {
        String url = RomUpdate.getDirectUrl(context);
        String fileName = RomUpdate.getFilename(context) + ".zip";
        String description = context.getResources().getString(R.string.downloading);
        File file = RomUpdate.getFullFile(context);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if(Preferences.getNetworkType(context).equals(WIFI_ONLY)) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }
        request.setTitle(fileName);
        request.setDescription(description);
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(OTA_DOWNLOAD_DIR, fileName);
        Utils.deleteFile(file);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        assert downloadManager != null;
        long mDownloadID = downloadManager.enqueue(request);
        Preferences.setDownloadID(context, mDownloadID);
        Preferences.setIsDownloadRunning(context, true);
        new DownloadProgress(context, downloadManager).execute(mDownloadID);
        Preferences.setMD5Passed(context, false);
        Preferences.setHasMD5Run(context, false);
        interactInterface.onDownloadStarted();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadProgress extends AsyncTask<Long, Integer, Void> implements Constants {
        private Context mContext;
        private DownloadManager mDownloadManager;

        DownloadProgress(Context context, DownloadManager downloadManager) {
            mContext = context;
            mDownloadManager = downloadManager;
        }

        @Override
        protected Void doInBackground(Long... params) {
            int previousValue = 0;
            while(Preferences.getIsDownloadOnGoing(mContext)) {
                long mDownloadID = Preferences.getDownloadID(mContext);
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(mDownloadID);
                Cursor cursor = mDownloadManager.query(q);
                cursor.moveToFirst();
                try {
                    final int bytesDownloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytesInTotal = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        Preferences.setIsDownloadRunning(mContext, false);
                    }
                    final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);
                    if (progressPercent != previousValue) {
                        publishProgress(progressPercent, bytesDownloaded, bytesInTotal);
                        previousValue = progressPercent;
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    Preferences.setIsDownloadRunning(mContext, false);
                } catch (ArithmeticException e) {
                    Preferences.setIsDownloadRunning(mContext, false);
                }
                cursor.close();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            if (Preferences.getIsDownloadOnGoing(mContext)) {
                interactInterface.onDownloadProgressChanched(progress[0], progress[1], progress[2]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d(TAG+"_MD5", "Download finished, start checking MD5...");
            String OTAFile, MD5OTAServer, MD5OTALocal;
            OTAFile = RomUpdate.getFullFile(context).getAbsolutePath();
            MD5OTAServer = RomUpdate.getMd5(context).trim();
            MD5OTALocal = Tools.shell("md5sum " + OTAFile + " | cut -d ' ' -f 1", false).trim();

            Log.d(TAG+"_MD5", "Server MD5: "+MD5OTAServer+"\n\t\t Local MD5: "+MD5OTALocal);
            boolean result = MD5OTALocal.equalsIgnoreCase(MD5OTAServer);

            Log.d(TAG+"_MD5", "Result of checking: "+result);

            if(result) {
                interactInterface.onMD5Checked(result);
                Preferences.setDownloadFinished(context, true);
                Preferences.setMD5Passed(context, true);
                Preferences.setIsDownloadRunning(context,false);
                interactInterface.onDownloadFinished();
            }
        }
    }
////////////////////////////////////////////////DOWNLOAD PART END/////////////////////////////////////////
////////////////////////////////////////////////MD5 CHECK/////////////////////////////////////////////////



///////////////////////////////////////////////MD5 END/////////////////////////////////////////////////
////////////////////////////////////////////////MANIFEST CHECK//////////////////////////////////////////

   @SuppressLint("StaticFieldLeak")
   private class GetManifest extends AsyncTask<Void, Void, Void> implements Constants{
       public final String TAG = this.getClass().getSimpleName();

       private Context mContext;

       private static final String MANIFEST = "update_manifest.xml";

       private ProgressDialog mLoadingDialog;

       // Did this come from the BackgroundReceiver class?
       boolean shouldUpdateForegroundApp;

       GetManifest(Context context, boolean input) {
           mContext = context;
           shouldUpdateForegroundApp = input;
       }

       @Override
       protected void onPreExecute() {
           if (!shouldUpdateForegroundApp) {
               mLoadingDialog = new ProgressDialog(mContext);
               mLoadingDialog.setIndeterminate(true);
               mLoadingDialog.setCancelable(false);
               mLoadingDialog.setMessage(mContext.getResources().getString(R.string.loading));
               mLoadingDialog.show();
           }

           File manifest = new File(mContext.getFilesDir().getPath(), MANIFEST);
           if (manifest.exists()) {
               //noinspection ResultOfMethodCallIgnored
               manifest.delete();
           }
       }

       @Override
       protected Void doInBackground(Void... v) {

           try {
               InputStream input;
               URL url = new URL(Utils.getProp("ro.ota.manifest").trim());
               URLConnection connection = url.openConnection();
               connection.connect();
               input = new BufferedInputStream(url.openStream());
               OutputStream output = mContext.openFileOutput(
                       MANIFEST, Context.MODE_PRIVATE);
               byte[] data = new byte[1024];
               int count;
               while ((count = input.read(data)) != -1) {
                   output.write(data, 0, count);
               }
               output.flush();
               output.close();
               input.close();
               RomXmlParser parser = new RomXmlParser();
               parser.parse(new File(mContext.getFilesDir(), MANIFEST),
                       mContext);
           } catch (Exception e) {
               Log.d(TAG, "Exception: " + e.getMessage());
           }
           return null;
       }

       @Override
       protected void onPostExecute(Void result) {
           Intent intent;
           if (!shouldUpdateForegroundApp) {
               mLoadingDialog.cancel();
               intent = new Intent(MANIFEST_LOADED);
           } else {
               intent = new Intent(MANIFEST_CHECK_BACKGROUND);
           }

           mContext.sendBroadcast(intent);
           interactInterface.onManifestUpdated();
           super.onPostExecute(result);
       }
    }

////////////////////////////////////////////////MANIFEST END//////////////////////////////////////////















    private static InteractInterface interactInterface;
    public void serInteractListener(InteractInterface interactlistener){
        interactInterface = interactlistener;
    }
}
