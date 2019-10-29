package com.ota.updates.OTAManager;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;

import com.ota.updates.R;
import com.ota.updates.utils.Utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class OTAManager {
    private String TAG = "OTAManager";
    private Context context;
    private OTAItem otaItem;
    private OTAManagerInterface interfaceOTA;

    private String OTA_MANIFEST_URL;
    private String OTA_MANIFEST_NAME = "update_manifest.xml";
    private File OTA_MANIFEST_FILE;
    private String OTA_DIR = "OTA_packages";
    private String OTA_DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator+OTA_DIR+File.separator;


    public void setInterface(OTAManagerInterface otaManagerInterface){
        this.interfaceOTA = otaManagerInterface;
    }


    public OTAManager(Context context) {
        this.context = context;
        this.otaItem = OTAItem.getInstance(context);
        this.OTA_MANIFEST_FILE = new File(context.getFilesDir().getPath(),OTA_MANIFEST_NAME);
        this.OTA_MANIFEST_URL = "";
        if(getProp("ro.manifest.url").trim().contains("http")){
            OTA_MANIFEST_URL = getProp("ro.manifest.url").trim();
        } else {
            this.OTA_MANIFEST_URL = new String(Base64.decode(getProp("ro.manifest.url").trim(), Base64.DEFAULT), StandardCharsets.UTF_8);
        }
        createOTADirectory();
    }

    public void downloadOTA(){
        if(Utils.isConnected(context)) {
            String description = context.getResources().getString(R.string.downloading);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(otaItem.getOTADownloadURL()));
            request.setTitle(otaItem.getOTAFilename());
            request.setDescription(description);
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(OTA_DIR, otaItem.getOTAFilename());
            deleteAllOTA();
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            assert downloadManager != null;
            long downloadID = downloadManager.enqueue(request);
            otaItem.setDownloadID(downloadID);
            otaItem.setDownloadRunningStatus(true);
            otaItem.setMD5Status(false);
            interfaceOTA.onDownloadStarted();
            new DownloadProgress(downloadManager).execute(downloadID);
        } else {
            interfaceOTA.noInternet();
        }
    }

    public void cancelDownloadOTA() {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        assert downloadManager != null;
        downloadManager.remove(otaItem.getDownloadID());
        otaItem.setDownloadRunningStatus(false);
        otaItem.setDownloadFinishStatus(false);
        interfaceOTA.onDownloadStopped();
    }

    public void installOTA(final boolean backup, final boolean wipeCache, final boolean wipeData){
        flashFiles(context, OTA_DOWNLOAD_DIR+File.separator+otaItem.getOTAFilename(), backup, wipeCache, wipeData);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadProgress extends AsyncTask<Long, Integer, Void> {
        private DownloadManager mDownloadManager;

        DownloadProgress(DownloadManager downloadManager) {
            mDownloadManager = downloadManager;
        }

        @Override
        protected Void doInBackground(Long... params) {
            int previousValue = 0;
            while(otaItem.getDownloadRunningStatus()) {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(otaItem.getDownloadID());
                Cursor cursor = mDownloadManager.query(q);
                cursor.moveToFirst();
                try {
                    final int bytesDownloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytesInTotal = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        otaItem.setDownloadFinishStatus(true);
                        otaItem.setDownloadRunningStatus(false);
                        interfaceOTA.onDownloadFinished();
                    }
                    final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);
                    if (progressPercent != previousValue) {
                        publishProgress(progressPercent, bytesDownloaded, bytesInTotal);
                        previousValue = progressPercent;
                    }
                } catch (CursorIndexOutOfBoundsException | ArithmeticException e) {
                    otaItem.setDownloadRunningStatus(false);
                    otaItem.setDownloadFinishStatus(false);
                    interfaceOTA.onDownloadFailed();
                }
                cursor.close();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            if (otaItem.getDownloadRunningStatus()) {
                interfaceOTA.onDownloading(progress[0], Utils.formatDataFromBytes(progress[1]), Utils.formatDataFromBytes(progress[2]));
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String OTAFile, MD5OTAServer, MD5OTALocal;
            OTAFile = getOTAFile(otaItem.getOTAFilename()).getPath();
            MD5OTAServer = otaItem.getOTAMD5().trim();
            MD5OTALocal = Utils.shell("md5sum " + OTAFile + " | cut -d ' ' -f 1", false).trim();

            if(MD5OTALocal.equalsIgnoreCase(MD5OTAServer)) {
                interfaceOTA.MD5Status(true);
                otaItem.setMD5Status(true);
                otaItem.setDownloadRunningStatus(false);
                otaItem.setDownloadFinishStatus(true);
                interfaceOTA.onDownloadFinished();
            } else {
                interfaceOTA.MD5Status(false);
                otaItem.setMD5Status(false);
                otaItem.setDownloadRunningStatus(false);
                otaItem.setDownloadFinishStatus(false);
                interfaceOTA.onDownloadFailed();
            }
            Log.d(TAG, "MD5Status, file= "+OTAFile+" |MD5Server: "+MD5OTAServer+" MD5Local: "+MD5OTALocal);
        }
    }

    public void updateManifest(){
        if(Utils.isConnected(context)) {
            DownloadManifest downloadManifest = new DownloadManifest();
            downloadManifest.execute();
        } else {
            interfaceOTA.noInternet();
        }
    }

    public void reset(){
        deleteAllOTA();
        otaItem.setMD5Status(false);
        otaItem.setDownloadFinishStatus(false);
        otaItem.setDownloadRunningStatus(false);
        updateManifest();
    }

    private boolean isOTADirectoryExist(){
        return new File(OTA_DOWNLOAD_DIR).exists();
    }

    private void createOTADirectory(){
        if(!isOTADirectoryExist()){
            File otaDir = new File(OTA_DOWNLOAD_DIR);
            otaDir.mkdirs();
        }
    }

    public File getOTAFile(String otaName){
        return new File(OTA_DOWNLOAD_DIR+File.separator+otaName);
    }

    public void deleteOTA(String otaName){
        File otaFile = new File(OTA_DOWNLOAD_DIR+File.separator+otaName);
        if(otaFile.exists()){
            otaFile.delete();
        }
    }

    private void deleteAllOTA(){
        File otaDir = new File(OTA_DOWNLOAD_DIR);
        for(File otafile:otaDir.listFiles()){
            otafile.delete();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadManifest extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            if(OTA_MANIFEST_FILE.exists()){
                OTA_MANIFEST_FILE.delete();
            }
            interfaceOTA.onManifestDownloadStart();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                InputStream input;
                URL url = new URL(OTA_MANIFEST_URL);
                URLConnection connection = url.openConnection();
                connection.connect();
                input = new BufferedInputStream(url.openStream());
                OutputStream output = context.openFileOutput(OTA_MANIFEST_NAME, Context.MODE_PRIVATE);
                byte[] data = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                ManifestParser manifestParser = new ManifestParser();
                manifestParser.parseManifest(OTA_MANIFEST_FILE);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            generateDownloadUrl();
            interfaceOTA.onManifestDownloaded();
            otaItem.setUpdateAvailable(haveUpdate());
            interfaceOTA.updateAvailable(haveUpdate());
            Log.d(TAG, "OTA Available: "+otaItem.isOTAUpdateAvailable());
        }
    }
    private class ManifestParser extends DefaultHandler {
        private StringBuffer value = new StringBuffer();
        private boolean tagDeviceNameExist = false;
        private boolean tagOTAVersionExist = false;
        private boolean tagOTASubVersionExist = false;
        private boolean tagOTAExtraVersionExist = false;
        private boolean tagOTAUpdateDateExist = false;
        private boolean tagOTAFilenameExist = false;
        private boolean tagOTAFilesizeExist = false;
        private boolean tagOTAFileMD5Exist = false;
        private boolean tagOTAChangelogExist = false;

        void parseManifest(File manifest){
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(manifest, this);
            } catch (ParserConfigurationException | SAXException ex) {
                Log.e(TAG, "", ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value.setLength(0);
            if (attributes.getLength() > 0) {
                @SuppressWarnings("unused")
                String tag = "<" + qName;
                for (int i = 0; i < attributes.getLength(); i++) {

                    tag += " " + attributes.getLocalName(i) + "="
                            + attributes.getValue(i);
                }
                tag += ">";
            }

            if (qName.equalsIgnoreCase("device_name")) {
                tagDeviceNameExist = true;
            }

            if (qName.equalsIgnoreCase("ota_version")) {
                tagOTAVersionExist = true;
            }

            if (qName.equalsIgnoreCase("ota_subversion")) {
                tagOTASubVersionExist = true;
            }

            if (qName.equalsIgnoreCase("ota_extraversion")) {
                tagOTAExtraVersionExist = true;
            }

            if (qName.equalsIgnoreCase("update_date")) {
                tagOTAUpdateDateExist = true;
            }

            if (qName.equalsIgnoreCase("filename")) {
                tagOTAFilenameExist = true;
            }

            if (qName.equalsIgnoreCase("file_size")) {
                tagOTAFilesizeExist = true;
            }

            if (qName.equalsIgnoreCase("file_md5")) {
                tagOTAFileMD5Exist = true;
            }

            if (qName.equalsIgnoreCase("changelog")) {
                tagOTAChangelogExist = true;
            }

        }

        @Override
        public void characters(char[] buffer, int start, int length){
            value.append(buffer, start, length);
        }

        public void endElement(String uri, String localName, String qName){
            String input = value.toString().trim();

            if(tagDeviceNameExist){
                tagDeviceNameExist = false;
                otaItem.setOTADeviceName(input.toLowerCase());
                Log.d(TAG, "OTADeviceName" + " - "+input.toLowerCase());
            }

            if(tagOTAVersionExist){
                tagOTAVersionExist = false;
                otaItem.setOTAVersion(Integer.valueOf(input));
                Log.d(TAG, "OTAVersion" + " - "+input);
            }

            if(tagOTASubVersionExist){
                tagOTASubVersionExist = false;
                otaItem.setOTASubVersion(Integer.valueOf(input));
                Log.d(TAG, "OTASubVersion" + " - "+input);
            }

            if(tagOTAExtraVersionExist){
                tagOTAExtraVersionExist = false;
                otaItem.setOTAExtraVersion(Integer.valueOf(input));
                Log.d(TAG, "OTAExtraVersion" + " - "+input);
            }

            if(tagOTAUpdateDateExist){
                tagOTAUpdateDateExist = false;
                otaItem.setOTAUpdateDate(input);
                Log.d(TAG, "OTAUpdateDate" + " - "+input);
            }

            if(tagOTAFilenameExist){
                tagOTAFilenameExist = false;
                otaItem.setOTAFilename(input);
                Log.d(TAG, "OTAFileName" + " - "+input);
            }

            if(tagOTAFilesizeExist){
                tagOTAFilesizeExist = false;
                otaItem.setOTAFilesize(Integer.valueOf(input));
                Log.d(TAG, "OTAFilesize" + " - "+input);
            }

            if(tagOTAFileMD5Exist){
                tagOTAFileMD5Exist = false;
                otaItem.setOTAMD5(input);
                Log.d(TAG, "OTAMD5" + " - "+input);
            }

            if(tagOTAChangelogExist){
                tagOTAChangelogExist = false;
                otaItem.setOTAChangelog(input);
                Log.d(TAG, "OTAChangelog" + " - "+input);
            }
        }
    }

    private void generateDownloadUrl(){
        String SERVER_URL = "aHR0cDovLzE4LjIyMi4yMTAuMjE5";
        byte[] byteArray = Base64.decode(SERVER_URL, Base64.DEFAULT);
        String serverUrl = new String(byteArray, StandardCharsets.UTF_8);
        String downloadOTAUrl = serverUrl+File.separator+otaItem.getOTADeviceName().toLowerCase()+File.separator+"updates"+File.separator+otaItem.getOTAFilename();
        otaItem.setOTADownloadURL(downloadOTAUrl);
    }

    private boolean haveUpdate(){
        int systemVersion = Integer.parseInt(getProp("ro.ota.version"));
        int systemSubVersion = Integer.parseInt(getProp("ro.ota.subversion"));
        int systemExtraVersion = Integer.parseInt(getProp("ro.ota.extraversion"));
        int manifestVersion = otaItem.getOTAVersion();
        int manifestSubVersion = otaItem.getOTASubVersion();
        int manifestExtraVersion = otaItem.getOTAExtraVersion();
        return manifestVersion > systemVersion || manifestSubVersion > systemSubVersion || manifestExtraVersion > systemExtraVersion;
    }
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

    private String getProp(String propName) {
        Process p;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line=br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getSystemVersion(){
        return getProp("ro.ota.version")+"."+getProp("ro.ota.subversion")+"."+getProp("ro.ota.extraversion");
    }
    public String getManifestVersion(){
        return otaItem.getOTAVersion()+"."+otaItem.getOTASubVersion()+"."+otaItem.getOTAExtraVersion();
    }

    public String getDeviceName(){
        return getProp("ro.ota.device").trim();
    }
}
