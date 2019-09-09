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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.tasks.RomXmlParser;
import com.ota.updates.views.OTADialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.ota.updates.utils.Constants.MANIFEST_CHECK_BACKGROUND;
import static com.ota.updates.utils.Constants.MANIFEST_LOADED;
import static com.ota.updates.utils.Constants.OTA_DOWNLOAD_DIR;
import static com.ota.updates.utils.Constants.TAG;
import static com.ota.updates.utils.Constants.WIFI_ONLY;

public class InteractClass{
    //Booleans
    private boolean downloadFinished;
    private boolean isDownloadRunning;
    private boolean isMD5HasRun;
    private boolean isMD5Passed;

    //SubClasses
    private OTADialog installDialog;

    //Utils
    private Context context;
    private GetManifest getManifest;



    public InteractClass(Context context){
        this.context = context;
        downloadFinished = Preferences.getDownloadFinished(context);
        isDownloadRunning = Preferences.getIsDownloadOnGoing(context);
    }


    public void updateManifest(boolean isForegroundUpdate){
        getManifest = new GetManifest(context, isForegroundUpdate);
        getManifest.execute();
    }


    public void installOTA(){
        checkMD5Summ();
        isMD5HasRun = Preferences.getHasMD5Run(context);
        isMD5Passed = Preferences.getMD5Passed(context);
        if(downloadFinished && !isDownloadRunning && isMD5Passed){
            installDialog = new OTADialog(context, "Установить?",
                    RomUpdate.getChangelog(context),101,
                    context.getString(R.string.cancel), " ",
                    context.getString(R.string.install));
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
        } else {
            Log.d(TAG+"Installer", "Statements not passed! Downloaded = ["+downloadFinished+
                    "] MD5 Check pass = ["+isMD5Passed+"] Download now running = ["+isDownloadRunning+"]");
            Log.d(TAG+"Installer", "Skip installation!");
        }
    }


    public void downloadOTA(){
        if(!isDownloadRunning && !downloadFinished){
            String httpUrl = RomUpdate.getHttpUrl(context);
            String directUrl = RomUpdate.getDirectUrl(context);
            boolean isMobile = Utils.isMobileNetwork(context);
            boolean isSettingWiFiOnly = Preferences.getNetworkType(context).equals(WIFI_ONLY);
            boolean directUrlEmpty = directUrl.equals("null") || directUrl.isEmpty();
            boolean httpUrlEmpty = httpUrl.equals("null") || httpUrl.isEmpty();
            startDownload(context);

        }
    }

    public void cancelDownloadOTA(Context context) {
        long mDownloadID = Preferences.getDownloadID(context);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.remove(mDownloadID);
        Preferences.setIsDownloadRunning(context, false);
        interactInterface.onDownloadStopped();
    }

    public void deleteUpdate(){
        Utils.deleteFile(RomUpdate.getFullFile(context));
        Preferences.setHasMD5Run(context, false);
        Preferences.setDownloadFinished(context, false);
        interactInterface.onOTADeleted();
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
                    final int progressPercent = (int) ((bytesDownloaded * 100l) / bytesInTotal);
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
            if(checkMD5Summ()){
                Preferences.setDownloadFinished(context, true);
                Preferences.setMD5Passed(context, true);
            }
            interactInterface.onDownloadFinished();
        }
    }
////////////////////////////////////////////////DOWNLOAD PART END/////////////////////////////////////////
////////////////////////////////////////////////MD5 CHECK/////////////////////////////////////////////////

    private boolean checkMD5Summ(){
        String file = RomUpdate.getFullFile(context).getAbsolutePath();
        String md5Remote = RomUpdate.getMd5(context);
        String md5Local = Tools.shell("md5sum " + file + " | cut -d ' ' -f 1", false);
        md5Local = md5Local.trim();
        md5Remote = md5Remote.trim();
        boolean result = md5Local.equalsIgnoreCase(md5Remote);
        Preferences.setMD5Passed(context, result);
        interactInterface.onMD5Checked(result);
        Preferences.setMD5Passed(context,result);
        return result;
    }
////////////////////////////////////////////////MD5 END/////////////////////////////////////////////////
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
               manifest.delete();
           }
       }

       @Override
       protected Void doInBackground(Void... v) {

           try {
               InputStream input = null;

               URL url;
               if (DEBUGGING) {
                   url = new URL("https://romhut.com/roms/aosp-jf/ota.xml");
               } else {
                   url = new URL(Utils.getProp("ro.ota.manifest").trim());
               }
               URLConnection connection = url.openConnection();
               connection.connect();
               // download the file
               input = new BufferedInputStream(url.openStream());

               OutputStream output = mContext.openFileOutput(
                       MANIFEST, Context.MODE_PRIVATE);

               byte data[] = new byte[1024];
               int count;
               while ((count = input.read(data)) != -1) {
                   output.write(data, 0, count);
               }

               output.flush();
               output.close();
               input.close();

               // file finished downloading, parse it!
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
