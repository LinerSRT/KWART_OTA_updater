package com.ota.updates.OTAManager;

import android.content.Context;
import android.content.SharedPreferences;

public class OTAPreferenceManager {
    private static OTAPreferenceManager preferenceManager;
    private SharedPreferences sharedPreferences;

    static OTAPreferenceManager getInstance(Context context) {
        if (preferenceManager == null) {
            preferenceManager = new OTAPreferenceManager(context);
        }
        return preferenceManager;
    }

    private OTAPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("ApplicationData",Context.MODE_PRIVATE);
    }

    void saveString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    void saveInt(String key, int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    void saveFloat(String key, float value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putFloat(key, value);
        prefsEditor.apply();
    }

    void saveLong(String key, long value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putLong(key, value);
        prefsEditor.apply();
    }

    long getLong(String key, long defValue){
        return sharedPreferences.getLong(key, defValue);
    }

    String getString(String key, String defValue) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, defValue);
        } else {
            return "null";
        }
    }

    int getInt(String key, int defValue) {
        int int_value = 0;
        if (sharedPreferences!= null) {
            int_value = sharedPreferences.getInt(key, defValue);
        }
        return int_value;
    }
    public float getFloat(String key, float defValue) {
        float float_value = 0;
        if (sharedPreferences!= null) {
            float_value = sharedPreferences.getFloat(key, defValue);
        }
        return float_value;
    }

    boolean getBoolean(String key, boolean defValue) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(key, defValue);
        } else {
            return false;
        }
    }
}
