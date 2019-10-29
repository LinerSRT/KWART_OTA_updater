package com.ota.updates.OTAManager;

import android.app.Application;

import com.ota.updates.utils.LocaleUtility;

import java.util.Locale;

public class OtaUpdates extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		LocaleUtility localeUtility = new LocaleUtility(this, new Locale("en"));
		Locale locale = getResources().getConfiguration().locale;
		localeUtility.setLocale(locale);
	}



}
