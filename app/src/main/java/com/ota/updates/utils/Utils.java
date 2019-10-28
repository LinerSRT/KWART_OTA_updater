package com.ota.updates.utils;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;


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


	public static int getAttrColor(Activity activity, int attrColor) {
		TypedValue themeBackgroundColor = new TypedValue();
		int parsedColor;

		if (activity.getTheme().resolveAttribute(attrColor,
				themeBackgroundColor, true)) {
			switch (themeBackgroundColor.type) {
				case TypedValue.TYPE_INT_COLOR_ARGB4:
					parsedColor = Color.argb(
							(themeBackgroundColor.data & 0xf000) >> 8,
							(themeBackgroundColor.data & 0xf00) >> 4,
							themeBackgroundColor.data & 0xf0,
							(themeBackgroundColor.data & 0xf) << 4);
					break;

				case TypedValue.TYPE_INT_COLOR_RGB4:
					parsedColor = Color.rgb(
							(themeBackgroundColor.data & 0xf00) >> 4,
							themeBackgroundColor.data & 0xf0,
							(themeBackgroundColor.data & 0xf) << 4);
					break;

				case TypedValue.TYPE_INT_COLOR_ARGB8:
					parsedColor = themeBackgroundColor.data;
					break;

				case TypedValue.TYPE_INT_COLOR_RGB8:
					parsedColor = Color.rgb(
							(themeBackgroundColor.data & 0xff0000) >> 16,
							(themeBackgroundColor.data & 0xff00) >> 8,
							themeBackgroundColor.data & 0xff);
					break;

				default:
					throw new RuntimeException("ClassName: couldn't parse theme " +
							"background color attribute " + themeBackgroundColor.toString());
			}
		} else {
			throw new RuntimeException("ClassName: couldn't find background color in " +
					"theme");
		}
		return parsedColor;
	}
}
