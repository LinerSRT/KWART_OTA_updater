/*
 * Copyright (C) 2015 Matt Booth (Kryten2k35).
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International 
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ota.updates.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.tasks.Changelog;
import com.ota.updates.tasks.LoadUpdateManifest;
import com.ota.updates.utils.Constants;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Utils;

public class MainActivity extends Activity implements Constants{

	public final String TAG = "OTATAG";
	private Context mContext;
	private Builder mCompatibilityDialog;
	View updateAvailable;
	View updateNotAvailable;
	public static ProgressBar mProgressBar;
	private TextView updateAvailableSummary,updateNotAvailableSummary,updateAvailableTitle,romVersion,romName;

	private boolean permissionGrant = false;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Objects.equals(intent.getAction(), MANIFEST_LOADED)) {
				updateRomInformation();
				updateRomUpdateLayouts();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ota_main);
		initViews();
		checkPermissions();

		
		boolean firstRun = Preferences.getFirstRun(mContext);				
		if(firstRun) {
			Preferences.setFirstRun(mContext, false);
			//showWhatsNew();
		}

		File installAfterFlashDir = new File(SD_CARD 
				+ File.separator
				+ OTA_DOWNLOAD_DIR
				+ File.separator
				+ INSTALL_AFTER_FLASH_DIR);
		installAfterFlashDir.mkdirs();

		createDialogs();

		// Check the correct build prop values are installed
		// Also executes the manifest/update check
		if (!Utils.isConnected(mContext)) {
			Builder notConnectedDialog = new Builder(mContext);
			notConnectedDialog.setTitle(R.string.main_not_connected_title)
			.setMessage(R.string.main_not_connected_message)
			.setPositiveButton(R.string.ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					((Activity) mContext).finish();
				}
			})
			.show();
		} else {
			new CompatibilityTask(mContext).execute();
		}

		// Has the download already completed?
		Utils.setHasFileDownloaded(mContext);

		updateRomInformation();
		updateRomUpdateLayouts();
	}



	private void initViews(){
		updateAvailable = findViewById(R.id.layout_main_update_available);
		updateNotAvailable = findViewById(R.id.layout_main_no_update_available);
		updateAvailableSummary = (TextView) findViewById(R.id.main_tv_update_available_summary);
		updateNotAvailableSummary = (TextView) findViewById(R.id.main_tv_no_update_available_summary);
		mProgressBar = (ProgressBar) findViewById(R.id.bar_main_progress_bar);
		updateAvailableTitle = (TextView) findViewById(R.id.main_tv_update_available_title);
		romVersion = (TextView) findViewById(R.id.tv_main_rom_version);
		romName = (TextView) findViewById(R.id.tv_main_rom_name);
	}


	private void checkPermissions(){
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				1);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode,	String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						permissionGrant = true;
				} else {
						permissionGrant = false;
				}
				return;
			}
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		this.registerReceiver(mReceiver, new IntentFilter(MANIFEST_LOADED));
	}

	@Override
	public void onStop() {
		super.onStop();
		this.unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}



	private void createDialogs() {
		// Compatibility Dialog
		mCompatibilityDialog = new AlertDialog.Builder(mContext);
		mCompatibilityDialog.setCancelable(false);
		mCompatibilityDialog.setTitle(R.string.main_not_compatible_title);
		mCompatibilityDialog.setMessage(R.string.main_not_compatible_message);
		mCompatibilityDialog.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
			}
		});
	}

	private void updateRomUpdateLayouts() {

		updateAvailable.setVisibility(View.GONE);
		updateNotAvailable.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);

		// Update is available
		if (RomUpdate.getUpdateAvailability(mContext) ||
                (!RomUpdate.getUpdateAvailability(mContext)) && Utils.isUpdateIgnored(mContext)) {
			updateAvailable.setVisibility(View.VISIBLE);

			if (Preferences.getDownloadFinished(mContext)) { //  Update already finished?
				updateAvailableTitle.setText(getResources().getString(R.string.main_update_finished));
				String htmlColorOpen  = "<font color='#33b5e5'>";

				String htmlColorClose = "</font>";
				String updateSummary = RomUpdate.getVersionName(mContext)
						+ "<br />"
						+ htmlColorOpen
						+ getResources().getString(R.string.main_download_completed_details)
						+ htmlColorClose;
				updateAvailableSummary.setText(Html.fromHtml(updateSummary));
			} else if (Preferences.getIsDownloadOnGoing(mContext)) {
				updateAvailableTitle.setText(getResources().getString(R.string.main_update_progress));
				mProgressBar.setVisibility(View.VISIBLE);
				String htmlColorOpen  = "<font color='#33b5e5'>";
				String htmlColorClose = "</font>";
				String updateSummary = htmlColorOpen
						+ getResources().getString(R.string.main_tap_to_view_progress)
						+ htmlColorClose;
				updateAvailableSummary.setText(Html.fromHtml(updateSummary));
			} else {
				updateAvailableTitle.setText(getResources().getString(R.string.main_update_available));
				String htmlColorOpen = "<font color='#33b5e5'>";
				String htmlColorClose = "</font>";
				String updateSummary = RomUpdate.getVersionName(mContext)
						+ "<br />"
						+ htmlColorOpen
						+ getResources().getString(R.string.main_tap_to_download)
						+ htmlColorClose;
				updateAvailableSummary.setText(Html.fromHtml(updateSummary));

			}
		} else {
			updateNotAvailable.setVisibility(View.VISIBLE);

			boolean is24 = DateFormat.is24HourFormat(mContext);
			Date now = new Date();
			Locale locale = Locale.getDefault();
			String time = "";

			if (is24) {
				time = new SimpleDateFormat("d, MMMM HH:mm", locale).format(now);
			} else {
				time = new SimpleDateFormat("d, MMMM hh:mm a", locale).format(now);
			}

			Preferences.setUpdateLastChecked(this, time);
			String lastChecked = getString(R.string.main_last_checked);
			updateNotAvailableSummary.setText(lastChecked + " " + time);
		}
	}
	




	private void updateRomInformation() {
		String htmlColorOpen  = "<font color='#33b5e5'>";
		String htmlColorClose = "</font>";

		//ROM name
		String romNameTitle = getApplicationContext().getResources().getString(R.string.main_rom_name) + " ";
		String romNameActual = Utils.getProp(OTA_ROMNAME);
		romName.setText(Html.fromHtml(romNameTitle + htmlColorOpen + romNameActual + htmlColorClose));

		//ROM version
		String romVersionTitle = getApplicationContext().getResources().getString(R.string.main_rom_version) + " ";
		String romVersionActual = Utils.getProp(OTA_VERSION);
		romVersion.setText(Html.fromHtml(romVersionTitle + htmlColorOpen + romVersionActual + htmlColorClose));

	}

	public void openCheckForUpdates(View v) {
		new LoadUpdateManifest(mContext, true).execute();
	}

	public void openDownload(View v) {
		Intent intent = new Intent(mContext, AvailableActivity.class);
		startActivity(intent);
	}
	


	public void openWebsitePage(View v) {
		String url = RomUpdate.getWebsite(mContext);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	public void openSettings(View v) {
		Intent intent = new Intent(mContext, SettingsActivity.class);
		startActivity(intent);
	}
	

	
	public static void updateProgress(int progress, int downloaded, int total, Activity activity) {
		if(mProgressBar != null) {
			mProgressBar.setProgress((int) progress);
		}
	}

	public class CompatibilityTask extends AsyncTask<Void, Boolean, Boolean> implements Constants{

		public final String TAG = this.getClass().getSimpleName();

		private Context mContext;
		private String mPropName;

		public CompatibilityTask(Context context) {
			mContext = context;
			mPropName = mContext.getResources().getString(R.string.prop_name);
		}

		@Override
		protected Boolean doInBackground(Void... v) {
			return Utils.doesPropExist(mPropName);
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				if (DEBUGGING)
					Log.d(TAG, "Prop found");
				new LoadUpdateManifest(mContext, true).execute();
			} else {
				if (DEBUGGING)
					Log.d(TAG, "Prop not found");
				try {
					mCompatibilityDialog.show();
				} catch(WindowManager.BadTokenException ex) {
					Log.e(TAG, ex.getMessage());
				}
			}
			super.onPostExecute(result);
		}
	}
}