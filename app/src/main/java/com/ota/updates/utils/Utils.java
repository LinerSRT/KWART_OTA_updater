package com.ota.updates.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ota.updates.MainActivity;
import com.ota.updates.R;
import com.ota.updates.RomUpdate;
import com.ota.updates.receivers.OTAReceivers;

public class Utils implements Constants{
	private Context context;

	private static final int KILOBYTE = 1024;
	private static int KB = KILOBYTE;
	private static int MB = KB * KB;
	private static int GB = MB * KB;

	private static DecimalFormat decimalFormat = new DecimalFormat("##0.#");
	static {
		decimalFormat.setMaximumIntegerDigits(3);
		decimalFormat.setMaximumFractionDigits(1);
	}

	public Utils(Context context){
		this.context = context;
	}

	public static Boolean doesPropExist(String propName) {
		boolean valid = false;
		
		if (DEBUGGING) {
			return true;
		}

		try {
			Process process = Runtime.getRuntime().exec("getprop");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				if (line.contains("[" + propName +"]")) {
					valid = true;
				}
			}
			bufferedReader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return valid;
	}

	public static String getProp(String propName) {
		Process p = null;
		String result = "";
		try {
			p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line=br.readLine()) != null) {
				result = line;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String formatDataFromBytes(long size) {

		String symbol;
		KB = KILOBYTE;
		symbol = "B";
		if (size < KB) {
			return decimalFormat.format(size) + symbol;
		} else if (size < MB) {
			return decimalFormat.format(size / (float)KB) + 'k' + symbol;
		} else if (size < GB) {
			return decimalFormat.format(size / (float)MB) + 'M' + symbol;
		}
		return decimalFormat.format(size / (float)GB) + 'G' + symbol;
	}

	public static void deleteFile(File file) {
		Tools.shell("rm -f " + file.getAbsolutePath(), false);
	}

	public static void setHasFileDownloaded(Context context) {
		File file = RomUpdate.getFullFile(context);
		int filesize = RomUpdate.getFileSize(context);
		//boolean downloadIsRunning = Preferences.getIsDownloadOnGoing(context);

		boolean status = false;
		if (DEBUGGING) {
			Log.d(TAG, "Local file " + file.getAbsolutePath());
			Log.d(TAG, "Local filesize " + file.length());
			Log.d(TAG, "Remote filesize " + filesize);
		}
		if (file.length() != 0 && file.length() == filesize && !Preferences.getIsDownloadOnGoing(context)) {
			status = true;
		}
		Preferences.setDownloadFinished(context, status);
	}

	public static void setBackgroundCheck(Context context, boolean set) {
		scheduleNotification(context, !set);
	}
	
	public static void scheduleNotification(Context context, boolean cancel) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, OTAReceivers.class);
		intent.setAction(START_UPDATE_CHECK);
		int intentId = 1673;
		int intentFlag = PendingIntent.FLAG_UPDATE_CURRENT;
		
		if (cancel) {
			if (alarmManager != null) {
				if (DEBUGGING)
					Log.d(TAG, "Cancelling alarm");
				alarmManager.cancel(PendingIntent.getBroadcast(
						context, 
						intentId, 
						intent, 
						intentFlag));
			}
		} else {
			int requestedInterval;
			
			if (DEBUG_NOTIFICATIONS) {
				requestedInterval = 30000;
			} else {
				requestedInterval = Preferences.getBackgroundFrequency(context);
			}
			
			if (DEBUGGING)
				Log.d(TAG, "Setting alarm for " + requestedInterval + " seconds");
			Calendar calendar = Calendar.getInstance();
			long time = calendar.getTimeInMillis() + requestedInterval * 1000;
			alarmManager.set(
					AlarmManager.RTC_WAKEUP, 
					time, 
					PendingIntent.getBroadcast(
							context, 
							intentId, 
							intent, 
							intentFlag));
		}
	}


	public static boolean isConnected(Context context) {
		boolean isConnected = false;
		ConnectivityManager cm =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if(activeNetwork != null) {
				isConnected = activeNetwork != null &&
						activeNetwork.isConnectedOrConnecting();
			}
		}
		return isConnected;
	}

	public static boolean isMobileNetwork(Context context) {
		boolean isMobileNetwork = false;
		ConnectivityManager cm =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if(activeNetwork != null) {
				isMobileNetwork = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
			}
		}
		return isMobileNetwork;
	}

	public static boolean isLollipop() {	
		return Build.VERSION.SDK_INT >= 21;
	}

    public static boolean isUpdateIgnored(Context context) {
        String manifestVer = RomUpdate.getVersionNumber(context);
        return Preferences.getIgnoredRelease(context).matches(manifestVer);
    }

    private static boolean compareVersion(String current, String manifest){
		String[] currentDevice = current.split("\\.", 2);
		String[] currentManifest = manifest.split("\\.", 2);
		int sumCurrent = 0;
		int sumManifest = 0;
        for (String c : currentDevice) {
            sumCurrent += Integer.parseInt(c);
        }
        for (String m : currentManifest) {
            sumManifest += Integer.parseInt(m);
        }
		return sumManifest > sumCurrent;
	}

	public static void setUpdateAvailability(Context context) {
		String currentVer = Utils.getProp("ro.ota.version");
		String manifestVer = RomUpdate.getVersionNumber(context);
        boolean available;
        if (Preferences.getIgnoredRelease(context).matches(manifestVer)) {
            available = false;
        } else {
            available =	compareVersion(currentVer, manifestVer);
        }

		RomUpdate.setUpdateAvailable(context, available);
		if (DEBUGGING)
			Log.d(TAG, "Update Availability is " + available);
	}
	
	public static void setupNotification(Context context, String filename) {
		if (DEBUGGING)
			Log.d(TAG, "Showing notification");	
		
		NotificationManager mNotifyManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		
		Builder mBuilder = new NotificationCompat.Builder(context);
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
        Intent skipIntent = new Intent(context, OTAReceivers.class);
        skipIntent.setAction(IGNORE_RELEASE);
        Intent downloadIntent = new Intent(context, com.ota.updates.MainActivity.class);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(context, 0, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent downloadPendingIntent = PendingIntent.getActivity(context, 0, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle(context.getString(R.string.update_available))
		.setContentText(filename)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentIntent(resultPendingIntent)
		.setAutoCancel(true)
		.setPriority(NotificationCompat.PRIORITY_HIGH)
		.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
		.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
		.setSound(Uri.parse(Preferences.getNotificationSound(context)))
		.addAction(R.drawable.ic_check, context.getString(R.string.download), downloadPendingIntent)
        .addAction(R.drawable.ic_update_not_available, context.getString(R.string.ignore), skipPendingIntent);

		if (Preferences.getNotificationVibrate(context)) {
			mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
		}

		mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	public static boolean isPackageInstalled(String packagename, Context context) {
	    PackageManager pm = context.getPackageManager();
	    try {
	        pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
	        return true;
	    } catch (NameNotFoundException e) {
	        return false;
	    }
	}
	
	public static String getRemovableMediaPath() {
		return Tools.shell("echo ${SECONDARY_STORAGE%%:*}", false);		
	}
}
