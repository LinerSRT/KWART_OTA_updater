package com.ota.updates.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.stericson.RootTools.BuildConfig;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Utils{

	private static final int KILOBYTE = 1024;
	private static int KB = KILOBYTE;
	private static int MB = KB * KB;
	private static int GB = MB * KB;

	private static DecimalFormat decimalFormat = new DecimalFormat("##0.#");
	static {
		decimalFormat.setMaximumIntegerDigits(3);
		decimalFormat.setMaximumFractionDigits(1);
	}

	public static String shell(String cmd, boolean root) {
		String out = "";
		ArrayList<String> r = system(root ? getSuBin() : "sh",cmd).getStringArrayList("out");
		for(String l: r) {
			out += l+"\n";
		}
		return out;
	}

	private static String getSuBin() {
		if (new File("/system/xbin","su").exists()) {
			return "/system/xbin/su";
		}
		if (RootTools.isRootAvailable()) {
			return "su";
		}
		return "sh";
	}

	private static Bundle system(String shell, String command) {
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> err = new ArrayList<String>();
		boolean success = false;
		try {
			Process process = Runtime.getRuntime().exec(shell);
			DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
			BufferedReader STDOUT = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader STDERR = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			if (BuildConfig.DEBUG) Log.i(shell, command);
			STDIN.writeBytes(command + "\n");
			STDIN.flush();
			STDIN.writeBytes("exit\n");
			STDIN.flush();

			process.waitFor();
			if (process.exitValue() == 255) {
				if (BuildConfig.DEBUG) Log.e(shell,"SU was probably denied! Exit value is 255");
				err.add("SU was probably denied! Exit value is 255");
			}

			while (STDOUT.ready()) {
				String read = STDOUT.readLine();
				if (BuildConfig.DEBUG) Log.d(shell, read);
				res.add(read);
			}
			while (STDERR.ready()) {
				String read = STDERR.readLine();
				if (BuildConfig.DEBUG) Log.e(shell, read);
				err.add(read);
			}

			process.destroy();
			success = true;
			if (err.size() > 0) {
				success = false;
			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG) Log.e(shell,"IOException: "+e.getMessage());
			err.add("IOException: "+e.getMessage());
		} catch (InterruptedException e) {
			if (BuildConfig.DEBUG) Log.e(shell,"InterruptedException: "+e.getMessage());
			err.add("InterruptedException: "+e.getMessage());
		}
		if (BuildConfig.DEBUG) Log.d(shell,"END");
		Bundle r = new Bundle();
		r.putBoolean("success", success);
		r.putString("cmd", command);
		r.putString("binary", shell);
		r.putStringArrayList("out", res);
		r.putStringArrayList("error", err);
		return r;
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

	public static boolean isConnected(Context context) {
		boolean isConnected = false;
		ConnectivityManager cm =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if(activeNetwork != null) {
				isConnected = activeNetwork.isConnectedOrConnecting();
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

	public static void requestRoot(){
		try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("ls\n");
			os.writeBytes("sync\n");
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
