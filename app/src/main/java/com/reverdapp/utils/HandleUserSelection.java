package com.reverdapp.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by alfredgeorge on 1/1/15.
 */
public class HandleUserSelection {
    private static final String TAG = LogConfig.genLogTag("HandleUserSelection");

    public static void rejectIncomingCall(Context context,String phoneNumber) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ITelephony telephonyService;
        try {
            Class<?> c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(tm);

            Log.d(TAG, phoneNumber);
            if ((phoneNumber != null)) {
                telephonyService.endCall();
                Log.d(TAG, "End call: " + phoneNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
