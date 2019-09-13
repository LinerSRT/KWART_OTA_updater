package com.ota.updates.receivers;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.ota.updates.MainActivity;
import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.utils.InteractClass;
import com.ota.updates.utils.Preferences;
import com.ota.updates.utils.Utils;
import static com.ota.updates.utils.Config.*;

public class OTAReceivers extends BroadcastReceiver {



	public final static String TAG = "OTATAG";
	private InteractClass interactClass = MainActivity.getInteractClass();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		long mRomDownloadID = Preferences.getDownloadID(context);

		assert action != null;
		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			assert extras != null;
			long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
				if (id != mRomDownloadID) {
					return;
				}

				DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				DownloadManager.Query query = new DownloadManager.Query();
				query.setFilterById(id);
			assert downloadManager != null;
			Cursor cursor = downloadManager.query(query);
				if (!cursor.moveToFirst()) {
					return;
				}

				int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
					Preferences.setDownloadFinished(context, false);
					return;
				} else {
					Preferences.setDownloadFinished(context, true);
					return;
				}

		}

		if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {

			assert extras != null;
			long[] ids = extras.getLongArray(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);

			assert ids != null;
			for (long id : ids) {
				if (id != mRomDownloadID) {
					return;
				} else {
					Intent i = new Intent(context, MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
				}
			}
		}

		if (action.equals(MANIFEST_CHECK_BACKGROUND)) {
			boolean updateAvailable = RomUpdate.getUpdateAvailability(context);
			String filename = RomUpdate.getFilename(context);
			if (updateAvailable) {
				Utils.setupNotification(context, filename);
				Utils.scheduleNotification(context, !Preferences.getBackgroundService(context));
			}
		}

		if (action.equals(START_UPDATE_CHECK)) {
			if(interactClass != null)
				interactClass.updateManifest(false);
		}

		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			boolean backgroundCheck = Preferences.getBackgroundService(context);
			if (backgroundCheck) {
				Utils.scheduleNotification(context, !Preferences.getBackgroundService(context));
			}
		}

		if (action.equals(IGNORE_RELEASE)) {
			Preferences.setIgnoredRelease(context, RomUpdate.getVersionNumber(context));
			final NotificationManager mNotifyManager =
					(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Builder mBuilder = new NotificationCompat.Builder(context);
			mBuilder.setContentTitle(context.getString(R.string.main_release_ignored))
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
			assert mNotifyManager != null;
			mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

			Handler h = new Handler();
			long delayInMilliseconds = 1500;
			h.postDelayed(new Runnable() {

				public void run() {
					mNotifyManager.cancel(NOTIFICATION_ID);
				}}, delayInMilliseconds);
		}
	}
}


