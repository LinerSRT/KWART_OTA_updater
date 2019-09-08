package com.ota.updates.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager{
    private static PreferenceManager preferenceManager;
    private SharedPreferences sharedPreferences;

    public static PreferenceManager getInstance(Context context) {
        if (preferenceManager == null) {
            preferenceManager = new PreferenceManager(context);
        }
        return preferenceManager;
    }

    private PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("UpdateData",Context.MODE_PRIVATE);
    }

    public void saveString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public void saveInt(String key, int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    public void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public void saveFloat(String key, float value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putFloat(key, value);
        prefsEditor.apply();
    }

    public String getString(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, "null");
        } else {
            return "null";
        }
    }

    public int getInt(String key, int defvalue) {
        int int_value = 0;
        if (sharedPreferences!= null) {
            int_value = sharedPreferences.getInt(key, defvalue);
        }
        return int_value;
    }
    public float getFloat(String key, float defvalue) {
        float float_value = 0;
        if (sharedPreferences!= null) {
            float_value = sharedPreferences.getFloat(key, defvalue);
        }
        return float_value;
    }

    public boolean getBoolean(String key, boolean defvalue) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(key, defvalue);
        } else {
            return false;
        }
    }
}