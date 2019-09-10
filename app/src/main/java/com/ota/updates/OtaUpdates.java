package com.ota.updates;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.ota.updates.utils.LocaleUtility;

public class OtaUpdates extends Application {
	private static LocaleUtility localeUtility;

	@Override
	public void onCreate() {
		super.onCreate();

		localeUtility = new LocaleUtility(this, new Locale("en"));
		Locale locale = getResources().getConfiguration().locale;
		localeUtility.setLocale(locale);


	}



}
