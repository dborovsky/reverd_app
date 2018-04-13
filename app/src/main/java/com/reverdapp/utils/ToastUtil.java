package com.reverdapp.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wojci on 6/1/15.
 */
public final class ToastUtil {
    private static final String TAG = LogConfig.genLogTag("ToastUtil");

    public static void showToast(final Context context, final String message)
    {
        if (message == null) {
            Log.w(TAG, "Empty message (null)");
            return;
        }

        if (message.length() <= 0) {
            Log.w(TAG, "Empty message (length)");
            return;
        }

        Log.d(TAG, "Showing toast with message: '" + message + "'");
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
