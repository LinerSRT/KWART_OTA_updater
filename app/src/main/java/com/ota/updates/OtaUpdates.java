package com.ota.updates;

import java.util.Locale;

import android.app.Application;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ota.updates.utils.Config;
import com.ota.updates.utils.LocaleUtility;

public class OtaUpdates extends Application{
	private static LocaleUtility localeUtility;

	@Override
	public void onCreate() {
		super.onCreate();
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		int height = displayMetrics.heightPixels;
		int width = displayMetrics.widthPixels;
		if(height < 400 && width < 400){
			Config.AMOLED_VERSION = false;
		} else {
			Config.AMOLED_VERSION = true;
		}

		localeUtility = new LocaleUtility(this, new Locale("en"));
		Locale locale = getResources().getConfiguration().locale;
		localeUtility.setLocale(locale);




	}



}
