package com.ota.updates.utils;



public class Config {
    public static boolean DEBUGGING 								= false;
    public static boolean DEBUG_NOTIFICATIONS 					    = false;
    public static boolean AMOLED_VERSION							= false;
    public static boolean CRYPTED_VERSION                           = true;
    public static String TAG										= "LINEROTA";
    public static String LANGUAGE = "lang";

    // Broadcast intents
    public static String MANIFEST_LOADED 							= "com.ota.update.MANIFEST_LOADED";
    public static String MANIFEST_CHECK_BACKGROUND 				    = "com.ota.update.MANIFEST_CHECK_BACKGROUND";
    public static String START_UPDATE_CHECK 						= "com.ota.update.START_UPDATE_CHECK";
    public static String IGNORE_RELEASE 							= "com.ota.update.IGNORE_RELEASE";

    //Notification
    public static int NOTIFICATION_ID 							    = 101;
    
}
