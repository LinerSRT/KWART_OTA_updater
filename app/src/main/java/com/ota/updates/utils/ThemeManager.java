package com.ota.updates.utils;

import android.app.Activity;

import com.ota.updates.R;

public class ThemeManager {


    public static void initTheme(Activity activity){
        switch (android.provider.Settings.Global.getInt(activity.getContentResolver(), "system_theme", 1)) {
            case 0:
                activity.setTheme(R.style.AppTheme);
                break;
            case 1:
                activity.setTheme(R.style.BlueDeepTheme);
                break;
            case 2:
                activity.setTheme(R.style.RedDeepTheme);
                break;
            case 3:
                activity.setTheme(R.style.GreenDeepTheme);
                break;
            case 4:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
    }
}
