package com.ota.updates.utils;

import android.os.Environment;

public class Config {
    public static boolean DEBUGGING 								= true;
    public static boolean DEBUG_NOTIFICATIONS 					    = false;
    public static boolean AMOLED_VERSION							= true;
    public static String TAG										= "LINEROTA";
    public static String LANGUAGE = "lang";




    public static String OTA_VERSION 								= "ro.ota.version";
    public static  String OTA_MANIFEST 							    = "ro.ota.manifest";
    public static String OTA_DOWNLOAD_LOC							= "ro.ota.download_loc";
    public static String OTA_DEFAULT_THEME						    = "ro.ota.default_theme";

    // Storage
    public static String SD_CARD 									= Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String OTA_DOWNLOAD_DIR 						    = Utils.doesPropExist(OTA_DOWNLOAD_LOC) ? Utils.getProp(OTA_DOWNLOAD_LOC) : "OTAUpdates";
    public static String INSTALL_AFTER_FLASH_DIR 					= "InstallAfterFlash";

    // Networks
    public static String WIFI_ONLY 								    = "2";
    public static String WIFI_OR_MOBILE 							= "1";

    // Theme
    public static String THEME_LIGHT 								= "0";
    public static String THEME_LIGHT_DARKACTIONBAR 				    = "1";
    public static String THEME_DARK 								= "2";

    // Settings
    public static String CURRENT_THEME 							    = "current_theme";
    public static String LAST_CHECKED 							    = "updater_last_update_check";
    public static String IS_DOWNLOAD_FINISHED 					    = "is_download_finished";
    public static String DELETE_AFTER_INSTALL 					    = "delete_after_install";
    public static String INSTALL_PREFS 							    = "install_prefs";
    public static String WIPE_DATA 								    = "wipe_data";
    public static String WIPE_CACHE 								= "wipe_cache";
    public static String WIPE_DALVIK 								= "wipe_dalvik";
    public static String MD5_PASSED 								= "md5_passed";
    public static String MD5_RUN 									= "md5_run";
    public static String DOWNLOAD_RUNNING		 					= "download_running";
    public static String NETWORK_TYPE 							    = "network_type";
    public static String DOWNLOAD_ID 								= "download_id";
    public static String UPDATER_BACK_SERVICE 					    = "background_service";
    public static String UPDATER_BACK_FREQ 						    = "background_frequency";
    public static String UPDATER_ENABLE_ORS 						= "updater_twrp_ors";
    public static String MOVE_TO_EXT_SD 							= "move_to_ext_sd";
    public static String NOTIFICATIONS_SOUND 						= "notifications_sound";
    public static String NOTIFICATIONS_VIBRATE 					    = "notifications_vibrate";
    public static String IGNORE_RELEASE_VERSION 					= "ignored_release";
    public static String ADS_ENABLED 								= "ads_enabled";
    public static String OLD_CHANGELOG 							    = "old_changelog";
    public static String FIRST_RUN 								    = "first_run";
    public static String ABOUT_ACTIVITY_PREF						= "about_activity_pref";
    public static String IS_PRO									    = "is_pro";
    public static String ABOUT_PREF_PRO 							= "about_pro";
    public static String STORAGE_LOCATION 						    = "updater_storage_location";

    // Broadcast intents
    public static String MANIFEST_LOADED 							= "com.ota.update.MANIFEST_LOADED";
    public static String MANIFEST_CHECK_BACKGROUND 				    = "com.ota.update.MANIFEST_CHECK_BACKGROUND";
    public static String START_UPDATE_CHECK 						= "com.ota.update.START_UPDATE_CHECK";
    public static String IGNORE_RELEASE 							= "com.ota.update.IGNORE_RELEASE";

    //Notification
    public static int NOTIFICATION_ID 							    = 101;
    
}
