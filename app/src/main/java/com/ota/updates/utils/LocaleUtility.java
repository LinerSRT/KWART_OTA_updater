package com.ota.updates.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleUtility {
    private Locale locale;
    private Context context;

    public LocaleUtility(Context context, Locale locale){
        this.locale = locale;
        this.context = context;
    }

    public void setLocale(Locale localeIn) {
        locale = localeIn;
        if(locale != null) {
            Locale.setDefault(locale);
            setConfigChange(context);
        }
    }
    private void setConfigChange(Context context){
        if(locale != null){
            Locale.setDefault(locale);
            Configuration configuration = context.getResources().getConfiguration();
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            configuration.locale=locale;
            context.getResources().updateConfiguration(configuration, displayMetrics);
        }
    }
}
