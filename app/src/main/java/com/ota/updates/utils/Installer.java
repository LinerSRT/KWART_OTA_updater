package com.ota.updates.utils;

import android.content.Context;
import android.os.PowerManager;

import com.ota.updates.RomUpdate;

import java.io.DataOutputStream;
import java.io.File;

import static com.ota.updates.utils.Constants.INSTALL_AFTER_FLASH_DIR;
import static com.ota.updates.utils.Constants.OTA_DOWNLOAD_DIR;

public class Installer {

    public static void flashFiles(Context context, String file, boolean backup, boolean wipeCache, boolean wipeData) {
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
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
