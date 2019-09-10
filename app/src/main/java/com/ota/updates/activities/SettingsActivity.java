
package com.ota.updates.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import com.ota.updates.MainActivity;
import com.ota.updates.R;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Tools;
import com.ota.updates.utils.Utils;

@SuppressLint({"SdCardPath", "ExportedPreferenceActivity"})
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, OnSharedPreferenceChangeListener, Constants{
    public final static String TAG = "OTATAG";
	private Context mContext;
	private Builder mInstallPrefsDialog;
	private RingtonePreference mRingtonePreference;
	private SparseBooleanArray mInstallPrefsItems = new SparseBooleanArray();

	@SuppressLint("NewApi") @Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Preferences.PREF_NAME);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		addPreferencesFromResource(R.xml.preferences);


		mRingtonePreference = (RingtonePreference) findPreference(NOTIFICATIONS_SOUND);


		String defValue = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString();
		String soundValue = getPreferenceManager().getSharedPreferences().getString(NOTIFICATIONS_SOUND, defValue);
		//setRingtoneSummary(soundValue);

		if (!Tools.isRootAvailable()) {
			SwitchPreference ors = (SwitchPreference) findPreference("updater_twrp_ors");
			ors.setEnabled(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		mRingtonePreference.setOnPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());

			if (key.equals(CURRENT_THEME)) {
				Preferences.setTheme(mContext, listPref.getValue());
				Intent intent = new Intent(mContext, MainActivity.class);
				startActivity(intent);
			} else if (key.equals(UPDATER_BACK_FREQ)) {
				Utils.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
			}
		} else if (pref instanceof SwitchPreference) {
			if (key.equals(UPDATER_BACK_SERVICE)) {
				Utils.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
			}
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean result = false;
		if (preference == mRingtonePreference) {
			//setRingtoneSummary((String)newValue);
			result = true;
		}
		return result;
	}


}