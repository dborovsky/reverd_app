package com.reverdapp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.utils.LogConfig;

/**
 * Created by wojci on 4/14/15.
 */
public class BootUpReceiver extends BroadcastReceiver {
    private final String TAG = LogConfig.genLogTag("BootUpReceiver");

    @Override
    public void onReceive(final Context context, final Intent i) {
        final Intent intent = new Intent(context, LocalCheckCallService.class);
        context.startService(intent);
        Log.d(TAG, "started service");
    }
}
