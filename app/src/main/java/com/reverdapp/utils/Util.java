package com.reverdapp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final String TAG = LogConfig.genLogTag("Util");

	public static boolean validateEmail(String email) {
		// ("[a-zA-Z]*[0-9]*@[a-zA-Z]*.[a-zA-Z]*");
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

		Matcher m = p.matcher(email);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * This function is used for keyboard hide
	 * 
	 **/
	public static void hideSoftKeyboard(Activity activity) {
		final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive()) {
			if (activity.getCurrentFocus() != null) {
				inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
			}
		}
	}

	/**
	 * check network is available or not
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		boolean isNetAvailable = false;
		if (context != null) {
			final ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (mConnectivityManager != null) {
				boolean mobileNetwork = false;
				boolean wifiNetwork = false;

				boolean mobileNetworkConnecetd = false;
				boolean wifiNetworkConnecetd = false;

				final NetworkInfo mobileInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				final NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (mobileInfo != null) {
					mobileNetwork = mobileInfo.isAvailable();
				}

				if (wifiInfo != null) {
					wifiNetwork = wifiInfo.isAvailable();
				}

				if (wifiNetwork || mobileNetwork) {
					if (mobileInfo != null)
						mobileNetworkConnecetd = mobileInfo.isConnectedOrConnecting();
					wifiNetworkConnecetd = wifiInfo.isConnectedOrConnecting();
				}

				isNetAvailable = (mobileNetworkConnecetd || wifiNetworkConnecetd);
			}
		}

		return isNetAvailable;
	}
	
	
	public static void displayDialog(String title, String msg, final Context context) {

		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setMessage(msg);
		alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public static String getUniqueDeviceId(Context context) {

		String deviceId = null;
		// 1 compute IMEI
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = TelephonyMgr.getDeviceId(); // Requires // READ_PHONE_STATE

		if (deviceId == null) {
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wm != null)
				deviceId = wm.getConnectionInfo().getMacAddress();
		}

		if (deviceId == null) {
			// 2 android ID - unreliable
			deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

		}

		if (deviceId == null) {
			// 3 compute DEVICE ID
			deviceId = "35"
					+ // we make this look like a valid IMEI
					Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
					+ Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10 + Build.USER.length() % 10; // 13
																																	// digits
		}
		return deviceId;
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean needPermissionForBlocking(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            Log.d(TAG, "Mode: " + mode);
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unknown name", e);
            return true;
        }
    }

	public static String ringerModeToString(final int mode)
	{
		final StringBuffer sb = new StringBuffer();
		switch (mode)
		{
			case AudioManager.RINGER_MODE_SILENT:
				sb.append("RINGER_MODE_SILENT");
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				sb.append("RINGER_MODE_VIBRATE");
				break;
			case AudioManager.RINGER_MODE_NORMAL:
				sb.append("RINGER_MODE_NORMAL");
				break;
			default:
				sb.append("UNKNOWN-RINGER_MODE");
				break;
		}

		return sb.toString();
	}

    // Get current timestamp in miliseconds.
	public static long getTimestamp() {
		return System.currentTimeMillis();

        //final Calendar c = Calendar.getInstance();
        //return c.getTime().getTime();
    }

    public static long daysBetween(final Long startDateStamp, final Long endDateStamp)
    {
        final Date startDate = new Date(startDateStamp);
        final Date endDate = new Date(endDateStamp);

        return daysBetween(startDate, endDate);
    }

    /**
     * Taken from: http://stackoverflow.com/questions/3838527/android-java-date-difference-in-days
     * This method assumes endDate >= startDate
     **/
    private static long daysBetween(final Date startDate, final Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private static Calendar getDatePart(final Date date){
        final Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    public static String dumpContents(final Bundle b) {
        StringBuffer sb = new StringBuffer();
        for (final String key : b.keySet()) {
            sb.append("key: ");
            sb.append(key);
            sb.append(" value: ");
            sb.append(b.get(key).toString());
            //sb.append(System.getProperty("line.seperator"));
            sb.append(" / ");
        }
        return sb.toString();
    }

    public static String generateAction(final Class c, final String action)
    {
        return c.getCanonicalName()+".ACTION."+action;
    }

    public static String generateParameter(final Class c, final String param)
    {
        return c.getCanonicalName()+".PARAM."+param;
    }
}
